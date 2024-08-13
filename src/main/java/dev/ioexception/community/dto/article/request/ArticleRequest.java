package dev.ioexception.community.dto.article.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
        @NotBlank
        @Size(max = 1000)
        String title,

        @NotBlank
        @Size(max = 2000)
        String content,

        @NotNull
        Long userId) {
}
