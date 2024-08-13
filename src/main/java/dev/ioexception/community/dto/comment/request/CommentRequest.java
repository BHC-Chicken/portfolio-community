package dev.ioexception.community.dto.comment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotNull
        Long articleId,

        @NotNull
        Long userId,

        @NotBlank
        @Size(max = 200)
        String content) {
}
