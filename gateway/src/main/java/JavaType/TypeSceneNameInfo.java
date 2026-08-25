package JavaType;

public class TypeSceneNameInfo
{
    public int sceneID;
    public int iconID;
    public int roomID;
    public int status;
    public int hidden;
    public String name;
    public TypeSceneNameInfo(int tsceneid, int ticonid, int troomid, int tstatus, int thidden, String tname)
    {
        sceneID = tsceneid;
        iconID = ticonid;
        roomID = troomid;
        status = tstatus;
        hidden = thidden;
        name = tname;
    }

    public boolean onIsChanged(TypeSceneNameInfo tscenenameifno)
    {
        return ((sceneID == tscenenameifno.sceneID) && (iconID == tscenenameifno.iconID) && (roomID == tscenenameifno.roomID)
                && (status == tscenenameifno.status) && (hidden == tscenenameifno.hidden) && (name.equalsIgnoreCase(tscenenameifno.name)));
    }
}
