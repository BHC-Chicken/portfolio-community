package dev.ioexception.community.dto.article.response;

import java.time.LocalDateTime;

public record ArticleResponse(Long id, String title, String content, LocalDateTime localDateTime) {
}
