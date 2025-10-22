package nomadia.nomadia.User;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController @RequestMapping("/api/users/me") @RequiredArgsConstructor
public class UserMeController {
    private final UserRepository users;
    private final PasswordEncoder encoder;


    record UpdateMeDto(@Size(min=3,max=40) String username, @Email String email, @Size(min=6,max=100) String password){}
    record MeDto(Long id, String username, String email, String role){}


    @GetMapping
    public MeDto me(@AuthenticationPrincipal UserDetails ud){
        var u = users.findByUsername(ud.getUsername()).orElseThrow();
        return new MeDto(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name());
    }


    @PutMapping
    public ResponseEntity<Void> update(@AuthenticationPrincipal UserDetails ud, @RequestBody UpdateMeDto dto){
        var u = users.findByUsername(ud.getUsername()).orElseThrow();
        if (dto.username()!=null) u.setUsername(dto.username());
        if (dto.email()!=null) u.setEmail(dto.email());
        if (dto.password()!=null) u.setPassword(encoder.encode(dto.password()));
        users.save(u);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud){
        var u = users.findByUsername(ud.getUsername()).orElseThrow();
        users.delete(u);
        return ResponseEntity.noContent().build();
    }
}