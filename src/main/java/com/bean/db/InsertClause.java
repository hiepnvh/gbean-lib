package com.bean.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import com.base.filter.*;
import com.bean.base.*;
import com.bean.annot.*;

public class InsertClause {
	
	String _colStr;
	String _valStr;
	List<String> _cols;

	public InsertClause(DataSource dataSource) throws Exception {
		_colStr = "";
		_valStr = "";
		_cols =  new ArrayList<String>();
		Class beanClass = dataSource.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		String autoIncrementCol = "";
		if((metaInfo.getAutoIncrementCols() != null) && (metaInfo.getAutoIncrementCols().size()>0))
			autoIncrementCol = metaInfo.getAutoIncrementCols().toArray()[0].toString();
		for (int i = 0; i < dataSource.getColumnCount(); i++) {
			String col = dataSource.getColumn(i);
			//Add col if it is'nt auto increment or quantity column
			if ((col != autoIncrementCol)&&(!col.equalsIgnoreCase("quantity"))){
				_cols.add(col);
				if (_colStr.length() == 0) {
					_colStr = col;
					_valStr = "?";
				} else {
					_colStr += "," + col;
					_valStr += ",?";
				}
			}
			
		}
		_colStr = "(" + _colStr + ")";
		_valStr = "(" + _valStr + ")";

	}


	public String getColumnString() {
		return _colStr;
	}

	public String getValueString() {
		return _valStr;
	}

	public int getColoumnCount() {
		return _cols.size();
	}

	public String getColumn(int slot) {
		return _cols.get(slot);
	}


}
