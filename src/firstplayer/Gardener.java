package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Gardener {

    static RobotController rc;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;

        while (true) {
            //code executed continually, don't let it end

            construct();

            Clock.yield();
        }
    }

    static void construct(){
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canBuildRobot(RobotType.SOLDIER, Util.main_dirs[i])){
                        rc.buildRobot(RobotType.SOLDIER, Util.main_dirs[i]);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}