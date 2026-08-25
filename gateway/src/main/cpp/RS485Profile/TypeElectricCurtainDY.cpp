//
// Created by wenyu xia on 2018/10/30.
//

#include "../Main/WinobleMain.h"

TypeElectricCurtainDY::TypeElectricCurtainDY(uint8_t *tbuff, int32_t len)
{
	addr = 0;//默认为0xFEFE
	cmdID = 0;
	buffLen = 0;
	buff = NULL;
	//协议分析
	int32_t tempIndex = 0;
	int32_t status = 0;
	int32_t crc16 = 0;
	int32_t tempLen = 0;
	while(tempIndex < len)
	{
		switch(status)
		{
			case 0: if(tbuff[tempIndex] == 0x55) {status = 1;} break;//头固定
			case 1:addr = tbuff[tempIndex];status = 2; break;
			case 2:addr |= (((uint32_t)tbuff[tempIndex] << 8) & 0xFF00);status = 3; break;
			case 3:
				cmdID = tbuff[tempIndex];
				status = 4;
				//推算出数据长度
				buffLen = len - 6;
				if(buffLen <= 0)
				{
					buffLen = 0;
				}
				else
				{
					buff = new TypeChar(128);
				}
			break;
			case 4:
			{
				if(tempLen < buffLen)
				{
					buff->ubuff[tempLen ++] = tbuff[tempIndex];
				}
				if(tempLen >= buffLen)
				{
					status = 5;
				}
			}
			break;
			case 5://判断CRC16
			{
				crc16 = tbuff[tempIndex];
				status = 6;
			}
			break;
			case 6:
			{
				crc16 = (uint32_t)((crc16 << 8) | tbuff[tempIndex]);
				//计算实现的crc值
				if(crc16 == onGetCRC16(tbuff, buffLen + 4))
				{
					//检验相等
					return;
				}
			}
				break;
			default:break;
		}
		tempIndex++;
	}
}

TypeElectricCurtainDY::TypeElectricCurtainDY(int32_t taddr, int32_t tcmdid)
{
	addr = 0;//默认为0xFEFE
	cmdID = 0;
	buffLen = 0;
	buff = NULL;
	//默认为0xFEFE
	if((tcmdid >= 0) && (tcmdid <= 100))
	{
		//调节百分比
		cmdID = 0x03;//控制命令
		buffLen = 9;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x04;//调节百分比
		buff->ubuff[6] = (uint8_t)(tcmdid & 0xFF);
	}
	else if(tcmdid == 102)
	{
		cmdID = 0x03;
		buffLen = 8;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x01;//直接打开
	}
	else if(tcmdid == 101)
	{
		cmdID = 0x03;
		buffLen = 8;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x02;//直接关闭
	}
	else if(tcmdid == 103)
	{
		cmdID = 0x03;
		buffLen = 8;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x03;//直接停止
	}
	else if(tcmdid == 104)//获取窗帘状态
	{
		cmdID = 0x01;
		buffLen = 9;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x02;//读取窗帘状态
		buff->ubuff[6] = 1;//读取一个地址信息
	}
	else if(tcmdid == 105)//获取窗帘方向
	{
		cmdID = 0x01;
		buffLen = 9;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x03;//读窗帘方向
		buff->ubuff[6] = 1;//读取一个地址信息
	}
	else if(tcmdid == 106)//设置反方向
	{
		cmdID = 0x02;
		buffLen = 10;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x03;//写反方向
		buff->ubuff[6] = 0x01;
		buff->ubuff[7] = 0x01;
	}
	else if(tcmdid == 107)//设置默认方向
	{
		cmdID = 0x02;
		buffLen = 10;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x03;//写默认方向
		buff->ubuff[6] = 0x01;
		buff->ubuff[7] = 0x00;
	}
	else if(tcmdid == 108)//清除电机行程
	{
		cmdID = 0x03;
		buffLen = 8;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x07;
	}
	else if(tcmdid == 109)//恢复出厂设置
	{
		cmdID = 0x04;
		buffLen = 8;
		buff = new TypeChar((uint32_t)buffLen);
		buff->ubuff[4] = (uint8_t)(cmdID & 0xFF);
		buff->ubuff[5] = 0x08;
	}
	if(cmdID)
	{
		//生成一条命令 用于发送
		buff->ubuff[0] = (uint8_t)(buffLen - 1);
		buff->ubuff[1] = 0x55;//头固定
		buff->ubuff[2] = (uint8_t)(taddr & 0xFF);
		buff->ubuff[3] = (uint8_t)((taddr >> 8) & 0xFF);
		//计算CRC16
		int32_t tempCRC16 = onGetCRC16(buff->ubuff + 1, buffLen - 3);
		buff->ubuff[buffLen - 2] = (uint8_t)((tempCRC16 >> 8) & 0xFF);
		buff->ubuff[buffLen - 1] = (uint8_t)(tempCRC16 & 0xFF);

	}
}

