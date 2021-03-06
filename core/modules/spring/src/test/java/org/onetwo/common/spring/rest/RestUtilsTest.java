package org.onetwo.common.spring.rest;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.spring.entity.RoleEntity;
import org.onetwo.common.spring.entity.UserEntity;
import org.onetwo.common.utils.ParamUtils;
import org.springframework.util.MultiValueMap;


public class RestUtilsTest {
	
	@Test
	public void test(){
		UserEntity user = new UserEntity();
		user.setId(11L);
		user.setUserName("testUserName");
		
		RoleEntity role = new RoleEntity();
		role.setId(20L);
		role.setName("testRoleName");

		RoleEntity role1 = new RoleEntity();
		role1.setId(21L);
		role1.setName("testRoleName1");
		
		user.setRoles(Arrays.asList(role, role1));
		
		MultiValueMap<String, Object> mmap = RestUtils.toMultiValueMap(user);
		System.out.println("mmap:"+mmap);
		String paramString = ParamUtils.toParamString(mmap);
		System.out.println("paramString:"+paramString);
		Assert.assertEquals("age=0&height=0.0&id=11&roles[0].id=20&roles[0].name=testRoleName&roles[0].version=0&roles[1].id=21&roles[1].name=testRoleName1&roles[1].version=0&userName=testUserName", 
				paramString);
	}

}
