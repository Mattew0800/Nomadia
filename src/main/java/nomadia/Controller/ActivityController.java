package nomadia.Controller;

import nomadia.DTO.Activity.ActivityCreateRequestDTO;
import nomadia.DTO.Activity.ActivityResponseDTO;
import nomadia.Service.ActivityService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

// todo esto a chequear
@RestController
@RequestMapping("/nomadia/{tripId}/activities") //a chequear en especial como vincular el id del viaje en la url
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ActivityResponseDTO> create(@PathVariable Long tripId,
                                                      @Valid @RequestBody ActivityCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(tripId, dto));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponseDTO>> list(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.listByTrip(tripId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDTO> get(@PathVariable Long tripId,
                                                   @PathVariable Long activityId) {
        return ResponseEntity.ok(service.get(tripId, activityId));
    }

//    @PutMapping("/{activityId}")
//    public ResponseEntity<ActivityResponseDTO> update(@PathVariable Long tripId,
//                                                      @PathVariable Long activityId,
//                                                      @Valid @RequestBody ActivityUpdateRequestDTO dto) {
//        return ResponseEntity.ok(service.update(tripId, activityId, dto));
//    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(@PathVariable Long tripId,
                                       @PathVariable Long activityId) {
        service.delete(tripId, activityId);
        return ResponseEntity.noContent().build();
    }
}
