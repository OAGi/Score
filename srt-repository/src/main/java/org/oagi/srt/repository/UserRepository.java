package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.appUserId from User u where u.loginId = ?1")
    public int findAppUserIdByLoginId(String loginId);

    @Query("select u from User u where u.loginId = ?1")
    public User findOneByLoginId(String loginId);

    @Query("select u from User u where u.appUserId in ?1")
    public List<User> findAllByUserIds(Collection<Long> userIds);

    @Query("select u from User u where u.name = ?1")
    public User findOneByName(String name);
}
