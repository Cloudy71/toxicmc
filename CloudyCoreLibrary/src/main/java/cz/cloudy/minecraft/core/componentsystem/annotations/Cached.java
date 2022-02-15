package cz.cloudy.minecraft.core.componentsystem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Implement automatic cache if informative is false
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Cached {
    /**
     * If set to true, there is its own caching system implemented.
     * Otherwise, parameter-value caching system is used.
     */
    boolean informative() default false;
}
