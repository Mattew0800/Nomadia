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
SELECT a FROM Activity a JOIN a.trip t
WHERE t.createdBy.id = ?1
  AND (?2 IS NULL OR a.date >= ?2)
  AND (?3 IS NULL OR a.date <= ?3)
  AND (?4 IS NULL OR ?5 IS NULL OR (a.startTime < ?5 AND a.endTime > ?4))
ORDER BY a.date ASC, a.startTime ASC, a.name ASC
""")
    List<Activity> findAllByUserTrips(
            Long userId, LocalDate fromDate, LocalDate toDate, LocalTime fromTime, LocalTime toTime);
}
