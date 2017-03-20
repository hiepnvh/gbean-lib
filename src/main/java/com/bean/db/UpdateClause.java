package com.bean.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.base.filter.*;
import com.bean.base.*;
import com.bean.annot.*;

public class UpdateClause {

	List<String> _cols;
	String _setStr;
	

	public UpdateClause(DataSource dataSource) throws Exception {
		_setStr = "";
		_cols =  new ArrayList<String>();
		for (int i = 0; i < dataSource.getColumnCount(); i++) {
			String col = dataSource.getColumn(i);
			_cols.add(col);
			if (_setStr.length() == 0)
				_setStr = col + "=?";
			else
				_setStr += "," + col + "=?";			
		}
	}


	public String getSetString() {
		return _setStr;
	}

	public int getColumnCount() {
		return _cols.size();
	}

	public String getColumn(int slot) {
		return _cols.get(slot);
	}


}
