package org.onetwo.common.spring.sql;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.onetwo.common.db.AbstractDataQuery;
import org.onetwo.common.db.CreateQueryable;
import org.onetwo.common.db.DataQuery;
import org.onetwo.common.db.FileNamedSqlGenerator;
import org.onetwo.common.db.SqlAndValues;
import org.onetwo.common.db.sql.QueryOrderByable;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.sql.SqlUtils.ParsedSqlWrapper;
import org.onetwo.common.spring.sql.SqlUtils.ParsedSqlWrapper.SqlParamterMeta;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.springframework.beans.BeanWrapper;

public class DefaultFileQueryImpl<T extends JFishNamedFileQueryInfo> extends AbstractDataQuery implements QueryOrderByable {

//	private DynamicQuery query;
//	private JFishNamedFileQueryInfo info;
	protected CreateQueryable baseEntityManager;
	private DataQuery dataQuery;
	
	protected boolean countQuery;


	private Map<Object, Object> params = LangUtils.newHashMap();
	private int firstRecord = -1;
	private int maxRecords;
	private Class<?> resultClass;
	
	protected T info;
	private FileSqlParser<T> parser;
	private ParserContext parserContext;
	
	private String[] ascFields;
	private String[] desFields;

	public DefaultFileQueryImpl(CreateQueryable baseEntityManager, T info, boolean count, FileSqlParser<T> parser) {
		Assert.notNull(baseEntityManager);
		this.baseEntityManager = baseEntityManager;
		this.countQuery = count;
		this.parser = parser;
		
		this.info = info;
		this.resultClass = countQuery?Long.class:info.getMappedEntityClass();
		/*String sql = count?info.getCountSql():info.getSql();
		Class<?> mappedClass = countQuery?Long.class:info.getMappedEntityClass();*/
		
		/*if(needParseSql){
			this.query = DynamicQueryFactory.createJFishDynamicQuery(sql, mappedClass);
			if(info.isIgnoreNull())
				this.query.ignoreIfNull();
		}else{
			this.dataQuery = createDataQuery(sql, mappedClass);
		}*/
	}
	
//	abstract protected DataQuery createDataQuery(DynamicQuery query);
//	abstract protected DataQuery createDataQuery(String sql, Class<?> mappedClass);

	
	protected DataQuery createDataQuery(String sql, Class<?> mappedClass){
		DataQuery dataQuery = this.baseEntityManager.createSQLQuery(sql, mappedClass);
		return dataQuery;
	}
	
	protected DataQuery createDataQueryIfNecessarry(){
		FileNamedSqlGenerator<T> sqlGen = new DefaultFileNamedSqlGenerator<T>(info, countQuery, parser, parserContext, resultClass, ascFields, desFields, params);
		SqlAndValues sqlAndValues = sqlGen.generatSql();
		if(sqlAndValues.isListValue()){
			dataQuery = createDataQuery(sqlAndValues.getParsedSql(), resultClass);
			
			int position = 0;
			for(Object value : sqlAndValues.asList()){
				dataQuery.setParameter(position++, value);
			}
			
			setLimitResult();
			return dataQuery;
		}

		String parsedSql = sqlAndValues.getParsedSql();
		dataQuery = createDataQuery(parsedSql, resultClass);
		
		ParsedSqlWrapper sqlWrapper = SqlUtils.parseSql(parsedSql);
		BeanWrapper paramBean = SpringUtils.newBeanWrapper(sqlAndValues.asMap());
		for(SqlParamterMeta parameter : sqlWrapper.getParameters()){
			if(!paramBean.isReadableProperty(parameter.getProperty()))
				continue;
			/*Object value = paramBean.getPropertyValue(parameterName);
			if(parameter.hasFunction()){
				value = ReflectUtils.invokeMethod(parameter.getFunction(), SqlParamterFunctions.getInstance(), value);
			}*/
			dataQuery.setParameter(parameter.getName(), parameter.getParamterValue(paramBean));
		}
		
		setLimitResult();
		
		return dataQuery;
	}
	private void setLimitResult(){
		if(firstRecord>0)
			dataQuery.setFirstResult(firstRecord);
		if(maxRecords>0)
			dataQuery.setMaxResults(maxRecords);
	}

