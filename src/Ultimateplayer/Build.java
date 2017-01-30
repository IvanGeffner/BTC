package Ultimateplayer;

import battlecode.common.*;

import java.util.Arrays;

/**
 * Created by Pau on 24/01/2017.
 */
public class Build {

    private static RobotController rc;

    static void init(RobotController rc2){
        rc = rc2;
    }
    static final int factor = (1 << 14);
    static int[] intervals;
    static int cont;
    static int overlap;

    static void encode(float angle, int bit){
        int a = Math.round(factor*angle);
        intervals[cont] = (a << 1)+(bit&1);
        ++cont;
    }

    static Direction findDirectionToBuild(Direction baseDir, float r){

        MapLocation pos = rc.getLocation();

        float R = rc.getType().bodyRadius + 2*r;
        cont = 0; overlap = 0;

        RobotInfo Ri[] = rc.senseNearbyRobots(R);
        TreeInfo Ti[] = rc.senseNearbyTrees(R);

        intervals = new int[2*Ti.length + 2*Ri.length + 8];

        float Rr = r + rc.getType().bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET;

        for (RobotInfo ri : Ri){
            MapLocation m = ri.getLocation();
            float a = pos.distanceTo(m), b = r + ri.getType().bodyRadius;
            float x = (a*a + Rr*Rr - b*b)/(2.0f * a* Rr);
            if (-1 <= x && x <= 1){
                x = (float)Math.acos(x) + 0.0001f;
                Direction dir = pos.directionTo(m), dirRight = dir.rotateRightRads(x), dirLeft = dir.rotateLeftRads(x);
                float right = baseDir.radiansBetween(dirRight);
                if (right < 0) right += 2*Math.PI;
                float left = baseDir.radiansBetween(dirLeft);
                if (left < 0) left += 2*Math.PI;
                encode(right, 0);
                encode(left, 1);
                if (left < right) ++overlap;
            }
        }
        for (TreeInfo ti : Ti){
            MapLocation m = ti.getLocation();
            float a = pos.distanceTo(m), b = r + ti.getRadius();
            float x = (a*a + Rr*Rr - b*b)/(2.0f * a* Rr);
            if (-1 <= x && x <= 1){
                x = (float)Math.acos(x) + 0.0001f ;
                Direction dir = pos.directionTo(m), dirRight = dir.rotateRightRads(x), dirLeft = dir.rotateLeftRads(x);
                float right = baseDir.radiansBetween(dirRight);
                if (right < 0) right += 2*Math.PI;
                float left = baseDir.radiansBetween(dirLeft);
                if (left < 0) left += 2*Math.PI;
                encode(right, 0);
                encode(left, 1);
                if (left < right) ++overlap;
            }
        }

        checkBoundaries(r, baseDir);

        intervals = Arrays.copyOf(intervals, cont);

        if (cont > 0) IfSorting.quickSortOnly(intervals);

        return closestDir(baseDir);
    }

    static void checkBoundaries(float r, Direction baseDir){
        MapLocation pos = rc.getLocation();
        float dist = Math.abs(Map.maxX - pos.x) - r;
        float Rr = r + rc.getType().bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET;
        if (dist <= Rr){
            float angle = (float)Math.acos(dist/Rr) + Constants.eps;
            Direction dir = Direction.getEast(), dirRight = dir.rotateRightRads(angle), dirLeft = dir.rotateLeftRads(angle);
            float right = baseDir.radiansBetween(dirRight);
            if (right < 0) right += 2*Math.PI;
            float left = baseDir.radiansBetween(dirLeft);
            if (left < 0) left += 2*Math.PI;
            encode(right, 0);
            encode(left, 1);
            if (left < right) ++overlap;
        }
        dist = Math.abs(Map.minX - pos.x) - r;
        if (dist <= Rr){
            float angle = (float)Math.acos(dist/Rr) + Constants.eps;
            Direction dir = Direction.getWest(), dirRight = dir.rotateRightRads(angle), dirLeft = dir.rotateLeftRads(angle);
            float right = baseDir.radiansBetween(dirRight);
            if (right < 0) right += 2*Math.PI;
            float left = baseDir.radiansBetween(dirLeft);
            if (left < 0) left += 2*Math.PI;
            encode(right, 0);
            encode(left, 1);
            if (left < right) ++overlap;
        }
        dist = Math.abs(Map.maxY - pos.y) - r;
        if (dist <= Rr){
            float angle = (float)Math.acos(dist/Rr) + Constants.eps;
            Direction dir = Direction.getNorth(), dirRight = dir.rotateRightRads(angle), dirLeft = dir.rotateLeftRads(angle);
            float right = baseDir.radiansBetween(dirRight);
            if (right < 0) right += 2*Math.PI;
            float left = baseDir.radiansBetween(dirLeft);
            if (left < 0) left += 2*Math.PI;
            encode(right, 0);
            encode(left, 1);
            if (left < right) ++overlap;
        }
        dist = Math.abs(Map.minY - pos.y) - r;
        if (dist <= Rr){
            float angle = (float)Math.acos(dist/Rr) + Constants.eps;
            Direction dir = Direction.getSouth(), dirRight = dir.rotateRightRads(angle), dirLeft = dir.rotateLeftRads(angle);
            float right = baseDir.radiansBetween(dirRight);
            if (right < 0) right += 2*Math.PI;
            float left = baseDir.radiansBetween(dirLeft);
            if (left < 0) left += 2*Math.PI;
            encode(right, 0);
            encode(left, 1);
            if (left < right) ++overlap;
        }
    }


    static Direction closestDir(Direction baseDir){
        if (overlap == 0) return baseDir;

        float minAngle = 100;

        for (int i = 0; i < intervals.length; ++i){
            int a = intervals[i];
            if ((a&1) == 0){
                if (overlap == 0){
                    float angle = (float)(a >> 1)/factor;
                    float absangle = Math.min(angle, 2*(float)Math.PI - angle);
                    if (absangle < minAngle){
                        minAngle = angle;
                    }
                }
                ++overlap;
            }
            else{
                --overlap;
                if (overlap == 0){
                    float angle = (float)(a >> 1)/factor;
                    float absangle = Math.min(angle, 2*(float)Math.PI - angle);
                    if (absangle < minAngle){
                        minAngle = angle;
                    }
                }
            }
        }
        if (minAngle < 100) return baseDir.rotateLeftRads(minAngle);
        return null;
    }


    static void incrementRobotsBuilt(){
        try {
            int robots_built = rc.readBroadcast(Communication.ROBOTS_BUILT);
            rc.broadcast(Communication.ROBOTS_BUILT, robots_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
