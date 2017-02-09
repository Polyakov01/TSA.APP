/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author PK
 */
public class APP_CONSTANT 
{
   // public static final String GET_AUTH = "get_auth";
  //  public static final String GET_SYNC_STATUS = "get_sync_status";
  //  public static final String GET_OPERATION = "operation";
  //  public static final String POST_SYNC_FILE_UPLOAD = "sync_file_upload";     
    public static final String LOG_NAME = "TSA.APP.TEST";    
    public static final String JNDI_NAME = "JNDI_BAT"; //_BY    
    public static final String SYSTEM_FOLDER_NAME = "test"; //"BAT";      
    public static final String SYNC_FOLDER_NAME = "sync\\";
    public static final String SYNC_UPLOAD_FOLDER_NAME = "upload\\";
    public static final String REPORTS_COMPLETE_FOLDER = "reports\\userdata\\";
    public static final String REPORTS_TEMPLATE_FOLDER = "reports\\template\\";
    public static final String FULL_PATH =  System.getProperty("jboss.home.dir").concat("\\standalone\\deployments\\ROOT.war\\").concat(SYSTEM_FOLDER_NAME).concat("\\"); 
    
    public static String FOLDER_DATE()
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        java.util.Date today = Calendar.getInstance().getTime();
        String folderDate = df.format(today).concat("\\");
        return folderDate;
    }
    
    public static String FILE_DATE(String fileExt)
    {
        DateFormat df = new SimpleDateFormat("ddMMyyyy_hhmmss");
        java.util.Date today = Calendar.getInstance().getTime();
        String fileDate = df.format(today).concat(".").concat(fileExt);
        return fileDate;
    }
    
}
