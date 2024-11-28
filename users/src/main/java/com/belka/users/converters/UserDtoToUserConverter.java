package com.belka.users.converters;

import com.belka.core.converters.BelkaConverter;
import com.belka.users.entities.UserEntity;
import com.belka.users.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoToUserConverter implements BelkaConverter<User, UserEntity> {
    @Override
    public UserEntity convert(User value) {
        return UserEntity.builder()
                .id(value.getId())
                .firstname(value.getFirstname())
                .lastname(value.getLastname())
                .username(value.getUsername())
                .registeredAt(value.getRegisteredAt())
                .build();
    }

    @Override
    public Class<UserEntity> getOutputType() {
        return UserEntity.class;
    }

    @Override
    public Class<User> getInputType() {
        return User.class;
    }
}
