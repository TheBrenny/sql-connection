package com.thebrenny.sqlconnection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;

public class Query {
	private static int queryID = 0;
	private SQLConnection connection;
	private QueryType type;
	private Statement statement;
	private String name = "Query_" + queryID++;
	private LinkedList<String> queries;
	private String query;
	private boolean whered = false;
	private boolean valued = false;
	private Result result;
	
	protected Query(SQLConnection connection) {
		query = "";
		this.queries = new LinkedList<String>();
		this.type = QueryType.NULL;
		this.connection = connection;
	}
	public Query setStatement(Statement statement) {
		this.statement = statement;
		return this;
	}
	
	public Query nameQuery(String name) {
		this.name = name;
		return this;
	}
	
	public Query insertInto(String table, String[] keys, String[] ... values) {
		if(wasExecuted() || this.type != QueryType.NULL) return this;
		
		insertInto(table);
		if(keys != null) keys(keys);
		values(values);
		
		return this;
	}
	public Query insertInto(String table) {
		if(wasExecuted() || this.type != QueryType.NULL) return this;
		this.type = QueryType.INSERT;
		query = "INSERT INTO " + table + " ";
		return this;
	}
	public Query keys(String[] keys) {
		if(wasExecuted()) return this;
		query += "(" + Util.joinArray(", ", keys) + ") ";
		return this;
	}
	public Query values(String[] ... values) {
		if(wasExecuted()) return this;
		for(int i = 0; i < values.length; i++) {
			values(values[i]);
			if(i < values.length - 1) query += ", ";
		}
		return this;
	}
	public Query values(String ... values) {
		if(wasExecuted()) return this;
		if(!valued) query += "VALUES ";
		valued = true;
		query += "(" + String.join(", ", values) + ") ";
		return this;
	}
	
	public Query deleteFrom(String table) {
		if(wasExecuted() || this.type != QueryType.NULL) return this;
		this.type = QueryType.DELETE;
		query = "DELETE FROM " + table + " ";
		return this;
	}
	public Query select(String ... columns) {
		if(wasExecuted() || this.type != QueryType.NULL) return this;
		this.type = QueryType.SELECT;
		query = "SELECT ";
		if(columns.length == 0) columns = new String[] {"*"};
		query += String.join(", ", columns) + " ";
		return this;
	}
	public Query from(String table) {
		if(wasExecuted()) return this;
		query += "FROM " + table + " ";
		return this;
	}
	public Query where(String condition) {
		if(wasExecuted()) return this;
		if(!whered) query += "WHERE " + condition + " ";
		else return and(condition);
		whered = true;
		return this;
	}
	public Query where(String a, String operator, String b) {
		return where(a + operator + "'" + b + "'");
	}
	public Query where(String a, String operator, int b) {
		return where(a + operator + b);
	}
	public Query and(String condition) {
		return and(condition, false);
	}
	public Query and(String condition, boolean brackets) {
		if(wasExecuted()) return this;
		if(brackets) query += "(";
		query += "AND " + condition + " ";
		if(brackets) query += ")";
		return this;
	}
	public Query and(String a, String operator, String b) {
		return and(a + operator + "'" + b + "'");
	}
	public Query and(String a, String operator, String b, boolean brackets) {
		return and(a + operator + "'" + b + "'", brackets);
	}
	public Query and(String a, String operator, int b) {
		return and(a + operator + b);
	}
	public Query and(String a, String operator, int b, boolean brackets) {
		return and(a + operator + b, brackets);
	}
	
	public Query or(String condition) {
		return or(condition, false);
	}
	public Query or(String condition, boolean brackets) {
		if(wasExecuted()) return this;
		if(brackets) query += "(";
		query += "OR " + condition + " ";
		if(brackets) query += ")";
		return this;
	}
	
	public Query sql(String sql) {
		if(wasExecuted()) return this;
		query += sql + " ";
		return this;
	}
	public Query clear() {
		if(wasExecuted()) return this;
		query = "";
		return this;
	}
	public Result execute() {
		if(!wasExecuted()) query = query.strip().replace(";", "") + ";";
		return (result = connection.executeQuery(this));
	}
	public SQLConnection save() {
		return connection.save(this);
	}
	public Query renew() {
		return renew(true);
	}
	public Query renew(boolean saveQuery) {
		if(wasExecuted()) result.finish();
		if(saveQuery) queries.push(this.query);
		this.query = "";
		this.type = QueryType.NULL;
		this.whered = false;
		this.valued = false;
		return this;
	}
	public SQLConnection finish() {
		if(this.result != null) this.result.finish();
		SQLEWrapper.handle(() -> {
			this.statement.close();
			return true;
		});
		return connection;
	}
	
	public Result getResult() {
		return result;
	}
	public String getQueryAsString() {
		return query;
	}
	public String getName() {
		return name;
	}
	public QueryType getType() {
		return type;
	}
	
	public boolean wasExecuted() {
		return result != null && result.set != null && !SQLEWrapper.handle(() -> result.set.isClosed());
	}
	
	public class Result implements Iterable<Object[]> {
		private Iterator<Object[]> iterator;
		private Query query;
		private ResultSet set;
		private int rowHead;
		private int size = -1;
		private LinkedList<Integer> headStack;
		
