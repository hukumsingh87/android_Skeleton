package me.shkschneider.skeleton;

import android.app.Application;
import android.content.Context;

import me.shkschneider.skeleton.helper.LogHelper;

public class SkeletonApplication extends Application {

    public static Boolean DEBUG = false;
    public static Context CONTEXT = null;

    @Override
    public void onCreate() {
        super.onCreate();

        CONTEXT = getApplicationContext();

        LogHelper.verbose("Hello, world!");
    }

}
