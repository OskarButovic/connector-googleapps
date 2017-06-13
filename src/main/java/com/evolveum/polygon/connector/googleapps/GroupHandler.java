/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.evolveum.polygon.connector.googleapps;

import static com.evolveum.polygon.connector.googleapps.GoogleAppsConnector.*;

import java.io.IOException;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.AttributesAccessor;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.PredefinedAttributeInfos;
import org.identityconnectors.framework.common.objects.PredefinedAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AndFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterVisitor;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.NotFilter;
import org.identityconnectors.framework.common.objects.filter.OrFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;

import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.groupssettings.Groupssettings;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;

/**
 * A GroupHandler is a util class to cover all Group related operations.
 * 
 * @author Laszlo Hordos
 */
public class GroupHandler implements FilterVisitor<Void, Directory.Groups.List> {

    /**
     * Setup logging for the {@link GroupHandler}.
     */
    private static final Log logger = Log.getLog(GroupHandler.class);

    public Void visitAndFilter(Directory.Groups.List list, AndFilter andFilter) {
        throw getException();
    }

    public Void visitContainsFilter(Directory.Groups.List list, ContainsFilter containsFilter) {
        if (containsFilter.getAttribute().is(MEMBERS_ATTR)) {
            list.setUserKey(containsFilter.getValue());
        } else {
            throw getException();
        }
        return null;
    }

    public Void visitContainsAllValuesFilter(Directory.Groups.List list,
            ContainsAllValuesFilter containsAllValuesFilter) {
        //TODO needed for removing deleted users from groups
        throw getException();
    }

    protected RuntimeException getException() {
        return new UnsupportedOperationException(
                "Only EqualsFilter(['domain','customer','userKey']) and ContainsFilter('members') are supported");
    }
    
    protected RuntimeException getException(EqualsFilter equalsFilter) {
        return new UnsupportedOperationException(
                "filter is:" + equalsFilter + "Only EqualsFilter(['domain','customer','userKey']) and ContainsFilter('members') are supported");
    }

    public Void visitEqualsFilter(Directory.Groups.List list, EqualsFilter equalsFilter) {
        if (equalsFilter.getAttribute().is("customer")) {
            if (null != list.getDomain() || null != list.getUserKey()) {
                throw new InvalidAttributeValueException(
                        "The 'customer', 'domain' and 'userKey' can not be in the same query");
            } else {
                list.setCustomer(AttributeUtil.getStringValue(equalsFilter.getAttribute()));
            }
        } else if (equalsFilter.getAttribute().is("domain")) {
            if (null != list.getCustomer() || null != list.getUserKey()) {
                throw new InvalidAttributeValueException(
                        "The 'customer', 'domain' and 'userKey' can not be in the same query");
            } else {
                list.setDomain(AttributeUtil.getStringValue(equalsFilter.getAttribute()));
            }
        } else if (equalsFilter.getAttribute().is("userKey")) {
            if (null != list.getDomain() || null != list.getCustomer()) {
                throw new InvalidAttributeValueException(
                        "The 'customer', 'domain' and 'userKey' can not be in the same query");
            } else {
                list.setUserKey(AttributeUtil.getStringValue(equalsFilter.getAttribute()));
            }
        } else {
            throw getException(equalsFilter) ; 
        }

        return null;
    }

    public Void visitExtendedFilter(Directory.Groups.List list, Filter filter) {
        throw getException();
    }

    public Void visitGreaterThanFilter(Directory.Groups.List list,
            GreaterThanFilter greaterThanFilter) {
        throw getException();
    }

    public Void visitGreaterThanOrEqualFilter(Directory.Groups.List list,
            GreaterThanOrEqualFilter greaterThanOrEqualFilter) {
        throw getException();
    }

    public Void visitLessThanFilter(Directory.Groups.List list, LessThanFilter lessThanFilter) {
        throw getException();
    }

    public Void visitLessThanOrEqualFilter(Directory.Groups.List list,
            LessThanOrEqualFilter lessThanOrEqualFilter) {
        throw getException();
    }

