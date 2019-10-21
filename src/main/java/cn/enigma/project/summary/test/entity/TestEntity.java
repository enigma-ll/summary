package cn.enigma.project.summary.test.entity;

import cn.enigma.project.jpa.entity.BaseEntity;
import cn.enigma.project.jpa.query.partial.QueryColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:33
 * Modified By:
 * Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Entity
@Table(name = "test_info")
@DynamicInsert
@DynamicUpdate
public class TestEntity extends BaseEntity {

    private static final long serialVersionUID = -451754529963459226L;

    @QueryColumn
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String age;

    @Column
    private String columnOne;

    @Column
    private String columnTwo;

    @Column
    private String columnThree;

    @Column
    private String columnFour;

    @Column
    private String columnFive;

    @Column
    private String columnSix;

    public TestEntity() {

    }

    public TestEntity(Integer id) {
        this.id = id;
    }

    public TestEntity(String columnOne, String columnTwo, String columnThree, String columnFour, String columnFive, String columnSix) {
        this.columnOne = columnOne;
        this.columnTwo = columnTwo;
        this.columnThree = columnThree;
        this.columnFour = columnFour;
        this.columnFive = columnFive;
        this.columnSix = columnSix;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                '}' + super.toString();
    }
}
