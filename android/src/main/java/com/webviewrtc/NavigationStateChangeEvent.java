package com.webviewrtc;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class NavigationStateChangeEvent extends Event<NavigationStateChangeEvent> {

    public static final String EVENT_NAME = "navigationStateChange";

    private final String mTitle;
    private final boolean mIsLoading;
    private final String mUrl;
    private final boolean mCanGoBack;
    private final boolean mCanGoForward;
    private final int mStatusCode;
    private final String mResponseMessage;

    public NavigationStateChangeEvent(int viewId, long timestampMs, String title, boolean isLoading, String url, boolean canGoBack, boolean canGoForward, int statusCode, String responseMessage) {
        super(viewId);

        mTitle = title;
        mIsLoading = isLoading;
        mUrl = url;
        mCanGoBack = canGoBack;
        mCanGoForward = canGoForward;
        mStatusCode = statusCode;
        mResponseMessage = responseMessage;
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("title", mTitle);
        eventData.putBoolean("loading", mIsLoading);
        eventData.putString("url", mUrl);
        eventData.putBoolean("canGoBack", mCanGoBack);
        eventData.putBoolean("canGoForward", mCanGoForward);
        eventData.putInt("statusCode", mStatusCode);
        eventData.putString("responseMessage", mResponseMessage);

        return eventData;
    }

}
