package com.belka.audio.entityes;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "audio")
@Entity
public class AudioEntity {
    @Id
    @Column(name = "id")
    @NonNull
    private String id;
    @Column(name = "user_id")
    @NonNull
    private Long userId;
    /**
     * Indicates whether the audio is public or not.
     */
    @Column(name = "is_public")
    private boolean isPublic;
    /**
     * The date when the audio was uploaded.
     */
    private LocalDate date;
}
