package nomadia.DTO.UserBalance;

import lombok.Data;

@Data
public class AutoSettleRequestDTO {
    private Long tripId;
    private Long creditorId;
}
