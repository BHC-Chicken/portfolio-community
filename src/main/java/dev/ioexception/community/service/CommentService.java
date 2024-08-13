package dev.ioexception.community.service;

import dev.ioexception.community.dto.comment.request.CommentDeleteRequest;
import dev.ioexception.community.dto.comment.request.CommentRequest;
import dev.ioexception.community.dto.comment.response.CommentResponse;
import dev.ioexception.community.entity.Comment;
import dev.ioexception.community.entity.User;
import dev.ioexception.community.mapper.CommentMapper;
import dev.ioexception.community.repository.CommentRepository;
import dev.ioexception.community.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse createComment(CommentRequest commentRequest) {
        User user = findUserById(commentRequest.userId());
        Comment comment = CommentMapper.INSTANCE.commentRequestToComment(commentRequest);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.INSTANCE.commentToCommentResponse(savedComment);
    }

    @Transactional
    public List<CommentResponse> getCommentList(Long articleId) {
        List<Comment> commentList = commentRepository.findAllByArticleIdAndDeleteFlagOrderByDateDesc(articleId, false);

        return commentList.stream()
                .map(CommentMapper.INSTANCE::commentToCommentResponse).toList();
    }

    @Transactional
    public CommentResponse modifyComment(Long commentId, CommentRequest commentRequest) {
        Comment comment = findCommentById(commentId);
        System.out.println(comment.getUser().getId());
        System.out.println(commentRequest.userId());

        validateUserOwnership(comment.getUser().getId(), commentRequest.userId());

        comment.setContent(commentRequest.content());
        comment.modifyDate();

        return CommentMapper.INSTANCE.commentToCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, CommentDeleteRequest request) {
        Comment comment = findCommentById(commentId);
        validateUserOwnership(comment.getUser().getId(), request.userId());

        comment.markAsDeleted();
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void validateUserOwnership(Long articleId, Long userId) {
        if (!articleId.equals(userId)) {
            throw new IllegalArgumentException("User does not match the article owner");
        }
    }
}
