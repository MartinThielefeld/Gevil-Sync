/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gevil.syncadapter;

public class Constants {

    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE =
    	"com.Gevil";

    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE =
    	"com.Gevil";
    
    /**
     * DisplayName für Gevil-Sync Kalendereinträge
     */
    
    public static final String ACCOUNT_DISPLAY_NAME = "Gevil-Sync Kalender";
    
    /**
     * MIMETYPE, bei dem unter columnData3 die UID gespeichert wird.
     */
    public static final String MIMETYPE_UID = "vnd.android.cursor.item/com.gevil.profile";
    
    /*****************************************************
     *  Adresse von Citadel und Calendar sowie Contacts  *
     *****************************************************
     */
    
    /**
     * Adresse des, in diesem Fall Citadel Server
     */
    //public static final String SERVER_URL = "http://192.168.239.128/";
    public static final String SERVER_URL = "http://192.168.2.103/";
    
    /*
     * Adresse des Kalenderraums
     */
    //public static final String CALENDAR_SERVER_URL = SERVER_URL + "groupdav/Calendar";
    
    
    /*
     * Adresse des Kontakte Raums
     */
   // public static final String CONTACTS_SERVER_URL = SERVER_URL + "groupdav/Contacts";

}
