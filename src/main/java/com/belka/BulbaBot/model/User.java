package com.belka.BulbaBot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Entity
public class User {
    @Id
    @Column(name = "id")
    private long id;
    private String firstname;
    private String lastname;
    private String username;
    private Timestamp registeredAt;

}
