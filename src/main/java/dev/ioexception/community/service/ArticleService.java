package dev.ioexception.community.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.ioexception.community.dto.article.request.ArticleRequest;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.entity.Article;
import dev.ioexception.community.entity.ArticleDocument;
import dev.ioexception.community.mapper.ArticleMapper;
import dev.ioexception.community.repository.ArticleRepository;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final int PAGE_SIZE = 10;
    private final int MAX_TITLE_LENGTH = 1000;
    private final int MAX_CONTENT_LENGTH = 2000;

    private final ElasticsearchClient client;
    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse createArticle(ArticleRequest articleRequest) throws IOException {
        if (articleRequest.title().isBlank() || articleRequest.title().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("title maximum length exceeded");
        }

        if (articleRequest.content().isBlank() || articleRequest.content().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("content maximum length exceeded");
        }

        Article article = articleRepository.save(ArticleMapper.INSTANCE.articleRequestToArticle(articleRequest));
        indexQuery(article);

        return ArticleMapper.INSTANCE.articleToArticleResponse(article);
    }

    public ArticleResponse getArticleDetail(Long articleId) {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        return ArticleMapper.INSTANCE.articleToArticleResponse(article.get());
    }

    public Page<ArticleResponse> getArticleList(int page) {
        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("date").descending());

        Page<Article> articleList = articleRepository.findAll(pageRequest);

        return articleList.map(ArticleMapper.INSTANCE::articleToArticleResponse);
    }

    @Transactional
    public ArticleResponse modifyArticle(Long articleId, ArticleRequest articleRequest) throws IOException {
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

        updateQuery(oldArticle);

        return ArticleMapper.INSTANCE.articleToArticleResponse(article.get());
    }

    @Transactional
    public void deleteArticle(Long articleId) throws IOException {
        Optional<Article> article = articleRepository.findById(articleId);

        if (article.isEmpty()) {
            throw new IllegalArgumentException("wrong articleId");
        }

        Article deleteArticle = article.get();
        deleteArticle.changeDeleteFlag();

        deleteQuery(deleteArticle);
    }

    public List<ArticleResponse> searchArticle(String category, String keyword, HttpSession session)
            throws IOException {
        SearchRequest searchRequest;
        List<FieldValue> searchAfter = getSearchAfterFromSession(session);

        if (searchAfter == null) {
            searchRequest = searchRequestInit(category, keyword);
        } else {
            searchRequest = searchRequestWithSearchAfter(searchAfter, category, keyword);
        }

        SearchResponse<ArticleDocument> response = client.search(searchRequest, ArticleDocument.class);

        List<FieldValue> newSearchAfter = getLastHitSortValues(response);
        session.setAttribute("searchAfter", newSearchAfter);

        return response.hits().hits().stream()
                .map(Hit::source).filter(Objects::nonNull)
                .map(ArticleMapper.INSTANCE::articleDocumentToArticleResponse)
                .collect(Collectors.toList());
    }

    private void indexQuery(Article article) throws IOException {
        ArticleDocument articleDocument = ArticleMapper.INSTANCE.articleToArticleDocument(article);

        IndexRequest<ArticleDocument> indexRequest = IndexRequest.of(i -> i
                .id(String.valueOf(articleDocument.getId()))
                .index("article")
                .document(articleDocument)
                .routing(articleDocument.getDate().toString()));

        client.index(indexRequest);
    }

    private void updateQuery(Article article) throws IOException {
        Map<String, Object> updateField = new HashMap<>();

        updateField.put("title", article.getTitle());
        updateField.put("content", article.getContent());

        UpdateRequest<ArticleDocument, Map<String, Object>> update = UpdateRequest.of(u -> u
                .index("article")
                .id(String.valueOf(article.getId()))
                .doc(updateField)
                .routing(String.valueOf(article.getDate())));

        client.update(update, ArticleDocument.class);
    }

    private void deleteQuery(Article article) throws IOException {
        DeleteRequest delete = DeleteRequest.of(d -> d
                .index("article")
                .id(String.valueOf(article.getId()))
                .routing(String.valueOf(article.getDate())));

        client.delete(delete);
    }

    private SearchRequest searchRequestInit(String category, String keyword) {
        return SearchRequest.of(s -> s
                .index("article")
                .size(PAGE_SIZE)
                .query(q -> q
                        .match(m -> m
                                .field(category)
                                .query(keyword)))
                .sort(so -> so
                        .field(f -> f.field("date").order(SortOrder.Desc))));
    }

    private SearchRequest searchRequestWithSearchAfter(List<FieldValue> searchAfter, String category, String keyword) {
        return SearchRequest.of(s -> s
                .index("article")
                .size(PAGE_SIZE)
                .query(q -> q
                        .match(m -> m
                                .field(category)
                                .query(keyword)))
                .searchAfter(searchAfter)
                .sort(so -> so
                        .field(f -> f.field("date").order(SortOrder.Desc))));
    }

    private SearchRequest searchRequestWithFrom(String category, String keyword, int page) {
        int pageSize = (page - 1) * PAGE_SIZE;

        return SearchRequest.of(s -> s
                .index("article")
                .size(PAGE_SIZE)
                .from(pageSize)
                .query(q -> q
                        .match(m -> m
                                .field(category)
                                .query(keyword)))
                .sort(so -> so
                        .field(f -> f.field("date").order(SortOrder.Desc))));
    }

    private List<FieldValue> getSearchAfterFromSession(HttpSession session) {
        Object sessionAttr = session.getAttribute("searchAfter");

        if (sessionAttr instanceof List<?>) {
            return (List<FieldValue>) sessionAttr;
        }

        return null;
    }

    private List<FieldValue> getLastHitSortValues(SearchResponse<ArticleDocument> searchResponse) {
        List<Hit<ArticleDocument>> hits = searchResponse.hits().hits();
        if (hits.isEmpty()) {
            return null;
        }
        return hits.get(hits.size() - 1).sort();
    }
}
