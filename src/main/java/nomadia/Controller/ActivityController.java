package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.DTO.ActivityAndTrip.ActivityWithTripDTO;
import nomadia.DTO.Activity.*;
import nomadia.DTO.ActivityAndTrip.ActivityUpdateWithTripDTO;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.Service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nomadia/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> create(@Valid @RequestBody ActivityWithTripDTO request) {
        try {
            ActivityResponseDTO response = activityService.create(
                    request.getTripId(),
                    request.getActivity()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listByTrip(@RequestBody TripIdRequestDTO tripIdRequestDTO) {
        try {
            List<ActivityResponseDTO> list = activityService.listByTrip(tripIdRequestDTO.getTripId());
            return ResponseEntity.ok(list);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }


    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getById(@RequestBody ActivityIdRequestDTO request) {
        try {
            ActivityResponseDTO response = activityService.findById(request.getActivityId());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> update(@Valid @RequestBody ActivityUpdateWithTripDTO request) {
        try {
            ActivityResponseDTO response = activityService.update(
                    request.getTripId(),
                    request.getActivityId(),
                    request.getUpdate()
            );
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> delete(@RequestBody ActivityIdRequestDTO request) {
        try {
            activityService.delete(request.getTripId(), request.getActivityId());
            return ResponseEntity.ok(Map.of("message", "Actividad eliminada correctamente"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

}
