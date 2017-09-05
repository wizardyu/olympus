/** 
 * <!-- 索引字段VO -->
 * Create on 2013-8-4
 * Copyright 2013 peopleBBS All Rights Reserved.
 */
package com.wizardyu.olympus.hera.search.domain;

public class LuceneFieldVO {

//	public static final int TYPE_INT = SortField.STRING_FIRST;       //排序字段类型-INT
//	public static final int TYPE_LONG = SortField.LONG;     //排序字段类型-LONG
//	public static final int TYPE_STRING = SortField.STRING; //排序字段类型-STRING
	
	private String fieldName;          //索引文件中的字段名
	private String fieldValue;         //索引文件中的字段值
	private int fieldType;             //字段类型（值来自于上面声明的静态常量）
	private boolean isCanSearch;       //是否能被索引
	private boolean isCanSort = false; //是否按这个字段排序
	private boolean isDesc = true;     //排序类型 -true:desc  false:asc 
	
	public LuceneFieldVO(){
		super();
	}
	public LuceneFieldVO(String fieldName,String fieldValue,int fieldType,boolean isCanSearch,boolean isCanSort,boolean isDesc){
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.fieldType = fieldType;
		this.isCanSearch = isCanSearch;
		this.isCanSort = isCanSort;
		this.isDesc = isDesc;
	}
	
//	public int getFieldType() {
//		if(fieldType==TYPE_INT){
//			return TYPE_INT;
//		}else if(fieldType==TYPE_LONG){
//			return TYPE_LONG;
//		}else{
//			return TYPE_STRING;
//		}
//	}
	public void setFieldType(int fieldType) {
		this.fieldType = fieldType;
	}
	public boolean isCanSort() {
		return isCanSort;
	}
	public void setCanSort(boolean isCanSort) {
		this.isCanSort = isCanSort;
	}
	public boolean isDesc() {
		return isDesc;
	}
	public void setDesc(boolean isDesc) {
		this.isDesc = isDesc;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldValue() {
		return fieldValue;
	}
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
//	public Index getFieldIndexAnalyzed() {
//		if(isCanSearch){
//			return Field.Index.ANALYZED;
//		}else{
//			return Field.Index.NOT_ANALYZED;
//		}
//	}
	public boolean isCanSearch() {
		return isCanSearch;
	}
	public void setCanSearch(boolean isCanSearch) {
		this.isCanSearch = isCanSearch;
	}
	
	
}

