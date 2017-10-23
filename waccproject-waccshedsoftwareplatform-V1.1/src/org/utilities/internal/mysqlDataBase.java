package org.utilities.internal;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;

public class mysqlDataBase implements DataBaseManager {
	
	private Connection connect = null;
	
	public mysqlDataBase(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Success loading Mysql Driver!");
		}
		catch(Exception e){
			System.out.print("Error loading Mysql Driver!");
			e.printStackTrace();
		}
	}
	
	// connect to mysql database at jdbc:mysql://serverAddress:port/databaseName, with userName, password
	/* (non-Javadoc)
	 * @see org.utilities.internal.DataBaseManager#connectDB(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void connectDB(String serverAddress,String port,String databaseName,String userName, String password){
		
		try{
		connect = DriverManager.getConnection("jdbc:mysql://"+serverAddress+":"+port+"/"+databaseName,userName,password);
		//connect URL is: jdbc:mysql//server address/database name, login user, password
		System.out.println("Success connect Mysql server!");
		}catch(Exception e){
			System.out.println("connect "+databaseName +" error!");
			e.printStackTrace();
		}
		
	}
	
	// query database
	/* (non-Javadoc)
	 * @see org.utilities.internal.DataBaseManager#queryDB(java.lang.String)
	 */
	@Override
	public ResultSet queryDB(String queryStatement){
		ResultSet rs = null;
		Statement stmt = null;
		
		if(connect == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				stmt = connect.createStatement();
				rs = stmt.executeQuery(queryStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("get data error");
				e.printStackTrace();
			}
		}
		return rs;
	}
	
	//update database
	/* (non-Javadoc)
	 * @see org.utilities.internal.DataBaseManager#updateDB(java.lang.String)
	 */
	public void updateDB(String updateStatement){
		
		if(connect == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				Statement stmt = connect.createStatement();
				stmt.executeUpdate(updateStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("update data error");
				e.printStackTrace();
			}
		}
	}
	
	//delete item in database
	/* (non-Javadoc)
	 * @see org.utilities.internal.DataBaseManager#deleteDB(java.lang.String)
	 */
	@Override
	public void deleteDB(String deleteStatement){
		if(connect == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				Statement stmt = connect.createStatement();
				stmt.executeUpdate(deleteStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("delete data error");
				e.printStackTrace();
			}
		}
	}
	
	//insert item in database
	/* (non-Javadoc)
	 * @see org.utilities.internal.DataBaseManager#insertDB(java.lang.String)
	 */
	@Override
	public void insertDB(String insertStatement){
		if(connect == null)
		{
			throw new NullPointerException("the dababase is not connected");
		}
		else
		{
			try {
				Statement stmt = connect.createStatement();
				stmt.executeUpdate(insertStatement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("insert data error");
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.utilities.internal.DataBaseManager#exportTable(java.lang.String, java.lang.String)
	 */
	@Override
	public void exportTable(String filename, String tablename){
		try {
			Statement stmt = connect.createStatement();
			String stmtString = "";
			if(tablename.equals("cityData_1997_2013"))
				stmtString = "(SELECT \'id\',\'time\',\'budget\',\'levee_invest\',\'subsidyrate\',\'levee_quality\',\'flood_damage\',\'road_repair_invest\',\'social_benefit\',\'max_Q\') UNION (SELECT * FROM cityData_1997_2013)";
			else if(tablename.equals("farmerData_1997_2013"))
				stmtString = "(SELECT \'id\',\'time\',\'total_land\',\'waterRetention_area\',\'crop_area\',\'fallow_area\',\'subsidy_rate\',\'corn_price_per_bushel\',\'bushels_per_acre\',\'grow_precip\',\'cropProfit_acre\',\'production_cost_per_acre\',\'total_profits\',\'money_balance\',\'CropCN\',\'current_farmerland_CN\',\'cornConsumption\',\'utilityOfConsumption\') UNION (SELECT * FROM farmerData_1997_2013)";

			ResultSet rs = stmt.executeQuery(stmtString);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			
            CSVWriter writer = new CSVWriter(new FileWriter(filename));
            List<String[]> data = new ArrayList<String[]>();
            while(rs.next()){
                 String[] strArray = new String[numberOfColumns];
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

	@Override
	public int insertDBreturnKey(String insertStatement) {
		// TODO Auto-generated method stub
		return 0;
	}

}
