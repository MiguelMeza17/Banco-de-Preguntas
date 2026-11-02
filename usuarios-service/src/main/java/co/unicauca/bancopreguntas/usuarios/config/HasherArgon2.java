package co.unicauca.bancopreguntas.usuarios.config;

import co.unicauca.bancopreguntas.usuarios.domain.HasherPassword;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

/** Implementacion de {@link HasherPassword} con Argon2id. */
@Component
public class HasherArgon2 implements HasherPassword {

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Override
    public String hash(String password) {
        char[] chars = password.toCharArray();
        try {
            return argon2.hash(2, 15360, 1, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }

    @Override
    public boolean verificar(String hash, String password) {
        if (hash == null || password == null) {
            return false;
        }
        char[] chars = password.toCharArray();
        try {
            return argon2.verify(hash, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }
}
