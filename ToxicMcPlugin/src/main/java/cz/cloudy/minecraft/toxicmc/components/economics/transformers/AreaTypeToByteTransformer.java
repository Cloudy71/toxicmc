package cz.cloudy.minecraft.toxicmc.components.economics.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.AreaType;

public class AreaTypeToByteTransformer implements IDataTransformer<AreaType, Byte> {
    @Override
    public Byte transform0to1(AreaType value) {
        return value.getValue();
    }

    @Override
    public AreaType transform1to0(Byte value) {
        return AreaType.resolveByByte(value);
    }
}
