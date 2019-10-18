package cn.enigma.project.summary.task;

import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
class CacheResult<T> {
    private T result;
    private Exception exception;

    private boolean hasException() {
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

    public void throwException() throws Exception {
        if (hasException()) {
            throw this.exception;
        }
    }

    public boolean hasResult() {
        return null != this.result;
    }
}
