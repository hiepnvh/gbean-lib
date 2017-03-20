package com.bean.db;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import com.base.filter.*;
import com.bean.base.*;
import com.bean.db.ConditionClause.OrGroup;
import com.bean.annot.*;

/**
 * @author Anhta
 *
 */
public class OrderClause {
	
	List<String> _availCols;
	Class<? extends Bean> _beanClass;
	String _fieldsOrder;
	Map<String, String> _orderFields;
	
	public OrderClause(BeanFilter beanFilter) throws Exception {
		_availCols = new ArrayList<String>();
		_orderFields = beanFilter.getfieldsOrder();
		_beanClass = beanFilter.getBeanClass();
		process();
	}
	
	public String getString() {
		return _fieldsOrder;
	}

	private void process() throws Exception {
		_fieldsOrder = "";
		BeanSchema metaInfo = BeanSchema.loadSchema(_beanClass);
		Set<String> _colsName = _orderFields.keySet();
		int i = 0;
		for (String col : _colsName) {
			if (metaInfo.getCols().contains(col)) {
				if (i == 0)
					_fieldsOrder += col + " " + _orderFields.get(col);
				else
					_fieldsOrder += ", " + col + " " + _orderFields.get(col);
				i += 1;
			}
		}
	}
}
