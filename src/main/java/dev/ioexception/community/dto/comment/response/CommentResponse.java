package dev.ioexception.community.dto.comment.response;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String email, String content, LocalDateTime date) {
}
