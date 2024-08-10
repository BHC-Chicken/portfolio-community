package dev.ioexception.community.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import dev.ioexception.community.dto.article.request.ArticleRequest;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.entity.Article;
import dev.ioexception.community.mapper.ArticleMapper;
import dev.ioexception.community.repository.ArticleRepository;
import dev.ioexception.community.util.UploadImage;
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

    private final UploadImage uploadImage;
    private final ArticleServiceES articleServiceES;
    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse createArticle(ArticleRequest articleRequest, MultipartFile file) throws IOException {
        if (articleRequest.title().isBlank() || articleRequest.title().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("title maximum length exceeded");
        }

        if (articleRequest.content().isBlank() || articleRequest.content().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("content maximum length exceeded");
        }

        Article article = ArticleMapper.INSTANCE.articleRequestToArticle(articleRequest);

        if (file == null || file.isEmpty()) {
            article.setImageUrl("no image");
        } else {
            article.setImageUrl(uploadImage.uploadS3(file));
        }

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
    public ArticleResponse modifyArticle(Long articleId, ArticleRequest articleRequest, MultipartFile file)
            throws IOException {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        if (articleRequest.title().isBlank() || articleRequest.title().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("title maximum length exceeded");
        }

        if (articleRequest.content().isBlank() || articleRequest.content().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("content maximum length exceeded");
        }

        Article oldArticle = article.get();

        oldArticle.changeTitle(articleRequest.title());
        oldArticle.changeContent(articleRequest.content());
        oldArticle.modifyDate();

        if (file.isEmpty()) {
            oldArticle.setImageUrl("no image");
        } else {
            oldArticle.setImageUrl(uploadImage.uploadS3(file));
        }

        articleServiceES.updateQuery(oldArticle);

        return ArticleMapper.INSTANCE.articleToArticleResponse(article.get());
    }

    @Transactional
    public void deleteArticle(Long articleId) throws IOException {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        Article deleteArticle = article.get();
        deleteArticle.markAsDeleted();

        articleServiceES.deleteQuery(deleteArticle);
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
}
