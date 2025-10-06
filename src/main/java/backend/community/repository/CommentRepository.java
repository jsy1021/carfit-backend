package backend.community.repository;

import backend.community.domain.Comment;
import backend.community.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
}



