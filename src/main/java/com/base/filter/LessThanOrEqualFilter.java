package com.base.filter;

public abstract class LessThanOrEqualFilter<T> extends BaseFilter<T> {

	public LessThanOrEqualFilter(T val) {
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
