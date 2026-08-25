/*
 * TypeDefine.h
 *
 *  Created on: Jun 30, 2017
 *      Author: root
 */

#ifndef DATATYPE_TYPEDEFINE_H_
#define DATATYPE_TYPEDEFINE_H_
#include "../Main/PublicDefine.h"
#include "../Main/WinobleMain.h"

typedef enum _NET_WORK_STATUS
{
	NetStatusInit = 0,
	NetHttpCertification,
	NetStatusLogin,
	NetStatusClose,
	NetStatusConnect,
	NetStatusRec,
}MNET_STATUS;

enum _DATA_BASE_DEVICEINFO_TYPE__
{
	IDeviceID = 0,
	IGatewayID = 1,
	IDevType = 2,
	IIeee = 3,
	IShortAddr = 4,
	IIeee_Ex = 5,
	IShortAddr_Ex = 6,
	IRgb = 7,
	ISaveRgb = 8,
	IOnline = 9,
	IProtocol = 10,
	IProtocolVe = 11,
	ITargetScreen = 12,
	IAttr = 13,
	ISerial = 14,
	ISwVer = 15,
	IHwVer = 16,
	IManufacturer = 17,
	ISubCount = 18,
	IInset = 49,
};

enum _DATA_ROOMINFO_TYPE__
{
	RoomInset = 0,
	RoomIcon = 1,
	RoomName = 4
};

enum _DATA_SUB_DEVICEINFO_TYPE__
{
	SubInset = 0,
	SubRoomID = 2,
	SubIconID = 3,
	SubSaveIconID = 4,
	SubName = 5,
	SubSaveName = 6,
	SubDevStatus = 8,
	SubLightSceneID = 9,
	SubRS485Para = 9,
	SubDimmingParaValue = 9,
	SubPower = 9,
	SubZone = 10,
	SubDimmingSaveParaValue = 10,
	SubSecurity = 11,
	SubPIRDelayTime = 12,
};

enum _DATA_SCENENAME_TYPE__
{
	SceneInset = 0,
	SceneName = 1,
	SceneRoomID = 2,
	SceneIconID = 3,
	SceneSpecialized = 4,
	SceneDisabled = 5,
	SceneHidden = 6,
	ScenePeriod = 8,
	SceneEnableTime = 9
};

enum _DATA_SCENEACTION_TYPE__
{
	SceneActionInset = 0,
	SceneActionType = 2,
	SceneActionDeviceID = 3,
	SceneActionSubID = 4,
	SceneActionActionType = 5,
	SceneActionAction = 6,
	SceneActionActionDesc = 7,
	SceneActionDelayTime = 8
};

enum _DATA_SCENECOND_TYPE__
{
	SceneCondInset = 0,
	SceneCondAction = 7,
	SceneCondActionDesc = 8,
	SceneCondDelayTime = 9
};

enum _DATA_APPLIANCEINFO_TYPE__
{
	ApplianceInset = 0,
	ApplianceIrID = 1,
	ApplianceIrSubID = 2,
	ApplianceName = 3,
	ApplianceManufacturer = 4,
	ApplianceModelType = 5,
	ApplianceVersion = 6,
	ApplianceSerial = 7,
	ApplianceRoomID = 8,
	ApplianceTType = 9,
	ApplianceValue = 10,
	ApplianceAddr = 11
};

enum _DATA_APPLIANCECMDINFO_TYPE__
{
	ApplianceCMDInset = 0,
	ApplianceCMDKeyID = 2,
	ApplianceCMDIrCode = 3,
	ApplianceCMDStatus = 4
};

typedef enum _DEV_EVENT_FLAG
{
	Event_INIT = 0x00,
	//Event_Dev_GetTemp = 0x02,
	//Event_dev_ShortAddr_Ex = 0x04,
	Event_Dev_RGB = 0x08,
	Event_Dev_Name = 0x10,
	Event_Dev_Icon = 0x20,
	Event_Dev_Heartbeat = 0x40,
	Event_Dev_Status = 0x80,
}EmunEventFlag;

typedef enum _ARRAY_LIST_TYPE__
{
	ArrayTypeChar = 1,
	ArrayTypeArrayList,
	ArrayTypeTcpCMD,
	ArrayTimer_Struct,
	ArrayTypeSeqNo,
	ArrayTypeDeviceTypeInfo,
	ArrayTypeRoomInfo,
	ArrayTypeDataBase,
	ArrayTypeSceneNameInfo,
	ArrayTypeSceneActionInfo,
	ArrayTypeSceneCondInfo,
	ArrayTypeCarriedSceneList,
	ArrayTypeDevNameInfo,
	ArrayTypeDevEventInfo,
	ArrayTypeWaterLeakStatus,
	ArrayTypeSerialProces,
	ArrayTypeDevIconInfo,
	ArrayTypeDBDeviceInfo,
	ArrayTypeApplianceInfo,
	ArrayTypeApplianceCodeInfo,
	ArrayTypeRobotDataInfo,
	ArrayTypeThreadInfo,
	ArrayTypeRS485Profile,
	ArrayTypeSmartDoorLockHLSInfo,
	ArrayTypeGasArmBingInfo,
	ArrayTypeCentralAirConditioningZH,
	ArrayTypeCentralAirConditioningMD,
	ArrayTypeElectricCurtainDY,
	ArrayTypeJniNotificationInfo,
	ArrayTypeGatewayInfo,

}ArrayListType;

typedef enum _DEVICE_STATUS_CLEAR__
{
	STATUS_CLEAR_ONFF1  = 0x0001,
	STATUS_CLEAR_LEVEL1 = 0x0002,
	STATUS_CLEAR_ONFF2  = 0x0004,
	STATUS_CLEAR_LEVEL2 = 0x0008,
	STATUS_CLEAR_ONFF3  = 0x0010,
	STATUS_CLEAR_LEVEL3 = 0x0020,
	STATUS_CLEAR_ONFF4  = 0x0040,
	STATUS_CLEAR_LEVEL4 = 0x0080,
	STATUS_CLEAR_TEMP   = 0x0100,
	STATUS_CLEAR_ILLUM  = 0x0200,
	STATUS_CLEAR_HUMID  = 0x0400,
	STATUS_CLEAR_QUALITY= 0x0800,
	STATUS_CLEAR_GAS    = 0x1000,
	STATUS_CLEAR_PM25   = 0x2000,
	STATUS_CLEAR_POWER  = 0x4000,
	STATUS_CLEAR_CO2  	= 0x8000,
}EmunDevStatusClear;

typedef  enum _DEVICE_EVENT_NOTIFY_
{
	DEV_EVENT_SCREEN_SHARE         = 0x0001,
}EmunDeviceEventNotity;

typedef union _TYPE_DEVICE_ATTR_
{
	int32_t value;
	struct __ATTR_BIT_DEFINE__
	{
		int32_t screen:1;
		int32_t key:1;
		int32_t pir:1;
		int32_t temp:1;
		int32_t humi:1;
		int32_t illu:1;
		int32_t retain:1;
		int32_t rgb:1;
	}bits;
	_TYPE_DEVICE_ATTR_(int32_t tscreen, int32_t tkey, int32_t tpir, int32_t ttemp, int32_t thumi, int32_t tillu, int32_t trgb)
	{
		value = 0;
		bits.screen = tscreen;
		bits.key = tkey;
		bits.pir = tpir;
		bits.temp = ttemp;
		bits.humi = thumi;
		bits.illu = tillu;
		bits.rgb = trgb;
	}

	_TYPE_DEVICE_ATTR_(int32_t tvalue)
	{
		value = tvalue;
	}
}TypeDeviceAttr;

typedef union __ATTR_DEV_ONLINE__
{
	int32_t value;
	struct __ATTR_DEV_ONLINE_BITS__
	{
		DeviceStatus status:8;
		DeviceStatus saveStatus:8;
	}bits;
	int32_t onSetValue(DeviceStatus tstatus, DeviceStatus tsavestatus)
	{
		value = 0;
		bits.status = tstatus;
		bits.saveStatus = tsavestatus;
		return value;
	};
}TypeDeviceOnLineStatus;

class TypeSeqNo
{
public:
	uint32_t seqNo;
	uint32_t command_id;
	int delayTime;
	TypeSeqNo(uint32_t seqno, uint32_t cmd_id)
	{
		command_id = cmd_id;
		seqNo = seqno;
		delayTime = 60000;
		mMemNewFreeCount++;
	};
	~TypeSeqNo()
	{
		mMemNewFreeCount--;
	};
};

class TypeChar
{
private:
	void onMemAdd()
	{
		mMemNewFreeCount++;
	};
public:
	uint32_t size;
	char *buff;
	uint8_t *ubuff;
	TypeChar(uint32_t psize)
	{
		size=psize + 1;
		buff = (char *)malloc((size_t)size);
		memset(buff, 0, (size_t)size);
		ubuff = (uint8_t *)buff;
		onMemAdd();
	};
	TypeChar(const char * pbuff)
	{
		onMemAdd();
		size = 0;
		buff = NULL;
        if(pbuff == NULL)
        {
            size = 1;
			buff = (char *)malloc((size_t)size);
			memset(buff, 0, (size_t)size);
			ubuff = (uint8_t *)buff;
        }
        else
        {
            size=strlen(pbuff) + 1;
			buff = (char *)malloc((size_t)size);
			memset(buff, 0, (size_t)size);

            memcpy(buff, pbuff, size - 1);
        }
		ubuff = (uint8_t *)buff;
	};
	TypeChar()
	{
		onMemAdd();
        size= 256;
		buff = (char *)malloc((size_t)size);
		memset(buff, 0, (size_t)size);
		ubuff = (uint8_t *)buff;
	};

	void onClear()
	{
		memset(buff, 0, (size_t)size);
	};

    bool onAddString(uint32_t index, const char *value)
    {
        uint32_t len = 0;
        if(value != NULL)
        {
            len = strlen(value);
            for(uint32_t i = 0; i < len; i++)
            {
                if((index + i) < size)
                {
                    buff[index + i] = value[i];
                }
            }
            return true;
        } else
        {
            return false;
        }
    }

	bool onAddString(char *value)
	{
		uint32_t oldLen = strlen(buff);
		uint32_t newLen = strlen(value);
		for(uint32_t i = 0; i < newLen; i++)
		{
			if((oldLen + i) < size)
			{
				buff[oldLen + i] = value[i];
			}
			else
			{
				return FALSE;
			}
		}
		return TRUE;
	}

    bool onAddUBuff(uint32_t index, uint8_t *value, int32_t len)
    {
        if(value != NULL)
        {
            for(int i = 0; i < len; i++)
            {
                if((index + i) < size)
                {
                    ubuff[index + i] = value[i];
                }
            }
            return true;
        } else
        {
            return false;
        }
    }

