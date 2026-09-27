package co.edu.unicauca.domain;

import java.util.List;

public interface IPasswordValidator {
    boolean isValid(String password);
    List<String> getValidationErrors(String password);
}

