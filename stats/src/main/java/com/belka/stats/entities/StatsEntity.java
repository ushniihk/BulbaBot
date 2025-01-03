package com.belka.stats.entities;

import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stats")
@Entity
public class StatsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "handler_code")
    private String handlerCode;
    @Column(name = "request_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime requestTime;
}
