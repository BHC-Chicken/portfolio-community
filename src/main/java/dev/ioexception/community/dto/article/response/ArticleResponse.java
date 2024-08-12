package dev.ioexception.community.dto.article.response;

import java.time.LocalDateTime;

public record ArticleResponse(Long id, String title, String content, int view, int like, LocalDateTime date,
                              String imageUrl, String email) {
}
