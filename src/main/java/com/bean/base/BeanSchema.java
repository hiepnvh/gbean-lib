package com.bean.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import com.bean.annot.*;



public class BeanSchema implements Serializable {


	protected static  Map<String, BeanSchema> BEAN_META_INFO;

	static {
		BEAN_META_INFO = new HashMap<String, BeanSchema>();
	}

	public static BeanSchema loadSchema(Class<? extends Bean> cls)
			throws Exception {
		if (!BEAN_META_INFO.containsKey(cls.getName())) {
			BeanSchema metaInfo = new BeanSchema(cls);
			BEAN_META_INFO.put(cls.getName(), metaInfo);
		}
		return BEAN_META_INFO.get(cls.getName());
	}

	String _table;
	Map<String, BeanProperty<?>> _propMap;
	Map<Integer, String> _colMap;
	Set<String> _cols;
	Set<String> _finalCols;
	Set<String> _extKeyCols;
	Set<String> _autoIncrementCols;
	Class<? extends Bean> _beanClass;

	private BeanSchema(Class<? extends Bean> beanClass) throws Exception {
		_beanClass = beanClass;
		_cols = new HashSet<String>();
		_finalCols = new HashSet<String>();
		_extKeyCols = new HashSet<String>();
		_autoIncrementCols = new HashSet<String>();
		_propMap =  new HashMap<String,BeanProperty<?>>();
		_colMap =  new HashMap<Integer,String>();
		_table = beanClass.getAnnotation(Entity.class).name();
		
		Field [] declaredFields = beanClass.getDeclaredFields();
		for (Field field : declaredFields) {
		    if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(BeanProperty.class)) {
		    	BeanProperty<?> beanProp = (BeanProperty<?> ) field.get(null);
		    	Attribute c = field.getAnnotation(Attribute.class);
				if (c!=null) {
					_cols.add(c.name());
					_propMap.put(c.name(), beanProp);
					_colMap.put(beanProp.hashCode(), c.name());
					Final fin = field.getAnnotation(Final.class);
					if (fin!=null)
						_finalCols.add(c.name());
					ExternalKey extKey = field.getAnnotation(ExternalKey.class);
					if (extKey!=null)
						_extKeyCols.add(c.name());
					AutoIncrement autoIncrement = field.getAnnotation(AutoIncrement.class);
					if (autoIncrement!=null)
						_autoIncrementCols.add(c.name());
				}
		    }
		}
		
	}

	public String getTable() {
		return _table;
	}
	
	/**
	 * @param beanProp
	 * @return column name in Database
	 */
	public  String getPropertyCol( BeanProperty<?> beanProp) {
		return _colMap.get(beanProp.hashCode());
	}
	
	public  BeanProperty<?> getProperty( String col) {
		return _propMap.get(col);
	}



	public Set<String> getCols() {
		return new HashSet<String>(_cols);
	}

	public Set<String> getExtKeyCols() {
		return new HashSet<String>(_extKeyCols);
	}

	public Set<String> getAutoIncrementCols() {
		return new HashSet<String>(_autoIncrementCols);
	}


	public  Set<String>  getFinalCols() {
		return new HashSet<String>(_finalCols);
	}

	


}
