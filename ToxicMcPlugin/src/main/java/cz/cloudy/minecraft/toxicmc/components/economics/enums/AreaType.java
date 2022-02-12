package cz.cloudy.minecraft.toxicmc.components.economics.enums;

import java.util.HashMap;
import java.util.Map;

public class AreaType {
    private static final Map<Byte, AreaType> typeMap = new HashMap<>();

    public static final AreaType SHOP_BUY = new AreaType((byte) 4, "Shop Buy");
    public static final AreaType SHOP_STOCK = new AreaType((byte) 3, "Shop Stock");
    public static final AreaType SHOP = new AreaType((byte) 2, "Shop", SHOP_STOCK, SHOP_BUY);
    public static final AreaType STOCK = new AreaType((byte) 1, "Stock");
    public static final AreaType GLOBAL = new AreaType((byte) 0, "Global", STOCK, SHOP);

    private final byte value;
    private final String name;
    private final AreaType[] supportedAreas;

    private AreaType(byte value, String name, AreaType... supportedAreas) {
        this.value = value;
        this.name = name;
        this.supportedAreas = supportedAreas;

        typeMap.put(this.value, this);
    }

    public byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public AreaType[] getSupportedAreas() {
        return supportedAreas;
    }

    public static AreaType resolveByByte(byte value) {
        return typeMap.get(value);
    }
}
