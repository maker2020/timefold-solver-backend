package cn.keyvalues.optaplanner.solution.cflp.domain;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfiguration;
import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

/**
 * 目标函数的系数(或者说 约束的权重) 可以由用户自己去配置，返回一个约束列表。
 */
@ConstraintConfiguration
public class FacilityLocationConstraintConfig {

    public static final String FACILITY_CAPACITY = "容量";
    // public static final String FACILITY_SETUP_COST = "facility setup cost";
    public static final String DISTANCE_FROM_FACILITY = "尽可能分配距离更近的";
    public static final String SERVER_RADIUS = "服务站半径";
    public static final String UNIQUE_ENTITY="唯一分配";
    public static final String GREEDY_DEMAND="尽可能多的分配需求";
    public static final String LESS_STATION="更少的服务站";
    public static final String NO_REST_DEMAND="不剩余需求";
    public static final String NO_OVER_DEMAND="不额外分配";
    
    public static final String MATCH_LEVEL="等级匹配";

    /**
     * 不超出服务站容量
     */
    @ConstraintWeight(FACILITY_CAPACITY)
    HardMediumSoftLongScore facilityCapacity = HardMediumSoftLongScore.ofHard(1);

    /**
     * 不过度分配
     */
    @ConstraintWeight(NO_OVER_DEMAND)
    HardMediumSoftLongScore noOverDemand=HardMediumSoftLongScore.ofMedium(2);

    /**
     * 服务半径
     */
    @ConstraintWeight(SERVER_RADIUS)
    HardMediumSoftLongScore serverRadius = HardMediumSoftLongScore.ofHard(1);

    /**
     * 不重复分配
     */
    @ConstraintWeight(UNIQUE_ENTITY)
    HardMediumSoftLongScore uniqueEntity = HardMediumSoftLongScore.ofHard(2);

    /**
     * 贪婪消耗需求
     */
    @ConstraintWeight(GREEDY_DEMAND)
    HardMediumSoftLongScore greedyDemand=HardMediumSoftLongScore.ofMedium(1);

    /**
     * 服务站数量尽可能少
     */
    @ConstraintWeight(LESS_STATION)
    HardMediumSoftLongScore lessStation=HardMediumSoftLongScore.ofMedium(1);

    /**
     * 不剩余需求
     */
    @ConstraintWeight(NO_REST_DEMAND)
    HardMediumSoftLongScore noRestDemand=HardMediumSoftLongScore.ofMedium(1);

    /**
     * 等级要匹配
     */
    @ConstraintWeight(MATCH_LEVEL)
    HardMediumSoftLongScore matchLevel=HardMediumSoftLongScore.ofHard(1);

    /**
     * 离服务站
     */
    @ConstraintWeight(DISTANCE_FROM_FACILITY)
    HardMediumSoftLongScore distanceFromFacility = HardMediumSoftLongScore.ofSoft(1);

    /************ 预留扩展 ************/


    /**
    * 设施修建费
    */
    // @ConstraintWeight(FACILITY_SETUP_COST)
    // HardMediumSoftLongScore facilitySetupCost = HardMediumSoftLongScore.ofSoft(2);

}