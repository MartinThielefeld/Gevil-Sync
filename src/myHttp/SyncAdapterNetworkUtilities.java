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

package myHttp;

import syncZustand.TypeBiMap;
import android.content.Context;
import android.os.Handler;
import com.gevil.authenticator.AuthenticatorActivity;


public class SyncAdapterNetworkUtilities {    
    

	
    /**
     * Configures the httpClient to connect to the URL provided.
     */
   /* public static void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params,
                REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }*/



    /**
     * Connects to the Voiper server, authenticates the provided username and
     * password.
     * 
     * @param username Citadel o.a. Benutzername
     * @param password Citadel o.a. Passwort
     * @param handler handler der aufrufenden Activity, an die das Ergebnis zurück gegeben wird.
     * @param context Gevil
     * @return boolean Ergebnis, ob das Passwort funktioniert hat.
     */
    public static boolean authenticate(String username, String password,
        Handler handler, final Context context) {   
    	    	
    	
		WebDAV wd = new WebDAV(username, password, null);
    	if( wd.authenticate(username, password, context)){
    		sendResult(true, handler, context);
    		return true;
    	}else{
    		sendResult(false, handler, context);
    		return false;
    	}
    }

    /**
     * Schickt das Authentifizierungsergebnis zurück an die Login-Activity.
     * Nutzt dazu deren handler.
     * 
     * @param result Ob die Authentifizierung erfolgreich war
     * @param handler Auth. Activity handler
     * @param context Gevil
     */
    private static void sendResult(final Boolean result, final Handler handler,
        final Context context) {
        if (handler == null || context == null) {
            return;
        }
        handler.post(new Runnable() {
            public void run() {
                ((AuthenticatorActivity) context).onAuthenticationResult(result);
            }
        });
    }
    
    
    
    
    /**
     * Ausführung als separater Thread.
     * 
     * @param runnable 
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Versucht die Benutzerdaten auf dem Server zu authentifizieren.
     * 
     * @param username Benutzername
     * @param password Passwort das authentifiziert werden soll
     * @param handler handler der Login-Activity
     * @param context Gevil
     * @return Thread Thread auf dem die Operationen ausgeführt werden
     */
    public static Thread attemptAuth(final String username,
        final String password, final Handler handler, final Context context) {
    	// diese Methode wird benötigt, dass die Authentication funktioniert.
        final Runnable runnable = new Runnable() {
            public void run() {
                authenticate(username, password, handler, context);
            }
        };
        
        return SyncAdapterNetworkUtilities.performOnBackgroundThread(runnable);
    }

    
}
