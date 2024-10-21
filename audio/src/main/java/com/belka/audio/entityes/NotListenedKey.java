package com.belka.audio.entityes;

import lombok.NonNull;

import java.io.Serializable;

public class NotListenedKey implements Serializable {
    @NonNull
    private Long subscriber;
    @NonNull
    private String audioId;
}