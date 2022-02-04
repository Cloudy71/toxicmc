/*
  User: Cloudy
  Date: 31/01/2022
  Time: 20:32
*/

package cz.cloudy.minecraft.core.maps;

import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * @author Cloudy
 */
@Component
public class MapCanvas {
    public void setRenderer(MapView mapView, MapRenderer renderer) {
        mapView.setScale(MapView.Scale.CLOSEST);
        for (MapRenderer mapViewRenderer : mapView.getRenderers()) {
            mapView.removeRenderer(mapViewRenderer);
        }
        mapView.getRenderers().clear();
        mapView.addRenderer(renderer);
    }
}
