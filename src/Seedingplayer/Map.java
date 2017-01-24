package Seedingplayer;

import battlecode.common.*;

/**
 * Created by Pau on 24/01/2017.
 */
public class Map {

    private static RobotController rc;

    public static float mapMinX = -Constants.INF;
    public static float mapMinY = -Constants.INF;
    public static float mapMaxX = Constants.INF;
    public static float mapMaxY = Constants.INF;

    public static void init(RobotController rc2){
        rc = rc2;
    }

    static void checkMapBounds() {
        if (rc == null){
            System.out.println("ERROR: intent de fer Map.checkMapBounds() sense haver assignat rc");
            return;
        }
        try {
            if (mapMaxY == Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_UPPER_BOUND));
                if (bound == Constants.INF) {
                    MapLocation m = checkMapBound(Direction.NORTH);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_UPPER_BOUND, Float.floatToIntBits(m.y));
                        mapMaxY = bound;
                    }
                }
                else {
                    mapMaxY = bound;
                }
            }
            if (mapMinY == -Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_LOWER_BOUND));
                if (bound == -Constants.INF) {
                    MapLocation m = checkMapBound(Direction.SOUTH);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_LOWER_BOUND, Float.floatToIntBits(m.y));
                        mapMinY = m.y;
                    }
                }
                else {
                    mapMinY = bound;
                }
            }
            if (mapMinX == -Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_LEFT_BOUND));
                if (bound == -Constants.INF) {
                    MapLocation m = checkMapBound(Direction.WEST);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_LEFT_BOUND, Float.floatToIntBits(m.x));
                        mapMinX = m.x;
                    }
                }
                else {
                    mapMinX = bound;
                }
            }
            if (mapMaxX == Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_RIGHT_BOUND));
                if (bound == Constants.INF) {
                    MapLocation m = checkMapBound(Direction.EAST);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_RIGHT_BOUND, Float.floatToIntBits(m.x));
                        mapMaxX = m.x;
                    }
                }
                else {
                    mapMaxX = bound;
                }
            }

        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }


    static MapLocation checkMapBound(Direction dir) {
        try {
            MapLocation pos = rc.getLocation();
            if (!rc.onTheMap(pos.add(dir, rc.getType().sensorRadius- Constants.eps))) {
                float a = 0, b = rc.getType().sensorRadius;
                while (b-a >= Constants.PRECISION_MAP_BOUNDS) {
                    float c = (b+a)/2;
                    if (rc.onTheMap(pos.add(dir, c))) a = c;
                    else b = c;
                }
                return pos.add(dir, (a+b)/2);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean onCurrentMap(MapLocation pos){
        return mapMinX < pos.x && pos.x < mapMaxX && mapMinY < pos.y && pos.y < mapMaxY;
    }
}
