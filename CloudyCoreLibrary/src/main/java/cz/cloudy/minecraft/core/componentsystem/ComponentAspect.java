/*
  User: Cloudy
  Date: 09/02/2022
  Time: 04:11
*/

package cz.cloudy.minecraft.core.componentsystem;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.interfaces.ICommandResponseResolvable;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

/**
 * @author Cloudy
 */
@Aspect
public class ComponentAspect {
    private static final Logger logger = LoggerFactory.getLogger(ComponentAspect.class);

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
}
