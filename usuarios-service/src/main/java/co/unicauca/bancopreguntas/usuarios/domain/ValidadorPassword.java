package co.unicauca.bancopreguntas.usuarios.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de complejidad de la contrasena (heredadas del Corte 1): minimo 6 caracteres,
 * una mayuscula, un digito y un caracter especial.
 */
public class ValidadorPassword {

    public List<String> errores(String password) {
        List<String> errores = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            errores.add("La contrasena no puede estar vacia.");
            return errores;
        }
        if (password.length() < 6) {
            errores.add("La contrasena debe tener al menos 6 caracteres.");
        }
        if (!password.matches(".*[A-Z].*")) {
            errores.add("La contrasena debe incluir al menos una letra mayuscula.");
        }
        if (!password.matches(".*[0-9].*")) {
            errores.add("La contrasena debe incluir al menos un digito.");
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            errores.add("La contrasena debe incluir al menos un caracter especial.");
        }
        return errores;
    }
}
