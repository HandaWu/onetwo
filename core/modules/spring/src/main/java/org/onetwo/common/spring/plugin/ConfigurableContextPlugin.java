package org.onetwo.common.spring.plugin;

import java.io.IOException;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.plugin.ConfigurableContextPlugin.LoadableConfig;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.propconf.JFishProperties;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.Resource;

abstract public class ConfigurableContextPlugin<T, C extends LoadableConfig> extends AbstractContextPlugin<T> {
	
	public static interface LoadableConfig {
		void load(JFishProperties properties);
		JFishProperties getSourceConfig();
	}

	public static final String CONFIG_POSTFIX = ".properties";
	
	private final String configBaseDir;
	private final String configName;
	private C config;
	

	public ConfigurableContextPlugin(String configBaseDir, String configName) {
		this(configBaseDir, configName, null);
		this.config = (C)ReflectUtils.newInstance(ReflectUtils.getSuperClassGenricType(getClass(), 1, ConfigurableContextPlugin.class));
	}
	
	public ConfigurableContextPlugin(String configBaseDir, String configName, C config) {
		super();
		this.configBaseDir = StringUtils.surroundWith(configBaseDir, "/");
		this.configName = configName;
		this.config = config;
	}

	protected void initWithEnv(ContextPluginMeta pluginMeta, String appEnv) {
		PropertiesFactoryBean pfb = SpringUtils.createPropertiesBySptring(getConfigPath(), getEnvConfigPath(appEnv));
		try {
			pfb.afterPropertiesSet();
			JFishProperties properties = (JFishProperties)pfb.getObject();
			config.load(properties);
		} catch (IOException e) {
			throw new BaseException("load config error: " + e.getMessage(), e);
		}
	}

	public boolean isConfigExists(){
		String path = getConfigPath();
		Resource config = SpringUtils.classpath(path);
		return config.exists();
	}

	public String getConfigPath(){
		return LangUtils.appendNotBlank(configBaseDir, configName, CONFIG_POSTFIX);
	}
	public String getEnvConfigPath(String env){
		return LangUtils.appendNotBlank(configBaseDir, configName, "-", env, CONFIG_POSTFIX);
	}

	public C getConfig() {
		return config;
	}
}