package com.mrlu.bigData.excel2mysql;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by stefan on 16-3-28.
 * 多线程模式
 */
public class CSV2MySQLThread{
    //数据库相关
    private static final String jdbcUrl=Constants.jdbcUrl;
    private static final String jdbcUserName=Constants.jdbcUserName;
    private static final String jdbcPassword=Constants.jdbcPassword;
    private static List<Connection> connectionList = new ArrayList<>();
    private static final String sql = Constants.prepareSql;
    private static final Integer colNum = 11; //要插入数据库的数据占的列数
    private static final Integer dataRecordNum = 1000; //每次要插入数据库的条数

    //文件相关
    private static Integer lineNum = 3; //线程从哪一行开始读取数据
    private static Integer lineCount = 100000; //每次读取文件多少行数据
    private static String path = Constants.cvsPath;

    //线程相关
    private static Integer threadCount = 0;
    private static Long startTime = System.currentTimeMillis();

    static {
        try {
            System.out.println(new Date());
            Class.forName("com.mysql.jdbc.Driver");

            //读取文件行数，得到需要多少个线程，每个线程一个数据库链接
            /*方法一：一行一行读取得到文件的行数，使用时间5分钟
            LineNumberReader reader = new LineNumberReader(new FileReader(path));
            while (reader.readLine() != null);
            int lineNumbers = reader.getLineNumber();
            reader.close();
            */

            //方法二：执行linux命令，获取文件行数，使用时间不到一分钟
            String[] cmdValue = {"sh", "-c"," cat "+path+ " | wc -l "} ;
            Process process = Runtime.getRuntime().exec(cmdValue);
            int lineNumbers = 0;
            process.waitFor();
            if(process.exitValue() == 0){ //正常退出
                InputStream is = process.getInputStream();
                LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
                String line = null;
                if((line = br.readLine()) != null) {
                    System.out.println("line num: " + line);
                    lineNumbers = Integer.parseInt(line);
                }
                br.close();
                is.close();
            }

            System.out.println("file line num:" + lineNumbers);

            threadCount = lineNumbers / lineCount;
            if(lineNumbers % lineCount != 0) threadCount += 1;

            for(int i=0; i<threadCount; i++){ //初始化threadCount个数据库链接，供子线程使用
                connectionList.add(DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //同步方法块，拿到链接
    private synchronized static Connection getConnection(){
        if(connectionList != null && connectionList.size() >1){
            return connectionList.remove(0);
        }
        return null;
    }

    //同步方法块，释放链接
    private synchronized static void addConnection(Connection connection){
        connectionList.add(connection);
        if(connectionList.size() == threadCount){
            try {
                for (Connection connection1: connectionList){
                        connection1.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println(new Date());
    }

    //执行读取csv文件内容并存到mysql数据库的子线程
    private class CSVSubThread implements Runnable{
        @Override
        public void run() {
            csv2MySQl(getNextLineNum());
        }
    }

    //同步方法块，每个子线程要处理的数据的开始行数
    //thread1: 3
    //thread2: 3+lineCount = 10003
    public synchronized static int getNextLineNum(){
        int num = lineNum;
        lineNum += lineCount;
        return num;
    }

    /**
     * 从指定行数开始，读取指定行数(lineCount)，并将读到的数据写入mysql中
     * @param startLineNum
     */
    public void csv2MySQl(int startLineNum){
        File file = new File(path);
        if(!file.exists()){
            System.out.println("file not found!!!");
            return;
        }
        try {
            int thisLinenum = 0;
            int endLinenum = startLineNum + lineCount;
            Connection connection = getConnection();
            if(connection == null) return;
//            System.out.println(Thread.currentThread().getName() + "--> connection: " + connection);

            Scanner scanner = new Scanner(file);
            List<String[]> stringList = new ArrayList<>();
            System.out.println(Thread.currentThread().getName() + "--->startLineNum:" + startLineNum + "  endLinenum:"+endLinenum);
            while (scanner.hasNextLine() && thisLinenum < endLinenum){
                if(thisLinenum++ < startLineNum) {
                    scanner.nextLine();
                    continue;
                }
                stringList.add(splitRawValue(scanner.nextLine()));
                if(stringList.size() == dataRecordNum){ //每次处理1000行数据，不要一次性全部读到内存中，内存会撑爆的
                    batchInsert(stringList,connection); //批量存储
                    stringList.clear();
                }
            }
            //避免最后一个线程读取的数量比指定的行数小，没有将数据存到mysql中
            if(stringList.size() != 0){
                batchInsert(stringList,connection);
            }
            stringList.clear();
            scanner.close();
            Long endTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + "-->: already use time:" + (endTime-startTime));
            addConnection(connection);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //处理每个单元格数据，即要插入数据库的数据
    private String trimString(String originStr) {
        if(StringUtils.isBlank(originStr)) return "";
        originStr = StringUtils.replace(originStr, " ", "");
        originStr = StringUtils.replace(originStr, "\n\r", "");
        originStr = StringUtils.replace(originStr, "\n", "");
        originStr = StringUtils.trim(originStr);
        return StringUtils.replace(originStr, "\t", "");
    }

    //处理没一行的数据，得到行的每个单元格数据
    private String[] splitRawValue(String rowOriginValue){
        try {
            String[] rowNewValues = rowOriginValue.split(",");
            for (int i=0; i<rowNewValues.length; i++){
                //每个单元格的数据在从excel转到csv时，会前后添加引号，此处处理掉引号
                if(StringUtils.isBlank(rowNewValues[i]) || rowNewValues[i].length() < 3) continue;
                rowNewValues[i] = rowNewValues[i].substring(1, rowNewValues[i].length()-1);
            }
            return rowNewValues;
        }catch (Exception e){
            System.out.println(rowOriginValue);
            e.printStackTrace();
            return null;
        }
    }

    private void batchInsert(List<String[]> rowList,Connection connection) {
        System.out.println(Thread.currentThread().getName() + "-->startTime:" + System.currentTimeMillis()+" listNum:" + rowList.size());
        PreparedStatement preparedStatement = null;
        try {
            //此处可以优化，一个线程可以只做一个预编译，不过每次做批处理之前需要先清空先前的数据
            //preparedStatement.clearParameters();
            //preparedStatement.clearBatch();

            preparedStatement = connection.prepareStatement(sql);
            //批量处理
            for (String[] eachRow : rowList) {
                if(eachRow == null || eachRow.length <= 0) continue;
                for(int i=0; i<eachRow.length; i++){
                    preparedStatement.setString(i+1, trimString(eachRow[i]));
                }
                if(eachRow.length < colNum){ //避免因Excel有些行的数据最后几列没有数据，到时sql异常
                    for(int i=eachRow.length; i<=colNum; i++){
                        preparedStatement.setString(i, null);
                    }
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();//批量处理
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != preparedStatement) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + "-->end:" + System.currentTimeMillis());
    }

    //junit中不能以正常方式使用多线程，因为junit会调用System.exit(1)手动去停止jvm
    //junit中可以使用Future来使用多线程
    //普通线程
    public static void main(String[] args){
        CSVSubThread csvSubThread = new CSV2MySQLThread().new CSVSubThread();
        for (int i=0; i<threadCount;i++){
            Thread thread = new Thread(csvSubThread);
            thread.start();
        }
    }

    //线程池
    @Test
    public void CSV2MySQLTest(){
        long addStartTime = System.currentTimeMillis();
        CSVSubThread csvSubThread = new CSV2MySQLThread().new CSVSubThread();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future> list = new ArrayList<>();
        for (int i=0; i<threadCount;i++){
            list.add(executorService.submit(csvSubThread));
        }
        for (Future future: list){
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdownNow();
        System.out.println("All thread end -->" + (System.currentTimeMillis() - addStartTime));
    }
}
