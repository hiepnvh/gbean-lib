package com.bean.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Bean implements Serializable {
	public interface BeanVisitor {
		public void visit(Bean b) throws Exception;
	}

	public void visit(BeanVisitor v) throws Exception {
		v.visit(this);
	}
	
	private Map<String,BeanProperty<?>> _propMap;
	private BeanSchema _schema;
	
	public Bean() {
		_propMap = new HashMap<String,BeanProperty<?>>();
		try {
			_schema = BeanSchema.loadSchema(this.getClass());
			for(String col : _schema.getCols()) {
				BeanProperty<?> propPrototype = _schema.getProperty(col);
				BeanProperty<?> beanProp = propPrototype.getClass().newInstance();
				_propMap.put(col, beanProp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public <T> void set(BeanProperty<T> propPrototype, T val) {
		String col = _schema.getPropertyCol(propPrototype);
		BeanProperty<T> beanProp =(BeanProperty<T>) _propMap.get(col);
		beanProp.set(val);
	}
	
	public <T> T get(BeanProperty<T> propPrototype) {
		String col = _schema.getPropertyCol(propPrototype);
		BeanProperty<T> beanProp =(BeanProperty<T>) _propMap.get(col);
		return beanProp.get();
	}
	
	public boolean isSet(BeanProperty propPrototype) {
		String col = _schema.getPropertyCol(propPrototype);
		BeanProperty beanProp = _propMap.get(col);
		return beanProp.isSet();
	}
	

}
