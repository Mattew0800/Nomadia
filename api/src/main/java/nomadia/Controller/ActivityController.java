package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Activity.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.ActivitySummaryDTO;
import nomadia.DTO.UserBalance.DebtDTO;
import nomadia.Service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
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
    public ResponseEntity<ActivityResponseDTO> create(@Valid @RequestBody ActivityCreateDTO request, @AuthenticationPrincipal UserDetailsImpl me){
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(request.getTripId(),request,me.getId()));
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ActivityResponseDTO>> listMine(@AuthenticationPrincipal UserDetailsImpl me,@RequestBody ActivityFilterRequestDTO dto ) {
        return ResponseEntity.ok(activityService.getActivitiesForUserAndTrip(me.getId(), dto));
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> getById(@RequestBody ActivityIdRequestDTO dto) {
        return ResponseEntity.ok(activityService.findById(dto.getActivityId()));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> update(@Valid @RequestBody ActivityUpdateRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(activityService.update(dto.getTripId(),dto.getActivityId(), dto, me.getId()));
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> delete(@RequestBody ActivityIdRequestDTO request, @AuthenticationPrincipal UserDetailsImpl me) {
        activityService.delete(request.getTripId(), request.getActivityId(),me.getId());
        return ResponseEntity.ok(Map.of("message", "Actividad eliminada correctamente"));
    }

    @PostMapping("/debts")//ARREGLAR Y MOVER A TRIPCONTROLLER
    @PreAuthorize("hasRole('USER')")
    public List<DebtDTO> getTripDebts(@RequestBody TripIdRequestDTO tripId,@AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getTripDebts(tripId, me.getId());
    }

    @PostMapping("/total-cost")//FUNCIONA (INCREIBLEMENTE)
    @PreAuthorize("hasRole('USER')")
    public BigDecimal getTotalTripCost(@RequestBody TripIdRequestDTO tripId, @AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getTotalTripCost(tripId,me.getId());
    }

    @PostMapping("/average-cost")//ARREGLAR
    @PreAuthorize("hasRole('USER')")
    public BigDecimal getAverageCostByActivity(@RequestBody ActivityIdRequestDTO activityId,@AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getAverageCostByActivity(activityId,me.getId());
    }

    @PostMapping("/summary")//agregar que sume a
    @PreAuthorize("hasRole('USER')")
    public ActivitySummaryDTO getActivitySummary(@RequestBody ActivityIdRequestDTO activityId, @AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getActivitySummary(activityId,me.getId());
    }
}
