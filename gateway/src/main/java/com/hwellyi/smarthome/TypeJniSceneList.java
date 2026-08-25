package com.hwellyi.smarthome;

import java.util.List;

public class TypeJniSceneList
{
	public class TypeJniSceneInfo
	{
		long id;
		int iconid;
		int roomid;
		int status;
		String name;
		public long getId()
		{
			return id;
		}
		public void setId(long tid)
		{
			id = tid;
		}
		public int getIconid()
		{
			return iconid;
		}
		public void setIconid(int ticonid)
		{
			iconid = ticonid;
		}
		public int getRoomid()
		{
			return roomid;
		}
		public void setRoomid(int troomid)
		{
			roomid = troomid;
		}
		public int getStatus()
		{
			return status;
		}
		public void setStatus(int tstatus)
		{
			status = tstatus;
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
	List<TypeJniSceneInfo> scenelist;
	public List<TypeJniSceneInfo> getScenelist()
	{
		return scenelist;
	}
	public void setScenelist(List<TypeJniSceneInfo> tscenelist)
	{
		scenelist = tscenelist;
	}
}
