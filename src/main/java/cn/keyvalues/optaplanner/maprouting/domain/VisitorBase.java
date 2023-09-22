package cn.keyvalues.optaplanner.maprouting.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.common.persistence.jackson.JacksonUniqueIdGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class VisitorBase extends AbstractPersistable{
    
    protected Location location;

    public VisitorBase(long id,Location location){
        super(id);
        this.location=location;
    }

}
