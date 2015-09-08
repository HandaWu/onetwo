package org.onetwo.common.db.filequery;

import org.onetwo.common.propconf.JFishProperties;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.utils.LangUtils;

public class NamespaceProperty {
	public static final char DOT_KEY = '.';

	private String name;
	private String value;
	
	private PropertiesNamespaceInfo<? extends NamespaceProperty> namespaceInfo;
	private String namespace;
	private JFishProperties config;
	private ResourceAdapter<?> srcfile;
	
	public String getNamespace() {
		return namespace;
	}

	public String getFullName(){
		if(namespaceInfo.isGlobal())
			return getName();
		return namespace+"."+getName();
	}
	
	public String toString(){
		return LangUtils.append("{ namespace:", namespace, ", name: ", getName(), "}");
	}

	public ResourceAdapter<?> getSrcfile() {
		return srcfile;
	}

	public void setSrcfile(ResourceAdapter<?> srcfile) {
		this.srcfile = srcfile;
	}

	public JFishProperties getConfig() {
		return config;
	}

	public void setConfig(JFishProperties config) {
		this.config = config;
	}

	public PropertiesNamespaceInfo<? extends NamespaceProperty> getNamespaceInfo() {
		return namespaceInfo;
	}

	public void setNamespaceInfo(PropertiesNamespaceInfo<? extends NamespaceProperty> namespaceInfo) {
		this.namespaceInfo = namespaceInfo;
		this.namespace = namespaceInfo.getNamespace();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}