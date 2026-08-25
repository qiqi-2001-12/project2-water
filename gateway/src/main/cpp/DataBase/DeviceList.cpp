/*
 * DeviceList.cpp
 *
 *  Created on: Jul 15, 2017
 *      Author: root
 */
#include "../Main/WinobleMain.h"

TypeDeviceList::TypeDeviceList()
{
	roomList = new TypeArrayList(ArrayTypeRoomInfo);
	sceneList = new TypeArrayList(ArrayTypeSceneNameInfo);
	applianceList = new TypeArrayList(ArrayTypeApplianceInfo);
	carriedOutSceneList = new TypeArrayList(ArrayTypeCarriedSceneList);
	dbDeviceInfoList = new TypeArrayList(ArrayTypeDBDeviceInfo);
	gatewayList = new TypeArrayList(ArrayTypeGatewayInfo);
	mDownLoadFlag = 0;
	mDownLoadingFlag = 0;
}

void TypeDeviceList::onDownLoadWithFlag(int32_t flag)
{
	mDownLoadFlag |= flag | DEVICE_GET_MASK_IN_FAMILY;
	if(DEVICE_GET_MASK_DEVICE & mDownLoadFlag)
	{
		mDownLoadFlag |= DEVICE_GET_MASK_LIGHT | DEVICE_GET_MASK_DIMMER | DEVICE_GET_MASK_CURTAIN | DEVICE_GET_MASK_SWITCH | DEVICE_GET_MASK_GAS |
		                 DEVICE_GET_MASK_IR_REMOTE | DEVICE_GET_MASK_PIR | DEVICE_GET_MASK_SMOKE | DEVICE_GET_MASK_FLOOD | DEVICE_GET_MASK_DOOR_WINDOW |
		                 DEVICE_GET_MASK_ENV_DETECTOR | DEVICE_GET_MASK_WATER_LEAKAGE_DETECTOR | DEVICE_GET_MASK_GAS_ARM | DEVICE_GET_MASK_APPLIANCE |
		                 DEVICE_GET_MASK_CLOTHES_HANGER | DEVICE_GET_MASK_RS485_TRANSFER | DEVICE_GET_MASK_SOS | DEVICE_GET_MASK_DOOR_LOCK | DEVICE_GET_MASK_OFFLINE_VOICE;
	}
}

void TypeDeviceList::onDownLoadResetAll()
{
	//清除正在下载中标志  代表已经下载完成
	mDownLoadingFlag = 0;
	mDownLoadFlag = DEVICE_GET_MASK_DEVICE | DEVICE_GET_MASK_ROOM | DEVICE_GET_MASK_SCENE | DEVICE_GET_MASK_IN_FAMILY |
			DEVICE_GET_MASK_LIGHT | DEVICE_GET_MASK_DIMMER | DEVICE_GET_MASK_CURTAIN | DEVICE_GET_MASK_SWITCH | DEVICE_GET_MASK_GAS |
			DEVICE_GET_MASK_IR_REMOTE | DEVICE_GET_MASK_PIR | DEVICE_GET_MASK_SMOKE | DEVICE_GET_MASK_FLOOD | DEVICE_GET_MASK_DOOR_WINDOW |
			DEVICE_GET_MASK_ENV_DETECTOR | DEVICE_GET_MASK_WATER_LEAKAGE_DETECTOR | DEVICE_GET_MASK_GAS_ARM | DEVICE_GET_MASK_APPLIANCE |
			DEVICE_GET_MASK_CLOTHES_HANGER | DEVICE_GET_MASK_RS485_TRANSFER | DEVICE_GET_MASK_SOS | DEVICE_GET_MASK_DOOR_LOCK | DEVICE_GET_MASK_OFFLINE_VOICE;
}

void TypeDeviceList::onDeleteRoomInfo(TypeRoomInfo *roominfo)
{
	if(roominfo)
	{
		//默认房间不能删除的哦
		if(roominfo->room_id)
		{
			pDataBase->onDeleteDataBase("roominfo", "roomid", roominfo->room_id);
			roomList->removeObject(roominfo);
		}
	}
}

void TypeDeviceList::onDeleteApplianceInfo(TypeApplianceInfo *applianceinfo)
{
	if(applianceinfo)
	{
		pDataBase->onDeleteDataBase("applianceinfoex", "appid", applianceinfo->appID);
		pDataBase->onDeleteDataBase("appliancecodeinfo", "appid", applianceinfo->appID);
	}
	applianceList->removeObject(applianceinfo);
}

void TypeDeviceList::onDeleteGateway()
{
	//删除所有设备  删除所有场景  删除所有白名单  删除所有ZoneID
	while(sceneList->size() > 0)
	{
		onDeleteSceneInfo((TypeSceneNameInfo *)sceneList->get(0));
	}

	TypeRoomInfo *tempRoomInfo = NULL;
	for(int i = 0; i < roomList->size(); )
	{
		tempRoomInfo = (TypeRoomInfo *)roomList->get(i);
		if(tempRoomInfo->room_id)
		{
			onDeleteRoomInfo(tempRoomInfo);
			continue;
		}
		i++;
	}

	//删除所有家电
	while(applianceList->size() > 0)
	{
		onDeleteApplianceInfo((TypeApplianceInfo *)applianceList->get(0));
	}

	//删除所有设备
	while(dbDeviceInfoList->size() > 0)
	{
		TypeDBDeviceInfo *dbDeviceInfo = (TypeDBDeviceInfo *)dbDeviceInfoList->get(0);
		mfLeaveToGateway(dbDeviceInfo->ieee);
	}

	mPrintf(Log_DataBase, "清除所有数据信息! ");
}

void TypeDeviceList::onPrintfRoomInfo()//
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	TypeRoomInfo *tempRoomInfo = NULL;
	TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
	mPrintf(Log_DataBase, "DeviceList:%d ", dbDeviceInfoList->size());
	mPrintf(Log_DataBase, "{ ");
	TypeChar *pringLog = new TypeChar(512);
	for(int i = 0; i < dbDeviceInfoList->size(); i++)
	{
		tempDBDeviceInfo = (TypeDBDeviceInfo *) dbDeviceInfoList->get(i);
		if(tempDBDeviceInfo != NULL)
		{
			//首先打印一下设备的IEEE 短地址等信息
			pringLog->onClear();
			if((tempDBDeviceInfo->gatewayID == 0) || (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
			{
				sprintf(pringLog->buff, "KEYID=%d(%s),%llx,$%04x$,%llx,%04x,%02x,%d,%d,%s t=%d", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->serial->buff, tempDBDeviceInfo->ieee,
				        tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->ieee_ex, tempDBDeviceInfo->shortAddr_ex, tempDBDeviceInfo->attr->value,
				        tempDBDeviceInfo->rssi, tempDBDeviceInfo->lqi, tempDBDeviceInfo->onLineFlag.bits.status ? "在线":"离线", tempDBDeviceInfo->delayTime);
			}
			else
			{
				//不是本网关的设备
				sprintf(pringLog->buff, "%d(%s),%llx,%llx,%lld,%02x,%d,%d,%s t=%d", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->serial->buff, tempDBDeviceInfo->ieee,
				        tempDBDeviceInfo->ieee_ex, tempDBDeviceInfo->gatewayID, tempDBDeviceInfo->attr->value,
				        tempDBDeviceInfo->rssi, tempDBDeviceInfo->lqi, tempDBDeviceInfo->onLineFlag.bits.status ? "在线":"离线", tempDBDeviceInfo->delayTime);
			}
			//这里打印子节点信息
			for(int j = 1; j <= tempDBDeviceInfo->subCount; j++)
			{
				tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
				if(tempDeviceTypeInfo != NULL)
				{
					int currentLen = strlen(pringLog->buff);
					tempRoomInfo = onFindRoomInfo(tempDeviceTypeInfo->roomID);
					sprintf(&pringLog->buff[currentLen], " %s-%s=%d ", tempRoomInfo ? tempRoomInfo->name->buff : "NULL", tempDeviceTypeInfo->name->buff, tempDeviceTypeInfo->onGetStatus());
				}
			}
			mPrintf(Log_DataBase, "%s ", pringLog->buff);
		}
		else
		{
			mPrintf(Log_DataBase, "Error:数据库白名单异常表=NULL ");
		}
	}
	mPrintf(Log_DataBase, "end ");
	mPrintf(Log_DataBase, "} ");

	//打印一下线程信息
	TypeThreadInfo *tempThreadInfo = NULL;
	for(int i = 0; i < mThreadInfoList->size(); ++ i)
	{
		tempThreadInfo = (TypeThreadInfo *)mThreadInfoList->get(i);
		//mPrintf(Log_DataBase, "ThreadInfo: title=%s ", tempThreadInfo->title->buff);
	}
	delete pringLog;
}

bool TypeDeviceList::onCheckWhiteList(bool ismaster, int64_t ieee)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	for(int i = 0; i < dbDeviceInfoList->size(); ++i)
	{
		tempDBDeviceInfo = (TypeDBDeviceInfo *) dbDeviceInfoList->get(i);
		if(tempDBDeviceInfo)
		{
			//暂时只检测 没有入网，或者已经入网本网关的设备
			if((tempDBDeviceInfo->gatewayID == 0) || (tempDBDeviceInfo->gatewayID && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID())))//还没有入网
			{
				if(ismaster)
				{
					if(tempDBDeviceInfo->ieee == ieee)
					{
						return TRUE;
					}
				}
				else
				{
					if(tempDBDeviceInfo->ieee_ex == ieee)
					{
						return TRUE;
					}
				}
			}
		}
	}
	return FALSE;
}

