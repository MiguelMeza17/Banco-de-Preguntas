package co.unicauca.bancopreguntas.usuarios.service;

/** Error de negocio del servicio de usuarios; {@code estadoHttp} indica como responder. */
public class UsuarioException extends RuntimeException {

    private final int estadoHttp;

    public UsuarioException(int estadoHttp, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
    }

    public int getEstadoHttp() {
        return estadoHttp;
    }
}
