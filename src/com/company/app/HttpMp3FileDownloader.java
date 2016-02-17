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

/**
 * @author adriano-fonseca
 *
 */
public class HttpMp3FileDownloader {
  /*Proxy details*/
  private static final String USER_AGENT = "Mozilla/5.0";
  private static final String PROXYURL = "your.proxy.com";
  private static final int PORTNUMBER = 3128;
  private static final String AUTHUSER = "user";
  private static final String AUTHPASSWORD = "senha";
  
  /*Log details*/
  private static final String LOGFOLDERPATH = "./log";  
  private static final String ERRORLOGPATH = "./log/error.log";
  private static final Logger LOGGER = Logger.getLogger("DownloadErrorLog");  
  
  /*File details*/
  private static final String FILEFOLDERPATH = "./files";
  private static final String FILEPREFIXNAME = "ESLPod";    
  private static final Integer NUMFINALEPISODE = 1157;
  private static final String EXTENSION = ".mp3";
  private static final String URLPARTIAL = "http://libsyn.com/media/eslpod/";

  public static void main(String[] args){
    createFolders();
    getFile(false,PROXYURL,PORTNUMBER,AUTHUSER,AUTHPASSWORD);
    
//      getFileTestWithProxyAuth();
  }

  /**
   * @author adriano-fonseca
   * 
   *  This method check if log and files folder exists otherwise it will create them
   */
  private static void createFolders() {
    File fileFolder = new File(FILEFOLDERPATH);
    File logFolder = new File(LOGFOLDERPATH);
    
    if (!fileFolder.exists()) {
        fileFolder.mkdir();
     } 
    
    if (!logFolder.exists()) {
      logFolder.mkdir();
    }
  }
  
  

  
  /**
   * @author adriano-fonseca 
   * @param proxyAuth
   * @param proxyUrl
   * @param portNumber
   * @param authUser
   * @param authPassword
   * 
   * This method is responsible to dowload all files within the range defined for 
   * numInitialEpisode and NUMFINALEPISODE
   */
  private static void getFile(boolean proxyAuth,String proxyUrl,int portNumber,final String authUser,final String authPassword) {
    Integer numInitialEpisode = 206;
    FileHandler fh;  

    byte[] bytes=null;
    StringBuffer urlFromFile = new StringBuffer();
    HttpURLConnection con=null;

    while(numInitialEpisode<NUMFINALEPISODE){
      StringBuffer fileName = new StringBuffer();
      fileName.append(FILEPREFIXNAME);
      fileName.append(numInitialEpisode.toString());
      fileName.append(EXTENSION);
      System.out.println(fileName);
      
      try {
        //Prepare Logger
        fh = new FileHandler(ERRORLOGPATH);  
        LOGGER.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);
        
        //Generate File Url
        urlFromFile.append(URLPARTIAL);
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
  

  /**
   * @author adriano-fonseca 
   * @param fileName
   * @param urlFromFile
   * @param e
   * 
   * responsible to genete logs for every file that was not able to download
   */
  private static void logError(StringBuffer fileName, StringBuffer urlFromFile, Exception e) {
    StringBuffer log = new StringBuffer();
    log.append("Impossible get file [");
    log.append(fileName);
    log.append("] from url [");
    log.append(urlFromFile);
    log.append("] \n");
    log.append("Cause: ");
    log.append(e.getMessage());
    LOGGER.severe(log.toString());
  }

  /**
   * @author adriano-fonseca 
   * @param inStream
   * @return
   * 
   * Convert inputStrem to byteArray in blocks of 8192 bytes this is base on mp3 
   * format specification
   */
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
  
  /**
   * @author adriano-fonseca 
   * @param byteArray
   * @param outFilePath
   * 
   * Convert byte array to file
   */
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
  
  /**
   * @author adriano-fonseca 
   * @param urlFromFile
   * @param proxyAuthentication
   * @param proxyUrl
   * @param portNumber
   * @param authUser
   * @param authPassword
   * @return
   * @throws IOException
   * 
   * This method is responsible to return a connection for a url.
   * Its possible have a connection through proxy just passing true in proxy authentication
   * and informing the follow parameter properly
   */
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
  
  /**
   * @author adriano-fonseca
   * 
   * Just used to test inside of network that have proxy rules the not allow you download mp3 files
   * So in this case, I found a link inside of intrate in order to test the code 
   */
  private static void getFileTestWithProxyAuth() {
    /*Proxy details*/
    String urlMP3 = "http://hvi.procergs.com.br/intranet/audios/Reporter.PROCERGS.20150612.mp3";
    HttpURLConnection con;
    try {
      con = getConnectionHttpUrlConnection(urlMP3,true,PROXYURL,PORTNUMBER,AUTHUSER,AUTHPASSWORD);
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