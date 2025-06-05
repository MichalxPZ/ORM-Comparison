package put.poznan.pl.michalxpz.jdbcapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JdbcAppApplication {

    private static final Logger log = LoggerFactory.getLogger(JdbcAppApplication.class);

    public static void main(String[] args) {
        log.info("🟢 Application started successfully!");
        SpringApplication.run(JdbcAppApplication.class, args);
    }

}
