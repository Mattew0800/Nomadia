package nomadia.Service;

import jakarta.transaction.Transactional;
import nomadia.DTO.User.UserResponseDTO;
import nomadia.DTO.User.UserUpdateDTO;
import nomadia.Enum.Role;
import nomadia.Model.User;
import nomadia.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        if(findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    @Transactional
    public UserResponseDTO updateSelf(Long userId, UserUpdateDTO dto) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validateRoleChange(me, dto);

        validateAndProcessPasswordChange(me, dto);

        String oldEmail = me.getEmail();
        User updated = updateUser(me.getId(), dto, false);

        UserResponseDTO response = UserResponseDTO.fromEntity(updated,false );

        if (!updated.getEmail().equals(oldEmail)) {
            String newToken = authService.generateToken(updated);
            response.setToken(newToken);
        }

        return response;
    }

    private void validateRoleChange(User user, UserUpdateDTO dto) {
        if (dto.getRole() != null && dto.getRole() != user.getRole() && user.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos para cambiar el rol de usuario.");
        }
    }
    private void validateAndProcessPasswordChange(User user, UserUpdateDTO dto) {
        boolean wantsPasswordChange = notBlank(dto.getOldPassword()) ||
                notBlank(dto.getNewPassword()) ||
                notBlank(dto.getNewNewPassword());

        if (!wantsPasswordChange) {
            return;
        }
        if (!notBlank(dto.getOldPassword()) ||
                !notBlank(dto.getNewPassword()) ||
                !notBlank(dto.getNewNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para cambiar la contraseña debés completar todos los campos");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta.");
        }

        if (!dto.getNewPassword().equals(dto.getNewNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas nuevas no coinciden.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña no puede ser igual a la actual.");
        }
        dto.setPassword(dto.getNewPassword());
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
    @Transactional
    public User updateUser(Long id, UserUpdateDTO dto, boolean allowRoleChange) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getEmail() != null &&
                userRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail().trim().toLowerCase(), id)) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        dto.applyToEntity(existing, passwordEncoder, allowRoleChange);
        return userRepository.save(existing);
    }


    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
