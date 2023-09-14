package com.keyvalues.optaplanner.maprouting.optional;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;


public class SwapEntitiesMove extends AbstractMove<MapRoutingSolution> {
    private RoutingEntity entity1;
    private RoutingEntity entity2;

    public SwapEntitiesMove(RoutingEntity entity1, RoutingEntity entity2) {
        this.entity1 = entity1;
        this.entity2 = entity2;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<MapRoutingSolution> scoreDirector) {
        // 在这里添加任何必要的检查以确保移动是可行的
        // 例如，确保交换不违反硬约束
        return true; // 根据你的需求进行适当的检查
    }

    @Override
    public AbstractMove<MapRoutingSolution> createUndoMove(ScoreDirector<MapRoutingSolution> scoreDirector) {
        // 创建一个撤销移动，以便可以撤销这个移动
        return new SwapEntitiesMove(entity2, entity1);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<MapRoutingSolution> scoreDirector) {
        // 执行移动，交换两个实体的位置
        int sequence1 = entity1.getOrder();
        int sequence2 = entity2.getOrder();
        entity1.setOrder(sequence2);
        entity2.setOrder(sequence1);
        scoreDirector.beforeVariableChanged(entity1, "order");
        scoreDirector.beforeVariableChanged(entity2, "order");
        // 在这里添加任何必要的操作，以更新评分
        scoreDirector.afterVariableChanged(entity1, "order");
        scoreDirector.afterVariableChanged(entity2, "order");
    }

    @Override
    public String toString() {
        return "SwapEntitiesMove{" +
                "entity1=" + entity1 +
                ", entity2=" + entity2 +
                '}';
    }
}
