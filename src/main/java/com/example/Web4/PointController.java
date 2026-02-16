package com.example.Web4;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;

    @Autowired
    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping
    public Point addPoint(@RequestBody PointRequest request, HttpSession session) {
        PointRequest validatedRequest = requireRequest(request);
        String login = requireAuthorizedLogin(session);
        return pointService.addPoint(validatedRequest.x(), validatedRequest.y(), validatedRequest.r(), login);
    }

    @GetMapping
    public List<Point> getPoints(HttpSession session) {
        String login = requireAuthorizedLogin(session);
        return pointService.getPointsByOwnerLogin(login);
    }

    @DeleteMapping
    public UserController.MessageResponse clearPoints(HttpSession session) {
        String login = requireAuthorizedLogin(session);
        pointService.clearPointsByOwnerLogin(login);
        return new UserController.MessageResponse("История очищена");
    }

    private String requireAuthorizedLogin(HttpSession session) {
        String login = (String) session.getAttribute(UserController.SESSION_USER_LOGIN);
        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return login;
    }

    private PointRequest requireRequest(PointRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тело запроса не передано");
        }
        return request;
    }

    public record PointRequest(float x, float y, float r) {}
}
