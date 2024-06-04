package edu.tinkoff.imageeditor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "username")
    private String username;

    @Column
    private String password;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserEntity userModel)) return false;
        return Objects.equals(username, userModel.username);
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

}
