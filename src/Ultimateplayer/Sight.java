package Ultimateplayer;

import ArchonsVisionPlayer.Constants;
import ArchonsVisionPlayer.Map;
import battlecode.common.*;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Ivan on 1/27/2017.
 */
public class Sight {

    static final int MAXTREES = 10;
    static final float distFactor = 20f;
    static final float angleFactor = 100f;

    static float gradientX, gradientY;
    static int[] intervals;
    static SortedSet<Integer> openObstacles;

    static int index;
    static Direction baseDir;

    static TreeInfo[] Ti;
    static float sightRange;
    static float distMin;
    static float currentAngle;

    static void encodeObstacle(MapLocation myPos, MapLocation obstacleLoc, float radius){
        float angle = (float)Math.asin(radius/myPos.distanceTo(obstacleLoc));
        if (baseDir == null) baseDir = myPos.directionTo(obstacleLoc).rotateRightRads(angle);

        float right = baseDir.radiansBetween(myPos.directionTo(obstacleLoc).rotateRightRads(angle));
        float left = baseDir.radiansBetween(myPos.directionTo(obstacleLoc).rotateLeftRads(angle));

        if (right < 0) right += 2*Math.PI;
        if (left < 0) left += 2*Math.PI;

        float realDist = myPos.distanceTo(obstacleLoc)*(float)Math.cos(angle);
        if (realDist > sightRange - 2.0f/distFactor) realDist = sightRange - 2.0f/distFactor;

        int dist = Math.round(distFactor*realDist); // <= 1000
        int intRight = Math.round(angleFactor*right); // <= 1000
        int intLeft = Math.round(angleFactor*left); // <= 1000


        int encodeRight = (intRight << 20) + (dist << 10) + (index << 1);
        int encodeLeft = (intLeft << 20) + (dist << 10) + (index << 1) + 1;

        intervals[index] = encodeRight;
        ++index;
        intervals[index] = encodeLeft;
        ++index;

        if (left < right){
            int encoded = (encodeRight&0xFFFFF);
            openObstacles.add(encoded);
            float newDist = (float)(encoded >> 10)/distFactor;
            if (newDist < distMin) distMin = newDist;
        }

    }

    static float computeSightRange(RobotController rc){
        return computeSightRange(rc, rc.getLocation(), rc.getType().sensorRadius);
    }

    static void getTrees(RobotController rc, MapLocation loc, float rmin, float rmax){
        Ti = rc.senseNearbyTrees(loc, rmax, null);
        if (Ti.length <= MAXTREES){
            sightRange = rmax;
            return;
        }

        float rmid = rmax;

        while (rmax - rmin > 0.25f){
            rmid = (rmax + rmin)/2.0f;
            Ti = rc.senseNearbyTrees(loc, rmid, null);
            if (Ti.length <= MAXTREES) rmin = rmid;
            else rmax = rmid;
        }

        sightRange = rmid;
        Ti = rc.senseNearbyTrees(loc, rmid, null);

    }

