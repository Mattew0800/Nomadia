package nomadia.DTO;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String email;
    private String name;
}