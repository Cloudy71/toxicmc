/*
  User: Cloudy
  Date: 21/01/2022
  Time: 00:39
*/

package cz.cloudy.minecraft.core.componentsystem.types;

import cz.cloudy.minecraft.core.componentsystem.annotations.ActionListener;

import java.lang.reflect.Method;

/**
 * @author Cloudy
 */
public record ActionListenerData(Object component, ActionListener actionListener, Method method) {
}
