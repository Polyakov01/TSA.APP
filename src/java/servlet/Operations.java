/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DB.DBOperations;
import classes.APP_CONSTANT;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.JDBCUtil;

/**
 *
 * @author PK
 */
@WebServlet(name = "Operations", urlPatterns = {"/Operations"})
public class Operations extends HttpServlet 
{    
    private final String OPERATION_GET_DATA = "get_query_data";
    private final String OPERATION_LOGIN = "get_login";
    private final String OPERATION_GET_ANY_QUERY = "exec_query";
    private final String OPERATION_UPDATE = "set_update";
    private final String OPERATION_INSERT = "set_insert";
    private final String OPERATION_VIEW_EXPORT = "view_export";    
    private final String OPERATION_RELOCATE = "relocate";     
    private final String GET_OPERATION = "operation";      
   // private final String APP_CONSTANT.LOG_NAME = "TSA.APP.KZ";
  

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
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");                   
        //Logger.getLogger("MEGALOG").log(Level.SEVERE, null, "sdfsdfsdfsdf");
        //System.out.println("sdfsdfsd");
        try 
        {                     
            String params = getBody(request);
            System.out.println(params);
           //Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, null, params);
            if (!params.isEmpty())
            {
                String current_operation; 
                JSONObject o;             
                o = new JSONObject(params);
                current_operation = o.getString(GET_OPERATION);
                System.out.println(current_operation);
                switch(current_operation)
                {
                    case OPERATION_GET_DATA:
                    {                
                        response.getWriter().println(getQueryData(o));                    
                        break;                        
                    }
                    case OPERATION_LOGIN:
                    {                
                        response.getWriter().println(getLogin(o));                    
                        break;                        
                    }
                    case OPERATION_GET_ANY_QUERY:
                    {                
                        response.getWriter().println(getAnyQuery(o));                    
                        break;                        
                    }                     
                    case OPERATION_UPDATE:
                    {                
                        response.getWriter().println(setUpdateData(o));                    
                        break;                        
                    } 
                    case OPERATION_INSERT:
                    {                
                        response.getWriter().println(setInsert(o));                    
                        break;                        
                    } 
                    case OPERATION_VIEW_EXPORT:
                    {                
                        response.getWriter().println(doViewExport(request.getServerName(),request.getServerPort(),o));                    
                        break;                        
                    } 
                    case OPERATION_RELOCATE:
                    {                
                        response.getWriter().println(relocate(o));                    
                        break;                        
                    } 
                    default: 
                    {
                        response.getWriter().println("{\"error\": \"ERROR: UNKNOWN OPERATION!\", \"code\": \"servlet error\"}");                    
                        break;                        
                    }
                }
            }
            else
            {
                response.getWriter().println("{\"error\": \"ERROR: OPERATION DEFINITION NEEDED!\", \"code\": \"servlet error\"}");                        
            }
        } 
        catch (IOException | JSONException ex )              
        {            
            response.getWriter().println("{\"error\": \"ERROR: ".concat(ex.getMessage().replaceAll("\"", "")).concat("\", \"code\": \"servlet error\"}")); 
            Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.SEVERE, null, ex);       
        }
    }
    
    private  String getBody(HttpServletRequest request) throws IOException 
    {
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } 
        catch (IOException ex) 
        {
            throw ex;
        } 
        finally 
        {
            if (bufferedReader != null) 
            {
                try 
                {
                    bufferedReader.close();
                } 
                catch (IOException ex) 
                {
                    throw ex;
                }
            }        
        }
        body = stringBuilder.toString();
        return body;
    }

    private String getRequestParam(String _name, Map <String, String[]> _params, Boolean prepareParam)
    {
        if(_params.containsKey(_name))
        {
            if (prepareParam)
            {
                return _params.get(_name)[0].replaceAll("--", "").replaceAll("'", "").replaceAll("\"", "");
            }
            else
            {
                return _params.get(_name)[0];
            }
        }
        else
        {
            return "";
        }
    }
    
    private String getLogin(JSONObject _params) throws JSONException
    {        
        String login = _params.getString("login"); //getRequestParam("login",_params,true);
        String password = _params.getString("password"); //getRequestParam("password",_params,true);
        String useCode;
        String accessToken;
        String result;
        int accessTokenActive;
        String salt = "(Nic!odY X<2E+{-_COBI+e95/^ a![EQh|);Z|V{lYR*_]e)=T%*e;G/f;ZlJjH')";
        String queryString = "SELECT * FROM VIEW_AUTHENTICATION WHERE USE_LOGIN = '".concat(login).concat("' AND USE_PASSWORD = '").concat(password).concat("'");
        try 
        {
            DBOperations dbo = new JDBCUtil().getDBOperations();           
            ArrayList<String> loginResult = dbo.doQueryString(queryString, "USE_CODE","ACCESS_TOKEN_ACTIVE","USE_ACCESS_TOKEN");
            if (loginResult.size() == 3)
            {
                useCode = loginResult.get(0);
                accessTokenActive = Integer.parseInt(loginResult.get(1));
                if(accessTokenActive == 1)
                {
                    accessToken = loginResult.get(2);
                    dbo.doBatchUpdate("UPDATE ST_USER SET USE_ACCESS_TOKEN_EXPIRED = DATEADD(MINUTE, 30, GETDATE()) WHERE USE_CODE = '".concat(useCode).concat("'"));
                }
                else
                {
                    //token time already expired
                    DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss");
                    Calendar cal = Calendar.getInstance();
                    accessToken = DigestUtils.sha1Hex(useCode+dateFormat.format(cal.getTime())+salt);
                    dbo.doUpdate("UPDATE ST_USER SET USE_ACCESS_TOKEN_EXPIRED = DATEADD(MINUTE, 30, GETDATE()), USE_ACCESS_TOKEN = '".concat(accessToken).concat("' WHERE USE_CODE = '").concat(useCode).concat("'"));
                }
                result = "{\"status\": \"ok\", \"token\": \""+accessToken+"\"}";            
            }
            else
            {
                result = "{\"status\": \"error\", \"message\": \"Auth failed\"}";
            }
        } 
        catch (Exception ex) 
        {
           result = "DB_ERROR: " + ex.getMessage();
           Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.SEVERE, null, ex);
        }                 
        return result;
    }
    
    private String getQueryData(JSONObject _params) throws JSONException
    {            
        String token = _params.getString("token");
        String fields = _params.getString("fields");
        String table = _params.getString("table");
        String where = _params.getString("where");
        String order = _params.getString("order");
        String start = _params.getString("start");
        String end = _params.getString("end");
        String result;
        DBOperations dbo = new JDBCUtil().getDBOperations();
        String query = queryPrepare(fields, table, where, token, order, start, end);
        
        if (query.contains("DSS_ERROR"))
        {
            return "{\"error\": \"".concat(query.replaceAll("DSS_ERROR","")).concat("\", \"code\": \"token_expired\"}");      
        }
        else
        {
            result = dbo.doQueryJSON(query);             
            if (result.contains("DSS_ERROR"))
            {
                Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.SEVERE, result);
                return "{\"error\": \"".concat(result.replaceAll("'", "").replaceAll("\"", "")).concat("\", \"code\": \"sql_error\"}");
            }
            else
            {
                return result;
            }
            
        }
    }

    private String getAnyQuery(JSONObject _params) throws JSONException
    {
        String token = _params.getString("token");
        String query = _params.getString("query");
        String result;
        
        DBOperations dbo = new JDBCUtil().getDBOperations();

        Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, 
                                    "QUERY PARAM \n!--- token - ".concat(token).concat( "---!")
                                    .concat("\n !--- query - ").concat(query).concat("---!"));
        
        

        if(!query.isEmpty() && !token.isEmpty())
        { 
            if(checkToken(token))
            {
                Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO,  "\n!---START QUERY ---!"
                                                            .concat(query)
                                                            .concat("\n!---END QUERY ---!") );
                result  = dbo.doQueryJSON(query); 
                Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, " QUERY RESULT ".concat(result));
            }
            else
            {
                result = "{\"error\": \"token expired\", \"code\": \"token_expired\"}";
            }
        }
        else
        {
            result = "{\"error\": \"query is empty\", \"code\": \"query_is_empty\"}";
        }
        return result;
    }
    
    private String setUpdateData(JSONObject _params) throws JSONException
    {            
        String token = _params.getString("token");
        String fields = _params.getString("fields");
        String table = _params.getString("table");
        String where = _params.getString("where");
        String result;
        DBOperations dbo = new JDBCUtil().getDBOperations();        
        Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, "QUERY PARAM \n" + "!--- token - {0} ---! \n !--- table - {1} ---!\n !--- fields - {2} ---!\n !--- where - {3} ---!", new Object[]{token, table, fields, where});

        if(!fields.isEmpty()&& !table.isEmpty()  && !where.isEmpty() && !token.isEmpty())
        {                     
            String queryString = "";

            if(checkToken(token))
            {
                queryString += "UPDATE " + table + " SET " + fields + " WHERE " + where;
                Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, "!---START QUERY --- ! \n {0} \n!---END QUERY ---!", queryString);           
                ArrayList<String> updateResult = new ArrayList<>();
                dbo.doUpdate(queryString,updateResult);                
                String errors = "";
                for (int i = 0; i<updateResult.size();i++)
                {
                    errors = errors.concat("DSS_ERROR ").concat(String.valueOf(i)).concat(" - ").concat(updateResult.get(i)).concat(" -| ");
                } 
                if (errors.isEmpty())
                {
                    return "";
                }
                else
                {
                    return "{\"error\": \""+errors+"\", \"code\": \"sql_error\"}";
                }
                    
            }
            else
            {
               return "{\"error\": \"token expired\", \"code\": \"token_expired\"}";
            }
        }
        else
        {
           return "{\"error\": \"query is empty\", \"code\": \"query_is_empty\"}";
        }
    }
    private String setInsert(JSONObject _params) throws JSONException
    {
        String token = _params.getString("token");
        String fields = _params.getString("fields");
        String values = _params.getString("values");
        String table = _params.getString("table");
        String result;
        
        DBOperations dbo = new JDBCUtil().getDBOperations();        
        Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, "INSERT \n" + "!--- token - {0} ---! \n !--- table - {1} ---!\n !--- fields - {2} ---!\n !--- where - {3} ---!", new Object[]{token, table, fields, values});

  

        if(!fields.isEmpty() && !table.isEmpty() && !values.isEmpty() && !token.isEmpty())
        {            
            String insertString = "";
            if(checkToken(token))
            {
                insertString += "INSERT INTO " + table + " (" + fields + ") VALUES (" + values + ");";
                insertString += "SELECT IDENT_CURRENT('" + table + "') AS 'key'";
                result = dbo.doQueryJSON(insertString);
                if (result.contains("DSS_ERROR"))
                {
                    result = result.replaceAll("'","").replaceAll("\"", "");
                    result = "{\"error\": \"Unable get inserted row key!, ".concat(result).concat(" \" , \"code\": \"sql_error\"}");
                }                   
            }
            else
            {
                result = "{\"error\": \"token expired\", \"code\": \"token_expired\"}";
            }
        }
        else
        {
             result = "{\"error\": \"query is empty\", \"code\": \"query_is_empty\"}";
        }
        return result;
    }
    
    private String doViewExport(String serverAddress,int serverPort,JSONObject _params) throws JSONException  
    {        
        String token = _params.getString("token");
        String table = _params.getString("table");
        String where = _params.getString("where");
        String order = _params.getString("order");
        String reportCode = _params.getString("report_code");
        String reportType;
        DBOperations dbo = new JDBCUtil().getDBOperations();
        String useCode = dbo.doQueryString("select USE_CODE from st_user where USE_ACCESS_TOKEN = '".concat(token).concat("'"), "USE_CODE").get(0);
        //report or export
        if (checkToken(token) && !useCode.contains("DSS_ERROR"))
        {
            if (!reportCode.isEmpty())
            {
                ArrayList<String> str = dbo.doQueryString("select REP_METHOD, REP_VIEW_NAME,REP_CODE from ST_REPORTS WHERE REP_CODE = '".concat(reportCode).concat("'"), "REP_METHOD","REP_VIEW_NAME","REP_CODE");
                if (str.size()==3)
                {
                    reportType = str.get(0);
                    table = str.get(1);
                    if (reportType.contains("DSS_ERROR"))
                    {
                        reportType = reportType.replaceAll("'", "").replaceAll("\"", "").replaceAll("DSS_ERROR", "");
                        return "{\"error\": \"".concat("Can't get report type, Error: ".concat(reportType)).concat("\", \"code\": \"db_error\"}");
                    }
                    else
                    {                    
                        switch(reportType)
                        {
                            case "CSV":
                            {                            
                                return "{\"file\": \"".concat(doCSVExport(token, table,where, serverAddress, serverPort, dbo,useCode)).concat("\"}");
                            }
                            case "CONNECTION":
                            {                         
                                String url = "http://".concat(serverAddress).concat(":").concat(String.valueOf(serverPort)).concat("\\").concat(APP_CONSTANT.SYSTEM_FOLDER_NAME).concat("\\").concat(APP_CONSTANT.REPORTS_TEMPLATE_FOLDER).concat(table.concat(".xlsm"));      
                                url = url.replaceAll("\\\\", "/");
                                return "{\"file\": \"".concat(url).concat("\"}");
                            }
                            case "HASH":
                            {                          
                                try 
                                {                                        
                                    where = where.replaceAll("'", "''");
                                    String hashname = dbo.doQueryString("exec REPORT_REGISTER_IN_CATALOG @IN_TABLE = '"+table+"', @IN_WHERE_CLAUSE = '"+where+"', @IN_LIMIT_CLAUSE  = '', @IN_ORDER_BY = '"+order+"', @IN_USE_CODE = '"+useCode+"'", "HASHNAME").get(0);                                                                     

                                    if (hashname.contains("DSS_ERROR"))
                                    {
                                        return "{\"error\": \"".concat(hashname.replaceAll("DSS_ERROR","")).concat("\", \"code\": \"db_error\"}"); 
                                    }
                                    else
                                    {
                                        File template = new File(APP_CONSTANT.FULL_PATH.concat(APP_CONSTANT.REPORTS_TEMPLATE_FOLDER).concat(table).concat(".xlsm"));
                                        String destFileName = APP_CONSTANT.FULL_PATH.concat(APP_CONSTANT.REPORTS_COMPLETE_FOLDER).concat(APP_CONSTANT.FOLDER_DATE()).concat(useCode).concat("_").concat(table).concat(".xlsm");
                                        File destination = new File(destFileName);
                                        FileUtils.copyFile(template, destination);
                                        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(destination));
                                        POIXMLProperties props = workbook.getProperties();
                                        POIXMLProperties.ExtendedProperties ext = props.getExtendedProperties();                                
                                        ext.getUnderlyingProperties().setCompany(hashname);
                                        FileOutputStream out = new FileOutputStream(destFileName);
                                        workbook.write(out);
                                        out.close();
                                        
                                        String url = "http://".concat(serverAddress).concat(":").concat(String.valueOf(serverPort)).concat("\\").concat(APP_CONSTANT.SYSTEM_FOLDER_NAME).concat("\\").concat(APP_CONSTANT.REPORTS_COMPLETE_FOLDER).concat(APP_CONSTANT.FOLDER_DATE()).concat(useCode).concat("_").concat(table).concat(".xlsm");      
                                        url = url.replaceAll("\\\\", "/");
                                        return "{\"file\": \"".concat(url).concat("\"}");
                                    }
                                } 
                                catch (IOException ex) 
                                {
                                    Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.SEVERE,null ,ex);
                                    return "{\"error\": \"".concat(ex.getMessage().replaceAll("\"","")).concat("\", \"code\": \"db_error\"}");
                                }                         
                            }
                            default:
                            {
                                return "{\"error\": \"".concat("UNKNOWN REPORT TYPE ".concat(reportType)).concat("\", \"code\": \"db_error\"}"); 
                            }                        
                        }
                    }
                }   
                else
                {
                    return "{\"error\": \"".concat("Can't get report information").concat("\", \"code\": \"db_error\"}");
                }
            }
            else
            {            
                return "{\"file\": \"".concat(doCSVExport(token, table,where, serverAddress, serverPort, dbo,useCode)).concat("\"}");
            }
        }
        else
        {
            return "{\"error\": \"token expired\", \"code\": \"token_expired\"}";        
        }
    }
    
    private String doCSVExport(String token,String table,String where, String serverAddress, int serverPort, DBOperations dbo,String useCode)
    {        
        String query = "EXEC REPORT_VIEW_EXPORT  @IN_WHERE_CLAUSE = '".concat(where).concat("',@IN_VIEW_NAME = '").concat(table).concat("' ,@IN_TOKEN = '").concat(token).concat("'");
        String fileName = useCode.concat("_").concat(table).concat(APP_CONSTANT.FILE_DATE("zip"));
        String csvResult = dbo.doQueryCSVFile(APP_CONSTANT.FULL_PATH.concat(APP_CONSTANT.REPORTS_COMPLETE_FOLDER).concat(fileName), query);
        if (csvResult.equals("OK"))
        {
            String url = "http://".concat(serverAddress).concat(":").concat(String.valueOf(serverPort)).concat("\\").concat(APP_CONSTANT.SYSTEM_FOLDER_NAME).concat("\\").concat(APP_CONSTANT.REPORTS_COMPLETE_FOLDER).concat(fileName);      
            url = url.replaceAll("\\\\", "/");
            return url;
        }
        else 
        {
            return "{\"error\": \"".concat(csvResult.replaceAll("DSS_ERROR","")).concat("\", \"code\": \"db_error\"}"); 
        }
    }
    
    private String queryPrepare(String fields, String table,  String where, String token, String order, String start, String end)
    {
        if(!fields.isEmpty()  && !table.isEmpty()  && !where.isEmpty()  && !token.isEmpty())
        { 
            String queryString = "";            
            if(checkToken(token))
            {
                String orderExpression = "";
                Boolean isOrder = false;
                if(!order.isEmpty())
                {
                    isOrder = true;
                    orderExpression = " ORDER BY " + order;
                }

                if(!start.isEmpty())
                {
                   queryString += "SELECT * FROM  (SELECT " + fields + ", ROW_NUMBER() OVER (ORDER BY (SELECT 1)"+(isOrder ? ","+order : "")+") as row FROM " + table + " " + where + " ) a WHERE row >= " + start + " and row <= " + end;
                }
                else
                {
                    queryString += "SELECT " + fields + " FROM " + table + " " + where + orderExpression;
                }

                Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, "!---START QUERY ---!\n"
                                                    .concat(queryString)
                                                    .concat("\n!---END QUERY ---!"));
                return queryString;
            }
            else
            {
                return "{\"error\": \"token expired\", \"code\": \"token_expired\"}";
            }
        }
        else
        {
            Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.SEVERE, "!--- token - ".concat(token).concat(" ---!")
                                            .concat("\n!--- fields - ").concat(fields).concat(" ---!")
                                            .concat("\n!--- table - ").concat(table).concat(" ---!")
                                            .concat("\n!--- where - ").concat(where).concat(" ---!")
                                            .concat("\n!--- order - ").concat(order).concat(" ---!")        
                                            .concat("\n!--- start - ").concat(start).concat(" ---!")
                                            .concat("\n!--- end - ").concat(end).concat(" ---!"));    
            return "DSS_ERROR Empty fields";  
        }
    }
    
    private String relocate(JSONObject _params) throws JSONException
    {
        String hash = _params.getString("hash"); //getRequestParam("hash",_params,true);
        DBOperations dbo = new JDBCUtil().getDBOperations();
        hash = hash.replaceAll("'", "").replaceAll("-", "").replaceAll("\"", "");
        
        ArrayList<String> tokenList = dbo.doQueryString("SELECT a.* , su.USE_ACCESS_TOKEN FROM AT_APP_RELOCATE a INNER JOIN ST_USER su ON su.USE_CODE = a.AAR_USE_CODE where AAR_HASH = '".concat(hash).concat("'"), "USE_ACCESS_TOKEN");
        if (tokenList.size()==1 && !tokenList.get(0).contains("DSS_ERROR"))
        {
            if (checkToken(tokenList.get(0)))
            {
                String result = dbo.doQueryJSON("SELECT a.* , su.USE_ACCESS_TOKEN FROM AT_APP_RELOCATE a INNER JOIN ST_USER su ON su.USE_CODE = a.AAR_USE_CODE where AAR_HASH = '".concat(hash).concat("'"));
                dbo.doUpdate("Delete from AT_APP_RELOCATE where  AAR_HASH = '".concat(hash).concat("'"));
                return result;
            }
            else
            {
                return "{\"error\": \"token expired\", \"code\": \"token_expired\"}";
            }
        }
        else
        {
            return "{\"error\": \""+tokenList.get(0).replaceAll("DSS_ERROR ", "").replaceAll("\"", "")+"\", \"code\": \"no_hash\"}";
        }
                
    }
    
   private boolean checkToken (String token)
   {
       DBOperations dbo = new JDBCUtil().getDBOperations();
       token = token.replaceAll("-", "").replaceAll("'", "");
       String tokenQuery = "SELECT (CASE WHEN USE_ACCESS_TOKEN_EXPIRED > GETDATE() THEN 1 ELSE 0 END) AS ACCESS_TOKEN_ACTIVE FROM ST_USER WHERE USE_ACCESS_TOKEN = '".concat(token).concat("'");       
       ArrayList<String> result = dbo.doQueryString(tokenQuery,"ACCESS_TOKEN_ACTIVE");
       if (result.size()==1)
       {         
           if(Integer.parseInt(result.get(0)) == 1)
            {
                dbo.doBatchUpdate("UPDATE ST_USER SET USE_ACCESS_TOKEN_EXPIRED = DATEADD(MINUTE, 30, GETDATE()) WHERE USE_ACCESS_TOKEN = '".concat(token).concat("'"));                
                return true;
            }
            else
            {
                Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.INFO, "Token expired");
                return false;
            }
       }
       else
       {
           Logger.getLogger(APP_CONSTANT.LOG_NAME).log(Level.SEVERE, "DB_ERROR: Can't check token!");
           return false;
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
