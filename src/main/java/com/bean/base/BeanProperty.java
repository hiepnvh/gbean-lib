package com.bean.base;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.bean.db.mysql.MysqlDbHelper;
import com.bean.json.JsonUtils;

public abstract class BeanProperty<T> extends Parametrizer<T> implements Serializable  {
	
	public abstract void fromString(String str);
	private static Logger LOGGER = Logger.getLogger(MysqlDbHelper.class.getName());
	
	static class IntegerProperty extends BeanProperty<Integer> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val =  Integer.parseInt(str);
		}};
	static class StringProperty extends BeanProperty<String> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val = str;
		}};
	static class DateProperty extends BeanProperty<Date> {

		@Override
		public void fromString(String str) {
			Integer time =  Integer.parseInt(str);
			try {
//				DateFormat df = new SimpleDateFormat(JsonUtils.DATE_FORMAT);
				this._val =  new Date(time);
//				LOGGER.info(this._val.toString());
			}catch (Exception exc) {
			}
		}};
	static class DoubleProperty extends BeanProperty<Double> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val =  Double.parseDouble(str);
		}};
	static class ByteProperty extends BeanProperty<Byte> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val =  Byte.parseByte(str);
		}};
	static class LongProperty extends BeanProperty<Long> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val =  Long.parseLong(str);
		}};
	static class CharProperty extends BeanProperty<Character> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val =  str.charAt(0);
		}};
	static class BooleanProperty extends BeanProperty<Boolean> {

		@Override
		public void fromString(String str) {
			// TODO Auto-generated method stub
			this._val =  Boolean.parseBoolean(str);
		}};
	
	public static BeanProperty<Integer> integerType() {
		return new IntegerProperty();
	}
	
	public static BeanProperty<String> stringType() {
		return new StringProperty();
	}
	
	public static BeanProperty<Date> dateType() {
		return new DateProperty();
	}
	
	public static BeanProperty<Double> doubleType() {
		return new DoubleProperty();
	}
	
	public static BeanProperty<Byte> byteType() {
		return new ByteProperty();
	}
	
	public static BeanProperty<Long> longType() {
		return new LongProperty();
	}
	
	public static BeanProperty<Character> charType() {
		return new CharProperty();
	}
	
	public static BeanProperty<Boolean> boolType() {
		return new BooleanProperty();
	}
	
	protected T _val;
	protected boolean _isset = false;
	
	public BeanProperty() {		
	}
	
	public void set(T val) {
		_val = val;
		_isset = true;
	}
	
	public T get() {
		return _val;
	}
	
	public boolean isSet(){
		return _isset;
	}
	

}
