package dev.ioexception.community.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Pattern(regexp = UserRequest.NAME_REGEX)
        String name,

        @NotBlank
        @Size(min = 5)
        @Pattern(regexp = UserRequest.PASSWORD_REGEX)
        String password,

        @NotBlank
        @Pattern(regexp = UserRequest.PHONE_NUMBER_REGEX)
        String phoneNumber
) {
    private static final String NAME_REGEX = "^[가-힣]+$";
    private static final String PASSWORD_REGEX = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{5,}";
    private static final String PHONE_NUMBER_REGEX = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
}
