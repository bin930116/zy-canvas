package vip.xiaonuo.canvas.modular.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * 用户-技能关联实体
 */
@Getter
@Setter
@TableName("zy_user_skill")
public class ZyUserSkill {

    @TableId
    private String id;

    private String userId;

    private String skillId;

    private Integer isAdded;

    private Integer isLiked;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
}
