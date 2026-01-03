package nomadia.Repository;

import nomadia.Model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
    SELECT COALESCE(SUM(e.totalAmount), 0)
    FROM Expense e
    WHERE e.activity.id = :activityId
    """)
    BigDecimal getTotalSpentByActivity(@Param("activityId") Long activityId);

    @Query("""
    SELECT a.id, COALESCE(SUM(e.totalAmount), 0)
    FROM Expense e
    JOIN e.activity a
    JOIN a.trip t
    WHERE t.id = :tripId
    GROUP BY a.id
    """)
    List<Object[]> findTotalSpentByActivity(@Param("tripId") Long tripId);

    @Query("""
    SELECT COALESCE(SUM(e.totalAmount), 0)
    FROM Expense e
    WHERE e.activity.trip.id = :tripId
    """)
    BigDecimal getTotalExpensesByTrip(@Param("tripId") Long tripId);
}

