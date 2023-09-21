package com.keyvalues.optaplanner.maprouting.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.keyvalues.optaplanner.common.enums.TacticsEnum;
import com.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import com.keyvalues.optaplanner.common.persistence.jackson.JacksonUniqueIdGenerator;
import com.keyvalues.optaplanner.geo.Point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
@ToString
public class Location extends AbstractPersistable{
    
    protected String name=null;
    protected Point point;
    
    @Schema(description = "地图API规划策略")
    protected TacticsEnum tactics;

    public Location(long id){
        super(id);
    }

    public Location(long id, Point point) {
        super(id);
        this.point=point;
    }

}
