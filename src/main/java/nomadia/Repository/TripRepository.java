package nomadia.Repository;

import nomadia.Model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {

    List<Trip> findByUsers_Id(Long userId);
    Optional<Trip> findByNameIgnoreCaseAndUsers_Id(String name, Long userId);


    @Query("SELECT t FROM Trip t JOIN t.users u WHERE u.id = :userId")
    List<Trip> findTripsByUserId(@Param("userId") Long userId);

    @Query("select t.createdBy.id from Trip t where t.id = :tripId")
    Optional<Long> findOwnerId(@Param("tripId") Long tripId);

    boolean existsByIdAndCreatedBy_Id(Long tripId, Long userId);

    boolean existsByIdAndUsers_Id(Long tripId, Long userId);

    boolean existsByNameIgnoreCaseAndUsers_Id(String name, Long userId);
}
