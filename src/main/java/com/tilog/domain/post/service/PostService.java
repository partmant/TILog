package com.tilog.domain.post.service;

import com.tilog.domain.post.dto.PostCommandDto;
import com.tilog.domain.post.dto.PostQueryDto;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.entity.PostImage;
import com.tilog.domain.post.entity.Visibility;
import com.tilog.domain.post.repository.PostImageRepository;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.tag.entity.PostTag;
import com.tilog.domain.tag.entity.Tag;
import com.tilog.domain.tag.repository.PostTagRepository;
import com.tilog.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// 게시글 로직 처리 서비스

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    // 게시글 이미지 접근 URL 접두사
    private static final String POST_IMAGE_URL_PREFIX = "/uploads/post/";

    // Markdown 이미지 문법에서 이미지 URL 추출
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*]\\(([^\\s)]+)(?:\\s+\"[^\"]*\")?\\)");

    // HTML img 태그에서 이미지 URL 추출
    private static final Pattern HTML_IMAGE_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostImageRepository postImageRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    // 게시글 목록 조회
    public List<PostQueryDto.ListResponse> getPostList(){
        return postRepository.findByIsDeletedFalseAndVisibilityNot(Visibility.DRAFT).stream()
                .map(post -> {
                    List<String> tagNames = postTagRepository.findByPost_Id(post.getId()).stream()
                            .map(postTag -> postTag.getTag().getName())
                            .toList();

                    return PostQueryDto.ListResponse.from(post, tagNames);
                })
                .toList();
    }

    /*
    // JWT 인증 필터 적용 후 사용할 게시글 목록 조회
    public List<PostQueryDto.ListResponse> getPostList(Long loginMemberId) {
        return postRepository.findByIsDeletedFalseAndVisibilityNot(Visibility.DRAFT).stream()
                .filter(post ->
                        post.getVisibility() == Visibility.PUBLIC ||
                                post.getMember().getId().equals(loginMemberId)
                )
                .map(post -> {
                    List<String> tagNames = postTagRepository.findByPost_Id(post.getId()).stream()
                            .map(postTag -> postTag.getTag().getName())
                            .toList();

                    return PostQueryDto.ListResponse.from(post, tagNames);
                })
                .toList();
    }
    */

    @Transactional
    public PostQueryDto.DetailResponse getPostDetail(Long postId, boolean increaseViewCount){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        if (increaseViewCount) {
            // 게시글 조회수 증가
            post.increaseViewCount();
        }

        // 게시글에 연결된 태그 이름 목록 조회
        List<String> tagNames = postTagRepository.findByPost_Id(postId).stream()
                .map(postTag -> postTag.getTag().getName())
                .toList();

        return PostQueryDto.DetailResponse.from(post, tagNames);
    }

    /*
    // JWT 인증 필터 적용 후 사용할 게시글 상세 조회
    @Transactional
    public PostQueryDto.DetailResponse getPostDetail(Long postId, boolean increaseViewCount, Long loginMemberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        boolean isOwner = post.getMember().getId().equals(loginMemberId);

        if (post.getVisibility() == Visibility.PRIVATE && !isOwner) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "비공개 게시글입니다."
            );
        }

        if (increaseViewCount) {
            post.increaseViewCount();
        }

        List<String> tagNames = postTagRepository.findByPost_Id(postId).stream()
                .map(postTag -> postTag.getTag().getName())
                .toList();

        return PostQueryDto.DetailResponse.from(post, tagNames);
    }
    */

    // 게시글 작성
    @Transactional
    public Long createPost(PostCommandDto.Create request){

        // 임시 회원 ID
        Long memberId = 1L;

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Post post = Post.create(
                member,
                request.getTitle(),
                request.getContent(),
                request.getDifficulty(),
                request.getVisibility(),
                request.getStudyTime()
        );

        Post savedPost = postRepository.save(post);

        // 게시글 태그 연결
        connectPostTags(savedPost, request.getTagNames());

        // 게시글 이미지 연결
        connectPostImages(savedPost.getId(), request.getContent());

        return savedPost.getId();
    }

    /*
    // JWT 인증 필터 적용 후 사용할 게시글 작성
    @Transactional
    public Long createPost(PostCommandDto.Create request, Long loginMemberId) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Post post = Post.create(
                member,
                request.getTitle(),
                request.getContent(),
                request.getDifficulty(),
                request.getVisibility(),
                request.getStudyTime()
        );

        Post savedPost = postRepository.save(post);

        connectPostTags(savedPost, request.getTagNames());
        connectPostImages(savedPost.getId(), request.getContent());

        return savedPost.getId();
    }
    */

    // 게시글 수정
    @Transactional
    public Long updatePost(Long postId, PostCommandDto.Update request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글은 수정할 수 없습니다.");
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getDifficulty(),
                request.getVisibility(),
                request.getStudyTime()
        );

        // 게시글 태그 갱신
        postTagRepository.deleteByPost_Id(post.getId());
        postTagRepository.flush();
        connectPostTags(post, request.getTagNames());

        // 게시글 이미지 연결
        connectPostImages(post.getId(), request.getContent());

        return post.getId();
    }

    /*
    // JWT 인증 필터 적용 후 사용할 게시글 수정
    @Transactional
    public Long updatePost(Long postId, PostCommandDto.Update request, Long loginMemberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글은 수정할 수 없습니다.");
        }

        if (!post.getMember().getId().equals(loginMemberId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "게시글 작성자만 수정할 수 있습니다."
            );
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getDifficulty(),
                request.getVisibility(),
                request.getStudyTime()
        );

        postTagRepository.deleteByPost_Id(post.getId());
        postTagRepository.flush();
        connectPostTags(post, request.getTagNames());

        connectPostImages(post.getId(), request.getContent());

        return post.getId();
    }
    */

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 게시글입니다.");
        }

        post.delete();
    }

    /*
    // JWT 인증 필터 적용 후 사용할 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long loginMemberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 게시글입니다.");
        }

        if (!post.getMember().getId().equals(loginMemberId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "게시글 작성자만 삭제할 수 있습니다."
            );
        }

        post.delete();
    }
    */

    // 게시글 태그 연결
    private void connectPostTags(Post post, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) {
                continue;
            }

            String normalizedTagName = tagName.trim();

            Tag tag = tagRepository.findByName(normalizedTagName)
                    .orElseGet(() -> tagRepository.save(Tag.create(normalizedTagName)));

            postTagRepository.save(PostTag.create(post, tag));
        }
    }

    // 게시글 본문에 포함된 이미지와 게시글 연결
    private void connectPostImages(Long postId, String content) {
        List<String> imageUrls = extractPostImageUrls(content);

        if (imageUrls.isEmpty()) {
            return;
        }

        Map<String, PostImage> postImageMap = postImageRepository.findByFileUrlIn(imageUrls).stream()
                .collect(Collectors.toMap(PostImage::getFileUrl, postImage -> postImage));

        int sortOrder = 0;

        for (String imageUrl : imageUrls) {
            PostImage postImage = postImageMap.get(imageUrl);

            if (postImage != null) {
                postImage.connectPost(postId, sortOrder++);
            }
        }
    }

    // 게시글 본문에서 이미지 URL 목록 추출
    private List<String> extractPostImageUrls(String content) {
        LinkedHashSet<String> imageUrls = new LinkedHashSet<>();

        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }

        collectImageUrls(MARKDOWN_IMAGE_PATTERN.matcher(content), imageUrls);
        collectImageUrls(HTML_IMAGE_PATTERN.matcher(content), imageUrls);

        return new ArrayList<>(imageUrls);
    }

    // 이미지 URL 수집
    private void collectImageUrls(Matcher matcher, LinkedHashSet<String> imageUrls) {
        while (matcher.find()) {
            String imageUrl = normalizePostImageUrl(matcher.group(1));

            if (imageUrl != null) {
                imageUrls.add(imageUrl);
            }
        }
    }

    // 게시글 이미지 URL 정규화
    private String normalizePostImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        String trimmedImageUrl = imageUrl.trim();

        if (trimmedImageUrl.startsWith(POST_IMAGE_URL_PREFIX)) {
            return trimmedImageUrl;
        }

        try {
            String path = URI.create(trimmedImageUrl).getPath();

            if (path != null && path.startsWith(POST_IMAGE_URL_PREFIX)) {
                return path;
            }
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return null;
    }
}
