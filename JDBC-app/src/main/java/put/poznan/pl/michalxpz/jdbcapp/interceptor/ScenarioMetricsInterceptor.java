package put.poznan.pl.michalxpz.jdbcapp.interceptor;

import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class ScenarioMetricsInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;
    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final ClassLoadingMXBean classBean;
    private final ThreadLocal<Long> startCpuTime = new ThreadLocal<>();
    private final ThreadLocal<Long> startNanoTime = new ThreadLocal<>();
    private final ThreadLocal<Integer> startLoadedClassesCount = new ThreadLocal<>();
    private final ThreadLocal<Long> startHeapUsed = new ThreadLocal<>();

    @Value("${orm:unknown}")
    private String orm;

    @Value("${db:unknown}")
    private String db;

    public ScenarioMetricsInterceptor(MeterRegistry meterRegistry, String orm, String db) {
        this.meterRegistry = meterRegistry;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.classBean = ManagementFactory.getClassLoadingMXBean();
        this.orm = orm;
        this.db = db;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startCpuTime.set(osBean.getProcessCpuTime());
        startNanoTime.set(System.nanoTime());
        startLoadedClassesCount.set(classBean.getLoadedClassCount());
        startHeapUsed.set(memoryBean.getHeapMemoryUsage().getUsed());
        QueryCountHolder.clear();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long endCpu = osBean.getProcessCpuTime();
        long endTime = System.nanoTime();
        long cpuTime = endCpu - startCpuTime.get();
        long wallTime = endTime - startNanoTime.get();
        int cpus = osBean.getAvailableProcessors();
        double cpuUsage = (wallTime > 0) ? ((double) cpuTime / (wallTime * cpus)) : 0.0;

        Object paramValue = RequestContextHolder.currentRequestAttributes().getAttribute("benchmarkParam", RequestAttributes.SCOPE_REQUEST);
        String paramTag = paramValue != null ? String.valueOf(paramValue) : "none";

        String scenario = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (scenario == null) {
            scenario = request.getRequestURI(); // fallback
        }

        // CPU usage
        DistributionSummary.builder(String.format("%s_cpu_usage", scenario))
                .description("CPU usage ratio during the scenario (process)")
                .tags("scenario", scenario, "orm", orm, "db", db, "param", paramTag)
                .register(meterRegistry)
                .record(cpuUsage);

        // Heap memory
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        DistributionSummary.builder(String.format("%s_memory_heap_used_bytes", scenario))
                .description("Heap memory used during scenario")
                .baseUnit("bytes")
                .tags("scenario", scenario, "orm", orm, "db", db, "param", paramTag)
                .register(meterRegistry)
                .record(heapUsed);

        long heapUsedDelta = heapUsed - startHeapUsed.get();
        DistributionSummary.builder(String.format("%s_memory_heap_used_delta_bytes", scenario)) // <-- ZMIANA NAZWY
                .description("Net change in heap memory used during scenario")
                .baseUnit("bytes")
                .tags("scenario", scenario, "orm", orm, "db", db, "param", paramTag)
                .register(meterRegistry)
                .record(heapUsedDelta);

        int endLoadedClasses = classBean.getLoadedClassCount();
        int loadedClassesDelta = endLoadedClasses - startLoadedClassesCount.get();         int loadedClasses = classBean.getLoadedClassCount();
        DistributionSummary.builder(String.format("%s_classes_loaded", scenario))
                .description("Number of classes loaded by ClassLoader")
                .tags("scenario", scenario, "orm", orm, "db", db, "param", paramTag)
                .register(meterRegistry)
                .record(loadedClassesDelta);

        QueryCount queryCount = QueryCountHolder.getGrandTotal();
        long totalQueries = queryCount != null ? queryCount.getTotal() : 0;
        DistributionSummary.builder(String.format("%s_sql_queries", scenario))
                .description("Number of SQL queries executed during scenario")
                .tags("scenario", scenario, "orm", orm, "db", db, "param", paramTag)
                .register(meterRegistry)
                .record(totalQueries);

        QueryCountHolder.clear();
    }
}
