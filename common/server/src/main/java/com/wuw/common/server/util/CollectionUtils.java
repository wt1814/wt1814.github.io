package com.wuw.common.server.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 集合操作的Utils函数集合.
 *
 * 主要针对Web应用与Hibernate的特征而开发.
 *
 * @author calvin
 */

/**
 * @author weisong
 */
public abstract class CollectionUtils extends org.apache.commons.collections.CollectionUtils {
	private CollectionUtils() {
	}
	
	public static Collection<String> split(String str) {
		Collection<String> coll = new ArrayList<String>();
		if (StringUtils.isNotBlank(str)) {
			addAll(coll, str.split(","));
		}
		return coll;
	}

	/**
	 * 提取集合中的对象的属性,组合成列表.
	 *
	 * @param collection 来源集合.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List fetchPropertyToList(final Collection collection, final String propertyName) {
		final List list = new ArrayList();
		try {
			for (final Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return list;
	}

	/**
	 * 提取集合中的对象的属性,组合成由分割符分隔的字符串.
	 *
	 * @param collection 来源集合.
	 * @param propertyName 要提取的属性名.
	 * @param separator 分隔符.
	 */
	@SuppressWarnings("rawtypes")
	public static String fetchPropertyToString(
			final Collection collection, 
			final String propertyName, 
			final String separator)
			throws Exception {
		final List list = fetchPropertyToList(collection, propertyName);
		return StringUtils.join(list, separator);
	}

	/**
	 * 根据对象ID集合,整理合并集合.
	 *
	 * 整理算法为：在源集合中删除不在ID集合中的元素,创建在ID集合中的元素并对其ID属性赋值并添加到源集合中.
	 * 多用于根据http请求中的id列表，修改对象所拥有的子对象集合.
	 *
	 * @param collection 源对象集合
	 * @param checkedIds  目标集合
	 * @param clazz  集合中对象的类型
	 *
	 */
	public static <T, ID> void mergeByCheckedIds(
			final Collection<T> collection, 
			final Collection<ID> checkedIds, 
			final Class<T> clazz)
			throws Exception {
		mergeByCheckedIds(collection, checkedIds, "id", clazz);
	}

	/**
	 * 根据对象ID集合,整理合并集合.
	 *
	 * http请求发送变更后的子对象id列表时，hibernate不适合删除原来子对象集合再创建一个全新的集合
	 * 需采用以下整合的算法：
	 * 在源集合中删除不在ID集合中的元素,创建在ID集合中的元素并对其ID属性赋值并添加到源集合中.
	 *
	 * @param collection 源对象集合
	 * @param checkedIds  目标集合
	 * @param idName 对象中ID的属性名
	 * @param clazz  集合中对象的类型
	 */
	public static <T, ID> void mergeByCheckedIds(
			final Collection<T> collection, 
			final Collection<ID> checkedIds, 
			final String idName,
			final Class<T> clazz) throws Exception {

		if (checkedIds == null) {
			collection.clear();
			return;
		}

		final Iterator<T> it = collection.iterator();

		while (it.hasNext()) {
			final T obj = it.next();
			if (checkedIds.contains(PropertyUtils.getProperty(obj, idName))) {
				checkedIds.remove(PropertyUtils.getProperty(obj, idName));
			} else {
				it.remove();
			}
		}

		for (final ID id : checkedIds) {
			final T obj = clazz.newInstance();
			PropertyUtils.setProperty(obj, idName, id);
			collection.add(obj);
		}
	}

	/**
	 * 从集合中删除包含在remove中的所有对象.
	 *
	 * @param collection 集合
	 * @param remove 要移除的对象
	 * @return 移除的对象数量
	 */
	@SuppressWarnings("rawtypes")
	public static int removeAll(final Collection collection, final Object[] remove) {
		if (remove == null) {
			return 0;
		}
		int removeCount = 0;
		for (final Object obj : remove) {
			if (collection.remove(obj)) {
				removeCount++;
			}
		}
		return removeCount;
	}

    @SuppressWarnings("rawtypes")
	public static Collection removeAll(final Collection collection, final Collection remove) {
    	// 修正commons-collections-3.2.1的bug
        return ListUtils.removeAll(collection, remove);
    }

    /**
     * 从参数列表构建map对象.
     *
     * @param objs 参数列表, 必须为偶数个
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map buildMap(final Object... objs) {
        if (objs.length == 0) {
            return new HashMap();
        }
        if(objs.length % 2 != 0){
				throw  new IllegalArgumentException("wrong nubmer parameters ,must be  even number");
		}
        final Map map = new HashMap();
        for (int i = 0; i < objs.length; i += 2) {
            map.put(objs[i], objs[i + 1]);
        }

        return map;
    }
    
    /**
	 * 
	 * Description: 判断缓存是否是不充足的，需要从数据库里读取
	 * 
	 * @Version1.0 2014年12月11日 下午12:05:30 by 许文轩（xuwenxuan@dangdang.com）创建
	 * @param objectList
	 * @param length
	 * @return
	 */
	public static boolean isNotEnough(Collection<?> objectList, int length) {
		if (objectList == null) {
			return true;
		}
		if (objectList.size() < length) {
			return true;
		}
		for (Object obj : objectList) {
			if (obj == null) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 * @comment 
	 * @date 2017年2月15日 下午12:51:12
	 */
    public static String listToString(List<String> stringList){
        if (stringList==null) {
            return null;
        }
        StringBuilder result=new StringBuilder();
        boolean flag=false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            }else {
                flag=true;
            }
            result.append(string);
        }
        return result.toString();
    }
    
    
        /**
         * 将一组数据平均分成n组
         *
         * @param source 要分组的数据源
         * @param n      平均分成n组
         * @param <T>
         * @return
         */
        public static <T> List<List<T>> averageAssign(List<T> source, int n) {
            List<List<T>> result = new ArrayList<List<T>>();
            int remainder = source.size() % n;  //(先计算出余数)
            int number = source.size() / n;  //然后是商
            int offset = 0;//偏移量
            for (int i = 0; i < n; i++) {
                List<T> value = null;
                if (remainder > 0) {
                    value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                    remainder--;
                    offset++;
                } else {
                    value = source.subList(i * number + offset, (i + 1) * number + offset);
                }
                result.add(value);
            }
            return result;
        }
        /**
         * 将一组数据固定分组，每组n个元素
         *
         * @param source 要分组的数据源
         * @param n      每组n个元素
         * @param <T>
         * @return
         */
        public static <T> List<List<T>> fixedGrouping(List<T> source, int n) {
            if (null == source || source.size() == 0 || n <= 0)
                return null;
            List<List<T>> result = new ArrayList<List<T>>();
            int remainder = source.size() % n;//余数
            int size = (source.size() / n);//商 不算余数 要分多少组。有余数的话下面有单独处理余数数据的
            for (int i = 0; i < size; i++) {//循环要分多少组
                List<T> subset = null;
                subset = source.subList(i * n, (i + 1) * n);//截取list
                result.add(subset);
            }
            if (remainder > 0) {//有余数的情况下把余数得到的数据再添加到list里面
                List<T> subset = null;
                subset = source.subList(size * n, size * n + remainder);
                result.add(subset);
            }
            return result;
        }

}
