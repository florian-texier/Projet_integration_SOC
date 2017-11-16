package imerir.scavengerhunt;

/**
 * Created by texfl on 16/11/2017.
 */

public class infDistance {
    private static final infDistance ourInstance = new infDistance();

    private double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public static infDistance getInstance() {
        return ourInstance;
    }

    private infDistance() {
    }
}
