package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.User;
import java.util.Optional;

/**
 * Puerto de persistencia requerido por {@link UserService}.
 * La capa DAO debe implementar este contrato sin exponer SQL al servicio.
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    User save(User user);

    /** Actualiza un usuario existente. */
    User update(User user);
}
