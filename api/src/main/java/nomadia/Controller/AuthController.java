package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.DTO.Login.LoginRequestDTO;
import nomadia.DTO.Login.LoginResponseDTO;
import nomadia.DTO.Login.RegisterRequestDTO;
import nomadia.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomadia/auth")
@CrossOrigin (origins={"http://localhost:4200","http://localhost:8080","https://nomadia-viajes.vercel.app"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(request));
    }

    @PostMapping("/register-admin") // esto para prueba dsps se va
    public ResponseEntity<LoginResponseDTO> registerAdmin(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login( @Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout") // por mera cortesia
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
