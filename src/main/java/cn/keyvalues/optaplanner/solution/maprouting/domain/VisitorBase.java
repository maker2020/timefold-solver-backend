package cn.keyvalues.optaplanner.solution.maprouting.domain;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;

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
public class VisitorBase extends AbstractPersistable{
    
    protected Location location;

    public VisitorBase(long id,Location location){
        super(id);
        this.location=location;
    }

}
