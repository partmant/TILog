package com.tilog.domain.admin.service;

import com.tilog.domain.admin.dto.MemberRoleChangeRequest;
import com.tilog.domain.admin.dto.AdminMemberDetailResponse;
import com.tilog.domain.admin.dto.AdminMemberListResponse;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.repository.TilPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {
    private final MemberRepository memberRepository;
    private final TilPostRepository tilPostRepository;

    public AdminService(MemberRepository memberRepository, TilPostRepository tilPostRepository) {
        this.memberRepository = memberRepository;
        this.tilPostRepository = tilPostRepository;
    }

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
}
