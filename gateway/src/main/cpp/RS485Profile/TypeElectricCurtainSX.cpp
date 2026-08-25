//
// Created by wenyu xia on 2019-07-02.
//
#include "../Main/WinobleMain.h"

TypeElectricCurtainSX::TypeElectricCurtainSX(uint8_t *tbuff, int32_t len)
{
	cmdID = 0;
	buffLen = 0;
	buff = NULL;
	//协议分析
	int32_t tempIndex = 0;
	int32_t status = 0;
	int32_t checkSum = 0;
	int32_t retCheckSum = 0;
	int32_t buffIndex = 0;
	while(tempIndex < len)
	{
		switch(status)
		{
			case 0: checkSum = tbuff[tempIndex]; if(tbuff[tempIndex] == 0x9a) {status = 1;} break;//头固定
			case 1: checkSum ^= tbuff[tempIndex]; cmdID = tbuff[tempIndex]; status = 2; break;//数据类型
			case 2: checkSum ^= tbuff[tempIndex]; buffLen = tbuff[tempIndex]; status = 3; buff = new TypeChar((uint32_t)(buffLen + 1)); break;//数据长度
			case 3: //得到数据
				if(buffIndex < buffLen)
				{
					checkSum ^= tbuff[tempIndex];
					buff->ubuff[buffIndex++] = tbuff[tempIndex];
				}
				if(buffIndex == buffLen)
				{
					status = 4;
				}
				break;
			case 4://数据检验
				retCheckSum = tbuff[tempIndex];
				if(checkSum == retCheckSum)
				{
					//得到一帧正确的数据
					mPrintf(Log_Master, "松下窗帘得到一个帧正确的数据! ");
				}
				break;
			default:break;
		}
		tempIndex++;
	}
	if(checkSum != retCheckSum)
	{
		cmdID = 0;
	}
}

TypeElectricCurtainSX::TypeElectricCurtainSX(int32_t cmdid, int32_t action, int32_t value)
{
	cmdID = 0;
	buffLen = 6;
	if(value == 105)
	{
		cmdID = 0xa3;
		value = 0;
		//窗帘反向
	}
	else if(value == 106)
	{
		//写反方向
		cmdID = 0x11;
		buffLen += 2;
		value = 0x02;
	}
	else if(value == 107)
	{
		//写默认方向
		cmdID = 0x11;
		buffLen += 2;
		value = 0x00;
	}
	else if(value == 103)
	{
		//窗帘停止
		cmdID = 0x0a;
		value = 0xcc;
	}
	else
	{
		//窗帘控制
		cmdID = 0x0d;
		if(value > 100) value = 100;
		if(value < 0) value = 0;
	}
	buff = new TypeChar(buffLen);
	buff->ubuff[0] = (uint8_t)(buffLen - 1);
	buff->ubuff[1] = 0x9a;
	buff->ubuff[2] = (uint8_t)cmdID;
	buff->ubuff[3] = (uint8_t)(buffLen - 5);
	buff->ubuff[4] = (uint8_t)value;
	uint8_t checkSum = 0;
	for(int i = 1; i < (buffLen - 1); ++ i)
	{
		checkSum ^= buff->ubuff[i];
	}
	buff->ubuff[buffLen - 1] = checkSum;
}

void TypeElectricCurtainSX::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
	//命令处理
	if(buff->ubuff)
	{
		if((cmdID == 0xa1) && (buffLen == 3))
		{
			if(buff->ubuff[1] == 0xFF)
			{
				if(buff->ubuff[0] == 1)
				{
					//开
					appinfo->value = 100;
				}
				else
				{
					appinfo->value = 0;
				}
			}
			else
			{
				appinfo->value = buff->ubuff[0];
			}
			//正常返回
			if(appinfo->value > 100) appinfo->value = 100;
			else if(appinfo->value < 0) appinfo->value = 0;
			ApplianceValueChangedNotification valueChangedNotification;
			valueChangedNotification.set_appliance_id(appinfo->appID);
			valueChangedNotification.set_value(appinfo->value);
			mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
		}
		else if((cmdID == 0xa3) && (buffLen == 3))
		{
			//if(buff->ubuff[0] & 0x02)
			{
				int32_t tempSave = appinfo->saveValue;
				RS485Profile *temp485Profile = new RS485Profile(106 + (bool)(buff->ubuff[0] & 0x02), NULL, 0, appinfo, tshortaddr);
				pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
				delete temp485Profile;
			}
		}
	}
}

TypeElectricCurtainSX::~TypeElectricCurtainSX()
{
	if(buff)
	{
		delete buff;
	}
}