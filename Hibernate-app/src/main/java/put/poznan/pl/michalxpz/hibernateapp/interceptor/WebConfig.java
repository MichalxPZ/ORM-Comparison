package put.poznan.pl.michalxpz.hibernateapp.interceptor;


import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${orm:unknown}")
    private String orm;

    @Value("${db:unknown}")
    private String db;

    private final MeterRegistry meterRegistry;

    public WebConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ScenarioMetricsInterceptor(meterRegistry, orm, db));
    }
}


