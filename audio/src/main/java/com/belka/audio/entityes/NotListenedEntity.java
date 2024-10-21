package com.belka.audio.entityes;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "not_listened")
@Entity
/**
 * Entity class for not listened audios.
 */
public class NotListenedEntity {
    @Id
    @Column(name = "subscriber")
    @NonNull
    private Long subscriber;

    @Column(name = "audio_id")
    @NonNull
    private String audioId;
}
