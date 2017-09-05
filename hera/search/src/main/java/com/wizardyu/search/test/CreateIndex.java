package com.wizardyu.search.test;
import static org.apache.lucene.document.TextField.TYPE_STORED;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LegacyLongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
  
  /**
 * Created by MoSon on 2017/6/30.
 */
  public class CreateIndex {
  
    public static void main(String[] args) throws IOException {
        //定义IndexWriter
        //index是一个相对路径，当前工程
        Path path = FileSystems.getDefault().getPath("", "index");
        Directory directory = FSDirectory.open(path);
        //定义分词器
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
  
        //定义文档
        Document document = new Document();
        //定义文档字段
        document.add(new LegacyLongField("id", 5499, Field.Store.YES));
        document.add(new Field("title", "小米6", TYPE_STORED));
        document.add(new Field("sellPoint", "骁龙835，6G内存，双摄！", TYPE_STORED));
        //写入数据
        indexWriter.addDocument(document);
        //添加新的数据
        document = new Document();
        document.add(new LegacyLongField("id", 8324, Field.Store.YES));
        document.add(new Field("title", "OnePlus5", TYPE_STORED));
        document.add(new Field("sellPoint", "8核，8G运行内存", TYPE_STORED));
        indexWriter.addDocument(document);
        //提交
        indexWriter.commit();
        //关闭
        indexWriter.close();
  
    }
   
  
}