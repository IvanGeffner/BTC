package Vells.scoutplayer;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Communication {

    /*Types of message*/

    static final int STOP = 0x10000000; //tells ally soldier to stop moving (collision)
    static final int PLANTTREE = 0x20000000; //Neutral tree found!
    static final int ENEMY = 0x30000000;
    static final int ENEMYTREE = 0x40000000;
    static final int BULLETTREE = 0x50000000;
    static final int UNITTREE = 0x60000000;
    static final int TREEZONE = 0x70000000;


    //BC parameters

    static final int MAX_BROADCAST_MESSAGE = 500;

    static final int[] unitChannels = {501, 502, 503, 504, 505, 506};
    static final int INITIALIZED = 507;

    static final int ARCHONTURN = 508;
    static final int ARCHONNUMBER = 509;


    static final int typeMask = 0xF0000000; //at most 15
    static final int iOffMask = 0x0FF00000; //at most 255
    static final int jOffMask = 0x000FF000; //at most 255
    static final int iOffShift = 20;
    static final int jOffShift = 12;
    static final int valueMask = 0x00000FFF; // at most 4095



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
        ret[1] = ((bitmap & iOffMask) >> iOffShift) - 127;
        ret[2] = ((bitmap & jOffMask) >> jOffShift) - 127;
        ret[3] = (bitmap & valueMask);
        return ret;
    }


    static final int MAP_UPPER_BOUND = 700;
    static final int MAP_LOWER_BOUND = 701;
    static final int MAP_LEFT_BOUND = 702;
    static final int MAP_RIGHT_BOUND = 703;

    static final int SIGHT_ZONES = 704; // el seguent lliure es el 718

}
