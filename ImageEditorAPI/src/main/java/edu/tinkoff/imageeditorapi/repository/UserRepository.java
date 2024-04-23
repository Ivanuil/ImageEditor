package edu.tinkoff.imageeditorapi.repository;


import edu.tinkoff.imageeditorapi.entity.Role;
import edu.tinkoff.imageeditorapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    List<UserEntity> findByRole(Role role);

}
