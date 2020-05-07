package com.thebrenny.sqlconnection;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLEWrapper<R> {
	public R run() throws SQLException;
	
	public static <RT> RT handle(SQLEWrapper<RT> s) {
		try {
			return (RT) s.run();
		} catch(SQLException e) {
			printSqlException(e);
		}
		return null;
	}
	public static void printSqlException(SQLException e) {
		System.out.println("SQLException: " + e.getMessage());
		System.out.println("SQLState:     " + e.getSQLState());
		System.out.println("VendorError:  " + e.getErrorCode());
		e.printStackTrace();
	}
}
