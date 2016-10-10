/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.app.util;

import com.gluonhq.charm.down.common.services.position.Position;

public final class NavigationTools {
    
    /**
     * Finds the direct distance, in meters, between two instances of {@link Position}
     * @param pos1 The start position
     * @param pos2 The end position
     * @return The distance, in meters
     */
    public static double getDistance (Position pos1, Position pos2) {
    	return NavigationTools.distance(pos1.getLatitude(), pos2.getLatitude(), pos1.getLongitude(), pos2.getLongitude());
    }   
    
    /**
     * Calculate distance between two points in latitude and longitude. Uses Haversine method as its base.
     * 
     * Source: http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     * 
     * @param lat1 Start point latitude
     * @param lat2 End point latitude
     * @param lon1 Start point longitude
     * @param lon2 End point longitude
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,double lon2) {
    	return distance(lat1, lat2, lon1, lon2, 0, 0);
    }
    
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * 
     * Source: http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     * 
     * @param lat1 Start point latitude
     * @param lat2 End point latitude
     * @param lon1 Start point longitude
     * @param lon2 End point longitude
     * @param el1 Start altitude in meters
     * @param el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
            double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
