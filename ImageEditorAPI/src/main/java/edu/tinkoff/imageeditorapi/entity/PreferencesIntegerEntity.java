package edu.tinkoff.imageeditorapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "preferences_integer")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesIntegerEntity {

    @Id
    private String key;

    private Integer value;

}
