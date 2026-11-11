package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.User;
import java.util.Optional;

/**
 * Puerto de persistencia requerido por {@link UserService}.
 * La capa DAO debe implementar este contrato sin exponer SQL al servicio.
 */
public interface UserRepository {

    default Optional<User> findById(int id) {
        return Optional.empty();
    }

    Optional<User> findByEmail(String email);

    User save(User user);

    /** Actualiza un usuario existente. */
    User update(User user);

    /** Restores exhausted lives if the database's daily rule allows it. */
    default int recoverLivesIfEligible(int userId) {
        throw new UnsupportedOperationException("La recuperación de vidas no está disponible en este repositorio.");
    }
}
