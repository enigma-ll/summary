package cn.enigma.project.summary.jpa.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * @author luzh
 * Create: 2019/9/9 上午11:01
 * Modified By:
 * Description: 抽离一个公共类，包含创建时间和修改时间，@MappedSuperclass表示是一个公共类而不是一个实体。
 * AuditingEntityListener开启后@CreatedDate和@LastModifiedDate这俩时间标签才会生效
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = -6196398819600840891L;

    @Setter(value = AccessLevel.PROTECTED)
    @CreatedDate
    private Long createTime;

    @LastModifiedDate
    private Long updateTime;
}