TypeDBDeviceInfo *TypeDeviceList::onCheckGatewayDeviceInfo(int32_t type, int64_t value)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	for(int i = 0; i < dbDeviceInfoList->size(); ++i)
	{
		tempDBDeviceInfo = (TypeDBDeviceInfo *) dbDeviceInfoList->get(i);
		if(tempDBDeviceInfo && ((tempDBDeviceInfo->gatewayID == 0) || (pDataBase->onGetGateway_ID() == tempDBDeviceInfo->gatewayID)))
		{
			int64_t tempValue = 0;
			switch(type)
			{
				case IGatewayID:tempValue = tempDBDeviceInfo->gatewayID;break;
				case IDeviceID:tempValue = tempDBDeviceInfo->deviceID;break;
				case IDevType:tempValue = tempDBDeviceInfo->devType;break;
				case IIeee:tempValue = tempDBDeviceInfo->ieee;break;
				case IShortAddr:tempValue = tempDBDeviceInfo->shortAddr;break;
				case IIeee_Ex:tempValue = tempDBDeviceInfo->ieee_ex;break;
				case IShortAddr_Ex:tempValue = tempDBDeviceInfo->shortAddr_ex;break;
				case IRgb:tempValue = tempDBDeviceInfo->rgb;break;
				case IOnline:tempValue = tempDBDeviceInfo->onLineFlag.value;break;
				case IProtocol:tempValue = tempDBDeviceInfo->protocol;break;
				case IProtocolVe:tempValue = tempDBDeviceInfo->protocolVer;break;
				case ITargetScreen:tempValue = tempDBDeviceInfo->targetScreen;break;
				case IAttr:tempValue = tempDBDeviceInfo->attr->value;break;
				case ISerial:break;//这些暂时没有处理
				case ISwVer:break;//这些暂时没有处理
				case IHwVer:break;//这些暂时没有处理
				case IManufacturer:break;//暂时没有处理
				case ISubCount:tempValue = tempDBDeviceInfo->subCount;break;
				default:tempValue = -1;mPrintf(Log_DataBase, "Error:check db unknow type=%d ", type);break;
			}
			if(tempValue == value)
			{
				break;
			}
			else
			{
				tempDBDeviceInfo = NULL;
			}
		}
		else
		{
			tempDBDeviceInfo = NULL;
		}
	}
	return tempDBDeviceInfo;
}

bool TypeDeviceList::onCheckDeviceEvent(TypeDBDeviceInfo *dbdeviceinfo, EmunEventFlag event)
{
	if(dbdeviceinfo && (dbdeviceinfo->gatewayID == pDataBase->onGetGateway_ID()))
	{
		if(event == Event_Dev_RGB)
		{
			if(dbdeviceinfo->attr->bits.rgb )
			{
				return TRUE;
			}
		}
		else if((event & Event_Dev_Icon) || (event & Event_Dev_Name))
		{
			if(dbdeviceinfo->attr->bits.screen && ((dbdeviceinfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_1) || (dbdeviceinfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_2) || (dbdeviceinfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_3) || (dbdeviceinfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_4) ||
			                                       (dbdeviceinfo->devType == DEVICE_TYPE_DIMMER_CHANNEL_1) || (dbdeviceinfo->devType == DEVICE_TYPE_DIMMER_CHANNEL_2) || (dbdeviceinfo->devType == DEVICE_TYPE_CURTAIN_CHANNEL_1) || (dbdeviceinfo->devType == DEVICE_TYPE_CURTAIN_CHANNEL_2)))
			{
				return TRUE;
			}
		}
	}
	return FALSE;
}

TypeDBDeviceInfo *TypeDeviceList::onCheckFamilyDeviceInfo(int32_t type, int64_t value)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	for(int i = 0; i < dbDeviceInfoList->size(); ++i)
	{
		tempDBDeviceInfo = (TypeDBDeviceInfo *) dbDeviceInfoList->get(i);
		if(tempDBDeviceInfo)
		{
			int64_t tempValue = 0;
			switch(type)
			{
				case IGatewayID:tempValue = tempDBDeviceInfo->gatewayID;break;
				case IDeviceID:tempValue = tempDBDeviceInfo->deviceID;break;
				case IDevType:tempValue = tempDBDeviceInfo->devType;break;
				case IIeee:tempValue = tempDBDeviceInfo->ieee;break;
				case IShortAddr:tempValue = tempDBDeviceInfo->shortAddr;break;
				case IIeee_Ex:tempValue = tempDBDeviceInfo->ieee_ex;break;
				case IShortAddr_Ex:tempValue = tempDBDeviceInfo->shortAddr_ex;break;
				case IRgb:tempValue = tempDBDeviceInfo->rgb;break;
				case IOnline:tempValue = tempDBDeviceInfo->onLineFlag.value;break;
				case IProtocol:tempValue = tempDBDeviceInfo->protocol;break;
				case IProtocolVe:tempValue = tempDBDeviceInfo->protocolVer;break;
				case ITargetScreen:tempValue = tempDBDeviceInfo->targetScreen;break;
				case IAttr:tempValue = tempDBDeviceInfo->attr->value;break;
				case ISerial:break;//这些暂时没有处理
				case ISwVer:break;//这些暂时没有处理
				case IHwVer:break;//这些暂时没有处理
				case IManufacturer:break;//暂时没有处理
				case ISubCount:tempValue = tempDBDeviceInfo->subCount;break;
				default:tempValue = -1;mPrintf(Log_DataBase, "Error:check db unknow type=%d ", type);break;
			}
			if(tempValue == value)
			{
				break;
			}
			else
			{
				tempDBDeviceInfo = NULL;
			}
		}
	}
	return tempDBDeviceInfo;
}

TypeDeviceTypeInfo *TypeDeviceList::onFindDeviceTypeInfo(int32_t deviceid, int32_t subid)
{
	TypeDBDeviceInfo *dbDeviceInfo = onCheckGatewayDeviceInfo(IDeviceID, deviceid);
	TypeDeviceTypeInfo *typeDeviceTypeInfo = NULL;
	if(dbDeviceInfo)
	{
		typeDeviceTypeInfo = dbDeviceInfo->onGetSubInfo(subid);
	}
	return typeDeviceTypeInfo;
}

bool TypeDeviceList::onCheckLightToScene(int64_t sceneid)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
	for(int i = 0; i < dbDeviceInfoList->size(); i++)
	{
		//只管本网关的设备
		tempDBDeviceInfo = (TypeDBDeviceInfo *) dbDeviceInfoList->get(i);
		if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))//
		{
			for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
			{
				tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
				if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT))
				{
					if(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->sceneID == sceneid)
					{
						//找到这个场景
						TypeSceneNameInfo *tempSceneNameInfo = onFindSceneInfo(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->sceneID);
						if(tempSceneNameInfo)
						{
							tempDeviceTypeInfo->onSetName(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Name), tempSceneNameInfo->name->buff);
							tempDeviceTypeInfo->onSetIconID(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Icon), tempSceneNameInfo->icon_id);
							tempDeviceTypeInfo->onSetRoomID(tempSceneNameInfo->room_id);
						}
					}
				}
			}
		}
	}
	return TRUE;
}

bool TypeDeviceList::onAddSubDeviceInfo(TypeDeviceTypeInfo *devicetypeinfo, int32_t randvalue)
{
	if(devicetypeinfo != NULL)
	{
		//首先找到这个数据库设备
		TypeDBDeviceInfo *dbDeviceInfo = onCheckFamilyDeviceInfo(IDeviceID, devicetypeinfo->deviceID);
		if(dbDeviceInfo)//如果存在这个设备
		{
			//找到这个子设备
			TypeDeviceTypeInfo *tempDeviceTypeInfo = dbDeviceInfo->onGetSubInfo(devicetypeinfo->subID);
			if(tempDeviceTypeInfo != NULL)
			{
				if(pDataBase)
				{
					//只更新设备子信息
					tempDeviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(dbDeviceInfo,  Event_Dev_Icon) , devicetypeinfo);//更新设备信息
				}
			}
			else
			{
				tempDeviceTypeInfo = devicetypeinfo;
				//添加设备子信息
				if(dbDeviceInfo->onAddSubInfo(devicetypeinfo, devicetypeinfo->subID))
				{
					//添加到数据库
					if(randvalue)
					{
						pDataBase->onUpdateSubDeviceInfo(devicetypeinfo, SubInset, 1);

						//也添加到数据库
						if(onCheckDeviceEvent(dbDeviceInfo,  Event_Dev_Icon))
						{
							pDataBase->onAddDevEventInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, Event_Dev_Icon, 0);
						}

						if(onCheckDeviceEvent(dbDeviceInfo,  Event_Dev_Name))
						{
							pDataBase->onAddDevEventInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, Event_Dev_Name, 0);
						}
					}
				}
			}
			tempDeviceTypeInfo->onSetShortAddr(&dbDeviceInfo->shortAddr);
		}
		else
		{
			//不存在这个子设备
			mPrintf(Log_Error, "不存在这个子设备，请检查一下! ");
		}
	}
	else
	{
		mPrintf(Log_DataBase, "LogError:添加子设备时，这个子设备不存在! ");
	}
	return TRUE;
}

int32_t TypeDeviceList::onGetBaseHeartCount()
{
	if(dbDeviceInfoList->size() < 10)
	{
		return 10;
	}
	else
	{
		return dbDeviceInfoList->size();
	}
}

bool TypeDeviceList::onSetHeartOK(bool ismater, TypeDBDeviceInfo *tempdbdeviceinfo)
{
	if(tempdbdeviceinfo)
	{
		if(ismater)
		{
			if(tempdbdeviceinfo->shortAddr && (onCheckBattery(tempdbdeviceinfo->devType) == FALSE))
			{
				//mPrintf(Log_Error, "OK!!! short=%04x", shortaddr);
				tempdbdeviceinfo->delayTime = onGetBaseHeartCount() * 3;//3 倍设备数量算离线
				tempdbdeviceinfo->saveCheckTime = onGetBaseHeartCount();//这个时间内可以不再发送
				//是存在这个设备的
				if(tempdbdeviceinfo->onLineFlag.bits.status == DEVICE_STATUS_OFFLINE)
				{
					//存储在本地
					pDataBase->onUpdateDeviceInfoSqlValue(tempdbdeviceinfo, IOnline, (int64_t)tempdbdeviceinfo->onLineFlag.onSetValue(DEVICE_STATUS_ONLINE, tempdbdeviceinfo->onLineFlag.bits.saveStatus));
					onUpdateOnOffLineRequest(tempdbdeviceinfo, DEVICE_STATUS_ONLINE);
				}
			}
		}
		else
		{
			//这个用于判断从模块当前是否是正常通信
			tempdbdeviceinfo->slaveTickTime = onGetTimeSec();
		}
	}
	return TRUE;
}

