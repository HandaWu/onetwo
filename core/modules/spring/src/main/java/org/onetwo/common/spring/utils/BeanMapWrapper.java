package org.onetwo.common.spring.utils;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.convert.Types;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.convert.TypeDescriptor;


public class BeanMapWrapper extends BeanWrapperImpl implements PropertyAccessor {
	
	private class MapTokens {
		final private String key;
		final private String propertyPath;
		final private int listIndex;
		public MapTokens(String key, String propertyPath) {
			this(key, Integer.MIN_VALUE, propertyPath);
		}
		
		public MapTokens(String key, int listIndex, String propertyPath) {
			super();
			this.key = key;
			this.propertyPath = propertyPath;
			this.listIndex = listIndex;
		}



		public boolean hasPropertyPath(){
			return StringUtils.isNotBlank(propertyPath);
		}

		public boolean isList() {
			return listIndex>=0;
		}

		
	}

//	private BeanWrapper beanWrapper;
	private Map<Object, Object> data;
	private boolean mapData;
	private Map<String, Class<?>> listElementTypes;
	
	public BeanMapWrapper(Object obj, Object...listElementTypes){
		super(obj);
		if(Map.class.isInstance(obj)){
			data = (Map<Object, Object>) obj;
			this.mapData = true;
		}else{
//			beanWrapper = SpringUtils.newBeanWrapper(obj);
		}
		if(LangUtils.isEmpty(listElementTypes)){
			this.listElementTypes = Collections.EMPTY_MAP;
		}else{
			this.listElementTypes = LangUtils.asMap(listElementTypes);
		}
		setAutoGrowNestedPaths(true);
	}
	
	private MapTokens parseMapExp(String exp){
		char[] chars = exp.toCharArray();
		char ch;
		MapTokens tokens = null;
		for(int i=0; i<chars.length; i++){
			ch = chars[i];
			if(ch=='.'){
				tokens = new MapTokens(exp.substring(0, i), exp.substring(i+1));
				break;
			}else if(ch=='['){
				String indexStr = null;
				int end = i+1;
				for(; end<chars.length; end++){
					if(chars[end]==']'){
						indexStr = exp.substring(i+1, end);
						break;
					}
				}
				if(StringUtils.isNotBlank(indexStr)){
					int listIndex = Types.convertValue(indexStr, int.class);
					if(end<chars.length-1 && chars[end+1]=='.')
						end = end+2;//user[0].xxx
					else
						end = end+1;
					tokens = new MapTokens(exp.substring(0, i), listIndex, exp.substring(end));
				}else{
					tokens = new MapTokens(exp.substring(0, i), exp.substring(i));
				}
				break;
			}
		}
		if(tokens==null){
			tokens = new MapTokens(exp, null);
		}
		return tokens;
		/*int dotIndex = StringUtils.indexOfAny(exp, ".", "[");
		if(dotIndex!=-1){
			return new MapTokens(exp.substring(0, dotIndex), exp.substring(dotIndex+1));
		}else{
			return new MapTokens(exp, null);
		}*/
	}
	
	public void setPropertyValue(String propertyName, Object value) throws BeansException {
		if(mapData){
			MapTokens token = parseMapExp(propertyName);
			if(token.hasPropertyPath()){
				Object propValue = data.get(token.key);
				propValue = getIndexValueIfListToken(token, propValue, true);
				BeanWrapper bw = SpringUtils.newBeanWrapper(propValue);
				bw.setPropertyValue(token.propertyPath, value);
			}else{
				data.put(propertyName, value);
			}
		}else{
			super.setPropertyValue(propertyName, value);
		}
	}

	@Override
	public Object getPropertyValue(String propertyName) throws BeansException {
		if(mapData){
			MapTokens token = parseMapExp(propertyName);
			if(token.hasPropertyPath()){
				Object value = data.get(token.key);
				if(value==null)
					return null;

				value = getIndexValueIfListToken(token, value, false);
				BeanWrapper bw = SpringUtils.newBeanWrapper(value);
				return bw.getPropertyValue(token.propertyPath);
			}else{
				return data.get(propertyName);
			}
		}else{
			return super.getPropertyValue(propertyName);
		}
	}

	private Object getIndexValueIfListToken(MapTokens token, Object value, boolean forSet){
		if(!token.isList())
			return value;
		
		Object rs = value;
		if(value instanceof List){
			List<?> list = (List<?>) value;
			if(forSet && isAutoGrowNestedPaths()){
				Class<?> eType = ReflectUtils.getGenricType(value, 0);
				if(eType==Object.class){
					if(this.listElementTypes.containsKey(token.key)){
						eType = this.listElementTypes.get(token.key);
					}else{
						throw new BaseException("the type of list element is unknow: " + eType);
					}
					Object e = ReflectUtils.newInstance(eType);
					((List)value).add(token.listIndex, e);
				}
			}
			rs = list.get(token.listIndex);
		}else{
			//array
			rs = Array.get(value, token.listIndex);
		}
		return rs;
	}

	@Override
	public boolean isReadableProperty(String propertyName) {
		if(mapData){
			MapTokens token = parseMapExp(propertyName);
			if(token.hasPropertyPath()){
				Object value = data.get(token.key);
				if(value==null)
					return data.containsKey(token.key);
				
				value = getIndexValueIfListToken(token, value, false);
				BeanWrapper bw = SpringUtils.newBeanWrapper(value);
				return bw.isReadableProperty(token.propertyPath);
			}else{
				return data.containsKey(propertyName);
			}
		}else{
			return super.isReadableProperty(propertyName);
		}
	}


	@Override
	public boolean isWritableProperty(String propertyName) {
		if(mapData){
			return true;
		}else{
			return super.isWritableProperty(propertyName);
		}
	}


	@Override
	public Class getPropertyType(String propertyName) throws BeansException {
		if(mapData){
			return null;
		}else{
			return super.getPropertyType(propertyName);
		}
	}


	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName)
			throws BeansException {
		if(mapData){
			return null;
		}else{
			return super.getPropertyTypeDescriptor(propertyName);
		}
	}


	@Override
	public void setPropertyValues(Map<?, ?> map) throws BeansException {
		if(mapData){
			data.putAll(map);
		}else{
			super.setPropertyValues(map);
		}
		
	}


	@Override
	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		if(mapData){
			throw new UnsupportedOperationException();
		}else{
			super.setPropertyValues(pvs);
		}
		
	}
	@Override
	public void setPropertyValue(PropertyValue pv) throws BeansException {
		if(mapData){
			throw new UnsupportedOperationException();
		}else{
			super.setPropertyValue(pv);
		}
	}


	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
			throws BeansException {
		if(mapData){
			throw new UnsupportedOperationException();
		}else{
			super.setPropertyValues(pvs, ignoreUnknown);
		}
		
	}


	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown,
			boolean ignoreInvalid) throws BeansException {
		if(mapData){
			throw new UnsupportedOperationException();
		}else{
			super.setPropertyValues(pvs, ignoreUnknown, ignoreInvalid);
		}
		
	}


	

}
