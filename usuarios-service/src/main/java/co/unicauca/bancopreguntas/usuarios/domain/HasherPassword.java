package co.unicauca.bancopreguntas.usuarios.domain;

/** Puerto para el cifrado de contrasenas. */
public interface HasherPassword {

    String hash(String password);

    boolean verificar(String hash, String password);
}
