/**
 * 
 */

package com.bean.json;


import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;








import org.json.*;

import com.bean.base.Bean;
import com.bean.base.BeanProperty;
import com.bean.base.BeanSchema;
import com.bean.db.mysql.MysqlDbHelper;

/**
 * @author QuangN
 *
 */
public class JsonUtils {
	
	private static Logger LOGGER = Logger.getLogger(MysqlDbHelper.class.getName());

	public static String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
	public static String DATE_FORMAT_YEAR_FIRST = "yyyy-MM-dd HH:mm:ss";
	
	public static void setDateFormat(String format) {
		DATE_FORMAT = format;
	}
	
	public static <P> P  fromJsonToPrimitive(String jVal, Class<P> primitiveClass) throws Exception {
		Object primitiveVal = null;
		if (jVal != null) {
			String type = primitiveClass.getSimpleName();
			if (type.compareTo("Integer") == 0) {
				try {
					primitiveVal = ((int) Math.round(Double.parseDouble(jVal)));
				} catch (Exception exc) {
					primitiveVal = null;
				}
			}
			if (type.compareTo("String") == 0) {
				primitiveVal = jVal;
			}
			if (type.compareTo("Boolean") == 0) {
				try {
					primitiveVal = Boolean.parseBoolean(jVal);
				} catch (Exception exc) {
					primitiveVal = null;
				}
			}
			if (type.compareTo("Double") == 0) {
				try {
					primitiveVal = Double.parseDouble(jVal);
				} catch (Exception exc) {
					primitiveVal = null;
				}
			}
			if (type.compareTo("Date") == 0) {
				try {
					DateFormat df = new SimpleDateFormat(DATE_FORMAT);
					primitiveVal = new Date(df.parse(jVal).getTime());
				} catch (Exception exc) {
				}
			}
		}
		return (P)primitiveVal;
	}
	
	public static Object fromPrimitiveToJson(Object prmitivieVal) throws Exception {	
//		LOGGER.info("prmitivieVal "+prmitivieVal);
		String type = prmitivieVal.getClass().getSimpleName();
		if (type.compareTo("Date") == 0) {
			try {
				String strVal = prmitivieVal.toString();
				SimpleDateFormat df = new SimpleDateFormat();
				df.applyPattern(DATE_FORMAT);
				strVal = df.format(prmitivieVal);
//				LOGGER.info("strval "  + strVal);
				return strVal;
			} catch (Exception exc) {
			}
		
		}
		return prmitivieVal;

	}
	
	
	
	public static <T extends Bean,P> T fromJsonToBean(JSONObject jsonObj, Class<T> beanClass) throws Exception {
		T bean = beanClass.newInstance();
		BeanSchema metaInfo = BeanSchema.loadSchema(bean.getClass());
		Iterator<String> keyItr = jsonObj.keys();
		while (keyItr.hasNext()) {
			String col = keyItr.next();
			String jVal = jsonObj.getString(col);
			BeanProperty<P> beanProp = (BeanProperty<P>)  metaInfo.getProperty(col);
			if (beanProp!=null) {
				P val  = (P) fromJsonToPrimitive(jVal,beanProp.returnedParamClass());
				bean.set(beanProp, val);
				
			}
		}
		return bean;
	}
	
	public static <T> List<T> fromJsonArrayToPrimitiveList(JSONArray jsonArray, Class<T> prmitiveClass) throws Exception {
		List<T> beans = new ArrayList<T>();
		for (int i=0;i<jsonArray.length();i++) {
			String jStr = jsonArray.getString(i);
			T beanValue = fromJsonToPrimitive(jStr,prmitiveClass);
			beans.add(beanValue);
		}
		return beans;
	}
	
	public static <T extends Bean> List<T > fromJsonArrayToBeanList(JSONArray jsonArray, Class<T> beanClass) throws Exception {
		List<T> beans = new ArrayList<T>();
		for (int i=0;i<jsonArray.length();i++) {
			JSONObject jsObj = jsonArray.getJSONObject(i);
			T bean = fromJsonToBean(jsObj,beanClass);
			beans.add(bean);
		}
		return beans;
	}
	
	public static JSONObject fromMapToJson(Map<String,String[]> params) throws Exception {
		JSONObject jsonObj = new JSONObject();
		Iterator<String> keyItr = params.keySet().iterator();
		while (keyItr.hasNext()) {
			String name = keyItr.next();
			String[] vals = params.get(name);
			if (vals!=null && vals.length>0) {
				String val = vals[0];
				if (val!=null && val.length()>0) {
					if (val.startsWith("{")) {					
						JSONObject jSubObj = new JSONObject(val);
						jsonObj.put(name,jSubObj );
					} else if ( val.startsWith("[")){
						JSONArray jSubObj = new JSONArray(val);
						jsonObj.put(name,jSubObj );
					} else {
						jsonObj.put(name,val );
					}
						
				} 
			}
		}
		return jsonObj;
	}
	
	public static JSONObject  fromBeanToJson(Bean bean) throws Exception {
		BeanSchema metaInfo = BeanSchema.loadSchema(bean.getClass());
		JSONObject jsonObj = new JSONObject();
		for (String col: metaInfo.getCols()) {
			BeanProperty<?> beanProp = metaInfo.getProperty(col);
			Object beanVal = bean.get(beanProp);
			if (beanVal!=null) {
				Object jVal = fromPrimitiveToJson(bean.get(beanProp));
				jsonObj.put(col, jVal);
			}
		}
		return jsonObj;
	}

	
	public static <T extends Bean> JSONObject fromBeanToJson(T bean,List<String> fields) throws Exception {
		JSONObject jsonObj = new JSONObject();
		BeanSchema metaInfo = BeanSchema.loadSchema(bean.getClass());
		for (String col: metaInfo.getCols()) {
			if (fields.contains(col)) {
				BeanProperty<?> beanProp = metaInfo.getProperty(col);
				Object beanVal = bean.get(beanProp);
				if (beanVal!=null) {
					Object jVal = fromPrimitiveToJson(bean.get(beanProp));
					jsonObj.put(col, jVal);
				}
			}
		}
		return jsonObj;
	}
	
	public static <T extends Bean> JSONArray fromBeanListToJsonArray(List<T> beans) throws Exception {
		JSONArray jsonArray = new JSONArray();
		for (int i=0;i<beans.size();i++) {
			Bean bean = beans.get(i);
			JSONObject jsonObj  = fromBeanToJson(bean);
			jsonArray.put(jsonObj);
		}
		return jsonArray;
	}
	

}
