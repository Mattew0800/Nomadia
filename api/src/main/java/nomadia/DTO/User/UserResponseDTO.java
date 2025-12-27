package nomadia.DTO.User;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    public static UserResponseDTO fromEntity(User user,boolean role) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setNick(user.getNick());
        dto.setAbout(user.getAbout());
        dto.setBirth(user.getBirth());
        dto.setAge(user.getAge());
        if(role){
            dto.setRole(user.getRole());
        }
        return dto;
    }
    public UserResponseDTO withToken(String token) {
        this.token = token;
        return this;
    }
}
