package com.bean.db;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import com.base.filter.*;
import com.bean.base.*;
import com.bean.annot.*;

public class ConditionClause {

	List<String> _availCols;
	List<FilterCriteria> _filterCriteria;
	String _condition;
	int _wildNum;
	List<Object> _wildObjects;
	Class<? extends Bean> _beanClass;
	Map<String,OrGroup> _orGroupMap;
	
	class OrGroup {
		List<String> _cols;
		List<Filter> _filters;
		
		OrGroup() {
			_cols = new ArrayList<String>();
			_filters = new ArrayList<Filter>();
		}
		
		public void add(String col, Filter filter) {
			_cols.add(col);
			_filters.add(filter);
		}
		
		public String processOrGroup() throws Exception {
			String condition ="";
			for (int i = 0; i < _cols.size(); i++) {
				String col = _cols.get(i);
				Filter filter = _filters.get(i);
				if (condition.length() == 0)
					condition = processFilter(col, filter);
				else
					condition += " OR " + processFilter(col, filter);
			}
			condition = "(" + condition + ")";
			return condition;
		}
	}

	public ConditionClause(BeanFilter beanFilter) throws Exception {
		_availCols = new ArrayList<String>();
		_orGroupMap = new HashMap<String,OrGroup>();
		_filterCriteria = new ArrayList<FilterCriteria>();
		_beanClass = beanFilter.getBeanClass();
		Class filterClass = beanFilter.getClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(_beanClass);
		for (String col : metaInfo.getCols()) {
			BeanProperty beanProp = metaInfo.getProperty(col);
			FilterCriteria colFilter = (FilterCriteria) beanFilter.getFilterCriteria(beanProp);
			_availCols.add(col);
			_filterCriteria.add(colFilter);
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
	
	private String processInFilter(InFilter inFilter, String table, String col ) {
		String condition = table + "." + col ;
		int size = inFilter.getValue().size();
		List<String> strList = new ArrayList<String>();
		if (size==0)
			return " 1=1 ";
		while (size>0) {
			int length = size;
			if (size >DbParams.MAX_IN_LIST_SIE)
				length =DbParams.MAX_IN_LIST_SIE;
			size -= DbParams.MAX_IN_LIST_SIE;
			String setStr ="?";
			for (int i=1;i<length;i++)
				setStr +=",?";
			strList.add(setStr);
		}
		condition += " IN (" + strList.get(0)+")";
		for (int i=1;i<strList.size();i++)
			condition += " OR  "+table + "." + col +" IN ("+ strList.get(i) +") ";
		Iterator itr = inFilter.getValue().iterator();
		while (itr.hasNext()) {
			_wildObjects.add(itr.next());
			_wildNum++;
		}
		return condition;
	}

	private String processFilter(String col, Filter filter) throws Exception {
		BeanSchema metaInfo = BeanSchema.loadSchema(_beanClass);
		String table = metaInfo.getTable();
		String subCondition = "";
		if (filter instanceof EqualFilter) {
			EqualFilter equalFilter = (EqualFilter) filter;
			subCondition = table + "." + col + " = ?";
			_wildObjects.add(equalFilter.getValue());
			_wildNum++;
		} else if (filter instanceof LikeFilter) {
			LikeFilter likeFilter = (LikeFilter) filter;
			subCondition = table + "." + col + " LIKE ?";
			_wildObjects.add(likeFilter.getValue());
			_wildNum++;
		} else if (filter instanceof GreaterThanOrEqualFilter) {
			GreaterThanOrEqualFilter gOrEqualFilter = (GreaterThanOrEqualFilter) filter;
			subCondition = table + "." + col + " >= ?";
			_wildObjects.add(gOrEqualFilter.getValue());
			_wildNum++;
		} else if (filter instanceof LessThanOrEqualFilter) {
			LessThanOrEqualFilter lOrEqualFilter = (LessThanOrEqualFilter) filter;
			subCondition = table + "." + col + " <= ?";
			_wildObjects.add(lOrEqualFilter.getValue());
			_wildNum++;

		} else if (filter instanceof InFilter) {
			InFilter inFilter = (InFilter) filter;
			subCondition += "(" + processInFilter(inFilter,table,col) + ")";
		} else if (filter instanceof IsNullFilter) {
			subCondition +=  col +" IS NULL ";
		}

		if (filter.getNegated())
			subCondition = " NOT (" + subCondition + ")";
		return subCondition;
	}

	private void process() throws Exception {
		_condition = "";
		_wildNum = 0;
		_wildObjects = new ArrayList<Object>();
		for (int i = 0; i < _availCols.size(); i++) {
			String col = _availCols.get(i);
			FilterCriteria criteria = _filterCriteria.get(i);
			List<Filter> filters = criteria.getFilters();
			for (int j = 0; j < filters.size(); j++) {
				Filter filter = filters.get(j);
				if (!(filter instanceof InnerJoinFilter)) {
					if (filter.getOrGroup()!=null) {
						OrGroup group = _orGroupMap.get(filter.getOrGroup());
						if (group == null) {
							group = new OrGroup();
							_orGroupMap.put(filter.getOrGroup(), group);
						}
						group.add(col, filter);
					} else {
						if (_condition.length() == 0)
							_condition = processFilter(col, filter);
						else
							_condition += " AND " + processFilter(col, filter);
					}
				}
			}
		}
		for (OrGroup group : _orGroupMap.values()) {
			if (_condition.length() == 0)
				_condition = group.processOrGroup();
			else
				_condition += " AND " + group.processOrGroup();
		}
		
		
		if (_condition.length() == 0)
			_condition = " 1=1  ";
		_condition = "(" + _condition + ")";
	}
}
