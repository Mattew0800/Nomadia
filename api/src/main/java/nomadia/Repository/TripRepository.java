package nomadia.Repository;

import jakarta.transaction.Transactional;
import nomadia.Enum.State;
import nomadia.Model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {

    Optional<Trip> findById(Long id);
    @Query("SELECT t FROM Trip t JOIN t.users u WHERE u.id = :userId")
    List<Trip> findTripsByUserId(@Param("userId") Long userId);

        @Query("""
    SELECT COUNT(t)
    FROM Trip t
    JOIN t.users u
    WHERE u.id = :userId
    AND t.id IN :tripIds
    """)
        long countUserTrips(
                @Param("userId") Long userId,
                @Param("tripIds") List<Long> tripIds
        );


    List<Trip> findByEndDateBeforeAndStateNot(LocalDate date, State state);

    @Modifying
    @Transactional
    @Query(
            value = "INSERT IGNORE INTO user_trip (user_id, trip_id) VALUES (:userId, :tripId)",
            nativeQuery = true
    )
    void insertCreator(@Param("userId") Long userId, @Param("tripId") Long tripId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_trip WHERE trip_id = :tripId", nativeQuery = true)
    int deleteRelations(@org.springframework.data.repository.query.Param("tripId") Long tripId);

    boolean existsByIdAndCreatedBy_Id(Long tripId, Long userId);

    boolean existsByIdAndUsers_Id(Long tripId, Long userId);

    boolean existsByNameIgnoreCaseAndUsers_Id(String name, Long userId);
}
