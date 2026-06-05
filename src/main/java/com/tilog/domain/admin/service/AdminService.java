package com.tilog.domain.admin.service;

import com.tilog.domain.admin.dto.*;
import com.tilog.domain.comment.entity.TilComment;
import com.tilog.domain.comment.repository.TilCommentRepository;
import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.member.entity.MemberSanction;
import com.tilog.domain.member.repository.MemberSanctionRepository;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.repository.TilPostRepository;
import com.tilog.domain.report.entity.Report;
import com.tilog.domain.report.entity.ReportProcess;
import com.tilog.domain.report.entity.TargetAction;
import com.tilog.domain.report.entity.TargetType;
import com.tilog.domain.report.repository.ReportProcessRepository;
import com.tilog.domain.report.repository.ReportRepository;
import com.tilog.domain.report.dto.ReportResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;
    private final TilPostRepository tilPostRepository;
    private final ReportRepository reportRepository;
    private final TilCommentRepository tilCommentRepository;
    private final MemberSanctionRepository memberSanctionRepository;
    private final ReportProcessRepository reportProcessRepository;

    public Page<AdminMemberListResponse> getMemberList(Pageable pageable) {  // 회원 목록 페이징 조회
        Page<Member> memberData = memberRepository.findAll(pageable);

        Page<AdminMemberListResponse> memberList =
                memberData.map(member -> new AdminMemberListResponse(
                        member.getId(),
                        member.getEmail(),
                        member.getNickname(),
                        member.getRole().name(),
                        member.isBanned(),
                        member.getCreatedAt()
                ));

        return memberList;
    }

    public AdminMemberDetailResponse getMemberDetail(Long memberId) {    // 특정 회원 상세 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다."));

        AdminMemberDetailResponse memberDetail = new AdminMemberDetailResponse(member.getId(), member.getEmail(), member.getNickname(), member.getRole().name(), member.isBanned(), member.getBannedUntil(), member.getCreatedAt());

        return memberDetail;
    }

    @Transactional
    public void changeMemberRole(MemberRoleChangeRequest request) {  // 회원 권한 변경
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다."));

        MemberRole role = MemberRole.valueOf(request.getRole());
        member.changeRole(role);
    }

    @Transactional
    public void forceDeletePost(Long postId) {  // 게시글 강제 삭제(숨김 처리)
        Post tilPost = tilPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 정보가 존재하지 않습니다."));

        tilPost.delete();
    }

    @Transactional
    public void doSanction(Long reportId, MemberSanctionRequestDto memberSanctionRequestDto){   // 신고 제재
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 정보가 존재하지 않습니다."));

        if (report.getStatus() == Status.PROCESSED) {
            throw new IllegalStateException("이미 처리가 완료된 신고입니다.");
        }

        Member reportedMember = null;

        if(report.getTargetType() == TargetType.TIL_POST){
            Post post = tilPostRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글 정보가 존재하지 않습니다."));

            reportedMember = post.getMember();

        } else if(report.getTargetType() == TargetType.TIL_COMMENT){
            TilComment comment = tilCommentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("코멘트 정보가 존재하지 않습니다."));

           reportedMember = comment.getMember();
        } else {
            throw new IllegalArgumentException("알 수 없는 신고 대상입니다.");
        }

        Member admin = memberRepository.findById(memberSanctionRequestDto.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        report.completeReport();

        ReportProcess process = ReportProcess.builder()
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .admin(admin)
                .processContent("회원 제재 완료: " + memberSanctionRequestDto.getContent())
                .targetAction(TargetAction.HIDE)
                .build();
        reportProcessRepository.save(process);

        // 4. 회원 제재 기록 저장
        MemberSanction memberSanction = MemberSanction.builder()
                .member(reportedMember)
                .admin(admin)
                .process(process) // 🔥 process와 연결!
                .sanctionType(memberSanctionRequestDto.getSanctionType())
                .reasonType(memberSanctionRequestDto.getReasonType())
                .content(memberSanctionRequestDto.getContent())
                .createdAt(LocalDateTime.now())
                .startAt(LocalDateTime.now())
                .build();

        memberSanctionRepository.save(memberSanction);
    }

    public List<ReportResponseDto> getRecentReports() {     // 최근 신고 4건
        return reportRepository.findTop4ByOrderByCreatedAtDesc()
                .stream()
                .map(report -> {
                    // 1. 신고자 닉네임 찾기
                    String reporterName = memberRepository.findById(report.getReportId())
                            .map(Member::getNickname)
                            .orElse("알 수 없음");

                    // 2. 피신고자(신고 당한 사람) 닉네임 찾기
                    String reportedName = "알 수 없음";
                    if (report.getTargetType() == TargetType.TIL_POST) {
                        reportedName = tilPostRepository.findById(report.getTargetId())
                                .map(post -> post.getMember().getNickname())
                                .orElse("삭제된 글/회원");
                    } else if (report.getTargetType() == TargetType.TIL_COMMENT) {
                        reportedName = tilCommentRepository.findById(report.getTargetId())
                                .map(comment -> comment.getMember().getNickname())
                                .orElse("삭제된 댓글/회원");
                    }

                    // 3. DTO로 묶어서 반환 (record의 of 메서드 사용)
                    return ReportResponseDto.of(report, reporterName, reportedName);
                })
                .toList();
    }

    public Map<String, Long> getReportStatistics() {
        long pending = reportRepository.countByStatus(Status.PENDING);
        long processed = reportProcessRepository.count();

        return Map.of("pending", pending, "processed", processed);
    }
}
