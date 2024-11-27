package com.belka.audio.entities;

import lombok.NonNull;

import java.io.Serializable;

public class NotListenedKey implements Serializable {
    @NonNull
    private Long subscriber;
    @NonNull
    private String audioId;
}