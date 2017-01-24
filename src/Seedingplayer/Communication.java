package Seedingplayer;


import battlecode.common.RobotController;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Communication {

    /*Types of message*/
    static final int ENEMYCHANNEL = 1000;
    static final int STOPCHANNEL = 1100;
    static final int ENEMYGARDENERCHANNEL = 1200;
    static final int CHOPCHANNEL = 1300;
    static final int ENEMYTREECHANNEL = 1400;
    static final int EMERGENCYCHANNEL = 1500;
    static final int TREEWITHGOODIES = 1600;
    static final int PLANTTREECHANNEL = 1700;

    static final int CYCLIC_CHANNEL_LENGTH = 99;



    //CANALS GARDENERS

    //teoricament ocupo del 600 al 656 pero no estic segur
    static final int ZONE_FIRST_POSITION = 600;
    static final int ROBOTS_BUILT = 695;
    static final int TREES_BUILT = 696;

    static int ZONE_ORIGIN_X = 697;//coordenades del centre de la zona (0,0)
    static int ZONE_ORIGIN_Y = 698;//son les coordenades on spawneja el primer gardener

    static int MIN_ZONE_X = 691;//tenen els valors de les zones desfasats +-20 per tal que el 0 mai caigui entre minim i maxim
    static int MIN_ZONE_Y = 692;//osigui si la x de zona maxima es 6, aqui es guardara un -14
    static int MAX_ZONE_X = 693;//i si la de zona minima es -3 aqui es guardara un 17
    static int MAX_ZONE_Y = 694;
    static int ZONE_LIMIT_OFFSET = 20;

    static final int MAP_UPPER_BOUND = 700; // valors amb precisio de Constants.PRECISION_MAP_BOUNDS de
    static final int MAP_LOWER_BOUND = 701; // les coordenades x o y respecte l'archon 0 dels limits del mapa
    static final int MAP_LEFT_BOUND = 702; // son floats, s'obtenen a partir de l'int amb la funcio Float.intBitsToFloat
    static final int MAP_RIGHT_BOUND = 703;

    static final int SIGHT_ZONES = 704; // bits que indiquen si cada zona de visio ha estat explorada


    static int xBase = 9999, yBase = 9999;


    //BC parameters
    //gardener lumberjack soldier tank scout trees
    static final int[] unitChannels = {501, 502, 503, 504, 505, 506};
    static final int INITIALIZED = 507;

    static final int ARCHONTURN = 508;
    static final int ARCHONNUMBER = 509;

    static final int MAX_BROADCAST_MESSAGE = 500;

    static final int typeMask = 0xF0000000; //at most 15
    static final int iOffMask = 0x0FF00000; //at most 255
    static final int jOffMask = 0x000FF000; //at most 255
    static final int iOffShift = 20;
    static final int jOffShift = 12;
    static final int valueMask = 0x00000FFF; // at most 4095


    static void setBase(int x, int y){
        xBase = x;
        yBase = y;
    }

    public static int encodeFinding(int type, int iOffset, int jOffset, int value) {
        int ret = type |
                (((iOffset + 127) & 0xFF) << iOffShift) | /*-100 <= iOffset <= 100*/
                (((jOffset + 127) & 0xFF) << jOffShift) |
                (value & 0xFFF);
        return ret;
    }

    public static int encodeFinding(int type, int iOffset, int jOffset) {
        return encodeFinding(type, iOffset, jOffset, 0);
    }

    public static int[] decode(int bitmap) {
        int[] ret = new int[4];
        ret[0] = bitmap & typeMask;
        ret[1] = ((bitmap & iOffMask) >> iOffShift) - 127 + xBase;
        ret[2] = ((bitmap & jOffMask) >> jOffShift) - 127 + yBase;
        ret[3] = (bitmap & valueMask);
        return ret;
    }

    public static void sendMessage(RobotController rc, int channel, int x, int y, int value) {
        try {
            if (xBase == 9999){
                System.out.println("NO POTS ENVIAR UN MISSATGE SENSE HAVER POSAT EL XBASE I EL YBASE!!!!!!!!!!!!!!!!!!!!!!");
            }
            int lastMessage = rc.readBroadcast(channel + CYCLIC_CHANNEL_LENGTH);
            int message = encodeFinding(0, x-xBase, y-yBase, value);
            rc.broadcast(channel + lastMessage, message);
            rc.broadcast(channel + CYCLIC_CHANNEL_LENGTH, (lastMessage + 1) % CYCLIC_CHANNEL_LENGTH);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