    static void addMapBounds(RobotController rc){
        MapLocation myPos = rc.getLocation();
        float distToMax = Math.abs(ArchonsVisionPlayer.Map.maxX - myPos.x);
        if (distToMax < sightRange){
            float ang = (float)Math.acos(distToMax/sightRange);
            float right = baseDir.radiansBetween(Direction.getEast().rotateRightRads(ang));
            float left = baseDir.radiansBetween(Direction.getEast().rotateLeftRads(ang));

            if (right < 0) right += 2*Math.PI;
            if (left < 0) left += 2*Math.PI;

            int dist = Math.round(distFactor*sightRange) - 1; // <= 1000
            int intRight = Math.round(angleFactor*right); // <= 1000
            int intLeft = Math.round(angleFactor*left); // <= 1000


            int encodeRight = (intRight << 20) + (dist << 10) + (index << 1);
            int encodeLeft = (intLeft << 20) + (dist << 10) + (index << 1) + 1;

            intervals[index] = encodeRight;
            ++index;
            intervals[index] = encodeLeft;
            ++index;

            if (left < right){
                int encoded = (encodeRight&0xFFFFF);
                openObstacles.add(encoded);
                float newDist = (float)(encoded >> 10)/distFactor;
                if (newDist < distMin) distMin = newDist;
            }
        }

        distToMax = Math.abs(ArchonsVisionPlayer.Map.minX - myPos.x);
        if (distToMax < sightRange){
            float ang = (float)Math.acos(distToMax/sightRange);
            float right = baseDir.radiansBetween(Direction.getWest().rotateRightRads(ang));
            float left = baseDir.radiansBetween(Direction.getWest().rotateLeftRads(ang));

            if (right < 0) right += 2*Math.PI;
            if (left < 0) left += 2*Math.PI;

            int dist = Math.round(distFactor*sightRange) - 1; // <= 1000
            int intRight = Math.round(angleFactor*right); // <= 1000
            int intLeft = Math.round(angleFactor*left); // <= 1000


            int encodeRight = (intRight << 20) + (dist << 10) + (index << 1);
            int encodeLeft = (intLeft << 20) + (dist << 10) + (index << 1) + 1;

            intervals[index] = encodeRight;
            ++index;
            intervals[index] = encodeLeft;
            ++index;

            if (left < right){
                int encoded = (encodeRight&0xFFFFF);
                openObstacles.add(encoded);
                float newDist = (float)(encoded >> 10)/distFactor;
                if (newDist < distMin) distMin = newDist;
            }
        }

        distToMax = Math.abs(ArchonsVisionPlayer.Map.minY - myPos.y);
        if (distToMax < sightRange){
            float ang = (float)Math.acos(distToMax/sightRange);
            float right = baseDir.radiansBetween(Direction.getSouth().rotateRightRads(ang));
            float left = baseDir.radiansBetween(Direction.getSouth().rotateLeftRads(ang));

            if (right < 0) right += 2*Math.PI;
            if (left < 0) left += 2*Math.PI;

            int dist = Math.round(distFactor*sightRange) - 1;// <= 1000
            int intRight = Math.round(angleFactor*right); // <= 1000
            int intLeft = Math.round(angleFactor*left); // <= 1000


            int encodeRight = (intRight << 20) + (dist << 10) + (index << 1);
            int encodeLeft = (intLeft << 20) + (dist << 10) + (index << 1) + 1;

            intervals[index] = encodeRight;
            ++index;
            intervals[index] = encodeLeft;
            ++index;

            if (left < right){
                int encoded = (encodeRight&0xFFFFF);
                openObstacles.add(encoded);
                float newDist = (float)(encoded >> 10)/distFactor;
                if (newDist < distMin) distMin = newDist;
            }
        }

        distToMax = Math.abs(Map.maxY - myPos.y);
        if (distToMax < sightRange){
            float ang = (float)Math.acos(distToMax/sightRange);
            float right = baseDir.radiansBetween(Direction.getNorth().rotateRightRads(ang));
            float left = baseDir.radiansBetween(Direction.getNorth().rotateLeftRads(ang));

            if (right < 0) right += 2*Math.PI;
            if (left < 0) left += 2*Math.PI;

            int dist = Math.round(distFactor*sightRange) - 1; // <= 1000
            int intRight = Math.round(angleFactor*right); // <= 1000
            int intLeft = Math.round(angleFactor*left); // <= 1000


            int encodeRight = (intRight << 20) + (dist << 10) + (index << 1);
            int encodeLeft = (intLeft << 20) + (dist << 10) + (index << 1) + 1;

            intervals[index] = encodeRight;
            ++index;
            intervals[index] = encodeLeft;
            ++index;

            if (left < right){
                int encoded = (encodeRight&0xFFFFF);
                openObstacles.add(encoded);
                float newDist = (float)(encoded >> 10)/distFactor;
                if (newDist < distMin) distMin = newDist;
            }
        }
    }

