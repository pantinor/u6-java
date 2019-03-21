package ultima6;

public class Schedule {

    public static enum WorkType {
        MOTIONLESS(0x0),
        IN_PARTY(0x1),
        PLAYER(0x2),
        COMBAT_COMMAND(0x2),
        COMBAT_FRONT(0x3),
        COMBAT_REAR(0x4),
        COMBAT_FLANK(0x5),
        COMBAT_BERSERK(0x6),
        COMBAT_RETREAT(0x7),
        COMBAT_ASSAULT(0x8),
        COMBAT_FLEE(0x9),
        COMBAT_LIKE(0xa),
        COMBAT_UNFRIENDLY(0xb),
        ANIMAL_WANDER(0xc),
        TANGLE_VINE(0xd),
        IMMOBILE_ATTACK(0xe),
        GUARD_WALK_EAST_WEST(0xf),
        GUARD_WALK_NORTH_SOUTH(0x10),
        LOOKOUT(0x11),
        WALK_TO_LOCATION(0x86),
        FACE_NORTH(0x87),
        FACE_EAST(0x88),
        FACE_SOUTH(0x89),
        FACE_WEST(0x8a),
        WALK_NORTH_SOUTH(0x8b),
        WALK_SOUTH_NORTH(0x8d),
        WALK_EAST_WEST(0x8c),
        WALK_WEST_EAST(0x8e),
        WANDER_AROUND(0x8f),
        WORK_WANDER(0x90),
        SLEEP(0x91),
        WORK_STILL(0x92),
        EATING(0x93),
        TEND_CROPS(0x94),
        PLAY_LUTE(0x95),
        BEG(0x96),
        BELL_RINGER(0x98),
        FIGHTING(0x99),
        MOUSE_SHERRY(0x9a),
        ATTACK_PARTY(0x9b);

        private final int wt;

        private WorkType(int wt) {
            this.wt = wt;
        }

        public int type() {
            return this.wt;
        }

        public static String getName(int wt) {
            for (WorkType obj : WorkType.values()) {
                if (obj.wt == wt) {
                    return obj.toString();
                }
            }
            return null;
        }

    }

    private int hour = 0;
    private int day_of_week = 0;
    private int worktype = 0;
    private int x = 0;
    private int y = 0;
    private int z = 0;

    public Schedule() {
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDayOfWeek() {
        return day_of_week;
    }

    public void setDayOfWeek(int day_of_week) {
        this.day_of_week = day_of_week;
    }

    public int getWorktype() {
        return worktype;
    }

    public void setWorktype(int worktype) {
        this.worktype = worktype;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Schedule{" + "hour=" + hour + ", day_of_week=" + day_of_week + ", worktype=" + WorkType.getName(worktype) + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }

}