TypeDBDeviceInfo * TypeDeviceList::onZoneIDFindDeviceInfoEx(uint8_t tzoneid, uint32_t shortaddr)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	if(tzoneid)
	{
		for(int i = 0; i < dbDeviceInfoList->size(); ++ i)
		{
			tempDBDeviceInfo = (TypeDBDeviceInfo *)dbDeviceInfoList->get(i);
			if(tempDBDeviceInfo && onCheckBattery(tempDBDeviceInfo->devType) && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()) && (tempDBDeviceInfo->shortAddr_ex == tzoneid))
			{
				//那这里最好更新一下短地址信息
				if(((uint8_t)tempDBDeviceInfo->shortAddr != shortaddr) && shortaddr)
				{
					mPrintf(Log_Master, "Master:短地址根据zoneID修改:keyID=%d(%llx) %04x->%04x ", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->ieee, tempDBDeviceInfo->shortAddr, shortaddr);
					pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr, shortaddr);
					//更新一下服务器地址
					onUpdateDeviceInfo(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex);
				}
				break;
			}
			tempDBDeviceInfo = NULL;
		}
	}
	return tempDBDeviceInfo;
}

TypeDBDeviceInfo * TypeDeviceList::onZoneIDFindDeviceInfo(uint8_t tzoneid, uint32_t shortaddr)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
	if(tzoneid)
	{
		for(int i = 0; i < dbDeviceInfoList->size(); ++ i)
		{
			tempDBDeviceInfo = (TypeDBDeviceInfo *)dbDeviceInfoList->get(i);
			if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()) && onCheckBattery(tempDBDeviceInfo->devType))
			{
				tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(1);
				if(tempDeviceTypeInfo && (tempDeviceTypeInfo->onGetZoneID() == tzoneid))
				{
					//那这里最好更新一下短地址信息
					if(((uint8_t)tempDBDeviceInfo->shortAddr != shortaddr) && shortaddr)
					{
						mPrintf(Log_Master, "Master:短地址根据zoneID修改:keyID=%d(%llx) %04x->%04x ", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->ieee, tempDBDeviceInfo->shortAddr, shortaddr);
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr, shortaddr);
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr_Ex, tzoneid);
						//更新一下服务器地址
						onUpdateDeviceInfo(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex);
					}
					break;
				}
			}
			tempDBDeviceInfo = NULL;
		}
	}
	return tempDBDeviceInfo;
}

TypeDBDeviceInfo * TypeDeviceList::onZclFindDeviceInfo(bool ismaster, TypeAFINComming *afinComming)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	if(afinComming)
	{
		if(ismaster)
		{
			tempDBDeviceInfo = onCheckGatewayDeviceInfo(IShortAddr, afinComming->shortAddr);
			if(tempDBDeviceInfo)
			{
				//低功耗只要有数据交互就认为设备是在线的
				if(onCheckBattery(tempDBDeviceInfo->devType))
				{
					tempDBDeviceInfo->delayTime = 86400000;//24h
					// 是存在这个设备的
					if(tempDBDeviceInfo->onLineFlag.bits.status == DEVICE_STATUS_OFFLINE)
					{
						//存储在本地
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, (int64_t)tempDBDeviceInfo->onLineFlag.onSetValue(DEVICE_STATUS_ONLINE, tempDBDeviceInfo->onLineFlag.bits.saveStatus));
						onUpdateOnOffLineRequest(tempDBDeviceInfo, DEVICE_STATUS_ONLINE);
					}
				}
				else if(tempDBDeviceInfo->onLineFlag.bits.status == 0)
				{
					//如果网关监控设备离线  就直接发送一个心跳包，因为设备肯定是在线的
					pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, 1, Event_Dev_Heartbeat, 0);
				}
			}
			else
			{
				//如果是低功耗可以试图根据zoneID更新短地址信息
				if((afinComming->clusterID == 0x0500) && (afinComming->zclProfile->zclHead->type == ZCL_FRAME_TYPE_SPECIFIC_CMD) && (afinComming->zclProfile->cmdID == 0) && (afinComming->zclProfile->attrubiteData->totalLen > 3))
				{
					//根据ZoneID 更新 短地址信息
					tempDBDeviceInfo = onZoneIDFindDeviceInfoEx(afinComming->zclProfile->attrubiteData->totalBuff->ubuff[3], afinComming->shortAddr);
					if(tempDBDeviceInfo == NULL)
					{
						//用旧版本也尝试一下
						tempDBDeviceInfo = onZoneIDFindDeviceInfo(afinComming->zclProfile->attrubiteData->totalBuff->ubuff[3], afinComming->shortAddr);
					}
				}
				//发送命令去读取IEEE
				if((tempDBDeviceInfo == NULL) && afinComming->shortAddr != SHORTADDR_BROADCAST)//正在下载设备的时候不检查
				{
					TypeChar *pSendBuff = new TypeChar(6);
					pSendBuff->onAddInt16Ex(0, afinComming->shortAddr & 0xFFFF);
					pSendBuff->ubuff[2] = 0x00;
					pSendBuff->ubuff[3] = 0x00;
					//找不到对应的设备   请求一下IEEE地址
					pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_IEEE_ADDR_REQ, new TypeAFAttribute(pSendBuff->ubuff, 4), 0));
					mPrintf(Log_Master, "找不到主模块地址: %04x ", afinComming->shortAddr);
					delete pSendBuff;
				}
			}
		}
		else {
			if (DUALZIGBEECHIP)
			{
				tempDBDeviceInfo = onCheckGatewayDeviceInfo(IShortAddr_Ex, afinComming->shortAddr);
				if(tempDBDeviceInfo == NULL)
				{
					//发送命令去读取IEEE
					if(afinComming->shortAddr != SHORTADDR_BROADCAST)//正在下载设备的时候不检查
					{
						TypeChar *pSendBuff = new TypeChar(6);
						pSendBuff->onAddInt16Ex(0, afinComming->shortAddr & 0xFFFF);
						pSendBuff->ubuff[2] = 0x00;
						pSendBuff->ubuff[3] = 0x00;
						//找不到对应的设备   请求一下IEEE地址
						pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_IEEE_ADDR_REQ, new TypeAFAttribute(pSendBuff->ubuff, 4), 0));
						mPrintf(Log_Slave, "找不到从模块地址: %04x ", afinComming->shortAddr);
						delete pSendBuff;
					}
				}
			}
		}
	}
	return tempDBDeviceInfo;
}

TypeDBDeviceInfo *TypeDeviceList::onAddDeviceInfo(TypeDBDeviceInfo *dbdeviceinfo, int32_t randvalue)
{
	if(dbdeviceinfo)
	{
		TypeDBDeviceInfo *tempDBDeviceInfo = onCheckFamilyDeviceInfo(IDeviceID, dbdeviceinfo->deviceID);
		if(tempDBDeviceInfo == NULL)
		{
			//添加一个
			if(randvalue)
			{
				pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, IInset, 1);
				if(onCheckDeviceEvent(dbdeviceinfo, Event_Dev_RGB))
				{
					//更新设备RGB 值
					pDataBase->onAddDevEventInfo(dbdeviceinfo->deviceID, 1, Event_Dev_RGB, 0);
				}
			}
			dbdeviceinfo->randomValue = randvalue;
			dbDeviceInfoList->add(dbdeviceinfo);
			return dbdeviceinfo;
		}
		else if(pDataBase)
		{
			//更新信息
			//代表信息有更新
			if(tempDBDeviceInfo->gatewayID != dbdeviceinfo->gatewayID)
			{
				if(dbdeviceinfo->gatewayID == 0)
				{
					if(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID())
					{
						//如果设备已经入网本网关
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IGatewayID, tempDBDeviceInfo->gatewayID);
						//那这里最好还要更新一下服务器设备ID
						onUpdateDeviceInfo(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex);
					}
					else
					{
						//入网的不是本网关，那就以服务器为主
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IGatewayID, dbdeviceinfo->gatewayID);
					}
				}
				else
				{
					//以服务器为主
					//如果设备以前是本网关的设备，网关ID被强制修改了，就让这个设备离网
					if(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID())
					{
						pmMasterSerialPort->onLeaveWithIEEE(tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->ieee);
						if(DUALZIGBEECHIP) {
							pmSlaveSerialPort->onLeaveWithIEEE(tempDBDeviceInfo->shortAddr_ex,
															   tempDBDeviceInfo->ieee_ex);
						}
					}
					pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IGatewayID, dbdeviceinfo->gatewayID);
				}
			}

			if(tempDBDeviceInfo->targetScreen != dbdeviceinfo->targetScreen)
			{
				pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, ITargetScreen, dbdeviceinfo->targetScreen);
			}

			//rgb标志更新一下
			if(tempDBDeviceInfo->attr->value != dbdeviceinfo->attr->value)
			{
				pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IAttr, dbdeviceinfo->attr->value);
			}

			if(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID())
			{
				//如果是这个网关的设备，以下信息以网关为主
				if((tempDBDeviceInfo->shortAddr != dbdeviceinfo->shortAddr) || (tempDBDeviceInfo->shortAddr_ex != dbdeviceinfo->shortAddr_ex))
				{
					//更新一下服务器短地址信息
					onUpdateDeviceInfo(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex);
				}
				//以本地在线离线为准
				if(tempDBDeviceInfo->onLineFlag.bits.status != dbdeviceinfo->onLineFlag.bits.status)
				{
					//更新服务器在线离线值,以本地为主
					if(tempDBDeviceInfo->shortAddr == 0)
					{
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, (int64_t)tempDBDeviceInfo->onLineFlag.onSetValue(DEVICE_STATUS_OFFLINE, tempDBDeviceInfo->onLineFlag.bits.saveStatus));
					}
					onUpdateOnOffLineRequest(tempDBDeviceInfo, tempDBDeviceInfo->onLineFlag.bits.status);
				}
			}
			else
			{
				//不是这个网关的设备，更新数据库
				if(tempDBDeviceInfo->shortAddr != dbdeviceinfo->shortAddr)
				{
					pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr, dbdeviceinfo->shortAddr);
				}
				if(tempDBDeviceInfo->shortAddr_ex != dbdeviceinfo->shortAddr_ex)
				{
					pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr_Ex, dbdeviceinfo->shortAddr_ex);
				}
				if((tempDBDeviceInfo->onLineFlag.bits.status != dbdeviceinfo->onLineFlag.bits.status) || (tempDBDeviceInfo->onLineFlag.bits.saveStatus != dbdeviceinfo->onLineFlag.bits.status))
				{
					tempDBDeviceInfo->onLineFlag.bits.saveStatus = tempDBDeviceInfo->onLineFlag.bits.status = dbdeviceinfo->onLineFlag.bits.saveStatus = dbdeviceinfo->onLineFlag.bits.status;
					//更新本地在线离线值
					pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, (int64_t)tempDBDeviceInfo->onLineFlag.value);
				}
			}
			//检查RGB
			if(tempDBDeviceInfo->attr->bits.rgb && (tempDBDeviceInfo->rgb != dbdeviceinfo->rgb))
			{
				pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IRgb, dbdeviceinfo->rgb);
				//更新设备RGB 值
				if(onCheckDeviceEvent(dbdeviceinfo, Event_Dev_RGB))
				{
					pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, 1, Event_Dev_RGB, 0);
				}
			}

			tempDBDeviceInfo->randomValue = randvalue;
			delete dbdeviceinfo;//释放内存
			return tempDBDeviceInfo;
		}
	}
	return dbdeviceinfo;
}

