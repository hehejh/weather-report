package com.weather.repository;

import com.weather.model.PhotoSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoSpotRepository extends JpaRepository<PhotoSpot, Long> {

    List<PhotoSpot> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query(value = """
            SELECT s FROM PhotoSpot s
            WHERE s.userId = :userId
              AND within(s.location, :filter) = true
            ORDER BY s.createdAt DESC
            """)
    List<PhotoSpot> findByUserIdWithinBounds(
            @Param("userId") String userId,
            @Param("filter") org.locationtech.jts.geom.Geometry filter
    );

    @Query(value = """
            SELECT s FROM PhotoSpot s
            WHERE s.userId = :userId
              AND LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY s.createdAt DESC
            """)
    List<PhotoSpot> searchByUserIdAndName(
            @Param("userId") String userId,
            @Param("query") String query
    );
}
