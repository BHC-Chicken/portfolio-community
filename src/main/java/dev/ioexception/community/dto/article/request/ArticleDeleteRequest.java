package dev.ioexception.community.dto.article.request;

import jakarta.validation.constraints.NotNull;

public record ArticleDeleteRequest(@NotNull Long userId) {

}
