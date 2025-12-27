package nomadia.Scheduler;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nomadia.Enum.State;
import nomadia.Model.Trip;
import nomadia.Repository.TripRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TripScheduler {
    private final TripRepository tripRepository;
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void finishTrips() {
        List<Trip> trips = tripRepository
                .findByEndDateBeforeAndStateNot(LocalDate.now(), State.FINALIZADO);
        for (Trip trip : trips) {
            trip.setState(State.FINALIZADO);
        }
    }
}
