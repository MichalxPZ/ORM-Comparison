package put.poznan.pl.michalxpz.jdbcapp.interceptor;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Configuration
public class DataSourceProxyConfig {

    @Bean
    public BeanPostProcessor dataSourceBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof DataSource) {
                    ProxyFactory factory = new ProxyFactory(bean);
                    factory.setProxyTargetClass(true);
                    factory.addAdvice(new ProxyDataSourceInterceptor((DataSource) bean));
                    return factory.getProxy();
                }
                return bean;
            }

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return bean;
            }
        };
    }

    private static class ProxyDataSourceInterceptor implements MethodInterceptor {
        private final DataSource dataSource;

        public ProxyDataSourceInterceptor(final DataSource dataSource) {
            this.dataSource = ProxyDataSourceBuilder.create(dataSource)
                    .countQuery()
                    .build();
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            Method proxyMethod = ReflectionUtils.findMethod(dataSource.getClass(), invocation.getMethod().getName());
            if (proxyMethod != null) {
                return proxyMethod.invoke(dataSource, invocation.getArguments());
            }
            return invocation.proceed();
        }
    }
}
