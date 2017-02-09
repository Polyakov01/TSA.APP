/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DB.DBOperations;
import classes.APP_CONSTANT;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import util.FileUtil;
import static util.FileUtil.getPathToSyncFile;
import static util.FileUtil.getPathToUploadFile;
import util.JDBCUtil;

/**
 *
 * @author PK
 */
public class Operations1 extends HttpServlet 
{
    private final String GET_AUTH = "get_auth";
    private final String GET_SYNC_STATUS = "get_sync_status";
    private final String GET_OPERATION = "operation";
    private final String POST_SYNC_FILE_UPLOAD = "sync_file_upload";
    private final String POST_SYNC_PHOTO_UPLOAD = "sync_photo_upload";
    private final String GET_MOBILE_DATA = "get_mobile_data";  
    private final String GET_WRITE_FILE_DATE = "write_file_data";  
    private final String LOG_NAME = "TSA.MOB.KZ";
    private final String CHANGE_LOG_LEVEL = "change_log_level";
             
  
    /**
     * Processes requests for HTTP <code>GET</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws java.io.UnsupportedEncodingException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, UnsupportedEncodingException
    {                	        
        
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");       
        try 
        {                     
            if (request.getParameterMap().containsKey(GET_OPERATION))
            {
                switch(request.getParameter(GET_OPERATION))
                {
                    case GET_AUTH:
                    {                
                        response.getWriter().println(setJSONAnswer(GET_AUTH, getAuth(request.getParameterMap())));                    
                        break;                        
                    }
                    case GET_SYNC_STATUS:
                    {                
                        response.getWriter().println(setJSONAnswer(GET_SYNC_STATUS, getSyncStatus(request.getParameterMap())));                    
                        break;                        
                    }
                    case POST_SYNC_FILE_UPLOAD:
                    {
                        String uploadResult = fileUpload(request, request.getParameterMap(),false);
                        response.getWriter().println(setJSONAnswer(POST_SYNC_FILE_UPLOAD,uploadResult));                    
                        break;
                    }
                    case POST_SYNC_PHOTO_UPLOAD:
                    {
                        String uploadResult = fileUpload(request, request.getParameterMap(),true);
                        response.getWriter().println(setJSONAnswer(POST_SYNC_PHOTO_UPLOAD,uploadResult));                    
                        break;
                    }
                    case GET_MOBILE_DATA:
                    {
                        response.getWriter().println(setJSONAnswer(GET_MOBILE_DATA,getMobileDataFileLink(request.getServerName(),request.getServerPort(),request.getParameterMap())));                 
                        break;
                    }
                    case GET_WRITE_FILE_DATE:
                    {
                        response.getWriter().println(setJSONAnswer(GET_WRITE_FILE_DATE,doWriteFileData(request.getParameterMap())));                        
                        break;
                    }
                    
                    case CHANGE_LOG_LEVEL:
                    {
                        changeLogLevel(request.getParameterMap());                        
                        break;
                    }
                    default: 
                    {
                        response.getWriter().println(setJSONAnswer(request.getParameter(GET_OPERATION),"DSS_ERROR OPERATION NOT FOUND"));                    
                        break;                        
                    }
                }
            }
            else
            {
                response.getWriter().println(setJSONAnswer("NOT OPERATION","DSS_ERROR UNSUPPORTED OPERATION"));                        
            }
        } 
        catch (IOException ex) 
        {            
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
    }
    
     private void changeLogLevel( Map <String, String[]> _params)
    {       
        String level = getRequestParam("log_level", _params);
        switch(level)
        {
            case "INFO":
            {
                Logger.getLogger(LOG_NAME).setLevel(Level.INFO);
                break;
            }
            case "SEVERE":
            {
                Logger.getLogger(LOG_NAME).setLevel(Level.SEVERE);
                break;
            }
            case "ALL":
            {
                Logger.getLogger(LOG_NAME).setLevel(Level.ALL);
                break;
            }
                    
        } 
    }
    
    private String doWriteFileData( Map <String, String[]> _params)
    {       
        String fileName = getRequestParam("file_name", _params);
        String use_code = getRequestParam("use_code", _params);
        String result = "OK";
        String[] queries = FileUtil.getUnzippedQuery(getPathToUploadFile(use_code).concat(fileName));           
        if (queries[0].contains("DSS_ERROR"))
        {
            return queries[0];
        }
        else
        {
            DBOperations dbo = new JDBCUtil().getDBOperations();
            dbo.doUpdate("insert INTO at_sync_status  (SYS_USE_CODE,SYS_STATUS)SELECT " + String.valueOf(use_code) + " as SYS_USE_CODE, '03' as SYS_STATUS WHERE  NOT EXISTS (select * from at_sync_status where SYS_USE_CODE = " + String.valueOf(use_code) + ")");
            SetSyncProgress("01", Integer.parseInt(use_code));
            dbo.doBatchUpdate(queries);
            SetSyncProgress("03", Integer.parseInt(use_code));
            return result;
        }
    }
    
    private String getMobileDataFileLink(String serverAddress,int serverPort, Map <String, String[]> _params)
    {               
        int use_code = Integer.parseInt(getRequestParam("use_code", _params));
        String url;
        Connection conn = null;
        StringBuilder sb = new StringBuilder();
        String lostViews = "Not FOUND VIEWS: ";
        try 
        {    
            DBOperations dbo = new JDBCUtil().getDBOperations();            
            conn = dbo.getConnection();
            SetSyncProgress("04", use_code);
            CallableStatement call = conn.prepareCall("SELECT * FROM VIEW_AT_MOBILE_VIEW_LIST WHERE USE_CODE = '".concat(String.valueOf(use_code)).concat("' order by MVL_ORDER"));           
            Logger.getLogger(LOG_NAME).log(Level.FINE, "SELECT * FROM VIEW_AT_MOBILE_VIEW_LIST WHERE USE_CODE = '".concat(String.valueOf(use_code)).concat("' order by MVL_ORDER"));                                    
            ResultSet rs = call.executeQuery();                  
            while (rs.next()) 
            {
                Logger.getLogger(LOG_NAME).log(Level.FINEST, "In loop by views");                        
                String sql = "Select * from " + rs.getString("MVL_VIEW_NAME");
                if (rs.getInt("MVL_REFERENCE") == 1) 
                {
                    sql += " where use_code is null or use_code = " + String.valueOf(use_code);
                }
                Logger.getLogger(LOG_NAME).log(Level.FINEST, "CURRENT VIEW QUERY:".concat(sql)); 
                SetSyncProgress("05" + rs.getString("MVL_TABLE_NAME"), use_code);
                String appendData = appendSyncData(rs.getString("MVL_TABLE_NAME"), sql);
                if (!appendData.contains("DSS_ERROR"))
                {
                    sb.append(appendData);
                    Logger.getLogger(LOG_NAME).log(Level.FINEST,appendData); 
                }
                else
                {
                    lostViews = lostViews.concat("\n").concat(appendData);
                }
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE,null,ex);           
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE,null,ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(LOG_NAME).log(Level.SEVERE,ex.getMessage());
                }
            }
        }
        Logger.getLogger(LOG_NAME).log(Level.WARNING, lostViews);
        Logger.getLogger(LOG_NAME).log(Level.FINE, "MOBILE_BEGIN_FILE_WRITE");
        String folderDate = APP_CONSTANT.FOLDER_DATE();
        String fileName = APP_CONSTANT.FILE_DATE("zip");
        String filePath = getPathToSyncFile(use_code,folderDate);   
        FileUtil.mkQueryZipFile(filePath.concat(fileName), use_code, sb);
        Logger.getLogger(LOG_NAME).log(Level.FINE, "MOBILE_END_FILE_WRITE");
        url = "http://".concat(serverAddress).concat(":").concat(String.valueOf(serverPort)).concat("\\").concat(APP_CONSTANT.SYSTEM_FOLDER_NAME).concat("\\").concat(APP_CONSTANT.SYNC_FOLDER_NAME).concat(folderDate).concat(String.valueOf(use_code)).concat("\\").concat(fileName);      
        url = url.replaceAll("\\\\", "/");
        SetSyncProgress("06".concat(url), use_code);
        return url;
    }
    
    private String appendSyncData(String currentTable, String query)
    {
        Logger.getLogger(LOG_NAME).log(Level.FINEST, "CURRENT VIEW QUERY TO APPEND:".concat(query));
        DBOperations dbo = new JDBCUtil().getDBOperations();
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(currentTable).append(";");
        String result = dbo.doQuerySQL(query, currentTable);
        sb.append(result);
        return sb.toString();
    }
    
    private int SetSyncProgress(String status, int use_code)
    {
        DBOperations dbo = new JDBCUtil().getDBOperations();
        return dbo.doUpdate("Update AT_SYNC_STATUS set SYS_STATUS = '".concat(status).concat("' Where SYS_USE_CODE = ").concat(String.valueOf(use_code)));
    }
    
    private String fileUpload(HttpServletRequest request, Map <String, String[]> _params, boolean isFoto) throws ServletException, UnsupportedEncodingException, IOException
    {        
        String file_name = getRequestParam("file_name", _params);
        String use_code = getRequestParam("use_code", _params);
        String resutlt = "DSS_ERROR TRY LOAD FILE WITHOUT FILE";
        try 
        {           
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);      
            if (isMultipart) 
            {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                while (iter.hasNext()) 
                {
                    FileItem item = (FileItem) iter.next();
                    if (!item.isFormField()) 
                    {
                        files.Uploader upl = new JDBCUtil().getFileOperations(LOG_NAME);
                        if (!isFoto)
                        {                            
                            resutlt =upl.doUpload(item, APP_CONSTANT.FULL_PATH.concat("upload\\") + APP_CONSTANT.FOLDER_DATE() + "\\".concat(use_code).concat("\\"),file_name,false);
                        }
                        else
                        {
                            resutlt =upl.doUpload(item, APP_CONSTANT.FULL_PATH.concat("photo\\") + APP_CONSTANT.FOLDER_DATE() + "\\".concat(use_code).concat("\\"),file_name,true);
                        }
                    }
                }
            }
        } 
        catch (Exception e) 
        {
           Logger.getLogger(LOG_NAME).log(Level.SEVERE,null , e);
           resutlt = "DSS_ERROR ".concat(e.getMessage());
        }
        return resutlt;
    }
    
    private String getRequestParam(String _name, Map <String, String[]> _params)
    {
        if(_params.containsKey(_name))
        {
            return _params.get(_name)[0];
        }
        else
        {
            return "";
        }
    }
        
    private String setJSONAnswer(String _operation, String _result)
    {        
        if (_result.contains("DSS_ERROR"))
        {
            return "{\"operation\": \"".concat(_operation)
                .concat("\", \"result\": [").concat("")
                .concat("] , \"error_message\": \"").concat(_result)
                .concat("\" , \"error_code\": \"").concat("")
                .concat("\"}");
        }
        else
        {
            if (_result.charAt(0) != '\"' && _result.charAt(0) != '[' && _result.charAt(0) != '{')
            {
                _result = "[{\"STATUS\":\"".concat(_result).concat("\"}]");
            }
            return "{\"operation\": \"".concat(_operation)
                    .concat("\", \"result\": ").concat(_result)
                    .concat(" , \"error_message\": \"").concat("")
                    .concat("\" , \"error_code\": \"").concat("")
                 .concat("\"}");
        }
    }
    
    private String getAuth(Map <String, String[]> _params)
    {
        String IMEI = getRequestParam("IMEI",_params);   
        IMEI = IMEI.replace("'", "").replace("\"", "").replace("-", "");
        return new JDBCUtil().getDBOperations().doQueryJSON("SELECT * FROM VIEW_AUTHENTICATION_IMEI WHERE IME_SERIAL_NUMBER = '".concat(IMEI).concat("'"));
    }
    
    private String getSyncStatus(Map <String, String[]> _params)
    {
        String _use_code = getRequestParam("use_code",_params);
        if (_use_code.length()>0)
        {
            ArrayList<String> result = new JDBCUtil().getDBOperations().doQueryString("select isNull(SYS_STATUS,'06') as SYS_STATUS from at_sync_status where SYS_USE_CODE = ".concat(_use_code), "SYS_STATUS");
            if (result.isEmpty())
            {
                return "DSS_ERROR ".concat("GET STATUS ERROR WITH USE CODE ").concat(_use_code);
            }
            else
            {
                //return "[{\"STATUS\": \"".concat(result.iterator().next()).concat("\"}]");
                return result.iterator().next();
            }
        }
        else
        {
            return "DSS_ERROR ".concat("GET STATUS ERROR WITH NO USE CODE ");
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
            throws ServletException, IOException 
    {
        request.setCharacterEncoding("UTF-8");	
        response.setCharacterEncoding("UTF-8");                
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
