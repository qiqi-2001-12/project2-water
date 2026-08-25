//
// Created by wenyu xia on 2019-07-02.
//
#include "../Main/WinobleMain.h"

TypeElectricCurtainBS::TypeElectricCurtainBS(uint8_t *tbuff, int32_t len)
{
	cmdID = 0;
	buffLen = 0;
	buff = new TypeChar(2);
	//协议分析
	int32_t tempIndex = 0;
	int32_t status = 0;
	int32_t checkSum = 0x2017;
	int32_t retCheckSum = 0;
	while(tempIndex < len)
	{
		switch(status)
		{
			case 0: checkSum +=tbuff[tempIndex]; if(tbuff[tempIndex] == 0x55) {status = 1;} break;//头固定
			case 1: checkSum +=tbuff[tempIndex]; if(tbuff[tempIndex] == 0xAA) {status = 2;} else {status = 0;} break;//头固定
			case 2: checkSum +=tbuff[tempIndex]; if(tbuff[tempIndex] == 0xAA) {status = 3;} else {status = 0;}break;//头固定
			case 3: checkSum +=tbuff[tempIndex]; if(tbuff[tempIndex] == 0x55) {status = 4;} else {status = 0;}break;//头固定
			case 4: retCheckSum = tbuff[tempIndex]; status = 5; break;//返回校验低8位
			case 5: retCheckSum |= (((uint32_t)tbuff[tempIndex] << 8) & 0xFF00);status = 6; break;//返回校验高8位
			case 6: checkSum +=tbuff[tempIndex]; cmdID = tbuff[tempIndex]; status = 7; break;//返回命令低8位
			case 7: checkSum +=tbuff[tempIndex]; cmdID |= (((uint32_t)tbuff[tempIndex] << 8) & 0xFF00);status = 8; break;//返回命令高8位
			case 8: checkSum +=tbuff[tempIndex]; buffLen = tbuff[tempIndex]; status = 9; break;//返回长度低8位
			case 9: checkSum +=tbuff[tempIndex]; buffLen |= (((uint32_t)tbuff[tempIndex] << 8) & 0xFF00);status = 10; break;//返回长度高8位
			case 10: checkSum +=tbuff[tempIndex]; status = 11; break;//返回协议版本低8位
			case 11: checkSum +=tbuff[tempIndex]; status = 12; break;//返回协议版本高8位
			case 12: checkSum +=tbuff[tempIndex]; status = 13; break;//返回产品型号
			case 13: buff->ubuff[0] = tbuff[tempIndex]; checkSum +=tbuff[tempIndex]; status = 14; break;//返回动作数据
			case 14: buff->ubuff[1] = tbuff[tempIndex]; checkSum +=tbuff[tempIndex]; status = 15; break;//返回位置信息
			case 15: checkSum +=tbuff[tempIndex]; status = 16; break;//返回序列号
			case 16: checkSum +=tbuff[tempIndex]; status = 17; break;//返回序列号
			case 17: checkSum +=tbuff[tempIndex]; status = 18; break;//返回序列号
			case 18: checkSum +=tbuff[tempIndex]; status = 19; break;//返回保留1
			case 19: checkSum +=tbuff[tempIndex]; status = 20; break;//返回保留2
			case 20: checkSum +=tbuff[tempIndex]; status = 21; break;//返回保留3
			case 21: checkSum +=tbuff[tempIndex]; status = 22; break;//返回保留4
			default:break;
		}
		tempIndex++;
	}
	if(checkSum != retCheckSum)
	{
		cmdID = 0;
	}
}

TypeElectricCurtainBS::TypeElectricCurtainBS(int32_t cmdid, int32_t action, int32_t value)
{
	cmdID = 0;
	buffLen = 0;
	buff = NULL;
	//调节百分比
	uint32_t checkSum = 0x2017;
	cmdID = cmdid;//控制命令
	buffLen = 0x000a + 13;
	buff = new TypeChar((uint32_t)buffLen);
	buff->ubuff[1] = 0x55;
	buff->ubuff[2] = 0xaa;
	buff->ubuff[3] = 0xaa;
	buff->ubuff[4] = 0x55;
	buff->ubuff[7] = (uint8_t)(cmdid & 0xFF);
	buff->ubuff[8] = (uint8_t)((cmdid >> 8) & 0xFF);
	buff->ubuff[9] = 0x0a;
	buff->ubuff[10] = 0x00;
	buff->ubuff[11] = 0x00;
	buff->ubuff[12] = 0x00;
	buff->ubuff[13] = 0x01;
	buff->ubuff[14] = (uint8_t)action;
	if(value > 100) value = 100;
	if(value < 0) value = 0;
	value = 100 - value;
	buff->ubuff[15] = (uint8_t)value;
	buff->ubuff[16] = 0x01;
	buff->ubuff[17] = 0x01;
	buff->ubuff[18] = 0x01;
	buff->ubuff[19] = 0x00;
	buff->ubuff[20] = 0x00;
	buff->ubuff[21] = 0x00;
	buff->ubuff[22] = 0x00;
	for(int i = 1; i < 23; ++ i)
	{
		checkSum += buff->ubuff[i];
	}
	buff->ubuff[5] = (uint8_t)(checkSum & 0xFF);
	buff->ubuff[6] = (uint8_t)((checkSum >> 8) & 0xFF);
	buff->ubuff[0] = (uint8_t)(buffLen - 1);
}

void TypeElectricCurtainBS::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
	//命令处理
	if(cmdID == 0x0a03)
	{
		if(buff && (buff->ubuff[0] == 2))
		{
			//正常返回
			if(buff->ubuff[1] > 100) buff->ubuff[1] = 100;
			appinfo->value = 100 - buff->ubuff[1];
			ApplianceValueChangedNotification valueChangedNotification;
			valueChangedNotification.set_appliance_id(appinfo->appID);
			valueChangedNotification.set_value(appinfo->value);
			mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
		}
	}
}

TypeElectricCurtainBS::~TypeElectricCurtainBS()
{
	if(buff)
	{
		delete buff;
	}
}