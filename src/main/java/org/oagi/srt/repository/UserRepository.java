package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.User;

public interface UserRepository {

    public User findOneByLoginId(String loginId);

    public User findOneByName(String name);
}
