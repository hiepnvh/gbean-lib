/**
 * 
 */
package com.bean.db.oracle;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bean.base.BeanProperty;
import com.bean.base.BeanSchema;
import com.bean.base.Parametrizer;




/**
 * @author QuangN
 *
 */
public class ResultSetWrapper {
	
	static abstract class  Conversion<T> extends Parametrizer<T> {
		public abstract T convert(ResultSet rs,String col);
	}
	
	static class IntegerConversion extends Conversion<Integer> {

		@Override
		public Integer convert(ResultSet rs, String col) {
			try {
				return rs.getInt(col);
			} catch (Exception exc) {
				return null;
			}
		}
		
	}
	
	static class BooleanConversion extends Conversion<Boolean> {

		@Override
		public Boolean convert(ResultSet rs, String col) {
			try {
				return rs.getBoolean(col);
			} catch (Exception exc) {
				return null;
			}
		}
		
	}
	
	static class StringConversion extends Conversion<String> {

		@Override
		public String convert(ResultSet rs, String col) {
			try {
				return rs.getString(col);
			} catch (Exception exc) {
				return null;
			}
		}
		
	}
	
	static class DoubleConversion extends Conversion<Double> {

		@Override
		public Double convert(ResultSet rs, String col) {
			try {
				return rs.getDouble(col);
			} catch (Exception exc) {
				return null;
			}
		}
		
	}
	
	static class DateConversion extends Conversion<java.util.Date> {

		@Override
		public java.util.Date convert(ResultSet rs, String col) {
			try {
				return new Date(rs.getTimestamp(col).getTime());
			} catch (Exception exc) {
				return null;
			}
		}
		
	}
	
	
	ResultSet _rs;
	BeanSchema _schema;
	static Map<String,Conversion<?>> TYPE_MAP;
	
	static {
		Conversion<?>[] convs = {new BooleanConversion(),new IntegerConversion(), new StringConversion(), new DoubleConversion(), new DateConversion()};
		TYPE_MAP = new HashMap<String,Conversion<?>>();
		for (int i=0;i<convs.length;i++) {
			Conversion<?> conv = convs[i];
			Class parametricClass = conv.returnedParamClass();
			TYPE_MAP.put(parametricClass.getName(), conv);
		}
	}
	
	public ResultSetWrapper(ResultSet rs, BeanSchema schema) {
		_rs = rs;
		_schema = schema;
	}
	
	public Object getObject(String col) {
		BeanProperty<?> beanProp = _schema.getProperty(col);
		Class propertyParamClass = beanProp.returnedParamClass();
		Conversion<?> conv = TYPE_MAP.get(propertyParamClass.getName());
		return conv.convert(_rs, col);
	}
	
	public ResultSet getResultSet() {
		return _rs;
	}

}
