package org.utilities.internal;

import java.sql.ResultSet;

public interface DataBaseManager {

	// connect to mysql database at jdbc:mysql://serverAddress:port/databaseName, with userName, password
	void connectDB(String serverAddress, String port, String databaseName, String userName, String password);

	// query database
	ResultSet queryDB(String queryStatement);

	//delete item in database
	void deleteDB(String deleteStatement);

	//insert item in database
	void insertDB(String insertStatement);
	
	/**
	 * Execute an insert statement in a table with IDENTITY (integer primary
	 * key with auto-increment).
	 * @param insertStatement
	 * @return key (id of newly inserted record) or -1 if fails
	 */ 
	int insertDBreturnKey(String insertStatement);

	void exportTable(String filename, String tablename);

}