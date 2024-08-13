package dev.ioexception.community.mapper;

import dev.ioexception.community.dto.article.request.ArticleRequest;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.entity.Article;
import dev.ioexception.community.entity.ArticleDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArticleMapper {
    ArticleMapper INSTANCE = Mappers.getMapper(ArticleMapper.class);

    @Mapping(target = "date", expression = "java(java.time.LocalDateTime.now())")
    Article articleRequestToArticle(ArticleRequest articleRequest);
    @Mapping(source = "user.email", target = "email")
    ArticleResponse articleToArticleResponse(Article article);
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "id", target = "articleId")
    ArticleDocument articleToArticleDocument(Article article);
    @Mapping(source = "articleId", target = "id")
    ArticleResponse articleDocumentToArticleResponse(ArticleDocument articleDocument);
}
