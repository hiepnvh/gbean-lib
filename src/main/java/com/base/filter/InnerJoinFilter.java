package com.base.filter;

public abstract class InnerJoinFilter<T> extends BaseFilter<T> {

	public InnerJoinFilter(T val) {
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
