//
// Created by wenyu xia on 2018/7/2.
//

#include "../Main/WinobleMain.h"

//处理命令
RS485Profile::RS485Profile(int32_t attrid, uint8_t *buff, uint8_t len, TypeDeviceTypeInfo *deviceinfo)
{
	sendBuff = NULL;
	sendLen = 0;
	if(deviceinfo)
	{
		if(deviceinfo->devType == SUB_DEVICE_TYPE_RS485_TRANSFER)
		{
			//得到这个家电
			TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfoKeyIDAndAddr(deviceinfo->deviceID, 0);
			if(tempApplianceInfo != NULL)
			{
				if(tempApplianceInfo->type == APPLIANCE_TYPE_DOOR_LOCK)//一个485协议转换器只能绑定一个门锁
				{
					TypeSmartDoorLockHLS *tempSmartDoorLockHLS = new TypeSmartDoorLockHLS(buff, len);
					tempSmartDoorLockHLS->onToProcessCMD(tempApplianceInfo);
					delete tempSmartDoorLockHLS;
					if(attrid == 0x0011)
					{
						//发送一条清除标志数据
						uint8_t tempChar[2];
						tempChar[0] = 1;
						tempChar[1] = 0;
						pmMasterSerialPort->onWriteAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, tempChar, 2), 0);
					}
				}
				else if(tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION)//一个485协议转换器能添加总线上的多个中央空调
				{
                    if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_MD) == 0)//MEDIA
					{
						//美的中央空调控制网关
						TypeCentralAirConditioningMD *tempCentralAirConditioning = new TypeCentralAirConditioningMD(buff, len);
						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempCentralAirConditioning;
						if(attrid == 0x0011)
						{
							//发送一条清除标志数据
							uint8_t tempChar[2];
							tempChar[0] = 1;
							tempChar[1] = 0;
							pmMasterSerialPort->onWriteAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, tempChar, 2), 0);
						}
					}
                    else if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)
                    {
                        //温控器，包含中央空调，新风，地暖
                        TypeTemperatureControl *tempCentralAirConditioning = new TypeTemperatureControl(buff, len);
                        tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
                        delete tempCentralAirConditioning;
                    }
					else //if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_ZH) == 0)
					{
						//中宏中央空调控制器
						TypeCentralAirConditioningZH *tempCentralAirConditioning = new TypeCentralAirConditioningZH(buff, len);
						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempCentralAirConditioning;
						if(attrid == 0x0011)
						{
							//发送一条清除标志数据
							uint8_t tempChar[2];
							tempChar[0] = 1;
							tempChar[1] = 0;
							pmMasterSerialPort->onWriteAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, tempChar, 2), 0);
						}
					}
				}
                else if(tempApplianceInfo->type == APPLIANCE_TYPE_VENTILATION_SYSTEM)
                {
//                    mPrintf(Log_DataBase,"manufacturer:%s %d",tempApplianceInfo->manufacturer->buff,strlen(tempApplianceInfo->manufacturer->buff));
                    if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)
                    {
						//温控器，新风
						TypeTemperatureControl *tempCentralAirConditioning = new TypeTemperatureControl(buff, len);
						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempCentralAirConditioning;
                    }
                    else if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_LF) == 0)
					{
						//温控器，新风
						TypeTemperatureControlLF *tempCentralAirConditioning = new TypeTemperatureControlLF(buff, len);
						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempCentralAirConditioning;
					}
                }
                else if(tempApplianceInfo->type == APPLIANCE_TYPE_FLOOR_HEATING)
                {
//                    mPrintf(Log_DataBase,"manufacturer:%s %d",tempApplianceInfo->manufacturer->buff,strlen(tempApplianceInfo->manufacturer->buff));
                    if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)//鸿雁地暖
                    {
////                        //温控器，地暖
						TypeTemperatureControl *tempCentralAirConditioning = new TypeTemperatureControl(buff, len);
						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempCentralAirConditioning;
                        //温控器，地暖,拉斐
//						TypeTemperatureControlLF *tempCentralAirConditioning = new TypeTemperatureControlLF(buff, len);
//						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
//						delete tempCentralAirConditioning;

                    }
					else if(strcmp(tempApplianceInfo->manufacturer->buff,CENTRAL_AIR_MANU_LF) == 0)//拉斐地暖
					{
						TypeTemperatureControlLF *tempCentralAirConditioning = new TypeTemperatureControlLF(buff, len);
						tempCentralAirConditioning->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempCentralAirConditioning;
					}
                    else
                    {

                    }
                }
				else if(tempApplianceInfo->type == APPLIANCE_TYPE_RGBW_LIGHT)
				{
					//没有返回
					if(attrid == 0x0011)
					{
						//发送一条清除标志数据
						uint8_t tempChar[2];
						tempChar[0] = 1;
						tempChar[1] = 0;
						pmMasterSerialPort->onWriteAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, tempChar, 2), 0);
					}
				}
				else if(tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN)
				{
					if(tempApplianceInfo->manufacturer && (strlen(tempApplianceInfo->manufacturer->buff) == 8))
					{
						//这里是丙申电动窗帘
						TypeElectricCurtainBS *tempElectricCurtaindy = new TypeElectricCurtainBS(buff, len);
						tempElectricCurtaindy->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempElectricCurtaindy;
						if(attrid == 0x000C)
						{
							//延时读取一下监控命令表
							pmMasterSerialPort->onReadAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, 0x10, 200);
						}
					}
					else if(tempApplianceInfo->manufacturer && (strlen(tempApplianceInfo->manufacturer->buff) == 9))//松下电动窗帘
					{
						//这里是丙申电动窗帘
						TypeElectricCurtainSX *tempElectricCurtainsx = new TypeElectricCurtainSX(buff, len);
						tempElectricCurtainsx->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempElectricCurtainsx;
						if(attrid == 0x000C)
						{
							//延时读取一下监控命令表
							pmMasterSerialPort->onReadAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, 0x10, 200);
						}
					}
                    else if(tempApplianceInfo->manufacturer && (strcmp(tempApplianceInfo->manufacturer->buff,EL_CURTAIN_AIR_AK) == 0))	//奥科涂鸦电动窗帘
                    {
                        //这里是奥科涂鸦电动窗帘
                        TypeElectricCurtainAK *tempElectricCurtainak = new TypeElectricCurtainAK(buff, len);
                        tempElectricCurtainak->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
                        delete tempElectricCurtainak;
                        if(attrid == 0x000C)
                        {
                            //延时读取一下监控命令表
                            pmMasterSerialPort->onReadAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, 0x10, 200);
                        }
                    }
                    else if(tempApplianceInfo->manufacturer && (strcmp(tempApplianceInfo->manufacturer->buff,EL_CURTAIN_AIR_485_AK) == 0))	//奥科485电动窗帘
                    {
                        //这里是奥科485电动窗帘
                        TypeElectricCurtain485AK *tempElectricCurtain485ak = new TypeElectricCurtain485AK(buff, len);
                        tempElectricCurtain485ak->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
                        delete tempElectricCurtain485ak;
                        if(attrid == 0x000C)
                        {
                            //延时读取一下监控命令表
                            pmMasterSerialPort->onReadAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, 0x10, 200);
                        }
                    }
					else
					{
						//这里是杜亚电动窗帘
						TypeElectricCurtainDY *tempElectricCurtaindy = new TypeElectricCurtainDY(buff, len);
						tempElectricCurtaindy->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempElectricCurtaindy;
						if(attrid == 0x0011)
						{
							//发送一条清除标志数据
							uint8_t tempChar[2];
							tempChar[0] = 1;
							tempChar[1] = 0;
							pmMasterSerialPort->onWriteAttribute((uint32_t)deviceinfo->onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, tempChar, 2), 0);
						}
					}
				}
				else if(tempApplianceInfo->type == APPLIANCE_TYPE_ROBOT_HUAYI_VM1)
				{
					TypeVoiceHW *tempVoiceHW = new TypeVoiceHW(buff, len);
					tempVoiceHW->lastRoomID = tempApplianceInfo->addr;
					tempVoiceHW->lastCMDID = tempApplianceInfo->saveValue;
					tempVoiceHW->onToProcessCMD(tempApplianceInfo->roomID);
					tempApplianceInfo->addr = tempVoiceHW->lastRoomID;
					tempApplianceInfo->saveValue = tempVoiceHW->lastCMDID;
					delete tempVoiceHW;
				}
				else if(tempApplianceInfo->type == APPLIANCE_TYPE_AIR_SWITCH)
				{
					if(strcmp(tempApplianceInfo->manufacturer->buff,AIR_SWITCH_MD_AIR) == 0)//曼顿空开
					{
////                        //空气开关
						TypeAirSwitch *tempAirSwitch = new TypeAirSwitch(buff, len);
						tempAirSwitch->onToProcessCMD(deviceinfo->onGetShortAddr(), tempApplianceInfo);
						delete tempAirSwitch;

					}
				}
			}
		}
	}
}

