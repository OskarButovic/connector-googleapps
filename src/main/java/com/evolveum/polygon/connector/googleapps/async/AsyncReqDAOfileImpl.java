/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.async;

import com.evolveum.polygon.connector.googleapps.GoogleAppsConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.identityconnectors.framework.common.objects.Uid;

/**
 *
 * @author oskar.butovic
 */
public class AsyncReqDAOfileImpl implements AsyncReqDAO{
    
    Map<String, String> requestsCache;
    File asincReqFile;

    @Override
    public void init(GoogleAppsConfiguration configuration) {
        String filename = configuration.getAsyncReqFile();
        if(filename == null || filename.isEmpty()){
            return;
        }
        asincReqFile = new File(filename);
        try {
            if(asincReqFile.exists()){
                loadFile(asincReqFile);
            }else{
                asincReqFile.createNewFile();
                loadFile(asincReqFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(AsyncReqDAOfileImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("google suite connector could not load async request file: " + filename);
        }
    }

    @Override
    public String findRequestId(Uid uid) {
        return requestsCache.get(uid.getUidValue());
    }

    @Override
    public void addRequestId(Uid uid, String requestId) {
        requestsCache.put(uid.getUidValue(), requestId);
        try {
            writeToFile(asincReqFile);
        } catch (IOException ex) {
            Logger.getLogger(AsyncReqDAOfileImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("google suite connector could not store to async request file: " + asincReqFile.getAbsolutePath());
        }
    }

    @Override
    public void deleteRequestId(Uid uid) {
        requestsCache.remove(uid.getUidValue());
        try {
            writeToFile(asincReqFile);
        } catch (IOException ex) {
            Logger.getLogger(AsyncReqDAOfileImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("google suite connector could not store to async request file: " + asincReqFile.getAbsolutePath());
        }
    }
    
    private void loadFile(File file) throws FileNotFoundException, IOException{
        requestsCache = new HashMap<>();
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        for (String key : properties.stringPropertyNames()) {
           requestsCache.put(key, properties.get(key).toString());
        }
    }
    
    private void writeToFile(File file) throws IOException{
        Properties properties = new Properties();

        for (Map.Entry<String,String> entry : requestsCache.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        properties.store(new FileOutputStream(file), null);
    }
    
}
