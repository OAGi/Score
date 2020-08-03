package org.oagi.score.repository;

import java.util.List;

public interface SrtRepository<T> {

    List<T> findAll();

    T findById(long id);

}
