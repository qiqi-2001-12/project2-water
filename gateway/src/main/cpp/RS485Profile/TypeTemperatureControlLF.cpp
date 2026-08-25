//
// Created by knight on 2020/08/26.
//

#include "../Main/WinobleMain.h"
TypeTemperatureControlLF::TypeTemperatureControlLF(uint8_t *tbuff, int32_t len)
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
                    if(tbuff[tempIndex] == 0x55){
                        tempStatus = 1;
                        tempCheckSum = tbuff[tempIndex];
                    }
                    else{
                        tempStatus = 0;
                    }
                }
                break;

                case 1:
                {
                    if(tbuff[tempIndex] == 0xAA){
                        tempStatus = 2;
                        tempCheckSum += tbuff[tempIndex];
                    }
                    else{
                        tempStatus = 0;
                    }
                }
                    break;

                case 2:
                {
                    if(tbuff[tempIndex] == 0x02){
                        tempStatus = 3;
                        tempCheckSum += tbuff[tempIndex];
                    }
                    else{
                        tempStatus = 0;
                    }
                }
                break;

				case 3:
                {
                    tempCheckSum += tbuff[tempIndex++];
                    tempCheckSum += tbuff[tempIndex++];
                    if(tbuff[tempIndex] <= 0x24) {
                        cmdID = tbuff[tempIndex];
                        tempCheckSum += tbuff[tempIndex];
                        tempStatus = 4;
                    } else{
                        tempStatus = 0;
                    }
                }
                break;

                case 4:
                {
                    airAddrBuffLen = ((tbuff[tempIndex] << 8) |  tbuff[tempIndex + 1]) & 0xffff;
                    airAddrBuff = new TypeChar((uint32_t)(airAddrBuffLen));
                    if(airAddrBuffLen < 128){
                        tempStatus = 5;
                        tempCheckSum += tbuff[tempIndex++];
                        tempCheckSum += tbuff[tempIndex];
                    }
                    else{
                        tempStatus = 0;
                    }
                    //mPrintf(Log_DataBase,"len report:%d %d ",airAddrBuffLen,tbuff[tempIndex]);
                }
                    break;

				case 5:
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
                        if(tempCheckSum == tbuff[tempIndex])
                        {
                            //检验成功 处理指令
//                        mPrintf(Log_DataBase, "air cmdid&crc:%d %d",cmdID,tempCheckSum);
                            return;
                        }
                    }
				}
					break;

				default:break;
			}
			tempIndex++;
		}
	}

}

TypeTemperatureControlLF::TypeTemperatureControlLF(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo)
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
		airAddrBuffLen = 24;
		airAddrBuff = new TypeChar((uint8_t)airAddrBuffLen);
		airAddrBuff->ubuff[1] = 0x55;
        airAddrBuff->ubuff[2] = 0xAA;
		airAddrBuff->ubuff[3] = 0x02;
		airAddrBuff->ubuff[4] = 0x00;
		airAddrBuff->ubuff[5] = 0x00;
        if (appinfo->type == APPLIANCE_TYPE_FLOOR_HEATING) {
            switch (tcmdid) {
                case 0x00://电源关
                case 0x01://电源开
                    airAddrBuff->ubuff[6] = 0x04;
                    airAddrBuff->ubuff[7] = 0x00;
                    airAddrBuff->ubuff[8] = 0x05;
                    airAddrBuff->ubuff[9] = 0x01;
                    airAddrBuff->ubuff[10] = 0x01;
                    airAddrBuff->ubuff[11] = 0x00;
                    airAddrBuff->ubuff[12] = 0x01;
                    airAddrBuff->ubuff[13] = tcmdid;
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
                        SetTemperature = SetTemperature + 16;
                        airAddrBuff->ubuff[6] = 0x04;
                        airAddrBuff->ubuff[7] = 0x00;
                        airAddrBuff->ubuff[8] = 0x08;
                        airAddrBuff->ubuff[9] = 0x10;
                        airAddrBuff->ubuff[10] = 0x02;
                        airAddrBuff->ubuff[11] = 0x00;
                        airAddrBuff->ubuff[12] = 0x04;
                        airAddrBuff->ubuff[13] = (SetTemperature >> 24) & 0xff;
                        airAddrBuff->ubuff[14] = (SetTemperature >> 16) & 0xff;
                        airAddrBuff->ubuff[15] = (SetTemperature >> 8) & 0xff;
                        airAddrBuff->ubuff[16] = SetTemperature & 0xff;
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
        airAddrBuff->ubuff[0] = airAddrBuff->ubuff[8] + 9;
        airAddrBuffLen = airAddrBuff->ubuff[0] + 1;
		for(int i = 1; i < airAddrBuff->ubuff[0]; i++)
		{
            airAddrBuff->ubuff[airAddrBuff->ubuff[8] + 9] += airAddrBuff->ubuff[i];
		}
	}
}

void TypeTemperatureControlLF::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo) {
    if (appinfo) {
        mPrintf(Log_DataBase, "air status cmdid:%d id:%d",cmdID,appinfo->ir_id);
        switch (cmdID) {
            case 0x05://状态上报(被动)
            case 0x06://状态上报（主动）
            {
                    int32_t tempStatus = 0;
                    if(appinfo->type == APPLIANCE_TYPE_FLOOR_HEATING){//地暖
                        uint8_t PowerState = appinfo->value & 0x01;
                        uint8_t RelayState = (appinfo->value >> 1) & 0x01;
                        uint16_t HotTemp = (appinfo->value >> 2) & 0x1F;
                        uint16_t CurrentTemp = (appinfo->value >> 7) & 0xFF;
                        mPrintf(Log_DataBase, "floor air status:%x", appinfo->value);
//                    mPrintf(Log_DataBase,"report:%d %d %d %d %d",airAddrBuff->ubuff[0],
//                            airAddrBuff->ubuff[1],airAddrBuff->ubuff[2],airAddrBuff->ubuff[3],airAddrBuff->ubuff[4]);
                        switch (airAddrBuff->ubuff[0]) {
                            case 0x01: {//开关状态
                                PowerState = airAddrBuff->ubuff[4];
                            }
                                break;
                            case 0x20://阀门状态
                            {
                                RelayState = airAddrBuff->ubuff[4];
                            }
                                break;


                            case 0x10://目标温度
                            {
                                HotTemp = airAddrBuff->ubuff[7] - 16;
                            }
                                break;

                            case 0x18://当前温度
                            {
                                CurrentTemp = airAddrBuff->ubuff[7];
                            }
                                break;

                        }
                        tempStatus = (CurrentTemp << 7) | (HotTemp << 2) | (RelayState << 1) | PowerState;

                        mPrintf(Log_DataBase, "floor current air status:%d currenttemp:%d hottemp:%d relay:%d power:%d", tempStatus,CurrentTemp,HotTemp,RelayState,PowerState);
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

TypeTemperatureControlLF::~TypeTemperatureControlLF() {
    if (airAddrBuff) {
        delete airAddrBuff;
    }
}