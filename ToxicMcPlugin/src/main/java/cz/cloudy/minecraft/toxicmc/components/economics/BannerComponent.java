/*
  User: Cloudy
  Date: 31/01/2022
  Time: 23:48
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.game.EntityUtils;
import cz.cloudy.minecraft.core.maps.MapCanvas;
import cz.cloudy.minecraft.core.maps.MapController;
import cz.cloudy.minecraft.core.maps.map_renderers.ImageMapRenderer;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.Banner;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.BannerPart;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.Company;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.map.MapView;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cloudy
 */
@Component
public class BannerComponent
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(BannerComponent.class);

    @Component
    private Database database;

    @Component
    private MapCanvas mapCanvas;

    @Component
    private EntityUtils entityUtils;

    @Component
    private MapController mapController;

    @Override
    public void onStart() {
        for (Banner banner : database.findEntities(
                Banner.class,
                null,
                null,
                FetchLevel.Primitive
        )) {
//            MapView mapView = Bukkit.getMap(banner.getMapId());
//            String path = getPlugin().getDataFolder().getAbsolutePath() + "/Images/" + banner.getImagePath();
//            mapCanvas.setRenderer(mapView, ImageMapRenderer.buildScaled(path, 128, 128));
            redrawBanner(banner);
        }
    }

    public Int2 getBannerSize(String path) {
        BufferedImage image = ImageMapRenderer.loadImage(getPlugin().getDataFolder().getAbsolutePath() + "/Images/" + path);
        if (image == null) {
            return null;
        }

        int xParts = image.getWidth() / 128;
        int yParts = image.getHeight() / 128;

        return new Int2(xParts, yParts);
    }

    public Banner createBanner(Player player, Company owner, String path) {
        BufferedImage image = ImageMapRenderer.loadImage(getPlugin().getDataFolder().getAbsolutePath() + "/Images/" + path);
        if (image == null) {
            return null;
        }

        int xParts = image.getWidth() / 128;
        int yParts = image.getHeight() / 128;

//        PlayerEmployee playerEmployee = entityUtils.getMetadata(player, ToxicConstants.PLAYER_EMPLOYEE);
        Banner banner = new Banner();
        banner.setOwner(owner.getUuid());
        banner.setImagePath(path);
        banner.setWorld(player.getWorld());
        banner.setPlaceSize(new Int2(xParts, yParts));
        banner.save();

        int totalParts = xParts * yParts;
        for (int i = 0; i < totalParts; i++) {
            MapView mapView = mapController.requestMap(player.getWorld());
            BannerPart bannerPart = new BannerPart();
            bannerPart.setBanner(banner);
            bannerPart.setMapId(mapView.getId());
            bannerPart.setUsed(true);
            bannerPart.save();
        }

        redrawBanner(banner);
        return banner;
    }

    public void redrawBanner(Banner banner) {
        List<BannerPart> bannerParts = new ArrayList<>(banner.getUsedBannerParts());

        BufferedImage image = ImageMapRenderer.loadImage(getPlugin().getDataFolder().getAbsolutePath() + "/Images/" + banner.getImagePath());
        int xParts = image.getWidth() / 128;
        int yParts = image.getHeight() / 128;
        for (int i = 0; i < bannerParts.size(); i++) {
            int y = i / xParts;
            int x = i - (y * xParts);
            logger.info("{}: {}, {} = {}", i, x, y, bannerParts.get(i).getMapId());
            MapView mapView = Bukkit.getMap(bannerParts.get(i).getMapId());
            mapCanvas.setRenderer(mapView, ImageMapRenderer.buildImagePart(
                    image,
                    x * 128,
                    y * 128,
                    128,
                    128
            ));
        }

        banner.setPlaceSize(new Int2(xParts, yParts));
    }

    @Nullable
    public List<ItemFrame> mapAllItemFrames(ItemFrame base, Int2 size) {
        List<ItemFrame> frames = new ArrayList<>();
        int xMove = 0;
        int zMove = 0;
        // NORTH = X ++
        // SOUTH = X --
        // EAST = Z ++
        // WEST = Z --
        if (base.getAttachedFace() == BlockFace.NORTH)
            xMove = 1;
        else if (base.getAttachedFace() == BlockFace.SOUTH)
            xMove = -1;
        else if (base.getAttachedFace() == BlockFace.EAST)
            zMove = 1;
        else
            zMove = -1;

        List<ItemFrame> itemFrames = base.getWorld()
                                         .getNearbyEntities(
                                                 new BoundingBox(
                                                         base.getLocation().getBlockX() - 1,
                                                         base.getLocation().getBlockY() + 1,
                                                         base.getLocation().getBlockZ() - 1,
                                                         base.getLocation().getBlockX() + 1 + xMove * size.getX(),
                                                         base.getLocation().getBlockY() - 1 - size.getY(),
                                                         base.getLocation().getBlockZ() + 1 + zMove * size.getX()
                                                 ),
                                                 entity -> entity instanceof ItemFrame itemFrame && itemFrame.getAttachedFace() == base.getAttachedFace()
                                         ).stream()
                                         .map(entity -> (ItemFrame) entity)
                                         .toList(); // TODO: Rework into forEach

        Vector basePosition = base.getLocation().toBlockLocation().toVector();
        for (int i = 0; i < size.getY(); ++i) {
            for (int j = 0; j < size.getX(); ++j) {
                Vector position = new Vector(basePosition.getX() + xMove * j, basePosition.getY() - i, basePosition.getZ() + zMove * j);
                logger.info("SEARCH FRAME: {}, {}", i * size.getY() + j, position);
                ItemFrame itemFrame = itemFrames.stream()
                                                .filter(frame -> frame.getLocation().toBlockLocation().toVector().equals(position))
                                                .findFirst()
                                                .orElse(null);
                if (itemFrame == null) {
                    return null;
                }
                frames.add(i * size.getY() + j, itemFrame);
            }
        }
        return frames;
    }

    public void placeBanner(Banner banner, List<ItemFrame> itemFrames) {
        List<BannerPart> bannerParts = new ArrayList<>(banner.getUsedBannerParts());
        for (int i = 0; i < itemFrames.size(); i++) {
            ItemFrame itemFrame = itemFrames.get(i);
            BannerPart bannerPart = bannerParts.get(i);

            mapController.setItemFrameMapView(itemFrame, bannerPart.getMapId());
            itemFrame.setMetadata(ToxicConstants.BANNER_ITEM_FRAMES, new FixedMetadataValue(getPlugin(), itemFrames));
            itemFrame.setFixed(true);
        }
    }

