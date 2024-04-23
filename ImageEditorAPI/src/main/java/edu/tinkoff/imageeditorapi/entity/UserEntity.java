package edu.tinkoff.imageeditorapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserEntity userModel)) {
            return false;
        }
        return Objects.equals(username, userModel.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

}
