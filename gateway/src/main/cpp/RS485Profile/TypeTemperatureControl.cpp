//
// Created by knight on 2020/08/26.
//

#include "../Main/WinobleMain.h"
TypeTemperatureControl::TypeTemperatureControl(uint8_t *tbuff, int32_t len)
{
	gatewayAddr = 0;
	cmdID = 0;
	ctlValue = 0;
	airCnt = 0;
	airAddrBuff = NULL;
	airAddrBuffLen = 0;
	int32_t tempIndex = 0;
	int32_t tempStatus = 0;
	int32_t tempBuffLen = 0;
	uint8_t tempCheckSum = 0;
	if(tbuff && len)
	{
		while(tempIndex < len)
		{
			switch(tempStatus)
			{
				case 0:
                {
                    if(tbuff[tempIndex] == 0x2A){
                        tempStatus = 1;
                    }
                    else{
                        tempStatus = 0;
                    }
                }
                break;
				case 1:
                {
                    airAddrBuffLen = tbuff[tempIndex] - 1;
                    airAddrBuff = new TypeChar((uint32_t)(airAddrBuffLen));
                    if(airAddrBuffLen < 80){
                        tempStatus = 2;
                    }
                    else{
                        tempStatus = 0;
                    }
                    //mPrintf(Log_DataBase,"len report:%d %d ",airAddrBuffLen,tbuff[tempIndex]);
                }
                break;

				case 2:
                {
                    if((tbuff[tempIndex] == 0x20) || (tbuff[tempIndex] == 0x21) || (tbuff[tempIndex] == 0x25)
                    ||(tbuff[tempIndex] == 0x26)) {
                        cmdID = tbuff[tempIndex];
                        tempCheckSum = tbuff[tempIndex];
                        tempStatus = 3;
                    } else{
                        tempStatus = 0;
                    }
                }
                break;

				case 3:
				{
                    if(tempBuffLen < airAddrBuffLen)
                    {
//                        mPrintf(Log_DataBase,"report:%d",airAddrBuff->ubuff[tempBuffLen]);
                        airAddrBuff->ubuff[tempBuffLen++] = tbuff[tempIndex];
//                        mPrintf(Log_DataBase,"all:%d %d ",airAddrBuff->ubuff[tempBuffLen - 1],tbuff[tempIndex]);
                        tempCheckSum += tbuff[tempIndex];
                    }
                    else
                    {
//                        mPrintf(Log_DataBase,"all:%d %d %d %d",airAddrBuff->ubuff[0],airAddrBuff->ubuff[1],airAddrBuff->ubuff[2],airAddrBuff->ubuff[3]);
                        tempStatus = 4;
                    }
				}
					break;

				case 4://判断校验是否正确
				{
                    //mPrintf(Log_DataBase,"crc report:%d %d ",tbuff[tempIndex - 1],tempCheckSum);
                    //mPrintf(Log_DataBase, "air status all in:%d %d %d",cmdID,tempCheckSum,tbuff[tempIndex - 1]);
                    if(tempCheckSum == tbuff[tempIndex - 1])
					{
						//检验成功 处理指令
//                        mPrintf(Log_DataBase, "air cmdid&crc:%d %d",cmdID,tempCheckSum);
						return;
					}
				}
					break;
				default:break;
			}
			tempIndex++;
		}
	}

}

