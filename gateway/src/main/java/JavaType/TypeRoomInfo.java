package JavaType;

public class TypeRoomInfo
{
    public int roomID;
    public int iconID;
    public String name;
    public TypeRoomInfo(int troomid, int ticonid, String tname)
    {
        roomID = troomid;
        iconID = ticonid;
        name = tname;
    }

    public boolean onIsChanged(TypeRoomInfo troominfo)
    {
        return ((roomID == troominfo.roomID) && (iconID == troominfo.iconID) && (name.equalsIgnoreCase(troominfo.name)));
    }
}
