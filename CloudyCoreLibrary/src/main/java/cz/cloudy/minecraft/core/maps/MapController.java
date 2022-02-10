/*
  User: Cloudy
  Date: 01/02/2022
  Time: 15:47
*/

package cz.cloudy.minecraft.core.maps;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.CheckConfiguration;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.data_transforming.DataTransformer;
import cz.cloudy.minecraft.core.data_transforming.transformers.Int2ToStringTransform;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.maps.pojo.MapRecord;
import cz.cloudy.minecraft.core.maps.pojo.MapRecordChunk;
import cz.cloudy.minecraft.core.types.Int2;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.slf4j.Logger;

/**
 * @author Cloudy
 */
@Component
@CheckConfiguration("maps.mapController=true")
public class MapController
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    @Component
    private Database database;

    @Component
    private DataTransformer dataTransformer;

    @Override
    public void onStart() {
        logger.info("ENTITIES");
        for (World world : Bukkit.getWorlds()) {
            logger.info("WORLD: {}", world.getName());
            for (Entity entity : world.getEntities()) {
                logger.info("Entity: {}", entity);
            }
        }
    }

    public MapView requestMap(World world) {
        MapRecord mapRecord = database.findEntity(
                MapRecord.class,
                "usage = 0 AND world = :world",
                ImmutableMap.of("world", world.getName()),
                FetchLevel.Primitive
        );

        if (mapRecord != null) {
//            mapRecord.setUsage(0);
//            mapRecord.saveAsync();
            return Bukkit.getMap(mapRecord.getMapId());
        }

        MapView mapView = Bukkit.createMap(world);
        mapRecord = new MapRecord();
        mapRecord.setWorld(world);
        mapRecord.setMapId(mapView.getId());
        mapRecord.setUsage(0);
        mapRecord.save();
//        mapRecord.saveAsync();

        return mapView;
    }

//    public void freeMap(MapView mapView) {
//        MapRecord mapRecord = database.findEntity(
//                MapRecord.class,
//                "world = :world AND map_id = :mapId",
//                ImmutableMap.of("world", mapView.getWorld().getName(),
//                                "mapId", mapView.getId()),
//                FetchLevel.Primitive
//        );
//
//        if (mapRecord != null) {
//            mapRecord.setUsage(0);
//            mapRecord.saveAsync();
//        } else {
//            mapRecord = new MapRecord();
//            mapRecord.setWorld(mapView.getWorld());
//            mapRecord.setMapId(mapView.getId());
//            mapRecord.setUsage(0);
//            mapRecord.saveAsync();
//        }
//
//        for (MapRecordChunk chunk : database.findEntities(
//                MapRecordChunk.class,
//                "map_record.id = :id",
//                ImmutableMap.of("id", mapRecord.getId()),
//                FetchLevel.Primitive
//        )) {
//            Chunk loadedChunk = chunk.getMapRecord().getWorld().getChunkAt(chunk.getChunkPosition().getX(), chunk.getChunkPosition().getY());
//            if (loadedChunk.isLoaded())
//                loadedChunk.load();
//
//            for (Entity entity : loadedChunk.getEntities()) {
//                if (!(entity instanceof ItemFrame itemFrame) || !(itemFrame.getItem().getItemMeta() instanceof MapMeta mapMeta) ||
//                    mapMeta.getMapView() == null || mapMeta.getMapView().getId() != chunk.getMapRecord().getMapId())
//                    continue;
//                itemFrame.setItem(null);
//            }
//
//            // TODO: Find all ItemFrames and clear them
//            chunk.deleteAsync();
//        }
//    }

    protected void placeMapRecordUsage(MapRecord mapRecord, Int2 chunkPosition) {
        MapRecordChunk chunk = new MapRecordChunk();
        chunk.setMapRecord(mapRecord);
        chunk.setChunkPosition(chunkPosition);
        chunk.saveAsync();
        mapRecord.setUsage(mapRecord.getUsage() + 1);
        mapRecord.saveAsync();
    }

    protected void removeMapRecordUsage(MapRecord mapRecord, Int2 chunkPosition) {
        MapRecordChunk chunk = database.findEntity(
                MapRecordChunk.class,
                "map_record.id = :id AND chunk_position = :pos",
                ImmutableMap.of(
                        "id", mapRecord.getId(),
                        "pos", dataTransformer.getDataTransformer(Int2ToStringTransform.class)
                                              .transform0to1(chunkPosition)
                ),
                FetchLevel.Primitive
        );
        if (chunk != null)
            chunk.deleteAsync();

        mapRecord.setUsage(mapRecord.getUsage() - 1);
        mapRecord.saveAsync();
    }

