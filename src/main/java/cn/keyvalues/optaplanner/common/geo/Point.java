package cn.keyvalues.optaplanner.common.geo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.io.Serializable;

@Data
public class Point implements Serializable {

    private static final long serialVersionUID = 7457963026513014856L;

    public double longitude = 0.0;
    public double latitude = 0.0;

    @JsonIgnore
    private PointType type = PointType.UNKNOWN;

    public Point() {
    }

    public Point(double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
            throw new IllegalArgumentException("The supplied coordinates " + this + " are out of range.");
        }
    }

    public Point(double longitude, double latitude, PointType type) {
        this(longitude, latitude);
        this.type = type;
    }

    @Override
    public String toString() {
        return "(" + longitude + "," + latitude + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point other = (Point) obj;
            return Double.compare(latitude, other.latitude) == 0 && Double.compare(longitude, other.longitude) == 0
//                && type == other.getType()
                ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 42;
        long latBits = Double.doubleToLongBits(latitude);
        long lonBits = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (latBits ^ (latBits >>> 32));
        result = 31 * result + (int) (lonBits ^ (lonBits >>> 32));
        return result;
    }
}
