package com.project.bookingya.bdd.steps;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.ReservationService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
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

public class ReservationSteps {

    private IReservationRepository reservationRepository;
    private IRoomRepository roomRepository;
    private IGuestRepository guestRepository;
    private ReservationService reservationService;

    private UUID reservationId;
    private UUID roomId;
    private UUID guestId;
    private RoomEntity roomEntity;
    private GuestEntity guestEntity;
    private ReservationEntity reservationEntity;
    private ReservationDto reservationDto;
    private Reservation resultado;
    private Exception excepcionCapturada;

    @Before
    public void init() {
        reservationRepository = mock(IReservationRepository.class);
        roomRepository = mock(IRoomRepository.class);
        guestRepository = mock(IGuestRepository.class);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        reservationService = new ReservationService(
            reservationRepository, roomRepository, guestRepository, mapper
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

        reservationEntity = new ReservationEntity();
        reservationEntity.setId(reservationId);
        reservationEntity.setRoomId(roomId);
        reservationEntity.setGuestId(guestId);
        reservationEntity.setCheckIn(LocalDateTime.now().plusDays(1));
        reservationEntity.setCheckOut(LocalDateTime.now().plusDays(3));
        reservationEntity.setGuestsCount(2);

        reservationDto = new ReservationDto();
        reservationDto.setRoomId(roomId);
        reservationDto.setGuestId(guestId);
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(3));
        reservationDto.setGuestsCount(2);
    }

    @Given("el sistema tiene una habitación disponible")
    public void elSistemaTieneHabitacionDisponible() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
    }

    @Given("el sistema tiene un huésped registrado")
    public void elSistemaTieneHuespedRegistrado() {
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
    }

    @Given("la habitación no está disponible")
    public void laHabitacionNoEstaDisponible() {
        roomEntity.setAvailable(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
    }

    @Given("existe al menos una reserva registrada")
    public void existeAlMenosUnaReserva() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservationEntity));
    }

    @Given("existe una reserva con un ID conocido")
    public void existeReservaConIdConocido() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservationEntity));
    }

    @Given("existe una reserva registrada")
    public void existeUnaReservaRegistrada() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservationEntity));
        when(reservationRepository.saveAndFlush(any())).thenReturn(reservationEntity);
    }

    @When("el usuario crea una reserva con fechas válidas")
    public void elUsuarioCrearReserva() {
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.saveAndFlush(any())).thenReturn(reservationEntity);
        resultado = reservationService.create(reservationDto);
    }

    @When("el usuario intenta crear una reserva")
    public void elUsuarioIntentaCrearReserva() {
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        try {
            reservationService.create(reservationDto);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @When("el usuario consulta todas las reservas")
    public void elUsuarioConsultaTodasLasReservas() {
        List<Reservation> reservas = reservationService.getAll();
        resultado = reservas.isEmpty() ? null : reservas.get(0);
    }

    @When("el usuario consulta la reserva por ese ID")
    public void elUsuarioConsultaPorId() {
        resultado = reservationService.getById(reservationId);
    }

    @When("el usuario actualiza la reserva con nuevas fechas")
    public void elUsuarioActualizaReserva() {
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(5));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(7));
        resultado = reservationService.update(reservationDto, reservationId);
    }

    @When("el usuario elimina la reserva")
    public void elUsuarioEliminaReserva() {
        reservationService.delete(reservationId);
    }

    @Then("la reserva es creada exitosamente")
    public void laReservaEsCreadaExitosamente() {
        assertNotNull(resultado);
    }

    @Then("el sistema rechaza la reserva con mensaje {string}")
    public void elSistemaRechazaLaReserva(String mensaje) {
        assertNotNull(excepcionCapturada);
        assertInstanceOf(BusinessRuleException.class, excepcionCapturada);
        assertEquals(mensaje, excepcionCapturada.getMessage());
    }

    @Then("el sistema retorna la lista de reservas")
    public void elSistemaRetornaLista() {
        assertNotNull(resultado);
    }

    @Then("el sistema retorna la reserva correcta")
    public void elSistemaRetornaReservaCorrecta() {
        assertNotNull(resultado);
    }

    @Then("la reserva es actualizada exitosamente")
    public void laReservaEsActualizada() {
        assertNotNull(resultado);
    }

    @Then("la reserva es eliminada del sistema")
    public void laReservaEsEliminada() {
        verify(reservationRepository, times(1)).delete(any());
    }
}