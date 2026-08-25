package com.hwellyi.smarthome;

import java.util.List;

public class TypeJniRoomList
{
	public class TypeJniRoomInfo
	{
		int roomid;
		int iconid;
		String name;
		public int getRoomid()
		{
			return roomid;
		}
		public void setRoomid(int troomid)
		{
			roomid = troomid;
		}
		public int getIconid()
		{
			return iconid;
		}
		public void setIconnid(int ticonid)
		{
			iconid = ticonid;
		}
		public String getName()
		{
			return name;
		}
		public void setName(String tname)
		{
			name = tname;
		}
	}
	List<TypeJniRoomInfo> roomlist = null;
	public List<TypeJniRoomInfo> getRoomlist()
	{
		return roomlist;
	}
	public void setRoomlist(List<TypeJniRoomInfo> troomlist)
	{
		roomlist = troomlist;
	}
	public String onFindRoomName(int troomid)
	{
		String retName = "默认房间";
		if(roomlist != null)
		{
			for (int i = 0; i < roomlist.size(); i++)
			{
				if(roomlist.get(i).getRoomid() == troomid)
				{
					retName = roomlist.get(i).getName();
					break;
				}
			}
		}
		return retName;
	}
}
