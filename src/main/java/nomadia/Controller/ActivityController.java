package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Activity.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.Service.ActivityService;
import nomadia.Service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/nomadia/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final TripService tripService;


    public ActivityController(ActivityService activityService, TripService tripService) {
        this.activityService = activityService;
        this.tripService = tripService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> create(@Valid @RequestBody ActivityCreateDTO request) {
        try {
            ActivityResponseDTO response = activityService.create(
                    request.getTripId(),
                    request
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
    public ResponseEntity<?> listMine(@AuthenticationPrincipal UserDetailsImpl me, @RequestBody TripIdRequestDTO dto){
        var list = activityService.getActivitiesForUserAndTrip(me.getId(), null, null, null, null, dto.getTripId());
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getById(@RequestBody ActivityIdRequestDTO dto) {
        try {
            ActivityResponseDTO response = activityService.findById(dto.getActivityId());
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
    public ResponseEntity<?> update(@Valid @RequestBody ActivityUpdateRequestDTO dto) {
        try {
            ActivityResponseDTO response = activityService.update(
                    dto.getTripId(),
                    dto.getActivityId(),
                    dto
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
