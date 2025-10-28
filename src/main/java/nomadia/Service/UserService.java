package nomadia.Service;

import jakarta.transaction.Transactional;
import nomadia.DTO.User.UserUpdateDTO;
import nomadia.Model.User;
import nomadia.Repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateDTO dto, boolean allowRoleChange) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getName() != null) { existing.setName(dto.getName().trim()); }
        if (dto.getEmail() != null) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, id)) {
                throw new IllegalArgumentException("El email ya está en uso por otro usuario.");
            }
            existing.setEmail(newEmail);
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) {
            if (!allowRoleChange) {
            } else {
                existing.setRole(dto.getRole());
            }
        }

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
