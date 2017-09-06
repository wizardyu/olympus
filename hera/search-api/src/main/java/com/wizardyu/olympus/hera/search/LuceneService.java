package com.wizardyu.olympus.hera.search;

import java.util.List;

import com.wizardyu.olympus.hera.search.domain.LuceneFieldVO;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.BooleanClause;


/**
 * 基于lucene的检索服务
 *
 */
public interface LuceneService {
	/**
	 * 创建端口
	 * 
	 * @param luceneFieldVOList
	 */
	public void createDocument(List<LuceneFieldVO> luceneFieldVOList, String appName);

	public String updateDocument(List<List<LuceneFieldVO>> luceneFieldVOList, String appName);

	public List<List<LuceneFieldVO>> search(String appName ,String[] fields, BooleanClause.Occur[] clauses, SortField sortField, String searchStr, int pageSize, int pageNum, int maxSize);

	/**
	 * 测试是否连接成功端口
	 * 
	 * @return
	 */
	public String test();
}
