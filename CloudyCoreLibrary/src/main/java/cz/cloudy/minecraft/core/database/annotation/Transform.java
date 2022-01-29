/*
  User: Cloudy
  Date: 15/01/2022
  Time: 18:20
*/

package cz.cloudy.minecraft.core.database.annotation;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Cloudy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transform {
    Class<? extends IDataTransformer<?, ?>> value();
}
