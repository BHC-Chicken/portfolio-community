package dev.ioexception.community.mapper;

import dev.ioexception.community.dto.user.request.UserRequest;
import dev.ioexception.community.dto.user.response.UserResponse;
import dev.ioexception.community.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User userRequestToUser(UserRequest userRequest);
    UserResponse userToUserResponse(User user);
}
