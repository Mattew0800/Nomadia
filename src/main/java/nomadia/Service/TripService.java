package nomadia.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nomadia.DTO.Activity.ActivityCreateDTO;
import nomadia.DTO.Trip.TripCreateDTO;
import nomadia.DTO.Trip.TripListDTO;
import nomadia.DTO.Trip.TripResponseDTO;
import nomadia.DTO.Trip.TripUpdateDTO;
import nomadia.DTO.User.UserResponseDTO;
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

    public boolean isMember(Long tripId, Long userId) {
        if (tripId == null || userId == null) return false;
        return tripRepository.existsByIdAndUsers_Id(tripId, userId);
    }

    public boolean isOwner(Long tripId, Long userId) {
        if (tripId == null || userId == null) return false;
        return tripRepository.existsByIdAndCreatedBy_Id(tripId, userId);
    }

    @Transactional
    public List<UserResponseDTO> getTravelers(Long tripId, Long requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El viaje no existe."));
        boolean isMember = tripRepository.existsByIdAndUserId(tripId, requesterId);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No perteneces a este viaje.");
        }
        return trip.getUsers()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public TripResponseDTO viewTrip(Long tripId, Long userId) {
        if (!isMember(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos para ver este viaje");
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El viaje no existe"));
        return TripResponseDTO.fromEntity(trip);
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
    public UserResponseDTO addUserToTrip(Long tripId, String email) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("El viaje no existe"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        if (!isOwner(tripId , user.getId())){
            throw new IllegalStateException("No tenés permiso para modificar este viaje");
        }
        if (isMember(tripId, user.getId())) {
            throw new IllegalStateException("El usuario ya está agregado a este viaje");
        }
        user.getTrips().add(trip);
        trip.getUsers().add(user);
        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);
    }


    @Transactional
    public void removeUserFromTrip(Long tripId, String email) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El viaje no existe."));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No existe un usuario con ese email."));
        if (!isMember(tripId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no pertenece a este viaje.");
        }
        user.getTrips().remove(trip);
        trip.getUsers().remove(user);
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