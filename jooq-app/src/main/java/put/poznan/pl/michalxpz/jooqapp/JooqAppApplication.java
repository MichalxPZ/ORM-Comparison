package put.poznan.pl.michalxpz.jooqapp;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import put.poznan.pl.michalxpz.jooqapp.interceptor.LoggingExecuteListener;

import javax.sql.DataSource;

@SpringBootApplication
public class JooqAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(JooqAppApplication.class, args);
	}

	@Bean
	public DSLContext dslContext(DataSource dataSource, Environment env) {
		String dialectStr = env.getProperty("JOOQ_DIALECT", "POSTGRES").toUpperCase();
		SQLDialect dialect = SQLDialect.valueOf(dialectStr);

		Settings settings = new Settings();
		settings.withRenderSchema(dialect != SQLDialect.POSTGRES);
		settings.withExecuteLogging(true);

		DefaultConfiguration configuration = new DefaultConfiguration();
		configuration.setSQLDialect(dialect);
		configuration.setDataSource(dataSource);
		configuration.setSettings(settings);
		configuration.setExecuteListener(new LoggingExecuteListener());

		return DSL.using(configuration);
	}

}
