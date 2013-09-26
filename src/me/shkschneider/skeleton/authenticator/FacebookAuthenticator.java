/**
 * Copyright 2013 ShkSchneider
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.shkschneider.skeleton.authenticator;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AbstractAjaxCallback;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.apache.http.HttpStatus;

import me.shkschneider.skeleton.helper.LogHelper;

@SuppressWarnings("unused")
public class FacebookAuthenticator {

    public static final String GRAPH_ME_URL = "https://graph.facebook.com/me/feed";
    public static final String PERMISSION_BASIC_INFO = "basic_info";
    public static final String PERMISSION_READ_STREAM = "read_stream";
    public static final String PERMISSION_READ_FRIENDLISTS = "read_friendlists";
    public static final String PERMISSION_MANAGE_FRIENDLISTS = "manage_friendlists";
    public static final String PERMISSION_MANAGE_NOTIFICATIONS = "manage_notifications";
    public static final String PERMISSION_PUBLISH_STREAM = "publish_stream";
    public static final String PERMISSION_PUBLISH_CHECKINS = "publish_checkins";
    public static final String PERMISSION_OFFLINE_ACCESS = "offline_access";
    public static final String PERMISSION_USER_PHOTOS = "user_photos";
    public static final String PERMISSION_USER_LIKES = "user_likes";
    public static final String PERMISSION_USER_GROUPS = "user_groups";
    public static final String PERMISSION_FRIENDS_PHOTOS = "friends_photos";

    protected String mAppId;
    protected Integer mRequestCode;
    protected AQuery mAQuery;
    protected FacebookHandle mHandle;

    public FacebookAuthenticator(final Context context, final String appId, final Integer requestCode) {
        if (context != null) {
            if (! TextUtils.isEmpty(appId)) {
                mAppId = appId;
                mRequestCode = requestCode;
                mAQuery = new AQuery(context);
            }
            else {
                LogHelper.w("AppId was NULL");
            }
        }
        else {
            LogHelper.w("Context was NULL");
        }
    }

    public void auth(final Activity activity, final FacebookAuthenticatorCallback facebookAuthenticatorCallback, final String permissions) {
        mHandle = new FacebookHandle(activity, mAppId, permissions) {

            @Override
            public boolean expired(final AbstractAjaxCallback<?, ?> callback, final AjaxStatus status) {
                if (status.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                    return true;
                }
                return super.expired(callback, status);
            }

        };
        mHandle.sso(mRequestCode);

        if (mAQuery != null) {
            mAQuery.auth(mHandle).ajax(GRAPH_ME_URL, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(final String url, final String object, final AjaxStatus status) {
                    super.callback(url, object, status);

                    if (TextUtils.isEmpty(status.getError()) && ! status.getMessage().equalsIgnoreCase("cancel")) {
                        final String token = mHandle.getToken();
                        if (! TextUtils.isEmpty(token)) {
                            LogHelper.d("Token: " + token);
                            facebookAuthenticatorCallback.facebookAuthenticatorCallback(token);
                        }
                        else {
                            LogHelper.w("Token is NULL");
                            if (facebookAuthenticatorCallback != null) {
                                facebookAuthenticatorCallback.facebookAuthenticatorCallback(null);
                            }
                        }
                    }
                    else {
                        LogHelper.w("Message: " + status.getMessage());
                        LogHelper.w("Error: " + status.getError());
                        if (facebookAuthenticatorCallback != null) {
                            facebookAuthenticatorCallback.facebookAuthenticatorCallback(null);
                        }
                    }
                }

            });
        }
        else {
            LogHelper.w("AQuery was NULL");
            if (facebookAuthenticatorCallback != null) {
                facebookAuthenticatorCallback.facebookAuthenticatorCallback(null);
            }
        }
    }

    public void unauth() {
        if (mHandle != null) {
            if (! TextUtils.isEmpty(mHandle.getToken())) {
                mHandle.unauth();
            }
            else {
                LogHelper.w("Token was NULL");
            }
        }
        else {
            LogHelper.w("Handle was NULL");
        }
    }

    public String getToken() {
        if (mHandle != null) {
            return mHandle.getToken();
        }
        else {
            LogHelper.w("Handle was NULL");
        }
        return null;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final android.content.Intent data) {
        if (requestCode == mRequestCode) {
            if (mHandle != null) {
                mHandle.onActivityResult(requestCode, resultCode, data);
            }
            else {
                LogHelper.w("Handle was NULL");
            }
        }
    }

    public void onDestroy() {
        if (mAQuery != null) {
            mAQuery.dismiss();
        }
        else {
            LogHelper.w("AQuery was NULL");
        }
    }

    public static interface FacebookAuthenticatorCallback {

        public void facebookAuthenticatorCallback(final String token);

    }

}