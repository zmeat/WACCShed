/*
 * Author: Diego Cardoso
 * Copyright (c) 2016, WACC and individual contributors as listed at
 * https://wacc.las.iastate.edu/
 * All rights reserved. 
 */

package org.utilities.internal;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;

public class HSQLDataBaseManager implements DataBaseManager{

	private Connection connection = null;
	
	// TODO Diego: Get this config from config file
	private String serverAddress = "file:res/data/db/";
	private String port = "";
	private String databaseName = "waccdb";
	private String userName = "SA";
	private String password = "";
	
	/**
	 * Returns a Manager for local HSQLDB. The constructor connects to local database automatically
	 * using configuration parameters.
	 * 
	 */
	public HSQLDataBaseManager() {
		try {
			connection = DriverManager.getConnection("jdbc:hsqldb:"+ serverAddress + databaseName+";shutdown=true" , userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void connectDB(String serverAddress, String port, String databaseName, String userName, String password) {
		try {
			connection = DriverManager.getConnection("jdbc:hsqldb:"+ serverAddress + databaseName, userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ResultSet queryDB(String queryStatement){
		ResultSet rs = null;
		Statement stmt = null;
		
		if(connection == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(queryStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("get data error");
				e.printStackTrace();
			}
		}
		return rs;
	}

	@Override
	public void deleteDB(String deleteStatement) {
		if(connection == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(deleteStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("delete data error");
				e.printStackTrace();
			}
		}	}

	@Override
	public void insertDB(String insertStatement) {
		if(connection == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(insertStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("insert data error");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Execute an insert statement in a table with IDENTITY (integer primary
	 * key with auto-increment).
	 * @param insertStatement
	 * @return key (id of newly inserted record) or -1 if fails
	 */
	public int insertDBreturnKey(String insertStatement) {
		int key = -1;

		if(connection == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		try {
			PreparedStatement result = connection.prepareStatement(insertStatement,
			        Statement.RETURN_GENERATED_KEYS);
			
			int updated = result.executeUpdate();
			if (updated == 1) {
			    ResultSet generatedKeys = result.getGeneratedKeys();
			    if (generatedKeys.next()) {
			        key = generatedKeys.getInt(1);
			    }
			}
			connection.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return key;
	}

	@Override
	public void exportTable(String filename, String tablename) {
		try { //TODO Diego: fix columns
			Statement stmt = connection.createStatement();
			String stmtString = "";
			if(tablename.equals("cityData_1997_2013")) {
				stmtString = "SELECT id, time,budget, levee_invest, subsidy_rate, levee_quality, "
						+ "flood_damage, road_repair_invest, social_benefit,max_Q"
						+ " FROM cityData_1997_2013";
			}
			else if(tablename.equals("farmerData_1997_2013")) {
				stmtString = "SELECT id, time, total_land, buf_area, crop_area, fallow_area, "
						+ "subsidy_rate, corn_price_per_bushel, bushels_per_acre, grow_precip, cropProfit_acre, "
						+ "production_cost_per_acre, total_profits, money_balance, CropCN, current_farmerland_CN, "
						+ "cornConsumption, utilityOfConsumption FROM farmerData_1997_2013";
			}

			ResultSet rs = stmt.executeQuery(stmtString);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			
            CSVWriter writer = new CSVWriter(new FileWriter(filename));
            List<String[]> data = new ArrayList<String[]>();
            // Insert header
            String[] header = new String[numberOfColumns];
            for(int i=1; i<=numberOfColumns; i++){
            	header[i-1] = rsMetaData.getColumnName(i);
            }
            data.add(header);
            // Insert data
            while(rs.next()){
            	 String[] strArray  = new String[numberOfColumns];
                 for(int i=1; i<=numberOfColumns; i++){
                	 strArray[i-1] = rs.getString(i);
                 }
                 data.add(strArray);
            }
            writer.writeAll(data);
            writer.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
