package com.belka.users.services;

import com.belka.core.converters.ConverterService;
import com.belka.users.models.User;
import com.belka.users.entities.SubscriptionEntity;
import com.belka.users.entities.UserEntity;
import com.belka.users.repositories.SubscriptionsRepository;
import com.belka.users.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SubscriptionsRepository subscriptionsRepository;
    private final ConverterService converterService;

    @Override
    public Collection<User> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userEntity -> converterService.convertTo(User.class, userEntity))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(User user) {
        userRepository.save(converterService.convertTo(UserEntity.class, user));
        log.info("user was saved");
    }

    @Override
    public boolean existsById(Long chatId) {
        return userRepository.existsById(chatId);
    }

    @Override
    @Transactional
    public void toSubscribe(Long chatId, Long producerId) {
        UserEntity subscriber = userRepository.findById(chatId).orElseThrow(RuntimeException::new);
        UserEntity producer = userRepository.findById(producerId).orElseThrow(RuntimeException::new);
        subscriptionsRepository.save(SubscriptionEntity.builder()
                .producer(producer)
                .subscriber(subscriber)
                .build());
    }

    @Override
    @Transactional
    public void toUnsubscribe(Long userId, Long producerId) {
        subscriptionsRepository.deleteByProducerAndSubscriber(producerId, userId);
    }

    @Override
    public Collection<Pair<Long, String>> getProducersNamesAndId(Long userId) {
        return userRepository.findAllProducers(userId).stream()
                .map(entity -> {
                            if (entity.getUsername() == null) {
                                return Pair.of(
                                        entity.getId(),
                                        entity.getFirstname());

                            } else {
                                return Pair.of(
                                        entity.getId(),
                                        entity.getFirstname() + " (" + entity.getUsername() + ")");
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getProducersNames(Long userId) {
        return userRepository.findAllProducers(userId).stream()
                .map(entity -> {
                    if (entity.getUsername() == null) {
                        return entity.getFirstname();
                    } else {
                        return entity.getFirstname() + " (" + entity.getUsername() + ")";
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getFollowersNames(Long userId) {
        return userRepository.findAllFollowers(userId).stream()
                .map(entity -> {
                    if (entity.getUsername() == null) {
                        return entity.getFirstname();
                    } else {
                        return entity.getFirstname() + " (" + entity.getUsername() + ")";
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Long> getFollowersId(Long userId) {
        return subscriptionsRepository.findAllFollowersID(userId);
    }

    @Override
    public String getName(Long userId) {
        UserEntity entity = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        return getNameFromEntity(entity);
    }

    private String getNameFromEntity(UserEntity entity) {
        if (entity.getUsername() == null) {
            return entity.getFirstname();
        }
        return "@" + entity.getUsername();
    }
}
