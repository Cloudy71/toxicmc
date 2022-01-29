/*
  User: Cloudy
  Date: 07/01/2022
  Time: 21:53
*/

package cz.cloudy.minecraft.core.types;

/**
 * @author Cloudy
 */
public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
