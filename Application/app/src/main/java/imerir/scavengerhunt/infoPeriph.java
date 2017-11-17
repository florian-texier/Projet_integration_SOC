package imerir.scavengerhunt;

/**
 * Created by texfl on 16/11/2017.
 */

public class infoPeriph {
    private static final infoPeriph ourInstance = new infoPeriph();

    private double distance = 50.0;

    public static infoPeriph getOurInstance() {
        return ourInstance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public static infoPeriph getInstance() {
        return ourInstance;
    }

    private infoPeriph() {
    }
}
