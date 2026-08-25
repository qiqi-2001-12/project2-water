package JavaType;

/**
 * Created by xia_w on 2017/10/13.
 */

public class TypeDownLoad
{
    public String debug;
    public String release;
    public String app_ver;
    public String md5sum;
    public String md5sum_release;
    public String desc;
    public int status;
    public boolean isGetting;
    public TypeDownLoad()
    {
        isGetting = false;
        onInit();
    }

    public void onInit()
    {
        status = 0;
        debug = "";
        release = "";
        app_ver = "";
        md5sum = "";
        md5sum_release = "";
        desc = "";
    }
}
