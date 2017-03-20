package com.base.filter;

public abstract class GreaterThanOrEqualFilter<T> extends BaseFilter<T> {

	public GreaterThanOrEqualFilter(T val) {
		_value = val;
	}

	protected T _value;

	public void setValue(T val) {
		_value = val;
	}

	public T getValue() {
		return _value;
	}

}
