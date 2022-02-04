/*
  User: Cloudy
  Date: 31/01/2022
  Time: 02:47
*/

package cz.cloudy.minecraft.toxicmc.components.economics.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cloudy
 */
public class PaymentType {
    private static final Map<Byte, PaymentType> typeMap = new HashMap<>();

    public static final PaymentType PRIVATE        = new PaymentType((byte) 0, "");
    public static final PaymentType COMPANY_AREA   = new PaymentType((byte) 1, "?company.area?");
    public static final PaymentType COMPANY_WORKER = new PaymentType((byte) 2, "?company.worker?");
    public static final PaymentType CONTRACT       = new PaymentType((byte) 3, "?contract?");
    public static final PaymentType COMPANY_BANNER = new PaymentType((byte) 4, "?company.banner?");

    private final byte   value;
    private final String message;

    public PaymentType(byte value, String message) {
        this.value = value;
        this.message = message;

        typeMap.put(this.value, this);
    }

    public byte getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    public static PaymentType resolveByByte(byte value) {
        return typeMap.get(value);
    }
}
