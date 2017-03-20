package com.base.filter;

import com.bean.base.Parametrizer;

public abstract class BaseFilter<T> extends Parametrizer implements
		Filter<T> {
	
	protected Boolean _negated = false;
	protected String _group = null;

	@Override
	public Boolean getNegated() {
		return _negated;
	}

	@Override
	public void setNegated(Boolean val) {
		_negated = val;
	}
	
	@Override
	public String getOrGroup() {
		return _group;
	}

	@Override
	public void setOrGroup(String val) {
		_group = val;
	}

}
