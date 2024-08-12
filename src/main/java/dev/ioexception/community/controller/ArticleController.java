package dev.ioexception.community.controller;

import dev.ioexception.community.dto.article.request.ArticleDeleteRequest;
import dev.ioexception.community.dto.article.request.ArticleRequest;
import dev.ioexception.community.dto.article.response.ArticleResponse;
import dev.ioexception.community.service.ArticleService;
import dev.ioexception.community.service.ArticleServiceES;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final ArticleServiceES articleServiceES;

    @GetMapping("/list/{page}")
    public ResponseEntity<Page<ArticleResponse>> articleList(@PathVariable int page) {
        Page<ArticleResponse> list = articleService.getArticleList(page);

        return ResponseEntity.ok(list);
    }

    @PostMapping("/article")
    public ResponseEntity<ArticleResponse> createArticle(@RequestPart(value = "article") ArticleRequest articleRequest,
                                                         @RequestPart(value = "file", required = false) MultipartFile file)
            throws IOException {
        ArticleResponse articleResponse = articleService.createArticle(articleRequest, file);

        return ResponseEntity.ok(articleResponse);
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<ArticleResponse> getArticleDetail(@PathVariable Long articleId) throws IOException {
        ArticleResponse articleResponse = articleService.getArticleDetail(articleId);

        return ResponseEntity.ok(articleResponse);
    }

    @PatchMapping("/article/{articleId}")
    public ResponseEntity<ArticleResponse> modifyArticle(@PathVariable Long articleId,
                                                         @RequestPart("article") ArticleRequest articleRequest,
                                                         @RequestPart(value = "file", required = false)
                                                         MultipartFile file)
            throws IOException {

        ArticleResponse articleResponse = articleService.modifyArticle(articleId, articleRequest, file);

        return ResponseEntity.ok(articleResponse);
    }

    @DeleteMapping("/article/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId,
                                              @RequestBody ArticleDeleteRequest articleDeleteRequest)
            throws IOException {
        articleService.deleteArticle(articleId, articleDeleteRequest.userId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArticleResponse>> searchArticle(@RequestParam("c") String c, @RequestParam("q") String q,
                                                               HttpSession session)
            throws IOException {
        List<ArticleResponse> list = articleServiceES.searchArticle(c, q, session);

        return ResponseEntity.ok(list);
    }
}