//生成命令
RS485Profile::RS485Profile(int32_t tcmdid, uint8_t *tbuff, int32_t len, TypeApplianceInfo *applianceinfo, int32_t tshortaddr)
{
	sendBuff = NULL;
	sendLen = 0;
	if(applianceinfo && tshortaddr)
	{
		if(applianceinfo->type == APPLIANCE_TYPE_DOOR_LOCK)
		{
			TypeSmartDoorLockHLS *tempSmartDoorLockHLS = new TypeSmartDoorLockHLS(applianceinfo->addr, tcmdid, tbuff, len);
			if(tempSmartDoorLockHLS->buff)
			{
				sendBuff = new TypeChar();
				sendBuff->onAddUBuff(0, tempSmartDoorLockHLS->buff->ubuff, tempSmartDoorLockHLS->buffLen);
				sendLen = tempSmartDoorLockHLS->buffLen;
			}
			delete tempSmartDoorLockHLS;
		}
		else if(applianceinfo->type == APPLIANCE_TYPE_AIR_CONDITION)
		{
			//mPrintf(Log_DataBase,"manufacturer:%s %d",applianceinfo->manufacturer->buff,strlen(applianceinfo->manufacturer->buff));
			if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_MD) == 0)//applianceinfo->manufacturer && (strlen() == 5))//MEDIA
			{
				//美的中央空调控制网关
				TypeCentralAirConditioningMD *tempCentralAirConditioning = new TypeCentralAirConditioningMD(tshortaddr, tcmdid, applianceinfo);
				if(tempCentralAirConditioning->airAddrBuff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
					sendLen = tempCentralAirConditioning->airAddrBuffLen;
				}
				delete tempCentralAirConditioning;
			}
			else if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)//if(applianceinfo->manufacturer && (strlen(applianceinfo->manufacturer->buff) == 10))
			{
				//温控器，包含中央空调，新风，地暖
				TypeTemperatureControl *tempCentralAirConditioning = new TypeTemperatureControl(tshortaddr, tcmdid, applianceinfo);
				if(tempCentralAirConditioning->airAddrBuff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
					sendLen = tempCentralAirConditioning->airAddrBuffLen;
				}
				delete tempCentralAirConditioning;
			}
			else //if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_ZH) == 0)//if(applianceinfo->manufacturer && (strlen(applianceinfo->manufacturer->buff) == 10))
			{
				//中宏中央空调网关
				TypeCentralAirConditioningZH *tempCentralAirConditioning = new TypeCentralAirConditioningZH(tshortaddr, tcmdid, applianceinfo);
				if(tempCentralAirConditioning->airAddrBuff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
					sendLen = tempCentralAirConditioning->airAddrBuffLen;
				}
				delete tempCentralAirConditioning;
			}
		}
        else if(applianceinfo->type == APPLIANCE_TYPE_VENTILATION_SYSTEM)
        {
            mPrintf(Log_DataBase,"manufacturer:%s %d",applianceinfo->manufacturer->buff,strlen(applianceinfo->manufacturer->buff));
			if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)//鸿雁新风
            {
                //温控器，包含中央空调，新风，地暖
                TypeTemperatureControl *tempCentralAirConditioning = new TypeTemperatureControl(tshortaddr, tcmdid, applianceinfo);
                if(tempCentralAirConditioning->airAddrBuff)
                {
                    sendBuff = new TypeChar();
                    sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
                    sendLen = tempCentralAirConditioning->airAddrBuffLen;
                }
                delete tempCentralAirConditioning;
            }
            else if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_LF) == 0)//拉斐新风
			{
				//温控器，包含中央空调，新风，地暖
				TypeTemperatureControlLF *tempCentralAirConditioning = new TypeTemperatureControlLF(tshortaddr, tcmdid, applianceinfo);
				if(tempCentralAirConditioning->airAddrBuff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
					sendLen = tempCentralAirConditioning->airAddrBuffLen;
				}
				delete tempCentralAirConditioning;
			}
            else
            {

            }
        }
        else if(applianceinfo->type == APPLIANCE_TYPE_FLOOR_HEATING)
        {
            mPrintf(Log_DataBase,"manufacturer:%s %d",applianceinfo->manufacturer->buff,strlen(applianceinfo->manufacturer->buff));
			if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)//鸿雁地暖
            {
                //温控器，包含中央空调，新风，地暖
                TypeTemperatureControl *tempCentralAirConditioning = new TypeTemperatureControl(tshortaddr, tcmdid, applianceinfo);
                if(tempCentralAirConditioning->airAddrBuff)
                {
                    sendBuff = new TypeChar();
                    sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
                    sendLen = tempCentralAirConditioning->airAddrBuffLen;
                }
                delete tempCentralAirConditioning;
            }
            else if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_LF) == 0)//拉斐地暖
           // if(strcmp(applianceinfo->manufacturer->buff,CENTRAL_AIR_MANU_HY) == 0)//拉斐地暖
            {
				//温控器，包含中央空调，新风，地暖
                TypeTemperatureControlLF *tempCentralAirConditioning = new TypeTemperatureControlLF(tshortaddr, tcmdid, applianceinfo);
				if(tempCentralAirConditioning->airAddrBuff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempCentralAirConditioning->airAddrBuff->ubuff, tempCentralAirConditioning->airAddrBuffLen);
					sendLen = tempCentralAirConditioning->airAddrBuffLen;
				}
				delete tempCentralAirConditioning;
            }
        }
		else if(applianceinfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN)
		{
			//这里是电动窗帘
			if(applianceinfo->manufacturer && (strlen(applianceinfo->manufacturer->buff) == 8))//丙申
			{
				TypeElectricCurtainBS *tempElectricCurtaindy = new TypeElectricCurtainBS(0x0a09, 0, tcmdid);
				applianceinfo->saveValue = tcmdid;
				if(tempElectricCurtaindy->buff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempElectricCurtaindy->buff->ubuff, tempElectricCurtaindy->buffLen);
					sendLen = tempElectricCurtaindy->buffLen;
				}
				delete tempElectricCurtaindy;

				if(applianceinfo->addr == 0)
				{
					//延时读取一下
					pmMasterSerialPort->onReadAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 200);
				}
			}
            else if(applianceinfo->manufacturer && (strcmp(applianceinfo->manufacturer->buff,EL_CURTAIN_AIR_AK) == 0)) //这里是奥科涂鸦电动窗帘
            {
                TypeElectricCurtainAK *tempElectricCurtainak = new TypeElectricCurtainAK(0x0001,tcmdid);
                applianceinfo->saveValue = tcmdid;
                if(tempElectricCurtainak->buff)
                {
                    sendBuff = new TypeChar();
                    sendBuff->onAddUBuff(0, tempElectricCurtainak->buff->ubuff, tempElectricCurtainak->buffLen);
                    sendLen = tempElectricCurtainak->buffLen;
                }
                delete tempElectricCurtainak;

                if(applianceinfo->addr == 0)
                {
                    //延时读取一下
                    pmMasterSerialPort->onReadAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 200);
                }
            }
            else if(applianceinfo->manufacturer && (strcmp(applianceinfo->manufacturer->buff,EL_CURTAIN_AIR_485_AK) == 0)) //这里是奥科485电动窗帘
            {
                TypeElectricCurtain485AK *tempElectricCurtain485ak = new TypeElectricCurtain485AK(0x09,0x8000,tcmdid);
                applianceinfo->saveValue = tcmdid;
                if(tempElectricCurtain485ak->buff)
                {
                    sendBuff = new TypeChar();
                    sendBuff->onAddUBuff(0, tempElectricCurtain485ak->buff->ubuff, tempElectricCurtain485ak->buffLen);
                    sendLen = tempElectricCurtain485ak->buffLen;
                }
                delete tempElectricCurtain485ak;

                if(applianceinfo->addr == 0)
                {
                    //延时读取一下
                    pmMasterSerialPort->onReadAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 200);
                }
