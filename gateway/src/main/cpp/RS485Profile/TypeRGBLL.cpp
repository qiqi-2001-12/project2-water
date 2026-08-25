//
// Created by wenyu xia on 2019/1/15.
//

#include "../Main/WinobleMain.h"
//生成发送命令的
TypeRGBLL::TypeRGBLL(int32_t *tcmdid)
{
	buffLen = 1;
	buff = new TypeChar(80);
	//生成一条命令 用于发送
	if(*tcmdid & 0x80)
	{
		if((*tcmdid & 0x7F) == 0)
		{
			*tcmdid |= 100;
		}
		else if((*tcmdid & 0x7F) > 100)
		{
			*tcmdid &= ~0x7F;
			*tcmdid |= 100;
		}
		buffLen += onAddRGBBuff((*tcmdid >> 8) & 0xFFFFFF, (uint8_t)(*tcmdid & 0x7F), &buff->ubuff[buffLen]);
	}
	else
	{
		//发送亮度为0
		//buffLen += onAddLightBuff(0, &buff->ubuff[buffLen]);
		buffLen += onAddRGBBuff((*tcmdid >> 8) & 0xFFFFFF, 0, &buff->ubuff[buffLen]);
	}
	buff->ubuff[0] = (uint8_t)(buffLen - 1);
}

int32_t TypeRGBLL::onAddRGBBuff(int32_t value, uint8_t percent, uint8_t *buff)
{
	int retLen = 12;
	buff[0] = 0xFE;
	buff[1] = 0x55;
	buff[2] = 10;
	buff[3] = 0x12;
	buff[4] = 0x00;
	buff[5] = 0x00;
	buff[6] = 0xb0;
	if(percent > 100) percent = 100;
	buff[7] = (uint8_t)((((value >> 0) & 0xFF) * percent / 100) & 0xFF);
	buff[8] = (uint8_t)((((value >> 8) & 0xFF) * percent / 100) & 0xFF);
	buff[9] = (uint8_t)((((value >> 16) & 0xFF) * percent / 100) & 0xFF);
	buff[10] = 0;
	buff[11] = 0;
	for(int i = 1; i < 11; ++ i)
	{
		buff[11] ^= buff[i];
	}
	return retLen;
}

TypeRGBLL::~TypeRGBLL()
{
	if(buff)
	{
		delete buff;
	}
}
