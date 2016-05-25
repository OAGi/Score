package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {

    @Query("select u from User u where u.loginId = ?1")
    public User findOneByLoginId(String loginId);

    @Query("select u from User u where u.name = ?1")
    public User findOneByName(String name);
}
