/*
  User: Cloudy
  Date: 02/02/2022
  Time: 03:01
*/

package cz.cloudy.minecraft.core.types;

import com.google.common.base.Objects;

/**
 * @author Cloudy
 */
public class Int2 {
    private int x;
    private int y;

    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Int2(String str) {
        String[] data = str.split(",");
        if (data.length != 2)
            return;

        this.x = Integer.parseInt(data[0]);
        this.y = Integer.parseInt(data[1]);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Int2 int2 = (Int2) o;
        return x == int2.x && y == int2.y;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y);
    }
}
