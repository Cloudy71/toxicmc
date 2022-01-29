/*
  User: Cloudy
  Date: 10/01/2022
  Time: 01:57
*/

package cz.cloudy.minecraft.core.componentsystem;

import cz.cloudy.minecraft.core.LoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Cloudy
 */
public class ReflectionUtils {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    public static List<Field> getAllClassFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.stream(clazz.getDeclaredFields()).toList());
        if (clazz.getSuperclass() != Object.class)
            fields.addAll(getAllClassFields(clazz.getSuperclass()));
        return fields;
    }

    public static Optional<Object> getValue(Field field, Object obj) {
        field.setAccessible(true);
        Object value;
        try {
            value = field.get(obj);
        } catch (IllegalAccessException e) {
            logger.error("", e);
            return Optional.empty();
        }
        field.setAccessible(false);
        return Optional.ofNullable(value);
    }

    public static void setValue(Field field, Object obj, Object value) {
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }
        field.setAccessible(false);
    }

    public static <T> T newInstance(Class<T> clazz, Class<?>[] constructorTypes, Object[] constructorValues) {
        try {
            Constructor<T> constructor = clazz.getConstructor(constructorTypes);
            return constructor.newInstance(constructorValues);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to create new instance: ", e);
        }
        return null;
    }

    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, new Class[0], new Object[0]);
    }

    public static Object invoke(Method method, Object object, Object... parameters) {
        method.setAccessible(true);
        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("", e);
        }
        if (!Modifier.isPublic(method.getModifiers()))
            method.setAccessible(false);
        return null;
    }
}
