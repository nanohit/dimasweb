package com.example.Web4;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByOwnerLoginOrderByCreatedAtDesc(String ownerLogin);

    void deleteByOwnerLogin(String ownerLogin);
}
