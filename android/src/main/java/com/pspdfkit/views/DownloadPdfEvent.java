package com.pspdfkit.views;

import androidx.annotation.IdRes;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.pspdfkit.react.events.PdfViewStateChangedEvent;

import java.util.UUID;

public class DownloadPdfEvent extends Event<PdfViewStateChangedEvent> {

  public DownloadPdfEvent(@IdRes int viewId) {
    super(viewId);
  }

  public static final String EVENT_NAME = "onStateChanged";
  private final String uuidString = UUID.randomUUID().toString();

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    WritableMap eventData = Arguments.createMap();
    eventData.putString("downloadInitWithUUID", uuidString);
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), eventData);
  }
}
