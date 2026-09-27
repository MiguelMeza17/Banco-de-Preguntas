package co.edu.unicauca.access;

import co.edu.unicauca.domain.User;
import java.util.List;

public interface IUserRepository {
    boolean save(User user);
    User findByLogin(String login);
    List<User> findAll();
}