	public DataQuery setParameter(int index, Object value) {
		this.params.put(index, value);
		return this;
	}

	public DataQuery setParameter(String name, Object value) {
		JNamedQueryKey key = JNamedQueryKey.ofKey(name);
		if(key!=null){
			this.processQueryKey(key, value);
		}else{
			this.params.put(name, value);
		}
		return this;
	}

	public <T> T getSingleResult() {
		return createDataQueryIfNecessarry().getSingleResult();
	}

	public int executeUpdate() {
		return this.createDataQueryIfNecessarry().executeUpdate();
	}
	
	public <T> List<T> getResultList() {
		return createDataQueryIfNecessarry().getResultList();
	}

	public DataQuery setFirstResult(int firstResult) {
		this.firstRecord = firstResult;
		return this;
	}

	public DataQuery setMaxResults(int maxResults) {
		this.maxRecords = maxResults;
		return this;
	}

	public DataQuery setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
		return this;
	}

	public DataQuery setParameters(Map<String, Object> params) {
		for(Entry<String, Object> entry : params.entrySet()){
			setParameter(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/****
	 * 根据key类型设置参数、返回结果类型、排序……等等
	 * @param params
	 */
	public void setQueryAttributes(Map<Object, Object> params) {
		Object key;
		for(Entry<Object, Object> entry : params.entrySet()){
			key = entry.getKey();
			if(String.class.isInstance(key)){
				setParameter(key.toString(), entry.getValue());
			}else if(Integer.class.isInstance(key)){
				setParameter((Integer)key, entry.getValue());
			}else if(JNamedQueryKey.class.isInstance(key)){
				this.processQueryKey((JNamedQueryKey)key, entry.getValue());
			}
		}
	}

	private void processQueryKey(JNamedQueryKey qkey, Object value){
		switch (qkey) {
			case ResultClass:
				if(!countQuery)
					setResultClass((Class<?>)value);
				break;
			case ASC:
				String[] ascFields = CUtils.asStringArray(value);
				asc(ascFields);
				break;
			case DESC:
				String[] desFields = CUtils.asStringArray(value);
				desc(desFields);
				break;
			case ParserContext:
				this.setParserContext((ParserContext)value);
			default:
				break;
		}
		
//		if(JNamedQueryKey.ResultClass.equals(qkey) && !countQuery){//count qyery 忽略
//			setResultClass((Class<?>)value);
//		}
	}
	
	public DataQuery setParameters(List<Object> params) {
		int position = 1;
		for(Object value : params){
			setParameter(position, value);
			position++;
		}
		return this;
	}

	@Override
	public void asc(String... fields) {
		this.ascFields = fields;
		/*if(isNeedParseSql() && QueryOrderByable.class.isAssignableFrom(query.getClass())){
			((QueryOrderByable)query).asc(fields);
		}else{
			throw new UnsupportedOperationException("the query can't supported orderby, you need set ignore.null to true.");
		}*/
	}

	@Override
	public void desc(String... fields) {
		this.desFields = fields;
	}
	

	@Override
	public DataQuery setParameters(Object[] params) {
		if(ArrayUtils.hasNotElement(params))
			return this;
		int position = 1;
		for(Object value : params){
			setParameter(position++, value);
		}
		return this;
	}

	public DataQuery setLimited(final Integer first, final Integer max) {
		this.firstRecord = first;
		this.maxRecords = max;
		return this;
	}
	
	@Override
	public <T> T getRawQuery(Class<T> clazz) {
		this.createDataQueryIfNecessarry();
		return (T)dataQuery;
	}
	@Override
	public DataQuery setQueryConfig(Map<String, Object> configs) {
		return null;
	}

	public void setParserContext(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

}
