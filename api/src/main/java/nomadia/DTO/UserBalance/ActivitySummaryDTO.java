package nomadia.DTO.UserBalance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ActivitySummaryDTO {
    private Long activityId;
    private String name;
    private BigDecimal totalSpent;
    private int participants;
    private BigDecimal averagePerPerson;
}
