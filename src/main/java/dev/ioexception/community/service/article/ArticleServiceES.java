package dev.ioexception.community.service.article;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.InlineScript;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.entity.Article;
import dev.ioexception.community.entity.ArticleDocument;
import dev.ioexception.community.mapper.ArticleMapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleServiceES {
    private final int PAGE_SIZE = 10;
    private final ElasticsearchClient client;

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

    public void indexQuery(Article article) throws IOException {
        ArticleDocument articleDocument = ArticleMapper.INSTANCE.articleToArticleDocument(article);

        IndexRequest<ArticleDocument> indexRequest = IndexRequest.of(i -> i
                .id(String.valueOf(articleDocument.getArticleId()))
                .index("article")
                .document(articleDocument)
                .routing(localDateTimeFormatter(articleDocument.getDate())));

        client.index(indexRequest);
    }

    public void updateQuery(Article article) throws IOException {
        Map<String, Object> updateField = new HashMap<>();

        updateField.put("title", article.getTitle());
        updateField.put("content", article.getContent());

        UpdateRequest<ArticleDocument, Map<String, Object>> updateRequest = UpdateRequest.of(u -> u
                .index("article")
                .id(String.valueOf(article.getId()))
                .doc(updateField)
                .routing(localDateTimeFormatter(article.getDate())));

        client.update(updateRequest, ArticleDocument.class);
    }

    public void incrementViewQuery(Article article) throws IOException {
        Map<String, JsonData> params = new HashMap<>();
        params.put("amount", JsonData.of(1));

        UpdateRequest<ArticleDocument, Map<String, Object>> updateRequest = UpdateRequest.of(u -> u
                .index("article")
                .id(String.valueOf(article.getId()))
                .routing(localDateTimeFormatter(article.getDate()))
                .script(sc -> sc
                        .inline(InlineScript.of(i -> i
                                .source("ctx._source.view += params.amount")
                                .lang("painless")
                                .params(params)))));

        client.update(updateRequest, ArticleDocument.class);
    }

    public void incrementLikeQuery(Article article) throws IOException {
        Map<String, JsonData> param = new HashMap<>();
        param.put("amount", JsonData.of(1));

        UpdateRequest<ArticleDocument, Map<String, Object>> updateRequest = UpdateRequest.of(u -> u
                .index("article")
                .id(String.valueOf(article.getId()))
                .routing(localDateTimeFormatter(article.getDate()))
                .script(sc -> sc
                        .inline(InlineScript.of(i -> i
                                .source("ctx._source.like += param.amount")
                                .lang("painless")
                                .params(param)))));

        client.update(updateRequest, ArticleDocument.class);
    }

    public void decrementLikeQuery(Article article) throws IOException {
        Map<String, JsonData> param = new HashMap<>();
        param.put("amount", JsonData.of(1));

        UpdateRequest<ArticleDocument, Map<String, Object>> updateRequest = UpdateRequest.of(u -> u
                .index("article")
                .id(String.valueOf(article.getId()))
                .routing(localDateTimeFormatter(article.getDate()))
                .script(sc -> sc
                        .inline(InlineScript.of(i -> i
                                .source("ctx._source.like -= param.amount")
                                .lang("painless")
                                .params(param)))));

        client.update(updateRequest, ArticleDocument.class);
    }

    public void deleteQuery(Article article) throws IOException {
        DeleteRequest delete = DeleteRequest.of(d -> d
                .index("article")
                .id(String.valueOf(article.getId()))
                .routing(localDateTimeFormatter(article.getDate())));

        client.delete(delete);
    }

    public String Top10Keyword() throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(".ds-spring-boot-metrics*")
                .size(0)
                .aggregations("top10-keyword", Aggregation.of(a -> a
                        .terms(t -> t
                                .field("query_question")
                                .size(10)
                                .order(NamedValue.of("_count", SortOrder.Desc))
                        ))));

        SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);
        StringBuilder sb = new StringBuilder();

        int index = 1;

        for (StringTermsBucket s : response.aggregations().get("top10-keyword").sterms().buckets().array()) {
            sb.append(index++).append(" : ").append(s.key()._get()).append("\n");
        }

        return sb.toString();
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
        int pageNumber = (page - 1) * PAGE_SIZE;

        return SearchRequest.of(s -> s
                .index("article")
                .size(PAGE_SIZE)
                .from(pageNumber)
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

    private String localDateTimeFormatter(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