	bool onAddInt64(uint32_t index, int64_t value)
	{
		if(index < (size - 7))
		{
			ubuff[index++] = (uint8_t)((value >> 56) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 48) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 40) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 32) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 24) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 16) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 8) & 0xFF);
			ubuff[index++] = (uint8_t)(value & 0xFF);
		}
		return true;
	};

	bool onAddInt64Ex(uint32_t index, int64_t value)
	{
		if(index < (size - 7))
		{
			ubuff[index++] = (uint8_t)(value & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 8) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 16) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 24) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 32) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 40) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 48) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 56) & 0xFF);
		}
		return true;
	}

	bool onAddInt32(uint32_t index, int32_t value)
	{
		if(index < (size - 3))
		{
			ubuff[index++] = (uint8_t)((value >> 24) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 16) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 8) & 0xFF);
			ubuff[index++] = (uint8_t)(value & 0xFF);
		}
		return true;
	};

	bool onAddInt32Ex(uint32_t index, int32_t value)
	{
		if(index < (size - 3))
		{
			ubuff[index++] = (uint8_t)(value & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 8) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 16) & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 24) & 0xFF);
		}
		return true;
	};

	bool onAddInt16(uint32_t index, int32_t value)
	{
		if(index < (size - 1))
		{
			ubuff[index++] = (uint8_t)((value >> 8) & 0xFF);
			ubuff[index++] = (uint8_t)(value & 0xFF);
		}
		return true;
	};

	bool onAddInt16Ex(uint32_t index, int32_t value)
	{
		if(index < (size - 1))
		{
			ubuff[index++] = (uint8_t)(value & 0xFF);
			ubuff[index++] = (uint8_t)((value >> 8) & 0xFF);
		}
		return true;
	};

	bool onStringCMP(const char *str)
	{
		bool retBool = false;
		if(str != NULL)
		{
			if((memcmp(buff, str, strlen(str)) == 0) && (strlen(buff) == strlen(str)))
			{
				retBool = true;
			}
		}
		return retBool;
	};

	bool onStringContain(const char *str)
	{
		bool retBool = false;
		if(str != NULL)
		{
			uint32_t maxLen = strlen(str);
			uint32_t index = 0;
			for(uint32_t i = 0; i < strlen(buff); i ++)
			{
				if(str[index] && (index < maxLen) && (buff[i] == str[index]))
				{
					index++;
					if(index == maxLen)
					{
						retBool = true;
						break;
					}
				}
				else
				{
					index = 0;
				}
			}
		}
		return retBool;
	};

	~TypeChar()
	{
		mMemNewFreeCount--;
		if(buff != NULL)
		{
			free(buff);
		}
		ubuff = NULL;
	};
};

class TypeArrayList
{

private:
	int strLength;
	int mallocSize;
	int arrayType;
	void **ppData;
	void mChangeMem(bool flag);
	void remove(int index, bool flag);
	void mfRemoveValue(void *obj);
public:
	TypeArrayList(ArrayListType type);
	~TypeArrayList();
	int size();
	void add(void * obj);
	void * get(int index);
	void UnFreeClear();
	void clear();
	void removeObject(void * obj);
	void UnFreeRemoveObject(void * obj);
	bool update(int index, void * obj);
	//bool deleteIndex(int index);
};

class TypeLinkedList
{
private:
	typedef struct __LinkedInfo__
	{
		void *data;
		struct __LinkedInfo__ *next;
		__LinkedInfo__(void *tdata)
		{
			data = tdata;
			next = NULL;
			mMemNewFreeCount++;
		}
		~__LinkedInfo__()
		{
			mMemNewFreeCount--;
		}
	}TypeLinkedInfo;
	ArrayListType linkedType;
	TypeLinkedInfo *pHead;
	TypeLinkedInfo *pTail;
	uint32_t count;
	pthread_mutex_t lockMutex;
public:
	uint32_t onGetCount();
	void *get();
	void add(void *obj);
	bool onDeleteSame(int ttype, void *tdata);
	void clear();
	TypeLinkedList(ArrayListType type);
	~TypeLinkedList();
};

class TypeTcpCMD
{
private:
	void onMemAdd()
	{
		mMemNewFreeCount++;
	}
public:
	uint32_t packetLength;
	uint16_t headerLength;
	uint16_t version;
	uint32_t commandID;
	uint32_t seqNo;
	uint32_t payLoadBuffLen;
	uint32_t sendCount;
	int delayTime;
	TypeChar * payLoadBuff;
	TypeTcpCMD(uint32_t commandid, char *payloadbuff, uint32_t len, uint32_t seqno)
	{
		payLoadBuff = NULL;
		packetLength = len + 12;
		headerLength = 10;
		version = 0x01;
		commandID = commandid;
		seqNo = seqno;
		payLoadBuff = new TypeChar(packetLength + 1 + 4);
		payLoadBuffLen = 0;
		//uint32_t tempInt32 = htonl(packetLength);
		payLoadBuff->onAddInt32(payLoadBuffLen, packetLength);//send packet length
		payLoadBuffLen += 4;
		//uint16_t tempInt16 = htons(headerLength);
		payLoadBuff->onAddInt16(payLoadBuffLen, headerLength);//send Header Length
		payLoadBuffLen += 2;
		//tempInt16 = version;
		//tempInt16 = htons(tempInt16);
		payLoadBuff->onAddInt16(payLoadBuffLen, version);//send version
		payLoadBuffLen += 2;
		//tempInt32 = commandID;
		//tempInt32 = htonl(tempInt32);
		payLoadBuff->onAddInt32(payLoadBuffLen, commandID);//send packet length
		payLoadBuffLen += 4;
		//tempInt32 = seqNo;
		//tempInt32 = htonl(tempInt32);
		payLoadBuff->onAddInt32(payLoadBuffLen, seqNo);//send packet length
		payLoadBuffLen += 4;
		memcpy(&payLoadBuff->buff[payLoadBuffLen], payloadbuff, len);
		payLoadBuffLen += len;
		sendCount = 3;
		delayTime = VALUE_TCP_DELEAY_REPEART;
		onMemAdd();
	};
	TypeTcpCMD()
	{
		payLoadBuff = NULL;
		onClear();
		onMemAdd();
	};

	void onClear()
	{
		packetLength = 0;
		headerLength = 0;
		version = 0;
		commandID = 0;
		seqNo = 0;
		payLoadBuffLen = 0;
		if(payLoadBuff)
		{
			delete payLoadBuff;
		}
		payLoadBuff = NULL;
	};

	~TypeTcpCMD()
	{
		mMemNewFreeCount--;
		if(payLoadBuff)
		{
			delete payLoadBuff;
		}
	};
};

class TypeTimerStruct
{
public:
	int timeID;
	int timeValue;
	int repeatTime;
	int para1;
	int para2;
	void (* mfHandler)(int para1, int para2);
	TypeTimerStruct(int timeid)
	{
		timeID = timeid;
		timeValue = 0;
		repeatTime = 0;
		para1 = 0;
		para2 = 0;
		mfHandler = NULL;
		mMemNewFreeCount++;
	};
	~TypeTimerStruct()
	{
		mMemNewFreeCount--;
	};
};

class TypeRoomInfo
{
public:
	int32_t room_id;
	int32_t iconID;
	int32_t temp_value;
	int32_t illum_value;
	TypeChar * name;
	//添加临时变量
	int32_t randValue;
	TypeRoomInfo(int32_t xroom_id, int32_t xiconid, int32_t xtemp_value, int32_t xillum_value, const char *xname)
	{
		mMemNewFreeCount++;
		room_id = xroom_id;
		iconID = xiconid;
		temp_value = xtemp_value;
		illum_value = xillum_value;
		name = new TypeChar(xname);
		randValue = 0;
	};

	~TypeRoomInfo()
	{
		mMemNewFreeCount--;
		delete name;
	}
};

class TypeScenePannelStatus
{
public:
	int32_t scene_id;
	int32_t status;
	TypeScenePannelStatus(int32_t xscene_id, int32_t xstatus)
	{
		mMemNewFreeCount++;
		scene_id = xscene_id;
		status = xstatus;
	};
	~TypeScenePannelStatus()
	{
		mMemNewFreeCount--;
	};
};

