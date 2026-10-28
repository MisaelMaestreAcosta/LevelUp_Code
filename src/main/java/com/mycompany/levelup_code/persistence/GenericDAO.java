package com.mycompany.levelup_code.persistence;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    T insert(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean update(T entity);
    boolean delete(ID id);
}