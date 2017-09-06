package com.wizardyu.search.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.SortField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wizardyu.olympus.hera.search.LuceneService;
import com.wizardyu.olympus.hera.search.domain.LuceneFieldVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/applicationContext.xml",
		"file:src/main/webapp/WEB-INF/spring-mvc-servlet.xml" })
public class LuceneIKTest extends AbstractJUnit4SpringContextTests {

	@Resource(name = "luceneService")
	private LuceneService luceneService;

	@Test
	public void test() {
		System.out.println(luceneService.test());

	}

	@Test
	public void testLuceneService() {
		List<List<LuceneFieldVO>> allList = new ArrayList<>();
		for (int i = 0; i < 50; i++) {	
			List<LuceneFieldVO> list = new ArrayList<LuceneFieldVO>();

			LuceneFieldVO luceneFieldVO = new LuceneFieldVO();
			luceneFieldVO.setFieldName("title");
			luceneFieldVO.setFieldValue("数值" + i);
			luceneFieldVO.setFieldType(LuceneFieldVO.TYPE_STRING);
			list.add(luceneFieldVO);

			LuceneFieldVO id = new LuceneFieldVO();
			id.setFieldName("id");
			id.setFieldValue("" + i);
			id.setCanSort(true);
			id.setFieldType(LuceneFieldVO.TYPE_INT);
			list.add(id);

			allList.add(list);
		}

		luceneService.updateDocument(allList, "test");

		String[] fields = { "title", "id" };
		BooleanClause.Occur[] clauses = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD };

		SortField sortField = new SortField("id", SortField.Type.LONG, true);
		String searchStr = "数值44";

		luceneService.search("test", fields, clauses, sortField, searchStr, 5, 0, 20);

	}
}
