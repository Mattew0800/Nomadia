package nomadia.DTO.UserBalance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserBalanceDTO {
    private Long userId;
    private String email;
    private BigDecimal balance;
}
