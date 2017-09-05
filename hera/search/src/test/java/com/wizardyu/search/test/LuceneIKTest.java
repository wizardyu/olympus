package com.wizardyu.search.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wizardyu.olympus.hera.search.LuceneService;
import com.wizardyu.olympus.hera.search.domain.LuceneFieldVO;


@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml","file:src/main/webapp/WEB-INF/spring-mvc-servlet.xml"}) 
public class LuceneIKTest extends AbstractJUnit4SpringContextTests{

	@Resource(name="luceneService")
    private LuceneService luceneService;
	
	@Test
	public void test() {  
		System.out.println(luceneService.test());
		
	}

	@Test
	public void testLuceneService() {  
		List<LuceneFieldVO> list = new ArrayList<LuceneFieldVO>();
		for(int i=0;i<100;i++) {
			LuceneFieldVO luceneFieldVO = new LuceneFieldVO();
			luceneFieldVO.setFieldName("名称"+i);
			luceneFieldVO.setFieldValue("数值"+i);
			list.add(luceneFieldVO);
		}
		luceneService.createDocument(list, "test");
	}
}
