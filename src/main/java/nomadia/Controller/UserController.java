package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.User.UserCreateDTO;
import nomadia.DTO.User.UserResponseDTO;
import nomadia.DTO.User.UserUpdateDTO;
import nomadia.Enum.Role;
import nomadia.Model.User;
import nomadia.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/nomadia/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/me") // chequeado
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> getSelf(@AuthenticationPrincipal UserDetailsImpl principal) {
        return userService.findById(principal.getId())
                .map(UserResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/me/update") // a chequear verificaciones
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateSelf(@AuthenticationPrincipal UserDetailsImpl principal,
                                        @Valid @RequestBody UserUpdateDTO dto) {
        User me = userService.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (dto.getRole() != null && me.getRole() == Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permisos para cambiar el rol de usuario.");
        } // modularizar al servicio
        boolean wantsPasswordChange =
                notBlank(dto.getOldPassword()) ||
                        notBlank(dto.getNewPassword()) ||
                        notBlank(dto.getNewNewPassword());

        if (wantsPasswordChange) {
            if (!notBlank(dto.getOldPassword()) ||
                    !notBlank(dto.getNewPassword()) ||
                    !notBlank(dto.getNewNewPassword())) {
                return ResponseEntity.badRequest()
                        .body("Para cambiar la contraseña debés enviar oldPassword, newPassword y newNewPassword.");
            }
            if (!passwordEncoder.matches(dto.getOldPassword(), me.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La contraseña actual es incorrecta.");
            }

            if (!dto.getNewPassword().equals(dto.getNewNewPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Las contraseñas nuevas no coinciden.");
            }

            if (passwordEncoder.matches(dto.getNewPassword(), me.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La nueva contraseña no puede ser igual a la actual.");
            }
            dto.setPassword(dto.getNewPassword());
        }

        User updated = userService.updateUser(me.getId(), dto, /*allowRoleChange=*/false);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated));
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    @PostMapping("/create") // esto dsps se va, solo para prueba
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        var saved = userService.createUser(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDTO.fromEntity(saved));
    }

    @GetMapping("/get-all") //chequeado
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> getAllUsers() {
        return userService.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/get-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") Long id) {
        return userService.findById(id)
                .map(UserResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/delete-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable ("id") Long id) {
        if(!userService.deleteUser(id)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
         return ResponseEntity.ok("Usuario eliminado con exito");
    }
}
