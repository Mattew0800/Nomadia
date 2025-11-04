package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.DTO.Activity.ActivityCreateDTO;
import nomadia.DTO.Activity.ActivityResponseDTO;
import nomadia.DTO.Activity.ActivityUpdateRequestDTO;
import nomadia.Service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomadia/{tripId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/create")
    public ResponseEntity<ActivityResponseDTO> create(
            @PathVariable Long tripId,
            @Valid @RequestBody ActivityCreateDTO dto) {
        ActivityResponseDTO response = activityService.create(tripId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping ("/get-activities")
    public ResponseEntity<List<ActivityResponseDTO>> list(@PathVariable Long tripId) {
        return ResponseEntity.ok(activityService.listByTrip(tripId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDTO> get(
            @PathVariable Long tripId,
            @PathVariable Long activityId) {
        return ResponseEntity.ok(activityService.get(tripId, activityId));
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tripId,
            @PathVariable Long activityId) {
        activityService.delete(tripId, activityId);
        return ResponseEntity.noContent().build();
    }



    @PutMapping("/modify-activty/{activityId}")
    public ResponseEntity<ActivityResponseDTO> update(@PathVariable Long tripId,
                                                      @PathVariable Long activityId,
                                                      @Valid @RequestBody ActivityUpdateRequestDTO dto) {
        return ResponseEntity.ok(activityService.update(tripId, activityId, dto));
   }
}

