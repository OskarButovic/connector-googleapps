/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.async;

import com.evolveum.polygon.connector.googleapps.GoogleAppsConfiguration;
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
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oskar.butovic
 * refference see https://developers.google.com/admin-sdk/email-audit/#creating_a_mailbox_for_export
 */
public class MailboxExporter {
    
    public static final String REQUEST_STATUS_COMPLETED = "COMPLETED";
    public static final String REQUEST_STATUS_ERROR = "ERROR";
    public static final String REQUEST_STATUS_PENDING = "PENDING";
    
    
    public String createMailboxForExport(String userEmail, AuditService service, GoogleAppsConfiguration configuration){
        try {
            MailBoxDumpRequest request = new MailBoxDumpRequest();
            request.setAdminEmailAddress(configuration.getAdminEmail());
            request.setUserEmailAddress(userEmail);
            
//            Calendar beginDate = Calendar.getInstance();
//            beginDate.set(2009, Calendar.JULY, 1, 4, 30);
//            request.setBeginDate(beginDate.getTime());
//
//            Calendar endDate = Calendar.getInstance();
//            endDate.set(2009, Calendar.AUGUST, 30, 20, 0);
//            request.setEndDate(endDate.getTime());
            
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
    
    /**
     * userName must be email address part before @
     */
    public String getRequestStatus(String requestId, String userName, AuditService service){
        try {
            GenericEntry mailboxDumpEntry1 = service.retrieveMailboxDumpRequest(userName, requestId);
            String status = mailboxDumpEntry1.getProperty("status");
            return status;
        } catch (IOException ex) {
            Logger.getLogger(MailboxExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (ServiceException ex) {
            Logger.getLogger(MailboxExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    public void downloadPreparedExport(String requestId, String userName, AuditService service, GoogleAppsConfiguration configuration){
        try {
            GenericEntry mailboxDumpEntry1 = service.retrieveMailboxDumpRequest(userName, requestId);
            String numberOfFiles = mailboxDumpEntry1.getProperty("numberOfFiles");
            int numberOfFilesInt = Integer.decode(numberOfFiles);
            List<String> fileUrls = new ArrayList<String>();
            for(int i = 0; i < numberOfFilesInt; i++){
                fileUrls.add(mailboxDumpEntry1.getProperty("fileUrl" + i));
            }
            int fileCounter = 0;
            for(String fileUrl : fileUrls){
                URL website = new URL(fileUrl);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(configuration.getMailboxExportDir() + "/" + userName + "@" + configuration.getDomain() + "_" + fileCounter);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fileCounter++;
            }
            //TODO delete exported and downloaded mailbox
        } catch (IOException ex) {
            Logger.getLogger(MailboxExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (ServiceException ex) {
            Logger.getLogger(MailboxExporter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
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
