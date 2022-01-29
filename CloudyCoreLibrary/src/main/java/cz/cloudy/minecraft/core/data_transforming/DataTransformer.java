/*
  User: Cloudy
  Date: 15/01/2022
  Time: 18:30
*/

package cz.cloudy.minecraft.core.data_transforming;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.core.types.Pair;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cloudy
 */
@Component
public class DataTransformer
        implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(DataTransformer.class);

    private static final Table<Class<?>, Class<?>, IDataTransformer<?, ?>>                    transformersByConsumers = HashBasedTable.create();
    private static final Map<Class<? extends IDataTransformer<?, ?>>, IDataTransformer<?, ?>> transformersByType      = new HashMap<>();

    @Override
    public void onClassScan(Class<?>[] classes) {
        for (Class<?> clazz : classes) {
            if (!IDataTransformer.class.isAssignableFrom(clazz) || clazz == IDataTransformer.class)
                continue;

            IDataTransformer dataTransformer = (IDataTransformer) ReflectionUtils.newInstance(clazz);
            if (dataTransformer == null) {
                logger.error("An error occurred while registering a DataTransformer {}.", clazz.getSimpleName());
                continue;
            }
            Pair<Class<?>, Class<?>> types = dataTransformer.getTypes();
            transformersByType.put((Class<? extends IDataTransformer<?, ?>>) clazz, dataTransformer);
            transformersByConsumers.put(types.getKey(), types.getValue(), dataTransformer);
            transformersByConsumers.put(types.getValue(), types.getKey(), dataTransformer);
            logger.info("DataTransformer {} has been registered.", clazz.getSimpleName());
        }
    }

    public IDataTransformer<?, ?> getUnknownDataTransformer(Class<? extends IDataTransformer<?, ?>> clazz) {
        return transformersByType.get(clazz);
    }

    public <T0, T1> IDataTransformer<T0, T1> getDataTransformer(Class<? extends IDataTransformer<T0, T1>> clazz) {
        return (IDataTransformer<T0, T1>) transformersByType.get(clazz);
    }

    public <T0, T1> IDataTransformer<T0, T1> getDataTransformer(Class<T0> class0, Class<T1> class1) {
        if (transformersByConsumers.contains(class0, class1))
            return (IDataTransformer<T0, T1>) transformersByConsumers.get(class0, class1);
        return null;
    }
}
