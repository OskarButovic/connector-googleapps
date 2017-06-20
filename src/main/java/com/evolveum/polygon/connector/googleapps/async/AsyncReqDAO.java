/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.async;

import com.evolveum.polygon.connector.googleapps.GoogleAppsConfiguration;
import org.identityconnectors.framework.common.objects.Uid;

/**
 *
 * @author oskar.butovic
 */
public interface AsyncReqDAO {
    
    public void init(GoogleAppsConfiguration configuration);
    
    public String findRequestId(Uid uid);
    
    public void addRequestId(Uid uid, String requestId);
    
    public void deleteRequestId(Uid uid);
    
}
