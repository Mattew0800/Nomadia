package nomadia.Service;

import nomadia.DTO.Activity.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.ActivitySummaryDTO;
import nomadia.DTO.UserBalance.DebtDTO;
import nomadia.DTO.UserBalance.UserBalanceDTO;
import nomadia.DTO.User.UserResponseDTO;
import nomadia.Model.Activity;
import nomadia.Model.Trip;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.ExpenseRepository;
import nomadia.Repository.ParticipantRepository;
import nomadia.Repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    public ActivityService(ActivityRepository activityRepository, TripRepository tripRepository, TripService tripService, ParticipantRepository participantRepository, ExpenseRepository expenseRepository) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
        this.tripService = tripService;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
    }
    //es como un dto solo para el service, piola para evitar duplicacion de codigo :D
    private record ActivityCostData(
            Activity activity,
            BigDecimal totalSpent,
            int participants,
            BigDecimal average
    ) {}

    private boolean overlaps(LocalTime aStart, LocalTime aEnd,
                             LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    @Transactional
    public ActivityResponseDTO create(Long tripId, ActivityCreateDTO dto, Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Viaje no encontrado"));
        if (!tripService.isMember(tripId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tenés permiso para modificar este viaje");
        }
        if (dto.getDate().isBefore(trip.getStartDate())
                || dto.getDate().isAfter(trip.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de la actividad debe estar dentro del rango del viaje");
        }
        List<Activity> sameDay =
                activityRepository.findByTripIdAndDate(tripId, dto.getDate());
        boolean conflict = sameDay.stream().anyMatch(a ->
                overlaps(dto.getStartTime(), dto.getEndTime(),
                        a.getStartTime(), a.getEndTime())
        );
        if (conflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra actividad en ese horario");
        }
        Activity activity = dto.toEntity();
        activity.setTrip(trip);

        return ActivityResponseDTO.fromEntity(
                activityRepository.save(activity)
        );
    }

    @Transactional
    public ActivityResponseDTO update(Long tripId,Long activityId,ActivityUpdateRequestDTO dto,Long userId ) {
        if (!tripService.isMember(tripId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para modificar esta actividad");
        }
        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Actividad no encontrada en este viaje"));
        dto.applyToEntity(activity);
        return ActivityResponseDTO.fromEntity(
                activityRepository.save(activity)
        );
    }

    @Transactional
    public void delete(Long tripId, Long activityId, Long userId) {
        if (!tripService.isMember(tripId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para modificar esta actividad");
        }
        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Actividad no encontrada en este viaje"));
        activityRepository.delete(activity);
    }
    //NO SE USA
    public List<ActivityResponseDTO> listByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Viaje no encontrado");
        }
        return activityRepository.findByTripId(tripId)
                .stream()
                .map(ActivityResponseDTO::fromEntity)
                .toList();
    }

    public ActivityResponseDTO get(Long tripId, Long activityId, Long userId) {
        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Actividad no encontrada en este viaje"));
        return ActivityResponseDTO.fromEntity(activity);
    }

    public ActivityResponseDTO findById(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Actividad no encontrada"));
        return ActivityResponseDTO.fromEntity(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getActivitiesForUserAndTrip(Long userId,ActivityFilterRequestDTO filter) {
        List<Long> tripIds = filter.getTripIds();
        LocalDate fromDate = filter.getFromDate();
        LocalDate toDate = filter.getToDate();
        LocalTime fromTime = filter.getFromTime();
        LocalTime toTime = filter.getToTime();
        if (tripIds != null && tripIds.isEmpty()) {
            tripIds = null;
        }
        if (tripIds != null) {
            long validTrips = tripRepository.countUserTrips(userId, tripIds);
            if (validTrips != tripIds.size()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No perteneces a uno o más viajes seleccionados");
            }
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El rango de fechas es inválido");
        }
        return activityRepository
                .findAllByUserTrips(
                        userId,
                        fromDate,
                        toDate,
                        fromTime,
                        toTime,
                        tripIds
                )
                .stream()
                .map(ActivityResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserBalanceDTO> getUserBalancesByTrip(
            Long tripId,
            Long requesterId
    ) {
        if (!tripService.isMember(tripId, requesterId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenes permiso para ver esta información");
        }
        Map<Long, UserBalanceDTO> balances = new HashMap<>();
        for (UserResponseDTO user :
                tripService.getTravelers(tripId, requesterId)) {
            balances.put(
                    user.getId(),
                    new UserBalanceDTO(user.getId(),
                            user.getEmail(),
                            BigDecimal.ZERO));
        }
        Map<Long, BigDecimal> totalSpentByActivity =
                expenseRepository.findTotalSpentByActivity(tripId)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (Long) r[0],
                                r -> (BigDecimal) r[1]
                        ));
        Map<Long, Integer> participantsByActivity =
                participantRepository
                        .countParticipantsGroupedByActivity(tripId);
        for (Object[] row :
                participantRepository.findPaidByUserAndActivity(tripId)) {
            Long activityId = (Long) row[0];
            Long userId = (Long) row[1];
            BigDecimal paid = (BigDecimal) row[2];
            BigDecimal total = totalSpentByActivity.get(activityId);
            if (total == null || total.compareTo(BigDecimal.ZERO) == 0) continue;
            int participants = participantsByActivity.get(activityId);
            if (participants == 0) continue;
            BigDecimal shouldPay = total.divide(
                    BigDecimal.valueOf(participants),
                    2,
                    RoundingMode.HALF_UP);
            BigDecimal delta = paid.subtract(shouldPay);
            balances.get(userId).setBalance(balances.get(userId).getBalance().add(delta));
        }
        return new ArrayList<>(balances.values());
    }

    public List<DebtDTO> calculateDebts(List<UserBalanceDTO> balances) {
        List<UserBalanceDTO> creditors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(b -> new UserBalanceDTO(
                        b.getUserId(),
                        b.getEmail(),
                        b.getBalance()))
                .toList();
        List<UserBalanceDTO> debtors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .map(b -> new UserBalanceDTO(
                        b.getUserId(),
                        b.getEmail(),
                        b.getBalance().abs()))
                .toList();
        List<DebtDTO> debts = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            UserBalanceDTO debtor = debtors.get(i);
            UserBalanceDTO creditor = creditors.get(j);
            BigDecimal amount =
                    debtor.getBalance().min(creditor.getBalance());
            debts.add(new DebtDTO(debtor.getUserId(),debtor.getEmail(),creditor.getUserId(),creditor.getEmail(),
                    amount
            ));
            debtor.setBalance(debtor.getBalance().subtract(amount));
            creditor.setBalance(creditor.getBalance().subtract(amount));
            if (debtor.getBalance().compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.getBalance().compareTo(BigDecimal.ZERO) == 0) j++;
        }
        return debts;
    }

    @Transactional(readOnly = true)
    public List<DebtDTO> getTripDebts(TripIdRequestDTO tripId, Long userId) {
        if (!tripService.isMember(tripId.getTripId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para ver esta información");
        }
        return calculateDebts(
                getUserBalancesByTrip(tripId.getTripId(), userId)
        );
    }

    private ActivityCostData getValidatedActivityCostData(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Actividad no encontrada"));
        Long tripId = activity.getTrip().getId();
        if (!tripService.isMember(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No perteneces a este viaje");
        }
        BigDecimal totalSpent = expenseRepository.getTotalSpentByActivity(activityId);//ver nota de repositorio
        int participants = participantRepository.countParticipantsByActivity(activityId);
        BigDecimal average = participants == 0 //ver si conviene seguir teniendo esto ya que el total gastado va a cambiar si la actividad tiene un costo de por sí
                ? BigDecimal.ZERO
                : totalSpent.divide(
                BigDecimal.valueOf(participants),
                2,
                RoundingMode.HALF_UP);
        return new ActivityCostData(activity, totalSpent, participants, average);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageCostByActivity(ActivityIdRequestDTO activityId, Long userId) {
        return getValidatedActivityCostData(activityId.getActivityId(), userId).average();
    }

    @Transactional(readOnly = true)
    public ActivitySummaryDTO getActivitySummary(ActivityIdRequestDTO activityId, Long userId) {
        ActivityCostData data = getValidatedActivityCostData(activityId.getActivityId(), userId);
        return new ActivitySummaryDTO(
                data.activity().getId(),
                data.activity().getName(),
                data.totalSpent(),
                data.participants(),
                data.average()
        );
    }
}
