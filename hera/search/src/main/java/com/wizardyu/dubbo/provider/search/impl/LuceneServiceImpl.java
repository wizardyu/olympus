
package com.wizardyu.dubbo.provider.search.impl;

import static org.apache.lucene.document.TextField.TYPE_STORED;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.wizardyu.olympus.hera.search.LuceneService;
import com.wizardyu.olympus.hera.search.domain.LuceneFieldVO;

@Service("luceneService")
public class LuceneServiceImpl implements LuceneService {

	private static Directory dir = null;
	private static int resultTotalNum = 0;
	private static int maxPageNum = 0;
	private static String indexPath = "/data/lucene/";
	
	

	@Override
	public void createDocument(List<LuceneFieldVO> luceneFieldVOList, String appName) {
		IndexWriter writer = getIndexWriter(appName);
		try {
			Document doc = new Document();
			if (luceneFieldVOList != null) {
				for (int i = 0; i < luceneFieldVOList.size(); i++) {
					LuceneFieldVO vo = (LuceneFieldVO) luceneFieldVOList.get(i);
					doc.add(new Field(vo.getFieldName(), vo.getFieldValue(), TYPE_STORED));
				}
			}
			writer.addDocument(doc);
			writer.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIndexWriter(writer);
		}
	}

	public static void main(String[] args) throws IOException {
		getIndexWriter("fangtan");
		System.out.println(dir.toString());
	}

	/**
	 * 创建索引方法
	 * 
	 * @return
	 */
	private static IndexWriter getIndexWriter(String appName) {
		// Path path = FileSystems.getDefault().getPath(indexPath, appName);
		File indexFile = new File(indexPath + appName);
		Path path = indexFile.toPath();
		IndexWriter writer = null;
		try {
			if (dir == null) {
				dir = FSDirectory.open(path);
			}
			Analyzer analyzer = new IKAnalyzer();
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer)
					.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			writer = new IndexWriter(dir, indexWriterConfig);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * 关闭写索引
	 * 
	 * @param writer
	 */
	private static void closeIndexWriter(IndexWriter writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	@Override
	public String test() {
		return "I'm work!!!";
	}
}