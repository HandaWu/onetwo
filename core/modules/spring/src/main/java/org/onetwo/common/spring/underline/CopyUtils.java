package org.onetwo.common.spring.underline;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.underline.BaseCopierBuilder.SimpleCopierBuilder;
import org.onetwo.common.utils.Page;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

public class CopyUtils {
    public static final PropertyDescriptor[] EMPTY_PROPERTIES_ARRAY = new PropertyDescriptor[0];
    
    


    public static class ObjectCopierBuilder extends SimpleCopierBuilder<Object, ObjectCopierBuilder> {
    	public static ObjectCopierBuilder newBuilder(){
    		return new ObjectCopierBuilder();
    	}
    	public static ObjectCopierBuilder from(Object obj){
    		return new ObjectCopierBuilder().copy(obj);
    	}
    }

    public static class BeanCopierBuilder<T> extends SimpleCopierBuilder<T, BeanCopierBuilder<T>> {
    	public static <E> BeanCopierBuilder<E> newBuilder(){
    		return new BeanCopierBuilder<E>();
    	}
    	public static <E> BeanCopierBuilder<E> from(E obj){
    		return new BeanCopierBuilder<E>().copy(obj);
    	}
    }
    public static class PageCopierBuilder<T> extends BaseCopierBuilder<PageCopierBuilder<T>> {
    	public static <E> PageCopierBuilder<E> from(Page<E> page){
    		return new PageCopierBuilder<E>(page);
    	}
    	
    	private Page<T> page;
    	
		public PageCopierBuilder(Page<T> page) {
			super();
			this.page = page;
		}
		
		public Page<T> to(Class<T> targetClass){
			Page<T> newPage = Page.createByPage(page, (e)->ReflectUtils.newInstance(targetClass));
			return newPage;
		}
		
    }
    public static class ListCopierBuilder<T> extends BaseCopierBuilder<ListCopierBuilder<T>> {
    	public static <E> ListCopierBuilder<E> from(Iterable<E> page){
    		return new ListCopierBuilder<E>(page);
    	}
    	
    	private Iterable<T> datas;
    	
		public ListCopierBuilder(Iterable<T> datas) {
			super();
			this.datas = datas;
		}

		public <R> ListCopierBuilder2<T, R> withElement(Class<R> elementClass){
			return new ListCopierBuilder2<T, R>(datas).withElement(elementClass);
		}
		
		public List<T> toList(){
			List<T> newDatas = StreamSupport.stream(datas.spliterator(), false)
						.map((e)->{
							return newCopier().fromObject(e, (Class<T>)e.getClass());
						})
						.collect(Collectors.toList());
			return newDatas;
		}
		
		
		public <E> List<E> toListClass(Class<E> targetClass){
			List<E> newDatas = StreamSupport.stream(datas.spliterator(), false)
						.map((e)->{
							return newCopier().fromObject(e, targetClass);
						})
						.collect(Collectors.toList());
			return newDatas;
		}
		
    }
    

    public static class ListCopierBuilder2<T, R> extends BaseCopierBuilder<ListCopierBuilder2<T, R>> {
    	public static <E1, E2> ListCopierBuilder2<E1, E2> from(Iterable<E1> page){
    		return new ListCopierBuilder2<E1, E2>(page);
    	}
    	
    	private Iterable<T> datas;
    	private Class<R> elementClass;
    	
		public ListCopierBuilder2(Iterable<T> datas) {
			super();
			this.datas = datas;
		}
		
		public ListCopierBuilder2<T, R> withElement(Class<R> elementClass){
			this.elementClass = elementClass;
			return this;
		}
		
		public List<R> toList(){
			Assert.notNull(elementClass);
			List<R> newDatas = StreamSupport.stream(datas.spliterator(), false)
						.map((e)->{
							return newCopier().fromObject(e, elementClass);
						})
						.collect(Collectors.toList());
			return newDatas;
		}
		
    }

    public static final PropertyNameConvertor UNDERLINE_CONVERTOR = SeperatorNamedConvertor.UNDERLINE_CONVERTOR;
    public static final SimpleBeanCopier BEAN_COPIER = SimpleBeanCopier.unmodifyCopier(new SimpleBeanCopier(UNDERLINE_CONVERTOR));
	
	public static BeanWrapper newBeanWrapper(Object obj){
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		bw.setAutoGrowNestedPaths(true);
		return bw;
	}
	
	public static <T> T newInstance(Class<T> targetClass){
		try {
			return targetClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("create instance error: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends Collection<?>> T newCollections(Class<?> clazz){
		if(!Collection.class.isAssignableFrom(clazz))
			throw new RuntimeException("class must be a Collection type: " + clazz);
		
		if(clazz==List.class){
			return (T)new ArrayList();
		}else if(clazz==Set.class){
			return (T) new HashSet();
		}else if(clazz==SortedSet.class || clazz==NavigableSet.class){
			return (T) new TreeSet();
		}else if(clazz==Queue.class || clazz==Deque.class){
			return (T) new ArrayDeque();
		}else{
			return (T)newInstance(clazz);
		}
	}
	public static <K, V> Map<K, V> newMap(Class<? extends Map> mapClass){
		if(mapClass==Map.class)
			return new HashMap<K, V>();
		return (Map<K, V>)ReflectUtils.newInstance(mapClass);
	}
	
    public static <T> T copy(Class<T> targetClass, Object src){
    	return copy(newInstance(targetClass), src, UNDERLINE_CONVERTOR);
    }


    public static <T> T copy(T target, Object src){
    	return BEAN_COPIER.fromObject(src, target);
    }

    public static <T> BeanCopierBuilder<T> from(T target){
    	return BeanCopierBuilder.from(target);
    }

    public static <T> ListCopierBuilder<T> from(Iterable<T> target){
    	return ListCopierBuilder.from(target);
    }

    /*public static <T, R> ListCopierBuilder2<T, R> from2(Iterable<T> target){
    	return ListCopierBuilder2.from(target);
    }*/

    public static <T> List<T> copy(Class<T> targetCls, Iterable<?> srcs){
    	return StreamSupport.stream(srcs.spliterator(), false)
    						.map(e->copy(targetCls, e)).collect(Collectors.toList());
    }
    
    /*****
     * 
     * @param target from src object copy to this target
     * @param src
     * @param convertor
     * @return
     */
    public static <T> T copy(T target, Object src, PropertyNameConvertor convertor){
//    	return new BeanWrappedCopier<T>(target, convertor).fromObject(src);
    	BeanCopierBuilder.from(src)
    					.propertyNameConvertor(convertor)
    					.to(target);
    	return target;
    }
    

    public static ObjectCopierBuilder copier(){
    	return ObjectCopierBuilder.newBuilder();
    }

	public static Collection<String> desribPropertyNames(Class<?> clazz){
		PropertyDescriptor[] props = desribProperties(clazz);
		Set<String> names = new HashSet<String>(props.length);
		for(PropertyDescriptor prop : props){
			names.add(prop.getName());;
		}
		return names;
	}
	
	public static PropertyDescriptor[] desribProperties(Class<?> clazz){
		if(clazz.isInterface())
			return EMPTY_PROPERTIES_ARRAY;
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		} catch (Exception e) {
			throw new RuntimeException("get beaninfo error.", e);
		}
		PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
		
		return props;
	}
	
	private CopyUtils(){
	}

}
