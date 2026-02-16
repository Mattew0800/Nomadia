package nomadia.DTO.UserBalance;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class UserDebtProgressDTO {
    private List<DebtDTO> debts;
    private int totalDebts;
    private int settledDebts;
    private int pendingDebts;
    private double percentage;
}
