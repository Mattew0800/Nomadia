package nomadia.nomadia.auth;

import nomadia.nomadia.auth.dto.AuthResponse;
import nomadia.nomadia.auth.dto.LoginRequest;
import nomadia.nomadia.auth.dto.RegisterRequest;
import nomadia.nomadia.security.JwtTokenProvider;
import nomadia.nomadia.User.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nomadia.nomadia.User.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service @RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;
    private final UserRepository users;
    private final PasswordEncoder encoder;


    public AuthResponse login(LoginRequest req) {
        var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var token = jwt.generate(auth);
        return new AuthResponse(token, "Bearer", jwt.getExpiration());
    }


    @Transactional
    public void register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) throw new IllegalArgumentException("username in use");
        if (users.existsByEmail(req.email())) throw new IllegalArgumentException("email in use");
        var u = UserEntity.builder()
                .username(req.username())
                .email(req.email())
                .password(encoder.encode(req.password()))
                .role(Role.TRAVELER)
                .enabled(true)
                .build();
        users.save(u);
    }
}