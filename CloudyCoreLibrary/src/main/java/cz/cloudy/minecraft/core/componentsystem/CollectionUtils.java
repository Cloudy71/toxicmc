package cz.cloudy.minecraft.core.componentsystem;

import java.util.*;

public class CollectionUtils {
    public static boolean isCollectionTypeSupported(Class<? extends Collection<?>> type) {
        return Set.class.isAssignableFrom(type) || List.class.isAssignableFrom(type);
    }

    public static <T> Collection<T> getCollection(Class<Collection<T>> type, Collection<T> initialItems) {
        if (!isCollectionTypeSupported(type))
            return null;

        if (Set.class.isAssignableFrom(type))
            return new HashSet<>(initialItems);
        if (List.class.isAssignableFrom(type))
            return new ArrayList<>(initialItems);
        return null;
    }
}
