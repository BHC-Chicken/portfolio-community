package dev.ioexception.community.repository;

import dev.ioexception.community.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @EntityGraph(value = "Article.user", type = EntityGraphType.LOAD)
    Page<Article> findAllByDeleteFlag(Pageable page, boolean deleteFlag);
}