void TypeElectricCurtainDY::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
	//命令处理
	switch(cmdID)
	{
		case 0x01://读取命令
		{
			if((buffLen > 1) && (buff->ubuff[0] == 0x03))
			{
				int32_t tempSave = appinfo->saveValue;
				RS485Profile *temp485Profile = new RS485Profile(106 + (bool)buff->ubuff[2], NULL, 0, appinfo, tshortaddr);
				pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
				delete temp485Profile;
			}
		}
			break;
		case 0x03://控制命令 返回
		{
			//如果返回0xFF 就发送一次开就OK了
			if((buffLen > 1) && (buff->ubuff[0] == 0x04) && (buff->ubuff[1] == 0xFF))
			{
				int32_t tempSave = appinfo->saveValue;
				RS485Profile *temp485Profile = new RS485Profile(102, NULL, 0, appinfo, tshortaddr);
				pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
				delete temp485Profile;
				appinfo->saveValue = tempSave;
			}
		}
			break;
		case 0x04://状态上报
		{
			if(buffLen == 10)
			{
				if(buff->ubuff[0] == 0x02)
				{
					if(buff->ubuff[5] == 0x00)
					{
						//正常返回
						appinfo->value = buff->ubuff[2];
						ApplianceValueChangedNotification valueChangedNotification;
						valueChangedNotification.set_appliance_id(appinfo->appID);
						valueChangedNotification.set_value(appinfo->value);
						mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
					}
					else if((buff->ubuff[5] == 0x04) && (buff->ubuff[2] == 100))//第一次初始化行程
					{
						if(appinfo->saveValue != 100)
						{
							//重新设置一下
							RS485Profile *temp485Profile = new RS485Profile(appinfo->saveValue, NULL, 0, appinfo, tshortaddr);
							pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
							delete temp485Profile;
						}
						else
						{
							//发送状态更新通知
							appinfo->value = buff->ubuff[2];
							ApplianceValueChangedNotification valueChangedNotification;
							valueChangedNotification.set_appliance_id(appinfo->appID);
							valueChangedNotification.set_value(appinfo->value);
							mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
						}
					}
					else if((buff->ubuff[2] == 0xff) && ((buff->ubuff[5] == 0x04) || (buff->ubuff[5] == 0x00)))//反向开关控制返回未设置行程
					{
						int cmdidd = 101;
						if(appinfo->saveValue == 102)
						{
							cmdidd = 101;
						}
						else if(appinfo->saveValue == 101)
						{
							cmdidd = 102;
						}
						RS485Profile *temp485Profile = new RS485Profile(cmdidd, NULL, 0, appinfo, tshortaddr);
						pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
						delete temp485Profile;
						appinfo->saveValue = cmdidd;
					}
				}
			}
		}
			break;
	}
}

TypeElectricCurtainDY::~TypeElectricCurtainDY()
{
	if(buff)
	{
		delete buff;
	}
}
