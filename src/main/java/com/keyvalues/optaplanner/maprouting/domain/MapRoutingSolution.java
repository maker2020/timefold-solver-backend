package com.keyvalues.optaplanner.maprouting.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import com.keyvalues.optaplanner.geo.Point;

import lombok.Data;
import lombok.ToString;

@PlanningSolution
@Data
@ToString
public class MapRoutingSolution {

    private String id;

    /**
     * 地图标出的点
     */
    // @ProblemFactCollectionProperty
    // @ValueRangeProvider(id = "pointRange")
    private List<Point> pointList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "orderRange")
    private List<Integer> orderRange;

    /**
     * 注意这个visitor当前假设都是同一出发点被派往不同地方送货
     */
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "visitorList")
    private List<String> visitor;

    @PlanningEntityCollectionProperty
    private List<RoutingEntity> routing;

    @PlanningScore
    private HardSoftScore score;

}
