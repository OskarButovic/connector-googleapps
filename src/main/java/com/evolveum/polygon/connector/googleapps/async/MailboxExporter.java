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
import com.google.gdata.client.appsforyourdomain.audit.AuditService;
import com.google.gdata.client.appsforyourdomain.audit.MailBoxDumpRequest;
import com.google.gdata.data.appsforyourdomain.generic.GenericEntry;
import com.google.gdata.util.ServiceException;

/**
 *
 * @author oskar.butovic
 */
public class MailboxExporter {
    
    public static final String REQUEST_STATUS_COMPLETED = "CREATED";
    public static final String REQUEST_STATUS_ERROR = "ERROR";
    public static final String REQUEST_STATUS_PENDING = "PENDING";
    
    
    public String createMailboxForExport(String userEmail, AuditService service){
        try {
            MailBoxDumpRequest request = new MailBoxDumpRequest();
            request.setUserEmailAddress(userEmail);
            
            request.setIncludeDeleted(true);
            request.setPackageContent("FULL_MESSAGE");
            
            GenericEntry mailboxDumpEntry = service.createMailboxDumpRequest(request);

            //TODO overit datovou strukturu debugem
            String requestId = mailboxDumpEntry.getProperty("requestId");
            return requestId;
        } catch (IOException ex) {
            Logger.getLogger(MailboxExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (ServiceException ex) {
            Logger.getLogger(MailboxExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
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
