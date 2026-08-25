package android.serialport.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ComBean
{
  public byte[] bRec = null;
  public String sComPort = "";
  public String sRecTime = "";
  
  public ComBean(String paramString, byte[] paramArrayOfByte, int paramInt)
  {
    this.sComPort = paramString;
    this.bRec = new byte[paramInt];
    int i = 0;
    for (;;)
    {
      if (i >= paramInt)
      {
        this.sRecTime = new SimpleDateFormat("hh:mm:ss").format(new Date());
        return;
      }
      this.bRec[i] = paramArrayOfByte[i];
      i += 1;
    }
  }
}
