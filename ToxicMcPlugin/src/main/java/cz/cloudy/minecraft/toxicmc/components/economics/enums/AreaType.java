package cz.cloudy.minecraft.toxicmc.components.economics.enums;

import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;

public class AreaType {
    private static final Map<Byte, AreaType> typeMap = new HashMap<>();

    public static final byte SHOP_BUY_VALUE = (byte) 4;
    public static final byte SHOP_STOCK_VALUE = (byte) 3;
    public static final byte SHOP_VALUE = (byte) 2;
    public static final byte STOCK_VALUE = (byte) 1;
    public static final byte GLOBAL_VALUE = (byte) 0;

    public static final AreaType SHOP_BUY = new AreaType(SHOP_BUY_VALUE, "Shop Buy", Color.GREEN);
    public static final AreaType SHOP_STOCK = new AreaType(SHOP_STOCK_VALUE, "Shop Stock", Color.NAVY);
    public static final AreaType SHOP = new AreaType(SHOP_VALUE, "Shop", Color.RED, SHOP_STOCK, SHOP_BUY);
    public static final AreaType STOCK = new AreaType(STOCK_VALUE, "Stock", Color.BLUE);
    public static final AreaType GLOBAL = new AreaType(GLOBAL_VALUE, "Global", Color.YELLOW, STOCK, SHOP);

    private final byte value;
    private final String name;
    private final Color color;
    private final AreaType[] supportedAreas;

    private AreaType(byte value, String name, Color color, AreaType... supportedAreas) {
        this.value = value;
        this.name = name;
        this.color = color;
        this.supportedAreas = supportedAreas;

        typeMap.put(this.value, this);
    }

    public byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public AreaType[] getSupportedAreas() {
        return supportedAreas;
    }

    public static AreaType resolveByByte(byte value) {
        return typeMap.get(value);
    }
}
