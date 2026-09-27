package co.edu.unicauca.domain;

public interface IPasswordHasher {
    String hash(String password);
    boolean verify(String hash, String password);
}

