package edu.tinkoff.imageeditor.repository;


import edu.tinkoff.imageeditor.entity.Role;
import edu.tinkoff.imageeditor.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    List<UserEntity> findByRole(Role role);

}
