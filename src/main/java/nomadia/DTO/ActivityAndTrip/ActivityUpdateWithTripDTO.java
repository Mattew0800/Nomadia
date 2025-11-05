package nomadia.DTO.ActivityAndTrip;

import lombok.Getter;
import lombok.Setter;
import nomadia.DTO.Activity.ActivityUpdateRequestDTO;

@Getter
@Setter
public class ActivityUpdateWithTripDTO {
    private Long tripId;
    private Long activityId;
    private ActivityUpdateRequestDTO update;
}
