CREATE TABLE IF NOT EXISTS users
(
    id            bigint PRIMARY KEY,
    firstname     varchar,
    lastname      varchar,
    registered_at timestamptz,
    username      varchar
);
comment on table users is 'Stores all users';

create table if not exists subscriptions
(
    producer   bigint not null,
    subscriber bigint not null,
    PRIMARY KEY (producer, subscriber),
    FOREIGN KEY (producer) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (subscriber) REFERENCES users (id) ON DELETE CASCADE
);
comment on table subscriptions is 'Stores all users subscriptions';

CREATE TABLE IF NOT EXISTS weather_history
(
    id   bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    temp int,
    date date,
    city varchar
);
comment on table weather_history is 'Stores information about weather in different cities';

CREATE TABLE IF NOT EXISTS previous_step
(
    user_id       bigint PRIMARY KEY,
    previous_step varchar,
    next_step     varchar,
    data          varchar
);
comment on table previous_step is 'Stores information about previous step of user in the bot';

CREATE TABLE IF NOT EXISTS audio
(
    id        varchar PRIMARY KEY,
    user_id   bigint,
    date      date,
    is_public boolean,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
comment on table audio is 'Stores information about audio files';

CREATE TABLE IF NOT EXISTS not_listened
(
    subscriber bigint,
    audio_id   varchar,
    PRIMARY KEY (subscriber, audio_id),
    FOREIGN KEY (subscriber) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (audio_id) REFERENCES audio (id) ON DELETE CASCADE
);
comment on table not_listened is 'Stores information about not listened audio files';

CREATE TABLE IF NOT EXISTS stats
(
    id           bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id      bigint,
    request_time timestamptz,
    handler_code varchar
);
comment on table stats is 'Stores information about requests to the bot';

CREATE TABLE IF NOT EXISTS Diary
(
    id      bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id bigint,
    note    text,
    date    date,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
comment on table Diary is 'Stores information about notes of users in the diary';

CREATE TABLE IF NOT EXISTS XXX
(
    id   bigint PRIMARY KEY,
    age  bigint,
    name text
);

