package com.base.filter;

import java.util.*;

public abstract class LikeFilter<T> extends BaseFilter<T> {

	public LikeFilter(T val) {
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