package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Trip.*;
import nomadia.Service.TripService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.method.P;


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

    @PostMapping("/view-trip")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> searchTrip(@AuthenticationPrincipal UserDetailsImpl me,
                                        @RequestBody TripIdRequestDTO request) {
        return tripService.findById(request.getTripId())
                .map(trip -> ResponseEntity.ok(TripResponseDTO.fromEntity(trip)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add-user")
    @PreAuthorize("@tripSecurity.isOwner(#body.tripId, #me.id)")
    public ResponseEntity<String> addUser(
                                     @Valid @RequestBody TripAddUserByEmailDTO body,
                                     @AuthenticationPrincipal UserDetailsImpl me) {
        try{
            tripService.addUserToTrip(body.getTripId(), body.getEmail(), me.getId());
            return ResponseEntity.ok("Usuario agregado con exito");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteTrip(
            @RequestBody TripIdRequestDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me,// a chequear
            Authentication authentication) {
        if (!tripService.isOwner(dto.getTripId(), authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para eliminar este viaje");
        }
        try {
            tripService.deleteTrip(dto.getTripId());
            return ResponseEntity.noContent().build();
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El viaje no existe");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar el viaje: tiene relaciones activas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el viaje");
        }
    }

    @PutMapping("/update") // a chequear
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateTrip(
                                        @Valid @RequestBody TripUpdateDTO dto,
                                        @AuthenticationPrincipal UserDetailsImpl me,Authentication authentication) {
        if (!tripService.isOwner(dto.getTripId(), authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para eliminar este viaje");
        }
        return tripService.updateTrip(dto.getTripId(), dto, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/remove-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeUser(@Valid @RequestBody TripAddUserByEmailDTO dto,
                                             @AuthenticationPrincipal UserDetailsImpl me,
                                            Authentication authentication
                                             ){
        if (!tripService.isOwner(dto.getTripId(), authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try{
            tripService.removeUserFromTrip(dto.getTripId(), dto.getEmail(), me.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
