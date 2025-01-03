package com.belka.users.services;

import com.belka.users.models.User;
import com.belka.users.entities.UserEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

/**
 * service for interaction with {@link UserEntity users}
 */
public interface UserService {
    Collection<User> getAll();

    void save(User user);

    boolean existsById(Long userId);

    /**
     * subscribe for user's updates;
     *
     * @param userId     {@link UserEntity follower}'s ID
     * @param producerId {@link UserEntity producer}'s ID
     */
    void toSubscribe(Long userId, Long producerId);

    /**
     * unsubscribe for user's updates;
     *
     * @param userId     {@link UserEntity follower}'s ID
     * @param producerId {@link UserEntity producer}'s ID
     */
    void toUnsubscribe(Long userId, Long producerId);

    /**
     * get names, usernames and Ids user subscribed to
     *
     * @param userId {@link UserEntity user}'s id
     * @return list of the users' names, usernames and Ids
     */
    Collection<Pair<Long, String>> getProducersNamesAndId(Long userId);

    /**
     * get names and usernames of the people user subscribed to
     *
     * @param userId {@link UserEntity user}'s id
     * @return list of the users' names and usernames
     */
    Collection<String> getProducersNames(Long userId);

    /**
     * get names and usernames of the people who are subscribed to the user
     *
     * @param userId {@link UserEntity user}'s id
     * @return list of the names and usernames
     */
    Collection<String> getFollowersNames(Long userId);

    /**
     * get the ID of the user's subscribers
     *
     * @param userId {@link UserEntity user}'s id
     * @return list of the ID
     */
    Collection<Long> getFollowersId(Long userId);

    /**
     * get {@link UserEntity user}'s id
     *
     * @param userId {@link UserEntity user}'s id
     * @return name
     */
    String getName(Long userId);
}
