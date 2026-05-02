package com.project.bookingya.tdd;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.ReservationService;
import com.project.bookingya.shared.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    private ReservationService reservationService;

    private UUID reservationId;
    private UUID roomId;
    private UUID guestId;
    private ReservationDto reservationDto;
    private ReservationEntity reservationEntity;
    private RoomEntity roomEntity;
    private GuestEntity guestEntity;

    @BeforeEach
    void setUp() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        reservationService = new ReservationService(
            reservationRepository,
            roomRepository,
            guestRepository,
            mapper
        );

        reservationId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        guestId = UUID.randomUUID();

        roomEntity = new RoomEntity();
        roomEntity.setId(roomId);
        roomEntity.setCode("ROOM-001");
        roomEntity.setName("Suite Deluxe");
        roomEntity.setCity("Bogotá");
        roomEntity.setMaxGuests(3);
        roomEntity.setNightlyPrice(new BigDecimal("150.00"));
        roomEntity.setAvailable(true);

        guestEntity = new GuestEntity();
        guestEntity.setId(guestId);
        guestEntity.setName("María García");
        guestEntity.setIdentification("123456789");
        guestEntity.setEmail("maria@example.com");

        reservationDto = new ReservationDto();
        reservationDto.setRoomId(roomId);
        reservationDto.setGuestId(guestId);
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(3));
        reservationDto.setGuestsCount(2);
        reservationDto.setNotes("Sin notas");

        reservationEntity = new ReservationEntity();
        reservationEntity.setId(reservationId);
        reservationEntity.setRoomId(roomId);
        reservationEntity.setGuestId(guestId);
        reservationEntity.setCheckIn(reservationDto.getCheckIn());
        reservationEntity.setCheckOut(reservationDto.getCheckOut());
        reservationEntity.setGuestsCount(2);

        // Mocks comunes para todos los tests
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.saveAndFlush(any())).thenReturn(reservationEntity);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservationEntity));
        when(reservationRepository.findAll()).thenReturn(List.of(reservationEntity));
    }

    // ==================== CREACIÓN ====================
    @Test
    void cuandoSeCrearReservaConDatosValidos_entoncesRetornaReservaCreada() {
        Reservation result = reservationService.create(reservationDto);

        assertNotNull(result);
        verify(reservationRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void cuandoSeCrearReservaConHabitacionNoDisponible_entoncesLanzaExcepcion() {
        roomEntity.setAvailable(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> reservationService.create(reservationDto));

        assertEquals(Constants.ROOM_NOT_AVAILABLE, ex.getMessage());
    }

    @Test
    void cuandoSeCrearReservaConCapacidadExcedida_entoncesLanzaExcepcion() {
        reservationDto.setGuestsCount(10);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> reservationService.create(reservationDto));

        assertEquals(Constants.ROOM_CAPACITY_EXCEEDED, ex.getMessage());
    }

    // ==================== CONSULTA ====================
    @Test
    void cuandoSeConsultanTodasLasReservas_entoncesRetornaLista() {
        List<Reservation> result = reservationService.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ==================== OBTENER POR ID ====================
    @Test
    void cuandoSeObtienReservaPorIdValido_entoncesRetornaReserva() {
        Reservation result = reservationService.getById(reservationId);

        assertNotNull(result);
    }

    @Test
    void cuandoSeObtienReservaPorIdInexistente_entoncesLanzaExcepcion() {
        UUID idFalso = UUID.randomUUID();
        when(reservationRepository.findById(idFalso)).thenReturn(Optional.empty());

        EntityNotExistsException ex = assertThrows(EntityNotExistsException.class,
            () -> reservationService.getById(idFalso));

        assertEquals(Constants.RESERVATION_NOT_FOUND, ex.getMessage());
    }

    // ==================== ACTUALIZACIÓN ====================
    @Test
    void cuandoSeActualizaReservaExistente_entoncesRetornaReservaActualizada() {
        Reservation result = reservationService.update(reservationDto, reservationId);

        assertNotNull(result);
        verify(reservationRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void cuandoSeActualizaReservaInexistente_entoncesLanzaExcepcion() {
        UUID idFalso = UUID.randomUUID();
        when(reservationRepository.findById(idFalso)).thenReturn(Optional.empty());

        EntityNotExistsException ex = assertThrows(EntityNotExistsException.class,
            () -> reservationService.update(reservationDto, idFalso));

        assertEquals(Constants.RESERVATION_NOT_FOUND, ex.getMessage());
    }

    // ==================== ELIMINACIÓN ====================
    @Test
    void cuandoSeEliminaReservaExistente_entoncesSeEliminaCorrectamente() {
        assertDoesNotThrow(() -> reservationService.delete(reservationId));

        verify(reservationRepository, times(1)).delete(reservationEntity);
        verify(reservationRepository, times(1)).flush();
    }

    @Test
    void cuandoSeEliminaReservaInexistente_entoncesLanzaExcepcion() {
        UUID idFalso = UUID.randomUUID();
        when(reservationRepository.findById(idFalso)).thenReturn(Optional.empty());

        EntityNotExistsException ex = assertThrows(EntityNotExistsException.class,
            () -> reservationService.delete(idFalso));

        assertEquals(Constants.RESERVATION_NOT_FOUND, ex.getMessage());
    }

    // ==================== FECHAS INVÁLIDAS ====================
    @Test
    void cuandoCheckInEsDespuesDeCheckOut_entoncesLanzaExcepcion() {
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(5));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(1));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> reservationService.create(reservationDto));

        assertEquals(Constants.INVALID_RESERVATION_RANGE, ex.getMessage());
    }
}