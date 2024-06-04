package edu.tinkoff.imageeditor.entity;

import lombok.Getter;
import java.util.Set;

@Getter
public enum Role {
    USER;

    private Set<Role> roles;

    static {
        USER.roles = Set.of(USER);
    }
}
