package EfficientShooting;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Created by Pau on 24/01/2017.
 */
public class Map {

    private static RobotController rc;

    public static float minX = -Constants.INF;
    public static float minY = -Constants.INF;
    public static float maxX = Constants.INF;
    public static float maxY = Constants.INF;

    public static void init(RobotController rc2){
        rc = rc2;
    }

    static void checkMapBounds() {
        if (rc == null){
            System.out.println("ERROR: intent de fer Map.checkMapBounds() sense haver assignat rc");
            return;
        }
        try {
            if (maxY == Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_UPPER_BOUND));
                if (bound == Constants.INF) {
                    MapLocation m = checkMapBound(Direction.NORTH);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_UPPER_BOUND, Float.floatToIntBits(m.y));
                        maxY = bound;
                    }
                }
                else {
                    maxY = bound;
                }
            }
            if (minY == -Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_LOWER_BOUND));
                if (bound == -Constants.INF) {
                    MapLocation m = checkMapBound(Direction.SOUTH);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_LOWER_BOUND, Float.floatToIntBits(m.y));
                        minY = m.y;
                    }
                }
                else {
                    minY = bound;
                }
            }
            if (minX == -Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_LEFT_BOUND));
                if (bound == -Constants.INF) {
                    MapLocation m = checkMapBound(Direction.WEST);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_LEFT_BOUND, Float.floatToIntBits(m.x));
                        minX = m.x;
                    }
                }
                else {
                    minX = bound;
                }
            }
            if (maxX == Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_RIGHT_BOUND));
                if (bound == Constants.INF) {
                    MapLocation m = checkMapBound(Direction.EAST);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_RIGHT_BOUND, Float.floatToIntBits(m.x));
                        maxX = m.x;
                    }
                }
                else {
                    maxX = bound;
                }
            }

        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }


    private static MapLocation checkMapBound(Direction dir) {
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
        return minX < pos.x && pos.x < maxX && minY < pos.y && pos.y < maxY;
    }

    public static boolean onCurrentMap(MapLocation pos, float r){
        return  minX < pos.x - r &&
                pos.x + r < maxX &&
                minY < pos.y - r &&
                pos.y + r < maxY;
    }

    static float distToEdge(MapLocation pos){
        return Math.min(Math.min(pos.x-minX,pos.y-minY),Math.min(maxX-pos.x,maxY-pos.y));
    }
}
