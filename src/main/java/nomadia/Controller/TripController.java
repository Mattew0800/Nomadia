package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Trip.TripCreateDTO;
import nomadia.DTO.Trip.TripListDTO;
import nomadia.DTO.Trip.TripResponseDTO;
import nomadia.DTO.Trip.TripUpdateDTO;
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

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> createTrip(@Valid @RequestBody TripCreateDTO dto,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TripResponseDTO created = tripService.createTrip(dto, userDetails.getId()); // verificar que no haya un viaje de el con el mismo nombre
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TripListDTO>> lookMyTrips(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TripListDTO> trips = tripService.findMyTrips(userDetails.getId());
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> searchTrip(@AuthenticationPrincipal UserDetailsImpl me,
                                                      @RequestParam String name) {
        return tripService.findByNameForUser(name, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/add-users")
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication)") // creador
    public ResponseEntity<TripResponseDTO> addUser(@PathVariable Long tripId,
                                                   @PathVariable Long userId, // que mande un dto de user para agregar al viaje, tirar error si no esta en la bdd o si ya esta en el viaje
                                                   @AuthenticationPrincipal UserDetailsImpl me) {
        return tripService.addUserToTrip(tripId, userId, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/remove-users/")
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication)") // creador
    public ResponseEntity<Void> removeUser(@PathVariable Long tripId,
                                           @PathVariable Long userId,// que mande un dto de user para agregar al viaje, tirar error si no esta en la bdd o si no esta en el viaje
                                           @AuthenticationPrincipal UserDetailsImpl me) {
        tripService.removeUserFromTrip(tripId, userId, me.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update") // actualizar campos del viaje
    @PreAuthorize("@tripSecurity.isMember(#tripId, authentication)")
    public ResponseEntity<?> updateTrip(@PathVariable Long tripId,
                                        @Valid @RequestBody TripUpdateDTO dto,
                                        @AuthenticationPrincipal UserDetailsImpl me) {
        return tripService.updateTrip(tripId, dto, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
