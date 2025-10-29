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

        if (dto.getEmail() != null &&
                userRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail().trim().toLowerCase(), id)) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario.");
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
