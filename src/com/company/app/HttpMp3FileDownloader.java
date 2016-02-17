package com.company.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class HttpMp3FileDownloader {

  private static final String USER_AGENT = "Mozilla/5.0";
  private static final String proxyUrl = "your.proxy.com";
  private static final int portNumber = 3128;
  private static final String authUser = "user";
  private static final String authPassword = "senha";
  
  private static final String urlpartial = "http://libsyn.com/media/eslpod/";
  
  private static final String logFolderPath = "./log";  
  private static final String errorLogPath = "./log/error.log";
  
  private static final String fileFolderPath = "./files";
  private static final String prefixNameFile = "ESLPod";    
  private static final Integer numFinalEpisode = 1157;
  private static final String extencionFile = ".mp3";
  private static final Logger logger = Logger.getLogger("DownloadErrorLog");  

  public static void main(String[] args){
    createFolders();
    getFile(false,proxyUrl,portNumber,authUser,authPassword);
    
//      getFileTestWithProxyAuth();
  }

  private static void createFolders() {
    File fileFolder = new File(fileFolderPath);
    File logFolder = new File(logFolderPath);
    
    if (!fileFolder.exists()) {
        fileFolder.mkdir();
     } 
    
    if (!logFolder.exists()) {
      logFolder.mkdir();
    }
  }

  // HTTP GET request
  private static void getFile(boolean proxyAuth,String proxyUrl,int portNumber,final String authUser,final String authPassword) {
    Integer numInitialEpisode = 206;
    FileHandler fh;  

    byte[] bytes=null;
    StringBuffer urlFromFile = new StringBuffer();
    HttpURLConnection con=null;

    while(numInitialEpisode<numFinalEpisode){
      StringBuffer fileName = new StringBuffer();
      fileName.append(prefixNameFile);
      fileName.append(numInitialEpisode.toString());
      fileName.append(extencionFile);
      System.out.println(fileName);
      
      try {
        //Prepare Logger
        fh = new FileHandler(errorLogPath);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);
        
        //Generate File Url
        urlFromFile.append(urlpartial);
        urlFromFile.append(fileName);
        
        if(proxyAuth){
           con = getConnectionHttpUrlConnection(urlFromFile.toString(),true,proxyUrl,portNumber,authUser,authPassword);
        }else{
           con = getConnectionHttpUrlConnection(urlFromFile.toString());
        }
        bytes = inputStreamToByteArray(con.getInputStream());
        byteArrayToFile(bytes,"./files/"+fileName);
      }catch (MalformedURLException e) {
        logError(fileName, urlFromFile, e);
        numInitialEpisode++;
        continue;
      } catch (FileNotFoundException e) {
        // the following statement is used to log any messages  
        logError(fileName, urlFromFile,e);
        numInitialEpisode++;
        continue;
      } catch (IOException e) {
        // the following statement is used to log any messages  
        logError(fileName, urlFromFile,e);
        numInitialEpisode++;
        continue;
      }
      numInitialEpisode++;
      System.out.println(fileName);
    }
  }
  
  private static void logError(StringBuffer fileName, StringBuffer urlFromFile, Exception e) {
    StringBuffer log = new StringBuffer();
    log.append("Impossible get file [");
    log.append(fileName);
    log.append("] from url [");
    log.append(urlFromFile);
    log.append("] \n");
    log.append("Cause: ");
    log.append(e.getMessage());
    logger.severe(log.toString());
  }

  public static byte[] inputStreamToByteArray(InputStream inStream) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int bytesRead;
    try {
      while ((bytesRead = inStream.read(buffer)) > 0) {
          baos.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return baos.toByteArray();
  }
  
  public static void byteArrayToFile(byte[] byteArray, String outFilePath){
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(outFilePath);
      fos.write(byteArray);
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
   
  }
  
  private static HttpURLConnection getConnectionHttpUrlConnection(String urlFromFile, Boolean proxyAuthentication,String proxyUrl,int portNumber,final String authUser,final String authPassword) throws IOException{
    HttpURLConnection con = null;
    URL server = new URL(urlFromFile);
    /*Without Proxy Authentication*/
    if(!proxyAuthentication){
      server = new URL(urlFromFile);
      con = (HttpURLConnection) server.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", USER_AGENT);
      return con;
    }else{
      Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl, portNumber));
      
      Authenticator.setDefault(
         new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(authUser, authPassword.toCharArray());
            }
         }
      );
      con = (HttpURLConnection) server.openConnection(proxy);
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", USER_AGENT);
      return con;
    }
  }
  
  private static void getFileTestWithProxyAuth() {
    /*Proxy details*/
    String urlMP3 = "http://hvi.procergs.com.br/intranet/audios/Reporter.PROCERGS.20150612.mp3";
    HttpURLConnection con;
    try {
      con = getConnectionHttpUrlConnection(urlMP3,true,proxyUrl,portNumber,authUser,authPassword);
      int responseCode = con.getResponseCode();
      System.out.println("\nSending 'GET' request to URL : " + urlMP3);
      System.out.println("Response Code : " + responseCode);
      byte[] bytes = inputStreamToByteArray(con.getInputStream());
      byteArrayToFile(bytes,"./files/export.mp3");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static HttpURLConnection getConnectionHttpUrlConnection(String urlFromFile) throws IOException{
    return getConnectionHttpUrlConnection(urlFromFile, false,"", 0,"","");
  }

  
}