package nomadia.nomadia.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nomadia.nomadia.auth.dto.AuthResponse;
import nomadia.nomadia.auth.dto.LoginRequest;
import nomadia.nomadia.auth.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService service;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req){
        return ResponseEntity.ok(service.login(req));
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req){
        service.register(req);
        return ResponseEntity.ok().build();
    }
}