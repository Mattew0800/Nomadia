package nomadia.Repository;

import nomadia.Model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    @Query("""
SELECT a.id, p.user.id, COALESCE(SUM(p.amountPaid), 0)
FROM Participant p
JOIN p.expense e
JOIN e.activity a
JOIN a.trip t
WHERE t.id = :tripId
GROUP BY a.id, p.user.id
""")
    List<Object[]> findPaidByUserAndActivity(@Param("tripId") Long tripId);


    @Query("""
SELECT a.id, COUNT(DISTINCT p.user.id)
FROM Participant p
JOIN p.expense e
JOIN e.activity a
JOIN a.trip t
WHERE t.id = :tripId
GROUP BY a.id
""")
    Map<Long, Integer> countParticipantsGroupedByActivity(@Param("tripId") Long tripId);
    @Query("""
SELECT COUNT(DISTINCT p.user.id)
FROM Participant p
WHERE p.expense.activity.id = :activityId
""")
    int countParticipantsByActivity(@Param("activityId") Long activityId);

}
