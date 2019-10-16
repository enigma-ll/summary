package cn.enigma.project.summary.cache.pojo;

import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
public class CacheResult<T> {
    private T result;
    private Exception exception;

    public boolean hasException() {
        return null != this.exception;
    }

    public T result() throws Exception {
        if (hasException()) {
            throw this.exception;
        }
        if (null == this.result) {
            throw new NullPointerException(null);
        }
        return this.result;
    }

    public Exception throwException() throws Exception {
        throw this.exception;
    }

    public boolean hasResult() {
        return null != this.result;
    }
}
