/*
  User: Cloudy
  Date: 07/01/2022
  Time: 21:27
*/

package cz.cloudy.minecraft.core.database.types;

import cz.cloudy.minecraft.core.database.enums.DatabaseEngine;

/**
 * @author Cloudy
 */
public record DatabaseConnectionData(DatabaseEngine engine, String url, String user, String pass) {
}
