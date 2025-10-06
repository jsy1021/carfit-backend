package backend.community.dto;

import backend.community.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private String content;
    private String authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 메서드
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())  // Post 전체가 아닌 ID만 추출
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

