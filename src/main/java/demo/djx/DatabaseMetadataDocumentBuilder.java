package demo.djx;

import com.mysql.jdbc.Driver;
import org.apache.commons.lang3.StringUtils;
import org.xmind.core.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

/**
 * @author dongjingxiang
 * @date 2021/7/27 13:49
 */
public class DatabaseMetadataDocumentBuilder {
    public static final String url = "jdbc:mysql://101.201.35.77:3306/cl_mes?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=False&serverTimezone=GMT%2B8";
    public static final String userName = "chaoliuroot";
    public static final String password = "chaoliupwd1234";
    public static final String dbName = "cl_mes";
    public static final String markdownName = "cl_mes.md";
    private static final Connection conn = getConn();

    public static Connection getConn() {
        try {
            Class.forName(Driver.class.getName());
            return DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        //createXmind("mango_server.xmind");
        createMarkdown(markdownName);

    }

    private static void createXmind(String file) throws SQLException, IOException, CoreException {
        // 创建思维导图的工作空间
        IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
        IWorkbook workbook = workbookBuilder.createWorkbook();

        // 获得默认sheet
        ISheet primarySheet = workbook.getPrimarySheet();

        // 获得根主题
        ITopic rootTopic = primarySheet.getRootTopic();
        // 设置根主题的标题
        rootTopic.setTitleText("mango 数据库结构");


        ArrayList<TableInfo> tableInfos = getTableInfos("mango");
        for (TableInfo tableInfo : tableInfos) {
            ITopic tableTopic = workbook.createTopic();
            tableTopic.setTitleText(tableInfo.getTableName());
            if (StringUtils.isNotBlank(tableInfo.getRemark())) {
                tableTopic.addLabel(tableInfo.getRemark());
            }

            System.out.println(tableInfo);

            ArrayList<ColumnInfo> columns = getColumns(tableInfo.getTableName());
            for (ColumnInfo column : columns) {
                ITopic columnTopic = workbook.createTopic();
                columnTopic.setTitleText(column.getColumnName());
                if (StringUtils.isNotBlank(column.getColumnComment())) {
                    columnTopic.addLabel(column.getColumnComment());
                }
                if (StringUtils.isNotBlank(column.getColumnType())) {
                    columnTopic.addLabel(column.getColumnType());
                }
                tableTopic.add(columnTopic, ITopic.ATTACHED);
                System.out.println(column);
            }
            rootTopic.add(tableTopic, ITopic.ATTACHED);
            System.out.println();
        }
        workbook.save(file);
    }


    public static void createMarkdown(String file) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8);
        ArrayList<TableInfo> tables = getTableInfos(dbName);


        writer.append("# mango 数据库表结构");
        writer.newLine();
        writer.newLine();
        writer.append("|表名|描述|");
        writer.newLine();
        writer.append("|:---|:---|");
        writer.newLine();
        for (TableInfo table : tables) {
            writer.append(String.format("|`%s`|%s|", table.getTableName(), table.getRemark().replaceAll("\n", "")));
            writer.newLine();
        }
        writer.newLine();
        for (TableInfo table : tables) {
            writer.append("## ").append(table.getTableName());
            writer.newLine();
            writer.append("\t").append(table.getRemark());
            writer.newLine();
            writer.append("|列名|类型|描述|\n");
            writer.append("|:---|:---|:---|\n");
            ArrayList<ColumnInfo> columns = getColumns(table.getTableName());
            for (ColumnInfo column : columns) {
                writer.append(String.format("|%s|%s|%s|\n", column.getColumnName(),
                        column.getColumnType(), column.getColumnComment().replaceAll("\n", "")));
            }
            writer.append("\n\n");
        }
        writer.close();

    }

    private static ArrayList<ColumnInfo> getColumns(String tableName) throws SQLException {
        ArrayList<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
        String sql = String.format("SELECT column_name,column_type,column_comment  " +
                "FROM INFORMATION_SCHEMA.COLUMNS where table_schema = '" + dbName + "' and table_name = '%s'", tableName);
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnName(rs.getString(1));
            columnInfo.setColumnType(rs.getString(2));
            columnInfo.setColumnComment(rs.getString(3));
            columnInfos.add(columnInfo);
        }
        ps.close();
        return columnInfos;

    }

    private static ArrayList<TableInfo> getTableInfos(String schema) throws SQLException {
        String sql = String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s'",
                schema);
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ArrayList<TableInfo> tableInfos = new ArrayList<TableInfo>();
        while (rs.next()) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(rs.getString("TABLE_NAME"));
            tableInfo.setRemark(rs.getString("TABLE_COMMENT"));
            tableInfos.add(tableInfo);
        }
        ps.close();
        return tableInfos;
    }

}
