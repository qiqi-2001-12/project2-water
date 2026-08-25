package com.hwellyi.smarthome;

import java.util.List;

public class TypeJniDeviceList
{
	public class TypeJniDeviceInfo
	{
		int id;//设备ID
		int subid;//设备子ID
		int subtype;//设备子类型
		int roomid;//房间ID
		int iconid;//图标ID
		String name;//设备名称
		int status;//设备状态
		public int getId()
		{
			return id;
		}
		public void setId(int tid)
		{
			id = tid;
		}
		public int getSubid()
		{
			return subid;
		}
		public void setSubid(int tsubid)
		{
			subid = tsubid;
		}
		public int getSubtype()
		{
			return subtype;
		}
		public void setSubtype(int tsubtype)
		{
			subtype = tsubtype;
		}
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
		public void setIconid(int ticonid)
		{
			iconid = ticonid;
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
	List<TypeJniDeviceInfo> devlist;
	public List<TypeJniDeviceInfo> getDevlist()
	{
		return devlist;
	}
	public void setDevlist(List<TypeJniDeviceInfo> tdevlist)
	{
		devlist = tdevlist;
	}
}
