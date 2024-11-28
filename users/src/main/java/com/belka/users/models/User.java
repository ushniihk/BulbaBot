package com.belka.users.models;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class User {
    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private OffsetDateTime registeredAt;
}
