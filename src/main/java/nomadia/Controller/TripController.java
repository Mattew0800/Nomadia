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
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/create") //chequeado
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> createTrip(@Valid @RequestBody TripCreateDTO dto,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TripResponseDTO created = tripService.createTrip(dto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/my-trips")// chequeado
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TripListDTO>> lookMyTrips(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TripListDTO> trips = tripService.findMyTrips(userDetails.getId());
        return ResponseEntity.ok(trips);
    }

//    @GetMapping("/search")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<TripResponseDTO> searchTrip(@AuthenticationPrincipal UserDetailsImpl me,
//                                                      @RequestBody String name) {
//        return tripService.findByNameForUser(name, me.getId())
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
//    }

    @PostMapping("/{tripId}/add-users") // a chequear , en especial como vincular el id del viaje en la url
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication)")
    public ResponseEntity<?> addUser(@PathVariable Long tripId,
                                     @Valid @RequestBody TripAddUserByEmailDTO body,
                                     @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(tripService.addUserToTrip(tripId, body.getEmail(), me.getId()));
    }

    @DeleteMapping("/{tripId}/users") // a chequear, en especial como vincular el id del viaje en la url
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication)") // defensa 1
    public ResponseEntity<Void> removeUserByEmail(@PathVariable Long tripId,
                                                  @Valid @RequestBody TripAddUserByEmailDTO body,
                                                  @AuthenticationPrincipal UserDetailsImpl me) {
        tripService.removeUserFromTrip(tripId, body.getEmail(), me.getId()); // defensa 2 dentro del service
        return ResponseEntity.noContent().build(); // 204
    }

    @PutMapping("/update") // a chequear
    @PreAuthorize("@tripSecurity.isMember(#tripId, authentication)")
    public ResponseEntity<?> updateTrip(@PathVariable Long tripId,
                                        @Valid @RequestBody TripUpdateDTO dto,
                                        @AuthenticationPrincipal UserDetailsImpl me) {
        return tripService.updateTrip(tripId, dto, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
