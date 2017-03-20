package com.base.filter;

public class StringLikeFilter extends LikeFilter<String> {

	public StringLikeFilter(String val) {
		super(val);
		val = "%" + val + "%";
		setValue(val);
	}

}
