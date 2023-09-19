package com.keyvalues.optaplanner.common.enums;

/**
 * <p>0：默认</p>
 * <p>2：距离最短（只返回一条路线，不考虑限行和路况，距离最短且稳定，用于估价场景）</p>
 * <p>3：不走高速</p>
 * <p>4：高速优先</p>
 * <p>5：躲避拥堵</p>
 * <p>6：少收费</p>
 * <p>7: 躲避拥堵 & 高速优先</p>
 * <p>8: 躲避拥堵 & 不走高速</p>
 * <p>9: 躲避拥堵 & 少收费</p>
 * <p>10: 躲避拥堵 & 不走高速 & 少收费</p>
 * <p>11: 不走高速 & 少收费</p>
 * <p>12: 距离优先（考虑限行和路况，距离相对短且不一定稳定）</p>
 * <p>13：时间优先</p>
 */
public enum TacticsEnum {

    ZERO(0),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    ELEVEN(11),
    TWELVE(12),
    THIRTEEN(13);
    
    private final Integer value;

    TacticsEnum(Integer value){
        this.value=value;
    }

    public Integer getValue() {
        return value;
    }

}
