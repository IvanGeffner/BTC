package simpleplayer;

import battlecode.common.Direction;
import scala.collection.immutable.Stream;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Mates {

    public static float getAngle (float a, float b, float c){
        double x = (a*a + b*b - c*c)/(2.0f*a*b);
        return (float)Math.acos(x);
    }

    public static boolean cclockwise (Direction a, Direction b, Direction c, boolean left) { //podem passar directament cx,cy
        if (b == null) return true;
        if (a == null) return false;
        float da = c.radiansBetween(a);
        float db = c.radiansBetween(b);
        return ((da > db) == left);
    }
}