		public Result(Query query, int rowsChanged) {
			this.query = query;
			this.rowHead = rowsChanged;
		}
		public Result(Query query, ResultSet set) {
			this.query = query;
			this.set = set;
			this.headStack = new LinkedList<Integer>();
			this.iterator = buildIterator(this);
			start();
		}
		public Result start() {
			if(set == null) return this;
			first();
			return this;
		}
		public boolean finish() {
			if(set == null) return true;
			return SQLEWrapper.handle(() -> {
				this.set.close();
				return true;
			});
		}
		
		public int first() {
			if(set == null) return this.rowHead;
			return SQLEWrapper.handle(() -> {
				set.first();
				return currentRow();
			});
		}
		public int previousRow() {
			if(set == null) return this.rowHead;
			return previousRow(false);
		}
		public int previousRow(boolean wrap) {
			if(set == null) return this.rowHead;
			return SQLEWrapper.handle(() -> {
				if(set.previous()) return currentRow();
				if(wrap) return last();
				else return first();
			});
		}
		public int currentRow() {
			if(set == null) return this.rowHead;
			return (rowHead = SQLEWrapper.handle(() -> set.getRow()));
		}
		public int gotoRow(int row) {
			if(set == null) return this.rowHead;
			return SQLEWrapper.handle(() -> {
				set.absolute(row);
				return currentRow();
			});
		}
		public boolean hasNext() {
			if(set == null) return false;
			return rowHead < size();
		}
		public int nextRow() {
			if(set == null) return this.rowHead;
			return nextRow(false);
		}
		public int nextRow(boolean wrap) {
			if(set == null) return this.rowHead;
			return SQLEWrapper.handle(() -> {
				if(set.next()) return currentRow();
				if(wrap) return first();
				else return last();
			});
		}
		public int last() {
			if(set == null) return this.rowHead;
			return SQLEWrapper.handle(() -> {
				set.last();
				return currentRow();
			});
		}
		public void pushRow() {
			if(set == null) return;
			this.headStack.push(rowHead);
		}
		public void popRow() {
			if(set == null) return;
			this.rowHead = this.headStack.pop();
			gotoRow(rowHead);
		}
		
		public String getColumnName(int col) {
			if(set == null) return null;
			return SQLEWrapper.handle(() -> set.getMetaData().getColumnLabel(col));
		}
		public String[] getColumnNames() {
			if(set == null) return null;
			String[] cols = new String[getColumnCount()];
			for(int i = 0; i < cols.length; i++) cols[i] = getColumnName(i + 1);
			return cols;
		}
		public int length() {
			return getColumnCount();
		}
		public int getColumnCount() {
			if(set == null) return 0;
			Integer ret = SQLEWrapper.handle(() -> set.getMetaData().getColumnCount());
			if(ret == null) ret = -1;
			return ret.intValue();
		}
		
		public int size() {
			return getRowCount();
		}
		public int getRowCount() {
			if(set == null) return rowHead;
			if(size != -1) return size;
			pushRow();
			int ret = last();
			popRow();
			return (size = ret);
		}
		
		public Object getData(int col) {
			if(set == null) return null;
			return SQLEWrapper.handle(() -> set.getObject(col));
		}
		public Object getData(String colName) {
			if(set == null) return null;
			String[] colNames = getColumnNames();
			for(int i = 0; i < colNames.length; i++) {
				if(colNames[i].equals(colName)) return getData(i + 1); // plus 1 because sql is 1-based
			}
			return null;
		}
		public Object[] getRowData() {
			return getRowData(currentRow());
		}
		public Object[] getRowData(int row) {
			if(set == null) return null;
			Object[] ret = new Object[getColumnCount()];
			pushRow();
			gotoRow(row);
			for(int col = 0; col < ret.length; col++) {
				ret[col] = getData(col + 1);
			}
			popRow();
			return ret;
		}
		
		public String getColumns() {
			if(set == null) return null;
			return String.join(", ", getColumnNames());
		}
		public String getRow() {
			if(set == null) return "Rows changed: " + rowHead;
			return getRow(SQLEWrapper.handle(() -> set.getRow()));
		}
		public String getRow(int row) {
			if(set == null) return "Rows changed: " + rowHead;
			String ret = "Row " + row + ": ";
			Object[] rowData = getRowData(row);
			ret += Util.joinArray(", ", rowData);
			return ret;
		}
		public String getName() {
			String qName = query.getName();
			if(qName.startsWith("Query_")) return "Result_" + qName.substring(qName.indexOf("_") + 1);
			return qName + " (result)";
		}
		public String toString() {
			return "Result{name:" + this.getName() + ", query:" + query.getName() + ", col_count:" + getColumnCount() + ", row_count:" + getRowCount() + "}";
		}
		public Iterator<Object[]> iterator() {
			return this.iterator;
		}
		
		private Iterator<Object[]> buildIterator(Result r) {
			return new Iterator<Object[]>() {
				private int innerHead = 0;
				
				public boolean hasNext() {
					return innerHead < r.size();
				}
				
				@Override
				public Object[] next() {
					return r.getRowData(++innerHead);
				}
				
			};
		}
	}
	
	public static enum QueryType {
		SELECT, UPDATE, INSERT, DELETE, NULL;
	}
}
