Feature: Gestión de reservas
  Como usuario de la plataforma BookingYa
  Quiero gestionar mis reservas
  Para organizar mis estadías de forma eficiente

  Background:
    Given el sistema tiene una habitación disponible
    And el sistema tiene un huésped registrado

  Scenario: Crear una reserva exitosamente
    When el usuario crea una reserva con fechas válidas
    Then la reserva es creada exitosamente

  Scenario: Intentar crear una reserva con habitación no disponible
    Given la habitación no está disponible
    When el usuario intenta crear una reserva
    Then el sistema rechaza la reserva con mensaje "Room is not available"

  Scenario: Consultar todas las reservas
    Given existe al menos una reserva registrada
    When el usuario consulta todas las reservas
    Then el sistema retorna la lista de reservas

  Scenario: Obtener una reserva por ID
    Given existe una reserva con un ID conocido
    When el usuario consulta la reserva por ese ID
    Then el sistema retorna la reserva correcta

  Scenario: Actualizar una reserva existente
    Given existe una reserva registrada
    When el usuario actualiza la reserva con nuevas fechas
    Then la reserva es actualizada exitosamente

  Scenario: Eliminar una reserva existente
    Given existe una reserva registrada
    When el usuario elimina la reserva
    Then la reserva es eliminada del sistema