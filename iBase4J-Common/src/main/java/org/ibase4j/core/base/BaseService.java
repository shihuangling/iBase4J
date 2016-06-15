package org.ibase4j.core.base;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ibase4j.core.Constants;
import org.ibase4j.core.util.RedisUtil;
import org.ibase4j.core.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import com.github.pagehelper.PageInfo;

/**
 * @author ShenHuaJie
 * @version 2016年5月20日 下午3:47:58
 */
public abstract class BaseService<P extends BaseProvider<T>, T> {
	protected Logger logger = LogManager.getLogger();
	protected P provider;
	@Autowired
	protected RedisSerializer<Object> valueSerializer;

	/** 修改 */
	public void update(T record) {
		Object id = null;
		try {
			id = record.getClass().getDeclaredMethod("getId").invoke(record);
			record.getClass().getDeclaredMethod("setUpdateBy", Integer.class).invoke(record, WebUtil.getCurrentUser());
		} catch (Exception e) {
		}
		Assert.notNull(id, "ID");
		provider.update(record);
	}

	/** 新增 */
	public void add(T record) {
		try {
			record.getClass().getDeclaredMethod("setCreateBy", Integer.class).invoke(record, WebUtil.getCurrentUser());
			record.getClass().getDeclaredMethod("setUpdateBy", Integer.class).invoke(record, WebUtil.getCurrentUser());
		} catch (Exception e) {
		}
		provider.update(record);
	}

	/** 删除 */
	public void delete(Integer id) {
		Assert.notNull(id, "ID");
		provider.delete(id, WebUtil.getCurrentUser());
	}

	/** 根据Id查询 */
	@SuppressWarnings("unchecked")
	public T queryById(Integer id) {
		Assert.notNull(id, "ID");
		StringBuilder sb = new StringBuilder(Constants.CACHE_NAMESPACE);
		String className = this.getClass().getSimpleName();
		sb.append(className.substring(0, 1).toLowerCase()).append(className.substring(1, className.length() - 7));
		sb.append(":").append(id);
		byte[] value = RedisUtil.get(sb.toString().getBytes());
		if (value != null) {
			return (T) valueSerializer.deserialize(value);
		}
		return provider.queryById(id);
	}

	/** 条件查询 */
	public PageInfo<T> query(Map<String, Object> params) {
		return provider.query(params);
	}
}