//                if(applianceinfo->addr == 0) // 读设备运行位置
//                {
//                    TypeElectricCurtain485AK *tempElectricCurtain485ak = new TypeElectricCurtain485AK(0x09, 0x8000, 108);
//                    TypeChar *sendChars = new TypeChar((uint32_t)(tempElectricCurtain485ak->buffLen + 1));
//                    sendChars->ubuff[0] = (uint8_t)(tempElectricCurtain485ak->buffLen);
//                    tempElectricCurtain485ak->buff->ubuff[0] = 1;
//                    sendChars->onAddUBuff(1, tempElectricCurtain485ak->buff->ubuff, tempElectricCurtain485ak->buffLen);
//                    pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_CHAR_STR, sendChars->ubuff, (uint8_t)(tempElectricCurtain485ak->buffLen + 1)), 1000);
//                    delete tempElectricCurtain485ak;
//                    delete sendChars;
//                    //延时读取一下
//                    //pmMasterSerialPort->onReadAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 200);
//                }
            }
			else if(applianceinfo->manufacturer && (strlen(applianceinfo->manufacturer->buff) == 9))
			{

				TypeElectricCurtainSX *tempElectricCurtainsx = new TypeElectricCurtainSX(0x0a09, 0, tcmdid);
				applianceinfo->saveValue = tcmdid;
				if(tempElectricCurtainsx->buff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempElectricCurtainsx->buff->ubuff, tempElectricCurtainsx->buffLen);
					sendLen = tempElectricCurtainsx->buffLen;
				}
				delete tempElectricCurtainsx;
			}
			else//杜亚
			{
				TypeElectricCurtainDY *tempElectricCurtaindy = new TypeElectricCurtainDY(0xFEFE, tcmdid);
				applianceinfo->saveValue = tcmdid;
				if(tempElectricCurtaindy->buff)
				{
					sendBuff = new TypeChar();
					sendBuff->onAddUBuff(0, tempElectricCurtaindy->buff->ubuff, tempElectricCurtaindy->buffLen);
					sendLen = tempElectricCurtaindy->buffLen;
				}
				delete tempElectricCurtaindy;
			}
		}
		else if(applianceinfo->type == APPLIANCE_TYPE_RGBW_LIGHT)
		{
			//RGB灯带
			TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IShortAddr, tshortaddr);
			if(tempDBDeviceInfo && tempDBDeviceInfo->onLineFlag.bits.status)
			{
				TypeRGBLL *tempRGBLL = new TypeRGBLL(&tcmdid);
				sendBuff = new TypeChar();
				sendBuff->onAddUBuff(0, tempRGBLL->buff->ubuff, tempRGBLL->buffLen);
				sendLen = tempRGBLL->buffLen;
				applianceinfo->value = tcmdid;
				//这里应答一下给app
				ApplianceValueChangedNotification valueChangedNotification;
				valueChangedNotification.set_appliance_id(applianceinfo->appID);
				valueChangedNotification.set_value(applianceinfo->value);
				mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
				delete tempRGBLL;
			}
		}
		else if(applianceinfo->type == APPLIANCE_TYPE_CUSTOM)
		{
			//直接发送这个数据 解析一下16进制字符串
			if(tbuff)
			{
				uint8_t tempChar = 0;
				//先过滤掉按键名称
				char *tempTBuff = (char *)tbuff;
				while(*tempTBuff)
				{
					if(*tempTBuff == '\n')
					{
						*tempTBuff = 0;
						break;
					}
					tempTBuff++;
				}
				uint8_t tempLen = (uint8_t)(strlen((const char *)tbuff));
				//判断一下字体串口类型
				if(tempLen > 1)
				{
					mPrintf(Log_Error, "%s ", tbuff);
					sendBuff = new TypeChar();
					if((tbuff[0] == '0') && (tbuff[1] == 'x'))
					{
						tempLen = (uint8_t)(tempLen / 2 - 1);
						sendLen = tempLen + 1;
						sendBuff->ubuff[0] = (uint8_t)tempLen;
						for(uint32_t i = 0; i < tempLen; ++ i)
						{
							tempChar = (uint8_t)(mfHexToChar((uint8_t)tbuff[2 + i * 2]) & 0x0F);
							tempChar <<= 4;
							tempChar |= (uint8_t)(mfHexToChar((uint8_t)tbuff[2 + i * 2 + 1]) & 0x0F);
							sendBuff->ubuff[i + 1] = tempChar;
						}
					}
                    else
                    {
                        sendLen = tempLen + 1;
                        sendBuff->ubuff[0] = (uint8_t)tempLen;
                        sendBuff->onAddString((char *)tbuff);
                    }
                }

            }
        }
		else if(applianceinfo->type == APPLIANCE_TYPE_AIR_SWITCH)   //空气开关
        {
            if(applianceinfo->manufacturer && (strcmp(applianceinfo->manufacturer->buff,AIR_SWITCH_MD_AIR) == 0)) //曼顿
            {
                TypeAirSwitch *tempAirSwitch = new TypeAirSwitch(tcmdid);
                applianceinfo->saveValue = tcmdid;
                if(tempAirSwitch->buff)
                {
                    sendBuff = new TypeChar();
                    sendBuff->onAddUBuff(0, tempAirSwitch->buff->ubuff, tempAirSwitch->buffLen);
                    sendLen = tempAirSwitch->buffLen;
                }
                delete tempAirSwitch;

                if(applianceinfo->addr == 0)
                {
                    //延时读取一下
                    pmMasterSerialPort->onReadAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, 0x0010, 200);
                }
            }
        }
    }
    if(sendBuff == NULL)
    {
        sendBuff = new TypeChar(1);
    }
}

RS485Profile::~RS485Profile()
{
    if(sendBuff)
    {
        delete sendBuff;
    }
}