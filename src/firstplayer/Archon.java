package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {
    @SuppressWarnings("unused")

    static RobotController rc;

    public static void run(RobotController rcc) {

        rc = rcc;
        int currentRound = 10000;

        while (true) {
            //code executed continually, don't let it end

            if (rc.getRoundNum() < currentRound){
                currentRound = rc.getRoundNum();
                try {
                    rc.broadcast(Util.MAX_BROADCAST_MESSAGE, 0);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            construct();

            /*int a = 1;
            for (int i = 0; i < 3; ++i){
                a *= 2;
                a %= a+1;
            }

            System.out.println(Clock.getBytecodeNum());*/

            Clock.yield();
        }
    }

    static void construct(){
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canHireGardener(Util.main_dirs[i])){
                    rc.hireGardener(Util.main_dirs[i]);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
