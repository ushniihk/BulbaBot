package com.belka.users.converters;

import com.belka.core.converters.BelkaConverter;
import com.belka.users.entities.UserEntity;
import com.belka.users.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserToUserDtoConverter implements BelkaConverter<UserEntity, User> {
    @Override
    public User convert(UserEntity value) {
        return User.builder()
                .id(value.getId())
                .firstname(value.getFirstname())
                .lastname(value.getLastname())
                .username(value.getUsername())
                .registeredAt(value.getRegisteredAt())
                .build();
    }

    @Override
    public Class<User> getOutputType() {
        return User.class;
    }

    @Override
    public Class<UserEntity> getInputType() {
        return UserEntity.class;
    }
}