class TypeZclAttribute
{
private:
	void onMemAdd()
	{
		mMemNewFreeCount++;
	};
public:
	uint32_t attributeID;
	uint8_t status;
	uint32_t dataType;
	uint8_t dataBuffLen;
	uint8_t totalLen;
	TypeChar *totalBuff;
	TypeChar *dataBuff;
public:
	TypeZclAttribute()
	{
		attributeID = 0;
		status = 0;
		totalLen = 0;
		totalBuff = NULL;
		dataBuffLen = 0;
		dataBuff = NULL;
		dataType = 0;
		onMemAdd();
	}
	uint8_t * onGetDataBuff()
	{
		if(dataBuff != NULL)
		{
			return dataBuff->ubuff;
		}
		else
		{
			return NULL;
		}
	}
	TypeZclAttribute(uint32_t attributeid, uint8_t attributetype, uint8_t *databuff, uint8_t len)
	{
		onMemAdd();
		uint8_t tempIndex = 0;
		attributeID = attributeid;
		dataBuff = NULL;
		dataType = 0;
		dataBuffLen = 0;
		status = 0;
		if(attributetype == 0)
		{
			totalLen = 2;
		}
		else
		{
			totalLen = (uint8_t)(3 + len);
		}
		totalBuff = new TypeChar(totalLen);
		totalBuff->onAddInt16Ex(tempIndex, attributeID);
		tempIndex += 2;
		if(totalLen > 2)
		{
			totalBuff->ubuff[tempIndex++] = attributetype;
			dataType = attributetype;
			dataBuffLen = len;
			dataBuff = new TypeChar(dataBuffLen);
			dataBuff->onAddUBuff(0, databuff, dataBuffLen);
		}
		totalBuff->onAddUBuff(tempIndex, databuff, len);
	}
	~TypeZclAttribute()
	{
		if(dataBuff != NULL)
		{
			delete dataBuff;
		}
		if(totalBuff != NULL)
		{
			delete totalBuff;
		}
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};

class TypeZclHead
{
private:
	void onMemAdd()
	{
		mMemNewFreeCount++;
	};
public:
	uint8_t type:2;
	uint8_t manuSpecific:1;
	uint8_t direction:1;
	uint8_t disableDefaultRsp:1;
	uint8_t reserved:3;
	TypeZclHead(uint8_t value)
	{
		reserved = 0;
		type = (uint8_t)(value & 0x03);
		manuSpecific = (uint8_t)((value >> 2) & 0x01);
		direction = (uint8_t)((value >> 3) & 0x01);
		disableDefaultRsp = (uint8_t)((value >> 4) & 0x01);
		onMemAdd();
	}
	TypeZclHead(uint8_t ttype, uint8_t tmanuspecific, uint8_t tdirection, uint8_t disablersp)
	{
		reserved = 0;
		type=ttype;
		manuSpecific = tmanuspecific;
		direction = tdirection;
		disableDefaultRsp = disablersp;
		onMemAdd();
	}
	uint8_t onToData()
	{
		uint8_t retData = 0;
		retData = (uint8_t)((disableDefaultRsp << 4) | (direction << 3) | (manuSpecific << 2) | type);
		return retData;
	}
	~TypeZclHead()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};

class TypeZclProfile
{
private:
	void onMemAdd();
public:
	TypeZclHead *zclHead;
	uint32_t manufacturerID;//厂家ID
	uint8_t seqNum;
	uint8_t cmdID;
	TypeZclAttribute *attrubiteData;
	TypeChar *toBuff;
	uint8_t toBuffLen;
	TypeZclProfile(uint8_t *pbuff, uint8_t datalen);
	TypeZclProfile(bool manufacturerflag, uint8_t tcmdid, TypeZclAttribute *attribute);
	~TypeZclProfile();
};

class TypeAFAttribute//
{
private:
	void onMemAdd()
	{
		mMemNewFreeCount++;
	};
public:
	/*
	uint32_t shortAddr;
	uint8_t desEndPoint;
	uint8_t srcEndPoint;
	uint32_t clusterID;
	uint8_t transID;
	uint8_t txOpts;
	uint8_t radius;
	uint8_t dataLen;
	TypeZclProfile * sendZcl;
	 */
	TypeChar *toDataBuff;
	uint8_t toDataLen;
	uint8_t afSeqNum;
	uint8_t desEndPoint;
	uint32_t clusterID;
	TypeZclProfile *sendZcl;
	TypeAFAttribute(uint8_t *pbuff, uint8_t len)
	{
		onMemAdd();
		toDataLen = len;
		toDataBuff = NULL;
		if(len > 0)
		{
			toDataBuff = new TypeChar(toDataLen);
			if(pbuff != NULL)
			{
				toDataBuff->onAddUBuff(0, pbuff, toDataLen);
			}
		}
		sendZcl = NULL;
		afSeqNum = 0;
		desEndPoint = 0;
		clusterID = 0;
	}
	TypeAFAttribute(uint32_t shortaddr, uint8_t desendpoint, uint32_t clusterid, TypeZclProfile *sendzcl)
	{
		onMemAdd();
		afSeqNum = onGetAFSendSeq();
		toDataLen = 0;
		toDataBuff = new TypeChar(10 + sendzcl->toBuffLen);
		toDataBuff->onAddInt16Ex(toDataLen, shortaddr);
		toDataLen += 2;
		desEndPoint = desendpoint;
		toDataBuff->ubuff[toDataLen++] = desEndPoint;
		toDataBuff->ubuff[toDataLen++] = USER_ENDPOINTNUM;
		clusterID = clusterid;
		toDataBuff->onAddInt16Ex(toDataLen, clusterID);
		toDataLen += 2;
		toDataBuff->ubuff[toDataLen++] = afSeqNum;
		toDataBuff->ubuff[toDataLen++] = 0;//
		toDataBuff->ubuff[toDataLen++] = 0;
		toDataBuff->ubuff[toDataLen++] = sendzcl->toBuffLen;
		toDataBuff->onAddUBuff(toDataLen, sendzcl->toBuff->ubuff, sendzcl->toBuffLen);
		toDataLen += sendzcl->toBuffLen;
		sendZcl = sendzcl;
	}
	~TypeAFAttribute()
	{
		if(toDataBuff != NULL)
		{
			delete toDataBuff;
		}
		toDataBuff = NULL;
		if(sendZcl != NULL)
		{
			delete sendZcl;
		}
		sendZcl = NULL;
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};

class TypeSerialProces
{
public:
	bool isMasterFlag;
	int8_t reSendBit;
	uint8_t cmdType;
	uint8_t subSystem;
	uint8_t subCMD;
	uint8_t status;
	uint8_t checkSum;
	int32_t delayTime;
	uint32_t shortAddr;
	int8_t repeatCount;
	StatusSerialProces retStatus;
	TypeAFAttribute *afAttribute;
	TypeSerialProces(bool ismater);
	TypeSerialProces(uint32_t shortaddr, uint8_t cmd1, uint8_t cmd2, TypeAFAttribute * afattribute, int32_t delaytime);
	bool onCheckTime(int32_t timems);
	bool onProces();
	void onClear();
	bool toString(bool sendflag);
	bool onPrintfError(uint8_t status, uint8_t type);
	bool onIsZigbeeCMD();
	bool onIsSameCMD(TypeSerialProces *proces);
	~TypeSerialProces();
private:
	void onMemAdd();
	bool onSysProces();
	bool onAFProces();
	bool onZDOProces();
	bool onSAPIProces();
	bool onUTILProces();
	bool onAPPProces();
};

class TypeAFINComming
{
public:
	uint32_t groupID;
	uint32_t clusterID;
	uint32_t shortAddr;
	uint8_t srcEndPoint;
	uint8_t desEndPoint;
	uint8_t wasBroadCast;
	int8_t LinkQuality;
	uint8_t SecurityUse;
	uint32_t timeStamp;
	uint8_t seqNum;
	uint8_t dataLen;
	TypeZclProfile *zclProfile;
	uint32_t macSrcAddr;
	int8_t radius;
	TypeAFINComming(uint8_t *pbuff);
	int onZclProcess(bool ismater);
	~TypeAFINComming();
};

class TypeSerialDrive
{
public:
	int32_t checkDrivceErrorCnt;
	long startNetFail;
	int  getSrcEntryTime;
	bool isMasterFlag;
	int32_t isError;
	TypeChar * deviceName;
	int deviceBaud;
	int deviceHandle;
	TypeSerialProces *deviceProces;
	TypeChar *reciveBuff;
	TypeSerialDrive(const char *name, int baud, bool ismaster);
	void onReviceData(TypeSerialProces *proces);
	void onWriteCMD(TypeSerialProces *proces);
	uint32_t onGetSendCount();
	void onCheckSendCMD(int mstime);
	void onSetSendStatus(uint8_t status, uint8_t type);
	int onStartNewNetWork(bool flag);
	int onLeaveWithIEEE(int32_t shortaddr ,int64_t ieee);
	int onWriteAttribute(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, TypeZclAttribute *attribute, uint32_t delaytime);
	//int onWriteAttributeNoRsp(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, TypeZclAttribute *attribute, uint32_t delaytime);
	int onWriteAttributeGeneric(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, TypeZclAttribute *attribute, uint32_t delaytime);
	int onReadAttribute(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint32_t attributeid, uint32_t delaytime);
	int onReadAttributeGeneric(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint32_t attributeid, uint32_t delaytime);
	int onWriteZclCMD(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint8_t commandid, uint8_t *databuff, uint8_t datalen, uint32_t delaytime);
	int onAirDLCMD(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint8_t commandid, uint8_t rspseq, uint8_t *databuff, uint8_t datalen, int32_t delaytime);
	int onDefaultRspGeneric(uint8_t srcpoint, uint32_t shortaddr, uint32_t clusterid, uint8_t seqnum, uint8_t commandid, uint8_t status);
	~TypeSerialDrive();
private:
	void onWriteData(TypeSerialProces *proces);
	void onAddDelayReSend(TypeSerialProces *proces, int32_t delaytime);
	TypeLinkedList *pCMDSendList;
	TypeSerialProces *pLastSendCMD;
	TypeArrayList *pDelaySendList;
};

class TypeLightStatus
{
public:
	int64_t sceneID;
	int32_t status;
	int32_t needSetStatus;
	TypeLightStatus(int32_t xstatus, int64_t sceneid)
	{
		mMemNewFreeCount++;
		needSetStatus = status = xstatus;
		sceneID = sceneid;
	};

	bool onSetStatus(int32_t xstatus)
	{
		bool retBool = false;
		//if(status != xstatus)
		{
			status = xstatus;
			retBool = true;
		}
		return retBool;
	};
	~TypeLightStatus()
	{
		mMemNewFreeCount--;
	};
};

class TypeDimmingStatus
{
public:
	int32_t status;
	int32_t paraValue;
	int32_t saveParaValue;
	int32_t needSetStatus;
	TypeDimmingStatus(int32_t xstatus, int32_t xparavalue, int32_t xsaveparavalue)
	{
		mMemNewFreeCount++;
		needSetStatus = status = xstatus;
		paraValue = xparavalue;
		saveParaValue = xsaveparavalue;

	};
    bool onSetStatus(int32_t xstatus)
    {
        bool retBool = false;
        /*
        if(status != xstatus)
        {
            status = xstatus;
            retBool = true;
        }*/
        return retBool;
    };

    bool onSetLevel(int32_t value)
    {
        bool retBool = false;
		if(value >= 254) value = 255;
		else if((value > 0) && (value < 3)) value = 3;
        //先转换成百分比
        float percent = value * 100.00 / 255.00;
        if(status != (int32_t)percent)
        {
            status = (int32_t)percent;
            retBool = true;
        }
        return retBool;
    };

	int32_t onGetLevel()
	{
		int32_t retInt = status;
		float value = (float)((retInt * 255.00) / 100.00);
		if(((retInt * 255) % 100) > 0) value += 1.0;
		retInt = (int32_t) value;
		if(retInt > 254)
			retInt = 254;
		return retInt;
	}

	~TypeDimmingStatus()
	{
		mMemNewFreeCount--;
	};
};

class TypeCurtainStatus
{
public:
	int32_t status;
	int32_t needSetStatus;
	TypeCurtainStatus(int32_t xstatus)
	{
		mMemNewFreeCount++;
		needSetStatus = status = xstatus;
	};
	bool onSetLevel(int32_t value)
	{
		bool retBool = false;
		//先转换成百分比
		if(value < 0) value = 0;
		if(value > 100) value = 100;
		if(status != value)
		{
			status = value;
			retBool = true;
		}
		return retBool;
	};
	int32_t onGetLevel()
	{
		return status;
	}
	~TypeCurtainStatus()
	{
		mMemNewFreeCount--;
	};
};

class TypeSwitchStatus
{
public:
	int32_t status;
	int32_t currentPower;
	int32_t tadayEnergy;
	int32_t lastEnergyDate;
	int32_t needSetStatus;
	TypeSwitchStatus(int32_t xstatus, int32_t xcurrentPower, int32_t xtadayenergy, int32_t xlastenergydate)
	{
		mMemNewFreeCount++;
		needSetStatus = status = xstatus;
		currentPower = xcurrentPower;
		tadayEnergy = xtadayenergy;
		lastEnergyDate = xlastenergydate;
	};

	bool onSetStatus(uint8_t tstatus)
	{
		if(status != tstatus)
		{
			status = tstatus;
			return TRUE;
		}
		else
		{
			return FALSE;
		}
	}

	bool onUpdateTadayEnergy(int32_t value)
	{
		if(value > 0)
		{
			tadayEnergy += value;
			return true;
		}
		return false;
	};

	void onClearTadayEnergy()
	{
		tadayEnergy = 0;
	};

