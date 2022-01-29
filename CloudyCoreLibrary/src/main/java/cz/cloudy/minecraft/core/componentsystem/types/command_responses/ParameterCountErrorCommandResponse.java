/*
  User: Cloudy
  Date: 18/01/2022
  Time: 03:06
*/

package cz.cloudy.minecraft.core.componentsystem.types.command_responses;

/**
 * @author Cloudy
 */
public class ParameterCountErrorCommandResponse
        extends ErrorCommandResponse {
    public ParameterCountErrorCommandResponse(int argumentCount) {
        super("Wrong parameter count. " + argumentCount + " is required.");
    }
}
