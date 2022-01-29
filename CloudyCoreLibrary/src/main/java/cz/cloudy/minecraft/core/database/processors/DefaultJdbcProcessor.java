/*
  User: Cloudy
  Date: 25/01/2022
  Time: 05:00
*/

package cz.cloudy.minecraft.core.database.processors;

import cz.cloudy.minecraft.core.database.interfaces.IDatabaseMapper;
import cz.cloudy.minecraft.core.database.interfaces.IDatabaseProcessor;

/**
 * @author Cloudy
 */
// TODO: Move all Mysql queries here.
public abstract class DefaultJdbcProcessor<T extends IDatabaseMapper>
        implements IDatabaseProcessor<T> {
}
