/*
  User: Cloudy
  Date: 17/01/2022
  Time: 01:10
*/

package cz.cloudy.minecraft.core.componentsystem.annotations;

import java.lang.annotation.*;

/**
 * @author Cloudy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckConditions {
    CheckCondition[] value();
}