	bool onCheckLastEnergyDate(int32_t date)
	{
		if(date != (lastEnergyDate / 86400))//86400 = 24 * 3600
		{
			lastEnergyDate = date * 86400;
			return true;
		}
		else
		{
			return false;
		}
	};

	~TypeSwitchStatus()
	{
		mMemNewFreeCount--;
	};
};

class TypeGasStatus
{
public:
	int32_t gasValue;
	TypeGasStatus(int32_t gasvalue)
	{
		mMemNewFreeCount++;
		gasValue = gasvalue;
	};

	bool onSetStatus(int32_t xstatus)
	{
		bool retBool = false;
		if(gasValue != xstatus)
		{
			gasValue = xstatus;
			retBool = true;
		}
		return retBool;
	};
	~TypeGasStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeApplianceCodeInfo
{
public:
	int32_t  appID;  // 家电ID
	int32_t  key_id;       // 遥控按键ID 这个在这个家电里面是唯一的
	int32_t  ir_code;      // 红外编码ID
	int32_t  status;       // status: 0-start, 1-leanring, 2-completed, 3-failed.
	//添加临时变量
	int32_t randValue;
	TypeApplianceCodeInfo(int32_t tappid, int32_t tkey_id, int32_t tir_code, int32_t tstatus)
	{
		appID = tappid;
		key_id = tkey_id;
		ir_code = tir_code;
		status = tstatus;
		randValue = 0;
	}
	~TypeApplianceCodeInfo()
	{

	}
};

class TypeApplianceInfo
{
public:
	int32_t appID;      // 家电ID
	int32_t ir_id;//红外伴侣ID
	int32_t ir_sub_id;//红外伴侣子ID
	TypeChar *name;//家电名称
	TypeChar *manufacturer;//厂家描述
	TypeChar *model; // 型号
	TypeChar *version;//版本号
	TypeChar *serial;
	int32_t roomID;//房间号
	int32_t type;   // 家电类型
	int32_t value;  //状态值
	int32_t addr;//地址
	TypeChar *value1;//状态值拓展
	TypeChar *config;//配置
	//int32_t band;//485协议中用到
	//还要添加一个指令列表
	TypeArrayList *codeList;
	int32_t saveValue;
	//添加临时变量
	int32_t randValue;
	TypeApplianceInfo(int32_t tappid, int32_t tir_id, int32_t tir_sub_id, char *tname, char *tmanufacturer, char *tmodel, char *tversion, char *tserial, int32_t troomid, int32_t ttype, int32_t tvalue, int32_t taddr, char *tvalue1, char *tconfig)
	{
		appID = tappid;
		saveValue = type = ttype;
		value = tvalue;
		ir_id = tir_id;
		ir_sub_id = tir_sub_id;
		roomID = troomid;
		addr = taddr;
		codeList = new TypeArrayList(ArrayTypeApplianceCodeInfo);
		name = new TypeChar(tname);
		manufacturer = new TypeChar(tmanufacturer);
		model = new TypeChar(tmodel);
		version = new TypeChar(tversion);
		serial = new TypeChar(tserial);
		value1 = new TypeChar(tvalue1);
		config = new TypeChar(tconfig);
		randValue = 0;
	}

	int32_t onSetStatus(int tkeyid)
	{
		if(type == APPLIANCE_TYPE_AIR_CONDITION)//只有空调有状态
		{
			if(tkeyid >= 0)
			{
				if(tkeyid <= 1)
				{
					value &= ~0x01;//开关
					value |= tkeyid;
				}
				else if(tkeyid <= 6)
				{
					value &= ~(0x1F << 7);//模式
					value |= (tkeyid - 1) << 7;
				}
				else if(tkeyid <= 0x0A)
				{
					value &= ~(0x0F << 3);//风速
					value |= (tkeyid - 7) << 3;
				}
				else if(tkeyid <= 0x0E)
				{
					//value &= ~(0x03 << 1);//
					if(tkeyid == 0x0B)
					{
						value |= 0x02;//上下扫风
					}
					else if(tkeyid == 0x0C)
					{
						value |= 0x04;//左右热风
					}
					else if(tkeyid == 0x0D)
					{
						value &= ~0x02;
					}
					else if(tkeyid == 0x0E)
					{
						value &= ~0x04;
					}
				}
				else
				{
					if((tkeyid >= 0x310) && (tkeyid <= 0x320))
					{
						value  &= ~(0x1F << 17);//制冷温度
						value  |= ((tkeyid - 0x300) << 17);
					}
					else if((tkeyid >= 0x410) && (tkeyid <= 0x420))
					{
						value  &= ~(0x1F << 12);//制热
						value  |= ((tkeyid - 0x400) << 12);
					}
				}
			}
		}
		return value;
	}

	TypeApplianceCodeInfo *onFindCMDWithCode(int32_t tcode)
	{
		TypeApplianceCodeInfo *tempApplianceCodeInfo = NULL;
		for(int i = 0; i < codeList->size(); ++ i)
		{
			tempApplianceCodeInfo = (TypeApplianceCodeInfo *)codeList->get(i);
			if(tempApplianceCodeInfo && (tempApplianceCodeInfo->ir_code == tcode))
			{
				break;
			}
			else
			{
				tempApplianceCodeInfo = NULL;
			}
		}
		return tempApplianceCodeInfo;
	}

	TypeApplianceCodeInfo *onFindCMDWithKeyID(int32_t keyid)
	{
		TypeApplianceCodeInfo *tempApplianceCodeInfo = NULL;
		for(int i = 0; i < codeList->size(); ++ i)
		{
			tempApplianceCodeInfo = (TypeApplianceCodeInfo *)codeList->get(i);
			if(tempApplianceCodeInfo && (tempApplianceCodeInfo->key_id == keyid))
			{
				break;
			}
			else
			{
				tempApplianceCodeInfo = NULL;
			}
		}
		return tempApplianceCodeInfo;
	}

