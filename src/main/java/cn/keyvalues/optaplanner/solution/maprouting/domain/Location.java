package cn.keyvalues.optaplanner.solution.maprouting.domain;

import cn.keyvalues.optaplanner.common.enums.TacticsEnum;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.geo.Point;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class Location extends AbstractPersistable{
    
    protected String name=null;
    protected Point point;
    
    @Schema(description = "地图API规划策略(上一个点到这里)")
    protected TacticsEnum tactics=TacticsEnum.TWO;

    public Location(long id){
        super(id);
    }

    public Location(long id, Point point) {
        super(id);
        this.point=point;
    }

}
