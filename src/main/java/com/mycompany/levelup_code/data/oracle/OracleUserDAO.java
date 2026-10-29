package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.User;
import java.util.Optional;

/**
 * Implementación de acceso a datos en Oracle para la entidad {@link User}.
 */
public class OracleUserDAO implements UserRepository {

    @Override
    public Optional<User> findByEmail(String email) {
        throw new UnsupportedOperationException("Pendiente de implementación en fase de conexión a Oracle.");
    }

    @Override
    public User save(User user) {
        throw new UnsupportedOperationException("Pendiente de implementación en fase de conexión a Oracle.");
    }

    @Override
    public User update(User user) {
        throw new UnsupportedOperationException("Pendiente de implementación en fase de conexión a Oracle.");
    }
}
