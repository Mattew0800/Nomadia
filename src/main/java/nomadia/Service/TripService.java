package nomadia.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nomadia.DTO.Activity.ActivityCreateDTO;
import nomadia.DTO.Trip.TripCreateDTO;
import nomadia.DTO.Trip.TripListDTO;
import nomadia.DTO.Trip.TripResponseDTO;
import nomadia.DTO.Trip.TripUpdateDTO;
import nomadia.Enum.State;
import nomadia.Model.Activity;
import nomadia.Model.Trip;
import nomadia.Model.User;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.TripRepository;
import nomadia.Repository.UserRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public List<TripListDTO> getMyTrips(Long userId) {
        return tripRepository.findTripsByUserId(userId)
                .stream()
                .map(TripListDTO::fromEntity)
                .toList();
    }

    public Optional<TripResponseDTO> findByNameForUser(String rawName, Long userId) {
        String name = rawName == null ? null : rawName.trim();
        if (name == null || name.isEmpty()) return Optional.empty();

        return tripRepository.findByNameIgnoreCaseAndUsers_Id(name, userId)
                .map(TripResponseDTO::fromEntity);
    }

    public boolean isOwner(Long tripId, Long userId) {
        if (tripId == null || userId == null) return false;
        return tripRepository.existsByIdAndCreatedBy_Id(tripId, userId);
    }


    public boolean isMember(Long tripId, Long userId) {
        if (tripId == null || userId == null) return false;
        return tripRepository.existsByIdAndUsers_Id(tripId, userId);
    }


    public TripResponseDTO createTrip(TripCreateDTO dto, Long userId) {
        Trip trip = dto.toEntity();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        trip.setState(State.CONFIRMADO);
        trip.setCreatedBy(user);
        trip.getUsers().add(user);
        Trip saved = tripRepository.save(trip);
        tripRepository.insertCreator(userId, saved.getId());
        return TripResponseDTO.fromEntity(saved);
    }

    public Optional<Trip> findById(Long tripId) {
        return tripRepository.findById(tripId);
    }


    @Transactional
    public void deleteTrip(Long tripId) throws ChangeSetPersister.NotFoundException {
        if (!tripRepository.existsById(tripId)) throw new ChangeSetPersister.NotFoundException();

        tripRepository.deleteRelations(tripId);
        activityRepository.deleteByTripId(tripId);
        tripRepository.deleteById(tripId);
    }


    @Transactional
    public Optional<TripResponseDTO> addUserToTrip(Long tripId, String email, Long requesterId) {
        if (!tripRepository.existsByIdAndCreatedBy_Id(tripId, requesterId)) {
            throw new SecurityException("Solo el creador puede agregar usuarios.");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (trip.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya está en el viaje");
        }

        boolean alreadyMember = trip.getUsers().stream().anyMatch(u -> u.getId().equals(user.getId()));
        if (!alreadyMember) {
            trip.getUsers().add(user);
            trip = tripRepository.save(trip);
        }
        return Optional.of(TripResponseDTO.fromEntity(trip));
    }

    @Transactional
    public void removeUserFromTrip(Long tripId, String email, Long requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El viaje no existe."));

        if (trip.getCreatedBy() == null || !trip.getCreatedBy().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el creador del viaje puede eliminar usuarios.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No existe un usuario con ese email."));

        if (trip.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No podés quitar al creador del viaje.");
        }

        boolean member = trip.getUsers().stream().anyMatch(u -> u.getId().equals(user.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no pertenece a este viaje.");
        }
        trip.getUsers().removeIf(u -> u.getId().equals(user.getId()));
        tripRepository.save(trip);
    }

    public Optional<TripResponseDTO> updateTrip(Long tripId, TripUpdateDTO dto, Long userId) {
        if (dto.getName() != null &&
                tripRepository.existsByNameIgnoreCaseAndUsers_Id(dto.getName(), userId)) {
            throw new IllegalArgumentException("Ya tenés otro viaje con ese nombre.");
        }

        return tripRepository.findById(tripId).map(trip -> {
            dto.applyToEntity(trip);
            Trip updated = tripRepository.save(trip);
            return TripResponseDTO.fromEntity(updated);
        });
    }


}