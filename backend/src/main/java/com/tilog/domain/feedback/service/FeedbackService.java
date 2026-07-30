package com.tilog.domain.feedback.service;

import com.tilog.domain.feedback.dto.FeedbackListResponseDto;
import com.tilog.domain.feedback.dto.FeedbackRequestDtoRequest;
import com.tilog.domain.feedback.dto.FeedbackWriteRequestDto;
import com.tilog.domain.feedback.dto.FeedbackDetailResponseDto;
import com.tilog.domain.feedback.entity.MentorFeedback;
import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.notification.entity.NotificationType;
import com.tilog.domain.notification.service.NotificationService;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.feedback.repository.MentorFeedbackRepository;
import com.tilog.domain.post.repository.TilPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {
    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final MemberRepository memberRepository;
    private final TilPostRepository tilPostRepository;
    private final NotificationService notificationService;

    public void requestFeedback(FeedbackRequestDtoRequest feedbackRequestDtoRequest) {   // 피드백 요청
        Member member = memberRepository.findById(feedbackRequestDtoRequest.getRequestorId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (member.getRole() != MemberRole.PREMIUM) {
            throw new IllegalArgumentException("현재 회원 Role로는 접근할 수 없습니다.");
        }

        Post tilpost = tilPostRepository.findById(feedbackRequestDtoRequest.getTilId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 정보를 찾을 수 없습니다."));

        if (tilpost.getIsDeleted()) {
            throw new IllegalArgumentException("게시글 정보를 찾을 수 없습니다.");
        }

        Member mentor = memberRepository.findById(feedbackRequestDtoRequest.getMentorId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (mentor.getRole() != MemberRole.MENTOR) {
            throw new IllegalArgumentException("현재 회원 Role로는 접근할 수 없습니다.");
        }

        MentorFeedback mentorFeedback = new MentorFeedback(tilpost, member, mentor);
        mentorFeedbackRepository.save(mentorFeedback);
    }

    public void writeFeedback(Long feedbackId, FeedbackWriteRequestDto feedbackWriteRequestDto){    // 피드백 정보 업데이트
        MentorFeedback findFeedback = mentorFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백 정보를 찾을 수 없습니다."));

        if(findFeedback.getStatus() == Status.COMPLETED){
            throw new IllegalArgumentException("이미 완료된 피드백입니다.");
        }

        if(!feedbackWriteRequestDto.getMentorId().equals(findFeedback.getMentor().getId())){
            throw new IllegalArgumentException("해당 피드백의 담당 멘토가 아닙니다.");
        }

        findFeedback.updateFeedback(feedbackWriteRequestDto.getTechnicalScore(),
                feedbackWriteRequestDto.getFlowScore(),
                feedbackWriteRequestDto.getDesignScore(),
                feedbackWriteRequestDto.getComment());

        Long receiverId = findFeedback.getRequestor().getId(); // 알림을 받을 일반 유저 ID
        Long senderId = findFeedback.getMentor().getId();      // 알림을 보내는 멘토 ID
        Long relatedEntityId = findFeedback.getTil().getId();  // 어떤 게시글에 대한 피드백인지

        notificationService.send(
                receiverId,
                senderId,
                NotificationType.FEEDBACK,
                relatedEntityId,
                "TIL" // 관련된 엔티티 타입 (프론트엔드 라우팅용)
        );


        findFeedback.updateFeedback(feedbackWriteRequestDto.getTechnicalScore(), feedbackWriteRequestDto.getFlowScore(), feedbackWriteRequestDto.getDesignScore(), feedbackWriteRequestDto.getComment());
    }

    @Transactional(readOnly = true)
    public FeedbackDetailResponseDto getFeedbackDetail(Long feedbackId){    // 피드백 상세 조회
        MentorFeedback findFeedback = mentorFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백 정보를 찾을 수 없습니다."));

        FeedbackDetailResponseDto feedbackDetail = new FeedbackDetailResponseDto(findFeedback.getFeedbackId(), findFeedback.getTil().getId(), findFeedback.getMentor().getId(), findFeedback.getStatus(), findFeedback.getTechnicalScore(), findFeedback.getFlowScore(), findFeedback.getDesignScore(), findFeedback.getComment(), findFeedback.getRequestedAt(), findFeedback.getCompletedAt());

        return feedbackDetail;
    }

    @Transactional(readOnly = true)
    public List<FeedbackListResponseDto> getFeedbackList(Long loginMemberId, MemberRole role) { // 피드백 목록 가져오기
        List<FeedbackListResponseDto> result = new java.util.ArrayList<>();

        // 1. 내가 '요청한' 피드백 목록 (일반/프리미엄 모두 확인 가능)
        List<MentorFeedback> requestedList = mentorFeedbackRepository.findByRequestor_Id(loginMemberId);
        result.addAll(requestedList.stream()
                .map(fb -> FeedbackListResponseDto.from(fb, "REQUESTED"))
                .toList());

        // 2. 만약 멘토라면, 내가 '받은' 피드백 목록도 추가로 가져옵니다
        if (role == MemberRole.MENTOR) {
            // 🔥 findByMentorId -> findByMentor_Id 로 변경
            List<MentorFeedback> receivedList = mentorFeedbackRepository.findByMentor_Id(loginMemberId);
            result.addAll(receivedList.stream()
                    .map(fb -> FeedbackListResponseDto.from(fb, "RECEIVED"))
                    .toList());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public MemberRole getMemberRole(Long memberId) {    // 회원의 권한을 DB에서 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return member.getRole();
    }
}
