package nomadia.Repository;


import jakarta.transaction.Transactional;
import nomadia.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    Optional<User> findById(Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}