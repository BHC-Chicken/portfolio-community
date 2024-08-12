package dev.ioexception.community.service;

import dev.ioexception.community.dto.article.request.ArticleRequest;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.entity.Article;
import dev.ioexception.community.entity.User;
import dev.ioexception.community.mapper.ArticleMapper;
import dev.ioexception.community.repository.ArticleRepository;
import dev.ioexception.community.repository.UserRepository;
import dev.ioexception.community.util.AWSS3Bucket;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final int PAGE_SIZE = 10;
    private final int MAX_TITLE_LENGTH = 1000;
    private final int MAX_CONTENT_LENGTH = 2000;

    private final AWSS3Bucket awsS3Bucket;
    private final ArticleServiceES articleServiceES;

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse createArticle(ArticleRequest articleRequest, MultipartFile file) throws IOException {
        validateArticleRequest(articleRequest);

        User user = findUserById(articleRequest.userId());

        Article article = ArticleMapper.INSTANCE.articleRequestToArticle(articleRequest);

        article.setUser(user);
        article.setImageUrl(handleFileUpload(file));

        Article savedArticle = articleRepository.save(article);
        articleServiceES.indexQuery(savedArticle);

        return ArticleMapper.INSTANCE.articleToArticleResponse(savedArticle);
    }

    @Transactional
    public ArticleResponse getArticleDetail(Long articleId) throws IOException {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        Article getArticle = article.get();

        getArticle.incrementView();
        articleServiceES.incrementViewQuery(getArticle);

        return ArticleMapper.INSTANCE.articleToArticleResponse(getArticle);
    }

    public Page<ArticleResponse> getArticleList(int page) {
        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("date").descending());

        Page<Article> articleList = articleRepository.findAllByDeleteFlag(pageRequest,false);

        return articleList.map(ArticleMapper.INSTANCE::articleToArticleResponse);
    }

    @Transactional
    public ArticleResponse modifyArticle(Long articleId, ArticleRequest articleRequest, MultipartFile file) throws IOException {
        validateArticleRequest(articleRequest);

        Article article = findArticleById(articleId);
        User user = findUserById(articleRequest.userId());

        validateUserOwnership(article, user);

        article.changeTitle(articleRequest.title());
        article.changeContent(articleRequest.content());
        article.modifyDate();
        article.setImageUrl(handleFileUpload(file));

        articleServiceES.updateQuery(article);

        return ArticleMapper.INSTANCE.articleToArticleResponse(article);
    }

    @Transactional
    public void deleteArticle(Long articleId, Long userId) throws IOException {
        Article article = findArticleById(articleId);
        User user = findUserById(userId);

        validateUserOwnership(article, user);
        awsS3Bucket.deleteImage(article.getImageUrl());

        article.markAsDeleted();
        articleServiceES.deleteQuery(article);
    }

    @Transactional
    public void incrementLike(Long articleId) throws IOException {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        Article likeArticle = article.get();

        likeArticle.incrementLike();
        articleServiceES.incrementLikeQuery(likeArticle);
    }

    @Transactional
    public void decrementLike(Long articleId) throws IOException {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        Article likeArticle = article.get();

        likeArticle.decrementLike();
        articleServiceES.decrementLikeQuery(likeArticle);
    }

    private void validateArticleRequest(ArticleRequest articleRequest) {
        if (articleRequest.title().isBlank() || articleRequest.title().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title maximum length exceeded");
        }

        if (articleRequest.content().isBlank() || articleRequest.content().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Content maximum length exceeded");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Article findArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
    }

    private void validateUserOwnership(Article article, User user) {
        if (!article.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User does not match the article owner");
        }
    }

    private String handleFileUpload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "no image";
        }
        return awsS3Bucket.uploadS3(file);
    }
}

