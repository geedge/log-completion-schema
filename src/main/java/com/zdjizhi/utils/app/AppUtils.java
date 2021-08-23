package com.zdjizhi.utils.app;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.http.HttpClientUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AppId 工具类
 *
 * @author qidaijie
 */

public class AppUtils {
    private static final Log logger = LogFactory.get();
    private static Map<Integer, String> appIdMap = new ConcurrentHashMap<>(128);
    private static AppUtils appUtils;

    private static void getAppInstance() {
        appUtils = new AppUtils();
    }


    /**
     * 构造函数-新
     */
    private AppUtils() {
        //定时更新
        updateAppIdCache();
    }

    /**
     * 更新变量
     */
    private static void change() {
        if (appUtils == null) {
            getAppInstance();
        }
        timestampsFilter();
    }


    /**
     * 获取变更内容
     */
    private static void timestampsFilter() {
        try {
            Long begin = System.currentTimeMillis();
            String schema = HttpClientUtil.requestByGetMethod(FlowWriteConfig.APP_ID_HTTP);
            if (StringUtil.isNotBlank(schema)) {
                String data = JSONObject.parseObject(schema).getString("data");
                JSONArray objects = JSONArray.parseArray(data);
                for (Object object : objects) {
                    JSONArray jsonArray = JSONArray.parseArray(object.toString());
                    int key = jsonArray.getInteger(0);
                    String value = jsonArray.getString(1);
                    if (appIdMap.containsKey(key)) {
                        if (!value.equals(appIdMap.get(key))) {
                            appIdMap.put(key, value);
                        }
                    } else {
                        appIdMap.put(key, value);
                    }
                }
                logger.warn("Updating the correspondence takes time:" + (begin - System.currentTimeMillis()));
                logger.warn("Pull the length of the interface data:[" + objects.size() + "]");
            }
        } catch (RuntimeException e) {
            logger.error("Update cache app-id failed, exception：" + e);
        }
    }


    /**
     * 验证定时器,每隔一段时间验证一次-验证获取新的Cookie
     */
    private void updateAppIdCache() {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (FlowWriteConfig.APP_TICK_TUPLE_FREQ_SECS != 0) {
                        change();
                    }
                } catch (RuntimeException e) {
                    logger.error("AppUtils update AppCache is error===>{" + e + "}<===");
                }
            }
        }, 1, FlowWriteConfig.APP_TICK_TUPLE_FREQ_SECS, TimeUnit.SECONDS);
    }


    /**
     * 获取 appName
     *
     * @param appId app_id
     * @return account
     */
    public static String getAppName(int appId) {

        if (appUtils == null) {
            getAppInstance();
        }

        if (appIdMap.containsKey(appId)) {
            return appIdMap.get(appId);
        } else {
            logger.warn("AppMap get appName is null, ID is :" + appId);
            return "";
        }
    }

}
