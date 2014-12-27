package com.cellasoft.univrapp;

import com.github.droidfu.DroidFuApplication;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.util.Stack;

@ReportsCrashes(
        formUri = Config.Links.ACRA,
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = Config.ACRA_USER,
        formUriBasicAuthPassword = Config.ACRA_PASS,
        formKey = Config.ACRA_KEY, // This is required for backward compatibility but not used
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE,
                ReportField.EVENTSLOG,
                ReportField.LOGCAT
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
public class Application extends DroidFuApplication {

    public static Stack<Class<?>> parents = new Stack<Class<?>>();
    private static Application instance;

    public Application() {
        instance = this;
    }

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //  if (!BuildConfig.DEBUG) {
        ACRA.init(this);
        //  }

        initImageLoader();

        //FlatUI.setDefaultTheme(FlatUI.DEEP);
    }

    private void initImageLoader() {
        BitmapDisplayer displayer = new FadeInBitmapDisplayer(300);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageOnLoading(R.drawable.user)
                .showImageForEmptyUri(R.drawable.user)
                .showImageOnFail(R.drawable.user).resetViewBeforeLoading(false)
                .displayer(displayer).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .diskCacheExtraOptions(240, 240, null)
                .threadPriority(Thread.NORM_PRIORITY - 2).threadPoolSize(4)
                .defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        ImageLoader.getInstance().clearMemoryCache();
        System.gc();
    }
}
