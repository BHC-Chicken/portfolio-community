package dev.ioexception.community.mapper;

import dev.ioexception.community.dto.comment.request.CommentRequest;
import dev.ioexception.community.dto.comment.response.CommentResponse;
import dev.ioexception.community.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(source = "articleId", target = "article.id")
    Comment commentRequestToComment(CommentRequest request);
    @Mapping(source = "user.email", target = "email")
    CommentResponse commentToCommentResponse(Comment comment);
}
