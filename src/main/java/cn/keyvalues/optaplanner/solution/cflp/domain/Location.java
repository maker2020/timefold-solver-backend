package cn.keyvalues.optaplanner.solution.cflp.domain;

import cn.keyvalues.optaplanner.common.geo.Point;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
@Schema(description = "位置类")
public class Location extends AbstractPersistable{

    // Approximate Metric Equivalents for Degrees. At the equator for longitude and for latitude anywhere,
    // the following approximations are valid: 1° = 111 km (or 60 nautical miles) 0.1° = 11.1 km.
    
    public static final double METERS_PER_DEGREE = 111_000;
    
    @Schema(description = "位置名称")
    protected String name=null;
    @Schema(description = "坐标点")
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
        return (long) Math.ceil(Math.sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff) * METERS_PER_DEGREE);
        // return (long)Math.sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff);
    }

}
