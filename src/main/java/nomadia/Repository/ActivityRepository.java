package nomadia.Repository;

import jakarta.transaction.Transactional;
import nomadia.Model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByTripId(Long tripId);
    List<Activity> findByTripIdAndDate(Long tripId, LocalDate date);
    Optional<Activity> findByIdAndTripId(Long id, Long tripId);
    boolean existsByTripIdAndNameIgnoreCase(Long tripId, String name);
    Optional<Activity> findById(Long activityId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Activity a WHERE a.trip.id = :tripId")
    default void deleteByTripId(Long tripId) {
    }

    @Query("""
SELECT DISTINCT a
FROM Activity a
JOIN a.trip t
LEFT JOIN t.users u
WHERE u.id = :userId
  AND (:tripId IS NULL OR t.id = :tripId)
  AND (:fromDate IS NULL OR a.date >= :fromDate)
  AND (:toDate IS NULL OR a.date <= :toDate)
  AND (:fromTime IS NULL OR :toTime IS NULL OR (a.startTime < :toTime AND a.endTime > :fromTime))
ORDER BY a.date ASC, a.startTime ASC, a.name ASC
""")
    List<Activity> findAllByUserTrips(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime,
            @Param("tripId") Long tripId
    );



}