TypeTemperatureControl::TypeTemperatureControl(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo)
{
	gatewayAddr = 0;
	cmdID = 0;
	ctlValue = 0;
	airCnt = 0;
	airAddrBuff = NULL;
	airAddrBuffLen = 0;
    int16_t SetTemperature = 0;
//	ApplianceValueChangedNotification valueChangedNotification;
//	valueChangedNotification.set_appliance_id(appinfo->appID);
//	valueChangedNotification.set_value(appinfo->value);
//	mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
	//修改一下校验
	if(appinfo)
	{
		airAddrBuffLen = 16;
		airAddrBuff = new TypeChar((uint8_t)airAddrBuffLen);
		airAddrBuff->ubuff[1] = 0x2A;
		airAddrBuff->ubuff[4] = 0x71;
		airAddrBuff->ubuff[5] = 0x2A;
		airAddrBuff->ubuff[7] = 0x00;
		if(tcmdid == 0x60){
            airAddrBuff->ubuff[2] = 0x0E;
            airAddrBuff->ubuff[3] = 0x21;
            airAddrBuff->ubuff[6] = 0x03;
            airAddrBuff->ubuff[8] = 0x08;
            airAddrBuff->ubuff[9] = (mTimerNow->tm_year + 1900) & 0xff;
            airAddrBuff->ubuff[10] = ((mTimerNow->tm_year + 1900) >> 8) & 0xff;
            airAddrBuff->ubuff[11] = (uint8_t)mTimerNow->tm_mon;
            airAddrBuff->ubuff[12] = (uint8_t)mTimerNow->tm_mday;
            airAddrBuff->ubuff[13] = (uint8_t)mTimerNow->tm_hour;
            airAddrBuff->ubuff[14] = (uint8_t)mTimerNow->tm_min;
            airAddrBuff->ubuff[15] = (uint8_t)mTimerNow->tm_sec;
            airAddrBuff->ubuff[16] = (uint8_t)mTimerNow->tm_wday;
		} else {
            if (appinfo->type == APPLIANCE_TYPE_AIR_CONDITION) {
                if ((tcmdid == 0x02) || (tcmdid == 0x03) || (tcmdid == 0x04) || (tcmdid == 0x05)
                    || (tcmdid == 0x06) || (tcmdid == 0x0F) || (tcmdid == 0x10) ||
                    (tcmdid == 0x11) || (tcmdid == 0x12)) {
                    airAddrBuff->ubuff[2] = 7;
                    airAddrBuff->ubuff[3] = 0x25;
                    airAddrBuff->ubuff[6] = 0x06;
                    airAddrBuff->ubuff[8] = 0x01;
                }
                if ((tcmdid == 0x13) || (tcmdid == 0x07) || (tcmdid == 0x08) || (tcmdid == 0x09) ||
                    (tcmdid == 0x0A)) {
                    airAddrBuff->ubuff[2] = 7;
                    airAddrBuff->ubuff[3] = 0x25;
                    airAddrBuff->ubuff[6] = 0x0E;
                    airAddrBuff->ubuff[8] = 0x01;
                }
                switch (tcmdid) {
                    case 0x00://电源关
                    case 0x01://电源开
                        airAddrBuff->ubuff[2] = 7;
                        airAddrBuff->ubuff[3] = 0x25;
                        airAddrBuff->ubuff[6] = 0x0b;
                        airAddrBuff->ubuff[8] = 0x01;
                        airAddrBuff->ubuff[9] = tcmdid;
                        break;
                    case 0x02:
                        airAddrBuff->ubuff[9] = 0x01;
                        break;/*自动*/
                    case 0x03:
                        airAddrBuff->ubuff[9] = 0x03;
                        break;/*制冷*/
                    case 0x04:
                        airAddrBuff->ubuff[9] = 0x04;
                        break;/*制热*/
                    case 0x05:
                        airAddrBuff->ubuff[9] = 0x07;
                        break;/*送风*/
                    case 0x06:
                        airAddrBuff->ubuff[9] = 0x08;
                        break;/*除湿*/
                    case 0x0F:
                        airAddrBuff->ubuff[9] = 0x02;
                        break;/*节能*/
                    case 0x10:
                        airAddrBuff->ubuff[9] = 0x05;
                        break;/*紧急加热*/
                    case 0x11:
                        airAddrBuff->ubuff[9] = 0x06;
                        break;/*预冷*/
                    case 0x12:
                        airAddrBuff->ubuff[9] = 0x09;
                        break;/*睡眠*/

                    case 0x13:
                        airAddrBuff->ubuff[9] = 0x01;
                        break;//风速静音
                    case 0x07:
                        airAddrBuff->ubuff[9] = 0x02;
                        break;//风速低
                    case 0x08:
                        airAddrBuff->ubuff[9] = 0x03;
                        break;//风速中
                    case 0x09:
                        airAddrBuff->ubuff[9] = 0x04;
                        break;//风速高
                    case 0x0A:
                        airAddrBuff->ubuff[9] = 0x00;
                        break;//风速自动
                    case 0x50:
                        airAddrBuff->ubuff[2] = 5;
                        airAddrBuff->ubuff[3] = 0x20;
                        airAddrBuff->ubuff[6] = 0x00;
                        break;//查询空调状态
                    default:
                        if (((tcmdid >= 0x300) && (tcmdid <= 0x31F)) ||
                            ((tcmdid >= 0x400) && (tcmdid <= 0x41F))) {
                            SetTemperature = tcmdid & 0x1F;
                            SetTemperature = (SetTemperature + 16) * 100;
                            airAddrBuff->ubuff[2] = 8;
                            airAddrBuff->ubuff[3] = 0x25;
                            airAddrBuff->ubuff[6] = 0x0C;
                            airAddrBuff->ubuff[8] = 0x02;
                            airAddrBuff->ubuff[9] = SetTemperature & 0xff;;
                            airAddrBuff->ubuff[10] = (SetTemperature >> 8) & 0xff;
                        } else {
                            airAddrBuff->ubuff[2] = 1;
                            //不支持的命令  直接更新一下空调状态
                            ApplianceValueChangedNotification valueChangedNotification;
                            valueChangedNotification.set_appliance_id(appinfo->appID);
                            valueChangedNotification.set_value(appinfo->value);
                            mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ,
                                         valueChangedNotification.SerializeAsString().c_str(),
                                         valueChangedNotification.SerializeAsString().length());
                        }
                        break;
                }
            }
            else if (appinfo->type == APPLIANCE_TYPE_FLOOR_HEATING) {
                switch (tcmdid) {
                    case 0x00://电源关
                    case 0x01://电源开
                        airAddrBuff->ubuff[2] = 7;
                        airAddrBuff->ubuff[3] = 0x25;
                        airAddrBuff->ubuff[6] = 0x0b;
                        airAddrBuff->ubuff[8] = 0x01;
                        airAddrBuff->ubuff[9] = tcmdid;
                        break;
                    case 0x50:
                        airAddrBuff->ubuff[2] = 5;
                        airAddrBuff->ubuff[3] = 0x20;
                        airAddrBuff->ubuff[6] = 0x00;
                        break;//查询空调状态
                    default:
                        if (((tcmdid >= 0x300) && (tcmdid <= 0x31F)) ||
                            ((tcmdid >= 0x400) && (tcmdid <= 0x41F))) {
                            SetTemperature = tcmdid & 0x1F;
                            SetTemperature = (SetTemperature + 16) * 100;
                            airAddrBuff->ubuff[2] = 8;
                            airAddrBuff->ubuff[3] = 0x25;
                            airAddrBuff->ubuff[6] = 0x0C;
                            airAddrBuff->ubuff[8] = 0x02;
                            airAddrBuff->ubuff[9] = SetTemperature & 0xff;;
                            airAddrBuff->ubuff[10] = (SetTemperature >> 8) & 0xff;
                        } else {
                            airAddrBuff->ubuff[2] = 1;
                            //不支持的命令  直接更新一下地暖状态
                            ApplianceValueChangedNotification valueChangedNotification;
                            valueChangedNotification.set_appliance_id(appinfo->appID);
                            valueChangedNotification.set_value(appinfo->value);
                            mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ,
                                         valueChangedNotification.SerializeAsString().c_str(),
                                         valueChangedNotification.SerializeAsString().length());
                        }
                        break;
                }
            }
            else if (appinfo->type == APPLIANCE_TYPE_VENTILATION_SYSTEM) {
                if ((tcmdid == 0x13) || (tcmdid == 0x07) || (tcmdid == 0x08) || (tcmdid == 0x09) ||
                    (tcmdid == 0x0A)) {
                    airAddrBuff->ubuff[2] = 7;
                    airAddrBuff->ubuff[3] = 0x25;
                    airAddrBuff->ubuff[6] = 0x0E;
                    airAddrBuff->ubuff[8] = 0x01;
                }
                switch (tcmdid) {
                    case 0x00://电源关
                    case 0x01://电源开
                        airAddrBuff->ubuff[2] = 7;
                        airAddrBuff->ubuff[3] = 0x25;
                        airAddrBuff->ubuff[6] = 0x0D;
                        airAddrBuff->ubuff[8] = 0x01;
                        airAddrBuff->ubuff[9] = tcmdid;
                        break;
                    case 0x02:/*正常*/
                    case 0x03:/*节能*/
                        airAddrBuff->ubuff[2] = 7;
                        airAddrBuff->ubuff[3] = 0x25;
                        airAddrBuff->ubuff[6] = 0x0F;
                        airAddrBuff->ubuff[8] = 0x01;
                        airAddrBuff->ubuff[9] = tcmdid - 2;
                        break;

                    case 0x13:
                        airAddrBuff->ubuff[9] = 0x01;
                        break;//风速静音
                    case 0x07:
                        airAddrBuff->ubuff[9] = 0x02;
                        break;//风速低
                    case 0x08:
                        airAddrBuff->ubuff[9] = 0x03;
                        break;//风速中
                    case 0x09:
                        airAddrBuff->ubuff[9] = 0x04;
                        break;//风速高
                    case 0x0A:
                        airAddrBuff->ubuff[9] = 0x00;
                        break;//风速自动
                    case 0x50:
                        airAddrBuff->ubuff[2] = 5;
                        airAddrBuff->ubuff[3] = 0x20;
                        airAddrBuff->ubuff[6] = 0x00;
                        break;//查询空调状态
                    default:
                        airAddrBuff->ubuff[2] = 1;
                        //不支持的命令  直接更新一下空调状态
                        ApplianceValueChangedNotification valueChangedNotification;
                        valueChangedNotification.set_appliance_id(appinfo->appID);
                        valueChangedNotification.set_value(appinfo->value);
                        mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ,
                                     valueChangedNotification.SerializeAsString().c_str(),
                                     valueChangedNotification.SerializeAsString().length());
                        break;
                }
            }
        }
        airAddrBuff->ubuff[0] = airAddrBuff->ubuff[2] + 4;
        airAddrBuffLen = airAddrBuff->ubuff[0] + 1;
		for(int i = 0; i < airAddrBuff->ubuff[2]; i++)
		{
            airAddrBuff->ubuff[airAddrBuff->ubuff[2] + 3] += airAddrBuff->ubuff[i + 3];
		}
        airAddrBuff->ubuff[airAddrBuff->ubuff[2] + 4] = 0x23;
	}
}

