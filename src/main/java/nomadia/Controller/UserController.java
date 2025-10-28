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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/nomadia/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> getSelf(@AuthenticationPrincipal UserDetailsImpl principal) {
        return userService.findById(principal.getId())
                .map(UserResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/me/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateSelf(@AuthenticationPrincipal UserDetailsImpl principal,
                                                      @Valid @RequestBody UserUpdateDTO dto) {
        if (dto.getRole()!=null&&(userService.findById(principal.getId())).get().getRole()== Role.USER){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("No tenés permisos para cambiar el rol de usuario.");        }
        User updated = userService.updateUser(principal.getId(), dto, /*allowRoleChange=*/false);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated));
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        var saved = userService.createUser(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDTO.fromEntity(saved));
    }
    @GetMapping("/get-all")
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
