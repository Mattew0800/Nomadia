package nomadia.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public List<TripListDTO> findMyTrips(Long userId) {
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

    public TripResponseDTO createTrip(TripCreateDTO dto, Long userId) {
        Trip trip = dto.toEntity();

        // Verificar duplicado por usuario
        if (tripRepository.existsByNameIgnoreCaseAndUsers_Id(dto.getName(), userId)) {
            throw new IllegalArgumentException("Ya existe un viaje con ese nombre para este usuario.");
        }


        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        trip.setState(State.CONFIRMADO);
        trip.setCreatedBy(user);
        trip.getUsers().add(user);
        Trip saved = tripRepository.save(trip);
        return TripResponseDTO.fromEntity(saved);
    }

    /** Solo el CREADOR puede agregar usuarios */
    public Optional<TripResponseDTO> addUserToTrip(Long tripId, Long userIdToAdd, Long requesterId) {
        if (!tripRepository.existsByIdAndCreatedBy_Id(tripId, requesterId)) {
            throw new SecurityException("Solo el creador puede agregar usuarios.");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        User user = userRepository.findById(userIdToAdd)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean alreadyMember = trip.getUsers().stream().anyMatch(u -> u.getId().equals(userIdToAdd));
        if (!alreadyMember) {
            trip.getUsers().add(user);
            trip = tripRepository.save(trip);
        }
        return Optional.of(TripResponseDTO.fromEntity(trip));
    }

    /** Solo el CREADOR puede quitar usuarios (no se puede quitar al creador) */
    public void removeUserFromTrip(Long tripId, Long userIdToRemove, Long requesterId) {
        if (!tripRepository.existsByIdAndCreatedBy_Id(tripId, requesterId)) {
            throw new SecurityException("Solo el creador puede quitar usuarios.");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        if (trip.getCreatedBy().getId().equals(userIdToRemove)) {
            throw new IllegalStateException("No se puede quitar al creador del viaje.");
        }

        boolean removed = trip.getUsers().removeIf(u -> u.getId().equals(userIdToRemove));
        if (!removed) throw new IllegalArgumentException("El usuario indicado no pertenece al viaje.");

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
