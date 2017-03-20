package com.bean.db;

import java.lang.reflect.Method;
import java.util.*;

import com.bean.base.*;
import com.base.filter.*;
import com.bean.annot.*;

public class InnerJoinClause {

	List<String> _availCols;
	List<FilterCriteria> _filterCriteria;
	Map<InnerJoinFilter, String> _joinFilters;
	String _condition;
	int _wildNum;
	List<Object> _wildObjects;
	Class _beanClass;
	BeanSchema _metaInfo; 

	public InnerJoinClause(BeanFilter beanFilter) throws Exception {
		_availCols = new ArrayList<String>();
		_filterCriteria = new ArrayList<FilterCriteria>();
		_joinFilters = new HashMap<InnerJoinFilter, String>();
		_beanClass = beanFilter.getBeanClass();
		_metaInfo = BeanSchema.loadSchema(_beanClass);
		for (String col : _metaInfo.getCols()) {
			_availCols.add(col);
			BeanProperty<?> beanProp = _metaInfo.getProperty(col);
			FilterCriteria criteria = beanFilter.getFilterCriteria(beanProp);
			_filterCriteria.add(criteria);
		}
		process();
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

	private String processInnerJoinFilter() throws Exception {
		String join = "";
		for (InnerJoinFilter filter: _joinFilters.keySet()) {
			BeanFilter joinFilter = (BeanFilter) filter.getValue();
			String joinCol = _joinFilters.get(filter);
			Class<? extends Bean> joinClass = joinFilter.getBeanClass();
			BeanSchema joinMetaInfo = BeanSchema.loadSchema(joinClass);
			String beanTable = _metaInfo.getTable();
			String joinTable = joinMetaInfo.getTable();

			ConditionClause conditionOnJoinTable = new ConditionClause(joinFilter);
			join += " INNER JOIN " + joinTable + " ON "
					+ beanTable + "." + joinCol + "="
					+ joinTable + "." + joinCol + " AND "
					+ conditionOnJoinTable.getString();
			for (int i = 0; i < conditionOnJoinTable.getWildcardNumber(); i++) {
				_wildObjects.add(conditionOnJoinTable.getWildcardValueAt(i));
				_wildNum++;
			}
		}
		return join;
	}

	private void process() throws Exception {
		_wildNum = 0;
		_wildObjects = new ArrayList<Object>();
		for (int i = 0; i < _availCols.size(); i++) {
			String col = _availCols.get(i);
			FilterCriteria criteria = _filterCriteria.get(i);
			List<Filter> filters = criteria.getFilters();
			for (int j = 0; j < filters.size(); j++) {
				Filter filter = filters.get(j);
				if (filter instanceof InnerJoinFilter)
					_joinFilters.put((InnerJoinFilter) filter, col);
			}
		}
		_condition = processInnerJoinFilter();
	}
}
