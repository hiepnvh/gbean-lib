package com.bean.db.oracle;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import com.bean.base.*;
import com.bean.db.*;
import com.bean.db.oracle.PreparedStatementWrapper;
import com.bean.db.oracle.ResultSetWrapper;
import com.bean.json.JsonUtils;

import java.util.logging.*;

public class OracleDbHelper {

	private static Logger LOGGER = Logger.getLogger(OracleDbHelper.class.getName());
	private static SimpleDateFormat sdfy = new SimpleDateFormat(JsonUtils.DATE_FORMAT_YEAR_FIRST);
	Connection mCon;

	public OracleDbHelper(Connection con) throws Exception {
		mCon = con;
	}


	
	public <T extends Bean >void removeBean(T bean) throws Exception {
		Class<T> beanClass = (Class<T>) bean.getClass();
		BeanIdentifier<T> externalId = new BeanIdentifier<T> (beanClass);
		boolean success = externalId.setValue(bean);
		if (!success)
			throw new Exception("Missing key value ");
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		ExistConditionClause<T> condition = new ExistConditionClause<T>(externalId, false);
		String tbl = metaInfo.getTable();
		String SQL = "DELETE FROM " + tbl + " WHERE  "
				+ condition.getString();
		LOGGER.info("BeanDbAdapter removeBean SQL "+ SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int i = 0; i < condition.getWildcardNumber(); i++) {
			Object wildcardValue = condition.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject( i + 1,	wildcardValue);
		}
		stmtWrapper.getStatement().execute();
		stmtWrapper.getStatement().close();
	}
	
	public  void removeBeans(BeanFilter filter) throws Exception {
		Class<? extends Bean> beanClass = filter.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		String tbl = metaInfo.getTable();
		ConditionClause condition = new ConditionClause(filter);
		String SQL = "DELETE FROM " + tbl 
				+ " WHERE   " + condition.getString();
		LOGGER.info("BeanDbAdapter getBeans " + SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int i = 0; i < condition.getWildcardNumber(); i++) {
			Object wildcardValue = condition.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject( i + 1,	wildcardValue);
		}
		stmtWrapper.getStatement().execute();
		stmtWrapper.getStatement().close();
	}
	

	public <T extends Bean,P> DataSource<T> getBeans(BeanFilter filter) throws Exception {
		Class<? extends Bean> beanClass = filter.getBeanClass();
		DataSource<T> dataSource = new DataSource(beanClass);
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		String tbl = metaInfo.getTable();
		ConditionClause condition = new ConditionClause(filter);
		InnerJoinClause join = new InnerJoinClause(filter);
		OrderClause order = new OrderClause(filter);
		// If oracle 12
		//String SQL = "SELECT * FROM " + tbl + join.getString()
		//		+ " WHERE   " + condition.getString();
		// Oracle  < 12
		String SqlCreateRow = "(SELECT "+tbl+".*, ROW_NUMBER() OVER (ORDER BY " +order.getString()+ ") rn FROM " 
				+ tbl + " " + tbl + " " + join.getString() +  " WHERE " + condition.getString()+") ";
		 
		String SQL ;
		if(filter.getLimit() != 0) {
			SQL = "SELECT * FROM " + SqlCreateRow + " WHERE (1=1) ";
		}else {
			SQL = "SELECT * FROM " + tbl + join.getString()
			+ " WHERE " + condition.getString();
		}
		if(filter.getLimit() != 0) {
			String limitString = filter.getLimitString(); //return offset,limit
			String offset = limitString.split(",")[0].trim();
			String limit = limitString.split(",")[1].trim();
			// If oracle >= 12 this case
			//SQL += " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
			//Oracle < 11
			int offsetInt = Integer.parseInt(limit) + Integer.parseInt(offset);
			SQL += " AND rn >= " + offset + " AND rn <=" + offsetInt;
		}
		
		/*hiepn 2016.10.04 - modified - begin*/
		if(order.getString().length() != 0) {
			SQL += " ORDER BY " + order.getString();
		}
		/*hiepn 2016.10.04 - modified - end*/
		LOGGER.info("BeanDbAdapter getBeans " + SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int i = 0; i < join.getWildcardNumber(); i++) {
			Object wildcardValue = join.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject(i + 1, wildcardValue);
		}
		for (int i = 0; i < condition.getWildcardNumber(); i++) {
			Object wildcardValue = condition.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject(join.getWildcardNumber() + i + 1,
					wildcardValue);
		}
		ResultSetWrapper rsWrapper = new ResultSetWrapper(stmtWrapper.getStatement().executeQuery(),metaInfo);
		List<T> beans = new ArrayList<T>();
		Set<String> cols = metaInfo.getCols();
		while (rsWrapper.getResultSet().next()) {			
			T bean =  (T)beanClass.newInstance();
			for (String col: cols) {
				Object beanVal = rsWrapper.getObject(col);
				if (beanVal != null) {
					BeanProperty<P> beanProp = (BeanProperty<P> )metaInfo.getProperty(col);
					bean.set(beanProp,  (P)beanVal);
				}
			}
			beans.add(bean);
		}
		rsWrapper.getResultSet().close();
		stmtWrapper.getStatement().close();
		dataSource.setSource(beans);
		return dataSource;
	}
	
	
	public <T extends Bean,P> DataSource<T> getBeans(BeanFilter filter, BeanProperty[] fields) throws Exception {
		Class<? extends Bean> beanClass = filter.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);