bool TypeDeviceList::onAddDeviceInfoCheck(int32_t randvalue)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	for(int i = 0; i < dbDeviceInfoList->size(); i++)
	{
		tempDBDeviceInfo = (TypeDBDeviceInfo *) dbDeviceInfoList->get(i);
		if(tempDBDeviceInfo->randomValue != randvalue)
		{
			//去服务器确认一下就可以删除了
			TestGetDeviceIdRequest deviceIDRequest;
			deviceIDRequest.set_ieee(tempDBDeviceInfo->ieee);
			mfTCPCMDSend(CMD_ID_DEVICE_TEST_GET_ID_REQ, deviceIDRequest.SerializeAsString().c_str(), deviceIDRequest.SerializeAsString().length());
		}
	}
	return TRUE;
}

bool TypeDeviceList::onDeleteApplianceCodeInfo(int32_t appid, int32_t keyid)
{
	TypeApplianceInfo *tempApplianceInfo = onFindApplianceInfo(appid);
	if(tempApplianceInfo)
	{
		TypeApplianceCodeInfo *tempApplianceCodeInfo = NULL;
		for(int i = 0; i < tempApplianceInfo->codeList->size(); ++ i)
		{
			tempApplianceCodeInfo = (TypeApplianceCodeInfo *)tempApplianceInfo->codeList->get(i);
			if(tempApplianceCodeInfo->key_id == keyid)
			{
				tempApplianceInfo->codeList->removeObject(tempApplianceCodeInfo);
				//删除数据库
				pDataBase->onDeleteApplianceCode(appid, keyid);
				break;
			}
		}
	}
	return TRUE;
}

bool TypeDeviceList::onDeleteDeviceInfo(int32_t deviceid)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = onCheckFamilyDeviceInfo(IDeviceID, deviceid);//删除白名单
	if(tempDBDeviceInfo != NULL)
	{
		pDataBase->onDeleteDeviceInfoSqlValue(tempDBDeviceInfo->deviceID);
		dbDeviceInfoList->removeObject(tempDBDeviceInfo);
	}
	return TRUE;
}

TypeApplianceCodeInfo * TypeDeviceList::onAddAppliancesCodeInfo(TypeApplianceInfo *applianceInfo, TypeApplianceCodeInfo *codeinfo, int32_t randvalue)
{
	if(applianceInfo && codeinfo)
	{
		TypeApplianceCodeInfo *tempCodeInfo = applianceInfo->onFindCMDWithKeyID(codeinfo->key_id);
		if(tempCodeInfo == NULL)
		{
			//不存在 就添加
			codeinfo->randValue = randvalue;
			applianceInfo->codeList->add(codeinfo);
			if(randvalue)
			{
				//添加到数据库
				pDataBase->onUpdateApplianceCodeInfo(codeinfo, ApplianceCMDInset, 0);
			}
			return codeinfo;
		}
		else if(pDataBase)
		{
			//检查指令更新
			if(tempCodeInfo->ir_code != codeinfo->ir_code)
			{
				pDataBase->onUpdateApplianceCodeInfo(tempCodeInfo, ApplianceCMDIrCode, codeinfo->ir_code);
			}
			if(tempCodeInfo->key_id != codeinfo->key_id)
			{
				pDataBase->onUpdateApplianceCodeInfo(tempCodeInfo, ApplianceCMDKeyID, codeinfo->key_id);
			}
			if(tempCodeInfo->status != codeinfo->status)
			{
				pDataBase->onUpdateApplianceCodeInfo(tempCodeInfo, ApplianceCMDStatus, codeinfo->status);
			}
			tempCodeInfo->randValue = randvalue;
			delete codeinfo;
			return tempCodeInfo;
		}
	}
	return codeinfo;
}

TypeApplianceInfo * TypeDeviceList::onAddAppliancesInfo(TypeApplianceInfo *applianceinfo, int32_t randvalue)
{
	if(applianceinfo)
	{
		TypeApplianceInfo *tempApplianceInfo = onFindApplianceInfo(applianceinfo->appID);
		if(tempApplianceInfo == NULL)
		{
			applianceList->add(applianceinfo);
			if(randvalue)
			{
				//添加到数据库
				pDataBase->onUpdateApplianceInfo(applianceinfo, ApplianceInset, 1);
			}
			applianceinfo->randValue = randvalue;
			return applianceinfo;
		}
		else if(pDataBase)
		{
			//已经存在  更新一下家电信息
			if(tempApplianceInfo->ir_id != applianceinfo->ir_id)
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceIrID, applianceinfo->ir_id);
			}
			if(tempApplianceInfo->ir_sub_id != applianceinfo->ir_sub_id)
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceIrSubID, applianceinfo->ir_sub_id);
			}
			if(!tempApplianceInfo->name->onStringCMP(applianceinfo->name->buff))
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceName, applianceinfo->name->buff);
			}
			if(!tempApplianceInfo->manufacturer->onStringCMP(applianceinfo->manufacturer->buff))
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceManufacturer, applianceinfo->manufacturer->buff);
			}
			if(!tempApplianceInfo->model->onStringCMP(applianceinfo->model->buff))
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceModelType, applianceinfo->model->buff);
			}
			if(!tempApplianceInfo->version->onStringCMP(applianceinfo->version->buff))
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceVersion, applianceinfo->version->buff);
			}
			if(!tempApplianceInfo->serial->onStringCMP(applianceinfo->serial->buff))
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceSerial, applianceinfo->serial->buff);
			}
			if(tempApplianceInfo->roomID != applianceinfo->roomID)
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceRoomID, applianceinfo->roomID);
			}
			if(tempApplianceInfo->type != applianceinfo->type)
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceTType, applianceinfo->type);
			}
			if(tempApplianceInfo->value != applianceinfo->value)
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceValue, applianceinfo->value);
			}
			if(tempApplianceInfo->addr != applianceinfo->addr)
			{
				pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceAddr, applianceinfo->addr);
			}
			tempApplianceInfo->randValue = randvalue;
			delete applianceinfo;
			return tempApplianceInfo;
		}
	}
	return applianceinfo;
}

TypeRoomInfo * TypeDeviceList::onAddRoomInfo(TypeRoomInfo *roominfo, int32_t randvalue)
{
	//check if is exit;
	if(roominfo)
	{
		TypeRoomInfo *tempRoomInfo = onFindRoomInfo(roominfo->room_id);
		if(tempRoomInfo == NULL)
		{
			roomList->add(roominfo);
			if(randvalue)
			{
				//添加到数据库
				pDataBase->onUpdateRoomInfo(roominfo, RoomInset, 1);
			}
			roominfo->randValue = randvalue;
			return roominfo;
		}
		else if(pDataBase)
		{
			//already exit
			if(tempRoomInfo->temp_value != roominfo->temp_value)
			{
				tempRoomInfo->temp_value = roominfo->temp_value;
			}
			if(tempRoomInfo->illum_value != roominfo->illum_value)
			{
				tempRoomInfo->illum_value = roominfo->illum_value;
			}
			if(tempRoomInfo->iconID != roominfo->iconID)
			{
				pDataBase->onUpdateRoomInfo(tempRoomInfo, RoomIcon, roominfo->iconID);
			}
			if(!tempRoomInfo->name->onStringCMP(roominfo->name->buff))
			{
				//添加到数据库
				pDataBase->onUpdateRoomInfo(tempRoomInfo, RoomName, roominfo->name->buff);
			}
			tempRoomInfo->randValue = randvalue;
			delete roominfo;
			return tempRoomInfo;
		}
	}
	return roominfo;
}

TypeRoomInfo *TypeDeviceList::onFindRoomInfo(int32_t roomid)
{
	TypeRoomInfo *retRoomInfo = NULL;
	for(int i = 0; i < roomList->size(); i++)
	{
		if(((TypeRoomInfo *) roomList->get(i))->room_id == roomid)
		{
			retRoomInfo = (TypeRoomInfo *) roomList->get(i);
			break;
		}
	}
	if(retRoomInfo == NULL)
	{
		if(roomid == 0)
		{
			retRoomInfo = new TypeRoomInfo(0, 0, 0, 0, (char *)"默认房间");
			//添加一个默认房间
			roomList->add(retRoomInfo);
		}
	}

	return retRoomInfo;
}

