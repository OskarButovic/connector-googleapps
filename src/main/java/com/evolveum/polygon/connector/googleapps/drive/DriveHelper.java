/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.drive;

import com.evolveum.polygon.connector.googleapps.GoogleAppsConfiguration;
import com.evolveum.polygon.connector.googleapps.Main;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.FileList;
//import com.google.api.services.drive.model.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oskar.butovic
 */
public class DriveHelper {
    
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
                    .setServiceAccountScopes(Main.SCOPES)
                    .setServiceAccountUser(emailFrom)
                    .build();
            Drive service = new Drive.Builder(httpTransport, JSON_FACTORY, credential).build();
            List<com.google.api.services.drive.model.File> result = new ArrayList<com.google.api.services.drive.model.File>();
            Files.List request = service.files().list();
            do {
                FileList files = request.execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() > 0);
            printFiles(result);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(DriveHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            Logger.getLogger(DriveHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * development method 
     */
    private void printFiles(List<com.google.api.services.drive.model.File> files){
        for(com.google.api.services.drive.model.File file : files){
            Logger.getLogger(DriveHelper.class.getName()).log(Level.INFO, file.getName());
        }
    }
    
}
