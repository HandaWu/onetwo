package org.onetwo.common.excel;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.profiling.Timeit;
import org.onetwo.common.profiling.UtilTimerStack;
import org.onetwo.common.utils.FileUtils;

public class ExcelTest {
	
	private List<CityCompainInfo> list;
	private int count = 10000;
	
	private String outputPath = "org/onetwo/common/excel/";
	@Before
	public void setup(){
		list = new ArrayList<CityCompainInfo>();
		CityCompainInfo city;
		for(long i=0; i<count; i++){
			city = new CityCompainInfo();
			city.setId(i);
			city.setName("name"+i);
			city.setPhone("phone"+i);
			city.setPostcode("postcode"+i);
			city.setAddress("address"+i);
			city.setFax("fax"+i);
			
			list.add(city);
		}
	}
	
	@Test
	public void testAll(){
//		this.testTemplateWithReflect();
//		this.testTemplateWithReflectOgnl();
		
//		UtilTimerStack.setActive(true);
//		UtilTimerStack.setNanoTime(true);
		Timeit.create()
		.timeit(this, "testRawPoi")
		.timeit(this, "testTemplateWithReflectOgnl")
		.timeit(this, "testTemplateWithReflect")
		.timeit(this, "testTemplateWithOgnl")
		.printAll();
	}
	
//	@Test
	public void testRawPoi(){
//		UtilTimerStack.setActive(true);
		String[] titles = new String[]{"主键", "名称", "电话", "邮编", "地址", "传真"};
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("报名客户资料");
		int rownum = 0;
		HSSFRow row = sheet.createRow(rownum++);
		HSSFCell cell = null;
		for(int i=0; i<titles.length; i++){
			cell = row.createCell(i);
			cell.setCellValue(titles[i]);
		}
		
		String colName = "";
		int profileCount = 50;
		for(CityCompainInfo city : list){
			int column = 0;
			if(column<profileCount){
				colName = "row-"+column;
				UtilTimerStack.push(colName);
			}
			
			row = sheet.createRow(rownum++);
			//id
			cell = row.createCell(column++);
			cell.setCellValue(city.getId());
			//name
			cell = row.createCell(column++);
			cell.setCellValue(city.getName());
			//phone
			cell = row.createCell(column++);
			cell.setCellValue(city.getPhone());
			//postcode
			cell = row.createCell(column++);
			cell.setCellValue(city.getPostcode());
			//address
			cell = row.createCell(column++);
			cell.setCellValue(city.getAddress());
			//fax
			cell = row.createCell(column++);
			cell.setCellValue(city.getFax());
			

			if(column<profileCount){
				UtilTimerStack.pop(colName);
			}
		}

		String path = FileUtils.getResourcePath(outputPath)+"city_company_raw.xls";
		try {
			workbook.write(new FileOutputStream(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("raw path: " + path);
	}
	
//	@Test
	public void testTemplateWithReflect(){
		System.out.println("===========================>>>>>testTemplateWithReflect ");
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("data", list);
		String path = FileUtils.getResourcePath(outputPath)+"city_company_reflect.xls";
		PoiExcelGenerator g = DefaultExcelGeneratorFactory.createExcelGenerator("org/onetwo/common/excel/template_city_company.xml", context);
		g.generateIt();
		g.write(path);
	}
	
//	@Test
	public void testTemplateWithReflectOgnl(){
		System.out.println("===========================>>>>>testTemplateWithReflectOgnl ");
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("data", list);
		String path = FileUtils.getResourcePath(outputPath)+"city_company_reflect_ognl.xls";
		PoiExcelGenerator g = DefaultExcelGeneratorFactory.createExcelGenerator("org/onetwo/common/excel/template_city_company_span_ognl.xml", context);
		g.generateIt();
		g.write(path);
	}
	
	public void testTemplateWithOgnl(){
//		UtilTimerStack.setActive(false);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("data", list);
		String path = FileUtils.getResourcePath(outputPath)+"city_company_ognl.xls";
		PoiExcelGenerator g = DefaultExcelGeneratorFactory.createExcelGenerator("org/onetwo/common/excel/template_city_company_ognl.xml", context);
		g.generateIt();
		g.write(path);
	}

}