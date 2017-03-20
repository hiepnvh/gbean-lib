package com.base.filter;

public class StringEndWidthFilter extends LikeFilter<String> {

	public StringEndWidthFilter(String val) {
		super(val);
		val = "%" + val;
		setValue(val);
	}

}