	~TypeApplianceInfo()
	{
		if(name)
		{
			delete name;
			name = NULL;
		}
		if(manufacturer)
		{
			delete manufacturer;
			manufacturer = NULL;
		}
		if(model)
		{
			delete model;
			model = NULL;
		}
		if(version)
		{
			delete version;
			version = NULL;
		}
		if(serial)
		{
			delete serial;
			serial = NULL;
		}
		if(value1)
		{
			delete value1;
			value1 = NULL;
		}
		if(config)
		{
			delete config;
			config = NULL;
		}
		delete codeList;
	}
};

class TypeIRRemoteStatus
{
public:
	int32_t status;
	TypeIRRemoteStatus(int32_t tstatus)
	{
		mMemNewFreeCount++;
		status = tstatus;
	};
	~TypeIRRemoteStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypePIRStatus
{
public:
	int32_t status;
	int32_t power;
	int32_t zoneID;
	int32_t securityStatus;
	int32_t outDelayTime;
	//添加一个临时变量
	uint8_t lastID;
	TypePIRStatus(int32_t tstatus, int32_t tpower, int32_t zoneid, int32_t tsecuritystatus, int32_t toutdelaytime)
	{
		mMemNewFreeCount++;
		status = tstatus;
		securityStatus = tsecuritystatus;
		outDelayTime = toutdelaytime;
		power = tpower;
		zoneID = zoneid;
		lastID = 0;
	};
	~TypePIRStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeSmokeStatus
{
public:
	int32_t status;
	int32_t power;
	int32_t zoneID;
	//添加一个临时变量
	uint8_t lastID;
	TypeSmokeStatus(int32_t tstatus, int32_t tpower, int32_t zoneid)
	{
		mMemNewFreeCount++;
		status = tstatus;
		power = tpower;
		zoneID = zoneid;
		lastID = 0;
	};
	~TypeSmokeStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeDoorLockStatus
{
public:
	int32_t status;
	int32_t power;
	int32_t zoneID;
	//添加一个临时变量
	uint8_t lastID;
	TypeDoorLockStatus(int32_t tstatus, int32_t tpower, int32_t zoneid)
	{
		mMemNewFreeCount++;
		status = tstatus;
		power = tpower;
		zoneID = zoneid;
		lastID = 0;
	};
	~TypeDoorLockStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeSOSStatus
{
public:
	int32_t status;
	int32_t power;
	int32_t zoneID;
	//添加一个临时变量
	uint8_t lastID;
	TypeSOSStatus(int32_t tstatus, int32_t tpower, int32_t zoneid)
	{
		mMemNewFreeCount++;
		status = tstatus;
		power = tpower;
		zoneID = zoneid;
		lastID = 0;
	};
	~TypeSOSStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeFloodStatus
{
public:
	int32_t status;
	int32_t power;
	int32_t zoneID;
	//添加一个临时变量
	uint8_t lastID;
	TypeFloodStatus(int32_t tstatus, int32_t tpower, int32_t zoneid)
	{
		mMemNewFreeCount++;
		status = tstatus;
		power = tpower;
		zoneID = zoneid;
		lastID = 0;
	};
	~TypeFloodStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeDoorWindowStatus
{
public:
	int32_t status;
	int32_t power;
	int32_t zoneID;
	int32_t securityStatus;
	//添加一个临时变量
	uint8_t lastID;
	TypeDoorWindowStatus(int32_t tstatus, int32_t tpower, int32_t zoneid, int32_t tsecuritystatus)
	{
		mMemNewFreeCount++;
		status = tstatus;
		securityStatus = tsecuritystatus;
		power = tpower;
		zoneID = zoneid;
		lastID = 0;
	};
	~TypeDoorWindowStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeENV_DetectorStatus
{
public:
	int32_t tempSensorValue;
	int32_t humiSensorValue;
	int32_t illumSensorValue;
	int32_t pm25Value;
	int32_t airLevel;
	int32_t CO2Value;
	TypeENV_DetectorStatus(int32_t ttempsensorvalue, int32_t thumisensorvalue, int32_t tillumsensorvalue, int32_t tpm25value, int32_t tairelevel,int32_t tCO2Value)
	{
		mMemNewFreeCount++;
		tempSensorValue = ttempsensorvalue;
		humiSensorValue = thumisensorvalue;
		illumSensorValue = tillumsensorvalue;
		pm25Value = tpm25value;
		airLevel = tairelevel;
		CO2Value = tCO2Value;
	};
	~TypeENV_DetectorStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeWaterLeakStatus
{
public:
	int32_t value_status;//阀门状态
	int32_t flux;//当前流量
	int32_t threshold;//当前阀值
	int32_t alarm_status;//报警状态
	TypeWaterLeakStatus(int32_t tvalue_status, int32_t tflux, int32_t tthreshold, int32_t talarm_status)
	{
		mMemNewFreeCount++;
		value_status = tvalue_status;
		flux = tflux;
		threshold = tthreshold;
		alarm_status = talarm_status;
	}

	~TypeWaterLeakStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	};
};

class TypeGasArmStatus
{
public:
	int32_t value_status;//燃气臂状态
	TypeGasArmStatus(int32_t tvalue_status)
	{
		mMemNewFreeCount++;
		value_status = tvalue_status;
	}

	~TypeGasArmStatus()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};

class TypeClothesHanger
{
public:
	int32_t status;//智能晾衣架状态状态： bit0:灯光状态 0 关，1 开 bit1:消毒灯状态 0 关，1 开 bit2:风扇状态 0 关，1 开 bit3~4:电机状态 00 暂停，01 关，10开 bit5~15:保留
	TypeClothesHanger(int32_t tvalue)
	{
		if(status != tvalue)
		{
			status = tvalue;
		}
		mMemNewFreeCount++;
	}

	bool onSetLight(int32_t value)
	{
		if((status & 0x01) != (value & 0x01))
		{
			status &= ~0x01;
			status |= (value & 0x01);
			return TRUE;
		}
		else
		{
			return FALSE;
		}
	}

	int onGetLight()
	{
		if(status & 0x01)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	bool onSetDisinfection(int32_t value)
	{
		if((status & 0x02) != (value & 0x02))
		{
			status &= ~0x02;
			status |= (value & 0x02);
			return TRUE;
		}
		else
		{
			return FALSE;
		}
	}

	int onGetDisinfection()
	{
		if(status & 0x02)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	bool onSetAnion(int32_t value)
	{
		if((status & 0x04) != (value & 0x04))
		{
			status &= ~0x04;
			status |= (value & 0x04);
			return TRUE;
		}
		else
		{
			return FALSE;
		}
	}

	int onGetAnion()
	{
		if(status & 0x04)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	bool onSetUpDown(int32_t value)
	{
		if((status & 0x18) != (value & 0x18))
		{
			status &= ~0x18;
			status |= (value & 0x18);
			return TRUE;
		}
		else
		{
			return FALSE;
		}
	}

	int onGetUpDown()
	{
		return (status >> 3) & 0x3;
	}

	~TypeClothesHanger()
	{
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};

class TypeRS485Status
{
public:
	int32_t status;//
	int32_t saveStatus;
	TypeRS485Status(int32_t tstatus, int32_t tsavestatus)
	{
		status = tstatus;
		saveStatus = tsavestatus;
		mMemNewFreeCount++;
	}
	~TypeRS485Status()
	{
		mMemNewFreeCount--;
	}
};

class TypeOffLineVoiceStatus
{
public:
	int32_t status; //当前是否被唤醒
	int32_t wakeup_id;//唤醒词ID
	int32_t duration;//唤醒时长
	int32_t volume;//声音调节
	//临时变量
	int32_t lastCMDID;
	int32_t lastCMDValue;
	TypeOffLineVoiceStatus(int32_t tstatus, int32_t twakeupid, int32_t tduration, int32_t tvolume)
	{
		lastCMDValue = 0;
		status = tstatus;
		wakeup_id = twakeupid;
		duration = tduration;
		volume = tvolume;
		mMemNewFreeCount++;
	};

	~TypeOffLineVoiceStatus()
	{
		mMemNewFreeCount--;
	};
	void onToProcessCMD(int roomid, uint8_t *ubuff, uint8_t len);
};

typedef union
{
	TypeLightStatus *lightStatus;
	TypeScenePannelStatus *scenePannelStatus;
	TypeDimmingStatus *dimmingStatus;
	TypeSwitchStatus * switchStatus;
	TypeCurtainStatus *curtainStatus;
	TypeGasStatus *gasStatus;
	TypeIRRemoteStatus *irRemoteStatus;
	TypePIRStatus *pirStatus;
	TypeSmokeStatus *smokeStatus;
	TypeFloodStatus *floodStatus;
	TypeSOSStatus *sosStatus;
	TypeDoorLockStatus *doorLockStatus;
	TypeDoorWindowStatus *doorWindowStatus;
	TypeENV_DetectorStatus *env_detectorStatus;
	TypeWaterLeakStatus *waterLeakStatus;
	TypeGasArmStatus *gasArmStatus;
	TypeClothesHanger *clothesHangerStatus;
	TypeRS485Status *rs485Status;
	TypeOffLineVoiceStatus *offLineVoiceStatus;
}EnumSubInfo;

class TypeDeviceTypeInfo
{
public:
	int32_t roomID;
	TypeChar *name;
	int32_t devType;
	int32_t *pShortAddr;
	int32_t deviceID;
	int32_t subID;
	int32_t iconID;
	TypeChar *saveName;
	int32_t saveIconID;
	int32_t statusUpdateFlag;
	EnumSubInfo subInfo;
	TypeDeviceTypeInfo(int32_t xkey_id, int32_t xsub_id, int32_t xroom_id, int32_t xiconid, int32_t xsaveiconid, const char *xname, const char *xsavename, SubDeviceType subtype, int32_t inittype, void *obj);
	void onSetShortAddr(int32_t *shortaddr);
	uint32_t onSetStatus(int32_t status);
	bool onUpdateDeviceStatus(DeviceValueFlag valueflag, int32_t value);
	bool onSetDimmingParaValue(bool currentgateway, int32_t paravalue);
	bool onSetName(bool currentgateay, bool todeviceflag, char * newname);
	bool onSetIconID(bool currentgateay, bool todeviceflag, int32_t newiconid);
	bool onSetRoomID(int32_t newiconid);
	uint8_t onGetZoneID();
	int onGetStatus();
	int32_t onGetShortAddr();
	EnumSubInfo *onGetSubInfo();
	bool onUpdateTypeInfo(bool currentgateway, bool todeviceflag, TypeDeviceTypeInfo *devicetypeinfo);
	char *onGetStatusSql(TypeChar *sql);
	~TypeDeviceTypeInfo();
};

class TypeDBDeviceInfo
{
private:
	TypeArrayList *subList;
public:
	TypeDeviceAttr *attr;
	int32_t deviceID;
	int64_t gatewayID;
	int32_t devType;
	int64_t ieee;
	int32_t shortAddr;
	int64_t ieee_ex;
	int32_t shortAddr_ex;
	int64_t rgb;
	int64_t saveRgb;
	TypeDeviceOnLineStatus onLineFlag;
	int32_t protocol;
	int32_t protocolVer;
	int32_t targetScreen;
	TypeChar *serial;
	uint8_t int8SWVer[2];
	TypeChar *swVer;
	TypeChar *hwVer;
	TypeChar *manufacturer;
	int32_t subCount;
	//暂时创建的变量
	//心跳包需要一个事件来标识
	uint8_t lastCheckEndPoint;
	int32_t delayTime;
	int32_t saveCheckTime;
	int32_t randomValue;
	int32_t tempperature;
	int32_t illumination;
	uint32_t statusFlag;
	int8_t rssi;
	int8_t lqi;
	//添加标志 如果从模块无效就使用主模块弥补心跳
	bool checkSrcRouter;
	bool checkSrcRouterEx;
	long slaveTickTime;
	TypeDBDeviceInfo(int32_t tdeviceid, int64_t tgatewayid, int32_t devtype, int64_t tieee, int32_t tshortaddr, int64_t tieee_ex, int32_t tshortaddr_ex, int64_t trgb, int64_t tsavergb, int32_t tonline,
	int32_t tprotocol, int32_t tprotocolver, int32_t ttargetscrren, TypeDeviceAttr *tattr, char *tserial, char *tswver, char * thwver, char *tmanufacturer, int32_t tsub_device_count)
	{
		mMemNewFreeCount++;
		lastCheckEndPoint = 0;
		statusFlag = 0;
		slaveTickTime = 0;
		rssi = 0;
		lqi = 0;
		checkSrcRouter = false;
		checkSrcRouterEx = false;
		subCount = tsub_device_count;
		subList = new TypeArrayList(ArrayTypeDeviceTypeInfo);
		if(subCount > 0)
		{
			for(int i = 0; i <= subCount; ++ i)
			{
				subList->add(NULL);
			}
		}
		tempperature = -100;//初始化标识成无效值
		illumination = -100;//初始化标识成无效值
		deviceID = tdeviceid;
		devType = devtype;
		ieee = tieee;
		shortAddr = tshortaddr;
		ieee_ex = tieee_ex;
		shortAddr_ex = tshortaddr_ex;
		rgb = trgb;
		saveRgb = tsavergb;
		onLineFlag.value = tonline;
		protocol = tprotocol;
		protocolVer = tprotocolver;
		gatewayID = tgatewayid;
		serial = new TypeChar(tserial);
		//这里把这个软件版本解析一下，便于检查
		swVer = new TypeChar(tswver);
		int8SWVer[0] = 0;
		int8SWVer[1] = 0;
		if(strlen(tswver) >= 6)
		{
			int8SWVer[1] = (uint8_t)((tswver[1] - 0x30) & 0x0F);
			int8SWVer[1] = (uint8_t)(int8SWVer[1] * 10 + ((tswver[3] - 0x30) & 0x0F));
			int8SWVer[0] = (uint8_t)((tswver[4] - 0x30) & 0x0F);
			int8SWVer[0] = (uint8_t)(int8SWVer[0] * 10 + ((tswver[5] - 0x30) & 0x0F));
		}
		//这里解析一下
		hwVer = new TypeChar(thwver);
		targetScreen = ttargetscrren;
		attr = tattr;
		manufacturer = new TypeChar(tmanufacturer);
		if(onLineFlag.bits.status)
		{
			if(onCheckBattery(devtype))
			{
				delayTime = 24 * 3600;
			}
			else
			{
				delayTime = 100;
			}
		}
		else
		{
			delayTime = 0;
		}
		saveCheckTime = 0;
		randomValue = 0;
	};

	TypeDeviceTypeInfo *onGetSubInfo(int32_t subid)
	{
		if(subList && (subid <= subCount))
		{
			return (TypeDeviceTypeInfo *)subList->get(subid);
		}
		else
		{
			return NULL;
		}
	}

	bool onAddSubInfo(TypeDeviceTypeInfo *subinfo, int32_t subid)
	{
		if(subList && (subid <= subCount))
		{
			subList->update(subid, subinfo);
			return TRUE;
		}
		else
		{
			delete subinfo;
			mPrintf(Log_Error, "Error:设备下载，添加子设备发生严重错误！理论上不会发生");
			return FALSE;
		}
	}

	~TypeDBDeviceInfo()
	{
		if(mMemNewFreeCount > 0) mMemNewFreeCount--;
		delete swVer;
		delete hwVer;
		delete manufacturer;
		delete attr;
		delete subList;
		if(serial)
		{
			delete serial;
		}
	};
};

class TypeGasArmBingInfo
{
public:
	int64_t gasKeyID;
	int64_t gasArmKeyID;
	int32_t randValue;
	TypeGasArmBingInfo(int64_t tgaskeyid, int64_t tgasarmkeyid, int32_t trandvalue)
	{
		gasKeyID = tgaskeyid;
		gasArmKeyID = tgasarmkeyid;
		randValue = trandvalue;
	}
};

class TypeDevEventInfo
{
public:
	int64_t keyID;
	int32_t subID;
	EmunEventFlag eventFlag;
	int32_t delyaTime;
	TypeDevEventInfo(int64_t keyid, int32_t subid, EmunEventFlag flag)
	{
		keyID = keyid;
		if(subid < 1) subid = 1;
		subID = 1 << subid;
		delyaTime = 0;
		eventFlag = flag;
		mMemNewFreeCount++;
	}

	bool onCheckDelayTime()
	{
		delyaTime -= 1;
		if(delyaTime <= 0) return TRUE;
		else return FALSE;
	}

	~TypeDevEventInfo()
	{
		if(mMemNewFreeCount > 0) mMemNewFreeCount--;
	}
};

class TypeSceneCondInfo
{
private:
	int32_t delayed_time;
public:
	int64_t scene_cond_id;
	int64_t scene_id;
	int32_t type;//0=设备 1=场景 2=家电 3=定时执行 4=房间
	int32_t cond_type;//关=0 开=1
	TypeChar *cond_expre;//关系描述符 '&'/'|'
	int32_t device_id;//设备ID
	int32_t sub_id;//设备子ID
	int32_t action;//条件值
	TypeChar * action_desc;//条件值描述
	//临时变量
	int32_t randValue;
	TypeSceneCondInfo(int64_t tcondid, int64_t tsceneid, int32_t ttype, int32_t tcond_type, char * tcond_expre, int32_t tdevice_id, int32_t tsub_id, int32_t taction, char *taction_desc, int32_t tdelayed_time)
	{
		delayed_time = 0;
		scene_cond_id = tcondid;
		scene_id = tsceneid;
		cond_type = tcond_type;
		cond_expre = new TypeChar(tcond_expre);
		device_id = tdevice_id;
		action_desc = NULL;
		{
			action = taction;
			if(action_desc && (action_desc->onStringCMP(taction_desc) == FALSE))
			{
				delete action_desc;
				action_desc = NULL;
			}
			if(action_desc == NULL)
			{
				action_desc = new TypeChar(taction_desc);
			}
			type = ttype;
			if(type == 3)//定时执行
			{
				action = -1;
				if(strlen(action_desc->buff) == 5)//12:06
				{
					int tempIndex = 0;
					while(action_desc->ubuff[tempIndex] != 0)
					{
						if((action_desc->ubuff[tempIndex] >= '0') && (action_desc->ubuff[tempIndex] <= '9'))
						{
							switch(tempIndex)
							{
								case 0:action = action_desc->ubuff[tempIndex] - '0';break;
								case 1:action = action * 10 + (action_desc->ubuff[tempIndex] - '0');break;
								case 2:break;
								case 3:action = action * 6 + (action_desc->ubuff[tempIndex] - '0');break;
								case 4:action = action * 10 + (action_desc->ubuff[tempIndex] - '0');break;
								default:break;
							}
						}
						tempIndex++;
					}
				}
			}
		}
		sub_id = tsub_id;
		onSetDelayTime(tdelayed_time);
		randValue = 0;
	};

	bool onCheckActionDesc(char *actiondesc)
	{
		bool retBool = TRUE;
		if((actiondesc != NULL) && (action_desc != NULL))
		{
			if(action_desc->onStringCMP(actiondesc) == FALSE)
			{
				retBool = FALSE;
			}
		}
		return retBool;
	}

	int32_t onGetDelayTime()
	{
		return delayed_time;
	};
	bool onSetDelayTime(int32_t second)
	{
		int32_t tempValue = second * 1000;
		if(delayed_time != tempValue)
		{
			delayed_time = tempValue;
			return true;
		}
		return false;
	};
	~TypeSceneCondInfo()
	{
		delete cond_expre;
		delete action_desc;
		//mPrintf(Log_SerialPort, "cond release: sceneID=%d condID=%d", (int)scene_id, (int)scene_cond_id);
	};
};

class TypeSceneActionInfo
{
private:
	int32_t delayed_time;
public:
	int64_t scene_action_id;
	int64_t scene_id;
	int32_t type;//0=设备 1=场景 2=家电
	int32_t device_id;//设备ID
	int32_t sub_id;//设备子ID
	int32_t action_type;//1=开/0=关 执行动作
	int32_t action;//动作值
	TypeChar * action_desc;//动作描述
	//临时变量
	int32_t randValue;
	TypeSceneActionInfo(int64_t tactionid, int64_t tscene_id, int32_t ttype, int32_t tdevice_id, int32_t tsub_id, int32_t taction_type, int32_t taction, char *taction_desc, int32_t tdelayed_time)
	{
		scene_action_id = tactionid;
		scene_id = tscene_id;
		type = ttype;
		device_id = tdevice_id;
		sub_id = tsub_id;
		action_type = taction_type;
		action = taction;
		action_desc = new TypeChar(taction_desc);
		delayed_time = 0;
		onSetDelayTime(tdelayed_time);
		randValue = 0;
	};
	int32_t onGetDelayTime()
	{
		return delayed_time;
	};
	bool onSetDelayTime(int32_t second)
	{
		int32_t tempValue = second * 1000;
		if(delayed_time != tempValue)
		{
			delayed_time = tempValue;
			return true;
		}
		return false;
	};
	~TypeSceneActionInfo()
	{
		delete action_desc;
	};
};

class TypeSceneNameInfo
{
private:
	uint32_t periodInt;
	uint32_t enableTimeStart;
	uint32_t enableTimeEnd;
public:
	int64_t scene_id;
	TypeChar *name;
	int32_t room_id;
	int32_t icon_id;
	int32_t specialized;
	int32_t disabled;
	int32_t hidden;
	int32_t status;
	TypeChar *period;
	TypeChar *enabledTime;
	TypeArrayList *onActionInfoList;
	TypeArrayList *offActionInfoList;
	TypeArrayList *onCondInfoList;
	TypeArrayList *offCondInfoList;
	//添加一个临时变量
	int32_t randValue;
	TypeSceneNameInfo(int64_t tsceneid, char *pname, int32_t troomid, int32_t ticonid, int32_t tspecialized, int32_t tdisabled, int32_t thidden, int32_t tstatus, char *tpperiod, char *tenabled_time)
	{
		scene_id = tsceneid;
		name = new TypeChar(pname);
		room_id = troomid;
		icon_id = ticonid;
		specialized = tspecialized;
		disabled = tdisabled;
		hidden = thidden;
		status = tstatus;
		period = NULL;
		enabledTime = NULL;
		onSetPeriod(tpperiod);
		onSetEnableTime(tenabled_time);
		onActionInfoList = new TypeArrayList(ArrayTypeSceneActionInfo);
		offActionInfoList = new TypeArrayList(ArrayTypeSceneActionInfo);
		onCondInfoList = new TypeArrayList(ArrayTypeSceneCondInfo);
		offCondInfoList = new TypeArrayList(ArrayTypeSceneCondInfo);
		randValue = 0;
		mMemNewFreeCount++;
	};

	void onSetPeriod(char *value)
	{
		char tempChar = 0;
		periodInt = 0;
		if(period)
		{
			delete period;
		}
		period = new TypeChar(value);
		if(value != NULL)
		{
			if(strlen(value) > 0)
			{
				while(*value)
				{
					if((*value >= '0') && (*value <= '6'))
					{
						tempChar = *value - '0';
						periodInt |= 1 << tempChar;
					}
					value++;
				}
			}
		}
	}

	bool onCheckTimeOnCond(int32_t mins)
	{
		TypeSceneCondInfo *sceneCondInfo = NULL;
		for(int i = 0; i < onCondInfoList->size(); ++i)
		{
			sceneCondInfo = (TypeSceneCondInfo *)onCondInfoList->get(i);
			if(sceneCondInfo->type == 3)//它是一个定时执行
			{
				if(mins == sceneCondInfo->action)
				{
					return TRUE;
				}
			}
		}
		return FALSE;
	}

	bool onCheckTimeOffCond(int32_t mins)
	{
		TypeSceneCondInfo *sceneCondInfo = NULL;
		for(int i = 0; i < offCondInfoList->size(); ++i)
		{
			sceneCondInfo = (TypeSceneCondInfo *)offCondInfoList->get(i);
			if(sceneCondInfo->type == 3)//它是一个定时执行
			{
				if(mins == sceneCondInfo->action)
				{
					return TRUE;
				}
			}
		}
		return FALSE;
	}

	void onSetEnableTime(char *value)//如果当前时间不在设置允许的时间范围内，返回TRUE
	{
		enableTimeStart = 0;
		enableTimeEnd = 0;
		uint32_t index = 0;
		uint32_t len = 0;
		char tempChar = 0;
		if(enabledTime)
		{
			delete enabledTime;
		}
		enabledTime = new TypeChar(value);
		if(value != NULL)
		{
			len = strlen(value);
			if(len > 0)
			{
				while(index < len)
				{
					tempChar = value[index];
					if((tempChar >= '0') && (tempChar <= '9'))
					{
						tempChar -= '0';
						switch(index)
						{
							case 0:enableTimeStart = (uint32_t)(tempChar * 10);break;
							case 1:enableTimeStart = enableTimeStart + tempChar;break;
							case 3:enableTimeStart = enableTimeStart * 60 + tempChar * 10;break;
							case 4:enableTimeStart = enableTimeStart + tempChar;break;
							case 6:enableTimeEnd = (uint32_t)(tempChar * 10);break;
							case 7:enableTimeEnd = enableTimeEnd + tempChar;break;
							case 9:enableTimeEnd = enableTimeEnd * 60 + tempChar * 10;break;
							case 10:enableTimeEnd = enableTimeEnd + tempChar;break;
							default:
								break;
						}
					}
					index++;
				}
			}
		}
	}

	bool onSetDisabled(int32_t enable)
	{
		if(enable != 0)
		{
			enable = 1;
		}
		disabled = enable;
		return TRUE;
	}

	bool onGetDisabled(uint32_t mins, uint32_t week)
	{
		bool retBool = FALSE;
		bool checkEnable = FALSE;
		if(periodInt & (1 << week))
		{
			if((enableTimeStart == 0) && (enableTimeEnd == 0))
			{
				checkEnable = TRUE;
			}
			else if(enableTimeStart < enableTimeEnd)
			{
				if((mins >= enableTimeStart) && (mins < enableTimeEnd))
				{
					checkEnable = TRUE;
				}
			}
			else if(enableTimeStart > enableTimeEnd)
			{
				if(((mins >= enableTimeStart) && (mins < 1440/*24 * 60*/)) || (mins < enableTimeEnd))
				{
					checkEnable = TRUE;
				}
			}
		}
		if(checkEnable && (disabled == 0))
		{
			retBool = TRUE;
		}
		return retBool;
	}

	TypeSceneActionInfo * onFindSceneActionInfo(int32_t taction_type, int64_t taction_id)
	{
		TypeArrayList * actionList = NULL;
		TypeSceneActionInfo *retSceneActionInfo = NULL;
		if(taction_type)
		{
			actionList = onActionInfoList;
		}
		else
		{
			actionList = offActionInfoList;
		}
		for(int i = 0; i < actionList->size(); ++i)
		{
			retSceneActionInfo = (TypeSceneActionInfo *)actionList->get(i);
			if(retSceneActionInfo->scene_action_id == taction_id)
			{
				break;
			}
			else
			{
				retSceneActionInfo = NULL;
			}
		}
		return retSceneActionInfo;
	};

	bool onDeleteActionInfo(int64_t actionid)
	{
		bool retBool = false;
		TypeSceneActionInfo *retSceneActionInfo = onFindSceneActionInfo(0, actionid);
		if(retSceneActionInfo != NULL)
		{
			offActionInfoList->removeObject(retSceneActionInfo);
			retBool = true;
		}
		else
		{
			retSceneActionInfo = onFindSceneActionInfo(1, actionid);
			if(retSceneActionInfo != NULL)
			{
				onActionInfoList->removeObject(retSceneActionInfo);
				retBool = true;
			}
		}
		return retBool;
	};

	bool onDeleteCondInfo(int64_t condid)
	{
		bool retBool = FALSE;
		TypeSceneCondInfo *retCondInfo = onFindSceneCondInfo(0, condid);
		if(retCondInfo != NULL)
		{
			offCondInfoList->removeObject(retCondInfo);
			retBool = TRUE;
		}
		else
		{
			retCondInfo = onFindSceneCondInfo(1, condid);
			if(retCondInfo != NULL)
			{
				onCondInfoList->removeObject(retCondInfo);
				retBool = TRUE;
			}
		}
		return retBool;
	}

	TypeSceneCondInfo * onFindSceneCondInfo(int32_t tcond_type, int64_t tcond_id)
	{
		TypeArrayList *condList = NULL;
		TypeSceneCondInfo *retSceneCondInfo = NULL;
		if(tcond_type != 0)
		{
			condList = onCondInfoList;
		}else
		{
			condList = offCondInfoList;
		}
		for(int i = 0; i < condList->size(); ++i)
		{
			retSceneCondInfo = (TypeSceneCondInfo *)condList->get(i);
			if(retSceneCondInfo->scene_cond_id == tcond_id)
			{
				break;
			}else{
				retSceneCondInfo = NULL;
			}
		}
		return retSceneCondInfo;
	};
	~TypeSceneNameInfo()
	{
		delete name;
		delete onActionInfoList;
		delete offActionInfoList;
		delete onCondInfoList;
		delete offCondInfoList;
		if(period) delete period;
		if(enabledTime) delete enabledTime;
		if(mMemNewFreeCount > 0) mMemNewFreeCount--;
	};
};

class TypeGatewayInfo
{
public:
	int64_t gatewayID;
	int32_t roomID;
	int32_t modelInt;
	int32_t onLine;
	int32_t randValue;
	//TypeChar *gatewayName;
	//TypeChar *swVer;
	//TypeChar *serial;
	//TypeChar *language;
	//TypeChar *timeZone;
	//TypeChar *model;
	TypeGatewayInfo(int64_t tgatewayid, int troomid, int32_t tmodelint, int32_t tonline)
	{
		gatewayID = tgatewayid;
		roomID = troomid;
		modelInt = tmodelint;
		onLine = tonline;
		randValue = 0;
	}

	~TypeGatewayInfo()
	{

	}
};

class TypeCarriedSceneList
{
public:
	int64_t scene_id;
	int32_t totalTime;
	int32_t addTime;
	int32_t status;
	bool IsAutoExit;
	TypeArrayList *actionList;
	TypeCarriedSceneList(TypeSceneNameInfo *scenenameinfo)
	{
		scene_id = scenenameinfo->scene_id;
		totalTime = 0;
		addTime = 0;
		status = scenenameinfo->status;
		IsAutoExit = false;
		actionList = new TypeArrayList(ArrayTypeSceneActionInfo);
		if(status)//开
		{
			totalTime = 2000;
			if((scenenameinfo->offActionInfoList->size() == 0) && (scenenameinfo->offCondInfoList->size() == 0))
			{
                IsAutoExit = true;
			}
			for(int i = 0; i < scenenameinfo->onActionInfoList->size(); ++i)
			{
				tempSceneActionInfo = (TypeSceneActionInfo *)scenenameinfo->onActionInfoList->get(i);
				actionList->add(tempSceneActionInfo);
				if((tempSceneActionInfo->onGetDelayTime()) > totalTime)
				{
					totalTime = tempSceneActionInfo->onGetDelayTime() + 2000;
				}
			}
			//mPrintf(Log_NetWork, "scene action on cnt=%d ", actionList->size());
		}
		else
		{
			//关
			for(int i = 0; i < scenenameinfo->offActionInfoList->size(); ++i)
			{
				tempSceneActionInfo = (TypeSceneActionInfo *)scenenameinfo->offActionInfoList->get(i);
				actionList->add(tempSceneActionInfo);
			}
			//mPrintf(Log_NetWork, "scene action off cnt=%d ", actionList->size());
		}
	};
	bool onUpdateCarriedScene(TypeSceneNameInfo *scenenameinfo)
	{
		bool retBool = false;
		IsAutoExit = false;
		if(status != scenenameinfo->status)
		{
			addTime = 0;
			totalTime = 0;
			status = scenenameinfo->status;
			actionList->UnFreeClear();
			if(status)//开
			{
				if((scenenameinfo->offActionInfoList->size() == 0) && (scenenameinfo->offCondInfoList->size() == 0))
				{
					IsAutoExit = TRUE;
				}
				for(int i = 0; i < scenenameinfo->onActionInfoList->size(); ++i)
				{
					tempSceneActionInfo = (TypeSceneActionInfo *)scenenameinfo->onActionInfoList->get(i);
					actionList->add(scenenameinfo->onActionInfoList->get(i));
					if((tempSceneActionInfo->onGetDelayTime()) > totalTime)
					{
						totalTime = tempSceneActionInfo->onGetDelayTime() + 1000;//最大多延时1s再执行关
					}
				}
			}
			else
			{
				//关
				for(int i = 0; i < scenenameinfo->offActionInfoList->size(); ++i)
				{
					tempSceneActionInfo = (TypeSceneActionInfo *)scenenameinfo->offActionInfoList->get(i);
					actionList->add(scenenameinfo->offActionInfoList->get(i));
				}
			}
			retBool = true;
		}
		return retBool;
	};

	~TypeCarriedSceneList()
	{
		actionList->UnFreeClear();
		delete actionList;
	};
private:
	TypeSceneActionInfo *tempSceneActionInfo;
};

class TypeDataBase
{
private:
	int64_t gateway_id;
	int32_t status;
	int32_t dbGateway;
	int64_t ieee;
	int64_t ieee_ex;
	int32_t family_id;
	int64_t ex_panid;
	int64_t ex_panid_ex;
	int32_t panid;
	int32_t panid_ex;
	int32_t channel;
	int32_t channel_ex;
	int32_t room_id;
	int32_t cc2538Ver;
	int32_t chipType;
	sqlite3 *pSqlHandle;
	TypeChar *cc2538md5;
	TypeChar *serial;
	TypeChar *time_zone;
	TypeChar *language;
	TypeChar *name;
	TypeArrayList *gasArmBingList;
	bool onSetGatewaySqlValue(int32_t type, int64_t intvalue, int64_t intvalue_ex, const char *strvalue);
	//bool onDeleteGatewaySqlValue(int32_t type, int64_t intvalue);
public:
	TypeArrayList *devEventList;
	bool onDeleteDeviceInfoSqlValue(int32_t deviceid);
	bool onDeleteDataBase(const char *dbname, const char *fieldname, int64_t value);
	bool onDeleteApplianceCode(int32_t appid, int32_t keyid);
	bool onUpdateApplianceCodeInfo(TypeApplianceCodeInfo *appliancecodeinfo, int32_t type, int32_t value);
	bool onUpdateApplianceInfo(TypeApplianceInfo *applianceinfo, int32_t type, int32_t value);
	bool onUpdateApplianceInfo(TypeApplianceInfo *applianceinfo, int32_t type, const char *value);
	bool onUpdateSceneActionInfo(TypeSceneActionInfo *sceneactioninfo, int32_t type, int32_t value);
	bool onUpdateSceneActionInfo(TypeSceneActionInfo *sceneactioninfo, int32_t type, char *value);
	bool onUpdateSceneCondInfo(TypeSceneCondInfo *sceneacondinfo, int32_t type, int32_t value);
	bool onUpdateSceneCondInfo(TypeSceneCondInfo *sceneacondinfo, int32_t type, char *value);
	bool onUpdateSceneNameInfo(TypeSceneNameInfo *scenenameinfo, int32_t type, int32_t value);
	bool onUpdateSceneNameInfo(TypeSceneNameInfo *scenenameinfo, int32_t type, const char *value);
	bool onUpdateRoomInfo(TypeRoomInfo * roominfo, int32_t type, const char *value);
	bool onUpdateRoomInfo(TypeRoomInfo * roominfo, int32_t type, int32_t value);
	bool onUpdateSubDeviceInfo(TypeDeviceTypeInfo *decicetypeinfo, int32_t type, int64_t value);
	bool onUpdateSubDeviceInfo(TypeDeviceTypeInfo *devicetypeinfo, int32_t type, const char *value);
	bool onUpdateDeviceInfoSqlValue(TypeDBDeviceInfo *dbdeviceinfo, int32_t type, int64_t value);
	uint8_t onGetZoneID(int64_t tieee);
	bool onAddDevEventInfo(int64_t keyid, int32_t subid, EmunEventFlag event, int32_t delaytime);
	bool onClearDevEventInfo(int64_t keyid, int32_t subid, EmunEventFlag event);
	int64_t onGetGateway_ID();
	bool onSetGateway_ID(int64_t value);
	int32_t onGetCC2538Ver();
	bool onSetCC2538Ver(int32_t ver);
	//bool onSetCC2538md5(char *md5);
	bool onSetChipType(int32_t type);
	int32_t onGetChipType();
	int32_t onGetStatus();
	bool onSetStatus(int32_t value);
	int32_t onGetDBGateway();
	bool onSetDBGateway(int32_t value);
	char * onGetSerial();
	bool onSetSerial(char * strvalue);
	//bool onCMPSerial(const char *strvalue);
	int64_t onGetIEEE();
	bool onSetIEEE(int64_t value);
	int64_t onGetIEEE_EX();
	bool onSetIEEE_EX(int64_t value);
	char * onGetTime_Zone();
	bool onSetTime_Zone(char * strvalue);
	char * onGetLanguage();
	bool onSetLanguage(char * strvalue);
	bool onAddGasArmBingInfo(int64_t gaskey, int64_t gasarmkey, int32_t trandvalue);
	bool onDeleteGasArmBingInfo(int64_t gaskey, int64_t gasarmkey);
	bool onDeleteGasArmBingInfo(int32_t randvalue);
	bool onAlarmsGasArmBingInfo(int64_t gaskey);
	char * onGetName();
	bool onSetName(char * strvalue);
	int32_t onGetFamilyID();
	bool onSetFamilyID(int32_t value);
	int64_t onGetEx_PANID();
	bool onSetEx_PANID(int64_t value);
	//int64_t onGetEx_PANID_Ex();
	bool onSetEx_PANID_Ex(int64_t value);
	int32_t onGetPANID();
	bool onSetPANID(int32_t value);
	int32_t onGetPANID_Ex();
	bool onSetPANID_Ex(int32_t value);
	int32_t onGetChannel();
	bool onSetChannel(int32_t value);
	int32_t onGetChannel_Ex();
	bool onSetChannel_Ex(int32_t value);
	int32_t onGetRoomID();
	bool onSetRoomID(int32_t value);
	int32_t onGetGatewayType(int32_t type);
	bool onCloseDataBase();
	void onToString();
	TypeDataBase();
	~TypeDataBase();
};

class TypeDeviceList
{
public:
	TypeArrayList *roomList;
	TypeArrayList *sceneList;
	TypeArrayList *applianceList;
	TypeArrayList *carriedOutSceneList;
	TypeArrayList *dbDeviceInfoList;
	TypeArrayList *gatewayList;
	int mDownLoadFlag;
	int mDownLoadingFlag;
	TypeRoomInfo * onAddRoomInfo(TypeRoomInfo *roominfo, int32_t randvalue);
	TypeApplianceInfo * onAddAppliancesInfo(TypeApplianceInfo *applianceinfo, int32_t randvalue);
	TypeApplianceCodeInfo * onAddAppliancesCodeInfo(TypeApplianceInfo *applianceInfo, TypeApplianceCodeInfo *cmdinfo, int32_t randvalue);
	bool onDeleteApplianceCodeInfo(int32_t appid, int32_t keyid);
	TypeDBDeviceInfo *onCheckGatewayDeviceInfo(int32_t type, int64_t value);
	TypeDBDeviceInfo *onCheckFamilyDeviceInfo(int32_t type, int64_t value);
	bool onCheckDeviceEvent(TypeDBDeviceInfo *dbdeviceinfo, EmunEventFlag event);
	bool onCheckWhiteList(bool ismaster, int64_t ieee);
	TypeDeviceTypeInfo *onFindDeviceTypeInfo(int32_t deviceid, int32_t subid);
	bool onAddSubDeviceInfo(TypeDeviceTypeInfo *devicetypeinfo, int32_t randvalue);
	bool onAddDeviceInfoCheck(int32_t randvalue);
	bool onCheckLightToScene(int64_t sceneid);
	int32_t onGetBaseHeartCount();
	bool onSetHeartOK(bool ismater, TypeDBDeviceInfo *tempdbdeviceinfo);
	TypeDBDeviceInfo * onZoneIDFindDeviceInfo(uint8_t tzoneid, uint32_t shortaddr);
	TypeDBDeviceInfo * onZoneIDFindDeviceInfoEx(uint8_t tzoneid, uint32_t shortaddr);
	TypeDBDeviceInfo *onZclFindDeviceInfo(bool ismaster, TypeAFINComming *afinComming);
	TypeDBDeviceInfo *onAddDeviceInfo(TypeDBDeviceInfo *dbdeviceinfo, int32_t randvalue);
	bool onDeleteDeviceInfo(int32_t deviceid);
	TypeRoomInfo *onFindRoomInfo(int32_t roomid);
	void onDownLoadWithFlag(int32_t flag);
	void onDownLoadResetAll();
	void onDeleteGateway();
	void onDeleteRoomInfo(TypeRoomInfo *roominfo);
	void onDeleteApplianceInfo(TypeApplianceInfo *applianceinfo);
	void onPrintfRoomInfo();
	int32_t onSetDeviceStatus(TypeDBDeviceInfo * dbdeviceinfo, int32_t sub_id, int32_t status, bool broadcastflag);
	int32_t onSetApplianceStatus(int32_t id, int32_t key_id, const char *key_data);
	bool onDeleteSceneInfo(TypeSceneNameInfo * scenenameinfo);
	bool onCheckSceneCarried(int32_t ttype, int32_t device_id, int32_t subid, int32_t action);
	bool onCheckSceneCarried(int32_t ttype, int32_t device_id, int32_t subid, int32_t action, char *action_desc);
	TypeSceneNameInfo *onAddSceneInfo(TypeSceneNameInfo * scenenameinfo, int32_t randvalue);
	TypeSceneActionInfo * onAddSceneActionInfo(TypeSceneNameInfo *scenenameinfo, TypeSceneActionInfo *actioninfo, int32_t randvalue);
	TypeSceneCondInfo * onAddSceneCondInfo(TypeSceneNameInfo *scenenameinfo, TypeSceneCondInfo *condinfo, int32_t randvalue);
	TypeApplianceInfo *onFindApplianceInfo(int32_t appid);
	TypeApplianceInfo *onFindApplianceInfoKeyIDAndAddr(int32_t keyid, int32_t taddr);
	TypeApplianceInfo *onFindApplianceInfoTypeAndSerial(int32_t type, char *serial);
	TypeSceneNameInfo * onFindSceneInfo(int64_t sceneid);
	bool onResetDeviceDBInfo(TypeDBDeviceInfo *dbdeviceinfo);
	bool onSetSceneStatus(TypeSceneNameInfo *scenenameinfo, int newstatus, bool report);
	bool onSetDeviceStatusFlag(TypeDBDeviceInfo * dbdeviceinfo, int32_t subid, int32_t type);
	bool onClearDeviceStatusFlag(TypeDBDeviceInfo * dbdeviceinf);
	bool onAddGatewayInfo(TypeGatewayInfo *gatewayinfo, int32_t randvalue);
	int64_t onGetFamilyMasterGateway();
	bool onDisAlarmInfo(int64_t devid, int32_t type, bool broadcastflag);
	bool onCheckDevOnLine(bool ismater, uint32_t shortaddr);
	TypeDeviceList();
	~TypeDeviceList();
private:
};

class TypeRobotDataInfo
{
private:
	void onMemAdd()
	{
		mMemNewFreeCount++;
	};
public:
	uint16_t dataLen;
	uint16_t cmdID;
	TypeChar *dataBuff;
	uint8_t checkValue;
	TypeRobotDataInfo(int cmdid, char *data, int len)
	{
		checkValue = 0;
		dataLen = (uint16_t)len;
		cmdID = (uint16_t)cmdid;
		dataBuff = new TypeChar((uint32_t)(len + 8));
		dataBuff->ubuff[0] = 0xAA;
		dataBuff->ubuff[1] = (uint8_t)((len >> 8) & 0xFF);
		checkValue ^= dataBuff->ubuff[1];
		dataBuff->ubuff[2] = (uint8_t)(len & 0xFF);
		checkValue ^= dataBuff->buff[2];
		dataBuff->ubuff[3] = (uint8_t)((cmdid >> 8) & 0xFF);
		checkValue ^= dataBuff->buff[3];
		dataBuff->ubuff[4] = (uint8_t)(cmdid & 0xFF);
		checkValue ^= dataBuff->ubuff[4];
		for(int i = 0; i < len; ++i)
		{
			dataBuff->buff[i + 5] = data[i];
			checkValue ^= data[i];
		}
		dataBuff->buff[len + 5] = checkValue;
		dataBuff->ubuff[len + 6] = 0xFE;
		dataBuff->ubuff[len + 7] = 0xFE;
		onMemAdd();
	}

	TypeRobotDataInfo(uint8_t * buff, int len)
	{
		dataBuff = NULL;
		checkValue = 0;
		cmdID = 0;
		dataLen = 0;
		int status = 0;
		int saveDataLen = 0;
		uint8_t tempChar = 0;
		if(len < 0) len = 0;
		if(buff != NULL)
		{
			while(len--)
			{
				tempChar = *buff++;
				if((status > 0) && (status < 6))
				{
					checkValue ^= tempChar;
				}
				switch(status)
				{
					case 0:
						if(tempChar == 0xAA)
						{
							status = 1;
						}
						break;
					case 1:dataLen = tempChar; status = 2;break;
					case 2:
						dataLen = (dataLen << 8) + tempChar;
						status = 3;
						dataBuff = new TypeChar(dataLen);
						saveDataLen = dataLen;
						break;
					case 3:cmdID = tempChar; status = 4;break;
					case 4:cmdID = (cmdID << 8) + tempChar;status = 5;break;
					case 5:
						if(saveDataLen > 0)
						{
							dataBuff->buff[dataLen - saveDataLen] = tempChar;
							saveDataLen--;
						}
						else
						{
							status = 6;
						}
						break;
					case 6:
						if(checkValue != tempChar)
						{
							//分析错误
							cmdID = 0;
						}
						else
						{
							//解析

						}
						status = 7;
						break;
					default:
						status = 0;
						break;
				}
			}
		}
		onMemAdd();
	};
	~TypeRobotDataInfo()
	{
		mMemNewFreeCount--;
		if(dataBuff != NULL)
		{
			delete dataBuff;
		}
	};
};


class TypeThreadInfo
{

public:
	int *pNetFD;
	pthread_t threadID;
	long lastSaveTime;
	TypeChar *title;
	TypeChar *threadPara;
	bool outFlag;
	TypeThreadInfo(char *ttitle, char *tthreadpara)
	{
		pNetFD = 0;
		threadID = 0;
		lastSaveTime = 0;
		outFlag = false;
		title = new TypeChar(ttitle);
		threadPara = new TypeChar(tthreadpara);
		mMemNewFreeCount++;
	}

	bool onCloseReq()
	{
		if(pNetFD != 0)
		{
			if(*pNetFD != -1)
			{
				shutdown(*pNetFD, SHUT_RDWR);
				return TRUE;
			}
		}
		return FALSE;
	}

	~TypeThreadInfo()
	{
		delete title;
		delete threadPara;
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};

class TypeJniNotificationInfo
{
public:
	int32_t notifyID;
	int64_t lcmd1;
	int64_t lcmd2;
	int64_t lvalue;
	TypeChar *strValue;
	TypeJniNotificationInfo(int32_t tnotifyid, int64_t tlcmd1, int64_t tlcmd2, int64_t tlvalue, const char *tstrvalue)
	{
		notifyID = tnotifyid;
		lcmd1 = tlcmd1;
		lcmd2 = tlcmd2;
		lvalue = tlvalue;
		strValue = NULL;
		strValue = new TypeChar(tstrvalue);
		mMemNewFreeCount++;

	}
	~TypeJniNotificationInfo()
	{
		delete strValue;
		strValue = NULL;
		if(mMemNewFreeCount > 0)
		{
			mMemNewFreeCount--;
		}
	}
};


#endif /* DATATYPE_TYPEDEFINE_H_ */
