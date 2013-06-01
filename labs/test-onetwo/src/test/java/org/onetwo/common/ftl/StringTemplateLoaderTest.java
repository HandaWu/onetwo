package org.onetwo.common.ftl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class StringTemplateLoaderTest {
	
	@Test
	public void testStringLoader() throws Exception{
		Configuration cfg = new Configuration(); 
		cfg.setTemplateLoader(new StringTemplateLoader("hello：${user} <#if user?has_content>test</#if>")); 
		cfg.setDefaultEncoding("UTF-8"); 

		Template template = cfg.getTemplate(""); 

		Map root = new HashMap(); 
		root.put("user", "lunzi"); 

		StringWriter writer = new StringWriter(); 
		template.process(root, writer); 
		System.out.println(writer.toString()); 
	}

}
