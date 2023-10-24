package cn.keyvalues.optaplanner.solution.cflp.domain;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

@ConstraintConfiguration
public class FacilityLocationConstraintConfig {

    public static final String FACILITY_CAPACITY = "facility capacity";
    public static final String FACILITY_SETUP_COST = "facility setup cost";
    public static final String DISTANCE_FROM_FACILITY = "distance from facility";
    public static final String SERVER_RADIUS = "server radius";
    public static final String UNIQUE_ENTITY="unique entity";
    public static final String GREEDY_DEMAND="greedy demand";
    public static final String LESS_STATION="less station";
    public static final String NO_REST_DEMAND="no rest demand";
    public static final String NO_OVER_DEMAND="no over demand";

    /**
     * 不超出服务站容量
     */
    @ConstraintWeight(FACILITY_CAPACITY)
    HardMediumSoftLongScore facilityCapacity = HardMediumSoftLongScore.ofHard(1);

    /**
     * 不过度分配
     */
    @ConstraintWeight(NO_OVER_DEMAND)
    HardMediumSoftLongScore noOverDemand=HardMediumSoftLongScore.ofSoft(1);

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
    HardMediumSoftLongScore greedyDemand=HardMediumSoftLongScore.ofSoft(1);

    /**
     * 服务站数量尽可能少
     */
    @ConstraintWeight(LESS_STATION)
    HardMediumSoftLongScore lessStation=HardMediumSoftLongScore.ofSoft(1);

    /**
     * 不剩余需求
     */
    @ConstraintWeight(NO_REST_DEMAND)
    HardMediumSoftLongScore noRestDemand=HardMediumSoftLongScore.ofHard(1);



    /************ 预留扩展 ************/


    /**
    * 设施修建费
    */
    @ConstraintWeight(FACILITY_SETUP_COST)
    HardMediumSoftLongScore facilitySetupCost = HardMediumSoftLongScore.ofSoft(2);

    /**
     * 离服务站
     */
    @ConstraintWeight(DISTANCE_FROM_FACILITY)
    HardMediumSoftLongScore distanceFromFacility = HardMediumSoftLongScore.ofSoft(5);
}