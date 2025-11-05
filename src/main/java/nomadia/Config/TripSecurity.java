package nomadia.Config;

import nomadia.Repository.TripRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class TripSecurity {

    private final TripRepository tripRepository;

    public TripSecurity(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }


}

