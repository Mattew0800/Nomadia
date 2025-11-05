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
    public ResponseEntity<ActivityResponseDTO> create(@Valid @RequestBody ActivityWithTripDTO request) {
        ActivityResponseDTO response = activityService.create(request.getTripId(), request.getActivity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ActivityResponseDTO>> listByTrip(@RequestBody TripIdRequestDTO tripIdRequestDTO) {
        List<ActivityResponseDTO> list = activityService.listByTrip(tripIdRequestDTO.getTripId());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> getById(@RequestBody ActivityIdRequestDTO request) {
        ActivityResponseDTO response = activityService.findById(request.getActivityId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> update(@Valid @RequestBody ActivityUpdateWithTripDTO request) {
        ActivityResponseDTO response = activityService.update(
                request.getTripId(),
                request.getActivityId(),
                request.getUpdate()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> delete(@RequestBody ActivityIdRequestDTO request) {
        activityService.delete(request.getTripId(), request.getActivityId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Actividad eliminada correctamente"));
    }

}
