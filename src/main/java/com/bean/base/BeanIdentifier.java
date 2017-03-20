package com.bean.base;

import java.util.*;

import com.bean.base.Bean;
import com.bean.annot.*;


public class BeanIdentifier<T extends Bean> {


	Class<T> _beanClass;
	Map<String, Object> _keyValues;
	Set<String> _keys;
	BeanSchema _metaInfo;
	String _id;

	public String getId() {
		return _id;
	}

	public BeanIdentifier(Class<T> beanClass) throws Exception {
		_beanClass = beanClass;
		_keyValues = new Hashtable<String, Object>();
		_keys = new HashSet<String>();
		_metaInfo = BeanSchema.loadSchema(_beanClass);
		Set<String> extKeys = _metaInfo.getExtKeyCols();
		for (String col:extKeys) {
			addKey(col);
		}
	}

	public Class<T> getBeanClass() {
		return _beanClass;
	}

	public boolean setValue(Bean bean) throws Exception {
		if (bean.getClass().getName().compareTo(_beanClass.getName()) != 0)
			throw new Exception("Source and target class inconsistent");
		_id = "";
		if (_keys.size() == 0) {
			return false;
		}
		for (String col:_keys) {
			if (_keys.contains(col)) {
				BeanProperty<?> beanProp = _metaInfo.getProperty(col);
				Object beanVal = bean.get(beanProp);				
				if (beanVal != null)
					setKeyValue(col, beanVal);
				_id += col + beanVal;
			}
		}
		return true;
	}

	public Object getValue(String col) {
		return _keyValues.get(col);
	}

	public void setKeyValue(String col, Object obj) {
		_keyValues.put(col, obj);
		addKey(col);
	}

	private void addKey(String col) {
		_keys.add(col);
	}
	
	public Set<String> getKey() {
		return new HashSet<String>(_keys);
	}



}
