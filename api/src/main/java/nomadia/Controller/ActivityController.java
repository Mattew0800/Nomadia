package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Activity.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.ActivitySummaryDTO;
import nomadia.DTO.UserBalance.DebtDTO;
import nomadia.Service.ActivityService;
import nomadia.Service.TripService;
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
    private final TripService tripService;

    public ActivityController(ActivityService activityService, TripService tripService) {
        this.activityService = activityService;
        this.tripService = tripService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> create(@Valid @RequestBody ActivityCreateDTO request, @AuthenticationPrincipal UserDetailsImpl me) {
        ActivityResponseDTO response = activityService.create(
                request.getTripId(),
                request,me.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ActivityResponseDTO>> listMine(
            @AuthenticationPrincipal UserDetailsImpl me,
            @RequestBody ActivityFilterRequestDTO dto) {

        var list = activityService.getActivitiesForUserAndTrip(
                me.getId(),
                dto.getFromDate(), dto.getToDate(),
                dto.getFromTime(), dto.getToTime(),
                dto.getTripId()
        );
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> getById(@RequestBody ActivityIdRequestDTO dto) {
        ActivityResponseDTO response = activityService.findById(dto.getActivityId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ActivityResponseDTO> update(@Valid @RequestBody ActivityUpdateRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        ActivityResponseDTO response = activityService.update(
                dto.getTripId(),
                dto.getActivityId(),
                dto, me.getId()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> delete(@RequestBody ActivityIdRequestDTO request, @AuthenticationPrincipal UserDetailsImpl me) {
        activityService.delete(request.getTripId(), request.getActivityId(),me.getId());
        return ResponseEntity.ok(Map.of("message", "Actividad eliminada correctamente"));
    }

    @PostMapping("/debts")
    @PreAuthorize("hasRole('USER')")
    public List<DebtDTO> getTripDebts(@RequestBody TripIdRequestDTO tripId,@AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getTripDebts(tripId, me.getId());
    }

    @PostMapping("/total-cost")
    @PreAuthorize("hasRole('USER')")
    public BigDecimal getTotalTripCost(@RequestBody TripIdRequestDTO tripId, @AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getTotalTripCost(tripId,me.getId());
    }

    @PostMapping("/average-cost")
    @PreAuthorize("hasRole('USER')")
    public BigDecimal getAverageCostByActivity(@RequestBody ActivityIdRequestDTO activityId,@AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getAverageCostByActivity(activityId,me.getId());
    }

    @PostMapping("/summary")
    @PreAuthorize("hasRole('USER')")
    public ActivitySummaryDTO getActivitySummary(@RequestParam ActivityIdRequestDTO activityId, @AuthenticationPrincipal UserDetailsImpl me){
        return activityService.getActivitySummary(activityId,me.getId());
    }
}
