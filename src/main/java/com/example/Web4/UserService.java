package com.example.Web4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String login, String password) {
        String normalizedLogin = normalizeLogin(login);
        validatePassword(password);

        if (userRepository.findByLogin(normalizedLogin).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Логин уже занят");
        }

        User user = new User(normalizedLogin, hashPassword(password));
        return userRepository.save(user);
    }

    public User login(String login, String password) {
        String normalizedLogin = normalizeLogin(login);
        validatePassword(password);

        Optional<User> user = userRepository.findByLogin(normalizedLogin);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        if (!hashPassword(password).equals(user.get().getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        return user.get();
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    private String normalizeLogin(String login) {
        if (login == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин обязателен");
        }

        String normalized = login.trim();
        if (normalized.length() < 3 || normalized.length() > 32) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Логин должен быть длиной от 3 до 32 символов"
            );
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 4 || password.length() > 128) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пароль должен быть длиной от 4 до 128 символов"
            );
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexBuilder.append('0');
                }
                hexBuilder.append(hex);
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
