package com.tilog.domain.post.service;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.dto.PostCommandDto;
import com.tilog.domain.post.dto.PostQueryDto;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 게시글 로직 처리 서비스

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 게시글 목록 조회
    public List<PostQueryDto.ListResponse> getPostList(){
        return postRepository.findByIsDeletedFalse().stream().map(post -> PostQueryDto.ListResponse.from(post)).toList();
    }

    // 게시글 상세 조회
    public PostQueryDto.DetailResponse getPostDetail(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        // TODO: 로그인 기능 병합 후 주석 해제
        // Long loginMemberId = 로그인한_회원_ID;
        //
        // if (
        //     post.getVisibility() == Visibility.PRIVATE &&
        //     !post.getMember().getId().equals(loginMemberId)
        // ) {
        //     throw new IllegalArgumentException("비공개 게시글입니다.");
        // }

        return PostQueryDto.DetailResponse.from(post);
    }

    // 게시글 작성
    @Transactional
    public Long createPost(PostCommandDto.Create request){

        // 임시 회원 ID
        Long memberId = 1L;
        // Long memberId = loginMember.getId();

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

        return savedPost.getId();
    }

    // 게시글 수정
    @Transactional
    public Long updatePost(Long postId, PostCommandDto.Update request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // TODO: 로그인 기능 병합 후 주석 해제
        // Long loginMemberId = 로그인한_회원_ID;
        //
        // if (!post.getMember().getId().equals(loginMemberId)) {
        //     throw new IllegalArgumentException("게시글 작성자만 수정할 수 있습니다.");
        // }

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

        return post.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 게시글입니다.");
        }

        // TODO: 로그인 기능 병합 후 주석 해제
        // Long loginMemberId = 로그인한_회원_ID;
        //
        // if (!post.getMember().getId().equals(loginMemberId)) {
        //     throw new IllegalArgumentException("게시글 작성자만 삭제할 수 있습니다.");
        // }

        post.delete();
    }
}
