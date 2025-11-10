package nomadia.Service;

import nomadia.DTO.Activity.ActivityCreateDTO;
import nomadia.DTO.Activity.ActivityResponseDTO;
import nomadia.DTO.Activity.ActivityUpdateRequestDTO;
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
import java.util.List;

@Service
@Transactional
public class ActivityService{

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;

    public ActivityService(ActivityRepository activityRepository, TripRepository tripRepository) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd,
                             LocalTime bStart, LocalTime bEnd) {
        // [aStart, aEnd) vs [bStart, bEnd)
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    @Transactional
    public ActivityResponseDTO create(Long tripId, ActivityCreateDTO dto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));


        if (dto.getDate().isBefore(trip.getStartDate()) || dto.getDate().isAfter(trip.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de la actividad debe estar dentro del rango del viaje");
        }

        List<Activity> sameDay = activityRepository.findByTripIdAndDate(tripId, dto.getDate());

        boolean conflict = sameDay.stream().anyMatch(a ->
                overlaps(dto.getStartTime(), dto.getEndTime(), a.getStartTime(), a.getEndTime())
        );

        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otra actividad en ese horario");
        }

        Activity activity = dto.toEntity();
        activity.setTrip(trip);

        Activity saved = activityRepository.save(activity);
        return ActivityResponseDTO.fromEntity(saved);
    }


    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> listByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");

        return activityRepository.findByTripId(tripId)
                .stream().map(ActivityResponseDTO::fromEntity).toList();
    }

@Transactional(readOnly = true)
public List<ActivityResponseDTO> getActivitiesForUserAndTrip(
        Long userId,
        LocalDate fromDate, LocalDate toDate,
        LocalTime fromTime, LocalTime toTime,Long tripId) {

    if (tripId != null && !tripRepository.existsByIdAndUserId(tripId, userId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No perteneces a este viaje");
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rango de fechas es inválido");
    }
    return activityRepository.findAllByUserTrips(userId, fromDate, toDate, fromTime, toTime,tripId)
            .stream()
            .map(ActivityResponseDTO::fromEntity)
            .toList();
}

    @Transactional(readOnly = true)
    public ActivityResponseDTO get(Long tripId, Long activityId,Long userId) {

        Activity a = activityRepository.findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada en este viaje"));
        return ActivityResponseDTO.fromEntity(a);
    }

    @Transactional(readOnly = true)
    public ActivityResponseDTO findById(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
        return ActivityResponseDTO.fromEntity(activity);
    }

    @Transactional
    public ActivityResponseDTO update(Long tripId, Long activityId, ActivityUpdateRequestDTO dto) {

        Activity a = activityRepository.findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Actividad no encontrada en este viaje"));

        dto.applyToEntity(a);
        Activity updated = activityRepository.save(a);
        return ActivityResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long tripId, Long activityId) {
        Activity a = activityRepository.findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Actividad no encontrada en este viaje"));
        activityRepository.delete(a);
    }

}
