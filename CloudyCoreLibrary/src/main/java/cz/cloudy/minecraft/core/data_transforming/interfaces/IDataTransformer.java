/*
  User: Cloudy
  Date: 15/01/2022
  Time: 18:20
*/

package cz.cloudy.minecraft.core.data_transforming.interfaces;

import cz.cloudy.minecraft.core.types.Pair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Cloudy
 */
public interface IDataTransformer<T0, T1> {
    default Pair<Class<T0>, Class<T1>> getTypes() {
        Type t0 = ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        Type t1 = ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[1];
        Class<T0> c0 = (Class<T0>) (t0 instanceof ParameterizedType pt0 ? pt0.getRawType() : t0);
        Class<T1> c1 = (Class<T1>) (t1 instanceof ParameterizedType pt1 ? pt1.getRawType() : t1);
        return new Pair<>(c0, c1);
    }

    T1 transform0to1(T0 value);

    T0 transform1to0(T1 value);
}