		DataSource<T> dataSource = new DataSource(beanClass);
		List<String> selectedCols = new ArrayList<String>();
		for (BeanProperty field : fields) {
			String col = metaInfo.getPropertyCol(field);
			selectedCols.add(col);
			dataSource.setDataCol(selectedCols);
		}
		String tbl = metaInfo.getTable();
		ConditionClause condition = new ConditionClause(filter);
		InnerJoinClause join = new InnerJoinClause(filter);
		String SQL = "SELECT * FROM " + tbl + join.getString()
				+ " WHERE   " + condition.getString();
		LOGGER.info("BeanDbAdapter getBeans " + SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int i = 0; i < join.getWildcardNumber(); i++) {
			Object wildcardValue = join.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject(i + 1, wildcardValue);
		}
		for (int i = 0; i < condition.getWildcardNumber(); i++) {
			Object wildcardValue = condition.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject(join.getWildcardNumber() + i + 1,
					wildcardValue);
		}
		ResultSetWrapper rsWrapper = new ResultSetWrapper(stmtWrapper.getStatement().executeQuery(),metaInfo);
		List<T> beans = new ArrayList<T>();
		Set<String> cols = metaInfo.getCols();
		while (rsWrapper.getResultSet().next()) {			
			T bean =  (T)beanClass.newInstance();
			for (String col: cols) {
				Object beanVal = rsWrapper.getObject(col);
				if (beanVal != null) {
					BeanProperty<P> beanProp = (BeanProperty<P> )metaInfo.getProperty(col);
					bean.set(beanProp,  (P)beanVal);
				}
			}
			beans.add(bean);
		}
		rsWrapper.getResultSet().close();
		stmtWrapper.getStatement().close();
		dataSource.setSource(beans);
		return dataSource;
	}
	
	

	private <T extends Bean> void validateBean(T bean) throws Exception {
		Class<T> beanClass = (Class<T>) bean.getClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		if (metaInfo.getExtKeyCols().size() == 0)
			return;
		BeanIdentifier<T> identifier = new BeanIdentifier<T>(beanClass);
		boolean success = identifier.setValue(bean);
		if (!success) {
			throw new Exception("Bean " + bean + " :Key value missing ");
		}

	}
	

	private <T extends Bean>  void validateBatch(DataSource<T> beanArray) throws Exception {
		Class<T> beanClass = beanArray.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		if (metaInfo.getExtKeyCols().size() == 0)
			return;
		BeanIdentifier<T> identifier = new BeanIdentifier<T>(beanClass);
		Set<String> idSet = new HashSet<String>();
		for (int i = 0; i < beanArray.getBeanCount(); i++) {
			Bean bean = beanArray.getBean(i);
			validateBean(bean);
			identifier.setValue(bean);
			String id = identifier.getId();
			if (idSet.contains(id)) {
				throw new Exception("Index " + i + " :Duplicate bean " + id);

			}
			idSet.add(id);
		}
	}

	private <T extends Bean>  void insertBean(T bean) throws Exception {
		DataSource<T> beanArray = new DataSource<T>((Class<T>)bean.getClass());
		beanArray.setSource(bean);
		insertBatch(beanArray);
	}

	private<T extends Bean>  void updateBean(T bean) throws Exception {
		List<String> updateCols = new ArrayList<String>();
		BeanSchema metaInfo = BeanSchema.loadSchema(bean.getClass());
		Set<String> totalCols = metaInfo.getCols();
		for (String col : totalCols) {
			BeanProperty<?> beanProp = metaInfo.getProperty(col);
			boolean isset = bean.isSet(beanProp);
			if (isset)
				updateCols.add(col);
		}
		DataSource<T> beanArray = new DataSource<T>((Class<T>)bean.getClass());
		beanArray.setDataCol(updateCols);
		beanArray.setSource(bean);		
		updateBatch(beanArray);
	}

	// insert beans by batch. Required bean in same class
	private <T extends Bean>  void insertBatch(DataSource<T> beanArray) throws Exception {
		Class<T> beanClass = beanArray.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		String tbl = metaInfo.getTable();
		InsertClause insertClause = new InsertClause(beanArray);
		String SQL = "INSERT INTO " + tbl
				+ insertClause.getColumnString() + " VALUES "
				+ insertClause.getValueString();
		LOGGER.info("BeanDbAdapter insertBatch " + SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int j = 0; j < beanArray.getBeanCount(); j++) {
			T bean = beanArray.getBean(j);
			for (int i = 0; i < insertClause.getColoumnCount(); i++) {
				String col = insertClause.getColumn(i);
				BeanProperty beanProp = metaInfo.getProperty(col);
				Object realVal = bean.get(beanProp);
				stmtWrapper.setObject(i + 1, realVal);
			}
			stmtWrapper.getStatement().addBatch();
		}
		stmtWrapper.getStatement().executeBatch();
		stmtWrapper.getStatement().close();
	}

	public <T extends Bean> int countBean(T bean) throws Exception {
		Class<T> beanClass = (Class<T>) bean.getClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		if (metaInfo.getExtKeyCols().size() == 0)
			return 0;
		BeanIdentifier<T> externalId = new BeanIdentifier<T>(beanClass);
		boolean success = externalId.setValue(bean);
		if (!success) {
			throw new Exception("Key value missing ");
		}
		ExistConditionClause<T> condition = new ExistConditionClause<T>(externalId, false);
		String tbl = metaInfo.getTable();
		String SQL = "SELECT COUNT(*) FROM " + tbl + " WHERE  "
				+ condition.getString();
		LOGGER.info("BeanDbAdapter isExist " + SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int i = 0; i < condition.getWildcardNumber(); i++) {
			Object wildValue = condition.getWildcardValueAt(i);
			if (wildValue == null) {
				throw new Exception("SQL error Wildcard null at " + i);
			}
			stmtWrapper.setObject(i + 1, wildValue);
		}
		int count =0;
		ResultSet rs = stmtWrapper.getStatement().executeQuery();
		if (rs.next()) {
			count =  rs.getInt(1);
		}
		rs.close();
		stmtWrapper.getStatement().close();
		return count;
	}

	// process beans by batch. Required bean in same class
	private <T extends Bean>  void updateBatch(DataSource<T> beanArray) throws Exception {
		Class<T> beanClass = beanArray.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		BeanIdentifier<T> identifier = new BeanIdentifier<T>(beanClass);
		String tbl = metaInfo.getTable();
		UpdateClause updateList = new UpdateClause(beanArray);
		ExistConditionClause<T> condition = new ExistConditionClause<T>(identifier, true);
		String SQL = "UPDATE " + tbl + " SET " + updateList.getSetString()
				+ " WHERE 1=1 AND " + condition.getString();
		LOGGER.info("BeanDbAdapter updateBeansByExternalId " + SQL);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int j = 0; j < beanArray.getBeanCount(); j++) {
			T bean = beanArray.getBean(j);
			condition.setBean(bean);
			for (int i = 0; i < updateList.getColumnCount(); i++) {
				String col = updateList.getColumn(i);
				BeanProperty beanProp = metaInfo.getProperty(col);
				Object realVal = bean.get(beanProp);
				stmtWrapper.setObject(i + 1, realVal);
			}
			for (int i = 0; i < condition.getWildcardNumber(); i++) {
				Object realVal = condition.getWildcardValueAt(i);
				stmtWrapper.setObject(updateList.getColumnCount() + i + 1,
						realVal);
			}
			stmtWrapper.getStatement().addBatch();
		}
		stmtWrapper.getStatement().executeBatch();
		stmtWrapper.getStatement().close();
	}

	public <T extends Bean>  void insertOrUpdateBatch(Class beanClass, List<T> beans, List<String> cols) throws Exception {
		mCon.setAutoCommit(false);
		DataSource<T> iGroup = new DataSource<T>(beanClass);
		DataSource<T> uGroup = new DataSource<T>(beanClass);
		uGroup.setDataCol(cols);
		List<T> iList = new ArrayList<T>();
		List<T> uList = new ArrayList<T>();
		for (T bean : beans) {
			int beanNo = countBean(bean);
			if (beanNo == 0)
				iList.add(bean);
			else if (beanNo == 1)
				uList.add(bean);
			else {
				throw new Exception("Duplicate bean ");
			}
		}
		iGroup.setSource(iList);
		uGroup.setSource(uList);
		validateBatch(iGroup);
		validateBatch(uGroup);
		if (iList.size() > 0)
			insertBatch(iGroup);
		if (uList.size() > 0)
			updateBatch(uGroup);
		mCon.commit();
		mCon.setAutoCommit(true);
	}

	public <T extends Bean>  void insertOrUpdateBean(T bean) throws Exception {
		validateBean(bean);
		int beanNo = countBean(bean);
		if (beanNo == 0)
			insertBean(bean);
		else if (beanNo == 1)
			updateBean(bean);
		else {
			throw new Exception("Duplicate bean ");
		}
	}

	public <T extends Bean>  void insertOrUpdateBeans(List<T> beans) throws Exception {
		mCon.setAutoCommit(false);
		for (Bean bean : beans) {
			insertOrUpdateBean(bean);
		}
		mCon.commit();
		mCon.setAutoCommit(true);
	}

	public <T extends Bean,P> long countBeans(BeanFilter filter) throws Exception {
		long quantity = 0;
		Class<? extends Bean> beanClass = filter.getBeanClass();
		BeanSchema metaInfo = BeanSchema.loadSchema(beanClass);
		String tbl = metaInfo.getTable();
		ConditionClause condition = new ConditionClause(filter);
		InnerJoinClause join = new InnerJoinClause(filter);
		String SQL = "SELECT COUNT(1) quantity FROM " + tbl + join.getString()
				+ " WHERE " + condition.getString();
		LOGGER.info("BeanDbAdapter getBeans" +" ["+ SQL +"] "+ mCon);
		PreparedStatementWrapper stmtWrapper = new PreparedStatementWrapper(mCon.prepareStatement(SQL));
		for (int i = 0; i < join.getWildcardNumber(); i++) {
			Object wildcardValue = join.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			stmtWrapper.setObject(i + 1, wildcardValue);
		}
		for (int i = 0; i < condition.getWildcardNumber(); i++) {
			Object wildcardValue = condition.getWildcardValueAt(i);
			if (wildcardValue == null) {
				throw new Exception("SQL error :Wildcard null at " + i);
			}
			if(wildcardValue.getClass()==Date.class){
				wildcardValue = sdfy.format(wildcardValue);
			}
			stmtWrapper.setObject(join.getWildcardNumber() + i + 1,wildcardValue);
		}
		ResultSet rs = stmtWrapper.getStatement().executeQuery();
		if (rs.next()) {
			quantity =  rs.getLong(1);
		}
		rs.close();
		stmtWrapper.getStatement().close();
		return quantity;
	}
}
