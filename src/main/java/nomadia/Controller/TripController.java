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
@CrossOrigin (origins={"http://localhost:4200","http://localhost:8080"})
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

    @PostMapping("/add-users") // a chequear , en especial como vincular el id del viaje en la url
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication)")
    public ResponseEntity<?> addUser(@RequestBody Long tripId,
                                     @Valid @RequestBody TripAddUserByEmailDTO body,
                                     @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(tripService.addUserToTrip(tripId, body.getEmail(), me.getId()));
    }

    @DeleteMapping("/users")
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication)")
    public ResponseEntity<Void> removeUserByEmail(@RequestBody Long tripId,
                                                  @Valid @RequestBody TripAddUserByEmailDTO body,
                                                  @AuthenticationPrincipal UserDetailsImpl me) {
        tripService.removeUserFromTrip(tripId, body.getEmail(), me.getId());
        return ResponseEntity.noContent().build();
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
