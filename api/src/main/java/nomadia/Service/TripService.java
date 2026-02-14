package nomadia.Service;

import nomadia.Repository.*;
import org.springframework.transaction.annotation.Transactional;
import nomadia.DTO.Trip.TripCreateDTO;
import nomadia.DTO.Trip.TripListDTO;
import nomadia.DTO.Trip.TripResponseDTO;
import nomadia.DTO.Trip.TripUpdateDTO;
import nomadia.DTO.User.UserResponseDTO;
import nomadia.Enum.State;
import nomadia.Model.Trip;
import nomadia.Model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class TripService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ParticipantRepository participantRepository;

    public TripService(ActivityRepository activityRepository, TripRepository tripRepository, UserRepository userRepository, ExpenseRepository expenseRepository, ParticipantRepository participantRepository) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.participantRepository = participantRepository;
    }

    public boolean isMember(Long tripId,Long userId){
        if(tripId==null||userId==null) return false;
        return tripRepository.existsByIdAndUsers_Id(tripId,userId);
    }

    public boolean isOwner(Long tripId,Long userId){
        if(tripId==null||userId==null) return false;
        return tripRepository.existsByIdAndCreatedBy_Id(tripId,userId);
    }

    public Trip getTripAndValidateMember(Long tripId, Long userId){
        Trip trip=tripRepository.findById(tripId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"El viaje no existe"));
        if(!isMember(tripId,userId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,"No perteneces a este viaje");
        }
        return trip;
    }

    private void validateOwner(Long tripId,Long userId){
        if(!isOwner(tripId,userId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para modificar este viaje");
        }
    }

    @Transactional(readOnly = true)
    public List<TripListDTO> getMyTrips(Long userId){
        return tripRepository.findTripsByUserId(userId)
                .stream()
                .map(TripListDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Trip findById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Viaje no encontrado" ));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getTravelers(Long tripId,Long requesterId){
        Trip trip=getTripAndValidateMember(tripId,requesterId);
        return trip.getUsers()
                .stream()
                .map(u->UserResponseDTO.fromEntity(u,false))
                .toList();
    }

    @Transactional(readOnly=true)
    public TripResponseDTO viewTrip(Long tripId,Long userId){
        return TripResponseDTO.fromEntity(
                getTripAndValidateMember(tripId,userId));
    }

    private void calculateState(Trip trip) {
        LocalDate today = LocalDate.now();
        if (trip.getEndDate().isBefore(today)) {
            trip.setState(State.FINALIZADO);
        } else if (!trip.getStartDate().isAfter(today)) {
            trip.setState(State.EN_CURSO);
        } else {
            trip.setState(State.CONFIRMADO);
        }
    }

    @Transactional
    public TripResponseDTO createTrip(TripCreateDTO dto,Long userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado"));
        Trip trip=dto.toEntity();
        calculateState(trip);
        trip.setCreatedBy(user);
        trip.getUsers().add(user);
        Trip saved=tripRepository.save(trip);
        tripRepository.insertCreator(userId,saved.getId());
        return TripResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteTrip(Long tripId,Long userId){
        validateOwner(tripId,userId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El viaje no existe"));
        for (User user : trip.getUsers()) {
            user.getTrips().remove(trip);
        }
        trip.getUsers().clear();
        tripRepository.deleteById(tripId);
    }

    @Transactional
    public UserResponseDTO addUserToTrip(Long tripId,String email,Long userId){
        validateOwner(tripId,userId);
        Trip trip=tripRepository.findById(tripId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"El viaje no existe"));
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"El usuario no existe"));
        if(isMember(tripId,user.getId())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya está agregado a este viaje");
        }
        user.getTrips().add(trip);
        trip.getUsers().add(user);
        userRepository.save(user);
        return UserResponseDTO.fromEntity(user,false);
    }

    private void validateParticipant(Long tripId,Long userId){
        if (participantRepository.existsByUserIdAndExpenseTripId(userId,tripId)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar el viajero ya que tiene gastos asociados");
        }
    }

    @Transactional
    public void removeUserFromTrip(Long tripId, String email, Long requesterId){
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El viaje no existe"));
        validateOwner(tripId, requesterId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe un usuario con ese email"));
        getTripAndValidateMember(tripId, user.getId());
        validateParticipant(tripId, user.getId());
        removeUserRelation(trip,user.getId());
        tripRepository.save(trip);
    }

    @Transactional
    public Optional<TripResponseDTO> updateTrip(Long tripId, TripUpdateDTO dto, Long userId) {
        validateOwner(tripId,userId);
         if (dto.getName() != null && tripRepository.existsByNameIgnoreCaseAndUsers_Id(dto.getName(), userId)) {
            throw new IllegalArgumentException("Ya tenés otro viaje con ese nombre."); }
        return tripRepository.findById(tripId).map(trip -> {
            dto.applyToEntity(trip);
            if (trip.getEndDate().isBefore(trip.getStartDate())) {
                throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la de inicio");
            }
            calculateState(trip);
            Trip updated = tripRepository.save(trip);
            return TripResponseDTO.fromEntity(updated);
        });
    }

    private void removeUserRelation(Trip trip, Long userId) {
        User user = trip.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Inconsistencia de datos"));
        user.getTrips().remove(trip);
        trip.getUsers().remove(user);
    }

    public void removeSelfFromTrip(Long tripId,Long userId){
        Trip trip=getTripAndValidateMember(tripId,userId);
        validateParticipant(tripId,userId);
        if(!trip.getCreatedBy().getId().equals(userId)){
            removeUserRelation(trip, userId);
            tripRepository.save(trip);
            return;
        }
        if (trip.getUsers().size() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No podés abandonar un viaje del que sos el único participante");
        }
        User newOwner = trip.getUsers().stream()
                .filter(u -> !u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay nuevo dueño"));
        trip.setCreatedBy(newOwner);
        removeUserRelation(trip, userId);
        tripRepository.save(trip);
    }
}
