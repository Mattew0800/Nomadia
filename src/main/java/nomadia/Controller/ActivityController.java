package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.DTO.Activity.ActivityCreateDTO;
import nomadia.DTO.Activity.ActivityResponseDTO;
import nomadia.DTO.Activity.ActivityUpdateRequestDTO;
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
    public ResponseEntity<ActivityResponseDTO> create(
            @Valid @RequestBody ActivityCreateDTO dto ,@RequestBody Long tripId) {
        ActivityResponseDTO response = activityService.create(tripId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ActivityResponseDTO>> listByTrip(
            @RequestBody Long tripId) {
        return ResponseEntity.ok(activityService.listByTrip(tripId));
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> getById(
            @RequestBody Long activityId) {
        return ResponseEntity.ok(activityService.findById(activityId));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> update(
            @Valid @RequestBody ActivityUpdateRequestDTO dto,@RequestBody Long tripId,@RequestBody Long actityId) {
        return ResponseEntity.ok(activityService.update(tripId,actityId,dto));
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(@RequestBody Long activityId,@RequestBody Long tripId) {
        activityService.delete(tripId,activityId);
        return ResponseEntity.noContent().build();
    }
}