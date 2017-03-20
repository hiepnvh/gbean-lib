package com.bean.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.bean.base.*;
import com.base.filter.*;
import com.bean.annot.*;

public class ExistConditionClause<T extends Bean> {


	List<String> _availCols;
	String _condition;
	int _wildNum;
	List<Object> _wildObjects;
	BeanIdentifier<T> _identifier;

	public ExistConditionClause(BeanIdentifier<T> identifier, boolean nullIgnored)
			throws Exception {
		_identifier = identifier;
		_availCols = new ArrayList<String>();
		_wildObjects = new ArrayList<Object>();
		for (String col : identifier.getKey()) {
			Object val = identifier.getValue(col);
			if (nullIgnored) {
				_wildNum++;
				_availCols.add(col);
				_wildObjects.add(val);
			} else if (val != null) {
				_wildNum++;
				_availCols.add(col);
				_wildObjects.add(val);
			}
		}
		BeanSchema metaInfo = BeanSchema.loadSchema(identifier.getBeanClass());
		process(metaInfo);
	}

	public void setBean(Bean bean) throws Exception {
		_identifier.setValue(bean);
		_wildObjects = new ArrayList<Object>();
		for (int i = 0; i < _availCols.size(); i++) {
			String col = _availCols.get(i);
			Object val = _identifier.getValue(col);
			_wildObjects.add(val);
		}
	}

	private void process(BeanSchema metaInfo) {
		String table = metaInfo.getTable();
		_condition = "";
		for (int i = 0; i < _availCols.size(); i++) {
			String col = _availCols.get(i);
			if (i == 0)
				_condition = table + "." + col + "= ? ";
			else
				_condition += " AND " + table + "." + col + "= ? ";
		}
		if (_condition.length() == 0)
			_condition = " 1=0  ";
		else
			_condition = "( " + _condition + ") ";
	}

	public String getString() {
		return _condition;
	}

	public int getWildcardNumber() {
		return _wildNum;
	}

	public Object getWildcardValueAt(int slot) {
		return _wildObjects.get(slot);
	}

}
