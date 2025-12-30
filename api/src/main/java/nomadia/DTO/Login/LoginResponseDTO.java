package nomadia.DTO.Login;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String email;
    private String name;
}