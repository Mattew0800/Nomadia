package nomadia.Repository;

import nomadia.Model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("""
    SELECT p
    FROM Participant p
    JOIN p.expense e
    WHERE e.trip.id = :tripId
""")
    List<Participant> findByTripId(@Param("tripId") Long tripId);

    boolean existsByUserIdAndExpenseTripId(Long userId, Long tripId);
}
