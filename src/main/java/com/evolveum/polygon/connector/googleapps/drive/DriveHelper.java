/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.drive;

import com.evolveum.polygon.connector.googleapps.GoogleAppsConfiguration;
import com.evolveum.polygon.connector.googleapps.GroupHandler;
import com.evolveum.polygon.connector.googleapps.Main;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Permissions;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
//import com.google.api.services.drive.model.File;
import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.objects.Uid;

/**
 *
 * @author oskar.butovic
 */
public class DriveHelper {
    
    /**
     * example on 
     */
    public void changeOwnerships(String emailFrom, String emailTo, GoogleAppsConfiguration configuration){
        try {
            JsonFactory JSON_FACTORY = GoogleAppsConfiguration.JSON_FACTORY;
            HttpTransport httpTransport = GoogleAppsConfiguration.HTTP_TRANSPORT;
            File privateKeyFile = new File(configuration.getDrivePrivateCert());
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(configuration.getServiceAccountId())
                    .setServiceAccountPrivateKeyFromP12File(privateKeyFile)
                    .setServiceAccountScopes(Main.DRIVE_SCOPES)
                    .setServiceAccountUser(emailFrom)
                    .setClientSecrets(configuration.getClientId(), SecurityUtil.decrypt(configuration.getClientSecret()))
                    .build();
            Drive service = new Drive.Builder(httpTransport, JSON_FACTORY, credential).build();
            List<com.google.api.services.drive.model.File> ownedFiles = new ArrayList<com.google.api.services.drive.model.File>();
            Files.List request = service.files().list();
            do {
                FileList files = request.execute();

                ownedFiles.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() > 0);
            //TODO remove logger
            //printFiles(ownedFiles, emailFrom);
            
            for(com.google.api.services.drive.model.File googleFile : ownedFiles){
                transferFile(emailFrom, emailTo, googleFile, service);
            }
            
            throw new RuntimeException("implementation not finished. Exception thrown to abort account delete.");
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(DriveHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            Logger.getLogger(DriveHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectorIOException(ex);
        }
    }
    
    /**
     * development method 
     */
    private void printFiles(List<com.google.api.services.drive.model.File> files, String emailFrom){
        for(com.google.api.services.drive.model.File file : files){
            Logger.getLogger(DriveHelper.class.getName()).log(Level.INFO, "google file \"" + file.getName() + "\" listed by " + emailFrom);
        }
    }
    
    
    
    public static String findUserDriveInheritor(Uid uid, Directory.Users service, GoogleAppsConfiguration configuration){
        try {
            Directory.Users.Get request = service.get(uid.getUidValue()).setAlt("json").setProjection("full");
            User reqResult = request.execute();
            String driveInheritorAttribute = configuration.getDriveInheritorAttribute();
            String schemaName = driveInheritorAttribute.split(configuration.getCustomFieldDelimiter())[0];
            String attributeName = driveInheritorAttribute.split(configuration.getCustomFieldDelimiter())[1];
            if(reqResult != null){
                if(reqResult.getCustomSchemas() != null && !reqResult.getCustomSchemas().isEmpty()){
                    //TODO check of existence and correct data type and propper exceptions
                    return (String)reqResult.getCustomSchemas().get(schemaName).get(attributeName);
                }else{
                    return null;
                }
            }else{
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(GroupHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectorIOException(ex);
        }
    }

    private void transferFile(String emailFrom, String emailTo, com.google.api.services.drive.model.File googleFile, Drive service) throws IOException {
        try{
            Permissions.List request = service.permissions().list(googleFile.getId());
            About about = service.about().get().setFields("user").execute();
            String permissionId = about.getUser().getPermissionId();
            List<Permission> filePermissions = new ArrayList<Permission>();
            do {
                PermissionList permissions = request.execute();

                filePermissions.addAll(permissions.getPermissions());
                request.setPageToken(permissions.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() > 0);
            for(Permission permission : filePermissions){
                //TODO remove logger
                Logger.getLogger(DriveHelper.class.getName()).log(Level.INFO, "google file permission \"" + permission + "\" listed by " + emailFrom + " with permissionId: " + permissionId);
                //TODO make constants
                if(permission.getRole() != null && permission.getRole().equals("owner") && permission.getId() != null && permission.getId().equals(permissionId)){
                    Permission newOwnerPermission = new Permission();
                    newOwnerPermission.setRole("owner");
                    newOwnerPermission.setEmailAddress(emailTo);
                    newOwnerPermission.setType("user");
                    Permissions.Create create = service.permissions().create(googleFile.getId(), newOwnerPermission);
                    create.setTransferOwnership(true);
                    create.execute();
                }
            }
        }catch(GoogleJsonResponseException gjex){
            if(isGjexOk(gjex, emailTo)){
                Logger.getLogger(GroupHandler.class.getName()).log(Level.WARNING, null, gjex);
            }else{
                Logger.getLogger(DriveHelper.class.getName()).log(Level.SEVERE, null, gjex);
                throw new RuntimeException(gjex);
            }
        }
    }
    
    private boolean isGjexOk(GoogleJsonResponseException gjex, String emailTo){
        int errorCode = gjex.getDetails().getCode();
        String message = gjex.getDetails().getMessage();
        if(errorCode == 400 && message != null && message.contains(emailTo)){
            //error probably just says that it cannot send emails because user is disabled which is ok
            return true;
        }else{
            return false;
        }
    }
}
