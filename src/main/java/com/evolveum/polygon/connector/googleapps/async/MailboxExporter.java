/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.async;

import com.evolveum.polygon.connector.googleapps.GroupHandler;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.User;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.objects.Uid;

/**
 *
 * @author oskar.butovic
 */
public class MailboxExporter {
    
    public static final String REQUEST_STATUS_COMPLETED = "CREATED";
    public static final String REQUEST_STATUS_ERROR = "ERROR";
    public static final String REQUEST_STATUS_PENDING = "PENDING";
    
    
    public String createMailboxForExport(String domain, String userEmail){
        //TODO implement
        return null;
    }
    
    public String getRequestStatus(String requestId){
        //TODO implement
        return null;
    }
    
    public void downloadPreparedExport(String requestId){
        //TODO implement
    }
     
    public static String findUserEmail(Uid uid, Directory.Users service){
        try {
            Directory.Users.Get request = service.get(uid.getUidValue()).setAlt("json");
            User reqResult = request.execute();
            if(reqResult != null){
                return reqResult.getPrimaryEmail();
            }else{
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(GroupHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectorIOException(ex);
        }
    }
}
