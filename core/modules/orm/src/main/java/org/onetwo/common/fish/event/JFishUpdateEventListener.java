package org.onetwo.common.fish.event;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.fish.exception.JFishEntityNotFoundException;
import org.onetwo.common.fish.exception.JFishEntityVersionException;
import org.onetwo.common.fish.orm.JFishMappedEntry;
import org.onetwo.common.fish.orm.JFishMappedField;
import org.onetwo.common.fish.orm.JdbcStatementContext;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;

/*****
 * 
 * @author wayshall
 *
 */
public class JFishUpdateEventListener extends UpdateEventListener {

	/*****
	 * 不是调用批量接口更新的，取用循环插入的方式，通过调用updateSingleEntity方法来检查是否更新成功！
	 */
	@Override
	protected void doUpdate(JFishUpdateEvent event, JFishMappedEntry entry){
		Object entity = event.getObject();
		JFishEventSource es = event.getEventSource();
//		JdbcStatementContext<List<Object[]>> update = null;
		int count = 0;
		if(event.isDynamicUpdate()){
			if(LangUtils.isMultiple(entity)){
				Collection<Object> entityCol = CUtils.toCollection(entity);
				for(Object e : entityCol){
					throwIfMultiple(entity, e);
					count += updateSingleEntity(true, es, entry, e);
				}
			}else{
				count += this.updateSingleEntity(true, es, entry, entity);
			}
		}else{
//			count = this.executeJdbcUpdate(es, entry.makeUpdate(entity));
			if(LangUtils.isMultiple(entity)){
				Collection<Object> entityCol = CUtils.toCollection(entity);
				for(Object e : entityCol){
					throwIfMultiple(entity, e);
					count += updateSingleEntity(false, es, entry, e);
				}
			}else{
				count = this.updateSingleEntity(false, es, entry, entity);
			}
		}
		event.setUpdateCount(count);
	}

	/*********
	 * 更新单个实体，如果更新条数少于1，则表示更新失败，抛出{@link JFishEntityNotFoundException JFishEntityNotFoundException}
	 * @param dymanic
	 * @param es
	 * @param entry
	 * @param singleEntity
	 * @return
	 */
	private int updateSingleEntity(boolean dymanic, JFishEventSource es, JFishMappedEntry entry, Object singleEntity){
		Object dbversion = null;

		if(entry.isVersionControll()){
			JFishMappedField versionField = entry.getVersionField();
			JdbcStatementContext<Object[]> versionContext = entry.makeSelectVersion(singleEntity);
			//因为在同一个事务里，实际上得到的version还是旧的
			dbversion = es.getJFishJdbcTemplate().queryForObject(versionContext.getSql(), versionField.getColumnType(), entry.getId(singleEntity));
			Object entityVersion = entry.getVersionField().getValue(singleEntity);
			
			if(!versionField.getVersionableType().isEquals(entityVersion, dbversion)){
				throw new JFishEntityVersionException(entry.getEntityClass(), entry.getId(singleEntity), entityVersion, dbversion);
			}a
		}
		
		JdbcStatementContext<List<Object[]>> update = dymanic?entry.makeDymanicUpdate(singleEntity):entry.makeUpdate(singleEntity);
		int count = this.executeJdbcUpdate(false, update.getSql(), update.getValue(), es);
		
		if(count<1){
			if(dbversion!=null){
//				Object entityVersion = update.getSqlBuilder().getVersionValue(update.getValue().get(0));
				Object entityVersion = entry.getVersionField().getValue(singleEntity);
				throw new JFishEntityVersionException(entry.getEntityClass(), entry.getId(singleEntity), entityVersion, dbversion);
			}else{
				throw new JFishEntityNotFoundException("update count is " + count + ".", singleEntity.getClass(), entry.getId(singleEntity));
			}
		}
		
		this.updateEntityVersionIfNecessary(update.getSqlBuilder(), update.getValue().get(0), singleEntity);
		
		return count;
	}

}
