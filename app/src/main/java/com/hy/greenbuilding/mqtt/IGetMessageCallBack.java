package com.hy.greenbuilding.mqtt;

public interface IGetMessageCallBack {
  public void setMessage(String message);
  public void setMessage1(String message);
  public void setMessage2(String message);
  void updateWeather(boolean isConnect);
  public void sendMessage3(byte[] bytes);
  public void sendOtaStatus(boolean status);
  void onDownloadProgressUpdate(int progress, int fileType,String message);
}
