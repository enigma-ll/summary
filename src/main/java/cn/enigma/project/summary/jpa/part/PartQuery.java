package cn.enigma.project.summary.jpa.part;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luzh
 * Create: 2019/9/5 下午4:44
 * Modified By:
 * Description:
 */
public class PartQuery<Entity> {

    /**
     * 使用一个不是实体的类来装载查询出来的数据，该装载类必须继承实体类
     * 注意：装载类必须要有合适的构造函数（代码中的）
     * 代码中是根据反射获取查询的字段名，那么装载类中的字段名必须与实体类中的字段名一致，然后注意构造函数里面的顺序
     * <p>
     * 使用EntityManager创建查询时，可以在输入中指定一个CriteriaQuery对象，它返回一个TypedQuery，
     * 它是JPA 2.0引入javax.persistence.Query接口的一个扩展，TypedQuery接口知道它返回的类型，
     * 所以使用中,先创建查询得到TypedQuery,然后通过typeQuery.getResultList得到结果
     *
     * @param em          em
     * @param resultClass resultClass
     * @param entityClass entityClass
     * @param predicate   predicate
     * @return list
     */
    public <Result> List<Result> statisticsQuery(EntityManager em, Class<Result> resultClass,
                                                                Class<Entity> entityClass, Predicate predicate) {
        // 下面是固定写法
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Result> criteriaQuery = criteriaBuilder.createQuery(resultClass);
        Root<Entity> root = criteriaQuery.from(entityClass);
        // 根据反射，得到要查询哪几个字段
        Selection<?>[] selections = getSelections(root, getFieldNames(resultClass));
        // 设置查询到数据后生成新的类的构造方法
        criteriaQuery.select(criteriaBuilder.construct(resultClass, selections));
        // 设置查询条件
        criteriaQuery.where(predicate);
        TypedQuery<Result> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    private static String[] getFieldNames(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        int length = fields.length;
        List<String> list = new ArrayList<>(length);
        for (Field field : fields) {
            if (field.getName().equals("serialVersionUID")) {
                continue;
            }
            list.add(field.getName());
        }
        return list.toArray(new String[0]);
    }

    private Selection<?>[] getSelections(Root<?> root, String... attributeNames) {
        Selection<?>[] selections = new Selection[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            selections[i] = getSelection(root, attributeNames[i]);
        }
        return selections;
    }

    private Selection<?> getSelection(Root<?> root, String attributeName) {
        return root.get(attributeName);
    }
}