    public Void visitNotFilter(Directory.Groups.List list, NotFilter notFilter) {
        throw getException();
    }

    public Void visitOrFilter(Directory.Groups.List list, OrFilter orFilter) {
        throw getException();
    }

    public Void visitStartsWithFilter(Directory.Groups.List list, StartsWithFilter startsWithFilter) {
        throw getException();
    }

    public Void visitEndsWithFilter(Directory.Groups.List list, EndsWithFilter endsWithFilter) {
        throw getException();
    }

    // /////////////
    //
    // GROUP
    //
    // /////////////

    public static ObjectClassInfo getGroupClassInfo() {
        // @formatter:off
            /* GROUP from https://devsite.googleplex.com/admin-sdk/directory/v1/reference/groups#resource
            {
              "kind": "admin#directory#group",
              "id": string,
              "etag": etag,
              "email": string,
              "name": string,
              "directMembersCount": long,
              "description": string,
              "adminCreated": boolean,
              "aliases": [
                string
              ],
              "nonEditableAliases": [
                string
              ]
            }
            */
        // @formatter:on
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(ObjectClass.GROUP_NAME);
        // email
        builder.addAttributeInfo(Name.INFO);
        builder.addAttributeInfo(AttributeInfoBuilder.build(NAME_ATTR));
        builder.addAttributeInfo(PredefinedAttributeInfos.DESCRIPTION);

        // Read-only
        builder.addAttributeInfo(AttributeInfoBuilder.define(ADMIN_CREATED_ATTR, Boolean.TYPE)
                .setUpdateable(false).setCreateable(false).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ALIASES_ATTR).setUpdateable(false)
                .setCreateable(false).setMultiValued(true).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(NON_EDITABLE_ALIASES_ATTR)
                .setUpdateable(false).setCreateable(false).setMultiValued(true).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(DIRECT_MEMBERS_COUNT_ATTR, Long.TYPE)
                .setUpdateable(false).setCreateable(false).build());

