package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.DTO.UserCreateDTO;
import nomadia.DTO.UserResponseDTO;
import nomadia.DTO.UserUpdateDTO;
import nomadia.Model.User;
import nomadia.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<UserResponseDTO> getSelf() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(UserResponseDTO.fromEntity(currentUser));
    }

    @PutMapping("/me/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> updateSelf(@Valid @RequestBody UserUpdateDTO dto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userService.updateUser(currentUser.getId(), dto.toEntity())
                .map(UserResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        User saved = userService.createUser(dto.toEntity());
        return new ResponseEntity<>(UserResponseDTO.fromEntity(saved), HttpStatus.CREATED);
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
