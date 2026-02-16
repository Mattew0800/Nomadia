package nomadia.Repository;

import nomadia.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTripId(Long tripId);
    int countByTripIdAndFromUserId(Long tripId, Long toUserId);
}