//    @EventHandler
//    public void onHangingBreakEvent(HangingBreakEvent e) {
//        if (!(e.getEntity() instanceof ItemFrame itemFrame) || !(itemFrame.getItem().getItemMeta() instanceof MapMeta mapMeta) ||
//            mapMeta.getMapView() == null || !itemFrame.hasMetadata(ToxicConstants.BANNER_ITEM_FRAMES))
//            return;
//
//        List<ItemFrame> itemFrames = entityUtils.getMetadata(itemFrame, ToxicConstants.BANNER_ITEM_FRAMES, null);
//        Preconditions.checkNotNull(itemFrames);
//        for (ItemFrame frame : itemFrames) {
//            if (frame == null)
//                continue;
//            frame.setItem(null);
//        }
//    }
//
//    @EventHandler
//    public void onPlayerItemFrameChangeEvent(PlayerItemFrameChangeEvent e) {
//        if (e.getItemStack().getType() != Material.FILLED_MAP || e.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE
//            || e.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE || !e.getItemFrame().hasMetadata(ToxicConstants.BANNER_ITEM_FRAMES))
//            return;
//
//        List<ItemFrame> itemFrames = entityUtils.getMetadata(e.getItemFrame(), ToxicConstants.BANNER_ITEM_FRAMES, null);
//        Preconditions.checkNotNull(itemFrames);
//        for (ItemFrame frame : itemFrames) {
//            if (frame == null)
//                continue;
//            frame.setItem(null);
//        }
//    }
}