    static float computeSightRange(RobotController rc, MapLocation loc, float radius){
        baseDir = null;
        index = 0;
        openObstacles = new TreeSet<>();
        currentAngle = 0;
        gradientX = 0;gradientY = 0;

        getTrees(rc, loc, rc.getType().bodyRadius, radius);

        distMin = sightRange;


        RobotInfo[] Ri = rc.senseNearbyRobots(loc, sightRange, null);

        intervals = new int[2*Ti.length + 2*Ri.length + 8];


        for (TreeInfo ti : Ti){
            encodeObstacle(loc, ti.getLocation(), ti.getRadius());
        }

        for (RobotInfo ri: Ri){
            if (ri.getID() == rc.getID()) continue;
            if (ri.getType() != RobotType.ARCHON) continue;
            encodeObstacle(loc, ri.getLocation(), ri.getType().bodyRadius);
        }

        if (baseDir == null) baseDir = Direction.getNorth();

        addMapBounds(rc);

        System.out.println(distMin);

        intervals = Arrays.copyOf(intervals, index);


        if (intervals.length > 0) quickSort(0, intervals.length - 1);

        float Area = 0;

        MapLocation prevPoint = loc.add(baseDir, distMin);

        for (int i = 0; i < intervals.length; ++i){
            int a = intervals[i];
            rc.setIndicatorDot(loc.add(baseDir.rotateLeftRads(currentAngle), distMin), 0, 0, 255);

            if ((a&1) == 0) { //RIGHT
                int encoded = (a&0xFFFFF);
                openObstacles.add(encoded);
                float newDist = (float)(encoded >> 10)/distFactor;
                float angle = (float)(a >> 20)/angleFactor;
                if (distMin == sightRange) Area += (angle - currentAngle)*distMin*distMin;
                else{
                    Area += distMin*distMin*(float)Math.sin((angle - currentAngle));
                    updateGradient(angle, rc);
                }

                rc.setIndicatorLine(loc.add(baseDir.rotateLeftRads(angle), distMin), prevPoint, 0, 0, 255);
                currentAngle = angle;
                if (newDist < distMin){
                    distMin = newDist;
                }
                prevPoint = loc.add(baseDir.rotateLeftRads(currentAngle), distMin);
            }
            else{ //LEFT!!
                int encoded = ((a-1)&0xFFFFF);
                if (openObstacles.contains(encoded)){
                    openObstacles.remove(encoded);
                }
                float angle = (float)(a >> 20)/angleFactor;
                float newDist = (float)(encoded >> 10)/distFactor;
                if (distMin == sightRange) Area += (angle - currentAngle)*distMin*distMin;
                else{
                    Area += distMin*distMin*(float)Math.sin((angle - currentAngle));
                    updateGradient(angle, rc);
                }
                rc.setIndicatorLine(loc.add(baseDir.rotateLeftRads(angle), distMin), prevPoint, 0, 0, 255);
                currentAngle = angle;
                if (Math.abs(newDist - distMin) < Constants.eps) {
                    distMin = sightRange;
                    if (openObstacles.size() > 0) {
                        encoded = openObstacles.first();
                        distMin = (float) (encoded >> 10) / distFactor;
                        if (distMin >= sightRange) distMin = sightRange - Constants.eps;
                    }
                }
                prevPoint = loc.add(baseDir.rotateLeftRads(currentAngle), distMin);
            }
            rc.setIndicatorDot(loc.add(baseDir.rotateLeftRads(currentAngle), distMin), 0, 0, 255);
        }

        float angle = 2.0f*(float)Math.PI;
        if (distMin == sightRange){
            Area += (angle - currentAngle)*distMin*distMin;
        }
        else{
            Area += distMin*distMin*(float)Math.sin((angle - currentAngle));
            updateGradient(angle, rc);
        }


        return Area;
    }


    static void quickSort(int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;
        //System.out.println("INDEX: "+ higherIndex + " " + lowerIndex);
        int pivot = intervals[(higherIndex+lowerIndex)/2];
        while (i <= j) {

            while (intervals[i] < pivot) {
                i++;
            }
            while (intervals[j] > pivot) {
                j--;
            }
            if (i <= j) {
                int temp = intervals[i];
                intervals[i] = intervals[j];
                intervals[j] = temp;
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j) {
            quickSort(lowerIndex, j);
        }
        if (i < higherIndex){
            quickSort(i, higherIndex);
        }
    }

    static void updateGradient(float angle, RobotController rc){
        MapLocation loc = rc.getLocation();
        MapLocation point1 = loc.add(baseDir.rotateLeftRads(currentAngle), distMin), point2 = loc.add(baseDir.rotateLeftRads(angle), distMin);
        float dist = point1.distanceTo(point2);
        Direction dirGradient = baseDir.rotateLeftRads((currentAngle + angle)/2).opposite();
        gradientX += dirGradient.getDeltaX(dist);
        gradientY += dirGradient.getDeltaY(dist);
        rc.setIndicatorLine(loc, loc.add(dirGradient, dist), 255, 0, 0);
    }


}