int32_t TypeDeviceList::onSetApplianceStatus(int32_t id, int32_t key_id, const char *key_data)
{
	int32_t retError = 0;
	//先查找到这个家电
	TypeApplianceInfo *tempApplianceInfo = onFindApplianceInfo(id);
	if(tempApplianceInfo)
	{
		if(tempApplianceInfo->type == APPLIANCE_TYPE_IKELINK_AIR_CONDITION_PLUG)
		{
			//这个执行的方式有点不一样啦  直接交给服务器去控制咯
			if(onGetFamilyMasterGateway() == pDataBase->onGetGateway_ID())
			{
				//发给服务器执行
				CtrlApplianceRequest ctrlApplianceRequest;
				ctrlApplianceRequest.set_appliance_id(tempApplianceInfo->appID);
				//ctrlApplianceRequest.set_user_id(pDataBase->onGetGateway_ID());
				ctrlApplianceRequest.set_key_id(key_id);
				mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CTRL_REQ, ctrlApplianceRequest.SerializeAsString().c_str(), ctrlApplianceRequest.SerializeAsString().length());
			}
		}
		else
		{
			//再找到这个zigbee设备
			TypeDBDeviceInfo *tempDBDeviceInfo = onCheckFamilyDeviceInfo(IDeviceID, tempApplianceInfo->ir_id);
			if(tempDBDeviceInfo)
			{
				if(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID())
				{
					if(tempDBDeviceInfo->devType == DEVICE_TYPE_RS485_TRANSFER)
					{
						//通过485进行控制
						RS485Profile *temp485Profile = new RS485Profile(key_id, (uint8_t *)key_data, 0, tempApplianceInfo, tempDBDeviceInfo->shortAddr);
						pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
						delete temp485Profile;
					}
					else if(tempDBDeviceInfo->devType == DEVICE_TYPE_IR_REMOTE)
					{
						//通过红外伴侣控制
						//再查找这条指令
						TypeApplianceCodeInfo *tempApplianceCodeInfo = tempApplianceInfo->onFindCMDWithKeyID(key_id);
						if(tempApplianceCodeInfo && (tempApplianceCodeInfo->ir_code != 0) && (tempApplianceCodeInfo->status == 2))//存在并且还要学习了
						{
							if(tempDBDeviceInfo->onLineFlag.bits.status && tempDBDeviceInfo->shortAddr)
							{
								//发送这条红外指令
								TypeChar *irCodeSend = new TypeChar(8);
								uint64_t tempUInt64 = (uint64_t)tempApplianceInfo->appID;
								tempUInt64 <<= 32;
								tempUInt64 |= tempApplianceCodeInfo->ir_code;
								irCodeSend->onAddInt64Ex(0, tempUInt64);
								pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0009, ZCL_DATATYPE_UINT64, irCodeSend->ubuff, 8), 0);
								delete irCodeSend;
							}
							else
							{
								//设备不在线
								retError = 20019;
							}
						}
						else
						{
							//指令不存在 或者说指令未学习
							retError = 20403;
						}
					}
				}
				else
				{
					//不是本网关的空调  发送控制命令给服务器
					CtrlApplianceRequest ctrlApplianceRequest;
					ctrlApplianceRequest.set_appliance_id(tempApplianceInfo->appID);
					//ctrlApplianceRequest.set_user_id(pDataBase->onGetGateway_ID());
					ctrlApplianceRequest.set_key_id(key_id);
					mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CTRL_REQ, ctrlApplianceRequest.SerializeAsString().c_str(), ctrlApplianceRequest.SerializeAsString().length());
				}
			}
			else
			{
				//设备没入网
				retError = 2019;
			}
		}
	}
	else
	{
		//家电不存在
		retError = 20404;
	}
	return retError;
}

//用于控制本网关的设备
int32_t TypeDeviceList::onSetDeviceStatus(TypeDBDeviceInfo * dbdeviceinfo, int32_t sub_id, int32_t status, bool broadcastflag)
{
	int32_t retInt = 0;
	if(dbdeviceinfo)
	{
		TypeDeviceTypeInfo *deviceTypeInfo = dbdeviceinfo->onGetSubInfo(sub_id);
		if(deviceTypeInfo)
		{
			if(onGetConnectFlag())
			{
				if(dbdeviceinfo->gatewayID == pDataBase->onGetGateway_ID())
				{
					deviceTypeInfo->onSetStatus(status);
				}
				else
				{
					//其它网关就让服务器去重新发控制命令
					//分两种情况 1、是zigbee协议设备  2、是其它协议设备
					if(dbdeviceinfo->gatewayID)
					{
						if((deviceTypeInfo->devType == SUB_DEVICE_TYPE_PIR) || (deviceTypeInfo->devType == SUB_DEVICE_TYPE_DOOR_WINDOW))
						{
							//配置安防状态
							onModifyDeviceSecurityStatus(deviceTypeInfo->deviceID, deviceTypeInfo->subID, deviceTypeInfo->devType, status);
						}
						else
						{
							//发送一条控制命令给服务器
							CtrlDeviceRequest ctrlDeviceRequest;
							ctrlDeviceRequest.set_device_id(deviceTypeInfo->deviceID);
							ctrlDeviceRequest.set_sub_id(sub_id);
							ctrlDeviceRequest.set_sub_type(deviceTypeInfo->devType);
							ctrlDeviceRequest.set_gateway_id(dbdeviceinfo->gatewayID);
							ctrlDeviceRequest.set_status(status);
							mfTCPCMDSend(CMD_ID_DEVICE_CTRL_REQ, ctrlDeviceRequest.SerializeAsString().c_str(), ctrlDeviceRequest.SerializeAsString().length());
						}
					}
					else
					{
						//它是其它协议的网关 这个时候要获取主网关，让它控制这个设备
						if(onGetFamilyMasterGateway() == pDataBase->onGetGateway_ID())
						{
							//由本网关负责通知给服务器
							CtrlDeviceRequest ctrlDeviceRequest;
							ctrlDeviceRequest.set_device_id(deviceTypeInfo->deviceID);
							ctrlDeviceRequest.set_sub_id(sub_id);
							ctrlDeviceRequest.set_sub_type(deviceTypeInfo->devType);
							ctrlDeviceRequest.set_gateway_id(dbdeviceinfo->gatewayID);
							ctrlDeviceRequest.set_status(status);
							mfTCPCMDSend(CMD_ID_DEVICE_CTRL_REQ, ctrlDeviceRequest.SerializeAsString().c_str(), ctrlDeviceRequest.SerializeAsString().length());
						}
					}
				}
			}
			else
			{
				if(dbdeviceinfo->gatewayID == pDataBase->onGetGateway_ID())
				{
					deviceTypeInfo->onSetStatus(status);
				}
				else
				{
					if(broadcastflag)
					{
						//需要广播
						cJSON *tempJson = cJSON_CreateObject();
						cJSON_AddLongNumberToObject(tempJson, "udp_type", 104);//
						cJSON_AddNumberToObject(tempJson, "udp_status", (uint32_t)onGetConnectFlag());
						cJSON_AddLongNumberToObject(tempJson, "udp_familyid", (uint64_t)pDataBase->onGetFamilyID());
						cJSON_AddNumberToObject(tempJson, "udp_gatewayid", (uint32_t)pDataBase->onGetGateway_ID());
						cJSON_AddNumberToObject(tempJson, "udp_device_keyid", (uint32_t)deviceTypeInfo->deviceID);
						cJSON_AddNumberToObject(tempJson, "udp_device_subid", (uint32_t)sub_id);
						cJSON_AddNumberToObject(tempJson, "udp_device_status", (uint32_t)status);
						char *retJsonChars = cJSON_Print(tempJson);
						onUDPSend(6666, retJsonChars);
						cJSON_Delete(tempJson);
						free(retJsonChars);
					}
				}
				//然后还要保持一下数据库  主要是安防状态
				//没有网络  直接修改设备状态
				if(deviceTypeInfo->devType == SUB_DEVICE_TYPE_DOOR_WINDOW)
				{
					deviceTypeInfo->onUpdateTypeInfo(dbdeviceinfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(dbdeviceinfo, Event_Dev_Name),
					                                 new TypeDeviceTypeInfo(deviceTypeInfo->deviceID, deviceTypeInfo->subID, deviceTypeInfo->roomID, deviceTypeInfo->iconID, deviceTypeInfo->saveIconID, deviceTypeInfo->name->buff, deviceTypeInfo->saveName->buff,
					                                                        SUB_DEVICE_TYPE_DOOR_WINDOW, 0, new TypeDoorWindowStatus(deviceTypeInfo->onGetSubInfo()->doorWindowStatus->status, deviceTypeInfo->onGetSubInfo()->doorWindowStatus->power, dbdeviceinfo->shortAddr_ex, status)));
					if(broadcastflag)
					{
						//需要广播
						onUDPBroadcastDeviceStatus(deviceTypeInfo->deviceID, deviceTypeInfo->subID, deviceTypeInfo->devType, status);
					}
				}
				else if(deviceTypeInfo->devType == SUB_DEVICE_TYPE_PIR)
				{
					deviceTypeInfo->onUpdateTypeInfo(dbdeviceinfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(dbdeviceinfo, Event_Dev_Name),
					                                 new TypeDeviceTypeInfo(deviceTypeInfo->deviceID, deviceTypeInfo->subID, deviceTypeInfo->roomID, deviceTypeInfo->iconID, deviceTypeInfo->saveIconID, deviceTypeInfo->name->buff, deviceTypeInfo->saveName->buff,
					                                                        SUB_DEVICE_TYPE_PIR, 0, new TypePIRStatus(deviceTypeInfo->onGetSubInfo()->pirStatus->status, deviceTypeInfo->onGetSubInfo()->pirStatus->power, dbdeviceinfo->shortAddr_ex, status, deviceTypeInfo->onGetSubInfo()->pirStatus->outDelayTime)));
					if(broadcastflag)
					{
						//需要广播
						onUDPBroadcastDeviceStatus(deviceTypeInfo->deviceID, deviceTypeInfo->subID, deviceTypeInfo->devType, status);
					}
				}
			}
		}
	}
	else
	{
		retInt = ERROR_DEVICE_IS_UNEXIST;
	}
	return retInt;
}

