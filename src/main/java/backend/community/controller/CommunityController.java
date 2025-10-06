package backend.community.controller;

import backend.community.domain.Comment;
import backend.community.domain.Post;
import backend.community.dto.CommentRequest;
import backend.community.dto.CommentResponse;
import backend.community.dto.PostRequest;
import backend.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;

    // 게시글 목록 (페이징)
    @GetMapping("/posts")
    public ResponseEntity<Page<Post>> listPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", defaultValue = "desc") String sort
    ) {
        return ResponseEntity.ok(communityService.getPosts(page, size, keyword, sort));
    }

    // 게시글 상세
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PostRequest request
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        return ResponseEntity.ok(communityService.createPost(
                userDetails.getUsername(),
                request.getTitle(),
                request.getContent()
        ));
    }

    // 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<Post> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") Long postId,
            @RequestBody PostRequest request
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        return ResponseEntity.ok(communityService.updatePost(
                postId, 
                userDetails.getUsername(), 
                request.getTitle(), 
                request.getContent()
        ));
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") Long postId
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        communityService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // 댓글 목록
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable("postId") Long postId,
                                                     @RequestParam(value = "sort", defaultValue = "desc") String sort) {
        return ResponseEntity.ok(communityService.getComments(postId, sort));
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequest request
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        return ResponseEntity.ok(
                communityService.createComment(postId, userDetails.getUsername(), request.getContent())
        );
    }

    // 댓글 수정
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest request
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        return ResponseEntity.ok(
                communityService.updateComment(commentId, userDetails.getUsername(), request.getContent())
        );
    }

    // 댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        communityService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
