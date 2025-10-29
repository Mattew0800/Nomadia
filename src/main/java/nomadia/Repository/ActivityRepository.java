package nomadia.Repository;

import nomadia.Model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByTripId(Long tripId);
    Optional<Activity> findByIdAndTripId(Long id, Long tripId);
    boolean existsByTripIdAndNameIgnoreCase(Long tripId, String name);
}
