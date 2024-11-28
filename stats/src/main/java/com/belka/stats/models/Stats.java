package com.belka.stats.models;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class Stats {
    private Long id;
    private Long userId;
    private String handlerCode;
    private OffsetDateTime requestTime;
}
