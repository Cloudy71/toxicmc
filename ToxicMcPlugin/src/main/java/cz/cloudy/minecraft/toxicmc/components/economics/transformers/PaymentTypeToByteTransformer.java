/*
  User: Cloudy
  Date: 31/01/2022
  Time: 02:49
*/

package cz.cloudy.minecraft.toxicmc.components.economics.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;

/**
 * @author Cloudy
 */
public class PaymentTypeToByteTransformer
        implements IDataTransformer<PaymentType, Byte> {
    @Override
    public Byte transform0to1(PaymentType value) {
        return value.getValue();
    }

    @Override
    public PaymentType transform1to0(Byte value) {
        return PaymentType.resolveByByte(value);
    }
}
