/*
  User: Cloudy
  Date: 04/02/2022
  Time: 15:14
*/

package cz.cloudy.minecraft.core.maps;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.maps.pojo.MapRecord;
import cz.cloudy.minecraft.core.types.Int2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * @author Cloudy
 */
@Aspect
public class MapControllerAspect {
    private static final Logger logger = LoggerFactory.getLogger(MapControllerAspect.class);

    @Pointcut("call(* org.bukkit.entity.ItemFrame.setItem(..))")
    private void pointcutItemFrameSetItem() {
    }

    @Before("pointcutItemFrameSetItem()")
    public void beforeItemFrameSetItem(JoinPoint joinPoint) {
//        logger.info("BEFORE ITEM_FRAME_SET_ITEM: {}", joinPoint.getArgs()[0]);

        MapController mapController = ComponentLoader.getNullable(MapController.class);
        if (mapController == null)
            return;

        ItemFrame itemFrame = (ItemFrame) joinPoint.getTarget();
        ItemStack currentItemStack = itemFrame.getItem();
        ItemStack newItemStack = (ItemStack) joinPoint.getArgs()[0];

//        MapMeta currentMapMeta;
        MapView currentMapView = null;
//        MapMeta newMapMeta;
        MapView newMapView = null;

        if (currentItemStack.getItemMeta() instanceof MapMeta mapMeta) {
//            currentMapMeta = mapMeta;
            currentMapView = mapMeta.getMapView();
        }
        if (newItemStack.getItemMeta() instanceof MapMeta mapMeta) {
//            newMapMeta = mapMeta;
            newMapView = mapMeta.getMapView();
        }

        if (Objects.equals(currentMapView, newMapView) || (currentMapView != null && newMapView != null && currentMapView.getId() == newMapView.getId()))
            return;

        Database database = ComponentLoader.get(Database.class);

        Int2 chunkPosition = new Int2(itemFrame.getChunk().getX(), itemFrame.getChunk().getZ());

        if (currentMapView != null) {
            MapRecord mapRecord = database.findEntity(
                    MapRecord.class,
                    "world = :world AND map_id = :mapId",
                    ImmutableMap.of(
                            "world", currentMapView.getWorld().getName(),
                            "mapId", currentMapView.getId()
                    ),
                    FetchLevel.Primitive
            );
            mapController.removeMapRecordUsage(mapRecord, chunkPosition);
        }

        if (newMapView != null) {
            MapRecord mapRecord = database.findEntity(
                    MapRecord.class,
                    "world = :world AND map_id = :mapId",
                    ImmutableMap.of(
                            "world", newMapView.getWorld().getName(),
                            "mapId", newMapView.getId()
                    ),
                    FetchLevel.Primitive
            );
            mapController.placeMapRecordUsage(mapRecord, chunkPosition);
        }

    }


//    private static final Map<MapMeta, ItemStack> mapMetaOwnerMap = new HashMap<>();

//    @Pointcut("call(* org.bukkit.inventory.meta.MapMeta.setMapView(..))")
//    private void pointcutSetMapView() {
//    }

//    @Pointcut("call(* org.bukkit.inventory.ItemStack.getItemMeta(..))")
//    private void pointcutGetItemMeta() {
//    }
//
//    @Pointcut("call(* org.bukkit.inventory.ItemStack.setItemMeta(..))")
//    private void pointcutSetItemMeta() {
//    }
//
//    @Pointcut("call(* org.bukkit.inventory.meta.MapMeta.setMapView(..))")
//    private void pointcutSetMapView() {
//    }
//
//    @Around("pointcutGetItemMeta()")
//    private Object aroundGetItemMeta(ProceedingJoinPoint joinPoint) throws Throwable {
//        logger.info("AROUND GETITEMMETA");
//
//        if (ComponentLoader.getNullable(MapController.class) == null)
//            return joinPoint.proceed();
//
//        Object value = joinPoint.proceed();
//
//        if (value instanceof MapMeta mapMeta) {
//            mapMetaOwnerMap.put(mapMeta, (ItemStack) joinPoint.getTarget());
//        }
//
//        return value;
//    }
//
//    private void afterSetItemMeta(JoinPoint joinPoint) {
//        logger.info("AFTER SETITEMMETA: {}", joinPoint.getArgs()[0]);
//
//        if (ComponentLoader.getNullable(MapController.class) == null)
//            return;
//
//        ItemMeta itemMeta = (ItemMeta) joinPoint.getArgs()[0];
//        if (itemMeta instanceof MapMeta mapMeta) {
//            mapMetaOwnerMap.remove(mapMeta);
//        }
//    }
//
//    @Before("pointcutSetMapView()")
//    private void beforeSetMapView(JoinPoint joinPoint) {
//        logger.info("BEFORE SETMAPVIEW: {}", joinPoint.getArgs()[0]);
//
//        MapController mapController = ComponentLoader.getNullable(MapController.class);
//        if (mapController == null)
//            return;
//
//        Database database = ComponentLoader.get(Database.class);
//
//        MapMeta mapMeta = (MapMeta) joinPoint.getTarget();
//        ItemStack itemStack = mapMetaOwnerMap.get(mapMeta);
//        if (itemStack == null)
//            return;
//
//        MapView currentMapView = mapMeta.getMapView();
//        MapView newMapView = (MapView) joinPoint.getArgs()[0];
//
//        if (currentMapView == newMapView || (currentMapView != null && newMapView != null && currentMapView.getId() == newMapView.getId()))
//            return;
//
//        MapRecord currentMapRecord;
//        MapRecord newMapRecord;
//
//        Int2 chunkPosition = new Int2(itemStack.);
//
//        if (currentMapView != null) {
//            currentMapRecord = database.findEntity(
//                    MapRecord.class,
//                    "world = :world AND map_id = :mapId",
//                    ImmutableMap.of(
//                            "world", currentMapView.getWorld().getName(),
//                            "mapId", currentMapView.getId()
//                    ),
//                    FetchLevel.Primitive
//            );
//            mapController.removeMapRecordUsage(currentMapRecord, );
//        }
//        if (newMapView != null)
//            newMapRecord = database.findEntity(
//                    MapRecord.class,
//                    "world = :world AND map_id = :mapId",
//                    ImmutableMap.of(
//                            "world", newMapView.getWorld().getName(),
//                            "mapId", newMapView.getId()
//                    ),
//                    FetchLevel.Primitive
//            );
//
//        if (newMapView != null) {
//
//            return;
//        }

//        joinPoint.
//        mapController.placeMapRecordUsage();

//    }
}
