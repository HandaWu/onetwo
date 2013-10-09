package org.onetwo.plugins.permission;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.spring.utils.JFishResourcesScanner;
import org.onetwo.common.spring.utils.ResourcesScanner;
import org.onetwo.common.spring.utils.ScanResourcesCallback;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.plugins.permission.anno.MenuMapping;
import org.onetwo.plugins.permission.entity.IFunction;
import org.onetwo.plugins.permission.entity.IMenu;
import org.onetwo.plugins.permission.entity.IPermission;
import org.onetwo.plugins.permission.entity.PermissionType;
import org.springframework.core.type.classreading.MetadataReader;

public class DefaultMenuInfoParser implements MenuInfoParser {

	private final Map<String, IPermission> menuNodeMap = new LinkedHashMap<String, IPermission>(50);
	private final Map<Class<?>, IPermission> menuNodeMapByClass = new LinkedHashMap<Class<?>, IPermission>(50);
	private IMenu<? extends IMenu<?, ?> , ? extends IFunction<?>> rootMenu;
	private final ResourcesScanner scaner = new JFishResourcesScanner();

	@Resource
	private PermissionConfig menuInfoable;
	private int sortStartIndex = 1000;
	

	public Map<String, ? extends IPermission> getMenuNodeMap() {
		return menuNodeMap;
	}
	
	public String getRootMenuCode(){
		return parseCode(menuInfoable.getRootMenuClass());
	}

	/* (non-Javadoc)
	 * @see org.onetwo.plugins.permission.MenuInfoParser#parseTree()
	 */
	@Override
	public IMenu<?, ?> parseTree(){
		Assert.notNull(menuInfoable);
		Class<?> menuInfoClass = menuInfoable.getRootMenuClass();
		
		IPermission perm = null;
		try {
			perm = parseMenuClass(menuInfoClass);
			perm.setSort(1);
		} catch (Exception e) {
			throw new BaseException("parse tree error: " + e.getMessage(), e);
		}
		if(!IMenu.class.isInstance(perm))
			throw new BaseException("root must be a menu node");
		rootMenu = (IMenu<?, ?>)perm;
		
		String[] childMenuPackages = menuInfoable.getChildMenuPackages();
		if(LangUtils.isEmpty(childMenuPackages))
			return rootMenu;
		
		List<Class<?>> childMenuClass = scaner.scan(new ScanResourcesCallback<Class<?>>(){

			@Override
			public boolean isCandidate(MetadataReader metadataReader) {
				if (metadataReader.getAnnotationMetadata().hasAnnotation(MenuMapping.class.getName()))
					return true;
				return false;
			}

			@Override
			public Class<?> doWithCandidate(MetadataReader metadataReader, org.springframework.core.io.Resource resource, int count) {
				Class<?> cls = ReflectUtils.loadClass(metadataReader.getClassMetadata().getClassName());
				return cls;
			}
			
		}, childMenuPackages);
		
		if(LangUtils.isEmpty(childMenuClass))
			return rootMenu;
		
		for(Class<?> childMc : childMenuClass){
			IPermission childPerm =  parseMenuClass(childMc);
			if(IMenu.class.isInstance(childPerm)){
				rootMenu.addChild((IMenu)childPerm);
			}else{
				rootMenu.addFunction((IFunction)childPerm);
			}
		}
		
		return rootMenu;
	}
	

	protected <T extends IPermission> T parseMenuClass(Class<?> menuClass){
		IPermission perm;
		try {
			perm = parsePermission(menuClass);
		} catch (Exception e) {
			throw new BaseException("parser permission error: " + e.getMessage(), e);
		}
		if(perm instanceof IFunction)
			return (T)perm;
		
		IMenu<? extends IMenu<?, ?> , ? extends IFunction<?>> menu = (IMenu) perm;
		Class<?>[] childClasses = menuClass.getDeclaredClasses();
//		Arrays.sort(childClasses);
		for(Class<?> childCls : childClasses){
			IPermission p = parseMenuClass(childCls);
			if(p instanceof IFunction){
				menu.addFunction((IFunction)p);
			}else{
				menu.addChild((IMenu)p);
			}
		}
		return (T)menu;
	}
	
	public IPermission parsePermission(Class<?> permissionClass) throws Exception{
		Field sortField = ReflectUtils.findField(permissionClass, "sort");
		Number sort = null;
		if(sortField!=null){
			Object pvalue = sortField.get(permissionClass);
			if(!Number.class.isInstance(pvalue))
				throw new BaseException("field[sort] of " + permissionClass + " must be Number.");
			sort = (Number) pvalue;
		}else{
			sort = sortStartIndex++;
		}
		
		Field pageElementField = ReflectUtils.findField(permissionClass, "permissionType");
		PermissionType ptype = PermissionType.MENU;
		if(pageElementField!=null){
			Object pvalue = pageElementField.get(permissionClass);
			if(!PermissionType.class.isInstance(pvalue))
				throw new BaseException("field[permissionType] of " + permissionClass + " must be PermissionType.");
			ptype = (PermissionType) pvalue;
		}
		


		Object nameValue = ReflectUtils.getFieldValue(permissionClass, "name", true);
		String name = nameValue==null?"":nameValue.toString();
		IPermission perm = null;
		if(ptype==PermissionType.FUNCTION){
			perm = (IPermission)ReflectUtils.newInstance(this.menuInfoable.getIFunctionClass());
		}else{
			perm = (IPermission)ReflectUtils.newInstance(this.menuInfoable.getIMenuClass());
		}
		perm.setName(name);
		String code = parseCode(permissionClass);
		perm.setCode(code);
		perm.setSort(sort.intValue());
		this.menuNodeMap.put(perm.getCode(), perm);
		this.menuNodeMapByClass.put(permissionClass, perm);
		return perm;
	}
	
	
	/* (non-Javadoc)
	 * @see org.onetwo.plugins.permission.MenuInfoParser#parseCode(java.lang.Class)
	 */
	@Override
	public String parseCode(Class<?> menuClass){
		String code = menuClass.getSimpleName();
		while(menuClass.getDeclaringClass()!=null){
			menuClass = menuClass.getDeclaringClass();
			code = menuClass.getSimpleName() + "_" + code;
		}
		MenuMapping mapping = menuClass.getAnnotation(MenuMapping.class);
		if(mapping!=null){
			Class<?> pcls = mapping.parent();
			IPermission perm = this.menuNodeMapByClass.get(pcls);
			if(perm==null)
				throw new BaseException("parse menu class["+menuClass+"] error. no parent menu found: " + pcls);
			code = perm.getCode() + "_" + code;
		}
		return code;
	}
	
	public IPermission getMenuNode(Class<?> clazz){
		return this.menuNodeMapByClass.get(clazz);
	}
	
	public IPermission getMenuNode(String code){
		return (IPermission)menuNodeMap.get(code);
	}

	public IMenu getRootMenu() {
		return rootMenu;
	}

	public void setRootMenu(IMenu rootMenu) {
		this.rootMenu = rootMenu;
	}

	public PermissionConfig getMenuInfoable() {
		return menuInfoable;
	}
	
	
}