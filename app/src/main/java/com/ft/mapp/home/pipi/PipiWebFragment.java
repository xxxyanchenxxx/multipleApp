package com.ft.mapp.home.pipi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ft.mapp.R;
import com.ft.mapp.VApp;
import com.ft.mapp.home.activity.LoginActivity;
import com.ft.mapp.utils.MiitHelper;
import com.ft.mapp.utils.SystemUtils;
import com.luck.picture.lib.tools.SPUtils;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.xqb.user.net.engine.ApiServiceDelegate;
import com.xqb.user.net.engine.UserAgent;
import com.xqb.user.net.lisenter.ApiCallback;

import org.jetbrains.annotations.NotNull;

import jonathanfinerty.once.Once;

import static android.content.Context.TELEPHONY_SERVICE;

public class PipiWebFragment extends Fragment {
    private static final int REQUEST_CODE = 0;//?????????
    //?????????????????????????????????
    private static final String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET};
    private AlertDialog mPermissionDialog;
    private String imei;
    private String meid = "";
    private String oaid = "";
    private String androidid = "";
    private String downUrlLocal = "";
    private String packagenameLocal = "";
    private WebView webView, webview_top;
    private SwipeRefreshLayout swipeLayout;
    private TextView tv_start_download, tv_show_time;
    private TouchGroup ll;
    TextView test;
    private List<String> mPermissionList = new ArrayList<>();
    private String pid = "60001";
    private String key = "glTzruIf23BErEtZmQq1jlU7fwZIRRvA";
    public static String AUTHORITY;

    private boolean firstLoad = true;
    private String OAID = "";
    private String keycode;
    private String userid;
    private String globalUrl = "";
    private Button btnLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(requireContext()).inflate(R.layout.fragment_pipi_web, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        test = view.findViewById(R.id.test);
        webView = view.findViewById(R.id.webview);
        webview_top = view.findViewById(R.id.webview_top);
        swipeLayout = view.findViewById(R.id.swipe_container);
        tv_start_download = view.findViewById(R.id.tv_start_download);
        tv_show_time = view.findViewById(R.id.tv_show_time);
        btnLogin = view.findViewById(R.id.pipi_web_btn_login);
        btnLogin.setOnClickListener(v -> LoginActivity.start(requireActivity()));
        ll = view.findViewById(R.id.ll);
//        swipeLayout.setEnabled(false);
        AUTHORITY = getPackageName(requireContext()) + ".provider";
        //set webView
        initWebView();
        //?????????
//        showBackBtn();
        //Android6.0????????????????????????
//        initPermission();

        //????????????
        swipeLayout.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                //??????????????????
                webView.loadUrl(webView.getUrl());
            }
        });
        //????????????????????????
        tv_start_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //????????????
                if (TextUtils.isEmpty(downUrlLocal)) {
                    Toast.makeText(requireActivity(), "??????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                //??????????????????
                final boolean isInstalled = SystemUtils.isAppInstalled(requireActivity(), packagenameLocal);
                if (isInstalled) {
                    doStartApplicationWithPackageName(packagenameLocal);
                    return;
                }
                //????????????
                int last = downUrlLocal.lastIndexOf("/") + 1;
                String apkName = downUrlLocal.substring(last);
                if (!apkName.contains(".apk")) {
                    if (apkName.length() > 10) {
                        apkName = apkName.substring(apkName.length() - 10);
                    }
                    apkName += ".apk";
                }
                String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "kuwanzhuan" + File.separator + apkName;
                downLoadApp(apkName, downloadPath, downUrlLocal, tv_start_download);

                //?????????????????????js??????
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:startDownApp()");
                    }
                });
            }
        });
    }


    @SuppressLint("JavascriptInterface")
    private void initWebView() {
        //??????WebSettings??????
        WebSettings webSettings = webView.getSettings();

        //??????????????????????????????Javascript????????????webview??????????????????Javascript
        webSettings.setJavaScriptEnabled(true);

        //????????????????????????????????????
        webSettings.setUseWideViewPort(true); //????????????????????????webview?????????
        webSettings.setLoadWithOverviewMode(true); // ????????????????????????
        //??????????????????
        webSettings.setCacheMode(WebSettings.LOAD_NORMAL); //??????webview?????????
        webSettings.setAllowFileAccess(true); //????????????????????????
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //????????????JS???????????????
        webSettings.setLoadsImagesAutomatically(true); //????????????????????????
        webSettings.setDefaultTextEncodingName("utf-8");//??????????????????
        webView.addJavascriptInterface(requireActivity(), "test");
        webSettings.setAllowFileAccess(true); // ??????????????????
        webSettings.setSupportZoom(true); // ????????????
        webSettings.setLoadWithOverviewMode(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webSettings.setAppCacheEnabled(true);
        webSettings.setSaveFormData(false);
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }
        webSettings.setUseWideViewPort(true); // ?????????
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //??????WebSettings??????
        WebSettings webSettingstop = webview_top.getSettings();

        //??????????????????????????????Javascript????????????webview??????????????????Javascript
        webSettingstop.setJavaScriptEnabled(true);

        //????????????????????????????????????
        webSettingstop.setUseWideViewPort(true); //????????????????????????webview?????????
        webSettingstop.setLoadWithOverviewMode(true); // ????????????????????????
        //??????????????????
        webSettingstop.setCacheMode(WebSettings.LOAD_NORMAL); //??????webview?????????
        webSettingstop.setAllowFileAccess(true); //????????????????????????
        webSettingstop.setJavaScriptCanOpenWindowsAutomatically(true); //????????????JS???????????????
        webSettingstop.setLoadsImagesAutomatically(true); //????????????????????????
        webSettingstop.setDefaultTextEncodingName("utf-8");//??????????????????
        webview_top.addJavascriptInterface(requireActivity(), "test");
        webSettingstop.setAllowFileAccess(true); // ??????????????????
        webSettingstop.setSupportZoom(true); // ????????????
        webSettingstop.setLoadWithOverviewMode(true);
        webview_top.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettingstop.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettingstop.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webSettingstop.setAppCacheEnabled(true);
        webSettingstop.setSaveFormData(false);
        webSettingstop.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webSettingstop.setLoadsImagesAutomatically(true);
        } else {
            webSettingstop.setLoadsImagesAutomatically(false);
        }
        webSettingstop.setUseWideViewPort(true); // ?????????
        webSettingstop.setAllowFileAccessFromFileURLs(false);
        webSettingstop.setAllowUniversalAccessFromFileURLs(false);
        webSettingstop.setJavaScriptCanOpenWindowsAutomatically(true);

        WebChromeClient webchromeclient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 15 && isCanLoadFiJs && !hadLoade15) {
                    webView.loadUrl("javascript:" + js);
                    hadLoade15 = true;
                }
                if (newProgress >= 45 && isCanLoadFiJs && !hadLoade45) {
                    webView.loadUrl("javascript:" + js);
                    hadLoade45 = true;
                }
                if (newProgress >= 75 && isCanLoadFiJs && !hadLoade75) {
                    webView.loadUrl("javascript:" + js);
                    hadLoade75 = true;
                }
                if (newProgress == 100) {
                    //???????????????
                    swipeLayout.setRefreshing(false);
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            int width = webView.getMeasuredWidth();
                            int height = webView.getMeasuredHeight();
                            webView.loadUrl("javascript:getMeasured(" + width + "," + height + ")");
                        }
                    });
                } else {
                    if (!swipeLayout.isRefreshing())
                        swipeLayout.setRefreshing(true);
                }

                super.onProgressChanged(view, newProgress);
            }

            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
                result.confirm();
                return true;
            }
        };


        ll.setClick(new TouchGroup.Click() {
            @Override
            public void click(float x, float y) {
                Log.i("webview", "" + x + "   " + y);
                taskClickTime++;
                webView.loadUrl("javascript:getClickLoact(" + (int) x + "," + (int) y + ")");
            }
        });
        webView.setWebChromeClient(webchromeclient);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                if (isCanLoadFiJs) {
                    webView.loadUrl("javascript:" + js);
                    isCanLoadFiJs = false;
                }
                swipeLayout.setRefreshing(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isCanLoadFiJs) {
                    if (!LyCheckClickUrlIsCanJump(url)) {
                        return true;
                    }
                }

