package simpleplayer;

import battlecode.common.Direction;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Mates {

    public static float getAngle (float a, float b, float c){
        double x = (a*a + b*b - c*c)/(2.0f*a*b);
        return (float)Math.acos(x);
    }

    public static boolean cclockwise (Direction a, Direction b, Direction c){ //podem passar directament cx,cy
        float da = c.radiansBetween(a);
        float db = c.radiansBetween(b);
        if (da > db) {
            float ax = a.getDeltaX(1);
            float ay = a.getDeltaY(1);
            float cx = c.getDeltaX(1);
            float cy = c.getDeltaY(1);
            if (cx*ay - cy*ax > 0) return true;
            else return false;
        }
        float bx = b.getDeltaX(1);
        float by = b.getDeltaY(1);
        float cx = c.getDeltaX(1);
        float cy = c.getDeltaY(1);
        if (cx*by - cy*bx > 0) return true;
        else return false;
    }

}
