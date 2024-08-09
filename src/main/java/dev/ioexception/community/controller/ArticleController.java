package dev.ioexception.community.controller;

import dev.ioexception.community.dto.article.request.ArticleRequest;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.service.ArticleService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping("/list/{page}")
    public ResponseEntity<Page<ArticleResponse>> articleList(@PathVariable int page) {
        Page<ArticleResponse> list = articleService.getArticleList(page);

        return ResponseEntity.ok(list);
    }

    @PostMapping("/article")
    public ResponseEntity<ArticleResponse> createArticle(@RequestBody ArticleRequest articleRequest)
            throws IOException {
        ArticleResponse articleResponse = articleService.createArticle(articleRequest);

        return ResponseEntity.ok(articleResponse);
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleResponse> getArticleDetail(@PathVariable Long articleId) {
        ArticleResponse articleResponse = articleService.getArticleDetail(articleId);

        return ResponseEntity.ok(articleResponse);
    }

    @PatchMapping("/{articleId}")
    public ResponseEntity<ArticleResponse> modifyArticle(@PathVariable Long articleId,
                                                         @RequestBody ArticleRequest articleRequest)
            throws IOException {
        ArticleResponse articleResponse = articleService.modifyArticle(articleId, articleRequest);

        return ResponseEntity.ok(articleResponse);
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId) throws IOException {
        articleService.deleteArticle(articleId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArticleResponse>> searchArticle(@RequestParam("c") String c, @RequestParam("q") String q, HttpSession session)
            throws IOException {
        List<ArticleResponse> list = articleService.searchArticle(c, q, session);

        return ResponseEntity.ok(list);
    }
}
