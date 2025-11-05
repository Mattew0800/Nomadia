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

import javax.swing.text.html.Option;
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


    public ActivityResponseDTO create(Long tripId, ActivityCreateDTO dto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        dto.setTripStartDate(trip.getStartDate());
        dto.setTripEndDate(trip.getEndDate());

        boolean solapamiento = trip.getActivities().stream()
                .anyMatch(a -> a.getDate().equals(dto.getDate())
                        && !(dto.getEndTime().isBefore(a.getStartTime()) || dto.getStartTime().isAfter(a.getEndTime())));
        if (solapamiento) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otra actividad en ese horario");
        }
        Activity activity = dto.toEntity();
        activity.setTrip(trip);
        return ActivityResponseDTO.fromEntity(activityRepository.save(activity));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> listByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");

        return activityRepository.findByTripId(tripId)
                .stream().map(ActivityResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponseDTO get(Long tripId, Long activityId) {
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

        if (dto.getName() != null && !dto.getName().isBlank()
                && !a.getName().equalsIgnoreCase(dto.getName())
                && activityRepository.existsByTripIdAndNameIgnoreCase(tripId, dto.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una actividad con ese nombre en este viaje");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) a.setName(dto.getName());
        if (dto.getDate() != null) a.setDate(dto.getDate());
        if (dto.getDescription() != null) a.setDescription(dto.getDescription());

        if (dto.getCost() != null) {
            if (dto.getCost() < 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo no puede ser negativo");
            a.setCost(dto.getCost());
        }

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
