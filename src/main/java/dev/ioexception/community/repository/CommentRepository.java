package dev.ioexception.community.repository;

import dev.ioexception.community.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(value = "Comment.user", type = EntityGraph.EntityGraphType.LOAD)
    List<Comment> findAllByArticleIdAndDeleteFlagOrderByDateDesc(Long articleId, boolean deleteFlag);
}
