package org.onetwo.plugins.permission;

import org.onetwo.common.spring.web.utils.JFishWebUtils;
import org.onetwo.common.utils.PermissionDetail;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.UserDetail;

final public class PermissionUtils {
	
	private static MenuInfoParser menuInfoParser;
	
	public static void setMenuInfoParser(MenuInfoParser menuInfoParser) {
		PermissionUtils.menuInfoParser = menuInfoParser;
	}

	public static boolean hasPermission(String permissionCode){
		if(StringUtils.isBlank(permissionCode))
			return true;
		UserDetail userDetail = JFishWebUtils.getUserDetail();
		if(!PermissionDetail.class.isInstance(userDetail))
			return false;
		PermissionDetail p = (PermissionDetail) userDetail;
		return hasPermission(p, permissionCode);
	}
	
	public static boolean hasPermission(Class<?> codeClass){
		UserDetail userDetail = JFishWebUtils.getUserDetail();
		if(!PermissionDetail.class.isInstance(userDetail))
			return false;
		PermissionDetail p = (PermissionDetail) userDetail;
		return hasPermission(p, codeClass);
	}
	
	public static boolean hasPermission(PermissionDetail userDetail, String permissionCode){
		return userDetail.getPermissions()!=null && userDetail.getPermissions().contains(permissionCode);
	}
	
	public static boolean hasPermission(PermissionDetail userDetail, Class<?> codeClass){
		String permissionCode = menuInfoParser.parseCode(codeClass);
		return userDetail.getPermissions()!=null && userDetail.getPermissions().contains(permissionCode);
	}
	
	
	private PermissionUtils(){}

}