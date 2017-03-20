package com.base.filter;

import java.util.*;

public abstract class InFilter<T> extends BaseFilter<T> {

	public InFilter(Set<T> val) throws Exception {
		if (val==null || val.size()==0)
			throw new Exception ("Invalid in list");
		_value = val;
	}

	protected Set<T> _value;

	public void setValue(Set<T> val) {
		_value = val;
	}

	public Set<T> getValue() {
		return _value;
	}

}