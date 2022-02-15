package cz.cloudy.minecraft.core.database.annotation;

import cz.cloudy.minecraft.core.database.DatabaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Join {
    Class<? extends DatabaseEntity> table();

    String where();
}
