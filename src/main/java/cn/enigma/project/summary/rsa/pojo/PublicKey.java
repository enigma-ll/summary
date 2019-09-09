package cn.enigma.project.summary.rsa.pojo;

import lombok.Data;

import java.security.PublicKey;

/**
 * @author luzh
 * Create: 2019-07-12 11:46
 * Modified By:
 * Description:
 */
@Data
public class PublicKey {
    private String id;
    private java.security.PublicKey publicKey;

    public PublicKey(String id, java.security.PublicKey publicKey) {
        this.id = id;
        this.publicKey = publicKey;
    }
}
