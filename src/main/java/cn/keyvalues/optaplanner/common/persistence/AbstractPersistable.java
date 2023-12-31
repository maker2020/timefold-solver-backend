package cn.keyvalues.optaplanner.common.persistence;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class AbstractPersistable {

    @Schema(hidden = true)
    // 用Long，fastjson拆箱会报错
    protected long id;

    protected AbstractPersistable() {
    }

    protected AbstractPersistable(long id) {
        this.id = id;
    }

    @PlanningId
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

}
