/*
  User: Cloudy
  Date: 17/01/2022
  Time: 18:17
*/

package cz.cloudy.minecraft.core;

import org.slf4j.Logger;

/**
 * @author Cloudy
 */
public class LoggerFactory {
    public static Logger getLogger(Class<?> clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz.getSimpleName());
    }
}
