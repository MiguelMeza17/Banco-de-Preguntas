package co.unicauca.bancopreguntas.usuarios.web.dto;

import co.unicauca.bancopreguntas.usuarios.domain.Rol;

public record RegistroRequest(String login, String nombreCompleto, String email, Rol rol, String password) {
}
