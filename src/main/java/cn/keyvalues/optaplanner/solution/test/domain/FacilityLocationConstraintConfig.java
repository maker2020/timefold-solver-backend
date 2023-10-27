package cn.keyvalues.optaplanner.solution.test.domain;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfiguration;
import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@ConstraintConfiguration
public class FacilityLocationConstraintConfig {

    public static final String FACILITY_CAPACITY = "facility capacity";
    public static final String FACILITY_SETUP_COST = "facility setup cost";
    public static final String DISTANCE_FROM_FACILITY = "distance from facility";
    public static final String SERVER_RADIUS = "server radius";

    /**
     * 容量
     */
    @ConstraintWeight(FACILITY_CAPACITY)
    HardSoftLongScore facilityCapacity = HardSoftLongScore.ofHard(1);

    /**
     * 服务半径
     */
    @ConstraintWeight(SERVER_RADIUS)
    HardSoftLongScore serverRadius = HardSoftLongScore.ofHard(1);

    /**
    * 设施修建费
    */
    @ConstraintWeight(FACILITY_SETUP_COST)
    HardSoftLongScore facilitySetupCost = HardSoftLongScore.ofSoft(2);

    /**
     * 离服务站
     */
    @ConstraintWeight(DISTANCE_FROM_FACILITY)
    HardSoftLongScore distanceFromFacility = HardSoftLongScore.ofSoft(5);
}