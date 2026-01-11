package nomadia.Service;

import nomadia.Repository.ExpenseRepository;
import org.springframework.transaction.annotation.Transactional;
import nomadia.DTO.Trip.TripCreateDTO;
import nomadia.DTO.Trip.TripListDTO;
import nomadia.DTO.Trip.TripResponseDTO;
import nomadia.DTO.Trip.TripUpdateDTO;
import nomadia.DTO.User.UserResponseDTO;
import nomadia.Enum.State;
import nomadia.Model.Trip;
import nomadia.Model.User;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.TripRepository;
import nomadia.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.PublicKey;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class TripService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public TripService(ActivityRepository activityRepository, TripRepository tripRepository, UserRepository userRepository, ExpenseRepository expenseRepository) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    public boolean isMember(Long tripId,Long userId){
        if(tripId==null||userId==null) return false;
        return tripRepository.existsByIdAndUsers_Id(tripId,userId);
    }

    public boolean isOwner(Long tripId,Long userId){
        if(tripId==null||userId==null) return false;
        return tripRepository.existsByIdAndCreatedBy_Id(tripId,userId);
    }

    //USAR PARA REEMPLAZAR MEMBER Y LLAMAR A ESTO
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

    @Transactional
    public TripResponseDTO createTrip(TripCreateDTO dto,Long userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"Usuario no encontrado"));
        Trip trip=dto.toEntity();
        trip.setState(State.CONFIRMADO);
        trip.setCreatedBy(user);
        trip.getUsers().add(user);
        Trip saved=tripRepository.save(trip);
        tripRepository.insertCreator(userId,saved.getId());
        return TripResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteTrip(Long tripId,Long userId){
        validateOwner(tripId,userId);
        if(!tripRepository.existsById(tripId)){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,"El viaje no existe");
        }
        tripRepository.deleteRelations(tripId);
        activityRepository.deleteByTripId(tripId);
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

    @Transactional
    public void removeUserFromTrip(Long tripId,String email,Long userId){
        if (expenseRepository.existsExpenseByTripAndUser(tripId,userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Los usuarios con gastos asociados no pueden salir del viaje ");
        }
        validateOwner(tripId,userId);
        Trip trip=tripRepository.findById(tripId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"El viaje no existe"));
        if (trip.getStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Los usuarios no pueden salir de viajes en curso"
            );
        }
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No existe un usuario con ese email"));
        if(!isMember(tripId,user.getId())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario no pertenece a este viaje");
        }
        user.getTrips().remove(trip);
        trip.getUsers().remove(user);
        tripRepository.save(trip);
    }

    @Transactional
    public Optional<TripResponseDTO> updateTrip(Long tripId, TripUpdateDTO dto, Long userId) {
        if (!isOwner(dto.getTripId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No tenés permiso para modificar este viaje");
        } if (dto.getName() != null && tripRepository.existsByNameIgnoreCaseAndUsers_Id(dto.getName(), userId)) {
            throw new IllegalArgumentException("Ya tenés otro viaje con ese nombre."); }
        return tripRepository.findById(tripId).map(trip -> { dto.applyToEntity(trip);
            Trip updated = tripRepository.save(trip);
            return TripResponseDTO.fromEntity(updated);
        });
    } }
