package com.keyvalues.optaplanner.maprouting.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import com.keyvalues.optaplanner.common.persistence.jackson.JacksonUniqueIdGenerator;

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
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Visitor extends AbstractPersistable{

    private VisitorBase base;

    /**
     * 默认不会重复
     */
    @PlanningListVariable(valueRangeProviderRefs = "customerList")
    protected List<Customer> customers=new ArrayList<>();

    public Visitor(Long id,VisitorBase base){
        this.id=id;
        this.base=base;
    }

}
