
package com.wizardyu.dubbo.provider.search.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
import org.springframework.stereotype.Service;
import org.wltea.analyzer.core.IKSegmenter;
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

	public String updateDocument(List<List<LuceneFieldVO>> luceneFieldVOList, String appName) {
		String msg = null;
		IndexWriter writer = getIndexWriter(appName);
		try {
			if (luceneFieldVOList != null) {
				for (int i = 0; i < luceneFieldVOList.size(); i++) {
					List<LuceneFieldVO> lvolist = luceneFieldVOList.get(i);
					Document doc = new Document();
					String idvalue = "";
					for (int j = 0; j < lvolist.size(); j++) {
						LuceneFieldVO vo = (LuceneFieldVO) lvolist.get(j);
						if (vo.getFieldValue() == null || vo.getFieldValue().isEmpty()) {
							vo.setFieldValue("");
						}
						doc.add(vo.getFieldByType());
						// 存储
						if (vo.getFieldType() == LuceneFieldVO.TYPE_INT
								|| vo.getFieldType() == LuceneFieldVO.TYPE_LONG) {
							doc.add(new StoredField(vo.getFieldName(), vo.getFieldValue()));
						}
						if (vo.isCanSort()) {
							doc.add(new NumericDocValuesField(vo.getFieldName(), Long.valueOf(vo.getFieldValue())));
						}
						if (vo.getFieldName().equals("id")) {
							idvalue = vo.getFieldValue();
						}
					}
					writer.updateDocument(new Term("id", idvalue), doc);
				}
			}
			writer.commit();
		} catch (Exception e) {
			msg = "update Index failed , postId=" + luceneFieldVOList.get(0).get(0).getFieldValue();
			e.printStackTrace();
		} finally {
			closeIndexWriter(writer);
		}
		return msg;
	}

	@Override
	public void createDocument(List<LuceneFieldVO> luceneFieldVOList, String appName) {
		IndexWriter writer = getIndexWriter(appName);

		try {
			Document doc = new Document();
			if (luceneFieldVOList != null) {
				String idvalue = "";
				for (int j = 0; j < luceneFieldVOList.size(); j++) {
					LuceneFieldVO vo = (LuceneFieldVO) luceneFieldVOList.get(j);
					if (vo.getFieldValue() == null || vo.getFieldValue().isEmpty()) {
						vo.setFieldValue("");
					}
					doc.add(vo.getFieldByType());
					// 存储
					if (vo.getFieldType() == LuceneFieldVO.TYPE_INT || vo.getFieldType() == LuceneFieldVO.TYPE_LONG) {
						doc.add(new StoredField(vo.getFieldName(), vo.getFieldValue()));
					}
					if (vo.isCanSort()) {
						doc.add(new NumericDocValuesField(vo.getFieldName(), Long.valueOf(vo.getFieldValue())));
					}
					if (vo.getFieldName().equals("id")) {
						idvalue = vo.getFieldValue();
					}
					// LuceneFieldVO vo = (LuceneFieldVO) luceneFieldVOList.get(i);
					// doc.add(new Field(vo.getFieldName(), vo.getFieldValue(), TYPE_STORED));
				}
				writer.updateDocument(new Term("id", idvalue), doc);
			}
			// writer.addDocument(doc);
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
			if (analyzer == null) {
				analyzer = new IKAnalyzer();
			}
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
	public List<List<LuceneFieldVO>> search(String appName, String[] fields, BooleanClause.Occur[] clauses,
			SortField sortField, String searchStr, int pageSize, int pageNum, int maxSize) {
		List<List<LuceneFieldVO>> result = new ArrayList<List<LuceneFieldVO>>();
		IndexSearcher indexSearcher = null;
		try {
			// 创建索引搜索器,且只读
			IndexReader indexReader = DirectoryReader.open(dirMap.get(appName));

			indexSearcher = new IndexSearcher(indexReader);
			Query query = MultiFieldQueryParser.parse(searchStr, fields, clauses, analyzer);

			Sort sort = new Sort(SortField.FIELD_SCORE, sortField);
			// 返回前number条记录
			TopDocs topDocs = indexSearcher.search(query, maxSize, sort);
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
			// System.out.println("总条数: " + total + "");
			int maxPageNum = getMaxPageNum(totalCount, pageSize);
			if (pageNum > maxPageNum) {
				pageNum = maxPageNum;
			}

			setResultTotalNum(totalCount);
			setMaxPageNum(maxPageNum);

			int start = pageSize * pageNum;
			int end = pageSize * pageNum + pageSize - 1;
			if (end > totalCount - 1) {
				end = totalCount - 1;
			}
			if (start > totalCount - 1) {
				start = start - pageSize;
			}
			if (topDocs.totalHits > 0) {
				for (int i = start; i <= end; i++) {
					ScoreDoc sr = scoreDocs[i];
					int docID = sr.doc;
					Document doc = indexSearcher.doc(docID);
					List<LuceneFieldVO> lvolist = new ArrayList<LuceneFieldVO>();
					for (int w = 0; w < fields.length; w++) {
						String fieldName = fields[w];
						System.out.println(fieldName + ":" + analyzer + ":" + doc);
						TokenStream tokenStream1 = analyzer.tokenStream(fieldName,
								new StringReader(doc.get(fieldName)));
						String highlighterStr1 = highlighter.getBestFragment(tokenStream1, doc.get(fieldName));
						String value = highlighterStr1 == null ? doc.get(fieldName) : highlighterStr1;
						LuceneFieldVO vo1 = new LuceneFieldVO();
						vo1.setFieldName(fieldName);
						vo1.setFieldValue(value);
						lvolist.add(vo1);
						// 把ID也查出来
						String fieldName2 = "id";
						TokenStream tokenStream2 = analyzer.tokenStream(fieldName2,
								new StringReader(doc.get(fieldName2)));
						String highlighterStr2 = highlighter.getBestFragment(tokenStream2, doc.get(fieldName2));
						String value2 = highlighterStr2 == null ? doc.get(fieldName2) : highlighterStr2;
						LuceneFieldVO vo2 = new LuceneFieldVO();
						vo2.setFieldName(fieldName2);
						vo2.setFieldValue(value2);
						lvolist.add(vo2);

					}

					result.add(lvolist);

				}
			}
			//
			// for (ScoreDoc scDoc : scoreDocs) {
			// Document document = indexSearcher.doc(scDoc.doc);
			// // Integer id = Integer.parseInt(document.get("id"));
			// String fieldName = document.get("fieldName");
			// String fieldValue = document.get("fieldValue");
			// // float score = scDoc.score; //相似度
			// System.out.println(fieldName + "fieldValue[" + fieldValue + "]");
			//
			// String lighterName = highlighter.getBestFragment(analyzer,
			// "fieldName", fieldName);
			// if (null == lighterName) {
			// lighterName = fieldName;
			// }
			//
			// String lighterFunciton = highlighter.getBestFragment(analyzer,
			// "fieldValue", fieldValue);
			// if (null == lighterFunciton) {
			// lighterFunciton = fieldValue;
			// }
			//
			// // LuceneFieldVO luceneFieldVO = new LuceneFieldVO();
			// //
			// // // luceneFieldVO.setId(id);
			// // // luceneFieldVO.setName(lighterName);
			// // // luceneFieldVO.setFunction(lighterFunciton);
			// //
			// // result.add(luceneFieldVO);
			// }
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

	private static int getMaxPageNum(int total, int pageSize) {
		int maxPageNum = total / pageSize;
		if (total % pageSize > 0) {
			maxPageNum = maxPageNum + 1;
		}
		return maxPageNum;
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

	public static int getResultTotalNum() {
		return resultTotalNum;
	}

	public static void setResultTotalNum(int resultTotalNum) {
		LuceneServiceImpl.resultTotalNum = resultTotalNum;
	}

	public static int getMaxPageNum() {
		return maxPageNum;
	}

	public static void setMaxPageNum(int maxPageNum) {
		LuceneServiceImpl.maxPageNum = maxPageNum;
	}

	@Override
	public String test() {
		return "I'm work!!!";
	}
}