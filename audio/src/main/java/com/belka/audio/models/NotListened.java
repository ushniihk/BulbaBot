package com.belka.audio.models;

import lombok.Builder;
import lombok.Data;

/**
 * Stores information about not listened audio files
 */
@Data
@Builder
public class NotListened {
    private Long subscriber;
    private String audioId;
}