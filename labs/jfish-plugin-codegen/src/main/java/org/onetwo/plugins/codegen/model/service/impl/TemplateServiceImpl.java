package org.onetwo.plugins.codegen.model.service.impl;
import java.io.File;
import java.util.List;

import javax.annotation.Resource;

import org.onetwo.common.db.ExtQuery.K;
import org.onetwo.common.exception.ServiceException;
import org.onetwo.common.fish.JFishCrudServiceImpl;
import org.onetwo.common.utils.FileUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.plugins.codegen.generator.DefaultTableManagerFactory;
import org.onetwo.plugins.codegen.generator.StringTemplateProvider;
import org.onetwo.plugins.codegen.model.dao.TemplateDao;
import org.onetwo.plugins.codegen.model.entity.TemplateEntity;
import org.onetwo.plugins.codegen.model.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;


//@Service
public class TemplateServiceImpl extends JFishCrudServiceImpl<TemplateEntity, Long> implements StringTemplateProvider, TemplateService {

	@Resource
	private TemplateDao templateDao;

	@Autowired
	private DefaultTableManagerFactory tableManagerFactory;
	
	@Override
	public String getTemplateContent(String name) {
		TemplateEntity temp = null;
		try {
			Long id = Long.parseLong(name);
			temp = load(id);
		} catch (NumberFormatException e) {
			temp = findUnique("name", name);
		}
		if(temp==null)
			throw new ServiceException("找不到模板：" + name);
		if(StringUtils.isNotBlank(temp.getFilePath())){
			String content = FileUtils.readAsString(temp.getFilePath());
			if(StringUtils.isNotBlank(content))
				return content;
		}
		return temp.getContent();
	}
	
	@Override
	public void findTemplatePage(Page<TemplateEntity> page){
		super.findPage(page, K.DESC, "lastUpdateTime");
	}

	@Override
	@Transactional
	public void initCodegenTemplate(){
		templateDao.createTemplateTable();
		
		String dir = FileUtils.getResourcePath("META-INF/codegen/template");
		if(StringUtils.isBlank(dir))
			return ;
		File[] files = FileUtils.listFiles(dir, ".ftl");
		if(files==null)
			return ;
		for(File file :files){
			TemplateEntity temp = new TemplateEntity();
			
			String fname = FileUtils.getFileNameWithoutExt(file.getName());
			String postfix = FileUtils.getExtendName(fname);
			if(StringUtils.isNotBlank(postfix)){
				fname = FileUtils.getFileNameWithoutExt(fname);
				temp.setFilePostfix(postfix);
			}else{
				temp.setFilePostfix("ftl");
			}
			String[] names = StringUtils.split(fname, "_");
			
			temp.setPackageName(parseName(names[0]));
			if(names.length>=2){
				temp.setFileNamePostfix(parseName(names[1]));
				temp.setName(temp.getFileNamePostfix());
			}else{
				temp.setName(fname);
			}
			List<String> lines = FileUtils.readAsList(file);
			String content = StringUtils.join(lines, String.valueOf('\n'));
			temp.setContent(content);
			
			getBaseEntityManager().save(temp);
		}
		
	}
	
	
	private String parseName(String name){
		return "-".equals(name)?"":name;
	}

	@Override
	public TemplateEntity findTempateById(Long id) {
		return super.findById(id);
	}

	@Override
	public boolean isInitTemplate() {
		List<String> tables = this.tableManagerFactory.getDefaultTableManager().getTableNames(true);
		return tables.contains(TemplateEntity.TABLE_NAME);
	}
}
