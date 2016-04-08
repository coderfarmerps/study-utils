package com.mrlu.bigData.mysql2excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Created by stefan on 16-3-31.
 */
public class MySQL2Excel {
    private static Connection connection; //只获取一个链接
    private static PreparedStatement preparedStatement; //做一次预编译
    private static Long startTime;
//    private static Map<String,String> fieldMap = new HashMap<>();
    private static List<FieldDefinition> fieldDefinitionList = new LinkedList<>();

    static{
        try {
            File file = new File(Constants.outputDir);
            if(file.exists()){
                file.delete();
            }
            file.mkdir();

            startTime = System.currentTimeMillis();
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(Constants.jdbcUrl, Constants.jdbcUserName, Constants.jdbcPassword);
            preparedStatement = connection.prepareStatement(Constants.prepareSql);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void mysql2BatchExcel(){

    }

    //mysql数据导入到同一个excel中
    public static void mysql2OneExcel(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Constants.outputDir + "/1.xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("sheetone");
            createTitle(sheet);
            createContent(sheet, resultSetToCellData(selectMySqlData(null)));
            workbook.write(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //excel内容行，从第二行开始
    private static void createContent(Sheet sheet, List<Map<String,String>> list) {
        int rowIndex = 1;
        for(Map<String,String> map: list){
            Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            /*for (String key: fieldMap.keySet()){
                Cell cell = row.createCell(cellIndex++);
                cell.setCellValue(map.get(key));
            }*/
            for (FieldDefinition field: fieldDefinitionList){
                Cell cell = row.createCell(cellIndex++);
                cell.setCellValue(map.get(field.getPropertyName()));
            }
        }
    }

    //excel第一行，标题行
    private static void createTitle(Sheet sheet) {
        Row row = sheet.createRow(0);
        int cellIndex = 0;
        /*for (String key: fieldMap.keySet()){
            Cell cell = row.createCell(cellIndex++);
            cell.setCellValue(fieldMap.get(key));
        }*/
        for (FieldDefinition field: fieldDefinitionList){
            Cell cell = row.createCell(cellIndex++);
            cell.setCellValue(field.getName());
        }
    }

    //mysql数据导入到同一个csv中
    public static void mysql2CSV(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Constants.outputDir + "/1.csv");
            List<Map<String,String>> dataList = resultSetToCellData(selectMySqlData(null));
            StringBuffer sb = new StringBuffer();
            /*for(String key: fieldMap.keySet()){
                sb.append("\"");
                sb.append(fieldMap.get(key));
                sb.append("\"");
                sb.append(",");
            }*/
            for (FieldDefinition field: fieldDefinitionList){
                sb.append("\"");
                sb.append(field.getName());
                sb.append("\"");
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("\n");
            fileOutputStream.write(sb.toString().getBytes());
            sb = new StringBuffer();

            for(Map<String,String> map: dataList){
                for (FieldDefinition field: fieldDefinitionList){
                    String value = map.get(field.getPropertyName());
                    sb.append("\"");
                    if(null == value) value = "";
                    sb.append(value);
                    sb.append("\"");
                    sb.append(",");
                }
                /*for (String key: fieldMap.keySet()){
                    String value = map.get(key);
                    sb.append("\"");
                    if(null == value) value = "";
                    sb.append(value);
                    sb.append("\"");
                    sb.append(",");
                }*/
                sb.deleteCharAt(sb.length()-1);
                sb.append("\n");
                fileOutputStream.write(sb.toString().getBytes());
                sb = new StringBuffer();
            }
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //数据库操作
    public static ResultSet selectMySqlData(List<Integer> params){
        try {
            preparedStatement.clearParameters();
            if(params != null) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setInt(i + 1, params.get(i));
                }
            }
            return preparedStatement.executeQuery();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //orm
    public static List<Map<String,String>> resultSetToCellData(ResultSet resultSet){
        if (null == resultSet){
            return null;
        }
        List<Map<String,String>> resultList = new LinkedList<>();
        try {
            while (resultSet.next()){
                Map<String,String> map = new HashMap<>();
                /*for (String fieldName: fieldMap.keySet()) {
                    map.put(fieldName, resultSet.getString(fieldName));
                }*/
                for (FieldDefinition field: fieldDefinitionList){
                    map.put(field.getPropertyName(), resultSet.getString(field.getPropertyName()));
                }
                resultList.add(map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultList;
    }

    //解析xml
    public static void parseXmlConfig(){
        String xmlPath = MySQL2Excel.class.getResource("/excel.xml").toString();
        try {
            //org.w3c.Document
            //用dom4j操作更便捷
            Document document = DocumentHelper.newDocumentBuilder().parse(xmlPath);
            NodeList nodeList = document.getElementsByTagName("fields");
            for(int i=0; i<nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                NodeList childNodeList = node.getChildNodes();
                for(int j=0; j<childNodeList.getLength(); j++){
                    Node eachNode = childNodeList.item(j);
                    if(eachNode.getNodeName().equals("field")) {
                        NamedNodeMap namedNodeMap = eachNode.getAttributes();
                        Node name = namedNodeMap.getNamedItem("name");
                        Node property = namedNodeMap.getNamedItem("property");
                        Node order = namedNodeMap.getNamedItem("order");
                        int cellOrder = 0;
                        if(order != null){
                            cellOrder = Integer.valueOf(order.getNodeValue());
                        }
                        fieldDefinitionList.add(new FieldDefinition(name.getNodeValue(), property.getNodeValue(), cellOrder));
//                        fieldMap.put(property.getNodeValue(), name.getNodeValue());
                    }
                }
            }

            Collections.sort(fieldDefinitionList, new Comparator<FieldDefinition>() {
                @Override
                public int compare(FieldDefinition o1, FieldDefinition o2) {
                    return o1.getOrder().compareTo(o2.getOrder());
                }
            });
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        parseXmlConfig();
//        mysql2CSV();
        mysql2OneExcel();
    }
}
