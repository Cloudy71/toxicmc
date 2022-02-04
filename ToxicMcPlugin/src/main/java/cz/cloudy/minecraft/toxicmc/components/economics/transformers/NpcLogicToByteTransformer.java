/*
  User: Cloudy
  Date: 26/01/2022
  Time: 23:34
*/

package cz.cloudy.minecraft.toxicmc.components.economics.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.core.types.Pair;
import cz.cloudy.minecraft.toxicmc.components.economics.NpcLogic;

import java.util.List;
import java.util.Objects;

/**
 * @author Cloudy
 */
public class NpcLogicToByteTransformer
        implements IDataTransformer<Class<? extends NpcLogic>, Byte> {
    private static final List<Pair<Class<? extends NpcLogic>, Byte>> logicList =
            List.of(

            );

    @Override
    public Byte transform0to1(Class<? extends NpcLogic> value) {
        if (value == null)
            return null;
        return logicList.stream()
                        .filter(classBytePair -> classBytePair.getKey() == value)
                        .map(Pair::getValue)
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public Class<? extends NpcLogic> transform1to0(Byte value) {
        if (value == null)
            return null;
        return logicList.stream()
                        .filter(classBytePair -> Objects.equals(classBytePair.getValue(), value))
                        .map(Pair::getKey)
                        .findFirst()
                        .orElse(null);
    }
}
