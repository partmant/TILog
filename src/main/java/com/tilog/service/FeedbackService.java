package com.tilog.service;

import com.tilog.dto.request.FeedbackRequestDtoRequest;
import com.tilog.entity.Member;
import com.tilog.entity.MentorFeedback;
import com.tilog.entity.Role;
import com.tilog.entity.TilPost;
import com.tilog.repository.MemberRepository;
import com.tilog.repository.MentorFeedbackRepository;
import com.tilog.repository.TilPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final MemberRepository memberRepository;
    private final TilPostRepository tilPostRepository;

    @Transactional
    public void requestFeedback(FeedbackRequestDtoRequest feedbackRequestDtoRequest) {   // 피드백 요청
        Member member = memberRepository.findById(feedbackRequestDtoRequest.getRequestorId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (member.getRole() != Role.PREMIUM) {
            throw new IllegalArgumentException("현재 회원 Role로는 접근할 수 없습니다.");
        }

        TilPost tilpost = tilPostRepository.findById(feedbackRequestDtoRequest.getTilId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 정보를 찾을 수 없습니다."));

        if (tilpost.isDeleted()) {
            throw new IllegalArgumentException("게시글 정보를 찾을 수 없습니다.");
        }

        Member mentor = memberRepository.findById(feedbackRequestDtoRequest.getMentorId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (mentor.getRole() != Role.MENTOR) {
            throw new IllegalArgumentException("현재 회원 Role로는 접근할 수 없습니다.");
        }

        MentorFeedback mentorFeedback = new MentorFeedback(tilpost, member, mentor);
        mentorFeedbackRepository.save(mentorFeedback);
    }
}
