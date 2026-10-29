package com.mycompany.levelup_code.data.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica base para las operaciones CRUD de acceso a datos (DAO).
 *
 * @param <T> Tipo de la entidad de dominio
 * @param <ID> Tipo del identificador único de la entidad
 */
public interface GenericDAO<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    T update(T entity);

    void deleteById(ID id);
}
