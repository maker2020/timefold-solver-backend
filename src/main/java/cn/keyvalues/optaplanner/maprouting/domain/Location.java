package cn.keyvalues.optaplanner.maprouting.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.common.persistence.jackson.JacksonUniqueIdGenerator;
import cn.keyvalues.optaplanner.geo.Point;

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

    public Location(long id){
        super(id);
    }

    public Location(long id, Point point) {
        super(id);
        this.point=point;
    }

}
