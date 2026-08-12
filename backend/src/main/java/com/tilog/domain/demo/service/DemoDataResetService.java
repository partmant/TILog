package com.tilog.domain.demo.service;

import com.tilog.domain.bookmark.repository.TilBookmarkRepository;
import com.tilog.domain.comment.repository.TilCommentRepository;
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
 * 공개 데모 계정(demo.account.email)이 남긴 데이터를 초기화한다.
 *
 * <p>데모 계정은 실제 계정과 동일하게 글쓰기·구독·AI 리포트 기능을 제한 없이 쓸 수 있게 두는 대신,
 * 방문자가 남긴 데이터를 매일 자정 비워서 다음 방문자에게 항상 깨끗한 상태를 보여주는 방식을 선택했다.
 *
 * <p>삭제 순서는 FK 제약을 따른다:
 * 1) 페이백 참여(구독 FK) → 2) 구독(+ PREMIUM 역할 복원) → 3) AI 주간 리포트 캐시 →
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

    @Value("${demo.account.email:demo@tilog.kr}")
    private String demoEmail;

    @Transactional
    public void resetDemoData() {
        Optional<Member> demoMemberOpt = memberRepository.findByEmail(demoEmail);
        if (demoMemberOpt.isEmpty()) {
            log.info("초기화할 데모 계정이 없습니다.");
            return;
        }

        Member demoMember = demoMemberOpt.get();
        Long memberId = demoMember.getId();

        int deletedPaybacks = resetSubscriptions(memberId, demoMember);
        int deletedReports = resetAiWeeklyReports(memberId);
        int deletedPosts = resetPosts(memberId);
        resetStreakAndHeatmap(memberId);
        notificationRepository.deleteAllByReceiverId(memberId);

        log.info("데모 데이터 초기화 완료 [게시글={}건, 리포트={}건, 페이백={}건]",
                deletedPosts, deletedReports, deletedPaybacks);
    }

    // 구독 + 페이백 참여 정리, 역할을 USER로 복원
    private int resetSubscriptions(Long memberId, Member demoMember) {
        List<PaybackParticipation> paybacks = paybackParticipationRepository.findByMemberId(memberId);
        paybackParticipationRepository.deleteAll(paybacks);

        List<Subscription> subscriptions = subscriptionRepository.findByMemberIdOrderByStartedAtDesc(memberId);
        subscriptionRepository.deleteAll(subscriptions);

        if (!subscriptions.isEmpty() && demoMember.getRole() != MemberRole.USER) {
            demoMember.changeRole(MemberRole.USER);
        }

        return paybacks.size();
    }

    // AI 주간 리포트 캐시 정리 (다음 방문자가 새 데이터로 다시 생성하도록)
    private int resetAiWeeklyReports(Long memberId) {
        List<AiWeeklyReport> reports = aiWeeklyReportRepository.findAllByMemberId(memberId);
        aiWeeklyReportRepository.deleteAll(reports);
        return reports.size();
    }

    // TIL 게시글 및 하위 데이터(태그·댓글·좋아요·즐겨찾기·이미지) 정리
    private int resetPosts(Long memberId) {
        List<Post> posts = postRepository.findByMember_Id(memberId);
        for (Post post : posts) {
            Long postId = post.getId();
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
