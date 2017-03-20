package com.base.filter;

import java.util.*;

public class FilterCriteria implements java.io.Serializable {
	public List<Filter> _filters;

	public FilterCriteria() {
		_filters = new ArrayList<Filter>();
	}

	public void addFilter(Filter filter) {
		_filters.add(filter);
	}

	public List<Filter> getFilters() {
		return _filters;
	}
}
