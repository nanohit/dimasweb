package com.example.Web4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PointService {
    private final PointRepository pointRepository;
    private final AreaChecker areaChecker;
    private final Validator validator;

    @Autowired
    public PointService(PointRepository pointRepository, AreaChecker areaChecker, Validator validator) {
        this.pointRepository = pointRepository;
        this.areaChecker = areaChecker;
        this.validator = validator;
    }

    public Point addPoint(float x, float y, float r, String ownerLogin) {
        if (!validator.validate(x, y, r)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Координаты точки или радиус заданы неверно");
        }

        Point point = new Point(x, y, r, ownerLogin);
        point.setResult(areaChecker.checkArea(x, y, r));
        return pointRepository.save(point);
    }

    public List<Point> getPointsByOwnerLogin(String login) {
        return pointRepository.findByOwnerLoginOrderByCreatedAtDesc(login);
    }

    public void clearPointsByOwnerLogin(String login) {
        pointRepository.deleteByOwnerLogin(login);
    }
}
