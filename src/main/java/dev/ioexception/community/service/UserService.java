package dev.ioexception.community.service;

import dev.ioexception.community.dto.user.request.UserRequest;
import dev.ioexception.community.dto.user.response.UserResponse;
import dev.ioexception.community.entity.User;
import dev.ioexception.community.mapper.UserMapper;
import dev.ioexception.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {

        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("not found email"));
    }

    public UserResponse userSave(@RequestBody UserRequest request) {
        User user = UserMapper.INSTANCE.userRequestToUser(request);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return UserMapper.INSTANCE.userToUserResponse(savedUser);
    }
}
