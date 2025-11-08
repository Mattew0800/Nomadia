package nomadia.Repository;

import jakarta.transaction.Transactional;
import nomadia.Model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
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
}
