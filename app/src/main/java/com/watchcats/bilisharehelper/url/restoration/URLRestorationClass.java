package com.watchcats.bilisharehelper.url.restoration;

import android.util.Log;

//import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLRestorationClass {

    public String getOriginalUrl(String shortUrl){
        try {
            URL originalUrl = new URL(shortUrl);
            HttpURLConnection connection = (HttpURLConnection) originalUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36");

            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuffer stringBuffer = new StringBuffer();
                String temp = null;
                while ((temp = bufferReader.readLine()) != null){
                    stringBuffer.append(temp);
                    stringBuffer.append("\r\n");
                }
                String result = stringBuffer.toString();

//                System.out.println(result);
                return getBiliUrlFromString(result);
            }else return "Connect Error";
        } catch (MalformedURLException e) {
            Log.e("error", "MalformedURLException from URLRestorationClass", e);
            return "Error";
        }catch (IOException e){
            Log.e("error", "IOException from URLRestorationClass", e);
            return "Error";
        }

    }

//    private String getResponseHtml(String url){
//        try {
//            URL originalUrl = new URL(shortUrl);
//            HttpURLConnection connection = (HttpURLConnection) originalUrl.openConnection();
//            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
//            //connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36");
//
//            connection.setRequestMethod("GET");
//            connection.connect();
//
//            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
//                InputStream inputStream = connection.getInputStream();
//                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//                StringBuffer stringBuffer = new StringBuffer();
//                String temp = null;
//                while ((temp = bufferReader.readLine()) != null){
//                    stringBuffer.append(temp);
//                    stringBuffer.append("\r\n");
//                }
//                String result = stringBuffer.toString();
//
//                return result;
//            }else return "Connect Error";
//        } catch (MalformedURLException e) {
//            Log.e("error", "MalformedURLException from URLRestorationClass", e);
//            return "Error";
//        }catch (IOException e){
//            Log.e("error", "IOException from URLRestorationClass", e);
//            return "Error";
//        }
//    }

    private String getBiliUrlFromString(String string){
        String patternStr = "https://www.bilibili.com/[^\\s]+(?=/)";
//        String patternStr2 = "https://www.youtube.com/[^\\s]+/";

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(string);

        if (matcher.find()) return matcher.group(0);
        else return "No matching URL";
    }

//    public String getAVNumber(String string){
//
//    }

}

