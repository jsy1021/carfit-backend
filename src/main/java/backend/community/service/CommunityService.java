package backend.community.service;

import backend.community.domain.Comment;
import backend.community.domain.Post;
import backend.community.dto.CommentResponse;
import backend.community.repository.CommentRepository;
import backend.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<Post> getPosts(int page, int size, String keyword, String sort) {
        boolean asc = "asc".equalsIgnoreCase(sort);
        // 정렬 보장이 필요한 경우 메서드 이름 기반 정렬을 사용해 확실히 고정
        Pageable pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.isBlank()) {
            return asc
                    ? postRepository.findAllByOrderByCreatedAtAsc(pageable)
                    : postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        // 검색 시에는 Pageable Sort를 이용해 정렬 지정
        Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable sortable = PageRequest.of(page, size, Sort.by(direction, "createdAt", "id"));
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, sortable);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    public List<Comment> getComments(Long postId, String sort) {
        Post post = getPost(postId);
        if ("asc".equalsIgnoreCase(sort)) {
            return commentRepository.findByPostOrderByCreatedAtAsc(post);
        }
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }

    @Transactional
    public Post createPost(String authorId, String title, String content) {
        Post post = Post.builder()
                .authorId(authorId)
                .title(title)
                .content(content)
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long postId, String authorId, String title, String content) {
        Post post = getPost(postId);
        if (!post.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다");
        }
        post.update(title, content);
        return post;
    }

    @Transactional
    public void deletePost(Long postId, String authorId) {
        Post post = getPost(postId);
        if (!post.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다");
        }
        postRepository.deleteById(postId);
    }

    @Transactional
    public Comment addComment(Long postId, String authorId, String content) {
        Post post = getPost(postId);
        Comment comment = Comment.builder()
                .post(post)
                .authorId(authorId)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    // 컨트롤러에서 사용하는 시그니처와 일치시키기 위한 alias 메서드
    @Transactional
    public CommentResponse createComment(Long postId, String authorId, String content) {
        Comment comment = addComment(postId, authorId, content);
        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String authorId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
        
        if (!comment.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다");
        }
        
        comment.updateContent(content);
        Comment updated = commentRepository.save(comment);
        return CommentResponse.from(updated);
    }

    @Transactional
    public void deleteComment(Long commentId, String authorId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
        if (!comment.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다");
        }
        commentRepository.deleteById(commentId);
    }
}


