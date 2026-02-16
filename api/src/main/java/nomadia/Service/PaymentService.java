package nomadia.Service;

import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.AutoSettleRequestDTO;
import nomadia.DTO.UserBalance.DebtDTO;
import nomadia.DTO.UserBalance.UserBalanceDTO;
import nomadia.Model.Payment;
import nomadia.Model.Trip;
import nomadia.Repository.PaymentRepository;
import nomadia.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final TripService tripService;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public PaymentService(PaymentRepository paymentRepository, TripService tripService, UserRepository userRepository, ExpenseService expenseService) {
        this.paymentRepository = paymentRepository;
        this.tripService = tripService;
        this.userRepository = userRepository;
        this.expenseService = expenseService;
    }

    @Transactional
    public void settleAutomatically(AutoSettleRequestDTO dto, Long userId) {
        Trip trip = tripService.getTripAndValidateMember(dto.getTripId(), userId);
        List<UserBalanceDTO> balances =
                expenseService.getTripBalance(new TripIdRequestDTO(dto.getTripId()),userId);
        List<DebtDTO> debts = expenseService.calculateDebts(balances);
        DebtDTO debt = debts.stream()
                .filter(d ->
                        d.getDebtorId().equals(userId) &&
                                d.getCreditorId().equals(dto.getCreditorId())
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No tenes deudas activas"));
        Payment payment = new Payment();
        payment.setTrip(trip);
        payment.setFromUser(userRepository.getReferenceById(userId));
        payment.setToUser(userRepository.getReferenceById(dto.getCreditorId()));
        payment.setAmount(debt.getAmount());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}