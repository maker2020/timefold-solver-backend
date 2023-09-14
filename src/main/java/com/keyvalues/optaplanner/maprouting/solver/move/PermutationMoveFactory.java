package com.keyvalues.optaplanner.maprouting.solver.move;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;

public class PermutationMoveFactory implements MoveListFactory<MapRoutingSolution> {

    @Override
    public List<? extends Move<MapRoutingSolution>> createMoveList(MapRoutingSolution solution) {
        List<RoutingEntity> entityList = solution.getRouting();
        List<Move<MapRoutingSolution>> moveList = new ArrayList<>();

        // 生成所有可能的排列移动
        for (int i = 0; i < entityList.size() - 1; i++) {
            for (int j = i + 1; j < entityList.size(); j++) {
                moveList.add(new SwapEntitiesMove(entityList.get(i), entityList.get(j)));
            }
        }
        
        return moveList;
    }
}
