package nomadia.DTO.UserBalance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DebtDTO {
    private Long fromUserId;
    private String fromEmail;
    private Long toUserId;
    private String toEmail;
    private BigDecimal amount;
}

