/*
  User: Cloudy
  Date: 20/01/2022
  Time: 23:58
*/

package cz.cloudy.minecraft.core.componentsystem.types;

import com.cronutils.model.time.ExecutionTime;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cron;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
public class CronData {
    private final Cron                     cron;
    private final Object                   component;
    private final Method                   method;
    private final com.cronutils.model.Cron cronObject;

    private ZonedDateTime nextRun;

    public CronData(Cron cron, Object component, Method method, com.cronutils.model.Cron cronObject) {
        this.cron = cron;
        this.component = component;
        this.method = method;
        this.cronObject = cronObject;
    }

    public Cron cron() {
        return cron;
    }

    public Object component() {
        return component;
    }

    public Method method() {
        return method;
    }

    public com.cronutils.model.Cron cronObject() {
        return cronObject;
    }

    // ============================================

    public void calculateNextRun() {
        ExecutionTime executionTime = ExecutionTime.forCron(cronObject);
        nextRun = executionTime.nextExecution(ZonedDateTime.now()).orElse(null);
    }

    public boolean canRun() {
        if (nextRun == null)
            return false;

        ZonedDateTime now = ZonedDateTime.now();
        return now.isEqual(nextRun) || now.isAfter(nextRun);
    }
}
