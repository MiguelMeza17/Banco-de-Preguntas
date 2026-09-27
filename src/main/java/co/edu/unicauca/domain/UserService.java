package co.edu.unicauca.domain;

import co.edu.unicauca.access.IUserRepository;
import java.util.ArrayList;
import java.util.List;


public class UserService {

    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final IPasswordValidator passwordValidator;

    public UserService(IUserRepository userRepository, IPasswordHasher passwordHasher, IPasswordValidator passwordValidator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
    }

    public boolean register(User user, String rawPassword) {

        if (userRepository.findByLogin(user.getLogin()) != null) {
            throw new IllegalArgumentException("El nombre de usuario (Login) ya existe.");
        }

        List<String> passwordErrors = passwordValidator.getValidationErrors(rawPassword);
        if (!passwordErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", passwordErrors));
        }

        String hash = passwordHasher.hash(rawPassword);
        user.setPasswordHash(hash);

        return userRepository.save(user);
    }

    public User login(String login, String rawPassword) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            return null;
        }
        
        if (!user.isActive()) {
            throw new IllegalStateException("El usuario está inactivo.");
        }

        if (passwordHasher.verify(user.getPasswordHash(), rawPassword)) {
            return user;
        }

        return null;
    }

    public List<User> getUsersByRole(Role role) {
        List<User> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (u.getRole() == role) {
                result.add(u);
            }
        }
        return result;
    }
}
