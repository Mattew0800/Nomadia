package nomadia.DTO.User;

import lombok.Data;
import nomadia.Enum.Role;
import nomadia.Model.User;

@Data
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
