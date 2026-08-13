package com.tilog.domain.demo.service;

import com.tilog.domain.bookmark.repository.TilBookmarkRepository;
import com.tilog.domain.comment.repository.TilCommentRepository;
import com.tilog.domain.feedback.repository.MentorFeedbackRepository;
import com.tilog.domain.like.repository.TilPostLikeRepository;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.notification.repository.NotificationRepository;
import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.repository.PaybackParticipationRepository;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.repository.PostImageRepository;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.report.entity.AiWeeklyReport;
import com.tilog.domain.report.repository.AiWeeklyReportRepository;
import com.tilog.domain.streak.repository.StreakStatRepository;
import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.subscription.repository.SubscriptionRepository;
import com.tilog.domain.tag.repository.PostTagRepository;
import com.tilog.domain.writeHistory.repository.WriteHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 공개 데모 계정들(demo.account.email, demo.mentor.email)이 남긴 데이터를 초기화한다.
 *
 * <p>데모 계정은 실제 계정과 동일하게 글쓰기·구독·AI 리포트 기능을 제한 없이 쓸 수 있게 두는 대신,
 * 방문자가 남긴 데이터를 매일 자정 비워서 다음 방문자에게 항상 깨끗한 상태를 보여주는 방식을 선택했다.
 * 데모 멘토 계정도 role만 MENTOR일 뿐 나머지 기능(TIL 작성, 구독 등)은 일반 계정과 동일하게
 * 열려 있어 똑같이 흔적이 쌓일 수 있으므로 같은 방식으로 초기화한다. 다만 role은 계정마다
 * 복원해야 할 "기본 role"이 다르므로(일반 데모 계정은 USER, 데모 멘토는 MENTOR) 인자로 받는다.
 *
 * <p>삭제 순서는 FK 제약을 따른다:
 * 1) 페이백 참여(구독 FK) → 2) 구독(+ 기본 role 복원) → 3) AI 주간 리포트 캐시 →
 * 4) TIL 게시글 하위 데이터(태그·댓글·좋아요·즐겨찾기·이미지) → 게시글 →
 * 5) 스트릭 통계 · 잔디(작성 이력) → 6) 알림
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataResetService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final TilCommentRepository tilCommentRepository;
    private final TilPostLikeRepository tilPostLikeRepository;
    private final TilBookmarkRepository tilBookmarkRepository;
    private final PostImageRepository postImageRepository;
    private final StreakStatRepository streakStatRepository;
    private final WriteHistoryRepository writeHistoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaybackParticipationRepository paybackParticipationRepository;
    private final AiWeeklyReportRepository aiWeeklyReportRepository;
    private final NotificationRepository notificationRepository;
    private final MentorFeedbackRepository mentorFeedbackRepository;

    @Value("${demo.account.email:demo@tilog.kr}")
    private String demoEmail;

    @Value("${demo.mentor.email:mentor@tilog.kr}")
    private String demoMentorEmail;

    @Transactional
    public void resetDemoData() {
        resetAccount(demoEmail, MemberRole.USER);
    }

    @Transactional
    public void resetDemoMentorData() {
        resetAccount(demoMentorEmail, MemberRole.MENTOR);
    }

    // baseRole: 초기화 후 복원해야 할 기본 role (일반 데모 계정=USER, 데모 멘토=MENTOR)
    private void resetAccount(String email, MemberRole baseRole) {
        Optional<Member> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isEmpty()) {
            log.info("초기화할 계정이 없습니다: {}", email);
            return;
        }

        Member member = memberOpt.get();
        Long memberId = member.getId();

        int deletedPaybacks = resetSubscriptions(memberId, member, baseRole);
        int deletedReports = resetAiWeeklyReports(memberId);
        int deletedPosts = resetPosts(memberId);
        resetStreakAndHeatmap(memberId);
        notificationRepository.deleteAllByReceiverId(memberId);

        log.info("데모 데이터 초기화 완료 [{}] [게시글={}건, 리포트={}건, 페이백={}건]",
                email, deletedPosts, deletedReports, deletedPaybacks);
    }

    // 구독 + 페이백 참여 정리, role을 baseRole로 복원
    private int resetSubscriptions(Long memberId, Member member, MemberRole baseRole) {
        List<PaybackParticipation> paybacks = paybackParticipationRepository.findByMemberId(memberId);
        paybackParticipationRepository.deleteAll(paybacks);

        List<Subscription> subscriptions = subscriptionRepository.findByMemberIdOrderByStartedAtDesc(memberId);
        subscriptionRepository.deleteAll(subscriptions);

        if (!subscriptions.isEmpty() && member.getRole() != baseRole) {
            member.changeRole(baseRole);
        }

        return paybacks.size();
    }

    // AI 주간 리포트 캐시 정리 (다음 방문자가 새 데이터로 다시 생성하도록)
    private int resetAiWeeklyReports(Long memberId) {
        List<AiWeeklyReport> reports = aiWeeklyReportRepository.findAllByMemberId(memberId);
        aiWeeklyReportRepository.deleteAll(reports);
        return reports.size();
    }

    // TIL 게시글 및 하위 데이터(태그·댓글·좋아요·즐겨찾기·이미지·멘토 피드백) 정리
    // - MentorFeedback.til_id는 NOT NULL FK이고 cascade가 없어서, 이걸 먼저 지우지 않으면
    //   피드백이 달린 게시글을 지울 때 FK 제약 위반으로 초기화 트랜잭션 전체가 실패한다.
    //   (한 번 실패하면 이후 초기화도 계속 조용히 실패하게 되는 치명적인 문제였음)
    private int resetPosts(Long memberId) {
        List<Post> posts = postRepository.findByMember_Id(memberId);
        for (Post post : posts) {
            Long postId = post.getId();
            mentorFeedbackRepository.deleteByTil_Id(postId);
            postTagRepository.deleteByPost_Id(postId);
            tilCommentRepository.deleteByPost_Id(postId);
            tilPostLikeRepository.deleteByPost_Id(postId);
            tilBookmarkRepository.deleteByPost_Id(postId);
            postImageRepository.deleteByPostId(postId);
        }
        postRepository.deleteAll(posts);
        return posts.size();
    }

    // 스트릭 통계 및 잔디(작성 이력) 정리
    private void resetStreakAndHeatmap(Long memberId) {
        writeHistoryRepository.deleteByMember_Id(memberId);
        if (streakStatRepository.existsById(memberId)) {
            streakStatRepository.deleteById(memberId);
        }
    }
}
