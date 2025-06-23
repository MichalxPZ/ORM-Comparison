package put.poznan.pl.michalxpz.jooqapp.interceptor;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LoggingExecuteListener implements ExecuteListener {
    private static final Logger log = LoggerFactory.getLogger(LoggingExecuteListener.class);
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public void start(ExecuteContext ctx) {
        startTime.set(System.nanoTime());
    }

    @Override
    public void end(ExecuteContext ctx) {
        long duration = System.nanoTime() - startTime.get();
        startTime.remove();

        if (ctx.batchQueries().length != 0) {
            log.info("jOOQ Batch Execution: {} statements in {} ms",
                    ctx.batchQueries().length,
                    TimeUnit.NANOSECONDS.toMillis(duration));
        } else {
            log.info("jOOQ Statement Execution: [{}] in {} ms",
                    ctx.sql(),
                    TimeUnit.NANOSECONDS.toMillis(duration));
        }
    }
}
