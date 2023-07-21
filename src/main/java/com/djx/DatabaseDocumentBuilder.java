package com.djx;

import com.djx.entity.ColumnInfo;
import com.djx.entity.Config;
import com.djx.entity.TableInfo;
import com.mysql.cj.jdbc.Driver;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dongjingxiang
 * @date 2021/7/27 13:49
 */
public class DatabaseDocumentBuilder {
	private static final String docPath = "doc/";
	private static Config config;
	private static Connection conn;

	public static Connection getConn() {
		Connection conn = null;
		try {
			Class.forName(Driver.class.getName());
			String url = "jdbc:mysql://" + config.getHostname() + ":" + config.getPort() + "/" + config.getDbName()
					+ "?useUnicode=true&characterEncoding=utf8&useSSL=True";
			conn = DriverManager.getConnection(
					url,
					config.getUsername(),
					config.getPassword());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return conn;
	}

	public static void createMarkdown() throws Exception {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(docPath + config.getDbName() + ".md"), StandardCharsets.UTF_8)) {
			List<TableInfo> tables = getTableInfos(config.getDbName())
					.stream().filter(table -> {
						if (config.getExportTableNames() == null || config.getExportTableNames().isEmpty()) {
							return true;
						}
						return config.getExportTableNames().contains(table.getTableName());
					})
					.collect(Collectors.toList());

			writer.append("# ").append(config.getDbName()).append(" 数据库表结构");
			writer.newLine();
			writer.newLine();
			writer.append("|表名|描述|");
			writer.newLine();
			writer.append("|:---:|:---:|");
			writer.newLine();
			for (TableInfo table : tables) {
				writer.append(String.format("|`%s`|%s|", table.getTableName(), table.getRemark().replaceAll("\n", "")));
				writer.newLine();
			}
			writer.newLine();
			for (TableInfo table : tables) {
				writer.append("## ").append(table.getTableName());
				writer.newLine();
				writer.append("**").append(table.getRemark()).append("**");
				writer.newLine();
				writer.append("|列名|类型|描述|\n");
				writer.append("|:---:|:---:|:---:|\n");
				ArrayList<ColumnInfo> columns = getColumns(table.getTableName());
				for (ColumnInfo column : columns) {
					writer.append(String.format("|`%s`|%s|%s|\n", column.getColumnName(),
							column.getColumnType(), column.getColumnComment().replaceAll("\n", "")));
				}
				writer.append("\n\n");
			}
		}

	}

	private static ArrayList<ColumnInfo> getColumns(String tableName) throws SQLException {
		ArrayList<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
		String sql = String.format("SELECT column_name,column_type,column_comment  " +
				"FROM INFORMATION_SCHEMA.COLUMNS where table_schema = '" + config.getDbName() + "' and table_name = '%s'", tableName);
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ColumnInfo columnInfo = new ColumnInfo();
				columnInfo.setColumnName(rs.getString(1));
				columnInfo.setColumnType(rs.getString(2));
				columnInfo.setColumnComment(rs.getString(3));
				columnInfos.add(columnInfo);
			}
		}
		return columnInfos;

	}

	private static ArrayList<TableInfo> getTableInfos(String schema) throws SQLException {
		String sql = String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' order by TABLE_NAME",
				schema);
		ArrayList<TableInfo> tableInfos;
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			tableInfos = new ArrayList<>();
			while (rs.next()) {
				TableInfo tableInfo = new TableInfo();
				tableInfo.setTableName(rs.getString("TABLE_NAME"));
				tableInfo.setRemark(rs.getString("TABLE_COMMENT"));
				tableInfos.add(tableInfo);
			}
		}
		return tableInfos;
	}

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			config = Config.load(args[0]);
		} else {
			config = Config.getInstance();
		}
		conn = getConn();
		new File(docPath).mkdirs();
		// createXmind();
		createMarkdown();
	}
}
