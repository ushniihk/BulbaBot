package com.belka.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Core configuration class for setting up beans and data sources.
 */
@Configuration
@EntityScan("com.belka.core")
@EnableJpaRepositories("com.belka.core")
@Slf4j
public class CoreConfig {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;

    /**
     * Configures the data source for PostgreSQL.
     *
     * @return the configured DataSource
     */
    @Bean
    public DataSource postgresDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(postgresDataSource());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return namedParameterJdbcTemplate().getJdbcTemplate();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    HttpHeaders headers() {
        return new HttpHeaders();
    }

    @Bean
    public ScheduledExecutorService ScheduledExecutorService() {
        return Executors.newScheduledThreadPool(100);
    }

    /**
     * Shuts down the ExecutorService when it's no longer needed.
     */
    @PreDestroy
    public void shutDownExecutorService() {
        ScheduledExecutorService().shutdown();
        try {
            if (!ScheduledExecutorService().awaitTermination(60, TimeUnit.SECONDS)) {
                ScheduledExecutorService().shutdownNow();
                if (!ScheduledExecutorService().awaitTermination(60, TimeUnit.SECONDS))
                    log.error("ExecutorService did not terminate");
            }
        } catch (InterruptedException ie) {
            ScheduledExecutorService().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}