bool TypeDeviceList::onAddGatewayInfo(TypeGatewayInfo *gatewayinfo, int32_t randvalue)
{
	//查找这个列表有没有这个网关
	if(gatewayinfo)
	{
		TypeGatewayInfo *tempGatewayInfo = NULL;
		for(int i = 0; i < gatewayList->size(); ++ i)
		{
			tempGatewayInfo = (TypeGatewayInfo *)gatewayList->get(i);
			if(tempGatewayInfo->gatewayID == gatewayinfo->gatewayID)
			{
				break;
			}
			tempGatewayInfo = NULL;
		}
		if(tempGatewayInfo)
		{
			if(tempGatewayInfo->roomID != gatewayinfo->roomID)
			{
				//更新网关的房间ID
				tempGatewayInfo->roomID = gatewayinfo->roomID;
			}
			if(tempGatewayInfo->onLine != gatewayinfo->onLine)
			{
				//更新网关在线离线状态
				tempGatewayInfo->onLine = gatewayinfo->onLine;
			}
			tempGatewayInfo->randValue = randvalue;
			delete gatewayinfo;
		}
		else
		{
			//添加这个网关
			gatewayinfo->randValue = randvalue;
			gatewayList->add(gatewayinfo);
		}
	}
	return TRUE;
}

int64_t TypeDeviceList::onGetFamilyMasterGateway()
{
	int64_t retGatewayID = 0;
	if(onGetConnectFlag())//如果自己都不在线就不要管别人了
	{
		TypeGatewayInfo *tempGatewayInfo = NULL;
		for(int i = 0; i < gatewayList->size(); ++ i)
		{
			tempGatewayInfo = (TypeGatewayInfo *)gatewayList->get(i);
			if(tempGatewayInfo && tempGatewayInfo->onLine && (tempGatewayInfo->gatewayID > retGatewayID))
			{
				retGatewayID = tempGatewayInfo->gatewayID;
			}
		}
	}
	return retGatewayID;
}

bool TypeDeviceList::onDisAlarmInfo(int64_t devid, int32_t type, bool broadcastflag)
{
	//解除报警  需要考虑一下没有网络的情况，解除报警
	TypeDBDeviceInfo *tempDBDeviceInfo = onCheckFamilyDeviceInfo(IDeviceID, devid);
	if(tempDBDeviceInfo)
	{
		TypeDeviceTypeInfo *tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(1);
		if(tempDeviceTypeInfo)
		{
			if(type == 2)
			{
				if(onGetConnectFlag())
				{
					//撤防
					if((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DOOR_WINDOW) || (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_PIR))
					{
						onModifyDeviceSecurityStatus(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, 0);
					}
				}
				//直接修改本地数据库吧
				if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DOOR_WINDOW)
				{
					tempDeviceTypeInfo->onUpdateTypeInfo(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Name),
					                                     new TypeDeviceTypeInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->roomID, tempDeviceTypeInfo->iconID, tempDeviceTypeInfo->saveIconID, tempDeviceTypeInfo->name->buff, tempDeviceTypeInfo->saveName->buff,
					                                                            SUB_DEVICE_TYPE_DOOR_WINDOW, 0, new TypeDoorWindowStatus(tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->status, tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->power, tempDBDeviceInfo->shortAddr_ex, 0)));
				}
				else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_PIR)
				{
					tempDeviceTypeInfo->onUpdateTypeInfo(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Name),
					                                     new TypeDeviceTypeInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->roomID, tempDeviceTypeInfo->iconID, tempDeviceTypeInfo->saveIconID, tempDeviceTypeInfo->name->buff, tempDeviceTypeInfo->saveName->buff,
					                                                            SUB_DEVICE_TYPE_PIR, 0, new TypePIRStatus(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status, tempDeviceTypeInfo->onGetSubInfo()->pirStatus->power, tempDBDeviceInfo->shortAddr_ex, 0, tempDeviceTypeInfo->onGetSubInfo()->pirStatus->outDelayTime)));
				}
			}
			if(onGetConnectFlag())
			{
				DeviceAlarmReleasedNotification releaseRequest;
				releaseRequest.set_family_id(pDataBase->onGetFamilyID());
				releaseRequest.set_device_id(tempDeviceTypeInfo->deviceID);
				releaseRequest.set_sub_id(tempDeviceTypeInfo->subID);
				releaseRequest.set_sub_type(tempDeviceTypeInfo->devType);
				mfTCPCMDSend(CMD_ID_DEVICE_ALARM_RELEASE_REQ, releaseRequest.SerializeAsString().c_str(), releaseRequest.SerializeAsString().length());
			}
			else
			{
				//本地解除一下吧
				TypeChar *alarmInfo = new TypeChar(7);//给设备发送解除报警
				alarmInfo->buff[0] = 6;
				alarmInfo->buff[1] = 0;
				alarmInfo->buff[2] = 0;
				alarmInfo->buff[3] = 0;
				alarmInfo->buff[4] = 0;
				alarmInfo->buff[5] = 0;
				alarmInfo->buff[6] = 0;
				pmMasterSerialPort->onWriteZclCMD((uint32_t)SHORTADDR_BROADCAST, (uint8_t)1, CLUSTER_ID_PERSONAL, 0x01, alarmInfo->ubuff, 7, 0);
				mIsAlarmingFlag = FALSE;
				delete alarmInfo;
			}
			if(broadcastflag)
			{
				onUDPDisAlarmInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, type);
			}
			else
			{
				onNotifyToJava(JNI_NOTIFY_ALARM, 0, 0, 0, "");
			}
		}
	}
	return TRUE;
}

bool TypeDeviceList::onSetDeviceStatusFlag(TypeDBDeviceInfo * dbdeviceinfo, int32_t subid, int32_t type)
{
	if(dbdeviceinfo)
	{
		switch(type)
		{
			case DEVICE_VALUE_FLAG_ONOFF:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_ONFF1 << ((subid - 1) * 2);
				break;
			case DEVICE_VALUE_FLAG_LEVEL:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_LEVEL1 << ((subid - 1) * 2);
				break;
			case DEVICE_VALUE_FLAG_ENV_TEMPERATURE:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_TEMP;
				break;
			case DEVICE_VALUE_FLAG_ENV_ILLUM_INTENSITY:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_ILLUM;
				break;
			case DEVICE_VALUE_FLAG_ENV_HUMIDITY:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_HUMID;
				break;
			case DEVICE_VALUE_FLAG_ENV_QUALITY:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_QUALITY;
				break;
			case DEVICE_VALUE_FLAG_GAS_DENSITY:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_GAS;
				break;
			case DEVICE_VALUE_FLAG_ENV_PM25:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_PM25;
				break;
			case DEVICE_VALUE_FLAG_POWER:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_POWER;
				break;
			case DEVICE_VALUE_FLAG_ENV_CO2:
				dbdeviceinfo->statusFlag |= STATUS_CLEAR_CO2;
				break;
			default:
				break;
		}
	}
	return true;
}

//状态确定机制
bool TypeDeviceList::onClearDeviceStatusFlag(TypeDBDeviceInfo * dbdeviceinfo)
{
	if((dbdeviceinfo != NULL) && (dbdeviceinfo->statusFlag != 0))
	{
		uint32_t tempUInt = 1;
		TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
		TypeChar * tempSendBuff = new TypeChar(128);
		tempSendBuff->onAddInt32(tempUInt, dbdeviceinfo->statusFlag);
		tempUInt += 4;
		int tempIndex = 0;
		while((dbdeviceinfo->statusFlag) && (tempIndex < 32))
		{
			if(dbdeviceinfo->statusFlag & (1 << tempIndex))
			{
				dbdeviceinfo->statusFlag &= ~(1 << tempIndex);
				switch(1 << tempIndex)
				{
					case STATUS_CLEAR_ONFF1:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_LIGHT:
								case SUB_DEVICE_TYPE_SWITCH:
								case SUB_DEVICE_TYPE_DIMMER:
								case SUB_DEVICE_TYPE_GAS_ARM:
								case SUB_DEVICE_TYPE_CURTAIN:
								case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:
									if(tempDeviceTypeInfo->onGetStatus() == 0)
									{
										tempSendBuff->onAddInt16(tempUInt, 0);
									}
									else
									{
										tempSendBuff->onAddInt16(tempUInt, 1);
									}
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_LEVEL1:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_DIMMER:tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->onGetLevel());tempUInt += 2;break;
								case SUB_DEVICE_TYPE_CURTAIN:tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->onGetLevel());tempUInt += 2;break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_ONFF2:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(2);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_LIGHT:
								case SUB_DEVICE_TYPE_DIMMER:
								case SUB_DEVICE_TYPE_CURTAIN:
									if(tempDeviceTypeInfo->onGetStatus() == 0)
									{
										tempSendBuff->onAddInt16(tempUInt, 0);
									}
									else
									{
										tempSendBuff->onAddInt16(tempUInt, 1);
									}
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_LEVEL2:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(2);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_DIMMER:tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->onGetLevel());tempUInt += 2;break;
								case SUB_DEVICE_TYPE_CURTAIN:tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->onGetLevel());tempUInt += 2;break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_ONFF3:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(3);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_LIGHT:
									if(tempDeviceTypeInfo->onGetStatus() == 0)
									{
										tempSendBuff->onAddInt16(tempUInt, 0);
									}
									else
									{
										tempSendBuff->onAddInt16(tempUInt, 1);
									}
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_LEVEL3:break;
					case STATUS_CLEAR_ONFF4:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(4);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_LIGHT:
									if(tempDeviceTypeInfo->onGetStatus() == 0)
									{
										tempSendBuff->onAddInt16(tempUInt, 0);
									}
									else
									{
										tempSendBuff->onAddInt16(tempUInt, 1);
									}
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_LEVEL4:break;
					case STATUS_CLEAR_TEMP:
					{
						tempSendBuff->onAddInt16(tempUInt, dbdeviceinfo->tempperature);
						tempUInt += 2;
					}
						break;
					case STATUS_CLEAR_ILLUM:
					{
						tempSendBuff->onAddInt16(tempUInt, dbdeviceinfo->illumination);
						tempUInt += 2;
					}
						break;
					case STATUS_CLEAR_HUMID://只有环境有这个属性
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_ENV_DETECTOR:
									tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->humiSensorValue);
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_QUALITY://只有环境探测有这个属性
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_ENV_DETECTOR:
									tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->airLevel);
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_GAS:
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_GAS:
									tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetStatus());
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_PM25://只有环境才有这个属性
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_ENV_DETECTOR:
									tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->env_detectorStatus->pm25Value);
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					case STATUS_CLEAR_POWER://智能插座对有这个属性
					{
						tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
						if(tempDeviceTypeInfo != NULL)
						{
							switch(tempDeviceTypeInfo->devType)
							{
								case SUB_DEVICE_TYPE_SWITCH:
									tempSendBuff->onAddInt16(tempUInt, tempDeviceTypeInfo->onGetSubInfo()->switchStatus->currentPower);
									tempUInt += 2;
									break;
								default:break;
							}
						}
					}
						break;
					default:break;
				}
			}
			tempIndex++;
		}
		tempSendBuff->ubuff[0] = (uint8_t)(tempUInt - 1);
		pmMasterSerialPort->onWriteAttribute((uint32_t)dbdeviceinfo->shortAddr, 1, 0x0000, new TypeZclAttribute(0x4003, ZCL_DATATYPE_CHAR_STR, tempSendBuff->ubuff, (uint8_t)tempUInt), 0);
		delete tempSendBuff;
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