void TypeTemperatureControl::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo) {
    if (appinfo) {
        mPrintf(Log_DataBase, "air status cmdid:%d id:%d",cmdID,appinfo->ir_id);
        switch (cmdID) {
            case 0x20://时间同步
            {
                if(airAddrBuff->ubuff[2] == 0x03)
                {
                    RS485Profile *temp485Profile = new RS485Profile(0x60, NULL, 0, appinfo, tshortaddr);
                    pmMasterSerialPort->onWriteAttribute((uint32_t) tshortaddr, 1, CLUSTER_ID_PERSONAL,
                                                         new TypeZclAttribute(0x000C,
                                                                              ZCL_DATATYPE_CHAR_STR,
                                                                              temp485Profile->sendBuff->ubuff,
                                                                              (uint8_t) temp485Profile->sendLen),
                                                         0);
                    delete temp485Profile;
                }
            }
            break;

            case 0x25://向下控制温度 0x10~0x1E 设定温度 16~30度
                break;

            case 0x26://向下控制模式 0x01 设定制冷 0x02 设定除湿 0x04 设定送风 0x08 设定制热
                //case 0x34://向下控制风速 0x01 高速 0x02 中速 0x04 低速
            {
                //所有这些控制完成后都主动查询一下中央空调的状态  有主动上报状态
//                RS485Profile *temp485Profile = new RS485Profile(0x50, NULL, 0, appinfo, tshortaddr);
//                pmMasterSerialPort->onWriteAttribute((uint32_t) tshortaddr, 1, CLUSTER_ID_PERSONAL,
//                                                     new TypeZclAttribute(0x000C,
//                                                                          ZCL_DATATYPE_CHAR_STR,
//                                                                          temp485Profile->sendBuff->ubuff,
//                                                                          (uint8_t) temp485Profile->sendLen),
//                                                     0);
//                delete temp485Profile;
            }
                break;

            case 0x21://向下查询空调状态返回
            {
                //更新空调状态 10Byte  空调外机 空调内机 开关状态 温度设定 模式设定 风速设定 房间温度 故障代码 备用1 备用2
                //一般我一次只读取一个中央空调的状态
//                TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfoKeyIDAndAddr(
//                        appinfo->ir_id, 0);
                    //找到这个中央空调了
                    int32_t tempStatus = 0;
                    if(appinfo->type == APPLIANCE_TYPE_AIR_CONDITION) {
                        mPrintf(Log_DataBase, "center air status type:%d",appinfo->type);
                        mPrintf(Log_DataBase, "center uart data:%d %d %d %d",airAddrBuff->ubuff[2],
                                airAddrBuff->ubuff[3],airAddrBuff->ubuff[4],airAddrBuff->ubuff[5]);
                        uint8_t PowerState = appinfo->value & 0x01;
                        uint8_t WindMode = (appinfo->value >> 1) & 0x03;
                        uint8_t WindSpeed = (appinfo->value >> 3) & 0x0F;
                        uint16_t PanelMode = (appinfo->value >> 7) & 0x1F;
                        uint16_t ColdTemp = (appinfo->value >> 17) & 0x1F;
                        uint16_t HotTemp = (appinfo->value >> 12) & 0x1F;
                        uint16_t CurrentTemp = (appinfo->value >> 22) & 0xFF;
                        mPrintf(Log_DataBase, "center air status:%d temperature:%d mode:%d windspeed:%d power:%d",
                                tempStatus,ColdTemp,PanelMode,WindSpeed,PowerState);
                        switch (airAddrBuff->ubuff[2]) {
                            case 0: {//全状态查询
                                switch (airAddrBuff->ubuff[17]) {
                                    case 0x00:PanelMode = 0x00;break;//关闭
                                    case 0x01:PanelMode = 0x01;break;//自动
                                    case 0x02:PanelMode = 0x06;break;//节能
                                    case 0x03:PanelMode = 0x02;break;//制冷
                                    case 0x04:PanelMode = 0x03;break;//制热
                                    case 0x05:PanelMode = 0x07;break;//紧急加热
                                    case 0x06:PanelMode = 0x08;break;//预冷
                                    case 0x07:PanelMode = 0x04;break;//送风
                                    case 0x08:PanelMode = 0x05;break;//除湿
                                    case 0x09:PanelMode = 0x09;break;//睡眠
                                    case 0x0A:PanelMode = 0x0A;break;//打开
                                }

                                int16_t CurrentTemperature = airAddrBuff->ubuff[22] << 8 | airAddrBuff->ubuff[21];
                                if ((CurrentTemperature >= -27315) && (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    CurrentTemp = CurrentTemperature;
                                }

                                CurrentTemperature = airAddrBuff->ubuff[24] << 8 | airAddrBuff->ubuff[23];
                                if ((CurrentTemperature >= -27315) && (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    ColdTemp = CurrentTemperature - 16;
                                    HotTemp = CurrentTemperature - 16;
                                }

                                if (airAddrBuff->ubuff[15] == 0x01) {
                                    PowerState = 0x01;
                                } else {
                                    PowerState = 0x00;
                                }

                                switch (airAddrBuff->ubuff[29]) {
                                    case 0x00:WindSpeed = 0x03;break;//自动
                                    case 0x01:WindSpeed = 0x03;break;//静音风
                                    case 0x02:WindSpeed = 0x00;break;//低风
                                    case 0x03:WindSpeed = 0x01;break;//中风
                                    case 0x04:WindSpeed = 0x02;break;//高风
                                }
                                mPrintf(Log_DataBase, "center air temperature:%d mode:%d windspeed:%d power:%d", ColdTemp,PanelMode,WindSpeed,PowerState);
                            }
                                break;
                            case 11://面板开关状态
                            {
                                if (airAddrBuff->ubuff[5] == 0x01) {
                                    PowerState = 0x01;
                                } else {
                                    PowerState = 0x00;
                                }
                            }
                                break;

                            case 5:
                                break;//面板锁定状态


                            case 6://空调工作模式
                            {

                                switch (airAddrBuff->ubuff[5]) {
                                    case 0x00:PanelMode = 0x00;break;//关闭
                                    case 0x01:PanelMode = 0x01;break;//自动
                                    case 0x02:PanelMode = 0x06;break;//节能
                                    case 0x03:PanelMode = 0x02;break;//制冷
                                    case 0x04:PanelMode = 0x03;break;//制热
                                    case 0x05:PanelMode = 0x07;break;//紧急加热
                                    case 0x06:PanelMode = 0x08;break;//预冷
                                    case 0x07:PanelMode = 0x04;break;//送风
                                    case 0x08:PanelMode = 0x05;break;//除湿
                                    case 0x09:PanelMode = 0x09;break;//睡眠
                                    case 0x0A:PanelMode = 0x0A;break;//打开
                                }
                            }
                                break;

                            case 9://当前温度
                            {
                                int16_t CurrentTemperature =
                                        airAddrBuff->ubuff[6] << 8 | airAddrBuff->ubuff[5];
                                if ((CurrentTemperature >= -27315) &&
                                    (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    CurrentTemp = CurrentTemperature << 22;
                                }
                            }
                                break;

                            case 10://目标温度
                            {
                                int16_t CurrentTemperature =
                                        airAddrBuff->ubuff[6] << 8 | airAddrBuff->ubuff[5];
                                if ((CurrentTemperature >= -27315) &&
                                    (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    ColdTemp = CurrentTemperature - 16;
                                    HotTemp = CurrentTemperature - 16;
                                }
                            }
                                break;

                            case 14://风速
                            {
                                switch (airAddrBuff->ubuff[5]) {
                                    case 0x00:WindSpeed = 0x03;break;//自动
                                    case 0x01:WindSpeed = 0x03;break;//静音风
                                    case 0x02:WindSpeed = 0x00;break;//低风
                                    case 0x03:WindSpeed = 0x01;break;//中风
                                    case 0x04:WindSpeed = 0x02;break;//高风
                                }
                            }
                                break;
                        }
                        tempStatus = (CurrentTemp << 22) | (ColdTemp << 17) | (HotTemp << 12) |
                                     (PanelMode << 7) | (WindSpeed << 3) | (WindMode << 1) |
                                     PowerState;
                        mPrintf(Log_DataBase, "center air status:%d temperature:%d mode:%d windspeed:%d power:%d", tempStatus,ColdTemp,PanelMode,WindSpeed,PowerState);
                    }
                    else if(appinfo->type == APPLIANCE_TYPE_FLOOR_HEATING){//地暖
                        uint8_t PowerState = appinfo->value & 0x01;
                        uint8_t RelayState = (appinfo->value >> 1) & 0x01;
                        uint16_t HotTemp = (appinfo->value >> 2) & 0x1F;
                        uint16_t CurrentTemp = (appinfo->value >> 7) & 0xFF;
                        mPrintf(Log_DataBase, "floor air status:%x", appinfo->value);
//                    mPrintf(Log_DataBase,"report:%d %d %d %d %d",airAddrBuff->ubuff[0],
//                            airAddrBuff->ubuff[1],airAddrBuff->ubuff[2],airAddrBuff->ubuff[3],airAddrBuff->ubuff[4]);
                        switch (airAddrBuff->ubuff[2]) {
                            case 0: {//全状态查询
                                if (airAddrBuff->ubuff[25] == 0x01) {
                                    PowerState = 0x01;
                                } else {
                                    PowerState = 0x00;
                                }

                                int16_t CurrentTemperature =
                                        airAddrBuff->ubuff[22] << 8 | airAddrBuff->ubuff[21];
                                if ((CurrentTemperature >= -27315) &&
                                    (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    CurrentTemp = CurrentTemperature;
                                }

                                CurrentTemperature =
                                        airAddrBuff->ubuff[27] << 8 | airAddrBuff->ubuff[26];
                                if ((CurrentTemperature >= -27315) &&
                                    (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    HotTemp = CurrentTemperature - 16;
                                }

                                if (airAddrBuff->ubuff[33] == 0x01) {
                                    RelayState = 0x01;
                                } else {
                                    RelayState = 0x00;
                                }
                            }
                                break;
                            case 11://面板开关状态
                            {
                                if (airAddrBuff->ubuff[5] == 0x01) {
                                    PowerState = 0x01;
                                } else {
                                    PowerState = 0x00;
                                }
                            }
                                break;


                            case 9://当前温度
                            {
                                int16_t CurrentTemperature =
                                        airAddrBuff->ubuff[6] << 8 | airAddrBuff->ubuff[5];
                                if ((CurrentTemperature >= -27315) &&
                                    (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    CurrentTemp = CurrentTemperature;
                                }
                            }
                                break;

                            case 10://目标温度
                            {
                                int16_t CurrentTemperature =
                                        airAddrBuff->ubuff[6] << 8 | airAddrBuff->ubuff[5];
                                if ((CurrentTemperature >= -27315) &&
                                    (CurrentTemperature <= 32767)) {
                                    CurrentTemperature = CurrentTemperature / 100;
                                    HotTemp = CurrentTemperature - 16;
                                }
                            }
                                break;

                            case 18://继电器状态
                            {
                                if (airAddrBuff->ubuff[5] == 0x01) {
                                    RelayState = 0x01;
                                } else {
                                    RelayState = 0x00;
                                }

                            }
                                break;

                        }
                        tempStatus = (CurrentTemp << 7) | (HotTemp << 2) | (RelayState << 1) | PowerState;

                        mPrintf(Log_DataBase, "floor current air status:%d currenttemp:%d hottemp:%d relay:%d power:%d", tempStatus,CurrentTemp,HotTemp,RelayState,PowerState);
                    }
                    else if(appinfo->type == APPLIANCE_TYPE_VENTILATION_SYSTEM){
                        uint8_t PowerState = appinfo->value & 0x01;
                        uint8_t WindSpeed = (appinfo->value >> 1) & 0x0F;
                        uint8_t WindMode = (appinfo->value >> 5) & 0x1F;
                        mPrintf(Log_DataBase, "ven normal air status:%x windmode:%d windspeed:%d power:%d", tempStatus,WindMode,WindSpeed,PowerState);
//                    mPrintf(Log_DataBase,"report:%d %d %d %d %d",airAddrBuff->ubuff[0],
//                            airAddrBuff->ubuff[1],airAddrBuff->ubuff[2],airAddrBuff->ubuff[3],airAddrBuff->ubuff[4]);
                        switch (airAddrBuff->ubuff[2]) {
                            case 0: {//全状态查询

                                switch (airAddrBuff->ubuff[30]) {
                                    case 0x00:
                                        WindMode = 0x01;
                                        break;//正常
                                    case 0x01:
                                        WindMode = 0x02;
                                        break;//节能
                                }

                                if (airAddrBuff->ubuff[28] == 0x01) {
                                    PowerState = 0x01;
                                } else {
                                    PowerState = 0x00;
                                }

                                switch (airAddrBuff->ubuff[29]) {
                                    case 0x00:
                                        WindSpeed = 0x03;
                                        break;//自动
                                    case 0x01:
                                        WindSpeed = 0x03;
                                        break;//静音风
                                    case 0x02:
                                        WindSpeed = 0x00;
                                        break;//低风
                                    case 0x03:
                                        WindSpeed = 0x01;
                                        break;//中风
                                    case 0x04:
                                        WindSpeed = 0x02;
                                        break;//高风
                                    default:
                                        WindSpeed = (appinfo->value >> 1) & 0x0F;
                                        break;
                                }

                            }
                                break;
                            case 13://面板开关状态
                            {
                                if (airAddrBuff->ubuff[5] == 0x01) {
                                    PowerState = 0x01;
                                } else {
                                    PowerState = 0x00;
                                }
                            }
                                break;


                            case 15://空调工作模式
                            {

                                switch (airAddrBuff->ubuff[5]) {
                                    case 0x00:
                                        WindMode = 0x01;
                                        break;//正常
                                    case 0x01:
                                        WindMode = 0x02;
                                        break;//节能
                                }
                            }
                                break;

                            case 14://风速
                            {
                                switch (airAddrBuff->ubuff[5]) {
                                    case 0x00:
                                        WindSpeed = 0x03;
                                        break;//自动
                                    case 0x01:
                                        WindSpeed = 0x03;
                                        break;//静音风
                                    case 0x02:
                                        WindSpeed = 0x00;
                                        break;//低风
                                    case 0x03:
                                        WindSpeed = 0x01;
                                        break;//中风
                                    case 0x04:
                                        WindSpeed = 0x02;
                                        break;//高风
                                    default:
                                        WindSpeed = (appinfo->value >> 1) & 0x0F;
                                        break;
                                }
                            }
                                break;
                        }

                        tempStatus = (WindSpeed << 5) | (WindSpeed << 1) | PowerState;
                        mPrintf(Log_DataBase, "ven current air status:%x windmode:%d windspeed:%d power:%d", tempStatus,WindMode,WindSpeed,PowerState);

                    }
                    if (appinfo->value != tempStatus) {
                        //更新家电状态
                        appinfo->value = tempStatus;
                        ApplianceValueChangedNotification valueChangedNotification;
                        valueChangedNotification.set_appliance_id(appinfo->appID);
                        valueChangedNotification.set_value(appinfo->value);
                        mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ,
                                     valueChangedNotification.SerializeAsString().c_str(),
                                     valueChangedNotification.SerializeAsString().length());
                    }
            }
                break;
            default:
                break;
        }
    }
}

TypeTemperatureControl::~TypeTemperatureControl() {
    if (airAddrBuff) {
        delete airAddrBuff;
    }
}