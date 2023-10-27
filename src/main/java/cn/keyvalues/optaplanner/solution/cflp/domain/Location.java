package cn.keyvalues.optaplanner.solution.cflp.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.geo.Point;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@PlanningEntity
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Location extends AbstractPersistable{

    // Approximate Metric Equivalents for Degrees. At the equator for longitude and for latitude anywhere,
    // the following approximations are valid: 1° = 111 km (or 60 nautical miles) 0.1° = 11.1 km.
    
    // 注释掉，demo不考虑角度
    // public static final double METERS_PER_DEGREE = 111_000;
    
    protected String name=null;
    protected Point point;

    public Location(long id){
        super(id);
    }

    public Location(long id, Point point) {
        super(id);
        this.point=point;
    }

    public long getDistanceTo(Location other) {
        double latitudeDiff = other.point.latitude - this.point.latitude;
        double longitudeDiff = other.point.longitude - this.point.longitude;
        // return (long) Math.ceil(Math.sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff) * METERS_PER_DEGREE);
        return (long)Math.sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff);
    }

}
