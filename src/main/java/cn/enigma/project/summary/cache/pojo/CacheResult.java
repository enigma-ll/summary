package cn.enigma.project.summary.cache.pojo;

import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
public class CacheResult<T> {
    private T result;
    private Exception exception;

    private boolean hasException() {
        return null != this.exception;
    }

    public T result() throws Exception {
        if (hasException()) {
            throw this.exception;
        }
        return this.result;
    }
}
