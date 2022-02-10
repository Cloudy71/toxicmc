/*
  User: Cloudy
  Date: 08/01/2022
  Time: 04:13
*/

package cz.cloudy.minecraft.core.database.enums;

/**
 * @author Cloudy
 */
public enum FetchLevel {
    /**
     * We know only primary key, other fields including foreign keys are all null.
     */
    None((byte) 0),
    /**
     * We know all primitive data, and we know all non-lazy foreign keys on FetchLevel.Primitive level otherwise we know them on FetchLevel.None.
     */
    Primitive((byte) 1),
    /**
     * We know all primitive data, and we know all non-lazy foreign keys on FetchLevel.Full otherwise we know them on FetchLevel.Primitive.
     */
    Full((byte) 2);

    private final byte byteValue;

    FetchLevel(byte byteValue) {
        this.byteValue = byteValue;
    }

    public boolean isHigherThan(FetchLevel fetchLevel) {
        return byteValue > fetchLevel.byteValue;
    }

    public boolean isLowerThan(FetchLevel fetchLevel) {
        return byteValue < fetchLevel.byteValue;
    }
}
