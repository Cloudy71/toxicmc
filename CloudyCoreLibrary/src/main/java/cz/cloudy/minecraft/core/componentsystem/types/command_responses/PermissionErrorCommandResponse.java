/*
  User: Cloudy
  Date: 17/01/2022
  Time: 00:51
*/

package cz.cloudy.minecraft.core.componentsystem.types.command_responses;

/**
 * @author Cloudy
 */
// TODO: Message should be editable with some translation configuration
public class PermissionErrorCommandResponse
        extends ErrorCommandResponse {

    public PermissionErrorCommandResponse() {
        super("Insufficient permissions.");
    }
}
