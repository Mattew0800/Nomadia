package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Trip.*;
import nomadia.DTO.UserBalance.AutoSettleRequestDTO;
import nomadia.DTO.UserBalance.UserDebtProgressDTO;
import nomadia.Service.ExpenseService;
import nomadia.Service.PaymentService;
import nomadia.Service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("nomadia/trip")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
public class TripController {

    private final TripService tripService;
    private final ExpenseService expenseService;
    private final PaymentService paymentService;

    public TripController(TripService tripService,ExpenseService expenseService,PaymentService paymentService) {
        this.tripService = tripService;
        this.expenseService = expenseService;
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> createTrip(@Valid @RequestBody TripCreateDTO dto,@AuthenticationPrincipal UserDetailsImpl me){
        return ResponseEntity.ok(tripService.createTrip(dto, me.getId()));
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TripListDTO>> lookMyTrips(@AuthenticationPrincipal UserDetailsImpl me){
        return ResponseEntity.ok(tripService.getMyTrips(me.getId()));
    }

    @PostMapping("/view-trip")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TripResponseDTO> viewTrip(@AuthenticationPrincipal UserDetailsImpl me,@RequestBody TripIdRequestDTO request){
        return ResponseEntity.ok(tripService.viewTrip(request.getTripId(), me.getId()));
    }

    @PostMapping("/get-travelers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listTravelers(@AuthenticationPrincipal UserDetailsImpl me, @RequestBody TripIdRequestDTO dto) {
        return ResponseEntity.ok(tripService.getTravelers(dto.getTripId(), me.getId()));
    }

    @PostMapping("/add-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addUser(@Valid @RequestBody TripAddUserByEmailDTO dto,@AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(tripService.addUserToTrip(dto.getTripId(), dto.getEmail(),me.getId()));
    }

    @PutMapping("/remove-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeUser(@Valid @RequestBody TripAddUserByEmailDTO dto,@AuthenticationPrincipal UserDetailsImpl me) {
        tripService.removeUserFromTrip(dto.getTripId(), dto.getEmail(), me.getId());
        return ResponseEntity.ok(Map.of("message", "Usuario removido con exito!"));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteTrip(@RequestBody TripIdRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        tripService.deleteTrip(dto.getTripId(),me.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateTrip(@Valid @RequestBody TripUpdateDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return tripService.updateTrip(dto.getTripId(), dto, me.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/remove-self")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeSelf(@Valid @RequestBody TripIdRequestDTO dto,@AuthenticationPrincipal UserDetailsImpl me){
        tripService.removeSelfFromTrip(dto.getTripId(), me.getId());
        return ResponseEntity.ok(Map.of("message", "Has sido eliminado del viaje correctamente"));
    }

    @PostMapping("/debts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDebtProgressDTO> getTripDebts(@Valid @RequestBody TripIdRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(expenseService.getTripDebts(expenseService.calculateDebts(expenseService.getTripBalance(dto,me.getId())),me.getId(),dto.getTripId()));
    }

    @PostMapping("/settle-debt")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> settleDebt(@Valid @RequestBody AutoSettleRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        paymentService.settleAutomatically(dto, me.getId());
        return ResponseEntity.ok().body(Map.of("message", "Deuda saldada correctamente")
        );
    }
}
