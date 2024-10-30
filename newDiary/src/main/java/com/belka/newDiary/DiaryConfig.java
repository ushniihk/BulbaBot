package com.belka.newDiary;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.belka.newDiary.entity")
@EnableJpaRepositories("com.belka.newDiary.repository")
@ComponentScan("com.belka.newDiary")
public class DiaryConfig {

}
