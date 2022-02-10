/*
  User: Cloudy
  Date: 20/01/2022
  Time: 23:41
*/

package cz.cloudy.minecraft.toxicmc;

import cz.cloudy.minecraft.core.CorePlugin;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.ComponentScan;
import org.slf4j.Logger;

/**
 * @author Cloudy
 */
@ComponentScan(classes = {
        ToxicMcPlugin.class
})
public class ToxicMcPlugin
        extends CorePlugin {
    private static final Logger logger = LoggerFactory.getLogger(ToxicMcPlugin.class);

    @Override
    public void onStart() {

    }

    @Override
    public void onLoaded() {
        logger.info("ToxicMc has been enabled.");

        saveDefaultConfig();
    }

    @Override
    public void onUnloaded() {
        saveConfig();
    }
}
