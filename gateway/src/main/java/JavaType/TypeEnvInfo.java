package JavaType;

public class TypeEnvInfo {

    public int keyID;
    public int subID;
    public int subType;
    public int tempSensorValue;
    public int humiSensorValue;
    public int illumSensorValue;
    public int pm25Value;
    public int airLevel;
    public int CO2Value;
    public String deviceName;
    public String roomName;
    public TypeEnvInfo(int tkeyid, int tsubid, int tsubtype, int ttempSensorValue,int thumiSensorValue,int tillumSensorValue,int tpm25Value,int tairLevel,int tCO2Value, String tdevicename, String troomname)
    {
        keyID = tkeyid;
        subID = tsubid;
        subType = tsubtype;
        tempSensorValue = ttempSensorValue;
        humiSensorValue= thumiSensorValue;
        illumSensorValue= tillumSensorValue;
        pm25Value= tpm25Value;
        airLevel= tairLevel;
        CO2Value= tCO2Value;
        deviceName = tdevicename;
        roomName = troomname;
    }
    public TypeEnvInfo()
    {
        keyID = 0;
        subID = 0;
        subType = 0;
        tempSensorValue = 0;
        humiSensorValue= 0;
        illumSensorValue= 0;
        pm25Value= 0;
        airLevel= 0;
        CO2Value= 0;
        deviceName = "";
        roomName = "";
    }
}
