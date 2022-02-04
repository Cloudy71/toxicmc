/*
  User: Cloudy
  Date: 07/01/2022
  Time: 04:26
*/

package cz.cloudy.minecraft.core;

import cz.cloudy.minecraft.core.componentsystem.annotations.ComponentScan;
import org.slf4j.Logger;


/**
 * @author Cloudy
 */
@ComponentScan("cz.cloudy.minecraft.core")
public final class CoreRunnerPlugin
        extends CorePlugin {
    private static final Logger logger = LoggerFactory.getLogger(CoreRunnerPlugin.class);

    public static CoreRunnerPlugin singleton;

    @Override
    public void onStart() {
        singleton=this;
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void doPostProcess() {
        super.doPostProcess();

        getComponentLoader().registerCronExecutor(this);
    }

    @Override
    public void onUnloaded() {

    }
}
