//
// Created by xia_w on 2017/12/14.
//

#include "../Main/WinobleMain.h"
#include "TypeZclProfile.h"
#include "../DataType/TypeDefine.h"

void TypeZclProfile::onMemAdd()
{
	mMemNewFreeCount++;
}
TypeZclProfile::TypeZclProfile(uint8_t *pbuff, uint8_t datalen)
{
	onMemAdd();
	toBuff = new TypeChar(datalen);
	toBuff->onAddUBuff(0, pbuff, datalen);
	toBuffLen = datalen;
	uint8_t tempLen = (uint8_t)(datalen - 3);

	zclHead = new TypeZclHead(pbuff[0]);
	pbuff++;
	if(zclHead->manuSpecific)
	{
		manufacturerID = onGetInt32Ex(pbuff, 2);
		pbuff += 2;
		if(tempLen > 2) tempLen -= 2;
	}
	seqNum = *pbuff++;
	cmdID = *pbuff++;
	attrubiteData = new TypeZclAttribute();
	attrubiteData->totalLen = tempLen;
	attrubiteData->totalBuff = new TypeChar(attrubiteData->totalLen);
	attrubiteData->totalBuff->onAddUBuff(0, pbuff, attrubiteData->totalLen);
	tempLen = 0;
	if(attrubiteData->totalLen > 0)
	{
		if(zclHead->type)
		{
			attrubiteData->attributeID = attrubiteData->totalBuff->ubuff[0];
			attrubiteData->dataBuffLen = (uint8_t)(attrubiteData->totalLen - 1);
			attrubiteData->dataBuff = new TypeChar(attrubiteData->dataBuffLen);
			attrubiteData->dataBuff->onAddUBuff(0, &attrubiteData->totalBuff->ubuff[1], attrubiteData->dataBuffLen);
		}
		else
		{
			attrubiteData->attributeID = onGetInt32Ex(attrubiteData->totalBuff->ubuff, 2);
			tempLen += 2;
			if(attrubiteData->totalLen > 2)
			{
				if(cmdID == ZCL_CMD_READ_RSP)
				{
					attrubiteData->status = attrubiteData->totalBuff->ubuff[tempLen++];
				}
				attrubiteData->dataType = attrubiteData->totalBuff->ubuff[tempLen++];
				if(attrubiteData->dataType == ZCL_DATATYPE_CHAR_STR)
				{
					tempLen++;
				}
				if((attrubiteData->totalLen - tempLen) >= 0)
				{
					attrubiteData->dataBuffLen = (uint8_t)(attrubiteData->totalLen - tempLen);
					attrubiteData->dataBuff = new TypeChar(attrubiteData->dataBuffLen);
					attrubiteData->dataBuff->onAddUBuff(0, &attrubiteData->totalBuff->ubuff[tempLen], attrubiteData->dataBuffLen);
				}
			}
		}
	}
}

TypeZclProfile::TypeZclProfile(bool manufacturerflag, uint8_t tcmdid, TypeZclAttribute *attribute)
{
	onMemAdd();
	zclHead = new TypeZclHead(ZCL_FRAME_TYPE_PROFILE_CMD, (uint8_t)manufacturerflag, ZCL_FRAME_CLIENT_SERVER_DIR, TRUE);
	if(manufacturerflag)
	{
		manufacturerID = ZCL_MANUSPCIFICID;
	}
	else
	{
		manufacturerID = 0;
	}
	seqNum = onGetZclSendSeq();
	cmdID = tcmdid;
	attrubiteData = attribute;
	//直接生成协议
	toBuffLen = 0;
	if(zclHead->manuSpecific)
	{
		toBuff = new TypeChar(attrubiteData->totalLen + 5);
	}
	else
	{
		toBuff = new TypeChar(attrubiteData->totalLen + 3);
	}
	toBuff->ubuff[toBuffLen++] = zclHead->onToData();
	if(zclHead->manuSpecific)
	{
		toBuff->onAddInt16Ex(toBuffLen, manufacturerID);
		toBuffLen+= 2;
	}
	toBuff->ubuff[toBuffLen++] = seqNum;
	toBuff->ubuff[toBuffLen++] = cmdID;

	toBuff->onAddUBuff(toBuffLen, attribute->totalBuff->ubuff, attribute->totalLen);
	toBuffLen += attribute->totalLen;
}

TypeZclProfile::~TypeZclProfile()
{
	delete zclHead;
	delete attrubiteData;
	delete toBuff;
	zclHead = NULL;
	attrubiteData = NULL;
	toBuff = NULL;
	if(mMemNewFreeCount > 0)
	{
		mMemNewFreeCount--;
	}
}

TypeAFINComming::TypeAFINComming(uint8_t *pbuff)
{
	mMemNewFreeCount++;
	groupID = onGetInt32Ex(pbuff, 2);
	pbuff += 2;
	clusterID = onGetInt32Ex(pbuff, 2);
	pbuff += 2;
	shortAddr = onGetInt32Ex(pbuff, 2);
	pbuff += 2;
	srcEndPoint = *pbuff++;
	desEndPoint = *pbuff++;
	wasBroadCast = *pbuff++;
	LinkQuality = (int8_t) *pbuff++;
	SecurityUse = *pbuff++;
	timeStamp = onGetInt32Ex(pbuff, 4);
	pbuff += 4;
	seqNum = *pbuff++;
	dataLen = *pbuff++;
	zclProfile = new TypeZclProfile(pbuff, dataLen);
	pbuff += dataLen;
	macSrcAddr = onGetInt32Ex(pbuff, 2);
	pbuff += 2;
	radius = (int8_t)(*pbuff);
}