        // Virtual Attribute
        builder.addAttributeInfo(AttributeInfoBuilder.define(MEMBERS_ATTR).setMultiValued(true)
                .setReturnedByDefault(false).build());
        
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_allowExternalMembers).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_allowGoogleCommunication).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_allowWebPosting).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_archiveOnly).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_customFooterText).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_customReplyTo).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_defaultMessageDenyNotificationText).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_description).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_email).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_includeCustomFooter).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_includeInGlobalAddressList).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_isArchived).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_maxMessageBytes, Integer.TYPE).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_membersCanPostAsTheGroup).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_messageDisplayFont).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_messageModerationLevel).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_name).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_replyTo).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_sendMessageDenyNotification).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_showInGroupDirectory).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_spamModerationLevel).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanAdd).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanContactOwner).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanInvite).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanJoin).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanLeaveGroup).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanPostMessage).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanViewGroup).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(GoogleAppsConnector.SETTING_whoCanViewMembership).build());

        return builder.build();
    }

    public static ObjectClassInfo getMemberClassInfo() {
        // @formatter:off
            /*
            {
			}
            */
        // @formatter:on
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(MEMBER.getObjectClassValue());
        builder.addAttributeInfo(AttributeInfoBuilder.define(Name.NAME).setUpdateable(false)
                .setCreateable(false)/* .setRequired(true) */.build());

        // optional
        builder.addAttributeInfo(AttributeInfoBuilder.define(GROUP_KEY_ATTR).setUpdateable(false)
        /* .setCreateable(false) */.setRequired(true).build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(EMAIL_ATTR).setUpdateable(false)
        /* .setCreateable(false) */.setRequired(true).build());

        builder.addAttributeInfo(AttributeInfoBuilder.build(ROLE_ATTR));
        builder.addAttributeInfo(AttributeInfoBuilder.define(TYPE_ATTR).setUpdateable(false)
                .setCreateable(false).build());

        return builder.build();
    }

    // https://support.google.com/a/answer/33386
    public static Directory.Groups.Insert createGroup(Directory.Groups groups,
            AttributesAccessor attributes) {
        Group group = new Group();
        group.setEmail(GoogleAppsUtil.getName(attributes.getName()));
        // Optional
        group.setDescription(attributes.findString(PredefinedAttributes.DESCRIPTION));
        group.setName(attributes.findString(NAME_ATTR));

        try {
            return groups.insert(group).setFields(ID_ETAG);
            // } catch (HttpResponseException e){
        } catch (IOException e) {
            logger.warn(e, "Failed to initialize Groups#Insert");
            throw ConnectorException.wrap(e);
        }
    }

    public static Directory.Groups.Patch updateGroup(Directory.Groups groups, String groupKey,
            AttributesAccessor attributes) {
        Group group = null;

        Name email = attributes.getName();
        if (email != null) {
            String stringValue = GoogleAppsUtil.getStringValueWithDefault(email, null);
            if (null != stringValue) {
                if (StringUtil.isBlank(stringValue)) {
                    throw new InvalidAttributeValueException(
                            "Invalid attribute '__NAME__'. The group's email address. Can not be blank when updating a group.");
                }
                group = new Group();
                group.setEmail(stringValue);
            }
        }

        Attribute description = attributes.find(PredefinedAttributes.DESCRIPTION);
        if (null != description) {
            String stringValue = GoogleAppsUtil.getStringValueWithDefault(description, null);
            if (null != stringValue) {
                if (null == group) {
                    group = new Group();
                }
                group.setDescription(stringValue);
            }
        }
        Attribute name = attributes.find(NAME_ATTR);
        if (null != name) {
            String stringValue = GoogleAppsUtil.getStringValueWithDefault(name, null);
            if (null != stringValue) {
                if (null == group) {
                    group = new Group();
                }
                group.setName(stringValue);
            }
        }

        if (null == group) {
            return null;
        }
        try {
            return groups.patch(groupKey, group).setFields(ID_ETAG);
            // } catch (HttpResponseException e){
        } catch (IOException e) {
            logger.warn(e, "Failed to initialize Groups#Patch");
            throw ConnectorException.wrap(e);
        }
    }
    
    public static Groupssettings.Groups.Patch updateGroupSettings(Groupssettings.Groups service, AttributesAccessor attributes, Uid uid, Directory.Groups dirService) {
        com.google.api.services.groupssettings.model.Groups groupSettings = null;
        
        String email = findGroupEmail(uid, dirService);
        if(email == null){
            logger.warn("not found email address for group with uid: " + uid.getUidValue());
            return null;
        }
        
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_allowExternalMembers);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_allowWebPosting);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_archiveOnly);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_customFooterText);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_customReplyTo);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_defaultMessageDenyNotificationText);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_description);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_email);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_includeInGlobalAddressList);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_isArchived);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_maxMessageBytes);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_membersCanPostAsTheGroup);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_messageDisplayFont);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_messageModerationLevel);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_name);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_replyTo);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_sendMessageDenyNotification);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_showInGroupDirectory);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_spamModerationLevel);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanAdd);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanContactOwner);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanInvite);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanJoin);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanLeaveGroup);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanPostMessage);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanViewGroup);
        groupSettings = setAttribute(groupSettings, attributes, GoogleAppsConnector.SETTING_whoCanViewMembership);
        
        if (null == groupSettings) {
            return null;
        }
        try {
            return service.patch(email, groupSettings);
            // } catch (HttpResponseException e){
        } catch (IOException e) {
            logger.warn(e, "Failed to initialize Groups#Patch");
            throw ConnectorException.wrap(e);
        }
    }
    
    public static com.google.api.services.groupssettings.model.Groups setAttribute(com.google.api.services.groupssettings.model.Groups groupSettings, AttributesAccessor attributes, String attributeName){
        Attribute attributeValue = attributes.find(attributeName);
        attributeName = attributeName.replaceAll("setting_", "");//hack for duplicately named attributes
        if (null != attributeValue) {
            String stringValue = GoogleAppsUtil.getStringValueWithDefault(attributeValue, null);
            if (null != stringValue) {
                try {
                    if (null == groupSettings) {
                        groupSettings = new com.google.api.services.groupssettings.model.Groups();
                    }
                    //TODO reflexi
                    BeanUtils.setProperty(groupSettings, attributeName, stringValue);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(GroupHandler.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(GroupHandler.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }
        }
        return groupSettings;
    }
    
    public static String findGroupEmail(Uid uid, Directory.Groups service){
        try {
            Directory.Groups.Get request = service.get(uid.getUidValue()).setAlt("json");
            Group reqResult = request.execute();
            if(reqResult != null){
                return reqResult.getEmail();
            }else{
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(GroupHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectorIOException(ex);
        }
    }

    public static Directory.Members.Insert createMember(Directory.Members service,
            AttributesAccessor attributes) {
        String groupKey = attributes.findString(GROUP_KEY_ATTR);
        if (StringUtil.isBlank(groupKey)) {
            throw new InvalidAttributeValueException(
                    "Missing required attribute 'groupKey'. Identifies the group in the API request. Required when creating a Member.");
        }

        String memberKey = attributes.findString(EMAIL_ATTR);
        if (StringUtil.isBlank(memberKey)) {
            throw new InvalidAttributeValueException(
                    "Missing required attribute 'memberKey'. Identifies the group member in the API request. Required when creating a Member.");
        }
        String role = attributes.findString(ROLE_ATTR);

        return createMember(service, groupKey, memberKey, role);
    }

    public static Directory.Members.Insert createMember(Directory.Members service, String groupKey,
            String memberKey, String role) {

        Member content = new Member();
        content.setEmail(memberKey);
        if (StringUtil.isBlank(role)) {
            content.setRole("MEMBER");
        } else {
            // OWNER. MANAGER. MEMBER.
            content.setRole(role);
        }
        try {
            return service.insert(groupKey, content).setFields(EMAIL_ETAG);
            // } catch (HttpResponseException e){
        } catch (IOException e) {
            logger.warn(e, "Failed to initialize Members#Insert");
            throw ConnectorException.wrap(e);
        }
    }

    public static Directory.Members.Patch updateMembers(Directory.Members service, String groupKey,
            String memberKey, String role) {
        Member content = new Member();
        content.setEmail(memberKey);

        if (StringUtil.isBlank(role)) {
            content.setRole("MEMBER");
        } else {
            // OWNER. MANAGER. MEMBER.
            content.setRole(role);
        }
        try {
            return service.patch(groupKey, memberKey, content).setFields(EMAIL_ETAG);
            // } catch (HttpResponseException e){
        } catch (IOException e) {
            logger.warn(e, "Failed to initialize Members#Insert");
            throw ConnectorException.wrap(e);
        }
    }

    public static Directory.Members.Delete deleteMembers(Directory.Members service,
            String groupKey, String memberKey) {
        try {
            return service.delete(groupKey, memberKey);
            // } catch (HttpResponseException e){
        } catch (IOException e) {
            logger.warn(e, "Failed to initialize Members#Delete");
            throw ConnectorException.wrap(e);
        }
    }

    public static ConnectorObject fromMember(String groupKey, Member content) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setObjectClass(MEMBER);

        Uid uid = generateMemberId(groupKey, content);
        builder.setUid(uid);
        builder.setName(uid.getUidValue());

        builder.addAttribute(AttributeBuilder.build(GROUP_KEY_ATTR, content.getId()));
        builder.addAttribute(AttributeBuilder.build(EMAIL_ATTR, content.getEmail()));
        builder.addAttribute(AttributeBuilder.build(ROLE_ATTR, content.getRole()));
        builder.addAttribute(AttributeBuilder.build(TYPE_ATTR, content.getType()));

        return builder.build();
    }

    public static Uid generateMemberId(String groupKey, Member content) {
        Uid uid = null;
        String memberName = groupKey + '/' + content.getEmail();

        if (null != content.getEtag()) {
            uid = new Uid(memberName, content.getEtag());
        } else {
            uid = new Uid(memberName);
        }
        return uid;
    }
}
