package org.utilities.internal;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.utilities.internal.HSQLDataBaseManager;

import org.junit.Test;

public class HSQLDataBaseManagerTest {

	protected static HSQLDataBaseManager dbManager;
	
	@BeforeClass
	public static void setUp() {
		dbManager = new HSQLDataBaseManager();
	}
	
	@Test
	public void testConnectDB() {
		assertNotNull(dbManager);
	}

	@Test
	public void testQueryDB() {
		ResultSet rs = dbManager.queryDB("select hourly_precip from lowPrecipScenario where time = '1999-01-01 00:00:00'");
		try {
			rs.next();
			Double precip =  rs.getDouble("hourly_precip");
			assertTrue(precip == 0.0); 
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Invalid return from DB");
		}
	}

	@Test
	public void testDeleteDB() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertDB() {
		fail("Not yet implemented");
	}

	@Test
	public void testExportTable() {
		fail("Not yet implemented");
	}

}