int TypeAFINComming::onZclProcess(bool ismater)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onZclFindDeviceInfo(ismater, this);//通过短地址从数据库中查找设备
	//设置一下心跳
	if((zclProfile->cmdID == ZCL_CMD_DEFAULT_RSP) || (zclProfile->cmdID == ZCL_CMD_WRITE_RSP) || (zclProfile->cmdID == ZCL_CMD_READ_RSP))
	{
		pDeviceList->onSetHeartOK(ismater, tempDBDeviceInfo);//心跳
	}
	if((tempDBDeviceInfo == NULL) || ((zclProfile->zclHead->type == ZCL_FRAME_TYPE_PROFILE_CMD) && ((zclProfile->cmdID == ZCL_CMD_WRITE_RSP) || (zclProfile->cmdID == ZCL_CMD_DEFAULT_RSP))))
	{
		//如果是一些应答一命令也不做处理
		return 0;
	}
    //mPrintf(Log_DataBase, "air cluster&attr:%x %x %x",shortAddr,clusterID,zclProfile->attrubiteData->attributeID);

	if(zclProfile->attrubiteData->dataBuffLen == 0)
	{
		mPrintf(Log_Error, "Error $%04x$ AF cluster=%04x attributeID=%04x", shortAddr, clusterID, zclProfile->attrubiteData->attributeID);
		//对不支持的属性不再重复操作
		if(clusterID == CLUSTER_ID_LEVELCONTROL)
		{
			if((zclProfile->attrubiteData->attributeID == 0x0010) || (zclProfile->attrubiteData->attributeID == 0x0015) || (zclProfile->attrubiteData->attributeID == 0x0016))
			{
				if(tempDBDeviceInfo)
				{
					TypeDeviceTypeInfo *tempDevice = tempDBDeviceInfo->onGetSubInfo(srcEndPoint);
					if(tempDevice && (tempDevice->devType == SUB_DEVICE_TYPE_DIMMER))
					{
						//由于不支持，所以认定设置无效
						tempDevice->subInfo.dimmingStatus->saveParaValue = tempDevice->subInfo.dimmingStatus->paraValue;
						//更新数据库
						pDataBase->onUpdateSubDeviceInfo(tempDevice, SubDimmingSaveParaValue, tempDevice->subInfo.dimmingStatus->saveParaValue);
					}
				}

			}
		}
		return 0;//zf 属性不存在
	}

	DeviceAlarmType deviceAlarmType = DEVICE_ALARM_TYPE_INVALID;
	UpdateDeviceValueRequest updateRequest;
	DeviceValue *tempDeviceValue = NULL;
	TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
	tempDBDeviceInfo->rssi = radius;
	tempDBDeviceInfo->lqi = LinkQuality;
	tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(srcEndPoint);
	switch(clusterID)
	{
		case CLUSTER_ID_BASIC://basic cluster
		{
			switch(zclProfile->attrubiteData->attributeID)
			{
				case 0x0000://当前用于智能插座有能耗变化通知
				{
					if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH))
					{
						//电量 有变化  请求读取 (clusterid == 0x0B04) && (attributeid == 0x4000)
						pmMasterSerialPort->onReadAttribute((uint32_t)tempDeviceTypeInfo->onGetShortAddr(), (uint8_t)tempDeviceTypeInfo->subID, 0x0B04, 0x4000, 0);
					}
				}
					break;
				case 0x0001://软件版本号  版本号有需要再说
				{
					if(tempDBDeviceInfo->int8SWVer[ismater] != zclProfile->attrubiteData->dataBuff->ubuff[0])
					{
						tempDBDeviceInfo->int8SWVer[ismater] = zclProfile->attrubiteData->dataBuff->ubuff[0];
						//更新一下版本号
						if(tempDBDeviceInfo->swVer)
						{
							delete tempDBDeviceInfo->swVer;
						}
						tempDBDeviceInfo->swVer = new TypeChar(6);
						sprintf(tempDBDeviceInfo->swVer->buff, "V%01d.%01d%02d", (tempDBDeviceInfo->int8SWVer[1] % 100) / 10, tempDBDeviceInfo->int8SWVer[1] % 10, tempDBDeviceInfo->int8SWVer[0] % 100);
						onUpdateDeviceSoftVer(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->swVer->buff);
					}
					onUDPDevVerReturn(ismater, tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex, tempDBDeviceInfo->swVer->buff);
				}
					break;
				case 0x0005://设备类型 没有做任何处理
					break;
				case 0x4000://图标ID
				{
					if(tempDeviceTypeInfo != NULL)
					{
						pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubSaveIconID, (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2));
					}
				}
					break;
				case 0x4001://设备名称
				{
					if(tempDeviceTypeInfo != NULL)
					{
						TypeChar *sendNameBuff = new TypeChar(16);
						TypeChar *tempPrintfBuffs = new TypeChar();
						int32_t nameLen = onConverUnicodeString(tempDeviceTypeInfo->name->buff, sendNameBuff->buff, 8);
						TypeChar *namePrintf = new TypeChar(onPrintfUBuff(sendNameBuff->ubuff, nameLen, tempPrintfBuffs->buff));
						TypeChar *retPrintf = new TypeChar(onPrintfUBuff(zclProfile->attrubiteData->onGetDataBuff(), zclProfile->attrubiteData->dataBuffLen, tempPrintfBuffs->buff));
						mPrintf(ismater, "name=%s ret=%s ", namePrintf->buff, retPrintf->buff);
						delete namePrintf;
						delete retPrintf;
						delete tempPrintfBuffs;
						if(nameLen == (int32_t)zclProfile->attrubiteData->dataBuffLen)
						{
							if(sendNameBuff->onStringCMP((char *)zclProfile->attrubiteData->onGetDataBuff()))
							{
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubSaveName, tempDeviceTypeInfo->name->buff);
							}
						}
						delete sendNameBuff;
					}
				}
					break;
				case 0x4004:
				{
					if(DUALZIGBEECHIP) {
						//需要发送的数据
						TypeChar *tempAttributeBuf = new TypeChar(12);
						tempAttributeBuf->ubuff[0] = 11;
						tempAttributeBuf->ubuff[1] = (uint8_t) pDataBase->onGetChannel();
						tempAttributeBuf->onAddInt16(2, pDataBase->onGetPANID());
						tempAttributeBuf->onAddInt64(4, pDataBase->onGetEx_PANID());
						pmSlaveSerialPort->onWriteAttribute(shortAddr, srcEndPoint, clusterID, new TypeZclAttribute(0x4004, ZCL_DATATYPE_CHAR_STR, tempAttributeBuf->ubuff, 12), 0);
						delete tempAttributeBuf;
					}
				}
					break;
				case 0x4002://心跳包参数
				{
					//心跳包只检查数据库
					uint8_t tempHeartType = ZCL_DATATYPE_UINT16;
					if(zclProfile && zclProfile->attrubiteData)
					{
						tempHeartType = (uint8_t)zclProfile->attrubiteData->dataType;
					}
					//给个单播应答
					if(tempHeartType == ZCL_DATATYPE_UINT64)
					{
						//读取
						TypeChar *tempAttributeBuf = new TypeChar(8);
						tempAttributeBuf->ubuff[5] = (uint8_t)tempDBDeviceInfo->tempperature;//温度
						if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->roomID != 0))
						{
							TypeRoomInfo * tempRoomInfo = pDeviceList->onFindRoomInfo(tempDeviceTypeInfo->roomID);
							if(tempRoomInfo != NULL)
							{
								tempAttributeBuf->ubuff[5] = (uint8_t)tempRoomInfo->temp_value;//温度
							}
						}
						if(onIsGoodTemp(tempAttributeBuf->ubuff[5]) == FALSE)
						{
							tempAttributeBuf->ubuff[5] = 0;
						}
						tempAttributeBuf->ubuff[4] = (uint8_t)mTimerNow->tm_hour;//时
						tempAttributeBuf->ubuff[3] = (uint8_t)mTimerNow->tm_min;//分
						tempAttributeBuf->ubuff[2] = (uint8_t)mTimerNow->tm_sec;//秒
						tempAttributeBuf->ubuff[1] = 3;//超时次数
						tempAttributeBuf->ubuff[0] = (uint8_t)(pDeviceList->onGetBaseHeartCount() * 2);//延时值
						if(ismater)
						{
							pmMasterSerialPort->onWriteAttribute(shortAddr, 1, 0x0000, new TypeZclAttribute(0x4002, ZCL_DATATYPE_UINT64, tempAttributeBuf->ubuff, 8), 0);
						}
						else
						{
							if(DUALZIGBEECHIP) {
								pmSlaveSerialPort->onWriteAttribute(shortAddr, 1, 0x0000, new TypeZclAttribute(0x4002, ZCL_DATATYPE_UINT64, tempAttributeBuf->ubuff, 8), 0);
							}
						}
						delete tempAttributeBuf;
					}
				}
					break;
				case 0x4005:
				{
					//请求图标ID
					//首先要保证这个设备存在
					//发命令向服务器请求数据
					if(zclProfile->attrubiteData->dataBuffLen >= 2)
					{
						IconFontBitmapGetRequest  tempBitmapGet;
						tempBitmapGet.set_type(ICON_FONT_BITMAP_TYPE_ICON);
						tempBitmapGet.set_code((zclProfile->attrubiteData->dataBuff->ubuff[0] << 8) + zclProfile->attrubiteData->dataBuff->ubuff[1]);
						tempBitmapGet.set_device_id(tempDBDeviceInfo->deviceID);
						tempBitmapGet.set_sub_id(srcEndPoint);
						mfTCPCMDSend(CMD_ID_ICON_FONT_BITMAP_GET_REQ, tempBitmapGet.SerializeAsString().c_str(), tempBitmapGet.SerializeAsString().length());
					}
				}
					break;//用于写图标数据
				case 0x4006:break;//用于屏幕共享
				case 0x4007://用于标识网关的单双模块类型
				{
					if(ismater && (zclProfile->attrubiteData->dataBuffLen == 1))
					{
						uint8_t gwType = 0;
						if(DUALZIGBEECHIP)
						{
							gwType = 0xa2;
						}
						else
                        {
							gwType = 0xa1;
                        }
						pmMasterSerialPort->onWriteAttribute(shortAddr, srcEndPoint, clusterID, new TypeZclAttribute(0x4007, ZCL_DATATYPE_UINT8, &gwType, 1), 0);
					}
				}
					break;
				case 0x4008://红外距离做场景联动
				{
					if(ismater && tempDeviceTypeInfo && (zclProfile->attrubiteData->dataBuffLen == 2))
					{
						int32_t tempPirDistance = onGetInt32Ex(zclProfile->attrubiteData->dataBuff->ubuff, 2);
						if(tempPirDistance > 0x0100)
						{
							//查找这个设备的场景条件
							pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1000);
						}
					}
				}
					break;
				case 0x4009://智能门锁用到的特殊属性
				{
					if(ismater && tempDeviceTypeInfo && (zclProfile->attrubiteData->dataBuffLen > 1))
					{
						if(zclProfile->attrubiteData->dataBuff->ubuff[0] == 0x01)
						{
							//开锁信息数据上报
							if(zclProfile->attrubiteData->dataBuffLen == 5)
							{
								if(zclProfile->attrubiteData->dataBuff->ubuff[1] < 0x80)
								{
									//这里不是远程开锁。
									//用用户ID来执行场景
									pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, onGetInt32(&zclProfile->attrubiteData->dataBuff->ubuff[3], 2));
								}
							}
						}
						else if(zclProfile->attrubiteData->dataBuff->ubuff[0] == 0x02)
						{
							//报警信息上报
							if(zclProfile->attrubiteData->dataBuffLen == 2)
							{
								switch(zclProfile->attrubiteData->dataBuff->ubuff[1])
								{
									case 0x01:deviceAlarmType = DEVICE_ALARM_TYPE_DISMANTLED;break;
									case 0x02:deviceAlarmType = DEVICE_ALARM_TYPE_SYSTEM_LOCKED;break;
									case 0x03:deviceAlarmType = DEVICE_ALARM_TYPE_BE_COERCED;break;
									case 0x04:deviceAlarmType = DEVICE_ALARM_TYPE_UNCLOSED;break;
									case 0x05:deviceAlarmType = DEVICE_ALARM_TYPE_FAKE_LOCKED;break;
									case 0x06:deviceAlarmType = DEVICE_ALARM_TYPE_LOW_BATTERY;break;
									case 0x07:deviceAlarmType = DEVICE_ALARM_TYPE_DOOR_BELL_RANG;break;
									//case 0x08:deviceAlarmType = DEVICE_ALARM_TYPE_CLOSED;break;
									case 0x09:deviceAlarmType = DEVICE_ALARM_TYPE_ARM;break;
									case 0xF0:deviceAlarmType = DEVICE_ALARM_TYPE_SYSTEM_BE_RESET;break;
									default:break;
								}
							}
						}
					}
				}
					break;
				default:break;
			}
		}
			break;
		case CLUSTER_ID_POWERCONFIG://低电量
		{
			if(zclProfile->attrubiteData->attributeID == 0x0021)
			{
				if(onCheckBattery(tempDBDeviceInfo->devType))
				{
					tempDBDeviceInfo->delayTime = 24 * 3600;
					if(tempDBDeviceInfo->onLineFlag.bits.status == DEVICE_STATUS_OFFLINE)
					{
						//存储在本地
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, tempDBDeviceInfo->onLineFlag.onSetValue(DEVICE_STATUS_ONLINE, tempDBDeviceInfo->onLineFlag.bits.saveStatus));
						onUpdateOnOffLineRequest(tempDBDeviceInfo, DEVICE_STATUS_ONLINE);
					}
				}
				if(tempDeviceTypeInfo != NULL)
				{
					switch(tempDeviceTypeInfo->devType)
					{
						case SUB_DEVICE_TYPE_PIR:
							if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->power != zclProfile->attrubiteData->onGetDataBuff()[0])
							{
								tempDeviceTypeInfo->onGetSubInfo()->pirStatus->power = zclProfile->attrubiteData->onGetDataBuff()[0];
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->power);
								//同时也写入数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubPower, tempDeviceTypeInfo->onGetSubInfo()->pirStatus->power);
							}
							break;
						case SUB_DEVICE_TYPE_SMOKE:
							if(tempDeviceTypeInfo->onGetSubInfo()->smokeStatus->power != zclProfile->attrubiteData->onGetDataBuff()[0])
							{
								tempDeviceTypeInfo->onGetSubInfo()->smokeStatus->power = zclProfile->attrubiteData->onGetDataBuff()[0];
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->smokeStatus->power);
								//同时也写入数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubPower, tempDeviceTypeInfo->onGetSubInfo()->smokeStatus->power);
							}
							break;
						case SUB_DEVICE_TYPE_FLOOD:
							if(tempDeviceTypeInfo->onGetSubInfo()->floodStatus->power != zclProfile->attrubiteData->onGetDataBuff()[0])
							{
								tempDeviceTypeInfo->onGetSubInfo()->floodStatus->power = zclProfile->attrubiteData->onGetDataBuff()[0];
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->floodStatus->power);
								//同时也写入数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubPower, tempDeviceTypeInfo->onGetSubInfo()->floodStatus->power);
							}
							break;
						case SUB_DEVICE_TYPE_DOOR_LOCK:
							if(tempDeviceTypeInfo->onGetSubInfo()->doorLockStatus->power != zclProfile->attrubiteData->onGetDataBuff()[0])
							{
								tempDeviceTypeInfo->onGetSubInfo()->doorLockStatus->power = zclProfile->attrubiteData->onGetDataBuff()[0];
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->doorLockStatus->power);
								//同时也写入数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubPower, tempDeviceTypeInfo->onGetSubInfo()->doorLockStatus->power);
							}
							break;
						case SUB_DEVICE_TYPE_SOS:
							if(tempDeviceTypeInfo->onGetSubInfo()->sosStatus->power != zclProfile->attrubiteData->onGetDataBuff()[0])
							{
								tempDeviceTypeInfo->onGetSubInfo()->sosStatus->power = zclProfile->attrubiteData->onGetDataBuff()[0];
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->sosStatus->power);
								//同时也写入数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubPower, tempDeviceTypeInfo->onGetSubInfo()->sosStatus->power);
							}
							break;
						case SUB_DEVICE_TYPE_DOOR_WINDOW:
							if(tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->power != zclProfile->attrubiteData->onGetDataBuff()[0])
							{
								tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->power = zclProfile->attrubiteData->onGetDataBuff()[0];
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->power);
								//同时也写入数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubPower, tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->power);
							}
							break;
						default:break;
					}
				}
			}
		}
			break;
		case CLUSTER_ID_TEMPCONFIG://设备通用温度值
		{
			//设备温度值 需要转化成房间温度值
			if(zclProfile->attrubiteData->attributeID == 0x0000)
			{
				//更新一下设备温度
				int32_t tempTemp = (int16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
				if(onIsGoodTemp(tempTemp) && tempDBDeviceInfo->attr->bits.temp)
				{
					//if(tempTemp != tempDBDeviceInfo->tempperature)
					{
						tempDBDeviceInfo->tempperature = tempTemp;
						if(tempDeviceTypeInfo != NULL)
						{
							TypeRoomInfo *tempRoomInfo = pDeviceList->onFindRoomInfo(tempDeviceTypeInfo->roomID);
							if(tempRoomInfo != NULL)
							{
								//直接交给服务器处理
								UpdateRoomEnvRequest updateRoomEnvRequest;
								updateRoomEnvRequest.set_room_id(tempDeviceTypeInfo->roomID);
								updateRoomEnvRequest.set_env_mask(ROOM_ENV_MASK_TEMP);
								updateRoomEnvRequest.set_device_id(tempDBDeviceInfo->deviceID);
								updateRoomEnvRequest.set_temp(tempDBDeviceInfo->tempperature);
								mfTCPCMDSend(CMD_ID_ROOM_ENV_UPDATE_REQ, updateRoomEnvRequest.SerializeAsString().c_str(), updateRoomEnvRequest.SerializeAsString().length());
							}
						}
					}
				}
				else
				{
					mPrintf(Log_Error, "Error:错误的温度值=%d KEYID=%d ", tempDBDeviceInfo->tempperature, tempDBDeviceInfo->deviceID);
				}
				if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
				{
					pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_TEMPERATURE);
				}
			}
		}
			break;
		case CLUSTER_ID_ONOFF://on/off cluster
		{
			if((zclProfile->attrubiteData->attributeID == 0x0000) && (tempDeviceTypeInfo != NULL))
			{
				if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
				{
					pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ONOFF);
				}
				switch(tempDeviceTypeInfo->devType)
				{
					case SUB_DEVICE_TYPE_LIGHT:
					{
						if(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->status != zclProfile->attrubiteData->onGetDataBuff()[0])
						{
							if(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->onSetStatus(zclProfile->attrubiteData->onGetDataBuff()[0]))
							{
								if(onGetConnectFlag())
								{
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->status);
								}
								else
								{
									//没有网络的情况下，如果有多网关，考虑把数据远程给其它网关
									onUDPBroadcastDeviceStatus(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, zclProfile->attrubiteData->onGetDataBuff()[0]);
								}
							}
							//暂时不保存 为了响应的速度考虑pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDevStatus, tempDeviceTypeInfo->onGetSubInfo()->lightStatus->status);
						}
					}
						break;
					case SUB_DEVICE_TYPE_SWITCH:
					{
						if(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->status != zclProfile->attrubiteData->onGetDataBuff()[0])
						{
							if(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->onSetStatus(zclProfile->attrubiteData->onGetDataBuff()[0]))
							{
								if(onGetConnectFlag())
								{
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->status);
								}
								else
								{
									//没有网络的情况下，如果有多网关，考虑把数据远程给其它网关
									onUDPBroadcastDeviceStatus(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, zclProfile->attrubiteData->onGetDataBuff()[0]);
								}
							}
						}
					}
						break;
					case SUB_DEVICE_TYPE_GAS_ARM:
					{
						//if(tempDeviceTypeInfo->onGetSubInfo()->gasArmStatus->value_status != zclProfile->attrubiteData->onGetDataBuff()[0])
						{
							tempDeviceTypeInfo->onGetSubInfo()->gasArmStatus->value_status = zclProfile->attrubiteData->onGetDataBuff()[0];
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
							tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->gasArmStatus->value_status);
						}
					}
						break;
					case SUB_DEVICE_TYPE_CURTAIN:
					{
						//如果==0
					}
						break;
					default:break;
				}
			}
		}
			break;
		case CLUSTER_ID_LEVELCONTROL://level cluster
		{
			if(tempDeviceTypeInfo)
			{
				if((zclProfile->attrubiteData->attributeID == 0x0000) && (zclProfile->attrubiteData->dataBuffLen == 1))
				{
					if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
					{
						if(tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->onSetLevel(zclProfile->attrubiteData->onGetDataBuff()[0]))
						{
							if(onGetConnectFlag())
							{
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->status);
							}
							else
							{
								//没有网络的情况下，如果有多网关，考虑把数据远程给其它网关
								onUDPBroadcastDeviceStatus(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, zclProfile->attrubiteData->onGetDataBuff()[0]);
							}
						}
					}
					else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN)
					{
						if(tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->onSetLevel(zclProfile->attrubiteData->onGetDataBuff()[0]))
						{
							if(onGetConnectFlag())
							{
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_LEVEL);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->status);
							}
							else
							{
								//没有网络的情况下，如果有多网关，考虑把数据远程给其它网关
								onUDPBroadcastDeviceStatus(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, zclProfile->attrubiteData->onGetDataBuff()[0]);
							}
						}
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_LEVEL);
					}
				}
				else if((zclProfile->attrubiteData->attributeID == 0x0010) && (zclProfile->attrubiteData->dataBuffLen == 2))
				{
					if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
					{
						//调光参数返回
						int32_t tempPapa16 = onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
						if(((tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue >> 16) & 0xFFFF) != tempPapa16)
						{
							//更新数据库
							tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue &= 0xFFFF;
							tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue |= (tempPapa16 << 16) & 0xFFFF0000;
							//更新数据库
							pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDimmingSaveParaValue, tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
						}
						//再检查一次新的
						tempDeviceTypeInfo->onSetDimmingParaValue(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), tempDeviceTypeInfo->subInfo.dimmingStatus->paraValue);
					}
				}
				else if((zclProfile->attrubiteData->attributeID == 0x0015) && (zclProfile->attrubiteData->dataBuffLen == 1))
				{
					if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
					{
						//调光参数返回
						if((tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue & 0xFF) != zclProfile->attrubiteData->onGetDataBuff()[0])
						{
							//更新数据库
							tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue &= 0xFFFFFF00;
							tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue |= zclProfile->attrubiteData->onGetDataBuff()[0] & 0xFF;
							//更新数据库
							pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDimmingSaveParaValue, tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
						}
						//再检查一次新的
						tempDeviceTypeInfo->onSetDimmingParaValue(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), tempDeviceTypeInfo->subInfo.dimmingStatus->paraValue);
					}
				}
				else if((zclProfile->attrubiteData->attributeID == 0x0016) && (zclProfile->attrubiteData->dataBuffLen == 1))
				{
					if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
					{
						//调光参数返回
						if(((tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue >> 8) & 0xFF) != zclProfile->attrubiteData->onGetDataBuff()[0])
						{
							//更新数据库
							tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue &= 0xFFFF00FF;
							tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue |= (zclProfile->attrubiteData->onGetDataBuff()[0] << 8) & 0xFF00;
							//更新数据库
							pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDimmingSaveParaValue, tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
						}
						//再检查一次新的
						tempDeviceTypeInfo->onSetDimmingParaValue(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), tempDeviceTypeInfo->subInfo.dimmingStatus->paraValue);
					}
				}
			}
		}
			break;
		case 0x0019:
		{
			if(zclProfile->zclHead->type == ZCL_FRAME_TYPE_SPECIFIC_CMD)
			{
				if((zclProfile->attrubiteData->dataBuffLen == 8) && (zclProfile->cmdID == 1))//next image request
				{
					onUDPDevNextImageReq(ismater, zclProfile->seqNum, (uint16_t)onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[2], 2), tempDBDeviceInfo->deviceID, onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[4], 4));
				}
				else if((zclProfile->attrubiteData->dataBuffLen == 13) && (zclProfile->cmdID == 3))//Image block request
				{
					onUDPDevImageBlockReq(ismater, zclProfile->seqNum, (uint16_t)onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[2], 2), tempDBDeviceInfo->deviceID, onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[4], 4), onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[8], 4), zclProfile->attrubiteData->dataBuff->ubuff[12]);
				}
				else if((zclProfile->attrubiteData->dataBuffLen == 8) && (zclProfile->cmdID == 6))//Image block request
				{
					onUDPDevUpgradeEnd(ismater, zclProfile->seqNum, (uint16_t)onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[2], 2), tempDBDeviceInfo->deviceID, onGetInt32Ex(&zclProfile->attrubiteData->dataBuff->ubuff[4], 4));
				}
			}
		}
			break;
		case 0x0400://illuminance measurement
		{
			tempDeviceTypeInfo = pDeviceList->onFindDeviceTypeInfo(tempDBDeviceInfo->deviceID, 1);//环境质量都用的端点1，有点特殊
			if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_ENV_DETECTOR))
			{
				if(zclProfile->attrubiteData->attributeID == 0x0000)
				{
					uint16_t tempIllumValue = (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
					tempDBDeviceInfo->illumination = tempIllumValue;//把光照值保存到设备
					//光照
					if(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->illumSensorValue != tempIllumValue)
					{
						tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->illumSensorValue = tempIllumValue;
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_ILLUM_INTENSITY);
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->illumSensorValue);
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_ILLUM_INTENSITY);
					}
				}
			}
		}
			break;
		case 0x0402://temperature measurement
		{
			tempDeviceTypeInfo = pDeviceList->onFindDeviceTypeInfo(tempDBDeviceInfo->deviceID, 1);//环境质量都用的端点1，有点特殊
			if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_ENV_DETECTOR))
			{
				uint16_t status = (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
				if(zclProfile->attrubiteData->attributeID == 0x0000)
				{
					tempDBDeviceInfo->tempperature = status;//把温度值保存到设备
					int16_t int16Value = (int16_t)status;
					//温度 精度=1
					int16Value /= 10;
					if(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue != int16Value)
					{
						//查找一下所有场景条件
						TypeSceneNameInfo *tempSceneNameInfo = NULL;
						TypeSceneCondInfo *tempSceneCondInfo = NULL;
						for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
						{
							bool isProcess = FALSE;
							tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
							if(tempSceneNameInfo != NULL)
							{
								for(int j = 0; j < tempSceneNameInfo->onCondInfoList->size(); ++j)
								{
									tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->onCondInfoList->get(j);
									if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 0) && (tempSceneCondInfo->device_id == tempDeviceTypeInfo->deviceID) && (tempSceneCondInfo->sub_id == tempDeviceTypeInfo->subID))
									{
										//是这个设备
										if((tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP UP") && (tempSceneCondInfo->action > tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue) && (tempSceneCondInfo->action <= int16Value))
											|| (tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP DOWN") && (tempSceneCondInfo->action < tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue) && (tempSceneCondInfo->action >= int16Value)))
										{
											//执行这个场景
											isProcess = TRUE;
											pDeviceList->onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
											break;
										}
									}
								}
								if(isProcess == FALSE)
								{
									for(int j = 0; j < tempSceneNameInfo->offCondInfoList->size(); ++j)
									{
										tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->offCondInfoList->get(j);
										if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 0) && (tempSceneCondInfo->device_id == tempDeviceTypeInfo->deviceID) && (tempSceneCondInfo->sub_id == tempDeviceTypeInfo->subID))
										{
											//是这个设备
											if((tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP UP") && (tempSceneCondInfo->action > tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue) && (tempSceneCondInfo->action <= int16Value))
											   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP DOWN") && (tempSceneCondInfo->action < tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue) && (tempSceneCondInfo->action >= int16Value)))
											{
												//执行这个场景
												pDeviceList->onSetSceneStatus(tempSceneNameInfo, 0, TRUE);
												break;
											}
										}
									}
								}
							}
						}
						tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue = int16Value;
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_TEMPERATURE);//
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->tempSensorValue);
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_TEMPERATURE);
					}
				}
				else if(zclProfile->attrubiteData->attributeID == 0x4000)
				{
					//湿度 精度=1
					if(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue != status)
					{
						//查找一下所有场景条件
						TypeSceneNameInfo *tempSceneNameInfo = NULL;
						TypeSceneCondInfo *tempSceneCondInfo = NULL;
						for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
						{
							bool isProcess = FALSE;
							tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
							if(tempSceneNameInfo != NULL)
							{
								for(int j = 0; j < tempSceneNameInfo->onCondInfoList->size(); ++j)
								{
									tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->onCondInfoList->get(j);
									if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 0) && (tempSceneCondInfo->device_id == tempDeviceTypeInfo->deviceID) && (tempSceneCondInfo->sub_id == tempDeviceTypeInfo->subID))
									{
										//是这个设备
										if((tempSceneCondInfo->action_desc->onStringCMP((char *)"HUMI UP") && (tempSceneCondInfo->action > tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue) && (tempSceneCondInfo->action <= status))
										   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"HUMI DOWN") && (tempSceneCondInfo->action < tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue) && (tempSceneCondInfo->action >= status)))
										{
											//执行这个场景
											isProcess = TRUE;
											pDeviceList->onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
											break;
										}
									}
								}
								if(isProcess == FALSE)
								{
									for(int j = 0; j < tempSceneNameInfo->offCondInfoList->size(); ++j)
									{
										tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->offCondInfoList->get(j);
										if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 0) && (tempSceneCondInfo->device_id == tempDeviceTypeInfo->deviceID) && (tempSceneCondInfo->sub_id == tempDeviceTypeInfo->subID))
										{
											//是这个设备
											if((tempSceneCondInfo->action_desc->onStringCMP((char *)"HUMI UP") && (tempSceneCondInfo->action > tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue) && (tempSceneCondInfo->action <= status))
											   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"HUMI DOWN") && (tempSceneCondInfo->action < tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue) && (tempSceneCondInfo->action >= status)))
											{
												//执行这个场景
												pDeviceList->onSetSceneStatus(tempSceneNameInfo, 0, TRUE);
												break;
											}
										}
									}
								}
							}
						}
						tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue = status;
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_HUMIDITY);
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue);
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_HUMIDITY);
					}
				}
				else if(zclProfile->attrubiteData->attributeID == 0x4003)
				{
					//PM2.5
					if(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->pm25Value != status)
					{
						tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->pm25Value = status;
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_PM25);
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->pm25Value);
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_PM25);
					}
				}
				else if(zclProfile->attrubiteData->attributeID == 0x4004)
				{
					//空气质量等级
					if(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->airLevel != status)
					{
						tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->airLevel = status;
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_QUALITY);
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->airLevel);
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_QUALITY);
					}
				}
                else if(zclProfile->attrubiteData->attributeID == 0x4001)
                {
                    //CO2 浓度
                    if(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->CO2Value != status)
                    {
                        tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->CO2Value = status;
                        tempDeviceValue = updateRequest.add_values();
                        tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_CO2);
                        tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->CO2Value);
                    }
                    if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
                    {
                        pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_CO2);
                    }
                }
			}
		}
			break;
		case 0x0500://报警设备  上报状态  主模块
		{
			//判断一下  旧版本需要更新一个zoneID
			if(zclProfile->zclHead->type == ZCL_FRAME_TYPE_SPECIFIC_CMD)
			{
				if(onCheckBattery(tempDBDeviceInfo->devType))
				{
					tempDBDeviceInfo->delayTime = 24 * 3600;
					if(tempDBDeviceInfo->onLineFlag.bits.status == DEVICE_STATUS_OFFLINE)
					{
						//存储在本地
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, tempDBDeviceInfo->onLineFlag.onSetValue(DEVICE_STATUS_ONLINE, tempDBDeviceInfo->onLineFlag.bits.saveStatus));
						onUpdateOnOffLineRequest(tempDBDeviceInfo, DEVICE_STATUS_ONLINE);
					}
				}
				if(tempDeviceTypeInfo && (zclProfile->cmdID == 0))//目前只处理了这一条特殊命令
				{
					//暂时不判断这个，判断了也没个鸟用
					//TypeZoneIDInfo *tempZoneInfo = pDataBase->onCheckZoneIDInfo(zclProfile->attrubiteData->totalBuff->ubuff[3]);
					//if(tempZoneInfo != NULL)
					{
						uint16_t alarmStatus = (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->totalBuff->ubuff, 2);
						switch(tempDBDeviceInfo->devType)
						{
							case DEVICE_TYPE_GAS:
							{
								if(alarmStatus == 0x01)
								{
									//发送警报
									deviceAlarmType = DEVICE_ALARM_TYPE_GAS_GAS_LEAK;
									pDataBase->onAlarmsGasArmBingInfo(tempDBDeviceInfo->deviceID);
									pDeviceList->onCheckSceneCarried(0, tempDBDeviceInfo->deviceID, 1, 0);
								}
								else
								{
									pDeviceList->onCheckSceneCarried(0, tempDBDeviceInfo->deviceID, 1, 1);
								}
							}
								break;
							case DEVICE_TYPE_PIR:
							{
								if(tempDeviceTypeInfo->subInfo.pirStatus->lastID != zclProfile->seqNum)
								{
									tempDeviceTypeInfo->subInfo.pirStatus->lastID = zclProfile->seqNum;
									if(alarmStatus & 0x08)
									{
										//低电量 报警
										deviceAlarmType = DEVICE_ALARM_TYPE_LOW_BATTERY;
									}
									if((alarmStatus & 0x03) > 0)//红外感应进入
									{
										tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status = tempDeviceTypeInfo->onGetSubInfo()->pirStatus->outDelayTime;
										tempDeviceValue = updateRequest.add_values();
										tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_PIR_STATUS);
										tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status);
										//启动一个1s的定时器
										onTimerAdd((tempDeviceTypeInfo->deviceID << 8) + tempDeviceTypeInfo->subID, 1000, true, mfPIRAlarmCB, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID);
										if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->securityStatus)
										{
											//执行布防动作
											pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 2);
											deviceAlarmType = DEVICE_ALARM_TYPE_PIR_INTRUSION;
										}
										else
										{
											//if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status == 0)  每次都检查 ，如果当前 场景没有打开就打开这个场景
											{
												//从无人变成有人
												pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 0);
											}
										}
									}
									if(alarmStatus & 0x04)
									{
										//执行退出程序
										if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status == 0)
										{
											if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->securityStatus == 0)
											{
												//撤防退出
												pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1);
											}
											else
											{
												//布防下没有退出概念
											}
										}
									}
								}
							}
								break;
							case DEVICE_TYPE_SMOKE:
								if(tempDeviceTypeInfo->subInfo.smokeStatus->lastID != zclProfile->seqNum)
								{
									tempDeviceTypeInfo->subInfo.smokeStatus->lastID = zclProfile->seqNum;
									if(alarmStatus & 0x08)
									{
										deviceAlarmType = DEVICE_ALARM_TYPE_LOW_BATTERY;
									}
									if(alarmStatus & 0x01)
									{
										deviceAlarmType = DEVICE_ALARM_TYPE_SMOKE_OUTBREAK_OF_FIRE;
										pDeviceList->onCheckSceneCarried(0, tempDBDeviceInfo->deviceID, 1, 0);
									}
								}
								break;
							case DEVICE_TYPE_FLOOD:
								if(tempDeviceTypeInfo->subInfo.floodStatus->lastID != zclProfile->seqNum)
								{
									tempDeviceTypeInfo->subInfo.floodStatus->lastID = zclProfile->seqNum;
									if(alarmStatus & 0x08)
									{
										//低电量 报警
										deviceAlarmType = DEVICE_ALARM_TYPE_LOW_BATTERY;
									}
									if((alarmStatus & 0x02) ^ tempDeviceTypeInfo->onGetSubInfo()->floodStatus->status)
									{
										//水浸报警
										if(alarmStatus & 0x02)
										{
											//水浸报警
											deviceAlarmType = DEVICE_ALARM_TYPE_FLOOD_WATER_LEAK;
											tempDeviceTypeInfo->onGetSubInfo()->floodStatus->status = 1;
											pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1);
										}
										else
										{
											//水浸报警解除
											tempDeviceTypeInfo->onGetSubInfo()->floodStatus->status = 0;
											pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 2);
										}
										tempDeviceValue = updateRequest.add_values();
										tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_FLOOD_STATUS);
										tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->floodStatus->status);
									}
									if(alarmStatus & 0x01)
									{
										//sos 求救
										deviceAlarmType = DEVICE_ALARM_TYPE_FLOOD_SOS;
										pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 0);
									}
								}
								break;
							case DEVICE_TYPE_SOS:
								if(tempDeviceTypeInfo->subInfo.sosStatus->lastID != zclProfile->seqNum)
								{
									tempDeviceTypeInfo->subInfo.sosStatus->lastID = zclProfile->seqNum;
									if(alarmStatus & 0x08)
									{
										//低电量 报警
										deviceAlarmType = DEVICE_ALARM_TYPE_LOW_BATTERY;
									}

									if(alarmStatus & 0x01)
									{
										//sos 求救
										deviceAlarmType = DEVICE_ALARM_TYPE_SOS;
										pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 0);
									}
								}
								break;
							case DEVICE_TYPE_DOOR_WINDOW:
								if(tempDeviceTypeInfo->subInfo.doorWindowStatus->lastID != zclProfile->seqNum)
								{
									tempDeviceTypeInfo->subInfo.doorWindowStatus->lastID = zclProfile->seqNum;
									if(alarmStatus & 0x08)
									{
										//低电量 报警
										deviceAlarmType = DEVICE_ALARM_TYPE_LOW_BATTERY;
									}
									//if(((alarmStatus & 0x01) != tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->status) || (tempDeviceTypeInfo->onGetKey_id() == 410))
									{
										tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->status = alarmStatus & 0x01;
										if(tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->securityStatus)
										{
											if(alarmStatus & 0x01)
											{
												//布防门开
												deviceAlarmType = DEVICE_ALARM_TYPE_DOOR_WINDOW_OPEN;
												pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 2);
											}
											else if((alarmStatus & 0x01) == 0)
											{
												//布防门关
												deviceAlarmType = DEVICE_ALARM_TYPE_DOOR_WINDOW_CLOSE;
												pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 3);
											}
										}
										else
										{
											if(alarmStatus & 0x01)
											{
												//撤防门开
												pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 0);
											}
											else if((alarmStatus & 0x01) == 0)
											{
												//撤防门关
												pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1);
											}
										}
										tempDeviceValue = updateRequest.add_values();
										tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_DOOR_WINDOW_STATUS);
										tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->status);
									}
								}
								break;
							default:break;
						}
					}
					/*
					else
					{
						mPrintf(Log_Error, "Error: Produce an alarm but unfind zoneID!");
						//找不到我就要重新分配呀
					}
					 */
					//默认响应一下
					pmMasterSerialPort->onDefaultRspGeneric(srcEndPoint, shortAddr, clusterID, zclProfile->seqNum, zclProfile->cmdID, 0);
				}
				else if(zclProfile->cmdID == 1)
				{
					//写CIE 和 zoneID
					//需要注册IEEE地址
					TypeChar *tempSend = new TypeChar(8);
					tempSend->onAddInt64Ex(0, pDataBase->onGetIEEE());
					pmMasterSerialPort->onWriteAttributeGeneric(shortAddr, 0x01, 0x0500, new TypeZclAttribute(0x0010, ZCL_DATATYPE_IEEE_ADDR, tempSend->ubuff, 8), 0);
					//写zoneID
					tempSend->onClear();
					tempSend->ubuff[0] = 0x00;
					tempSend->ubuff[1] = (uint8_t)pDataBase->onGetZoneID(tempDBDeviceInfo->ieee);
					pmMasterSerialPort->onWriteZclCMD(shortAddr, 0x01, 0x0500, 0x00, tempSend->ubuff, 2, 0);
					delete tempSend;
				}
				else
				{
					mPrintf(Log_Error, "Error:0x0500 unprocess!special cmdID=%02x ", zclProfile->cmdID);
				}
			}
			else
			{
				switch(zclProfile->attrubiteData->attributeID)
				{
					case 0x0010:
					{
						//需要注册IEEE地址
						TypeChar *tempSend = new TypeChar(8);
						tempSend->onAddInt64Ex(0, pDataBase->onGetIEEE());
						pmMasterSerialPort->onWriteAttributeGeneric(shortAddr, 0x01, 0x0500, new TypeZclAttribute(0x0010, ZCL_DATATYPE_IEEE_ADDR, tempSend->ubuff, 8), 0);
						delete tempSend;
					}
						break;
					case 0x3000://gas value
					{
						if(tempDeviceTypeInfo)
						{
							if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_GAS)
							{
								uint16_t tempGasValue = (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
								if(tempDeviceTypeInfo->onGetSubInfo()->gasStatus->onSetStatus(tempGasValue))
								{
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_GAS_DENSITY);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->gasStatus->gasValue);
								}
								if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
								{
									pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_GAS_DENSITY);
								}
							}
						}
					}
						break;
					default:break;
				}
			}
		}
			break;
		case 0x0B04://主要用于智能插座相关
		{
			if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH))
			{
				if(zclProfile->attrubiteData->attributeID == 0x050B)
				{
					//当前能耗
					int16_t tempPower = (int16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
					if(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->currentPower != tempPower)
					{
						tempDeviceTypeInfo->onGetSubInfo()->switchStatus->currentPower = tempPower;
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_POWER);
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->currentPower);
					}
					if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
					{
						pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_POWER);
					}
				}
				else if(zclProfile->attrubiteData->attributeID == 0x4000)
				{
					uint16_t tempEnhanced = (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
					//更新今天的电量值
					if(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->onUpdateTadayEnergy(tempEnhanced))
					{
						//更新到服务器
						tempDeviceValue = updateRequest.add_values();
						tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_POWER_CONSUMPTION);
						tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->tadayEnergy);
					}
				}
			}
		}
			break;
		case CLUSTER_ID_PERSONAL://私有cluster
		{
			if(tempDeviceTypeInfo != NULL)
			{
				switch(zclProfile->attrubiteData->attributeID)
				{
					case 0x0000://RGB 属性
					{
						//得到一个64位的值
						if(zclProfile->attrubiteData->dataBuff && (zclProfile->attrubiteData->dataBuffLen == 8))
						{
							pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, ISaveRgb, onGetInt64Ex(zclProfile->attrubiteData->onGetDataBuff(), 8));
						}
						else
						{
							mPrintf(Log_Error, "Error:RGB 属性长度返回不正确! ");
						}
					}
						break;
					case 0x0001://设备开关属性上报
					{
						if(tempDeviceTypeInfo != NULL)
						{
							pDataBase->onClearDevEventInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, Event_Dev_Status);
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_LIGHT://灯光面板
								{
                                    //第一时间发送一个屏幕共享
                                    if(tempDBDeviceInfo->targetScreen && (tempDBDeviceInfo->attr->bits.screen == 0) && tempDBDeviceInfo->subCount)
                                    {
                                        //找到目标设备 并发送屏幕共享  并且只有手动按键的时候有效
                                        TypeChar *screenShareBuff = new TypeChar();
                                        //先得到这个设备
	                                    if((tempDBDeviceInfo->devType & 0x0F) == 4)
	                                    {
		                                    screenShareBuff->ubuff[1] = 1;//四路=1
	                                    }
	                                    else
	                                    {
		                                    screenShareBuff->ubuff[1] = 2;//两路=2  其它设备还没做
	                                    }
                                        uint8_t currentIndex = 3;
                                        if(screenShareBuff->ubuff[1] > 0)
                                        {
                                            for(int (indi) = 1; (indi) <= tempDBDeviceInfo->subCount; ++(indi))
                                            {
                                                TypeDeviceTypeInfo *tempTypeInfo = tempDBDeviceInfo->onGetSubInfo(indi);
                                                if(tempTypeInfo)
                                                {
                                                    //得到状态
                                                    if((int)srcEndPoint == indi)
                                                    {
                                                        screenShareBuff->ubuff[2] |= ((zclProfile->attrubiteData->onGetDataBuff()[0] ? 1 : 0) << (indi - 1)) & 0x0F;
                                                    }
                                                    else
                                                    {
                                                        screenShareBuff->ubuff[2] |= ((tempTypeInfo->onGetStatus() ? 1 : 0) << (indi - 1)) & 0x0F;
                                                    }
                                                    //得到图标ID
                                                    screenShareBuff->ubuff[currentIndex++] = (uint8_t)tempTypeInfo->iconID;
                                                    //得到名称长度
                                                    screenShareBuff->ubuff[currentIndex] = (uint8_t)onGetUtf8NameLen(tempTypeInfo->name->buff);
	                                                currentIndex++;
                                                    //得到数据区
                                                    screenShareBuff->onAddUBuff(currentIndex, (uint8_t *)tempTypeInfo->name->buff, screenShareBuff->ubuff[currentIndex -1]);
                                                    currentIndex += screenShareBuff->ubuff[currentIndex -1];
                                                }

                                            }
                                            screenShareBuff->ubuff[0] = (uint8_t)(currentIndex - 1);
	                                        //如果目标屏幕是本地的设备就直接发送给本地设备
	                                        //先查找有没有这个设备
	                                        TypeDBDeviceInfo *tempTargetDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempDBDeviceInfo->targetScreen);
	                                        if(tempTargetDBDeviceInfo)
	                                        {
		                                        if(tempTargetDBDeviceInfo->attr->bits.screen)
		                                        {
			                                        pmMasterSerialPort->onWriteAttribute((uint32_t)tempTargetDBDeviceInfo->shortAddr, (uint8_t)1, 0x0000, new TypeZclAttribute(0x4006, ZCL_DATATYPE_CHAR_STR, screenShareBuff->ubuff, currentIndex), 0);
		                                        }
	                                        }
	                                        else
	                                        {
		                                        //发送给设备
		                                        onSendDevNoticeEvent(tempDBDeviceInfo->targetScreen, 1, DEV_EVENT_SCREEN_SHARE, screenShareBuff->ubuff, currentIndex);
	                                        }
                                        }
                                        delete screenShareBuff;
                                    }
                                    if(tempDeviceTypeInfo != NULL)
                                    {
                                        if(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->sceneID != 0)
                                        {
                                            //执行所关联的场景
	                                        pDeviceList->onSetSceneStatus(pDeviceList->onFindSceneInfo(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->sceneID), zclProfile->attrubiteData->onGetDataBuff()[0], TRUE);
                                        }
                                        else
                                        {
                                            if(zclProfile->attrubiteData->onGetDataBuff()[0])
                                            {
                                                //开
                                                pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1);
                                            }
                                            else
                                            {
                                                //关
                                                pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 0);
                                            }
                                        }
                                    }
                                    tempDeviceTypeInfo->onGetSubInfo()->lightStatus->needSetStatus = zclProfile->attrubiteData->onGetDataBuff()[0];
								}
									break;
								case SUB_DEVICE_TYPE_SWITCH://智能插座
								{
									if(zclProfile->attrubiteData->onGetDataBuff()[0])
									{
										//开
										pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1);
									}
									else
									{
										//关
										pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 0);
									}
									tempDeviceTypeInfo->onGetSubInfo()->switchStatus->needSetStatus = zclProfile->attrubiteData->onGetDataBuff()[0];
								}
								break;
								default:break;
							}
						}
					}
						break;
					case 0x0002://通用设备光照值
					{
						//设备通用光照值  与温度不同  需要降低上报频率
						int32_t saveIllumValue = (uint16_t)onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
						if ((tempDBDeviceInfo->attr->bits.illu) && (saveIllumValue != tempDBDeviceInfo->illumination))
						{
							tempDBDeviceInfo->illumination = saveIllumValue;
							TypeRoomInfo *tempRoomInfo = pDeviceList->onFindRoomInfo(tempDeviceTypeInfo->roomID);
							if(tempRoomInfo != NULL)
							{
								UpdateRoomEnvRequest updateRoomEnvRequest;
								updateRoomEnvRequest.set_room_id(tempDeviceTypeInfo->roomID);
								updateRoomEnvRequest.set_env_mask(ROOM_ENV_MASK_ILLUM);
								updateRoomEnvRequest.set_device_id(tempDBDeviceInfo->deviceID);
								updateRoomEnvRequest.set_illum(tempDBDeviceInfo->illumination);
								mfTCPCMDSend(CMD_ID_ROOM_ENV_UPDATE_REQ, updateRoomEnvRequest.SerializeAsString().c_str(), updateRoomEnvRequest.SerializeAsString().length());
							}
						}
						if(zclProfile->cmdID == ZCL_CMD_REPORT)//如果是主动上找才需要应答
						{
							pDeviceList->onSetDeviceStatusFlag(tempDBDeviceInfo, srcEndPoint, DEVICE_VALUE_FLAG_ENV_ILLUM_INTENSITY);
						}
					}
						break;
					case 0x0006:
					{
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR))
						{
							uint32_t status = onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 4);
							int32_t tempAlarmStatus = (status >> 24) & 0xFF;
							int32_t  tempValue = status & 0xFFFF;
							int32_t  tempStatus = (status >> 16) & 0xFF;
							if(tempStatus == 0x0A) tempStatus = 1;
							else if(tempStatus == 0x0B) tempStatus = 0;
							else if(tempStatus == 0x0C) tempStatus = 2;
							else tempStatus = 3;
							if(tempAlarmStatus == 0x03)
							{
								//更新流量值
								if(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->flux != tempValue)
								{
									tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->flux = tempValue;
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_WATER_FLUX);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->flux);
								}
							}
							else
							{
								//更新报警值
								if(tempAlarmStatus == 0)
								{
									//更新阀值
									if(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->threshold != tempValue)
									{
										tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->threshold = tempValue;
										tempDeviceValue = updateRequest.add_values();
										tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_LEVEL);
										tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->threshold);
									}
								}
								if(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->alarm_status != tempAlarmStatus)
								{
									//更新报警状态
									tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->alarm_status = tempAlarmStatus;
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ALARM_STATUS);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->alarm_status);
									if(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->alarm_status == 1)
									{
										deviceAlarmType = DEVICE_ALARM_TYPE_WATER_PIPE_BURST;
									}
									else if(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->alarm_status == 2)
									{
										deviceAlarmType = DEVICE_ALARM_TYPE_FLOOD_WATER_LEAK;
									}
									else
									{
										//解除报警
										DeviceAlarmReleasedNotification releaseRequest;
										releaseRequest.set_family_id(pDataBase->onGetFamilyID());
										releaseRequest.set_device_id(tempDeviceTypeInfo->deviceID);
										releaseRequest.set_sub_id(tempDeviceTypeInfo->subID);
										releaseRequest.set_sub_type(tempDeviceTypeInfo->devType);
										mfTCPCMDSend(CMD_ID_DEVICE_ALARM_RELEASE_REQ, releaseRequest.SerializeAsString().c_str(), releaseRequest.SerializeAsString().length());
									}
								}
							}
							if(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->value_status != tempStatus)
							{
								//更新阀值状态
								tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->value_status = tempStatus;
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->waterLeakStatus->value_status);
							}
						}
					}
						break;
					case 0x0008://红外学习指令返回
					{
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_IR_REMOTE))
						{
							//目前只有红外伴侣用到了这个命令
							uint64_t tempUInt64 = onGetInt64Ex(zclProfile->attrubiteData->onGetDataBuff(), 8);
							//先找到这个家电和指令信息
							TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo((int32_t)((tempUInt64 >> 32) & 0xFFFFFFFF));
							if(tempApplianceInfo)
							{
								TypeApplianceCodeInfo *tempApplianceCodeInfo = tempApplianceInfo->onFindCMDWithCode((int32_t)(tempUInt64 & 0xFFFF));
								if(tempApplianceCodeInfo)
								{
									//更新服务器家电学习指令的状态
									UpdateApplianceCmdStatusRequest updateApplianceCmdStatusRequest;
									if((int32_t)((tempUInt64 >> 16) & 0xFFFF) == 0)
									{
										tempApplianceCodeInfo->status = 2;//修改状态成学习完成
									}
									else
									{
										tempApplianceCodeInfo->status = 3;//学习失败
									}
									updateApplianceCmdStatusRequest.set_ir_code(tempApplianceCodeInfo->ir_code);
									updateApplianceCmdStatusRequest.set_key_id(tempApplianceCodeInfo->key_id);
									updateApplianceCmdStatusRequest.set_appliance_id((int32_t)((tempUInt64 >> 32) & 0xFFFFFFFF));
									updateApplianceCmdStatusRequest.set_status(tempApplianceCodeInfo->status);
									updateApplianceCmdStatusRequest.set_reason((int32_t)((tempUInt64 >> 16) & 0xFFFF));
									mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_UPDATE_REQ, updateApplianceCmdStatusRequest.SerializeAsString().c_str(), updateApplianceCmdStatusRequest.SerializeAsString().length());
								}
							}
						}
					}
						break;
					case 0x0009://红外控制返回
					{
						//这里要更新一下家电的状态
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_IR_REMOTE))
						{
							//目前只有红外伴侣用到了这个命令
							uint64_t tempUInt64 = onGetInt64Ex(zclProfile->attrubiteData->onGetDataBuff(), 8);
							//先找到这个家电和指令信息
							TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo((int32_t)((tempUInt64 >> 32) & 0xFFFFFFFF));
							if(tempApplianceInfo)
							{
								TypeApplianceCodeInfo *tempApplianceCodeInfo = tempApplianceInfo->onFindCMDWithCode((int32_t)(tempUInt64 & 0xFFFF));
								if(((int32_t)((tempUInt64 >> 16) & 0xFFFF) == 0) && tempApplianceCodeInfo && (tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION))
								{
									tempApplianceInfo->onSetStatus(tempApplianceCodeInfo->key_id);
									//只有空调需要更新家电状态
									ApplianceValueChangedNotification valueChangedNotification;
									valueChangedNotification.set_appliance_id((int32_t)((tempUInt64 >> 32) & 0xFFFFFFFF));
									valueChangedNotification.set_value(tempApplianceInfo->value);
									mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
								}
							}
						}
					}
						break;
					case 0x0011://RS485协议数据上报
					case 0x000C://RS485协议数据上报
					{
						//mPrintf(Log_DataBase, "------air state in------");
						RS485Profile *tempRS485Profile = new RS485Profile(zclProfile->attrubiteData->attributeID, zclProfile->attrubiteData->onGetDataBuff(), zclProfile->attrubiteData->dataBuffLen, tempDeviceTypeInfo);
						delete tempRS485Profile;
					}
						break;
					case 0x000D://RS485参数返回
					{
						if(tempDeviceTypeInfo)
						{
							if(zclProfile->attrubiteData->dataBuffLen == 8)
							{
								tempDeviceTypeInfo->onGetSubInfo()->rs485Status->saveStatus = (int32_t)onGetInt64Ex(zclProfile->attrubiteData->onGetDataBuff(), 8);
								//保存到数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubRS485Para, tempDeviceTypeInfo->onGetSubInfo()->rs485Status->saveStatus);
								//读取一下当前是否带命令监控功能
								//延时读取一下监控命令表
								pmMasterSerialPort->onReadAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, 0x10, 200);
							}
						}
					}
						break;
					case 0x000F://智能晾衣架状态返回
					{
						if(tempDeviceTypeInfo)
						{
							int32_t tempStatus = onGetInt32Ex(zclProfile->attrubiteData->onGetDataBuff(), 2);
							if(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onSetLight(tempStatus))
							{
								//灯开关状态不一样
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onGetLight());
							}
							if(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onSetDisinfection(tempStatus))
							{
								//灯开关状态不一样
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_DISINFECTION);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onGetDisinfection());
							}
							if(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onSetAnion(tempStatus))
							{
								//灯开关状态不一样
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ANION);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onGetAnion());
							}
							if(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onSetUpDown(tempStatus))
							{
								//灯开关状态不一样
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_UPDOWN);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->clothesHangerStatus->onGetUpDown());
							}
						}
					}
						break;
					case 0x0010://返回当前485是否有命令监控
					{
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_RS485_TRANSFER) && (zclProfile->attrubiteData->dataBuffLen > 0))
						{
							//查找这个家电
							TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfoKeyIDAndAddr(tempDeviceTypeInfo->deviceID, 0);
							if(tempApplianceInfo)
							{
								if((tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN) && tempApplianceInfo->manufacturer && (strlen(tempApplianceInfo->manufacturer->buff) == 8))
								{
									if(zclProfile->attrubiteData->dataBuff->ubuff[0] == 0)
									{
										//设置一下监控
										TypeElectricCurtainBS *tempElectricCurtainbs = new TypeElectricCurtainBS(0x0a01, 0x02, 0);
										TypeChar *sendChars = new TypeChar((uint32_t)(tempElectricCurtainbs->buffLen + 1));
										sendChars->ubuff[0] = (uint8_t)(tempElectricCurtainbs->buffLen);
										tempElectricCurtainbs->buff->ubuff[0] = 1;
										sendChars->onAddUBuff(1, tempElectricCurtainbs->buff->ubuff, tempElectricCurtainbs->buffLen);
										pmMasterSerialPort->onWriteAttribute((uint32_t)shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, sendChars->ubuff, (uint8_t)(tempElectricCurtainbs->buffLen + 1)), 0);
										delete tempElectricCurtainbs;
										delete sendChars;
										//再延时读取一下
										pmMasterSerialPort->onReadAttribute((uint32_t)shortAddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 2000);
									}
									else
									{
										TypeElectricCurtainBS *tempElectricCurtainbs = new TypeElectricCurtainBS(0x0a01, 0x02, 0);
										TypeChar *sendChars = new TypeChar((uint32_t)(tempElectricCurtainbs->buffLen + 1));
										sendChars->ubuff[0] = (uint8_t)(tempElectricCurtainbs->buffLen);
										tempElectricCurtainbs->buff->ubuff[0] = 1;
										if((tempElectricCurtainbs->buffLen == zclProfile->attrubiteData->dataBuffLen) && (memcmp(tempElectricCurtainbs->buff->ubuff, zclProfile->attrubiteData->dataBuff->ubuff, zclProfile->attrubiteData->dataBuffLen) == 0))
										{
											tempApplianceInfo->addr = 1;
											ModifyApplianceRequest modifyApplianceRequest;
											modifyApplianceRequest.set_id(tempApplianceInfo->appID);
											modifyApplianceRequest.set_device_id(tempApplianceInfo->ir_id);
											modifyApplianceRequest.set_sub_id(tempApplianceInfo->ir_sub_id);
											modifyApplianceRequest.set_attr_mask(APPLIANCE_ATTR_MASK_ADDR);
											modifyApplianceRequest.set_addr(tempApplianceInfo->addr);
											mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_MODIFY_REQ, modifyApplianceRequest.SerializeAsString().c_str(), modifyApplianceRequest.SerializeAsString().length());
										}
										else
										{
											sendChars->onAddUBuff(1, tempElectricCurtainbs->buff->ubuff, tempElectricCurtainbs->buffLen);
											pmMasterSerialPort->onWriteAttribute((uint32_t)shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, sendChars->ubuff, (uint8_t)(tempElectricCurtainbs->buffLen + 1)), 0);
											//再延时读取一下
											pmMasterSerialPort->onReadAttribute((uint32_t)shortAddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 2000);
										}
										delete tempElectricCurtainbs;
										delete sendChars;
									}
								}
								else
								{
									if(zclProfile->attrubiteData->dataBuff->ubuff[0])
									{
										//取消一下监控
										//发送一条清除标志数据
										uint8_t tempChar[2];
										tempChar[0] = 1;
										tempChar[1] = 0;
										pmMasterSerialPort->onWriteAttribute((uint32_t)shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, tempChar, 2), 0);
										//再延时读取一下
										pmMasterSerialPort->onReadAttribute((uint32_t)shortAddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 2000);
									}
								}
							}
						}
					}
						break;
					case 0x0013://当前用于离线语音上报
					{
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_OFFLINE_VOICE) && (zclProfile->attrubiteData->dataBuffLen == 12))
						{
							//0x09 是485小翌小翌版本 0x15是第一个小翌小翌设备版本
							tempDeviceTypeInfo->subInfo.offLineVoiceStatus->onToProcessCMD(tempDeviceTypeInfo->roomID, zclProfile->attrubiteData->dataBuff->ubuff, zclProfile->attrubiteData->dataBuffLen);
							if((tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDID == 0x11) || (tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDID == 0x12))
							{
								//更新语音唤醒状态
								tempDeviceValue = updateRequest.add_values();
								tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
								tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->offLineVoiceStatus->status);
								//不存储数据库
							}
							else if(tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDID == 0x15)
							{
								//先存储数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDevStatus, tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue);
								if(tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue != tempDeviceTypeInfo->subInfo.offLineVoiceStatus->wakeup_id)
								{
									tempDeviceTypeInfo->subInfo.offLineVoiceStatus->wakeup_id = tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue;
								}
							}
							else if(tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDID == 0x19)
							{
								//先存储数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDevStatus + 1, tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue);
								if(tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue != tempDeviceTypeInfo->subInfo.offLineVoiceStatus->duration)
								{
									tempDeviceTypeInfo->subInfo.offLineVoiceStatus->duration = tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue;
									//更新到服务器
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_DURATION);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->offLineVoiceStatus->duration);
								}
							}
							else if(tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDID == 0x1C)
							{
								//先存储数据库
								pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDevStatus + 2, tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue);
								if(tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue != tempDeviceTypeInfo->subInfo.offLineVoiceStatus->volume)
								{
									tempDeviceTypeInfo->subInfo.offLineVoiceStatus->volume = tempDeviceTypeInfo->subInfo.offLineVoiceStatus->lastCMDValue;
									//更新到服务器
									tempDeviceValue = updateRequest.add_values();
									tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_LEVEL);
									tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->offLineVoiceStatus->volume);
								}
							}
						}
					}
						break;
					default: break;
				}
			}
		}
			break;
		default:break;
	}
	if(tempDeviceTypeInfo != NULL)
	{
		if(updateRequest.values_size() > 0)
		{
			updateRequest.set_device_id(tempDeviceTypeInfo->deviceID);
			updateRequest.set_sub_id(tempDeviceTypeInfo->subID);
			updateRequest.set_sub_type(tempDeviceTypeInfo->devType);
			mfTCPCMDSend(CMD_ID_DEVICE_VALUE_UPDATE_REQ, updateRequest.SerializeAsString().c_str(), updateRequest.SerializeAsString().length());
		}
		if(deviceAlarmType != 0)
		{
			//本地发送一份报警
			long tempCurrentTime = onGetTimeSec();
			TypeChar *retString = new TypeChar();
			onSendAlarmInfo(tempCurrentTime, TRUE, tempDeviceTypeInfo->name->buff, tempDeviceTypeInfo->roomID, deviceAlarmType, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, retString);
			//发送给其它网关
			onUDPSetAlarmInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, deviceAlarmType, tempCurrentTime, retString->buff);
			delete retString;
			DeviceAlarmRequest  deviceAlarmRequest;
			DeviceAlarmInfo * deviceAlarmInfo = deviceAlarmRequest.mutable_device_alarm();
			deviceAlarmInfo->set_alarm_type(deviceAlarmType);
			deviceAlarmInfo->set_family_id(pDataBase->onGetFamilyID());
			deviceAlarmInfo->set_gateway_id(pDataBase->onGetGateway_ID());
			deviceAlarmInfo->set_device_id(tempDeviceTypeInfo->deviceID);
			deviceAlarmInfo->set_sub_id(tempDeviceTypeInfo->subID);
			deviceAlarmInfo->set_sub_type(tempDeviceTypeInfo->devType);
			deviceAlarmInfo->set_alarm_time(tempCurrentTime);
			mfTCPCMDSend(CMD_ID_DEVICE_ALARM_REQ, deviceAlarmRequest.SerializeAsString().c_str(), deviceAlarmRequest.SerializeAsString().length());
		}
	}
	return 0;
}

TypeAFINComming::~TypeAFINComming()
{
	delete zclProfile;
	if(mMemNewFreeCount > 0)
	{
		mMemNewFreeCount--;
	}
}
