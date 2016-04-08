package com.mrlu.bigData.excel2mysql;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Date;

/**
 * Created by stefan on 16-3-28.
 * 单线程模式
 */
public class CSV2MySQL {
    private static Connection connection; //只获取一个链接
    private static PreparedStatement preparedStatement; //做一次预编译
    private static Long startTime = System.currentTimeMillis();
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(Constants.jdbcUrl, Constants.jdbcUserName, Constants.jdbcPassword);
            preparedStatement = connection.prepareStatement(Constants.prepareSql);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void csv2MySQl(){
        System.out.println("start Time: " + (new Date()));
        String path = Constants.cvsPath;
        File file = new File(path);
        if(!file.exists()){
            System.out.println("file not found!!!");
            return;
        }
        try {
            int lineNum = 0 ;
            Scanner scanner = new Scanner(file);
            List<String[]> stringList = new ArrayList<>();
            while (scanner.hasNextLine()){
                if(lineNum++ < 3) { //前边几行不处理(空行，index行)
                    scanner.nextLine();
                    continue;
                }
                stringList.add(splitRawValue(scanner.nextLine()));
                if(stringList.size() == 1000){ //每次处理1000行数据，避免内存使用过多
                    batchInsert(stringList);
                    stringList.clear();
                }
            }
            if(stringList.size() != 0){
                batchInsert(stringList);
            }
            stringList.clear();
            scanner.close();
            Long endTime = System.currentTimeMillis();
            System.out.println("already use time:" + (endTime-startTime));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if(preparedStatement != null)
                    preparedStatement.close();
                if(connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("end Time: " + (new Date()));
    }

    private String trimString(String originStr) {
        if(StringUtils.isBlank(originStr)) return "";
        originStr = StringUtils.replace(originStr, " ", "");
        originStr = StringUtils.replace(originStr, "\n\r", "");
        originStr = StringUtils.replace(originStr, "\n", "");
        originStr = StringUtils.trim(originStr);
        return StringUtils.replace(originStr, "\t", "");
    }

    private String[] splitRawValue(String rowOriginValue){
        String[] rowNewValues = rowOriginValue.split(",");
        for (int i=0; i<rowNewValues.length; i++){
            if(StringUtils.isBlank(rowNewValues[i]) || rowNewValues[i].length() < 3) continue;
            rowNewValues[i] = rowNewValues[i].substring(1, rowNewValues[i].length()-1);
        }
        return rowNewValues;
    }

    private void batchInsert(List<String[]> rowList) {
        try {
            //清空上一次处理的数据
            preparedStatement.clearParameters();
            preparedStatement.clearBatch();

            for (String[] eachRow : rowList) {
                for(int i=0; i<eachRow.length; i++){
                    preparedStatement.setString(i+1, trimString(eachRow[i]));
                }
                if(eachRow.length < 11){
                    for(int i=eachRow.length; i<=11; i++){
                        preparedStatement.setString(i, null);
                    }
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
