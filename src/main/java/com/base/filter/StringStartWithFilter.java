package com.base.filter;

public class StringStartWithFilter extends LikeFilter<String> {

	public StringStartWithFilter(String val) {
		super(val);
		val = val + "%";
		setValue(val);
	}

}
