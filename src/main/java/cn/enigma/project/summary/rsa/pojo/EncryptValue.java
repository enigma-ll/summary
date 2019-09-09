package cn.enigma.project.summary.rsa.pojo;

import lombok.Data;

/**
 * @author luzh
 * Create: 2019-07-12 14:23
 * Modified By:
 * Description:
 */
@Data
public class EncryptValue {
    private String id;
    private String value;

    public EncryptValue(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
