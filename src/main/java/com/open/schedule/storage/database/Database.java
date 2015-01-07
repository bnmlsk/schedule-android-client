package com.open.schedule.storage.database;

import com.open.schedule.io.Tables;

import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.open.schedule.storage.tables.*;
import com.open.schedule.storage.tables.Table.Permission;
import com.open.schedule.utility.Utility;

public class Database {
	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;

	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);

	public Database(Context context) {
		this.dbHelper = new DatabaseHelper(context);
		this.database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void loadTables(Tables tables) {
		String[] columns = {DatabaseHelper.INNER_ID, DatabaseHelper.GLOBAL_ID, DatabaseHelper.UPDATE_TIME};
		Cursor cursorTables = database.query(DatabaseHelper.TABLE_TABLES, columns, null, null, null, null, null);

		if (cursorTables.moveToFirst()) {
			int id = cursorTables.getColumnIndex(DatabaseHelper.INNER_ID);
			int globalId = cursorTables.getColumnIndex(DatabaseHelper.GLOBAL_ID);
			int updateTime = cursorTables.getColumnIndex(DatabaseHelper.UPDATE_TIME);
			while(!cursorTables.isAfterLast()) {
				Integer idVal = cursorTables.getInt(id);
				Integer globalIdVal = cursorTables.getInt(globalId);
				Long updateTimeVal = cursorTables.getLong(updateTime);
				tables.createTable(idVal, globalIdVal, updateTimeVal);
				cursorTables.moveToNext();
			}
		}
		cursorTables.close();

		String[] columnsChanges = {DatabaseHelper.TABLE_ID, DatabaseHelper.TIME, DatabaseHelper.USER_ID, 
				DatabaseHelper.CHANGE_NAME, DatabaseHelper.CHANGE_DESCRIPTION};
		Cursor cursorChanges = database.query(DatabaseHelper.TABLE_TABLE_CHANGES, columnsChanges, null, null, null, null, null);

		if (cursorChanges.moveToFirst()) {
			int tableId = cursorChanges.getColumnIndex(DatabaseHelper.TABLE_ID);
			int time = cursorChanges.getColumnIndex(DatabaseHelper.TIME);
			int userId = cursorChanges.getColumnIndex(DatabaseHelper.USER_ID);
			int name = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_NAME);
			int description = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_DESCRIPTION);

			while(!cursorChanges.isAfterLast()) {
				Integer tableIdVal = cursorChanges.getInt(tableId);
				Integer userIdVal = cursorChanges.getInt(userId);
				Long timeVal = cursorChanges.getLong(time);
				String nameVal = cursorChanges.getString(name);
				String descriptionVal = cursorChanges.getString(description);
				tables.changeTable(tableIdVal, userIdVal, timeVal, nameVal, descriptionVal);
				cursorChanges.moveToNext();
			}
		}
		cursorChanges.close();

		loadPermissions(tables);
		loadTasks(tables);
	}

	private void loadTasks(Tables tables) {
		String[] columns = {DatabaseHelper.INNER_ID, DatabaseHelper.TABLE_ID, DatabaseHelper.GLOBAL_ID, DatabaseHelper.UPDATE_TIME};
		Cursor cursorTasks = database.query(DatabaseHelper.TABLE_TASKS, columns, null, null, null, null, null);
		if (cursorTasks.moveToFirst()) {
			int id = cursorTasks.getColumnIndex(DatabaseHelper.INNER_ID);
			int tableId = cursorTasks.getColumnIndex(DatabaseHelper.TABLE_ID);
			int globalId = cursorTasks.getColumnIndex(DatabaseHelper.GLOBAL_ID);
			int updateTime = cursorTasks.getColumnIndex(DatabaseHelper.UPDATE_TIME);
			while(!cursorTasks.isAfterLast()) {
				Integer idVal = cursorTasks.getInt(id);
				Integer tableIdVal = cursorTasks.getInt(tableId);
				Integer globalIdVal = cursorTasks.getInt(globalId);
				Long updateTimeVal = cursorTasks.getLong(updateTime);
				tables.createTask(tableIdVal, idVal, globalIdVal, updateTimeVal);
				cursorTasks.moveToNext();
			}
		}
		cursorTasks.close();

		String[] columnsChanges = {DatabaseHelper.TABLE_ID, DatabaseHelper.TASK_ID, DatabaseHelper.TIME, DatabaseHelper.USER_ID, 
				DatabaseHelper.CHANGE_NAME, DatabaseHelper.CHANGE_DESCRIPTION, DatabaseHelper.CHANGE_TASK_PERIOD,
				DatabaseHelper.CHANGE_TASK_START_DATE, DatabaseHelper.CHANGE_TASK_END_DATE, 
				DatabaseHelper.CHANGE_TASK_START_TIME, DatabaseHelper.CHANGE_TASK_END_TIME};
		Cursor cursorChanges = database.query(DatabaseHelper.TABLE_TASK_CHANGES, columnsChanges, null, null, null, null, null);

		if (cursorChanges.moveToFirst()) {
			int tableId = cursorChanges.getColumnIndex(DatabaseHelper.TABLE_ID);
			int taskId = cursorChanges.getColumnIndex(DatabaseHelper.TASK_ID);
			int time = cursorChanges.getColumnIndex(DatabaseHelper.TIME);
			int userId = cursorChanges.getColumnIndex(DatabaseHelper.USER_ID);
			int name = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_NAME);
			int description = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_DESCRIPTION);
			int startDate = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_START_DATE);
			int endDate = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_END_DATE);
			int startTime = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_START_TIME);
			int endTime = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_END_TIME);
			int period = cursorChanges.getColumnIndex(DatabaseHelper.CHANGE_TASK_PERIOD);

			while(!cursorChanges.isAfterLast()) {
				Integer tableIdVal = cursorChanges.getInt(tableId);
				Integer taskIdVal = cursorChanges.getInt(taskId);

				Integer userIdVal = cursorChanges.getInt(userId);
				Long timeVal = cursorChanges.getLong(time);
				String nameVal = cursorChanges.getString(name);
				String descVal = cursorChanges.getString(description);
				Integer periodVal = cursorChanges.getInt(period);
				Date startDateVal = null;
				Date endDateVal = null;
				Date startTimeVal = null;
				Date endTimeVal = null;

				try {
					String val = cursorChanges.getString(startDate);
					if (val != null)
						startDateVal = dateFormatter.parse(val);
					
					val = cursorChanges.getString(endDate);
					if (val != null)
						endDateVal = dateFormatter.parse(val);
					
					val = cursorChanges.getString(startTime);
					if (val != null)
						startTimeVal = timeFormatter.parse(val);
					
					val = cursorChanges.getString(endTime);
					if (val != null)
						endTimeVal = timeFormatter.parse(val);
				} catch (ParseException e) {
					Log.w(Database.class.getName(), "Date task changes parsing", e);
				}

				tables.changeTask(tableIdVal, taskIdVal, userIdVal, timeVal, nameVal, descVal, startDateVal, endDateVal, startTimeVal, endTimeVal, periodVal);
				cursorChanges.moveToNext();
			}
		}
		cursorChanges.close();

		loadComments(tables);
	}

	public void loadPermissions(Tables tables) {
		String[] columns = {DatabaseHelper.TABLE_ID, DatabaseHelper.USER_ID, DatabaseHelper.READERS_PERMISSION};
		Cursor cursor = database.query(DatabaseHelper.TABLE_READERS, columns, null, null, null, null, null);
		
		if (cursor.moveToFirst()) {
			int tableId = cursor.getColumnIndex(DatabaseHelper.TABLE_ID);
			int userId = cursor.getColumnIndex(DatabaseHelper.USER_ID);
			int permissionId = cursor.getColumnIndex(DatabaseHelper.READERS_PERMISSION);
			
			while (!cursor.isAfterLast()) {
				Integer tableIdVal = cursor.getInt(tableId);
				Integer userIdVal = cursor.getInt(userId);
				Integer permission = cursor.getInt(permissionId);
				tables.changePermission(tableIdVal, userIdVal, Permission.values()[permission]);
				cursor.moveToNext();
			}
		}
		cursor.close();
	}

	public void loadComments(Tables tables) {
		String[] columns = {DatabaseHelper.TABLE_ID, DatabaseHelper.TASK_ID, DatabaseHelper.TIME, DatabaseHelper.USER_ID, DatabaseHelper.COMMENTS_TEXT};
		Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS, columns, null, null, null, null, DatabaseHelper.TABLE_ID + ", " + DatabaseHelper.TASK_ID + ", " + DatabaseHelper.COMMENTS_TEXT);

		if (cursor.moveToFirst()) {
			int tableId = cursor.getColumnIndex(DatabaseHelper.TABLE_ID);
			int taskId = cursor.getColumnIndex(DatabaseHelper.TASK_ID);
			int time = cursor.getColumnIndex(DatabaseHelper.TIME);
			int userId = cursor.getColumnIndex(DatabaseHelper.USER_ID);
			int textId = cursor.getColumnIndex(DatabaseHelper.COMMENTS_TEXT);

			while(!cursor.isAfterLast()) {
				Integer tableIdVal = cursor.getInt(tableId);
				Integer taskIdVal = cursor.getInt(taskId);
				Integer commentatorVal = cursor.getInt(userId);
				Long timeVal = cursor.getLong(time);
				String textVal = cursor.getString(textId);
				tables.createComment(tableIdVal, taskIdVal, commentatorVal, timeVal, textVal);
				cursor.moveToNext();
			}
		}
		cursor.close();
	}

	public Integer createTable(Integer userId, Long time, String name, String description) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.UPDATE_TIME, Utility.getUnixTime());
		Integer tableId = (int) database.insert(DatabaseHelper.TABLE_TABLES, null, values);
		changeTable(userId, tableId, time, name, description);

		return tableId;
	}

	public Integer createTask(Integer userId, Integer tableId, Long time, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime, Integer period) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.UPDATE_TIME, Utility.getUnixTime());
		Integer taskId = (int) database.insert(DatabaseHelper.TABLE_TASKS, null, values);
		changeTask(userId, tableId, taskId, time, name, description, startDate, endDate, startTime, endTime, period);
		return taskId;
	}

	public void createComment(Integer tableId, Integer taskId, Long time, Integer userId, String text) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.TASK_ID, taskId);
		values.put(DatabaseHelper.USER_ID, userId);
		values.put(DatabaseHelper.TIME, time);
		values.put(DatabaseHelper.COMMENTS_TEXT, text);
		Long res = database.insert(DatabaseHelper.TABLE_COMMENTS, null, values);
		Log.d("Comment creation", res.toString());
	}

	public void changeTable(Integer userId, Integer tableId, Long time, String name, String description) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, userId);
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.TIME, time);
		values.put(DatabaseHelper.CHANGE_NAME, name);
		values.put(DatabaseHelper.CHANGE_DESCRIPTION, description);
		Long res = database.insert(DatabaseHelper.TABLE_TABLE_CHANGES, null, values);
		Log.d("ChangeTable", res.toString());
	}

	public void changeTask(Integer userId, Integer tableId, Integer taskId, Long time, String name, String description, Date startDate, Date endDate, Date startTime, Date endTime, Integer period) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, userId);
		values.put(DatabaseHelper.TABLE_ID, tableId);
		values.put(DatabaseHelper.TASK_ID, taskId);
		values.put(DatabaseHelper.TIME, time);
		values.put(DatabaseHelper.CHANGE_NAME, name);
		values.put(DatabaseHelper.CHANGE_DESCRIPTION, description);
		values.put(DatabaseHelper.CHANGE_TASK_START_DATE, dateFormatter.format(startDate));
		values.put(DatabaseHelper.CHANGE_TASK_END_DATE, dateFormatter.format(endDate));
		values.put(DatabaseHelper.CHANGE_TASK_START_TIME, timeFormatter.format(startTime));
		values.put(DatabaseHelper.CHANGE_TASK_END_TIME, timeFormatter.format(endTime));
		values.put(DatabaseHelper.CHANGE_TASK_PERIOD, period);
		Long res = database.insert(DatabaseHelper.TABLE_TASK_CHANGES, null, values);
		Log.d("ChangeTask", res.toString());
	}

	public void updateTableGlobalId(Integer tableId, Integer tableGlobalId) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.GLOBAL_ID, tableGlobalId);
		database.update(DatabaseHelper.TABLE_TABLES, values, DatabaseHelper.INNER_ID + " =?", new String[] {tableId.toString()});
	}

	public void updateTaskGlobalId(Integer tableGlobalId, Integer taskGlobalId, Integer taskId) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.GLOBAL_ID, tableGlobalId);
		String columns[] = {DatabaseHelper.INNER_ID};
		String whereArg = DatabaseHelper.GLOBAL_ID + " = " + tableGlobalId.toString();
		Cursor cursor;

		cursor = database.query(DatabaseHelper.TABLE_TABLES, columns, whereArg, null, null, null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			return;
		}

		Integer tableId = 0;
		while(!cursor.isAfterLast()) 
			tableId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.INNER_ID));
		cursor.close();

		values = new ContentValues();
		values.put(DatabaseHelper.GLOBAL_ID, taskGlobalId);
		database.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.TABLE_ID + " =? AND " + DatabaseHelper.INNER_ID + " = ?", new String[] {tableId.toString(), taskId.toString()});
	}
	
	public void updateTable(Integer tableId, Long time) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.UPDATE_TIME, time);
		database.update(DatabaseHelper.TABLE_TABLES, values, DatabaseHelper.INNER_ID + " = ?", new String[] {tableId.toString()});
	}

	public void updateTask(Integer tableId, Integer taskId, Long time) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.UPDATE_TIME, time);
		database.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.TABLE_ID + " =? AND " + DatabaseHelper.INNER_ID + " = ?", new String[] {tableId.toString(), taskId.toString()});
	}

	public void setPermission(Integer table_id, Integer user_id, Table.Permission permission) {
		if (permission == Permission.NONE) {
			database.delete(DatabaseHelper.TABLE_READERS, DatabaseHelper.USER_ID + " = ?", new String[] {user_id.toString()});
		}
		else {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.TABLE_ID, table_id);
			values.put(DatabaseHelper.USER_ID, user_id);
			values.put(DatabaseHelper.READERS_PERMISSION, permission.ordinal());
			long res = database.insert(DatabaseHelper.TABLE_READERS, null, values);
			Log.d("Database", Long.valueOf(res).toString());
		}
	}

	public void addUser(Integer user_id, String name) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, user_id);
		values.put(DatabaseHelper.USERS_NAME, name);
		database.insert(DatabaseHelper.TABLE_USERS, null, values);
	}
	
	public void updateUserId(Integer userId) {
		ContentValues userValues = new ContentValues();
		userValues.put(DatabaseHelper.GLOBAL_ID, userId);
		database.update(DatabaseHelper.TABLE_USERS, userValues, DatabaseHelper.GLOBAL_ID + " = 0", null);

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.USER_ID, userId);
		String tablesToUpdate[] = {DatabaseHelper.TABLE_TABLE_CHANGES, DatabaseHelper.TABLE_TASK_CHANGES, DatabaseHelper.TABLE_READERS, DatabaseHelper.TABLE_COMMENTS};
		for (String table : tablesToUpdate) {
			database.update(table, values, DatabaseHelper.USER_ID + " = 0", null);
		}
	}
	
	public void updateLogoutTime(long logoutTime) {
		
	}
}