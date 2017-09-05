package com.wizardyu.search.test;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * Created by MoSon on 2017/7/1.
 */
public class SearchPage {

    public static void main(String[] args)throws IOException,ParseException {
        //定义索引目录
        Path path = FileSystems.getDefault().getPath("index");
        System.out.println(path);
        Directory directory = FSDirectory.open(path);
        //定义索引查看器
        IndexReader indexReader = DirectoryReader.open(directory);
        //定义搜索器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        
        //搜索内容
        

        //搜索关键字
        String  keyWords = "骁龙";

        //分页信息
        Integer page = 1;
        Integer pageSize = 20;
        Integer start = (page-1) * pageSize;
        Integer end = start + pageSize;

        Query query = new QueryParser("sellPoint",new IKAnalyzer()).parse(keyWords);//模糊搜索

        //命中前10条文档
        TopDocs topDocs = indexSearcher.search(query,end);//根据end查询

        Integer totalPage = ((topDocs.totalHits/ pageSize) == 0)
                ? topDocs.totalHits/pageSize
                : ((topDocs.totalHits / pageSize) +1);

        System.out.println("“"+ keyWords + "”搜索到" + topDocs.totalHits
                + "条数据，页数：" + page + "/" + totalPage);
        //打印命中数
        System.out.println("命中数："+topDocs.totalHits);
        //取出文档
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        int length = scoreDocs.length> end ? end : scoreDocs.length;
        //遍历取出数据
        for (int i = start; i < length; i++){
            ScoreDoc doc = scoreDocs[i];
            System.out.println("得分："+ doc.score);
            Document document = indexSearcher.doc(doc.doc);
            System.out.println("ID:"+ document.get("id"));
            System.out.println("sellPoint:"+document.get("sellPoint"));
            System.out.println("-----------------------");
        }

        //关闭索引查看器
        indexReader.close();
    }
}