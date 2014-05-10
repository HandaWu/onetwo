package org.onetwo.plugins.admin.utils;

import org.onetwo.common.web.config.BaseSiteConfig;

final public class AdminPluginConfig {

	public static final String ADMIN_INDEX_TITLE = "admin.index.title";
	public static final String ADMIN_INDEX_VIEW = "admin.index.view";
	public static final String ADMIN_INDEX_ENABLE = "admin.index.enable";
	
	private static final AdminPluginConfig instance = new AdminPluginConfig();
	private BaseSiteConfig config = BaseSiteConfig.getInstance();
	
	private AdminPluginConfig(){
	}

	public static AdminPluginConfig getInstance() {
		return instance;
	}

	public boolean isAdminIndexEnable(){
		return config.getBoolean(ADMIN_INDEX_ENABLE, false);
	}
	
	public String getTitle(){
		return BaseSiteConfig.getInstance().getProperty("admin.index.title", "管理后台");
	}
	public String getAdminView(){
		return BaseSiteConfig.getInstance().getProperty("admin.index.view");
	}
}