package com.tilog.domain.feedback.service;

import com.tilog.domain.feedback.dto.FeedbackRequestDtoRequest;
import com.tilog.domain.feedback.dto.FeedbackWriteRequestDto;
import com.tilog.domain.feedback.dto.FeedbackDetailResponseDto;
import com.tilog.domain.feedback.entity.MentorFeedback;
import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.feedback.repository.MentorFeedbackRepository;
import com.tilog.domain.post.repository.TilPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {
    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final MemberRepository memberRepository;
    private final TilPostRepository tilPostRepository;


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

        findFeedback.updateFeedback(feedbackWriteRequestDto.getTechnicalScore(), feedbackWriteRequestDto.getFlowScore(), feedbackWriteRequestDto.getDesignScore(), feedbackWriteRequestDto.getComment());
    }

    @Transactional(readOnly = true)
    public FeedbackDetailResponseDto getFeedbackDetail(Long feedbackId){    // 피드백 상세 조회
        MentorFeedback findFeedback = mentorFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백 정보를 찾을 수 없습니다."));

        FeedbackDetailResponseDto feedbackDetail = new FeedbackDetailResponseDto(findFeedback.getFeedbackId(), findFeedback.getTil().getId(), findFeedback.getMentor().getId(), findFeedback.getStatus(), findFeedback.getTechnicalScore(), findFeedback.getFlowScore(), findFeedback.getDesignScore(), findFeedback.getComment(), findFeedback.getRequestedAt(), findFeedback.getCompletedAt());

        return feedbackDetail;
    }
}
