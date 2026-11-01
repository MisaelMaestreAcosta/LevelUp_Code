package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.User;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa {@link UserRepository} delegando
 * las operaciones JDBC sobre la tabla {@code Usuarios} a {@link UserDAO}.
 */
public class OracleUserRepository implements UserRepository {

    private final UserDAO userDAO;

    public OracleUserRepository() {
        this(new UserDAO());
    }

    public OracleUserRepository(UserDAO userDAO) {
        this.userDAO = userDAO != null ? userDAO : new UserDAO();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userDAO.save(user);
    }

    @Override
    public User update(User user) {
        userDAO.update(user);
        return user;
    }
}