bool TypeDeviceList::onDeleteSceneInfo(TypeSceneNameInfo *scenenameinfo)
{
	if(scenenameinfo != NULL)
	{
		//删除数据库
		pDataBase->onDeleteDataBase("scenenameinfo", "sceneid", scenenameinfo->scene_id);
		pDataBase->onDeleteDataBase("sceneactioninfo", "sceneid", scenenameinfo->scene_id);
		pDataBase->onDeleteDataBase("scenecondinfo", "sceneid", scenenameinfo->scene_id);
		sceneList->removeObject(scenenameinfo);
		return true;
	}
	return false;
}

bool TypeDeviceList::onSetSceneStatus(TypeSceneNameInfo *scenenameinfo, int newstatus, bool report)
{
	//得到当前时间
	bool retBool = FALSE;
	uint32_t tempUInt = 0;
	uint32_t tempWeek = 0;
	onGetCurrentTime(&tempUInt, &tempWeek);
	if(scenenameinfo && (scenenameinfo->status != newstatus))
	{
		mPrintf(Log_NetWork, "ID=%d, Into Scene = %d ", scenenameinfo->scene_id, newstatus);
		int saveStatus = scenenameinfo->status;
		if((newstatus == 0) || (newstatus && scenenameinfo->onGetDisabled(tempUInt, tempWeek)))
		{
			TypeCarriedSceneList *tempCarriedSceneList = NULL;
			for(int i = 0; i < carriedOutSceneList->size(); ++i)//当前场景动作执行列表
			{
				tempCarriedSceneList = (TypeCarriedSceneList *)carriedOutSceneList->get(i);
				if(tempCarriedSceneList->scene_id == scenenameinfo->scene_id)//当前场景有正在执行
				{
					break;
				}
				else
				{
					tempCarriedSceneList = NULL;
				}
			}
			if(tempCarriedSceneList != NULL)//当前场景正在处理执行状态
			{
				if(tempCarriedSceneList->status != newstatus)
				{
                    scenenameinfo->status = newstatus;//更新成现在的状态
					//更新当前场景动作值
					tempCarriedSceneList->onUpdateCarriedScene(scenenameinfo);
					//mPrintf(Log_NetWork, "Scene update ");
				}
			}
			else
			{
				//执行一个新的场景
				scenenameinfo->status = newstatus;
				carriedOutSceneList->add(new TypeCarriedSceneList(scenenameinfo));
				//mPrintf(Log_NetWork, "Scene new ");
			}
			retBool = TRUE;
		}
		if(scenenameinfo->status != saveStatus)
		{
			//要遍历一下所有设备，是有按键与这个场景存在绑定关系  同步按键和场景状态
			TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
			TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
			for (int i = 0; i < dbDeviceInfoList->size(); ++i)
			{
				tempDBDeviceInfo = (TypeDBDeviceInfo *)dbDeviceInfoList->get(i);
				if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()) && ((tempDBDeviceInfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_1 || tempDBDeviceInfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_2 || tempDBDeviceInfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_3 || tempDBDeviceInfo->devType == DEVICE_TYPE_LIGHT_CHANNEL_4)))
				{
					for(int j = 1; j <= tempDBDeviceInfo->subCount; j++)
					{
						tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT))//目前只有灯光可以替换成场景使用
						{
							if(tempDeviceTypeInfo->onGetSubInfo()->lightStatus->sceneID == scenenameinfo->scene_id)
							{
								//mPrintf(Log_NetWork, "status = %d", sceneInfoChangedNotification.scene().status());
								onSetDeviceStatus(tempDBDeviceInfo, j, scenenameinfo->status, TRUE);
							}
						}
					}
				}
			}
			if(report)
			{
				onUpdateSceneStatusInfo(scenenameinfo->scene_id, scenenameinfo->status);//发送广播 我在执行退出
			}
		}
	}
	return retBool;
}

bool TypeDeviceList::onCheckSceneCarried(int32_t ttype, int32_t device_id, int32_t subid, int32_t action, char *action_desc)
{
	TypeSceneNameInfo *tempSceneNameInfo = NULL;
	TypeSceneCondInfo *tempSceneCondInfo = NULL;
	for(int i = 0; i < sceneList->size(); ++i)
	{
		tempSceneNameInfo = (TypeSceneNameInfo *)sceneList->get(i);
		for(int j = 0; j < tempSceneNameInfo->onCondInfoList->size(); ++j)
		{
			tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->onCondInfoList->get(j);
			if((tempSceneCondInfo->type == ttype) && (tempSceneCondInfo->device_id == device_id) && (tempSceneCondInfo->sub_id == subid) && (tempSceneCondInfo->action == action) && tempSceneCondInfo->onCheckActionDesc(action_desc))
			{
				//进入一个场景
				if(tempSceneNameInfo->status == 0)
				{
					onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
				}
			}
		}
		for(int j = 0; j < tempSceneNameInfo->offCondInfoList->size(); ++j)
		{
			tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->offCondInfoList->get(j);
			if((tempSceneCondInfo->type == ttype) && (tempSceneCondInfo->device_id == device_id) && (tempSceneCondInfo->sub_id == subid) && (tempSceneCondInfo->action == action) && tempSceneCondInfo->onCheckActionDesc(action_desc))
			{
				//进入一个场景
				if(tempSceneNameInfo->status == 1)
				{
					onSetSceneStatus(tempSceneNameInfo, 0, TRUE);
				}
			}
		}
	}
	return TRUE;
}

bool TypeDeviceList::onCheckSceneCarried(int32_t ttype, int32_t device_id, int32_t subid, int32_t action)
{
	return onCheckSceneCarried(ttype, device_id, subid, action, NULL);
}

bool TypeDeviceList::onCheckDevOnLine(bool ismater, uint32_t shortaddr)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	if(ismater)
	{
		tempDBDeviceInfo = onCheckGatewayDeviceInfo(IShortAddr, shortaddr);
	}
	else
	{
		tempDBDeviceInfo = onCheckGatewayDeviceInfo(IShortAddr_Ex, shortaddr);
	}
	if(tempDBDeviceInfo)
	{
		return tempDBDeviceInfo->onLineFlag.bits.status;
	}
	else
	{
		return FALSE;
	}
}

TypeSceneNameInfo *TypeDeviceList::onAddSceneInfo(TypeSceneNameInfo *scenenameinfo, int32_t randvalue)
{
	//如果已经存在  需要判断是否有场景信息更新
	if(scenenameinfo)
	{
		TypeSceneNameInfo *tempSceneNameInfo = onFindSceneInfo(scenenameinfo->scene_id);
		if(tempSceneNameInfo == NULL)
		{
			sceneList->add(scenenameinfo);
			if(randvalue)
			{
				//添加到数据库
				pDataBase->onUpdateSceneNameInfo(scenenameinfo, SceneInset, 0);
			}
			scenenameinfo->randValue = randvalue;
			return scenenameinfo;
		}
		else if(pDataBase)
		{
			//更新场景的状态，以本地为主
			if(tempSceneNameInfo->status != scenenameinfo->status)
			{
				//向服务器更新场景的状态
				tempSceneNameInfo->status = scenenameinfo->status;
				onUpdateSceneStatusInfo(scenenameinfo->scene_id, tempSceneNameInfo->status);
			}
			//更新场景信息
			if(tempSceneNameInfo->name->onStringCMP(scenenameinfo->name->buff) == FALSE)//更新场景名称
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneName, scenenameinfo->name->buff);
				//发送通知给app
				if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_SCENENAME)
				{
					onNotifyToJava(JNI_NOTIFY_UPDATE_SCENENAME, tempSceneNameInfo->scene_id, tempSceneNameInfo->room_id, tempSceneNameInfo->icon_id, scenenameinfo->name->buff);
				}
			}
			if(tempSceneNameInfo->period->onStringCMP(scenenameinfo->period->buff) == FALSE)//更新
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, ScenePeriod, scenenameinfo->period->buff);
				tempSceneNameInfo->onSetPeriod(scenenameinfo->period->buff);//更新一下另个一个临时标志
			}
			if(tempSceneNameInfo->enabledTime->onStringCMP(scenenameinfo->enabledTime->buff) == FALSE)//更新
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneEnableTime, scenenameinfo->enabledTime->buff);
				tempSceneNameInfo->onSetEnableTime(scenenameinfo->enabledTime->buff);//更新一下另个一个临时标志
			}
			if(tempSceneNameInfo->room_id != scenenameinfo->room_id)//更新场景房间
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneRoomID, scenenameinfo->room_id);
			}
			if(tempSceneNameInfo->icon_id != scenenameinfo->icon_id)//更新场景图标
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneIconID, scenenameinfo->icon_id);
			}
			if(tempSceneNameInfo->specialized != scenenameinfo->specialized)//
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneSpecialized, scenenameinfo->specialized);
			}
			if(tempSceneNameInfo->disabled != scenenameinfo->disabled)//
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneDisabled, scenenameinfo->disabled);
			}
			if((tempSceneNameInfo->hidden & 0x0F) != scenenameinfo->hidden)//
			{
				pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneHidden, (tempSceneNameInfo->hidden & 0xF0) | (scenenameinfo->hidden & 0x0F));
			}
			tempSceneNameInfo->randValue = randvalue;
			delete scenenameinfo;
			return tempSceneNameInfo;
		}
	}
	return scenenameinfo;
}

