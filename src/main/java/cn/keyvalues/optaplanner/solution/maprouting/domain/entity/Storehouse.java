package cn.keyvalues.optaplanner.solution.maprouting.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author generator v3.5.3.1
 * @since 2023-10-09
 */
@Getter
@Setter
@Schema(name = "Storehouse", description = "$!{table.comment}")
public class Storehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    private String coordinate;

    private String address;

    private String citycode;
}
