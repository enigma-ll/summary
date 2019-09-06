package cn.enigma.project.summary.test.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:33
 * Modified By:
 * Description:
 */
@Data
@Entity
@Table(name = "TEST_INFO")
public class TestEntity implements Serializable {
    private static final long serialVersionUID = 428896576368292316L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    public TestEntity(String columnOne, String columnTwo, String columnThree, String columnFour, String columnFive, String columnSix) {
        this.columnOne = columnOne;
        this.columnTwo = columnTwo;
        this.columnThree = columnThree;
        this.columnFour = columnFour;
        this.columnFive = columnFive;
        this.columnSix = columnSix;
    }
}
