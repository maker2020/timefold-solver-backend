package cn.keyvalues.optaplanner.maprouting.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@PlanningEntity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Visitor extends AbstractPersistable{

    private VisitorBase base;

    /**
     * 默认不会重复
     */
    @Schema(hidden = true)
    @PlanningListVariable(valueRangeProviderRefs = "customerList")
    protected List<Customer> customers=new ArrayList<>();

    public Visitor(Long id,VisitorBase base){
        this.id=id;
        this.base=base;
    }

}
