package com.dmytrozah.profitsoft.config;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {
        "com.dmytrozah.profitsoft.domain.repository"
})
@Configuration
public class JpaConfig {

    @Value("${jdbc.uri:jdbc:postgresql://localhost:5432/profitsoft?sslmode=disable}")
    private String jdbcUrl;

    @Value("${jdbc.user:postgres}")
    private String user;

    @Value("${jdbc.password:root}")
    private String password;

    @Bean
    public HikariDataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(user);
        ds.setPassword(password);

        return ds;
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setDataSource(dataSource());
        return liquibase;
    }
}
