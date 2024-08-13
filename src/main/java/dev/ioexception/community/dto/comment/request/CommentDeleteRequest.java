package dev.ioexception.community.dto.comment.request;

import jakarta.validation.constraints.NotNull;

public record CommentDeleteRequest(@NotNull Long userId) {
}
