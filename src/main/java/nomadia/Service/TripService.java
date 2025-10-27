package nomadia.Service;


import nomadia.DTO.Trip.TripCreateDTO;
import nomadia.DTO.Trip.TripListDTO;
import nomadia.DTO.Trip.TripResponseDTO;
import nomadia.DTO.Trip.TripUpdateDTO;
import nomadia.Enum.State;
import nomadia.Model.Trip;
import nomadia.Model.User;
import nomadia.Repository.TripRepository;
import nomadia.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public TripService(TripRepository tripRepository,UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository=userRepository;
    }

    public List<TripListDTO> findMyTrips(Long userId) {
        return tripRepository.findTripsByUserId(userId)
                .stream()
                .map(TripListDTO::fromEntity)
                .toList();
    }

    public Optional<TripResponseDTO> findByNameForUser(String name, Long userId) {
        return tripRepository.findByNameAndUser(name, userId)
                .map(TripResponseDTO::fromEntity);
    }

    public TripResponseDTO createTrip(TripCreateDTO dto, Long userId) {
        Trip trip = dto.toEntity();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        trip.setState(State.CONFIRMADO);
        trip.setCreatedBy(user);
        trip.getUsers().add(user);
        Trip saved = tripRepository.save(trip);
        return TripResponseDTO.fromEntity(saved);
    }

    public Optional<TripResponseDTO> addUserToTrip(Long tripId, Long userIdToAdd) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        User user = userRepository.findById(userIdToAdd)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        trip.getUsers().add(user);
        return Optional.of(TripResponseDTO.fromEntity(tripRepository.save(trip)));
    }

    public void removeUserFromTrip(Long tripId, Long userIdToRemove) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        if (trip.getCreatedBy().getId().equals(userIdToRemove)) {
            throw new IllegalStateException("No se puede quitar al creador del viaje.");
        }

        trip.getUsers().removeIf(u -> u.getId().equals(userIdToRemove));
        tripRepository.save(trip);
    }

    public Optional<TripResponseDTO> updateTrip(Long tripId, TripUpdateDTO dto) {
        return tripRepository.findById(tripId).map(trip -> {
            dto.applyToEntity(trip);
            return TripResponseDTO.fromEntity(tripRepository.save(trip));
        });
    }


}
