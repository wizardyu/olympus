
package com.wizardyu.dubbo.provider.search.impl;

import static org.apache.lucene.document.TextField.TYPE_STORED;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.wizardyu.olympus.hera.search.LuceneService;
import com.wizardyu.olympus.hera.search.domain.LuceneFieldVO;

@Service("luceneService")
public class LuceneServiceImpl implements LuceneService {
	private Analyzer analyzer;
	private static int resultTotalNum = 0;
	private static int maxPageNum = 0;
	private static String indexPath = "/data/lucene/";
	private static Map<String, Directory> dirMap;

	public LuceneServiceImpl() {
		analyzer = new IKAnalyzer();
		dirMap = new HashMap<String, Directory>();
	}

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
		// getIndexWriter("fangtan");
		// System.out.println(dir.toString());
	}

	/**
	 * 创建索引方法
	 * 
	 * @return
	 */
	private IndexWriter getIndexWriter(String appName) {
		File indexFile = new File(indexPath + appName);
		Path path = indexFile.toPath();
		IndexWriter writer = null;
		Directory dir = null;
		try {
			if (dirMap.containsKey(appName) && dirMap.get(appName) != null) {
				dir = dirMap.get(appName);
			} else {
				dir = FSDirectory.open(path);
			}
			dirMap.put(appName, dir);
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
	 * 
	 * Description：查询
	 * 
	 * @author dennisit@163.com Apr 3, 2013
	 * @param where
	 *            查询条件
	 * @param scoreDoc
	 *            分页时用
	 */
	public List<LuceneFieldVO> search(String[] fields, String keyword, String appName) {

		IndexSearcher indexSearcher = null;
		List<LuceneFieldVO> result = new ArrayList<LuceneFieldVO>();
		try {
			// 创建索引搜索器,且只读
			// IndexReader indexReader = IndexReader.open(dir,true);
			System.out.println(dirMap.get(appName).toString());
			IndexReader indexReader = DirectoryReader.open(dirMap.get(appName));

			indexSearcher = new IndexSearcher(indexReader);
			
//			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
			QueryParser queryParser=new MultiFieldQueryParser(fields,analyzer);
			Query query = queryParser.parse(keyword);
//			Query query = new MatchAllDocsQuery();	

			// 返回前number条记录
			TopDocs topDocs = indexSearcher.search(query, 10);
			// 信息展示
			int totalCount = topDocs.totalHits;
			System.out.println("共检索出 " + totalCount + " 条记录");

			// 高亮显示
			/*
			 * 创建高亮器,使搜索的结果高亮显示 SimpleHTMLFormatter：用来控制你要加亮的关键字的高亮方式 此类有2个构造方法
			 * 1：SimpleHTMLFormatter()默认的构造方法.加亮方式：<B>关键字</B> 2：SimpleHTMLFormatter(String
			 * preTag, String postTag).加亮方式：preTag关键字postTag
			 */
			Formatter formatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
			/*
			 * QueryScorer QueryScorer 是内置的计分器。计分器的工作首先是将片段排序。QueryScorer使用的项是从用户输入的查询中得到的；
			 * 它会从原始输入的单词、词组和布尔查询中提取项，并且基于相应的加权因子（boost factor）给它们加权。
			 * 为了便于QueryScoere使用，还必须对查询的原始形式进行重写。 比如，带通配符查询、模糊查询、前缀查询以及范围查询
			 * 等，都被重写为BoolenaQuery中所使用的项。 在将Query实例传递到QueryScorer之前，可以调用Query.rewrite
			 * (IndexReader)方法来重写Query对象
			 */
			Scorer fragmentScorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(formatter, fragmentScorer);
			Fragmenter fragmenter = new SimpleFragmenter(100);
			/*
			 * Highlighter利用Fragmenter将原始文本分割成多个片段。
			 * 内置的SimpleFragmenter将原始文本分割成相同大小的片段，片段默认的大小为100个字符。这个大小是可控制的。
			 */
			highlighter.setTextFragmenter(fragmenter);

			ScoreDoc[] scoreDocs = topDocs.scoreDocs;

			for (ScoreDoc scDoc : scoreDocs) {
				Document document = indexSearcher.doc(scDoc.doc);
//				Integer id = Integer.parseInt(document.get("id"));
				String fieldName = document.get("fieldName");
				String fieldValue = document.get("fieldValue");
				// float score = scDoc.score; //相似度
				System.out.println(fieldName);

				String lighterName = highlighter.getBestFragment(analyzer, "fieldName", fieldName);
				if (null == lighterName) {
					lighterName = fieldName;
				}

				String lighterFunciton = highlighter.getBestFragment(analyzer, "fieldValue", fieldValue);
				if (null == lighterFunciton) {
					lighterFunciton = fieldValue;
				}

				LuceneFieldVO luceneFieldVO = new LuceneFieldVO();

				// luceneFieldVO.setId(id);
				// luceneFieldVO.setName(lighterName);
				// luceneFieldVO.setFunction(lighterFunciton);

				result.add(luceneFieldVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// try {
			// indexSearcher.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}

		return result;
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