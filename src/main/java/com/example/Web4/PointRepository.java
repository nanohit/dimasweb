package com.example.Web4;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByOwnerLoginOrderByCreatedAtDesc(String ownerLogin);

    @Modifying
    @Transactional
    @Query("delete from Point p where p.ownerLogin = :ownerLogin")
    int deleteAllByOwnerLogin(@Param("ownerLogin") String ownerLogin);
}
