/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import classes.APP_CONSTANT;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author PK
 */
@WebServlet(name = "Tester", urlPatterns = {"/Tester"})
public class Tester extends HttpServlet {
    private final String LOG_NAME = "TSA.MOB.KZ";
    
    //private final String FULL_PATH =  System.getProperty("jboss.home.dir").concat("\\standalone\\deployments\\ROOT.war\\BAT\\");   
    //private final String FULL_PATH =  System.getProperty("jboss.home.dir").concat("\\standalone\\deployments\\ROOT.war\\").concat(getServletContext().getInitParameter("SystemDir")).concat("\\");   

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
       /*try
       {
            if (request.getParameter("log_status").equalsIgnoreCase("ON"))
            {
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, "LOG_ON");
                Logger.getLogger(LOG_NAME).setLevel(Level.ALL);
              //  Logger log = LogManager.getLogManager().getLogger("");
              //  for (Handler h : log.getHandlers()) {
              //      h.setLevel(Level.ALL);                    
              //  }                
            }
            if (request.getParameter("log_status").equalsIgnoreCase("OFF"))
            {
                //Logger.getLogger(LOG_NAME).log(Level.SEVERE, "LOG_OFF");
                //Logger.getLogger(LOG_NAME).setLevel(Level.INFO);  
                Logger log = LogManager.getLogManager().getLogger("");
                for (Handler h : log.getHandlers()) {
                 Logger.getLogger(LOG_NAME).log(Level.SEVERE, h.toString());
                    
                    //h.setLevel(Level.INFO);                    
                }                
               // LogManager.getLogManager().getLogger(LOG_NAME).setLevel(Level.INFO);
            }
            if (request.getParameter("log_status").equalsIgnoreCase("List"))
            {
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, "LOG_LIST");
                Logger.getLogger(LOG_NAME).setLevel(Level.INFO);
                
                Logger log = LogManager.getLogManager().getLogger("");
              //  for (Handler h : log.getHandlers()) {
             //       h.setLevel(Level.INFO);                    
             //   }
                
                String lg="";
                while((lg=LogManager.getLogManager().getLoggerNames().nextElement())!=null)
                {
                     Logger.getLogger(LOG_NAME).log(Level.SEVERE, "LOG_LIST: ".concat(lg));
                }
            }
            
        }
       catch(Exception e)
       {
           
       }
        */
       //Logger.getLogger(LOG_NAME).setLevel(Level.ALL);
       DateFormat df = new SimpleDateFormat("ddMMyyyyhhmmss");
       Date today = Calendar.getInstance().getTime();
       
       String use_code = "202";        
       String fileName = df.format(today).concat(".zip");  
       String IMEI = "359514064091488";
       
