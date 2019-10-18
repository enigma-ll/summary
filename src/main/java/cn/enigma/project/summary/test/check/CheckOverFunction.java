package cn.enigma.project.summary.test.check;

import cn.enigma.project.summary.cache.pojo.CacheResult;

/**
 * @author luzh
 * Create: 2019-10-18 12:08
 * Modified By:
 * Description:
 */
public interface CheckOverFunction<T> {

    void apply(CheckBean checkBean, CacheResult<T> result) throws Exception;
}
