package com.bean.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.base.filter.*;
import com.bean.base.*;
import com.bean.annot.*;

public class DataSource<T extends Bean> {

	List<String> _availCols;
	Class<T> _beanClass;
	BeanSchema _metaInfo;
	List<T> _beans;

	public DataSource(Class<T> beanClass) throws Exception {
		_beanClass = beanClass;
		_metaInfo = BeanSchema.loadSchema(_beanClass);
		setAllCol();
	}
	
	public Class getBeanClass() {
		return _beanClass;
	}

	public void setDataCol(List<String> cols) throws Exception {
		_availCols = new ArrayList<String>();		
		for (int i = 0; i < cols.size(); i++) {
			String col = cols.get(i);
			List<String> beanCols = new ArrayList<String>(_metaInfo.getCols());
			for (int j = 0; j < beanCols.size(); j++) {
				String beanCol = beanCols.get(j);
				if (beanCol.compareTo(col) == 0
						&& !_metaInfo.getFinalCols().contains(beanCol)) {
					_availCols.add(beanCol);
				}
			}
		}
	}


	private void setAllCol() throws Exception {
		_availCols = new ArrayList<String>();
		List<String> cols = new ArrayList<String> (_metaInfo.getCols());
		for (int i = 0; i < cols.size(); i++) {
			String col = cols.get(i);
			if (!_metaInfo.getFinalCols().contains(col)) {
				_availCols.add(col);
			}
		}
	}

	public void setSource(List<T> beans) throws Exception {		
		for (Bean bean : beans)
			if (bean.getClass().getName().compareTo(_beanClass.getName()) != 0)
				throw new Exception("Bean class mismatch");
		_beans = beans;
	}
	
	public void setSource(T bean) throws Exception {		
		setSource(Arrays.asList(bean));
	}


	public int getColumnCount() {
		return _availCols.size();
	}

	public String getColumn(int slot) {
		return _availCols.get(slot);
	}

	public int getBeanCount() {
		return _beans.size();
	}

	public T getBean(int slot) {
		return _beans.get(slot);
	}
	
}
