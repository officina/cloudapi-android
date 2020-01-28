/*
 * Copyright (C) 2019 Umberto Marini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.officina.cloudapilib;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created on 18/04/16.
 *
 * @author Umberto Marini
 */
public class CloudApiPreferencesManager {

    private static SharedPreferences sPrefs;

    private CloudApiPreferencesManager() {
        //empty private coordinator to hide the implicit public one
    }

    public static void init(Context context) {
        sPrefs = context.getSharedPreferences(context.getPackageName() + "_cloudapi", Context.MODE_PRIVATE);
    }

    /**
     * This class stores all the possible preferences' keys.
     */
    private static final class PrefsKeys {

        public static final String HOST_NAME = "hostName";
        public static final String TOKEN = "token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String SCOPE = "scope";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String IS_AUTH = "isAuth";
        public static final String AUTH_TYPE = "auth_type";
        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";

        private PrefsKeys() {
            //empty private coordinator to hide the implicit public one
        }
    }

    public static String getHostName() {
        return sPrefs.getString(PrefsKeys.HOST_NAME, "");
    }

    public static void storeHostName(String value) {
        sPrefs.edit().putString(PrefsKeys.HOST_NAME, value).apply();
    }

    public static void removeHostName() {
        sPrefs.edit().remove(PrefsKeys.HOST_NAME).apply();
    }

    public static String getToken() {
        return sPrefs.getString(PrefsKeys.TOKEN, "");
    }

    public static void storeToken(String value) {
        sPrefs.edit().putString(PrefsKeys.TOKEN, value).apply();
    }

    public static void removeToken() {
        sPrefs.edit().remove(PrefsKeys.TOKEN).apply();
    }

    public static String getRefreshToken() {
        return sPrefs.getString(PrefsKeys.REFRESH_TOKEN, "");
    }

    public static void storeRefreshToken(String value) {
        sPrefs.edit().putString(PrefsKeys.REFRESH_TOKEN, value).apply();
    }

    public static void removeRefreshToken() {
        sPrefs.edit().remove(PrefsKeys.REFRESH_TOKEN).apply();
    }

    public static String getScope() {
        return sPrefs.getString(PrefsKeys.SCOPE, "");
    }

    public static void storeScope(String value) {
        sPrefs.edit().putString(PrefsKeys.SCOPE, value).apply();
    }

    public static void removeScope() {
        sPrefs.edit().remove(PrefsKeys.SCOPE).apply();
    }

    public static String getClientId() {
        return sPrefs.getString(PrefsKeys.CLIENT_ID, "");
    }

    public static void storeClientId(String value) {
        sPrefs.edit().putString(PrefsKeys.CLIENT_ID, value).apply();
    }

    public static void removeClientId() {
        sPrefs.edit().remove(PrefsKeys.CLIENT_ID).apply();
    }

    public static String getClientSecret() {
        return sPrefs.getString(PrefsKeys.CLIENT_SECRET, "");
    }

    public static void storeClientSecret(String value) {
        sPrefs.edit().putString(PrefsKeys.CLIENT_SECRET, value).apply();
    }

    public static void removeClientSecret() {
        sPrefs.edit().remove(PrefsKeys.CLIENT_SECRET).apply();
    }

    public static boolean getIsAuth() {
        return sPrefs.getBoolean(PrefsKeys.IS_AUTH, false);
    }

    public static void storeIsAuth(boolean value) {
        sPrefs.edit().putBoolean(PrefsKeys.IS_AUTH, value).apply();
    }

    public static void removeIsAuth() {
        sPrefs.edit().remove(PrefsKeys.IS_AUTH).apply();
    }

    public static String getAuthType() {
        return sPrefs.getString(PrefsKeys.AUTH_TYPE, "");
    }

    public static void storeAuthType(String value) {
        sPrefs.edit().putString(PrefsKeys.AUTH_TYPE, value).apply();
    }

    public static void removeAuthType() {
        sPrefs.edit().remove(PrefsKeys.AUTH_TYPE).apply();
    }

    public static String getUsername() {
        return sPrefs.getString(PrefsKeys.LOGIN, "");
    }

    public static void storeUsername(String value) {
        sPrefs.edit().putString(PrefsKeys.LOGIN, value).apply();
    }

    public static void removeUsername() {
        sPrefs.edit().remove(PrefsKeys.LOGIN).apply();
    }

    public static String getPassword() {
        return sPrefs.getString(PrefsKeys.PASSWORD, "");
    }

    public static void storePassword(String value) {
        sPrefs.edit().putString(PrefsKeys.PASSWORD, value).apply();
    }

    public static void removePassword() {
        sPrefs.edit().remove(PrefsKeys.PASSWORD).apply();
    }

    public static void clear() {
        removeAuthType();
        removeClientId();
        removeClientSecret();
        removeHostName();
        removeIsAuth();
        removePassword();
        removeRefreshToken();
        removeScope();
        removeToken();
        removeUsername();
    }
}
