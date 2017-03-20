/**
 * 
 */
package com.bean.db.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bean.base.Bean;
import com.bean.base.BeanProperty;
import com.bean.base.BeanSchema;
import com.bean.base.Parametrizer;




/**
 * @author QuangN
 *
 */
public class PreparedStatementWrapper {
	
	static abstract class  Adaptation<T> extends Parametrizer<T> {
		public abstract void setObject(PreparedStatement stmt, int i, T beanVal) throws Exception;
	}
	
	static class IntegerConversion extends Adaptation<Integer> {

		@Override
		public void setObject(PreparedStatement stmt, int i, Integer beanVal) throws Exception{		
			stmt.setInt(i,  beanVal);
		}
		
	}
	
	static class BooleanConversion extends Adaptation<Boolean> {

		@Override
		public void setObject(PreparedStatement stmt, int i, Boolean beanVal) throws Exception{		
			stmt.setBoolean(i,  beanVal);
		}
		
	}
	
	static class StringConversion extends Adaptation<String> {

		@Override
		public void setObject(PreparedStatement stmt, int i, String beanVal) throws Exception{		
			stmt.setString(i,  beanVal);
		}
		
	}
	
	static class DoubleConversion extends Adaptation<Double> {

		@Override
		public void setObject(PreparedStatement stmt, int i, Double beanVal) throws Exception{		
			stmt.setDouble(i,  beanVal);
		}
		
	}
	
	static class DateConversion extends Adaptation<java.util.Date> {

		@Override
		public void setObject(PreparedStatement stmt, int i, java.util.Date beanVal) throws Exception{		
			java.sql.Date sqlDate = new java.sql.Date (beanVal.getTime());
			stmt.setDate(i,  sqlDate);
		}
		
	}
	
	
	PreparedStatement _stmt;
	static Map<String,Adaptation<?>> TYPE_MAP;
	
	static {
		Adaptation<?>[] convs = {new BooleanConversion(),new IntegerConversion(), new StringConversion(), new DoubleConversion(), new DateConversion()};
		TYPE_MAP = new HashMap<String,Adaptation<?>>();
		for (int i=0;i<convs.length;i++) {
			Adaptation<?> conv = convs[i];
			Class parametricClass = conv.returnedParamClass();
			TYPE_MAP.put(parametricClass.getName(), conv);
		}
	}
	
	public PreparedStatementWrapper(PreparedStatement stmt) throws Exception {
		_stmt = stmt;
	}
	
	public <T> void setObject(int i, T beanVal) throws Exception {
		if (beanVal!=null) {
			Class<T> propertyParamClass = (Class<T>) beanVal.getClass();
			Adaptation<T> conv = (Adaptation<T>)TYPE_MAP.get(propertyParamClass.getName());
			conv.setObject(_stmt, i, beanVal);
		} else
			_stmt.setObject(i, null);
	}
	
	public PreparedStatement getStatement() {
		return _stmt;
	}

}
