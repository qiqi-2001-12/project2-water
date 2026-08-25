package JavaType;

/**
 * Created by xia_w on 2017/9/18.
 */

public class TypeSecurityInfo
{
    public int keyID;
    public int subID;
    public int subType;
    public int securityStatus;
    public String deviceName;
    public String roomName;
    public TypeSecurityInfo(int tkeyid, int tsubid, int tsubtype, int tsecuritystatus, String tdevicename, String troomname)
    {
        keyID = tkeyid;
        subID = tsubid;
        subType = tsubtype;
        securityStatus = tsecuritystatus;
        deviceName = tdevicename;
        roomName = troomname;
    }
    public TypeSecurityInfo()
    {
        keyID = 0;
        subID = 0;
        subType = 0;
        securityStatus = 0;
        deviceName = "";
        roomName = "";
    }
}
