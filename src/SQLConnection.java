import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

public class SQLConnection {
	private Driver driver;
	private String url;
	private String db;
	private String user;
	private String pass;
	
	private Connection connection;
	private boolean connected = false;
	private LinkedList<Query> openQueries;
	private LinkedList<Query> closedQueries;
	private boolean cacheQueries;
	
	public SQLConnection(String url, String db, String user, String pass) {
		this.url = url;
		this.db = db;
		this.user = user;
		this.pass = pass;
		this.driver = Driver.getDefault();
	}
	public SQLConnection setDriver(Driver driver) {
		this.driver = driver;
		return this;
	}
	public SQLConnection cacheQueries(boolean toggle) {
		this.cacheQueries = toggle;
		if(toggle) {
			this.openQueries = new LinkedList<Query>();
			this.closedQueries = new LinkedList<Query>();
		} else {
			this.openQueries = null;
			this.closedQueries = null;
		}
		return this;
	}
	
	public SQLConnection connect() {
		try {
			Class.forName(driver.driverClass);
			String jdbcUrl = "jdbc:" + driver.jdbcProtocol + ":" + url + db;
			Util.debug("Connecting to DB ... [" + jdbcUrl + "|" + user + "|**********]");
			Properties props = new Properties();
			props.setProperty("retainStatementAfterResultSetClose", "true");
			props.setProperty("user", user);
			props.setProperty("password", pass);
			props.setProperty("useSSL", "false");
			connection = DriverManager.getConnection(jdbcUrl, props);
			connected = true;
			Util.debug("Connected!");
		} catch(SQLException e) {
			connected = false;
			SQLEWrapper.printSqlException(e);
		} catch(Exception e) {
			connected = false;
			e.printStackTrace();
		}
		return this;
	}
	public SQLConnection save(Query q) {
		openQueries.push(q);
		return this;
	}
	public SQLConnection finish() {
		if(!SQLEWrapper.handle(() -> {
			connection.endRequest();
			connection.close();
			return true;
		})) Util.debug("Error when closing connection! It might still be open!");
		return this;
	}
	
	public Query customQuery(String sql) {
		Query q = new Query(this);
		q.clear();
		return q;
	}
	public Query insertInto(String table, String ... values) {
		return new Query(this).insertInto(table, null, values);
	}
	public Query insertInto(String table, String[] keys, String[] ... values) {
		if(keys == null) return new Query(this).insertInto(table, null, values);
		
		for(int i = 0; i < values.length; i++) if(keys.length != values[i].length) values[i] = Arrays.copyOf(values[i], keys.length);
		return new Query(this).insertInto(table, keys, values);
	}
	public Query select(String ... columns) {
		return new Query(this).select(columns);
	}
	public Query deleteFrom(String table) {
		return new Query(this).deleteFrom(table);
	}
	
	public Query.Result executeQuery(Query query) {
		if(connected) {
			try {
				Statement s = connection.createStatement();
				ResultSet rs = null;
				query.setStatement(s);
				Query.Result result = null;
				
				switch(query.getType()) {
				case DELETE:
				case UPDATE:
				case INSERT:
					int rowChanged = s.executeUpdate(query.getQueryAsString());
					//rs = s.executeQuery("SELECT * FROM " + query.table + " LIMIT 1 OFFSET " + (rowChanged - 1));
					result = query.new Result(query, rowChanged);
					break;
				case SELECT:
					// do nothing because it makes no real difference.
					rs = s.executeQuery(query.getQueryAsString());
					result = query.new Result(query, rs);
					break;
				default:
				case NULL:
					// do nothing, because the query building hasn't even been started
					break;
				}
				
				if(cacheQueries && openQueries.contains(query)) {
					openQueries.remove(query);
					closedQueries.push(query);
				}
				
				return result;
			} catch(SQLException e) {
				SQLEWrapper.printSqlException(e);
				return null;
			}
		}
		return query.new Result(query, 0);
	}
	public Query.Result[] executeOpenQueries() {
		synchronized(openQueries) {
			Query.Result[] results = new Query.Result[openQueries.size()];
			for(int i = 0; i < openQueries.size(); i++) {
				results[i] = executeQuery(openQueries.get(i));
			}
			return results;
		}
	}
	
	public enum Driver {
		MYSQL("com.mysql.jdbc.Driver", "mysql");
		
		public final String driverClass;
		public final String jdbcProtocol;
		
		private Driver(String driverClass, String jdbcProtocol) {
			this.driverClass = driverClass;
			this.jdbcProtocol = jdbcProtocol;
		}
		
		public static Driver getDefault() {
			return Driver.MYSQL;
		}
	}
}
