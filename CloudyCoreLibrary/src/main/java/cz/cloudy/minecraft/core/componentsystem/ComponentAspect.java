/*
  User: Cloudy
  Date: 09/02/2022
  Time: 04:11
*/

package cz.cloudy.minecraft.core.componentsystem;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cached;
import cz.cloudy.minecraft.core.componentsystem.interfaces.ICommandResponseResolvable;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cloudy
 */
@Aspect
public class ComponentAspect {
    private static final Logger logger = LoggerFactory.getLogger(ComponentAspect.class);

    private static final Map<Method, Map<Integer, Object>> cachedCache = new HashMap<>(); // Method => ArrayHashCode => Value

    @Around("@annotation(cz.cloudy.minecraft.core.componentsystem.annotations.Async)")
    public Object asyncExecution(ProceedingJoinPoint joinPoint) {
        Object thisObject = joinPoint.getTarget();
        Preconditions.checkState(thisObject instanceof IComponent, "Async can be used only on components");
        IComponent component = (IComponent) thisObject;
        System.out.println("ASYNC!");
        Bukkit.getScheduler().runTaskAsynchronously(
                component.getPlugin(),
                () -> {
                    try {
                        Object returnValue = joinPoint.proceed(joinPoint.getArgs());
                        if (returnValue instanceof ICommandResponseResolvable response && joinPoint.getArgs().length == 1 &&
                                joinPoint.getArgs()[0] instanceof CommandData commandData) {
                            commandData.sender().sendMessage(response.getComponent(commandData));
                        }
                    } catch (Throwable e) {
                        logger.error("Async method exception: ", e);
                    }
                }
        );
        return null;
    }

    //    @Around("@annotation(cz.cloudy.minecraft.core.componentsystem.annotations.Cached(informative=false))")
    @Around("execution(@cz.cloudy.minecraft.core.componentsystem.annotations.Cached(informative=false) * *(..))")
    public Object aroundCached(ProceedingJoinPoint joinPoint) throws Throwable {
//        Object thisObject = joinPoint.getTarget();
//        Preconditions.checkState(thisObject instanceof IComponent, "Cached can be used only on components");
//        IComponent component = (IComponent) thisObject;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
//        Cached cached = method.getAnnotation(Cached.class);
//        if (cached.informative()) // Ignore if cached annotation is used only as information
//            return joinPoint.proceed(joinPoint.getArgs());
        Preconditions.checkState(methodSignature.getReturnType() != Void.class, "Cached cannot be used on void return types");

        int hash = Arrays.deepHashCode(joinPoint.getArgs());
        logger.info("CACHED: {}", hash);
        return cachedCache.computeIfAbsent(method, m -> new HashMap<>())
                .computeIfAbsent(hash, h -> {
                    logger.info("NEW");
                    try {
                        return joinPoint.proceed(joinPoint.getArgs());
                    } catch (Throwable e) {
                        logger.error("Error while caching method return value: ", e);
                    }
                    return null;
                });
    }
}
