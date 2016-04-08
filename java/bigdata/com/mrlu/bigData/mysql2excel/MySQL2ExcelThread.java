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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stefan on 16-4-8.
 */
public class MySQL2ExcelThread {
    private List<Connection> connectionList = new ArrayList<>();
    public final String threadSql="xxx"; //数据库记录总数，计算每个线程处理的数据个数
    public final String limitSql = " limit ";
    public final int threadTimeNum = 3000; //线程每次处理的数据
    public int threadDataNum = 0; //每个线程处理的总数据行数
    public int index = 0;//每个线程查询mysql开始的行数
    public int threadCount = 2; //线程数量
    private List<FieldDefinition> fieldDefinitionList = new LinkedList<>();

    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(Constants.jdbcUrl, Constants.jdbcUserName, Constants.jdbcPassword);
            PreparedStatement preparedStatement = connection.prepareStatement(threadSql);
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 0;
            if(resultSet.next()){
                count = resultSet.getInt(1);
            }
            threadDataNum = count/threadCount;
            if(count % threadCount != 0){
                threadCount++;
            }
            preparedStatement.close();
            connection.close();

            for (int i=0; i<threadCount; i++){
                connectionList.add(DriverManager.getConnection(Constants.jdbcUrl, Constants.jdbcUserName, Constants.jdbcPassword));
            }
            System.out.println("connectionList size:" + connectionList.size());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection(){
        return connectionList.remove(0);
    }

    public synchronized int getThreadBeginIndex(){
        int thisIndex = index;
        index += threadDataNum;
        return thisIndex;
    }

    public void threadConcurrency(){
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i=0; i<threadCount; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection connection = getConnection();
                    System.out.println(connection);
                    try {
                        Statement statement = connection.createStatement();
                        String fileName = Constants.outputDir + "/" + Thread.currentThread().getName();
                        int begin = getThreadBeginIndex();
                        int nextIndex = begin + threadTimeNum;
                        int end = begin + threadDataNum;
                        System.out.println(Thread.currentThread().getName() + "begin--->end:" + begin + "<>" + end);
                        int threadIndex = 0;
                        while (begin < end){
                            System.out.println(Thread.currentThread().getName() + "begin--->nextIndex:" + begin + "<>" + nextIndex);
                            System.out.println(Thread.currentThread().getName() + "  statement:" + statement);
//                            mysql2Excel(preparedStatement.executeQuery(), fileName + threadIndex + ".xlsx");
                            mysql2CSV(statement.executeQuery(Constants.prepareSql + limitSql + begin + "," + nextIndex), fileName + threadIndex + ".csv");

                            begin = nextIndex;
                            nextIndex = begin + threadTimeNum;
                            threadIndex++;
                        }
                        statement.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            /*executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Connection connection = getConnection();
                    System.out.println(connection);
                    try {
                        Statement statement = connection.createStatement();
                        String fileName = Constants.outputDir + "/" + Thread.currentThread().getName();
                        int begin = getThreadBeginIndex();
                        int nextIndex = begin + threadTimeNum;
                        int end = begin + threadDataNum;
                        System.out.println(Thread.currentThread().getName() + "begin--->end:" + begin + "<>" + end);
                        int threadIndex = 0;
                        while (begin < end){
                            System.out.println(Thread.currentThread().getName() + "begin--->nextIndex:" + begin + "<>" + nextIndex);
                            System.out.println(Thread.currentThread().getName() + "  statement:" + statement);
//                            mysql2Excel(preparedStatement.executeQuery(), fileName + threadIndex + ".xlsx");
                            mysql2CSV(statement.executeQuery(Constants.prepareSql + limitSql + begin + "," + nextIndex), fileName + threadIndex + ".csv");

                            begin = nextIndex;
                            nextIndex = begin + threadTimeNum;
                            threadIndex++;
                        }
                        statement.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });*/
        }
    }

    //mysql数据导入到同一个csv中
    public void mysql2CSV(ResultSet resultSet, String fileName){
        try {
            System.out.println(Thread.currentThread().getName() + "-->begin output file");
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            List<Map<String,String>> dataList = resultSetToCellData(resultSet);
            StringBuffer sb = new StringBuffer();
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

    public void mysql2Excel(ResultSet resultSet, String fileName){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("sheetone");
            createTitle(sheet);
            createContent(sheet, resultSetToCellData(resultSet));
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
    private void createContent(Sheet sheet, List<Map<String,String>> list) {
        int rowIndex = 1;
        for(Map<String,String> map: list){
            Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            for (FieldDefinition field: fieldDefinitionList){
                Cell cell = row.createCell(cellIndex++);
                cell.setCellValue(map.get(field.getPropertyName()));
            }
        }
    }

    //excel第一行，标题行
    private void createTitle(Sheet sheet) {
        Row row = sheet.createRow(0);
        int cellIndex = 0;
        for (FieldDefinition field: fieldDefinitionList){
            Cell cell = row.createCell(cellIndex++);
            cell.setCellValue(field.getName());
        }
    }

    //orm
    public List<Map<String,String>> resultSetToCellData(ResultSet resultSet){
        if (null == resultSet){
            return null;
        }
        List<Map<String,String>> resultList = new LinkedList<>();
        try {
            while (resultSet.next()){
                Map<String,String> map = new HashMap<>();
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
    public void parseXmlConfig(){
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
        MySQL2ExcelThread mySQL2ExcelThread = new MySQL2ExcelThread();
        mySQL2ExcelThread.parseXmlConfig();
        mySQL2ExcelThread.threadConcurrency();
    }
}
