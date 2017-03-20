package com.bean.base;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.base.filter.*;
import com.bean.db.DbParams;

public  class BeanFilter {
	
	private static Logger LOGGER = Logger.getLogger(BeanFilter.class.getName());
	
	private Map<String,FilterCriteria> _criteriaMap;
	private BeanSchema _schema;
	private Class<? extends Bean> _beanClass;
	private int limit, offset;
	private Map<String, String> _orderPops;
	
	public  BeanFilter(Class<? extends Bean> beanClass) {
		_beanClass = beanClass;
		limit = 0; offset = 0;
		_criteriaMap = new HashMap<String,FilterCriteria>();
		_orderPops = new HashMap<String, String>();
		try {
			_schema = BeanSchema.loadSchema(beanClass);
			for(String col : _schema.getCols()) {
				_criteriaMap.put(col, new FilterCriteria());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Class<? extends Bean> getBeanClass() {
		return _beanClass;
	}
	
	
	public void setFilter(BeanProperty<?> beanProp, Filter<?> filter) {
		String col = _schema.getPropertyCol(beanProp);
		FilterCriteria criteria = _criteriaMap.get(col);
		criteria.addFilter(filter);
	}
	
	public void setFilterCriteria(BeanProperty<?> beanProp,FilterCriteria criteria) {
		String col = _schema.getPropertyCol(beanProp);
		_criteriaMap.put(col, criteria);
	}
	
	public FilterCriteria getFilterCriteria(BeanProperty<?> beanProp) {
		String col = _schema.getPropertyCol(beanProp);
		FilterCriteria criteria = _criteriaMap.get(col);
		return criteria;
	}
	
	
	public int getLimit() {
		return limit;
	}
	public String getLimitString() {
		return offset+","+limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public void setLimit(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}
	public Map<String, String> getfieldsOrder(){
		return _orderPops;
	}
	/**
	 * @param beanProp
	 * @param orderType is DbParams.ORDER_TYPE.DESC, or DbParams.ORDER_TYPE.ASC
	 */
	public void setFieldOrder(BeanProperty<?> beanProp, String orderType){
		String col = _schema.getPropertyCol(beanProp);
		_orderPops.put(col, orderType);
	}
}
