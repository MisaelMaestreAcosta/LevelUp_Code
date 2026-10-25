package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.User;
import java.util.Optional;

/**
 * Puerto de persistencia requerido por {@link UserService}.
 * La capa DAO debe implementar este contrato sin exponer SQL al servicio.
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    User save(User user);
}
