package com.example.Web4;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    public static final String SESSION_USER_LOGIN = "session_user_login";

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request, HttpSession session) {
        AuthRequest validatedRequest = requireRequest(request);
        User user = userService.register(validatedRequest.login(), validatedRequest.password());
        session.setAttribute(SESSION_USER_LOGIN, user.getLogin());
        return new AuthResponse(user.getLogin());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpSession session) {
        AuthRequest validatedRequest = requireRequest(request);
        User user = userService.login(validatedRequest.login(), validatedRequest.password());
        session.setAttribute(SESSION_USER_LOGIN, user.getLogin());
        return new AuthResponse(user.getLogin());
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpSession session) {
        session.invalidate();
        return new MessageResponse("Сессия завершена");
    }

    @GetMapping("/me")
    public AuthResponse me(HttpSession session) {
        String login = (String) session.getAttribute(SESSION_USER_LOGIN);
        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return new AuthResponse(login);
    }

    private AuthRequest requireRequest(AuthRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тело запроса не передано");
        }
        return request;
    }

    public record AuthRequest(String login, String password) {}

    public record AuthResponse(String login) {}

    public record MessageResponse(String message) {}
}
