package dev.ioexception.community.util;

import dev.ioexception.community.entity.ModifyWarning;
import dev.ioexception.community.entity.User;
import dev.ioexception.community.repository.ModifyWarningRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final String CONTENT_TYPE_TEXT = "application/text";
    private static final String CHARACTER_ENCODING_UTF_8 = "UTF-8";
    private static final String WARNING_MESSAGE = "게시물의 수정 가능 기간이 오늘로 만료됩니다.";

    private final ModifyWarningRepository modifyWarningRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType(CONTENT_TYPE_TEXT);
        response.setCharacterEncoding(CHARACTER_ENCODING_UTF_8);

        User user = (User) authentication.getPrincipal();
        List<ModifyWarning> warnings = modifyWarningRepository.findAllByUserId(user.getId());

        if (!warnings.isEmpty()) {
            String warningMessage = buildWarningMessage(warnings);
            response.getWriter().write(warningMessage);
            response.getWriter().flush();
        }
    }

    private String buildWarningMessage(List<ModifyWarning> modifyWarnings) {
        StringBuilder messageBuilder = new StringBuilder();
        for (ModifyWarning warning : modifyWarnings) {
            messageBuilder.append(warning.getArticle().getTitle()).append("\n");
        }
        messageBuilder.append(WARNING_MESSAGE);
        return messageBuilder.toString();
    }
}