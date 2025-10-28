package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.DTO.Login.LoginRequestDTO;
import nomadia.DTO.Login.LoginResponseDTO;
import nomadia.DTO.Login.RegisterRequestDTO;
import nomadia.Model.User;
import nomadia.Service.AuthService;
import nomadia.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static nomadia.Enum.Role.ADMIN;
import static nomadia.Enum.Role.USER;

@RestController
@RequestMapping("/nomadia/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(USER);

        User savedUser = userService.createUser(user);

        String token = authService.generateToken(savedUser);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/register-admin")
    public ResponseEntity<LoginResponseDTO> registerAdmin(@Valid @RequestBody RegisterRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(ADMIN);

        User savedUser = userService.createUser(user);

        String token = authService.generateToken(savedUser);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.authenticate(request.getEmail(), request.getPassword())
                .map(user -> {
                    String token = authService.generateToken(user);

                    LoginResponseDTO response = new LoginResponseDTO();
                    response.setToken(token);
                    response.setName(user.getName());
                    response.setEmail(user.getEmail());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .header("Error-Message", "Correo o contraseña incorrectos")
                                .body(null)
                );
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
