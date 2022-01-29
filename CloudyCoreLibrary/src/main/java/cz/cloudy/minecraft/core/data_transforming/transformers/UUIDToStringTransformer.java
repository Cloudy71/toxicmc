/*
  User: Cloudy
  Date: 15/01/2022
  Time: 18:23
*/

package cz.cloudy.minecraft.core.data_transforming.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;

import java.util.UUID;

/**
 * @author Cloudy
 */
public class UUIDToStringTransformer
        implements IDataTransformer<UUID, String> {
    @Override
    public String transform0to1(UUID value) {
        return value.toString();
    }

    @Override
    public UUID transform1to0(String value) {
        return UUID.fromString(value);
    }
}
