package nomadia.Controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Trip.*;
import nomadia.Service.TripService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("nomadia/trip")
@CrossOrigin (origins={"http://localhost:4200","http://localhost:8080"})
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createTrip(
            @Valid @RequestBody TripCreateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl me) {
        try {
            TripResponseDTO created = tripService.createTrip(dto, me.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo crear el viaje"));
        }
    }

    @GetMapping("/my-trips")// chequeado
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> lookMyTrips(@AuthenticationPrincipal UserDetailsImpl me) {
        List<TripListDTO> trips=tripService.getMyTrips(me.getId());
        if(trips.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No tenes viajes realizados");
        }
        return ResponseEntity.ok(trips);
    }

    @PostMapping("/view-trip")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> searchTrip(@AuthenticationPrincipal UserDetailsImpl me,
                                        @RequestBody TripIdRequestDTO request) {
        if(!tripService.isMember(request.getTripId(), me.getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenes permisos para ver este viaje");
        }
        return tripService.findById(request.getTripId())
                .map(trip -> ResponseEntity.ok(TripResponseDTO.fromEntity(trip)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addUser(@Valid @RequestBody TripAddUserByEmailDTO dto,
                                     @AuthenticationPrincipal UserDetailsImpl me) {

        if (!tripService.isOwner(dto.getTripId(), me.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para modificar este viaje");
        }

        try {
            Optional<TripResponseDTO> dtoo= tripService.addUserToTrip(dto.getTripId(), dto.getEmail());
            return ResponseEntity.ok(Map.of("message","Usuario agregado con Exito"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al agregar el usuario al viaje");
        }
    }

    @PutMapping("/remove-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeUser(@Valid @RequestBody TripAddUserByEmailDTO dto,
                                        @AuthenticationPrincipal UserDetailsImpl me) {
        if (!tripService.isOwner(dto.getTripId(), me.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para modificar este viaje");
        }
        try {
            tripService.removeUserFromTrip(dto.getTripId(), dto.getEmail());
            return ResponseEntity.ok().body("Usuario removido con exito");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El usuario o el viaje no existen");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al intentar remover el usuario");
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteTrip(@RequestBody TripIdRequestDTO dto,
                                        @AuthenticationPrincipal UserDetailsImpl me) {
        if (!tripService.isOwner(dto.getTripId(), me.getId())) {
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

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateTrip(@Valid @RequestBody TripUpdateDTO dto,
                                        @AuthenticationPrincipal UserDetailsImpl me) {

        if (!tripService.isOwner(dto.getTripId(), me.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para eliminar este viaje");
        }

        return tripService.updateTrip(dto.getTripId(), dto, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


}
