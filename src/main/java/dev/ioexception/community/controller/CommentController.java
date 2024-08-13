package dev.ioexception.community.controller;

import dev.ioexception.community.dto.comment.request.CommentDeleteRequest;
import dev.ioexception.community.dto.comment.request.CommentRequest;
import dev.ioexception.community.dto.comment.response.CommentResponse;
import dev.ioexception.community.entity.Comment;
import dev.ioexception.community.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/comment")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse comment = commentService.createComment(commentRequest);

        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comment/{articleId}")
    public ResponseEntity<List<CommentResponse>> commentList(@PathVariable Long articleId) {
        List<CommentResponse> list = commentService.getCommentList(articleId);

        return ResponseEntity.ok(list);
    }

    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<CommentResponse> modifyComment(@PathVariable Long commentId, @Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse comment = commentService.modifyComment(commentId, commentRequest);

        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @Valid @RequestBody CommentDeleteRequest request) {
        commentService.deleteComment(commentId, request);

        return ResponseEntity.ok().build();
    }
}
