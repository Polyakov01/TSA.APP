/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import DB.DBOperations;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import static org.apache.poi.hssf.usermodel.HeaderFooter.file;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.json.JSONObject;
import util.JDBCUtil;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

/**
 *
 * @author PK
 */
public final class ExportExcel 
{
    //private static final String  _JNDINAME = "JNDI_SAPSAN";
    public static String DoExport () throws NamingException, IOException, SQLException          
    {
         XSSFWorkbook workbook = null;
          FileOutputStream fileOut = null;
          Connection con = null;
       try
       {
        Logger.getLogger("EXPORT").log(Level.INFO,"1");
        workbook= new XSSFWorkbook();
        DBOperations dbo = new JDBCUtil().getDBOperations();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.'); 
        
        con = dbo.getConnection();
        //PreparedStatement st = con.prepareStatement("EXEC REPORT_VIEW_EXPORT @IN_WHERE_CLAUSE = '1=1', @IN_VIEW_NAME = 'VIEW_ST_SALEPOINT', @IN_CURRENT_USER = '192'");
        PreparedStatement st = con.prepareStatement("SELECT * FROM ST_SALEPOINT ss    INNER JOIN ST_SALEPOINT ss1 ON 1=1  INNER JOIN ST_SALEPOINT ss2 ON 1=1    INNER JOIN ST_SALEPOINT ss3 ON 1=1    INNER JOIN ST_SALEPOINT ss4 ON 1=1    INNER JOIN ST_SALEPOINT ss5 ON 1=1  INNER JOIN ST_SALEPOINT ss6 ON 1=1  INNER JOIN ST_SALEPOINT ss7 ON 1=1 ");
        

        ResultSet rs = st.executeQuery();
                
        Logger.getLogger("EXPORT").log(Level.INFO,"2");         
        XSSFSheet sh = workbook.createSheet("RowData");          
       Logger.getLogger("EXPORT").log(Level.INFO,"3");
       
        ResultSetMetaData rsmd = rs.getMetaData();
        Row row = sh.createRow(0);
               
        
        int j = 1;
        while (rs.next()) 
        {
            Row dataRow = sh.createRow(j);
            int numColumns = rsmd.getColumnCount();
         //   Logger.getLogger("EXPORT").log(Level.INFO,"4");
            for (int i = 1; i < numColumns + 1; i++) 
            {
                String column_name = rsmd.getColumnName(i);
                String value;
                if (j==1)
                {
                    row.getCell(i-1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(column_name); 
                    
                    if (rs.getObject(column_name) == null)
                    {
                        value = "";
                    }
                    else
                    {
                        value = String.valueOf(rs.getObject(column_name));
                    }                    
                    dataRow.getCell(i-1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(value);                      
                }
                else
                {
                    if (rs.getObject(column_name) == null)
                    {
                        value = "";
                    }
                    else
                    {
                        value = String.valueOf(rs.getObject(column_name));
                    }                    
                    dataRow.getCell(i-1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(value);                         
                }

               // if(dataRow == null)
               // {
                    //obj.put(column_name, JSONObject.NULL);               
               // }
               // else
               // {
            //        obj.put(column_name, rs.getObject(column_name));
              //  }
            }
            j++;
        }     
       
              Logger.getLogger("EXPORT").log(Level.INFO,"7"); 
       fileOut = new FileOutputStream("D:\\jboss\\standalone\\deployments\\ROOT.war\\BAT\\csv\\test4.xlsx");
        workbook.write(fileOut);
       }
       catch( Exception e)
       {
           
       }
       finally
       {
           con.close();
            fileOut.close();
            workbook.close(); 
       }
        
        
        return "";
}
    
    public static String getImportDifference()
    {                       
        DBOperations dbo = new JDBCUtil().getDBOperations();
        ArrayList<String> missingPositions = new ArrayList<>();
        dbo.doQueryString("select MISSING_POSITION from IMP_TARGET_SKU_CHECK ", "MISSING_POSITION");
        String result = "";
        int i = 0;
        for (String item:missingPositions)
        {
              result = result.concat("<br>").concat(item);
              result = result.concat("");
              i++;
        }
        if (i <3 )
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO,"getImportDifference ALL OK");
            result = "";
        }          
        return result;

    }
    
    
    
    private static String cellValueToString(Cell _cell)
    {
        String result = "ERROR WHEN GET VALUE";
        switch(_cell.getCellType()) 
        {
            case Cell.CELL_TYPE_STRING:
                result = _cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(_cell)) 
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    result =  dateFormat.format(_cell.getDateCellValue());
                } else 
                {
                     _cell.setCellType(Cell.CELL_TYPE_STRING);
                    result = _cell.getStringCellValue();
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                result =  String.valueOf(_cell.getBooleanCellValue());
                break;
            default:
                result = "";
            break;
        }
        
        return result;
    }
}
