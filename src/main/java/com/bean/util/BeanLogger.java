package com.bean.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.bean.base.*;



public class BeanLogger  {


	
	private BeanLogger(Logger logger) {
		_logger = logger;
	}



	private Logger _logger;
	
	private static Map<String,BeanLogger> _instances;
	
	static {
		_instances =  new HashMap<String,BeanLogger>();
		
	}
	
	public static BeanLogger createInstance(Class cls) {
		if (!_instances.containsKey(cls.getName())) {
			BeanLogger gcpLogger = new BeanLogger(Logger.getLogger(cls.getName()));
			_instances.put(cls.getName(), gcpLogger);
		}
		return _instances.get(cls.getName());
	}

	private  String getString(Bean bean) throws Exception {
		BeanSchema metaInfo = BeanSchema.loadSchema(bean.getClass());
		String beanString = "";
		for (String col :  metaInfo.getCols()) {
			BeanProperty<?> beanProp= metaInfo.getProperty(col);
			Object val = bean.get(beanProp);
			beanString += col + ":" + val + ";";
		}
		return beanString;
	}
	
	public  void info(Bean bean) throws Exception {		
		_logger.info(getString(bean));
	}



	public  void info(List<Bean> beans) throws Exception {
		for (int i = 0; i < beans.size(); i++) {
			_logger.info(getString(beans.get(i)));
		}
	}
	
	public void info(String str) {
		_logger.info(str);
	}
}
