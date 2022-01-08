package com.example.chatapplication.classes;

import com.akexorcist.localizationactivity.ui.LocalizationApplication;
import java.util.Locale;

public class RootApplication extends LocalizationApplication {


    private static RootApplication instance;
    private  SharedPManger sharedPManger;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        sharedPManger = new SharedPManger(instance);
    }

    public static RootApplication getInstance() {
        return instance;
    }

    public  SharedPManger getSharedPManger() {
        return sharedPManger;
    }

    @Override
    public Locale getDefaultLanguage() {
        return Locale.ENGLISH;
    }
}