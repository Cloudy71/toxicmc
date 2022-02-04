/*
  User: Cloudy
  Date: 31/01/2022
  Time: 20:40
*/

package cz.cloudy.minecraft.core.maps.map_renderers;

import cz.cloudy.minecraft.core.LoggerFactory;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cloudy
 */
public class ImageMapRenderer
        extends MapRenderer {
    private static final Logger logger = LoggerFactory.getLogger(ImageMapRenderer.class);

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    private int   x;
    private int   y;
    private Image image;

    private boolean rendered;

    private ImageMapRenderer(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    private ImageMapRenderer(Image image) {
        this(0, 0, image);
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (rendered)
            return;
        canvas.drawImage(x, y, image);
        rendered = true;
    }

    public static BufferedImage loadImage(String path) {
        if (imageCache.containsKey(path))
            return imageCache.get(path);
        try {
            BufferedImage image = ImageIO.read(new File(path));
            imageCache.put(path, image);
            return image;
        } catch (IOException e) {
            logger.error("Failed to load image: ", e);
        }
        return null;
    }

    public static ImageMapRenderer build(Image image) {
        return new ImageMapRenderer(image);
    }

    public static ImageMapRenderer build(String imagePath) {
        return build(loadImage(imagePath));
    }

    public static ImageMapRenderer buildScaled(Image image, int width, int height) {
        Image scaledImg = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        return build(scaledImg);
    }

    public static ImageMapRenderer buildScaled(String imagePath, int width, int height) {
        return buildScaled(loadImage(imagePath), width, height);
    }

    public static ImageMapRenderer buildCentered(BufferedImage image) {
        ImageMapRenderer renderer = build(image);
        renderer.x = 64 - image.getWidth() / 2;
        renderer.y = 64 - image.getHeight() / 2;
        return renderer;
    }

    public static ImageMapRenderer buildCentered(String imagePath) {
        return buildCentered(loadImage(imagePath));
    }

    public static ImageMapRenderer buildCenteredScaled(BufferedImage image, int width, int height) {
        ImageMapRenderer renderer = buildScaled(image, width, height);
        renderer.x = 64 - width / 2;
        renderer.y = 64 - height / 2;
        return renderer;
    }

    public static ImageMapRenderer buildCenteredScaled(String imagePath, int width, int height) {
        return buildCenteredScaled(loadImage(imagePath), width, height);
    }

    public static ImageMapRenderer buildImagePart(BufferedImage image, int x, int y, int width, int height) {
        return build(image.getSubimage(x, y, width, height));
    }

    public static ImageMapRenderer buildImagePart(String path, int x, int y, int width, int height) {
        return buildImagePart(loadImage(path), x, y, width, height);
    }
}
