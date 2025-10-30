package nomadia.DTO.User;

import lombok.Data;
import nomadia.Enum.Role;
import nomadia.Model.User;

import java.util.Date;

@Data
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String photoUrl;
    private String nick;
    private String about;
    private Date birth;
    private Integer age;

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPhone(user.getPhone());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setNick(user.getNick());
        dto.setAbout(user.getAbout());
        dto.setBirth(user.getBirth());
        dto.setAge(user.getAge());
        dto.setRole(user.getRole());
        return dto;
    }
}
