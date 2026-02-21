package nomadia.Service;

import nomadia.DTO.Activity.*;
import nomadia.Model.Activity;
import nomadia.Model.Trip;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;

    public ActivityService(ActivityRepository activityRepository, TripRepository tripRepository, TripService tripService) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
        this.tripService = tripService;
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd,
                             LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    @Transactional
    public ActivityResponseDTO create(Long tripId, ActivityCreateDTO dto, Long userId) {
        Trip trip=tripService.getTripAndValidateMember(tripId, userId);
        if (dto.getDate().isBefore(trip.getStartDate())|| dto.getDate().isAfter(trip.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La fecha de la actividad debe estar dentro del rango del viaje");
        }
        List<Activity> sameDay =activityRepository.findByTripIdAndDate(tripId, dto.getDate());
        boolean conflict = sameDay.stream().anyMatch(a ->
                overlaps(dto.getStartTime(), dto.getEndTime(),
                        a.getStartTime(), a.getEndTime())
        );
        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya existe otra actividad en ese horario");
        }
        Activity activity = dto.toEntity();
        activity.setTrip(trip);
        return ActivityResponseDTO.fromEntity(
                activityRepository.save(activity)
        );
    }

    @Transactional
    public ActivityResponseDTO update(Long tripId,Long activityId,ActivityUpdateRequestDTO dto,Long userId ) {
        tripService.getTripAndValidateMember(tripId, userId);
        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Actividad no encontrada en este viaje"));
        dto.applyToEntity(activity);
        return ActivityResponseDTO.fromEntity(activityRepository.save(activity));
    }

    @Transactional
    public void delete(Long tripId, Long activityId, Long userId) {
       tripService.getTripAndValidateMember(tripId, userId);
        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Actividad no encontrada en este viaje"));
        activityRepository.delete(activity);
    }

    public List<ActivityResponseDTO> listByTrip(Long tripId,Long userId) {
        tripService.getTripAndValidateMember(tripId, userId);
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
                .findAllByUserTrips(userId,fromDate,toDate,fromTime,toTime,tripIds)
                .stream()
                .map(ActivityResponseDTO::fromEntity)
                .toList();
    }

}
