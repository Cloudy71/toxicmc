/*
  User: Cloudy
  Date: 02/02/2022
  Time: 03:00
*/

package cz.cloudy.minecraft.core.data_transforming.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.core.types.Pair;

/**
 * @author Cloudy
 */
public class Int2ToStringTransform
        implements IDataTransformer<Int2, String> {

    @Override
    public String transform0to1(Int2 value) {
        return value.getX()+","+value.getY();
    }

    @Override
    public Int2 transform1to0(String value) {
        return new Int2(value);
    }
}