        //String uploadFile  = ;
        try (PrintWriter out = response.getWriter()) 
        {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Tester</title>");            
            out.println("</head>");
            out.println("<body> UPLOAD TEST:");
            out.println("<h1>TSA.MOB.KZ Tester at " + request.getContextPath() + "</h1>");
            out.println();
            out.println("</br>");            
            out.println("CHECK AUTH: " + checkAuth(request.getServerName(), String.valueOf(request.getServerPort()),IMEI));      
            out.println("</br>");  
            out.println("UPLOAD FILE RESULT: " + uploadSyncFile(request.getServerName(), String.valueOf(request.getServerPort()),use_code,fileName));      
            out.println("</br>");
            out.println("CHECK FILE ON SERVER: " + checkSyncFile(use_code,fileName));            
            out.println("</br>");
            out.println("WRITE FILE IN DB: " + writeFileToDb(request.getServerName(), String.valueOf(request.getServerPort()),fileName,use_code));            
            out.println("</br>");
            out.println("Get MOBILE DATA: " + getMobileData(request.getServerName(), String.valueOf(request.getServerPort()),fileName,use_code));  
            out.println("</br>");
            String syncStatus = getSyncStatus(request.getServerName(), String.valueOf(request.getServerPort()),fileName,use_code); 
            out.println("GET SYNC STATUS: " + syncStatus);
            out.println("</br>");
            out.println("LOAD MOBILE DATA FILE: " + loadMobileData(request.getServerName(), String.valueOf(request.getServerPort()),syncStatus));             
            out.println("</br>");
            out.println("UPLOAD PHOTO ON SERVER: " + uploadPhotoFile(request.getServerName(), String.valueOf(request.getServerPort()),use_code,fileName.substring(0, fileName.lastIndexOf('.')).concat(".jpg")));               
            out.println("</body>");
            out.println("</html>");
        }
        
    }
    
    //sync_photo_upload
    
    private String loadMobileData(String serverName, String serverPort, String syncStatus) throws IOException 
    {    
        syncStatus = syncStatus.substring(2);
        Logger.getLogger(LOG_NAME).log(Level.INFO, "TEST LOAD FILE: ".concat(syncStatus));
       // downloadFile(syncStatus, "");
        String result = "";
        
        InputStream is = null;
        FileOutputStream fos = null;
   
    try {
         URL website = new URL(syncStatus);
        URLConnection urlConn = website.openConnection();//connect

        is = urlConn.getInputStream();               //get connection inputstream
        fos = new FileOutputStream(APP_CONSTANT.FULL_PATH.concat("UNIT_TEST\\upload\\test.download.zip"));   //open outputstream to local file

        byte[] buffer = new byte[4096];              //declare 4KB buffer
        int len;

        //while we have availble data, continue downloading and storing to local file
        while ((len = is.read(buffer)) > 0) {  
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        
         ZipInputStream zis = new ZipInputStream(new FileInputStream(APP_CONSTANT.FULL_PATH.concat("UNIT_TEST\\upload\\test.download.zip")));
            ZipEntry ze = zis.getNextEntry();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer1 = new byte[1024];

            while (ze != null) {
                int len1;
                while ((len1 = zis.read(buffer1)) > 0) {
                    bos.write(buffer1, 0, len1);
                }

                bos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            result = Arrays.toString((new String(bos.toByteArray(), "UTF-8").split(";")));
        
    }   catch (MalformedURLException ex) 
    {
            result = Arrays.toString((new String [] {"DSS_ERROR".concat(ex.getMessage())}));
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) 
    {
           result = Arrays.toString((new String [] {"DSS_ERROR".concat(ex.getMessage())}));
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            if (is != null) {
                is.close();
            }
        }   catch (IOException ex) {
                Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    
        return result.substring(0, 150).concat("...");   
    }
    
     private  void downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
          //  System.out.println("Content-Type = " + contentType);
          //  System.out.println("Content-Disposition = " + disposition);
          //  System.out.println("Content-Length = " + contentLength);
          //  System.out.println("fileName = " + fileName);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = File.separator + "test.file.mobiledata.zip";
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
           // System.out.println("File downloaded");
        } else {
           // System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
    
    private String getSyncStatus(String serverName, String serverPort, String file_name,String use_code)
    {
         Map <String, String> _params  = new HashMap<>();
        _params.put("use_code", use_code);      
        return urlRequest(null, null, serverName, serverPort,"get_sync_status",null,"STATUS",_params);
       /*
        try
       {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,AuthScope.ANY_PORT), new UsernamePasswordCredentials("batuser", "Batpromo12#"));
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httppost = new HttpPost("http://"+serverName+":"+serverPort+"/TSA.MOB.KZ/Operations?operation=get_sync_status&use_code="+use_code);

            String boundary = "-------------"+UUID.randomUUID().toString();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setBoundary(boundary);
            httppost.setHeader("Content-Type", "multipart/mixed;boundary="+boundary);
            File file = new File(APP_CONSTANT.FULL_PATH.concat("UNIT_TEST//185_16022016160635.zip"));
            //builder.addPart("img_file",new FileBody(file));        
            BufferedHttpEntity multipart = new BufferedHttpEntity(builder.build());
            httppost.setEntity(multipart);        
            httpclient.execute(httppost);
            StringBuilder sb = new StringBuilder();
            String line = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpclient.execute(httppost).getEntity().getContent()));

            while ((line = in.readLine()) != null) 
            {
                sb.append(line);
                sb.append('\n');
            }
           String GETTING_IMEI  = "";
           try 
           {
                JSONObject o;             
                o = new JSONObject(sb.toString());
                 GETTING_IMEI = (String) ((JSONObject)((JSONArray)o.get("result")).get(0)).get("STATUS");
            } catch (JSONException ex) {
                Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
            }
           
            System.out.println("12312");
            if (GETTING_IMEI.length()>0)
            {
                return "true ".concat(GETTING_IMEI);
            }
            else
            {
                return sb.toString();                           
            }
       }
       catch(IOException e)
       {
           return e.getMessage();           
       }  
        */
    }
    
    private String getMobileData(String serverName, String serverPort, String file_name,String use_code)
    {
        Map <String, String> _params  = new HashMap<>();
        _params.put("use_code", use_code);
        _params.put("file_name", file_name);
        return urlRequest(null, null, serverName, serverPort,"get_mobile_data",null,"STATUS",_params);
       
        /*Logger.getLogger(LOG_NAME).setLevel(Level.ALL);
        try
       {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,AuthScope.ANY_PORT), new UsernamePasswordCredentials("batuser", "Batpromo12#"));
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httppost = new HttpPost("http://"+serverName+":"+serverPort+"/TSA.MOB.KZ/Operations?operation=get_mobile_data&use_code="+use_code);

            String boundary = "-------------"+UUID.randomUUID().toString();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setBoundary(boundary);
            httppost.setHeader("Content-Type", "multipart/mixed;boundary="+boundary);
            File file = new File(APP_CONSTANT.FULL_PATH.concat("UNIT_TEST//185_16022016160635.zip"));
            //builder.addPart("img_file",new FileBody(file));        
            BufferedHttpEntity multipart = new BufferedHttpEntity(builder.build());
            httppost.setEntity(multipart);        
            httpclient.execute(httppost);
            StringBuilder sb = new StringBuilder();
            String line = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpclient.execute(httppost).getEntity().getContent()));

            while ((line = in.readLine()) != null) 
            {
                sb.append(line);
                sb.append('\n');
            }
           String GETTING_IMEI  = "";
           try 
           {
                JSONObject o;             
                o = new JSONObject(sb.toString());
                 GETTING_IMEI = (String) ((JSONObject)((JSONArray)o.get("result")).get(0)).get("STATUS");
            } catch (JSONException ex) {
                Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
            }
           
            System.out.println("12312");
            if (GETTING_IMEI.length()>0)
            {
                return "true ".concat(GETTING_IMEI);
            }
            else
            {
                return sb.toString();                           
            }
       }
       catch(IOException e)
       {
           return e.getMessage();           
       }      
        */
    }
    
    private String urlRequest(String bauth_name, String bauth_password, String serverName, String serverPort, String operation, String filePath, String AnswerField, Map <String, String> _params )
    {       
        String params = "";
        //APP_CONSTANT.FULL_PATH.concat("UNIT_TEST//185_16022016160635.zip")
        for(Map.Entry<String, String> entry : _params.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            params = params.concat("&").concat(key).concat("=").concat(value);
            Logger.getLogger("TESTER").log(Level.INFO, params);
        }
               
       try
       {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,AuthScope.ANY_PORT), new UsernamePasswordCredentials("batuser", "Batpromo12#"));
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httppost = new HttpPost("http://"+serverName+":"+serverPort+"/TSA.MOB.KZ/Operations?operation=".concat(operation).concat(params));

            String boundary = "-------------"+UUID.randomUUID().toString();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setBoundary(boundary);
            httppost.setHeader("Content-Type", "multipart/mixed;boundary="+boundary);
            
            if (filePath != null)
            {
                File file = new File(filePath);
                builder.addPart("file",new FileBody(file));                        
                BufferedHttpEntity multipart = new BufferedHttpEntity(builder.build());
                httppost.setEntity(multipart);
            }
            
            httpclient.execute(httppost);
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpclient.execute(httppost).getEntity().getContent()));

            while ((line = in.readLine()) != null) 
            {
                sb.append(line);
                sb.append('\n');
            }
            
            String status = "";
            try 
            {
                JSONObject o;             
                o = new JSONObject(sb.toString());
                 status = (String) ((JSONObject)((JSONArray)o.get("result")).get(0)).get(AnswerField);
            } catch (JSONException ex) {
                Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (status.length()>0)
            {
                return "".concat(status);
            }
            else
            {
                return sb.toString();
            }
       }
       catch(IOException e)
       {
           return e.getMessage();
       }       
    }
    
    private String writeFileToDb(String serverName, String serverPort, String file_name,String use_code)
    {    
         Map <String, String> _params  = new HashMap<>();
        _params.put("use_code", use_code);
        _params.put("file_name", file_name);
        return urlRequest(null, null, serverName, serverPort,"write_file_data",null,"STATUS",_params);
    }
    
    private String checkSyncFile(String use_code ,String fileName) 
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Date today = Calendar.getInstance().getTime();
        String folderDate = df.format(today); 
        File f = new File(APP_CONSTANT.FULL_PATH.concat("upload\\").concat(folderDate).concat("\\").concat(use_code).concat("\\").concat(fileName));        
        return String.valueOf(f.exists());
    }
    
    private String checkAuth(String serverName, String serverPort,String IMEI) 
    {
       Map <String, String> _params  = new HashMap<>();
        _params.put("IMEI", IMEI);
        return urlRequest(null, null, serverName, serverPort,"get_auth",null,"IME_SERIAL_NUMBER",_params);       
    }
    
    private String uploadPhotoFile(String serverName, String serverPort,String use_code,String fileName) 
    {
       try
       {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,AuthScope.ANY_PORT), new UsernamePasswordCredentials("batuser", "Batpromo12#"));
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httppost = new HttpPost("http://"+serverName+":"+serverPort+"/TSA.MOB.KZ/Operations?operation=sync_photo_upload&use_code="+use_code+"&file_name="+fileName);

            String boundary = "-------------"+UUID.randomUUID().toString();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setBoundary(boundary);
            httppost.setHeader("Content-Type", "multipart/mixed;boundary="+boundary);
            File file = new File(APP_CONSTANT.FULL_PATH.concat("UNIT_TEST//photo_test.jpg"));
            builder.addPart("img_file",new FileBody(file));        
            BufferedHttpEntity multipart = new BufferedHttpEntity(builder.build());
            httppost.setEntity(multipart);        
            httpclient.execute(httppost);
            StringBuilder sb = new StringBuilder();
            String line = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpclient.execute(httppost).getEntity().getContent()));

            while ((line = in.readLine()) != null) 
            {
                sb.append(line);
                sb.append('\n');
            }
            if (sb.toString().contains("\"STATUS\":\"OK\""))
            {
                return "true";
            }
            else
            {
                return sb.toString();
            }
       }
       catch(IOException e)
       {
           return e.getMessage();
       }       
    }
    
    private String uploadSyncFile(String serverName, String serverPort,String use_code,String fileName) 
    {
       try
       {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,AuthScope.ANY_PORT), new UsernamePasswordCredentials("batuser", "Batpromo12#"));
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httppost = new HttpPost("http://"+serverName+":"+serverPort+"/TSA.MOB.KZ/Operations?operation=sync_file_upload&use_code="+use_code+"&file_name="+fileName);

            String boundary = "-------------"+UUID.randomUUID().toString();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setBoundary(boundary);
            httppost.setHeader("Content-Type", "multipart/mixed;boundary="+boundary);
            File file = new File(APP_CONSTANT.FULL_PATH.concat("UNIT_TEST//185_16022016160635.zip"));
            builder.addPart("img_file",new FileBody(file));        
            BufferedHttpEntity multipart = new BufferedHttpEntity(builder.build());
            httppost.setEntity(multipart);        
            httpclient.execute(httppost);
            StringBuilder sb = new StringBuilder();
            String line = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpclient.execute(httppost).getEntity().getContent()));

            while ((line = in.readLine()) != null) 
            {
                sb.append(line);
                sb.append('\n');
            }
            if (sb.toString().contains("\"STATUS\":\"OK\""))
            {
                return "true";
            }
            else
            {
                return sb.toString();
            }
       }
       catch(IOException e)
       {
           return e.getMessage();
       }       
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
