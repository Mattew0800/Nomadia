package nomadia.Repository;

import nomadia.Model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("select distinct p from Participant p join p.expense e where e.trip.id = :tripId")
    List<Participant> findByTripId(Long tripId);
    boolean existsByUserIdAndExpenseTripId(Long userId, Long tripId);
}
