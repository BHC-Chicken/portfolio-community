package dev.ioexception.community.controller;

import dev.ioexception.community.dto.user.request.UserRequest;
import dev.ioexception.community.dto.user.response.UserResponse;
import dev.ioexception.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse user = userService.userSave(userRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
