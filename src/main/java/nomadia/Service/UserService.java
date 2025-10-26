package nomadia.Service;

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

    public Optional<User> updateUser(Long id, User updatedData) {
        return userRepository.findById(id).map(existingUser -> {
            if (updatedData.getName() != null) existingUser.setName(updatedData.getName());
            if (updatedData.getEmail() != null) existingUser.setEmail(updatedData.getEmail());
            if (updatedData.getRole() != null) existingUser.setRole(updatedData.getRole());

            if (updatedData.getPassword() != null && !updatedData.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
            }
            return userRepository.save(existingUser);
        });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
