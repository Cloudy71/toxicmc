/*
  User: Cloudy
  Date: 06/01/2022
  Time: 18:47
*/

package cz.cloudy.minecraft.messengersystem;

import cz.cloudy.minecraft.core.CorePlugin;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.ComponentScan;
import org.slf4j.Logger;

/**
 * @author Cloudy
 */
@ComponentScan(classes = {
        MessengerSystemPlugin.class
})
public class MessengerSystemPlugin
        extends CorePlugin {
    private static final Logger logger = LoggerFactory.getLogger(MessengerSystemPlugin.class);

    @Override
    public void onStart() {
    }

    @Override
    public void onLoaded() {
        logger.info("MessengerSystem has been enabled.");

        saveDefaultConfig();
    }

    @Override
    public void doPostProcess() {
        super.doPostProcess();

    }

    @Override
    public void onUnloaded() {
        logger.info("LoginSystem has been disabled.");
    }
}
