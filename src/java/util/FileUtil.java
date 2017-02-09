/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import classes.APP_CONSTANT;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class FileUtil 
{
    private final static String LOG_NAME = "TSA.MOB.KZ";
    
    public static String[] getUnzippedQuery(String fullPath) 
    {
        Logger.getLogger(LOG_NAME).log(Level.INFO, "getUnzippedQuery: ".concat(fullPath));
        String[] result;
        try 
        {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fullPath));
            ZipEntry ze = zis.getNextEntry();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            while (ze != null) {
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            result = new String(bos.toByteArray(), "UTF-8").split(";");
        } 
        catch (IOException ex) 
        {
            result = new String [] {"DSS_ERROR".concat(ex.getMessage())};
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
        return result;
    }


    public static String getPathToSyncFile(int use_code, String folderDate) 
    {
        String filePath = APP_CONSTANT.FULL_PATH.concat(APP_CONSTANT.SYNC_FOLDER_NAME).concat(folderDate).concat(String.valueOf(use_code)).concat("\\");
        File f = new File(filePath);        
        if (!f.exists()) {f.mkdirs(); Logger.getLogger(LOG_NAME).log(Level.FINE, "FOLDER CREATED ".concat(filePath));}        
        return filePath;
    }
    
    public static String getPathToUploadFile(String use_code) 
    {
        String filePath = APP_CONSTANT.FULL_PATH.concat(APP_CONSTANT.SYNC_UPLOAD_FOLDER_NAME).concat(APP_CONSTANT.FOLDER_DATE()).concat(use_code).concat("\\");
        File f = new File(filePath);        
        if (!f.exists()) {"DSS_ERROR ".concat("UPLOAD FILE NOT FOUND");}        
        return filePath;
    }

    public static boolean mkQueryZipFile(String filePath, int use_code, StringBuilder sb) 
    {
        boolean result = false;
        FileOutputStream fos = null;
        try 
        {
            fos = new FileOutputStream(filePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry(String.valueOf(use_code) + ".txt");
            zos.putNextEntry(ze);
            zos.write(sb.toString().getBytes("UTF-8"));
            zos.closeEntry();
            zos.close();
            result = true;
            Logger.getLogger(LOG_NAME).log(Level.FINE, "CREATING MOBILEDATA SYNC FILE ".concat(filePath));
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        } 
        finally 
        {
            try 
            {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
}
