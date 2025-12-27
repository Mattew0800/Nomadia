package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Trip.*;
import nomadia.Service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("nomadia/trip")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> createTrip(
            @Valid @RequestBody TripCreateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me) {
        TripResponseDTO created = tripService.createTrip(dto, me.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TripListDTO>> lookMyTrips(@AuthenticationPrincipal UserDetailsImpl me) {
        List<TripListDTO> trips = tripService.getMyTrips(me.getId());
        if (trips.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(trips);
    }

    @PostMapping("/view-trip")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> viewTrip(
            @AuthenticationPrincipal UserDetailsImpl me,
            @RequestBody TripIdRequestDTO request) {
        TripResponseDTO trip = tripService.viewTrip(request.getTripId(), me.getId());
        return ResponseEntity.ok(trip);
    }

    @PostMapping("/get-travelers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listTravelers(
            @AuthenticationPrincipal UserDetailsImpl me,
            @RequestBody TripIdRequestDTO dto) {
        var travelers = tripService.getTravelers(dto.getTripId(), me.getId());
        if (travelers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(travelers);
    }

    @PostMapping("/add-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addUser(
            @Valid @RequestBody TripAddUserByEmailDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me) {
        System.out.println("DTO email = '" + dto.getEmail() + "'");
        System.out.println("DTO tripId = " + dto.getTripId());
        System.out.println("ME userId = " + me.getId());
        System.out.println("tripService class = " + tripService.getClass());

        return ResponseEntity.ok(tripService.addUserToTrip(dto.getTripId(), dto.getEmail(),me.getId()));
    }

    @PutMapping("/remove-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeUser(
            @Valid @RequestBody TripAddUserByEmailDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me) {
        tripService.removeUserFromTrip(dto.getTripId(), dto.getEmail(), me.getId());
        return ResponseEntity.ok("Usuario removido con exito");
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteTrip(
            @RequestBody TripIdRequestDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me) {
        tripService.deleteTrip(dto.getTripId(),me.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateTrip(
            @Valid @RequestBody TripUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me) {
        return tripService.updateTrip(dto.getTripId(), dto, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
