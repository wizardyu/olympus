package com.wizardyu.olympus.hera.search;

import java.util.List;

import com.wizardyu.olympus.hera.search.domain.LuceneFieldVO;

/**
 * 基于lucene的检索服务
 *
 */
public interface LuceneService {
	/**
	 * 创建端口
	 * @param luceneFieldVOList
	 */
	public void createDocument(List<LuceneFieldVO> luceneFieldVOList,String appName);
	
	/**
	 * 测试是否连接成功端口
	 * @return
	 */
	public String test();
}
