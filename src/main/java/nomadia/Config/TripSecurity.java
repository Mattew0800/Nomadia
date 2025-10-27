package nomadia.Config;

import lombok.RequiredArgsConstructor;
import nomadia.Repository.TripRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TripSecurity {

    private final TripRepository tripRepository;

    public boolean isOwner(Long tripId, Authentication auth) {
        Long requesterId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        return tripRepository.existsByIdAndCreatedBy_Id(tripId, requesterId);
    }

    public boolean isMember(Long tripId, Authentication auth) {
        Long requesterId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        return tripRepository.existsByIdAndUsers_Id(tripId, requesterId);
    }
}

