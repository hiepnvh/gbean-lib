package com.base.filter;

public interface Filter<T> extends java.io.Serializable {
	public Boolean getNegated();

	public void setNegated(Boolean val);
	
	public void setOrGroup(String group);
	public String getOrGroup();
}
