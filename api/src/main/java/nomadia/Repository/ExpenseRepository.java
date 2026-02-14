package nomadia.Repository;

import nomadia.Model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
        SELECT e
        FROM Expense e
        JOIN FETCH e.trip t
        WHERE e.id = :expenseId
    """)
    Optional<Expense> findByIdWithTrip(@Param("expenseId") Long expenseId);

    @Query("""
        SELECT COALESCE(SUM(e.totalAmount), 0)
        FROM Expense e
        WHERE e.trip.id = :tripId
    """)
    BigDecimal getTotalByTrip(@Param("tripId") Long tripId);

    @Query("SELECT e FROM Expense e WHERE e.trip.id = :tripId")
    List<Expense> findByTripId(@Param ("tripId")Long tripId);

    @Query("SELECT e FROM Expense e WHERE e.activity.id = :activityId")
    List<Expense> findByActivityId(@Param("activityId")Long activityId);

}

