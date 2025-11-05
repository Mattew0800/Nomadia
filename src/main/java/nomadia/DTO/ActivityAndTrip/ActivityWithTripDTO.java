package nomadia.DTO.ActivityAndTrip;

import lombok.Getter;
import lombok.Setter;
import nomadia.DTO.Activity.ActivityCreateDTO;

@Getter
@Setter
public class ActivityWithTripDTO {
    private Long tripId;
    private ActivityCreateDTO activity;
}
