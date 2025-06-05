package put.poznan.pl.michalxpz.mybatisapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("put.poznan.pl.michalxpz.mybatisapp.repository") // skanuj interfejsy mapper-ów
public class MybatisAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisAppApplication.class, args);
    }

}
