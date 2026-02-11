package nomadia.Service;

import nomadia.DTO.Activity.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.ActivitySummaryDTO;
import nomadia.DTO.UserBalance.DebtDTO;
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

}
