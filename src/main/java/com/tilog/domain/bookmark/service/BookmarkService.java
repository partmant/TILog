package com.tilog.domain.bookmark.service;

import com.tilog.domain.bookmark.dto.BookmarkResponse;
import com.tilog.domain.bookmark.dto.BookmarkedPostResponse;
import com.tilog.domain.bookmark.entity.TilBookmark;
import com.tilog.domain.bookmark.repository.TilBookmarkRepository;
import com.tilog.domain.comment.repository.TilCommentRepository;
import com.tilog.domain.like.repository.TilPostLikeRepository;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.tag.repository.PostTagRepository;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final TilBookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TilPostLikeRepository likeRepository;
    private final TilCommentRepository commentRepository;
    private final PostTagRepository postTagRepository;

    /** 즐겨찾기 등록 (멱등성: 이미 등록된 경우 성공 처리) */
    @Transactional
    public BookmarkResponse addBookmark(Long postId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 이미 즐겨찾기된 경우 멱등성 있게 처리
        if (bookmarkRepository.existsByPost_IdAndMember_Id(postId, memberId)) {
            return BookmarkResponse.of(true);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND_FOR_BOOKMARK));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        bookmarkRepository.save(TilBookmark.create(member, post));
        return BookmarkResponse.of(true);
    }

    /** 즐겨찾기 해제 (멱등성: 등록되지 않은 경우도 성공 처리) */
    @Transactional
    public BookmarkResponse removeBookmark(Long postId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Optional<TilBookmark> bookmark = bookmarkRepository.findByPost_IdAndMember_Id(postId, memberId);
        bookmark.ifPresent(bookmarkRepository::delete);

        return BookmarkResponse.of(false);
    }

    /** 내가 즐겨찾기한 TIL 목록 조회 (검색/난이도/정렬/페이지네이션 지원) */
    public Page<BookmarkedPostResponse> getMyBookmarkedPosts(
            String keyword, String difficulty, String sortType, Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 전체 즐겨찾기 로드 후 인메모리 필터/정렬
        List<TilBookmark> allBookmarks =
                bookmarkRepository.findByMember_IdOrderByCreatedAtDesc(memberId);

        List<BookmarkedPostResponse> responses = allBookmarks.stream()
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    Long pid = post.getId();
                    List<String> tagNames = postTagRepository.findByPost_Id(pid).stream()
                            .map(pt -> pt.getTag().getName())
                            .toList();
                    long likeCount = likeRepository.countByPost_Id(pid);
                    long commentCount = commentRepository.countByPost_IdAndIsDeletedFalse(pid);
                    return BookmarkedPostResponse.from(bookmark, tagNames, likeCount, commentCount);
                })
                .collect(Collectors.toList());

        // 키워드 필터 (제목 포함 검색)
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            responses = responses.stream()
                    .filter(r -> r.getTitle() != null && r.getTitle().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
        }

        // 난이도 필터
        if (difficulty != null && !difficulty.isBlank() && !"ALL".equalsIgnoreCase(difficulty)) {
            responses = responses.stream()
                    .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                    .collect(Collectors.toList());
        }

        // 정렬
        if ("LIKES".equals(sortType)) {
            responses.sort(Comparator.comparingLong(BookmarkedPostResponse::getLikeCount).reversed());
        } else if ("COMMENTS".equals(sortType)) {
            responses.sort(Comparator.comparingLong(BookmarkedPostResponse::getCommentCount).reversed());
        }
        // LATEST: 이미 createdAt desc 순으로 조회됨

        // 수동 페이지네이션
        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<BookmarkedPostResponse> pageContent = start >= total ? List.of() : responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, total);
    }
}