TypeSceneActionInfo * TypeDeviceList::onAddSceneActionInfo(TypeSceneNameInfo *scenenameinfo, TypeSceneActionInfo *actioninfo, int32_t randvalue)
{
	if(scenenameinfo && actioninfo)
	{
		TypeSceneActionInfo *tempSceneActionInfo = scenenameinfo->onFindSceneActionInfo(actioninfo->action_type, actioninfo->scene_action_id);
		if(tempSceneActionInfo == NULL)
		{
			//添加到action 列表
			if(actioninfo->action_type)
			{
				scenenameinfo->onActionInfoList->add(actioninfo);
			}
			else
			{
				scenenameinfo->offActionInfoList->add(actioninfo);
			}
			if(randvalue)
			{
				//添加到数据库
				pDataBase->onUpdateSceneActionInfo(actioninfo, SceneActionInset, 0);
			}
			actioninfo->randValue = randvalue;
			return actioninfo;
		}
		else if(pDataBase)
		{
			//更新action值
			if(tempSceneActionInfo->action != actioninfo->action)
			{
				//这个要更新到数据库
				pDataBase->onUpdateSceneActionInfo(tempSceneActionInfo, SceneActionAction, actioninfo->action);
			}
			if(tempSceneActionInfo->sub_id != actioninfo->sub_id)
			{
				pDataBase->onUpdateSceneActionInfo(tempSceneActionInfo, SceneActionSubID, actioninfo->sub_id);
			}
			if(tempSceneActionInfo->onGetDelayTime() != actioninfo->onGetDelayTime())
			{
				//这个要更新到数据库
				pDataBase->onUpdateSceneActionInfo(tempSceneActionInfo, SceneActionDelayTime, actioninfo->onGetDelayTime() / 1000);
			}
			if(!tempSceneActionInfo->action_desc->onStringCMP(actioninfo->action_desc->buff))
			{
				pDataBase->onUpdateSceneActionInfo(tempSceneActionInfo, SceneActionActionDesc, actioninfo->action_desc->buff);
			}
			//其它信息理论上都不会修改
			tempSceneActionInfo->randValue = randvalue;
			delete actioninfo;
			return tempSceneActionInfo;
		}
	}
	return actioninfo;
}

TypeSceneCondInfo * TypeDeviceList::onAddSceneCondInfo(TypeSceneNameInfo *scenenameinfo, TypeSceneCondInfo *condinfo, int32_t randvalue)
{
	if(scenenameinfo && condinfo)
	{
		TypeSceneCondInfo *tempSceneCondInfo = scenenameinfo->onFindSceneCondInfo(condinfo->cond_type, condinfo->scene_cond_id);
		if(tempSceneCondInfo == NULL)
		{
			//添加到action 列表
			if(condinfo->cond_type)
			{
				scenenameinfo->onCondInfoList->add(condinfo);
			}
			else
			{
				scenenameinfo->offCondInfoList->add(condinfo);
			}
			if(randvalue)
			{
				//添加到数据库
				pDataBase->onUpdateSceneCondInfo(condinfo, SceneCondInset, 0);
			}
			condinfo->randValue = randvalue;
			return condinfo;
		}
		else if(pDataBase)
		{
			//更新action值
			if(tempSceneCondInfo->action != condinfo->action)
			{
				//这个要更新到数据库
				pDataBase->onUpdateSceneCondInfo(tempSceneCondInfo, SceneCondAction, condinfo->action);
			}
			if(tempSceneCondInfo->onGetDelayTime() != condinfo->onGetDelayTime())
			{
				//这个要更新到数据库
				pDataBase->onUpdateSceneCondInfo(tempSceneCondInfo, SceneCondDelayTime, condinfo->onGetDelayTime() / 1000);
			}
			if(!tempSceneCondInfo->action_desc->onStringCMP(condinfo->action_desc->buff))
			{
				pDataBase->onUpdateSceneCondInfo(tempSceneCondInfo, SceneCondActionDesc, condinfo->action_desc->buff);
			}
			//其它信息理论上都不会修改
			tempSceneCondInfo->randValue = randvalue;
			delete condinfo;
			return tempSceneCondInfo;
		}
	}
	return condinfo;
}

TypeApplianceInfo *TypeDeviceList::onFindApplianceInfo(int32_t appid)
{
	TypeApplianceInfo *retApplianceInfo = NULL;
	for(int i = 0; i < applianceList->size(); ++ i)
	{
		retApplianceInfo = (TypeApplianceInfo *)applianceList->get(i);
		if(retApplianceInfo->appID == appid)
		{
			break;
		}
		else
		{
			retApplianceInfo = NULL;
		}
	}
	return retApplianceInfo;
}

TypeApplianceInfo *TypeDeviceList::onFindApplianceInfoKeyIDAndAddr(int32_t keyid, int32_t taddr)
{
	TypeApplianceInfo *retApplianceInfo = NULL;
	for(int i = 0; i < applianceList->size(); ++ i)
	{
		retApplianceInfo = (TypeApplianceInfo *)applianceList->get(i);
		if(retApplianceInfo->ir_id == keyid)
		{
			if(taddr)
			{
				if(retApplianceInfo->addr == taddr)
				{
					break;
				}
			}
			else
			{
				break;
			}
		}
		retApplianceInfo = NULL;
	}
	return retApplianceInfo;
}

TypeApplianceInfo *TypeDeviceList::onFindApplianceInfoTypeAndSerial(int32_t type, char *serial)
{
	TypeApplianceInfo *retApplianceInfo = NULL;
	for(int i = 0; i < applianceList->size(); ++ i)
	{
		retApplianceInfo = (TypeApplianceInfo *)applianceList->get(i);
		if(retApplianceInfo && (retApplianceInfo->type == type) && (retApplianceInfo->serial->onStringCMP(serial)))
		{
			break;
		}
		else
		{
			retApplianceInfo = NULL;
		}
	}
	return retApplianceInfo;
}

TypeSceneNameInfo *TypeDeviceList::onFindSceneInfo(int64_t sceneid)
{
	TypeSceneNameInfo *sceneNameInfo = NULL;
	for(int i = 0; i < sceneList->size(); i++)
	{
		sceneNameInfo = (TypeSceneNameInfo *) sceneList->get(i);
		if(sceneNameInfo->scene_id == sceneid)
		{
			break;
		}
		else
		{
			sceneNameInfo = NULL;
		}
	}
	return sceneNameInfo;
}

bool TypeDeviceList::onResetDeviceDBInfo(TypeDBDeviceInfo *dbdeviceinfo)
{
	if(dbdeviceinfo)
	{
		if(onCheckDeviceEvent(dbdeviceinfo, Event_Dev_Name))
		{
			for(int i = 1; i <= dbdeviceinfo->subCount; ++ i)
			{
				TypeDeviceTypeInfo *tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(i);
				if(tempDeviceTypeInfo)
				{
					pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubSaveName, (char *)"_F#DSWs");//删除名称信息
					pDataBase->onAddDevEventInfo(dbdeviceinfo->deviceID, i, Event_Dev_Name, 0);
				}
			}
		}
		if(onCheckDeviceEvent(dbdeviceinfo, Event_Dev_RGB))
		{
			//清除RGB内容
			pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, ISaveRgb, 1);
			pDataBase->onAddDevEventInfo(dbdeviceinfo->deviceID, 1, Event_Dev_RGB, 0);
		}

		//还需要恢复调光的配置信息
		if(dbdeviceinfo->devType == DEVICE_TYPE_DIMMER_CHANNEL_1)
		{
			TypeDeviceTypeInfo *tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
			if(tempDeviceTypeInfo)
			{
				tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue = onGetDimmingParaValue(10, 90, 1200);
				pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDimmingSaveParaValue, tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
			}
		}
		else if(dbdeviceinfo->devType == DEVICE_TYPE_DIMMER_CHANNEL_2)
		{
			TypeDeviceTypeInfo *tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
			if(tempDeviceTypeInfo)
			{
				tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue = onGetDimmingParaValue(10, 90, 1200);
				pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDimmingSaveParaValue, tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
			}
			tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(2);
			if(tempDeviceTypeInfo)
			{
				tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue = onGetDimmingParaValue(10, 90, 1200);
				pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubDimmingSaveParaValue, tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
			}
		}
		else if(dbdeviceinfo->devType == DEVICE_TYPE_RS485_TRANSFER)
		{
			//清除一下485的参数信息，毕竟复位这些设备参数信息是变了的
			TypeDeviceTypeInfo *tempDeviceTypeInfo = dbdeviceinfo->onGetSubInfo(1);
			if(tempDeviceTypeInfo)
			{
				tempDeviceTypeInfo->subInfo.rs485Status->saveStatus = 0;
				pDataBase->onUpdateSubDeviceInfo(tempDeviceTypeInfo, SubRS485Para, tempDeviceTypeInfo->subInfo.rs485Status->saveStatus);
			}
		}
	}
	return TRUE;
}

TypeDeviceList::~TypeDeviceList()
{
	if(roomList != NULL)
	{
		delete roomList;
	}
	if(sceneList != NULL)
	{
		delete sceneList;
	}
	delete carriedOutSceneList;
	delete dbDeviceInfoList;
	delete applianceList;
	delete gatewayList;
}

