package ultima6;

public class Clock implements Runnable {

    public static final int TICKS_PER_MINUTE = 4;
    public static final int NUM_TIMERS = 16;

    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    private int dayOfWeek;
    private int moveCounter; // player steps taken since start
    private int timeCounter; // game minutes
    private final int[] timers = new int[NUM_TIMERS];
    private int numTimers;
    private int restCounter; //hours until the party will heal again while resting.

    @Override
    public void run() {
        incMinute(1);
    }

    public int getMinute() {
        return minute;
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void incMoveCounter() {
        moveCounter++;
    }

    public void incMoveCounterByMinute() {
        moveCounter += TICKS_PER_MINUTE;
    }

    public void incMinute(int amount) {
        minute += amount;

        if (minute >= 60) {
            for (; minute >= 60; minute -= 60) {
                inc_hour();
            }
            timeCounter += minute;
        } else {
            timeCounter += amount;
        }
    }

    private void inc_hour() {
        if (restCounter > 0) {
            restCounter--;
        }

        if (hour == 23) {
            hour = 0;
            inc_day();
        } else {
            hour++;
            timeCounter += 60;
        }

        //update_moongates
    }

    private void inc_day() {
        if (day == 28) {
            day = 1;
            inc_month();
        } else {
            day++;
            timeCounter += 1440;
        }
        update_day_of_week();

    }

    private void update_day_of_week() {
        dayOfWeek = day % 7;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
    }

    private void inc_month() {
        if (month == 12) {
            month = 1;
            inc_year();
        } else {
            month++;
            timeCounter += 40320;
        }
    }

    private void inc_year() {
        year++;
        timeCounter += 483840;
    }

    public int getMoveCount() {
        return moveCounter;
    }

    public String getTimeOfDayString() {
        if (hour < 12) {
            return "morning";
        }

        if (hour >= 12 && hour <= 18) {
            return "afternoon";
        }

        return "evening";
    }

    public String getTimeString() {
        char c;
        int tmp_hour;

        if (hour < 12) {
            c = 'A';
        } else {
            c = 'P';
        }

        if (hour > 12) {
            tmp_hour = hour - 12;
        } else if (hour == 0) {
            tmp_hour = 12;
        } else {
            tmp_hour = hour;
        }

        String ret = String.format("%02d:%02d %cM", tmp_hour, minute, c);

        return ret;
    }

    public void setTimer(int timer_num, int val) {
        if (timer_num < numTimers) {
            timers[timer_num] = val;
        }
    }

    public int getTimer(int timer_num) {
        if (timer_num < numTimers) {
            return timers[timer_num];
        }
        return 0;
    }

    public void updateTimers(int amount) {
        for (int i = 0; i < numTimers; i++) {
            if (timers[i] > amount) {
                timers[i] -= amount;
            } else {
                timers[i] = 0;
            }
        }
    }

}
