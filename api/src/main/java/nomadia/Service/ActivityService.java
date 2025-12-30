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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class ActivityService{

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
    public ActivityResponseDTO create(Long tripId, ActivityCreateDTO dto,Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
        if(!tripService.isMember(tripId,userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No tenés permiso para modificar este viaje");
        }
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

    public BigDecimal getAllCostByTrip(Long tripId, Long userId) { //PROVISORIO

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        if (!tripService.isMember(tripId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tenés permiso para ver este viaje");
        }
        BigDecimal sum = BigDecimal.ZERO;
        List<Activity> allActivities = activityRepository.findByTripId(tripId);
        for (Activity act : allActivities) {
            if (act.getCost() != null) {
                sum = sum.add(act.getCost());
            }
        }
        return sum;
    }


//    public BigDecimal getDailyCostByTrip(Long tripId,Long userId,LocalDate localdate){//PROVISORIO
//        Trip trip = tripRepository.findById(tripId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
//        if(!tripService.isMember(tripId,userId)){
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No tenés permiso para modificar este viaje");
//        }
//        if(localdate==null){
//            localdate=LocalDate.now();
//        }
//        List<Activity> activityDay=activityRepository.findByTripIdAndDate(tripId,localdate);
//        float sum=0;
//        for(Activity act:activityDay){
//            sum+=act.getCost();
//        }
//        return sum;
//    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getActivitiesForUserAndTrip(// PROVISORIO
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
    public ActivityResponseDTO update(Long tripId, Long activityId, ActivityUpdateRequestDTO dto,Long userId) {
        if(!tripService.isMember(tripId,userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No tenés permiso para modificar esta actividad");
        }
        Activity a = activityRepository.findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Actividad no encontrada en este viaje"));
        dto.applyToEntity(a);
        Activity updated = activityRepository.save(a);
        return ActivityResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long tripId, Long activityId,Long userId) {
        if (!tripService.isMember(tripId,userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No tenés permiso para modificar esta actividad");
        }
        Activity a = activityRepository.findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Actividad no encontrada en este viaje"));
        activityRepository.delete(a);
    }
}
