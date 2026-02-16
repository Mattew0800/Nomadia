package nomadia.DTO.UserBalance;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DebtDTO {
    private Long debtorId;
    private String debtorEmail;
    private Long creditorId;
    private String creditorEmail;
    private BigDecimal amount;

}