//    public void setItemFrameMapView(ItemFrame itemFrame, int mapId) {
//        if (itemFrame.getItem().getItemMeta() instanceof MapMeta) {
//            clearItemFrameMapView(itemFrame);
//        }
//
//        MapView map = Bukkit.getMap(mapId);
//        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
//        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
//        mapMeta.setMapView(map);
//        itemStack.setItemMeta(mapMeta);
//        itemFrame.setItem(itemStack);
//
//        MapRecord mapRecord = database.findEntity(
//                MapRecord.class,
//                "world = :world AND map_id = :mapId",
//                ImmutableMap.of(
//                        "world", mapMeta.getMapView().getWorld().getName(),
//                        "mapId", mapMeta.getMapView().getId()
//                ),
//                FetchLevel.Primitive
//        );
//        placeMapRecordUsage(mapRecord, new Int2(itemFrame.getChunk().getX(), itemFrame.getChunk().getZ()));
//    }
//
//    public void clearItemFrameMapView(ItemFrame itemFrame) {
//        Preconditions.checkState(itemFrame.getItem().getItemMeta() instanceof MapMeta);
//        ItemStack itemStack = itemFrame.getItem();
//        MapMeta mapMeta = (MapMeta) itemFrame.getItem().getItemMeta();
//        Preconditions.checkNotNull(mapMeta.getMapView());
//        itemFrame.setItem(null);
//
//        MapRecord mapRecord = database.findEntity(
//                MapRecord.class,
//                "world = :world AND map_id = :mapId",
//                ImmutableMap.of(
//                        "world", mapMeta.getMapView().getWorld().getName(),
//                        "mapId", mapMeta.getMapView().getId()
//                ),
//                FetchLevel.Primitive
//        );
//        removeMapRecordUsage(mapRecord, new Int2(itemFrame.getChunk().getX(), itemFrame.getChunk().getZ()));
//    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHangingBreakEvent(HangingBreakEvent e) {
        logger.info("HANGING BREAK: {}", e.getEntity());
        if (!(e.getEntity() instanceof ItemFrame itemFrame) || !(itemFrame.getItem().getItemMeta() instanceof MapMeta mapMeta) ||
            mapMeta.getMapView() == null)
            return;

        MapRecord mapRecord = database.findEntity(
                MapRecord.class,
                "world = :world AND map_id = :mapId",
                ImmutableMap.of(
                        "world", mapMeta.getMapView().getWorld().getName(),
                        "mapId", mapMeta.getMapView().getId()
                ),
                FetchLevel.Primitive
        );

        if (mapRecord == null)
            return;

        removeMapRecordUsage(mapRecord, new Int2(itemFrame.getChunk().getX(), itemFrame.getChunk().getZ()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemFrameChangeEvent(PlayerItemFrameChangeEvent e) {
        logger.info("MATERIAL: {}", e.getItemStack().getType());
        logger.info("ACTION: {}", e.getAction());
        if (e.getItemStack().getType() != Material.FILLED_MAP || e.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE)
            return;

        MapMeta mapMeta = (MapMeta) e.getItemStack().getItemMeta();

        MapRecord mapRecord = database.findEntity(
                MapRecord.class,
                "world = :world AND map_id = :mapId",
                ImmutableMap.of(
                        "world", mapMeta.getMapView().getWorld().getName(),
                        "mapId", mapMeta.getMapView().getId()
                ),
                FetchLevel.Primitive
        );
        if (mapRecord == null)
            return;

        if (e.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE) {
            placeMapRecordUsage(mapRecord, new Int2(e.getItemFrame().getChunk().getX(), e.getItemFrame().getChunk().getZ()));
            return;
        }
        removeMapRecordUsage(mapRecord, new Int2(e.getItemFrame().getChunk().getX(), e.getItemFrame().getChunk().getZ()));
    }
}
