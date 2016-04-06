import com.alibaba.fastjson.JSON;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * 如果二维码中是一个链接地址，若用微信扫描，微信就会跳转到此链接地址，用微信内嵌的浏览器打开此链接地址
 */
public class QrCodeUtil {

    /**
     * 生成二维码
     * @param content
     * @param outputStream
     */
    public void generateCode(String content, OutputStream outputStream){
        int width = 200;
        int height = 200;

        HashMap<EncodeHintType, Object> hints = new HashMap<>();
        //存入二维码中内容的编码方式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

        String format = "png";
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateObjectQrCode(Object object, OutputStream outputStream){
        generateCode(objectToJson(object), outputStream);
    }

    public String objectToJson(Object object){
        if(object == null){
            return "";
        }
        return JSON.toJSONString(object).trim();
    }

    /**
     * 解析二维码
     * @param inputStream
     * @return
     */
    public String resolveQrCode(InputStream inputStream){
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        try {
            //javax.imageio.ImageReader可以读取一个图片并转成BufferedImage对象
            BufferedImage image = ImageIO.read(inputStream);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
            //用指定的编码解析二维码中包含的内容
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            Result result = multiFormatReader.decode(binaryBitmap, hints);
            return result.getText();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        QrCodeUtil codeUtil = new QrCodeUtil();
        /*try {
            //FileOutputStream写文件的时候，如果文件不存在，会首先去创建文件
            //FileNotFoundException: 文件存在但是一个目录，或者文件不存在却不能创建
            FileOutputStream fileOutputStream = new FileOutputStream("/home/stefan/code.png");
            Map<String, Object> map = new HashMap<>();
            map.put("name", "张三");
            map.put("sex", "男");
            map.put("age", "19");
            codeUtil.generateCode(JSON.toJSONString(map), fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            FileInputStream fileInputStream = new FileInputStream("/home/stefan/code.png");
            System.out.println(codeUtil.resolveQrCode(fileInputStream));
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