//                if (url.contains("51gzdhh.xyz")) {
//                    swipeLayout.setEnabled(false);
//                } else {
//                    swipeLayout.setEnabled(false);
//                }
                if (shouldOverrideUrlLoadingByApp(view, url)) {
                    return true;
                }
                if (url.equals(globalUrl)) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                if (firstLoad) {
                    firstLoad = false;
                    return super.shouldOverrideUrlLoading(view, url);
                }
                AdWebActivity.start(requireActivity(), url);
                return true;
//                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView webView, int i, String s, String s1) {
                super.onReceivedError(webView, i, s, s1);
                Log.i("---------", "onReceivedError");
                firstLoad = true;
            }

        });
        openUrl();
    }

    /**
     * ?????????????????????
     */
    private void initPermission() {
        mPermissionList.clear();//??????????????????????????????????????????
        //??????????????????????????????????????????
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//??????????????????????????????mPermissionList???
            }
        }
        //????????????
        if (mPermissionList.size() > 0) {//????????????????????????????????????
            ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE);
        } else {
            //?????????????????????????????????????????????????????????
            TelephonyManager TelephonyMgr = (TelephonyManager) requireActivity().getSystemService(TELEPHONY_SERVICE);
            imei = TelephonyMgr.getDeviceId();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    meid = TelephonyMgr.getMeid();
                } catch (Exception e) {
                }
            }
            openUrl();
        }
    }

    private boolean shouldOverrideUrlLoadingByApp(WebView view, String url) {
        if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
            //?????????http, https, ftp?????????
            return false;
        }

        if (isSupportedDeepLink(url)) {
            boolean ret = openDeepLink(webView.getView().getContext(), url);
            return true;//????????????????????????
        }

        Intent intent;
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException e) {
            return false;
        }
        intent.setComponent(null);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            return true;
        }
        return true;
    }

    private static String[] deepLinkPrex = {
            "weixin://",
            "pinduoduo://",
            "openapp.jdmobile://",
            "market://",
            "taobao://",
            "alipay://",
            "market://"
    };

    public boolean isSupportedDeepLink(String url) {
        for (int i = 0; i < deepLinkPrex.length; i++) {
            if (url.startsWith(deepLinkPrex[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean openDeepLink(Context ctx, String url) {
        try {
            Intent intent1 = new Intent();
            intent1.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(url);
            intent1.setData(uri);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    float DownX = 0, DownY = 0;
    private boolean isCanLoadFiJs = false;
    private boolean hadLoade15 = false;
    private boolean hadLoade45 = false;
    private boolean hadLoade75 = false;
    private String js = "";

    private void openUrl() {
//        String userid = "123456";
//        imei= "";
//        meid = "";
//        oaid = "";
//        androidid = "";
//        //pid+imei+gtype+userid+key   ????????????
//        String keycode = "3" + imei+meid + "1" + userid + "adhefoiqijefijewfowef";
//        keycode = string2MD5(keycode).toLowerCase();
//        //???????????? ???????????????????????????????????????oaid ??? androidid
//        webView.loadUrl("http://kuwen10005.xyz/static/web/test/dist/index.html?userid=" + userid + "&imei=" + imei + "&gtype=1&pid=2&sign=" + keycode+"&meid="+meid+"&oaid="+"&androidid=");
////        Log.i("url:", "http://kuwen10005.xyz/static/test/web/dist/index.html?userid=" + userid + "&imei=" + imei + "&gtype=1&pid=2&sign=" + keycode+"&meid="+meid+"&oaid="+"&androidid=");

        if (UserAgent.getInstance(requireActivity()).getUserInfo() == null) {
            btnLogin.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
//            new ApiServiceDelegate(requireContext()).register("", "", new ApiCallback() {
//                @Override
//                public void onSuccess() {
//                    prepareUserInfo();
//                }
//
//                @Override
//                public void onFail(String errorMsg) {
//
//                }
//            });
            return;
        }
        btnLogin.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        prepareUserInfo();

    }

    private void prepareUserInfo() {
        userid = UserAgent.getInstance(requireActivity()).getUserInfo().userId;
        imei = "";
        meid = "";
        oaid = "";
        androidid = UserAgent.getInstance(requireContext()).getAndroidId();
        //pid+imei+gtype+userid+key   ????????????
        String keycodePre = pid + imei + meid + oaid + androidid + "1" + userid + key;
        keycode = string2MD5(keycodePre).toLowerCase();
        //???????????? ???????????????????????????????????????oaid ??? androidid
        try {
            // ???????????????????????????oaid??????????????????????????????????????????
            if (Build.VERSION.SDK_INT >= 29) {
                if (TextUtils.isEmpty(OAID)) {
                    MiitHelper miitHelper = new MiitHelper(new MiitHelper.AppIdsUpdater() {
                        @Override
                        public void OnIdsAvalid(@NonNull String oaid) {
                            Log.i("json", "oaid = " + oaid);
                            OAID = oaid;
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadUrl();
                                }
                            });
                        }
                    });
                    miitHelper.getDeviceIds(requireActivity());
                } else {
                    loadUrl();
                }
            } else {
                loadUrl();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUrl() {
        String keycodePre = pid + imei + meid + OAID + androidid + "1" + userid + key;
        keycode = string2MD5(keycodePre).toLowerCase();
        globalUrl = "https://www.51gzdhh.xyz/pipi/newList?userid=" + userid + "&imei=" + imei + "&gtype=1&pid=" + pid + "&sign=" + keycode + "&meid=" + meid + "&oaid=" + OAID + "&androidid=" + androidid;
        webView.loadUrl(globalUrl);
        Log.i("-----------url", "url = " + globalUrl);
    }


    private String LyTempUrl = ""; // ???????????? ?????????????????????Url
    private long LyTempStarTime = 0; // ???????????? ?????????????????????

    /**
     * ?????????????????????Url????????????????????????????????????
     *
     * @param url ????????????Url
     * @return true???????????????false???????????????
     */
    private boolean LyCheckClickUrlIsCanJump(String url) {
        if (!LyTempUrl.contains(url)) {
            LyTempUrl = url;
            LyTempStarTime = System.currentTimeMillis();
            return true;
        }
        if (System.currentTimeMillis() - LyTempStarTime > 600) { // ???????????????600ms
            LyTempStarTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * ??????app
     */
    private void doStartApplicationWithPackageName(String packagename) {
        PackageManager packageManager = requireActivity().getPackageManager();
        Intent intent1 = new Intent();
        intent1 = packageManager.getLaunchIntentForPackage(packagename);
        if (intent1 == null) {
            Toast.makeText(requireActivity(), "?????????", Toast.LENGTH_LONG).show();
        } else {
            startActivity(intent1);
        }
        return;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                if (webView.canGoBack()) {
//                    webView.goBack();
//                } else {
//                    finish();
//                }
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @JavascriptInterface
    public void setJs(String js) {
        isCanLoadFiJs = true;
        hadLoade15 = false;
        hadLoade45 = false;
        hadLoade75 = false;
        this.js = js;
        Log.i("setJs:", "..." + js);
    }

    @JavascriptInterface
    public void closeJs(String js) {
        this.isCanLoadFiJs = false;

        this.js = "";
        Log.i("closeJs:", "...");
    }

    @JavascriptInterface
    public void test() {
        Log.i("CheckInstall:", "...");
    }

    /**
     * ?????????????????????app?????????????????????????????????????????????H5
     *
     * @param packageName
     */
    @JavascriptInterface
    public void CheckInstall(final String packageName) {

        Log.i("CheckInstall:", packageName + "...");

        packagenameLocal = packageName;
        final boolean isInstalled = SystemUtils.isAppInstalled(requireActivity(), packageName);
        if (isInstalled) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:CheckInstall_Return(1)");
                    Log.i("CheckInstall:", packageName + "...1");
                }
            });
        } else {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:CheckInstall_Return(0)");
                    Log.i("CheckInstall:", packageName + "...2");
                }
            });
        }
    }

    /**
     * @param appId    ??????appId
     * @param userName ???????????????Id
     * @param path     ???????????????????????????????????????????????????????????????????????????
     */
    @JavascriptInterface
    public void openMiniProgram(String appId, String userName, String path) {
        IWXAPI api = WXAPIFactory.createWXAPI(requireActivity(), appId);
        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        req.userName = userName;
        req.path = path;
        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE; // ???????????? ?????????????????????????????????
        api.sendReq(req);
    }

    @JavascriptInterface
    public void setTopAd(final int isshow, final String url) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isshow == 0) {
                    webview_top.setVisibility(View.GONE);
                } else {
                    webview_top.setVisibility(View.VISIBLE);
                    webview_top.loadUrl(url);
                }
            }
        });
    }

    boolean isTaskFinish = false;
    int taskClickTime = 0;
    CountDownTimer countDownTimer;

    @JavascriptInterface
    public void setBottomTime(final int isshow, final String str, final int time, final int clickTime) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isshow == 0) {
                    tv_show_time.setVisibility(View.GONE);
                } else {
                    isTaskFinish = false;
                    taskClickTime = 0;
                    tv_show_time.setVisibility(View.VISIBLE);
                }
                tv_show_time.setText(str.replaceAll("x", taskClickTime + "").replaceAll("f", "" + time));
                if (time != 0 && str != null && !TextUtils.isEmpty(str) && str.contains("f")) {
                    if (countDownTimer != null) {
                        countDownTimer.onFinish();
                        countDownTimer.cancel();
                    }
                    countDownTimer = new CountDownTimer(time * 100000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            long s = millisUntilFinished / 1000;
                            s = millisUntilFinished / 1000;
                            s = time - (time * 100 - s);
                            if (s < 0) {
                                s = 0;
                            }
                            if (taskClickTime >= clickTime) {
                                taskClickTime = clickTime;
                            }

                            tv_show_time.setText(str.replace("f", "" + (s)).replaceAll("x", taskClickTime + ""));
                            if (taskClickTime >= clickTime && s == 0) {
                                isTaskFinish = true;
                                tv_show_time.setText("???????????????~");
                            }
                        }

                        @Override
                        public void onFinish() {

                        }
                    }.start();
                }
            }
        });
    }

    @JavascriptInterface
    public boolean getTaskStatue() {
        return isTaskFinish;
    }

    /**
     * ??????????????????App
     *
     * @param packageName
     */
    @JavascriptInterface
    public void AwallOpen(String packageName) {
        doStartApplicationWithPackageName(packageName);
    }

    @JavascriptInterface
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // ???Flag??????????????????????????????????????????????????????????????????????????????????????????Q???????????????????????????????????????????????????????????????    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // ????????????Q???????????????????????????
            return false;
        }
    }

    /**
     * Toast????????????
     *
     * @param message
     */
    @JavascriptInterface
    public void popout(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void gameBegin() {

        //????????????
        if (TextUtils.isEmpty(downUrlLocal)) {
            Toast.makeText(requireActivity(), "??????????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        //??????????????????
        final boolean isInstalled = SystemUtils.isAppInstalled(requireActivity(), packagenameLocal);
        if (isInstalled) {
            doStartApplicationWithPackageName(packagenameLocal);
            return;
        }

        //????????????
        int last = downUrlLocal.lastIndexOf("/") + 1;
        String apkName = downUrlLocal.substring(last);
        if (!apkName.contains(".apk")) {
            if (apkName.length() > 10) {
                apkName = apkName.substring(apkName.length() - 10);
            }
            apkName += ".apk";
        }
        String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "kuwanzhuan" + File.separator + apkName;
        downLoadApp(apkName, downloadPath, downUrlLocal, tv_start_download);

        //?????????????????????js??????
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:startDownApp()");
            }
        });
    }

    public static class ShareToolUtil {
        private static String sharePicName = "share_pic.jpg";
        private static String sharePicPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "kuwanzhuan" + File.separator + "sharepic" + File.separator;

        /**
         * ??????????????????????????????File???????????????
         * ??????Android??????????????????28??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         * ?????????Android6.0????????????????????????????????????????????????file.mkdirs??????.
         * ???????????????1.??????sdcard????????????????????????manifest.xml??????????????????????????????????????????????????????2.?????????targetSdkVersion??????21???22????????????
         * ????????????????????????????????????????????????????????????????????????????????????????????????
         */
        public static File saveSharePic(Context context, Bitmap bitmap) {
            File file = new File(sharePicPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File filePic = new File(sharePicPath, sharePicName);
            if (filePic.exists()) {
                filePic.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(filePic);
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_logo);
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return filePic;
        }
    }

    /**
     * ??????
     */
    @JavascriptInterface
    public void refresh() {
        if (null != webView) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.reload();
                }
            });
        }
    }

    public static class PlatformUtil {
        public static final String PACKAGE_WECHAT = "com.tencent.mm";
        public static final String PACKAGE_MOBILE_QQ = "com.tencent.mobileqq";
        public static final String PACKAGE_QZONE = "com.qzone";
        public static final String PACKAGE_SINA = "com.sina.weibo";

        // ????????????????????????app
        public static boolean isInstallApp(Context context, String app_package) {
            final PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
            if (pInfo != null) {
                for (int i = 0; i < pInfo.size(); i++) {
                    String pn = pInfo.get(i).packageName;
                    if (app_package.equals(pn)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * ?????????????????????????????????
     */
    @JavascriptInterface
    public void shareWechatFriend(String content) {
        if (PlatformUtil.isInstallApp(requireActivity(), PlatformUtil.PACKAGE_WECHAT)) {
            Intent intent = new Intent();
            ComponentName cop = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
            intent.setComponent(cop);
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra("android.intent.extra.TEXT", content);
//            intent.putExtra("sms_body", content);
            intent.putExtra("Kdescription", !TextUtils.isEmpty(content) ? content : "");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(requireActivity(), "??????????????????????????????", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * ???????????????
     */
    @JavascriptInterface
    public void jsShareWechatMoment(final String url, final String contents) {
        new Handler(requireActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // ????????????????????????????????? ???????????????????????????ui????????????????????? ??????????????????ui
                Glide.with(requireActivity()).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        File file = ShareToolUtil.saveSharePic(requireActivity(), resource);
                        shareWechatMoment(requireActivity(), contents, file);
                    }
                });
            }
        });

    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param context
     * @param content
     */
    public static void shareWechatMoment(Context context, String content, File picFile) {
        if (PlatformUtil.isInstallApp(context, PlatformUtil.PACKAGE_WECHAT)) {
            Intent intent = new Intent();
            //?????????????????????????????????????????????????????????????????????????????????
            ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
            intent.setComponent(comp);
//            intent.setAction(Intent.ACTION_SEND_MULTIPLE);// ???????????????????????????
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            //??????Uri????????????--????????????????????????
            //ArrayList<Uri> imageUris = new ArrayList<>();
            //intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            if (picFile != null) {
                if (picFile.isFile() && picFile.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(context, AUTHORITY, picFile);
                    } else {
                        uri = Uri.fromFile(picFile);
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                }
            }
            // ???????????????????????????????????????
            intent.putExtra("Kdescription", !TextUtils.isEmpty(content) ? content : "");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "??????????????????????????????", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * ?????????????????????
     *
     * @param url
     */
    @JavascriptInterface
    public void openBrowser(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
    }


    /**
     * ??????????????????????????????
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//?????????????????????
        if (REQUEST_CODE == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
        }
        if (hasPermissionDismiss) {//?????????????????????????????????
            showPermissionDialog();
        } else {
            //?????????????????????????????????????????????????????????
            TelephonyManager TelephonyMgr = (TelephonyManager) requireActivity().getSystemService(TELEPHONY_SERVICE);
            imei = TelephonyMgr.getDeviceId();
            openUrl();
        }
    }

    /**
     * ??????????????????
     */
    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(requireActivity())
                    .setMessage("?????????????????????????????????")
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" + getPackageName(requireActivity()));
                            Intent intent = new Intent(Settings.
                                    ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //?????????????????????????????????
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    /**
     * ????????????
     */
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    /**
     * [????????????????????????????????????]
     *
     * @param context
     * @return ???????????????????????????
     */
    public static synchronized String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ????????????
     */
    public void downLoadApp(final String apkName, final String path, final String url, final TextView tv) {
        BaseDownloadTask baseDownloadTask = FileDownloader.getImpl().create(url)
                .setPath(path)
                .setCallbackProgressTimes(100)
                .setMinIntervalUpdateSpeed(100)
                .setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.pending(task, soFarBytes, totalBytes);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.progress(task, soFarBytes, totalBytes);

                        if (totalBytes == -1) {
                            tv.setText("????????????");
                        } else {
                            int progress = (int) ((Long.valueOf(soFarBytes) * 100) / Long.valueOf(totalBytes));
                            tv.setText("????????????" + "(" + progress + "%)");
                        }
                        tv.setEnabled(false);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        tv.setText("????????????");
                        installAPK(new File(path), apkName);
                        tv.setEnabled(true);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.paused(task, soFarBytes, totalBytes);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        super.warn(task);
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                    }
                });
        baseDownloadTask.start();
    }

    /**
     * ??????????????????????????????
     */
    protected void installAPK(File file, String apkName) {
        if (!file.exists())
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean hasInstallPermission = isHasInstallPermissionWithO(requireActivity());
            if (!hasInstallPermission) {
                Toast.makeText(requireActivity(), "???????????????????????????", Toast.LENGTH_SHORT).show();
                startInstallPermissionSettingActivity(requireActivity());
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //?????????????????????7.0??????
            Uri apkUri = FileProvider.getUriForFile(requireActivity(), AUTHORITY, file);//???AndroidManifest??????android:authorities???
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            startActivity(install);
        } else {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(install);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isHasInstallPermissionWithO(Context context) {
        if (context == null) {
            return false;
        }

        return context.getPackageManager().canRequestPackageInstalls();
    }

    int REQUEST_CODE_APP_INSTALL = 9;

    /**
     * ????????????????????????????????????????????????
     *
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_APP_INSTALL);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK){
//            if(requestCode == REQUEST_CODE_APP_INSTALL ){
//
//            }
//        }
//    }

    /**
     * md5??????
     *
     * @param string
     * @return
     */
    public String string2MD5(String string) {

        if (TextUtils.isEmpty(string)) {
            return "";
        }

        MessageDigest md5 = null;

        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * ??????????????????
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //???????????????
                InputStream inStream = new FileInputStream(oldPath); //???????????????
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //????????? ????????????
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("??????????????????????????????");
            e.printStackTrace();
        }
    }
    /**
     * ???????????????
     *
     * @param keyCode
     * @param event
     * @return
     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
//            webView.goBack();
//            if(!webView.canGoBack() && webView.getUrl().contains("51gzdhh.xyz")){
//                swipeLayout.setEnabled(true);
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    /**
     * ??????????????????????????????
     *
     * @param showType
     * @param buttonType
     * @param buttonName
     * @param downUlr
     */
    @JavascriptInterface
    public void initPceggsData(final String showType, final String buttonType, final String buttonName, String downUlr) {

        Log.i("initPceggsData:", showType + "...." + buttonType + "..." + buttonName + "..." + downUlr);

        downUrlLocal = downUlr;

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ("0".equals(showType)) {
                    tv_start_download.setVisibility(View.GONE);
                } else {
                    tv_start_download.setVisibility(View.VISIBLE);
                }

                tv_start_download.setText(buttonName);

                if ("0".equals(buttonType)) {
                    tv_start_download.setEnabled(false);
                } else {
                    tv_start_download.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!firstLoad) {
            swipeLayout.setRefreshing(true);
            //??????????????????
            webView.loadUrl(webView.getUrl());
        }
    }

    private static final String TAG_RELOAD = "tag_reload";

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && (btnLogin != null && btnLogin.getVisibility() == View.VISIBLE) && !Once.beenDone(TimeUnit.SECONDS, 5, TAG_RELOAD)) {
            openUrl();
            Once.markDone(TAG_RELOAD);
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}