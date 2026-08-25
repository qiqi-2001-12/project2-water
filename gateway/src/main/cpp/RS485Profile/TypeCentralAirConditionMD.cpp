//
// Created by wenyu xia on 2019/9/20.
//

#include "../Main/WinobleMain.h"
TypeCentralAirConditioningMD::TypeCentralAirConditioningMD(uint8_t *tbuff, int32_t len)
{
	deviceID = 0;
	cmdID = 0;
	airAddrBuffLen = 0;
	crc16 = 0;
	airAddrBuff = NULL;
	int32_t tempIndex = 0;
	int32_t tempStatus = 0;
	int32_t tempBuffLen = 0;
	if(tbuff && len)
	{
		while(tempIndex < len)
		{
			switch(tempStatus)
			{
				case 0:deviceID = tbuff[tempIndex]; tempStatus = 1;break;
				case 1:cmdID = tbuff[tempIndex]; tempStatus = 2;break;
				case 2:airAddrBuffLen = tbuff[tempIndex]; tempStatus = 3;break;
				case 3:
				{
					if(airAddrBuff == NULL)
					{
						airAddrBuff = new TypeChar((uint32_t)(airAddrBuffLen + 1));
						tempBuffLen = airAddrBuffLen;
					}
					if(tempBuffLen)
					{
						airAddrBuff->ubuff[tempBuffLen--] = tbuff[tempIndex];
					}
					if(tempBuffLen <= 0)
					{
						tempStatus = 4;
					}
				}
					break;
				case 4:
				{
					//得到Crc16

					crc16 = tbuff[tempIndex];
					tempStatus = 5;
				}
					break;
				case 5://判断校验是否正确
				{
					crc16 = ((((crc16 << 8) & 0xFF00) | tbuff[tempIndex]) & 0xFFFF);
					if(crc16 == onGetCRC16(tbuff, airAddrBuffLen + 3))
					{
						//检验成功 处理指令
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

TypeCentralAirConditioningMD::TypeCentralAirConditioningMD(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo)
{
	airAddrBuffLen = 5 + 4;
	airAddrBuff = new TypeChar((uint32_t(airAddrBuffLen)));
	airAddrBuff->ubuff[0] = (uint8_t)(airAddrBuffLen - 1);//数据长度
	airAddrBuff->ubuff[1] = 0x01;//网关地址
	cmdID = 0x04;
	airAddrBuff->ubuff[2] = (uint8_t)(cmdID & 0xFF);//命令码
	int regAddr = ((appinfo->addr & 0xFF00) ? 2000 : 0) + 16 * (appinfo->addr & 0xFF);
	airAddrBuff->ubuff[3] = (uint8_t)((regAddr >> 8) & 0xFF);
	airAddrBuff->ubuff[4] = (uint8_t)(regAddr & 0xFF);
	airAddrBuff->ubuff[5] = 0;
	airAddrBuff->ubuff[6] = 4;
	if(tcmdid == 10000)
	{
		//主动去读取空调的状态信息

	}
	crc16 = (uint32_t)onGetCRC16(&airAddrBuff->ubuff[1], airAddrBuffLen - 3);
	airAddrBuff->ubuff[7] = (uint8_t)((crc16 >> 8)  & 0xFF);
	airAddrBuff->ubuff[8] = (uint8_t)(crc16& 0xFF);
	/*
	gatewayAddr = 0;
	cmdID = 0;
	ctlValue = 0;
	airCnt = 0;
	airAddrBuff = NULL;
	airAddrBuffLen = 0;
	ApplianceValueChangedNotification valueChangedNotification;
	valueChangedNotification.set_appliance_id(appinfo->appID);
	valueChangedNotification.set_value(appinfo->value);
	mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
	//合成一条命令
	TypeChar *tempRS485Para = new TypeChar(16);
	tempRS485Para->onAddInt64Ex(0, 0x01020801);//9600 even cs8 1
	pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000D, ZCL_DATATYPE_UINT64, tempRS485Para->ubuff, (uint8_t)8), 0);
	delete tempRS485Para;
	//修改一下校验
	if(appinfo)
	{
		airAddrBuffLen = 8;
		airAddrBuff = new TypeChar((uint8_t)airAddrBuffLen);
		airAddrBuff->ubuff[0] = 7;
		airAddrBuff->ubuff[1] = (uint8_t)((appinfo->addr >> 16) & 0xFF);
		switch(tcmdid)
		{
			case 0x00:airAddrBuff->ubuff[2] = 0x31; airAddrBuff->ubuff[3] = 0x02; break;//电源关
			case 0x01:airAddrBuff->ubuff[2] = 0x31; airAddrBuff->ubuff[3] = 0x01; break;//电源开
			case 0x03:airAddrBuff->ubuff[2] = 0x33; airAddrBuff->ubuff[3] = 0x01; break;//制冷
			case 0x04:airAddrBuff->ubuff[2] = 0x33; airAddrBuff->ubuff[3] = 0x08; break;//制热
			case 0x05:airAddrBuff->ubuff[2] = 0x33; airAddrBuff->ubuff[3] = 0x04; break;//送风
			case 0x06:airAddrBuff->ubuff[2] = 0x33; airAddrBuff->ubuff[3] = 0x02; break;//除湿
			case 0x07:airAddrBuff->ubuff[2] = 0x34; airAddrBuff->ubuff[3] = 0x04; break;//风速低
			case 0x08:airAddrBuff->ubuff[2] = 0x34; airAddrBuff->ubuff[3] = 0x02; break;//风速中
			case 0x09:airAddrBuff->ubuff[2] = 0x34; airAddrBuff->ubuff[3] = 0x01; break;//风速高
			case 0x0A:airAddrBuff->ubuff[2] = 0x34; airAddrBuff->ubuff[3] = 0x04; break;//风速低
			case 0x50:airAddrBuff->ubuff[2] = 0x50; airAddrBuff->ubuff[3] = 0x01; break;//查询空调状态
			default:
				if(((tcmdid >= 0x310) && (tcmdid <= 0x31E)) || ((tcmdid >= 0x410) && (tcmdid <= 0x41E)))
				{
					airAddrBuff->ubuff[2] = 0x32; airAddrBuff->ubuff[3] = (uint8_t)(tcmdid & 0x1F);
				}
				else
				{
					//不支持的命令  直接更新一下空调状态
					ApplianceValueChangedNotification valueChangedNotification;
					valueChangedNotification.set_appliance_id(appinfo->appID);
					valueChangedNotification.set_value(appinfo->value);
					mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
				}
				break;
		}
		airAddrBuff->ubuff[4] = 0x01;
		airAddrBuff->ubuff[5] = (uint8_t)((appinfo->addr >> 8) & 0xFF);
		airAddrBuff->ubuff[6] = (uint8_t)((appinfo->addr >> 0) & 0xFF);
		for(int i = 1; i < 7; ++ i)
		{
			airAddrBuff->ubuff[7] += airAddrBuff->ubuff[i];
		}
	}*/
}

void TypeCentralAirConditioningMD::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
	if(appinfo)
	{
		if(cmdID == 0x04)
		{
			//读取保存寄存器返回
		}
		/*
		switch(cmdID)
		{
			case 0x31://向下控制开关  0x01 开机 0x02关机
			case 0x32://向下控制温度 0x10~0x1E 设定温度 16~30度
			case 0x33://向下控制模式 0x01 设定制冷 0x02 设定除湿 0x04 设定送风 0x08 设定制热
			case 0x34://向下控制风速 0x01 高速 0x02 中速 0x04 低速
			{
				//所有这些控制完成后都主动查询一下中央空调的状态  有主动上报状态
				//RS485Profile *temp485Profile = new RS485Profile(0x50, NULL, 0, appinfo, tshortaddr);
				//pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
				//delete temp485Profile;
			}
				break;
				//case 0x35://向下控制风向
				//	break;
				//case 0x36://向下控制湿度
				//	break;
				//case 0x37://向下控制洁度
				//	break;
			case 0x50://向下查询空调状态返回
			{
				//更新空调状态 10Byte  空调外机 空调内机 开关状态 温度设定 模式设定 风速设定 房间温度 故障代码 备用1 备用2
				//一般我一次只读取一个中央空调的状态
				for(int i = 0; i < airCnt; ++ i)
				{
					TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfoKeyIDAndAddr(appinfo->ir_id, ((uint32_t)gatewayAddr << 16) + (((int32_t)airAddrBuff->ubuff[i * 10]) << 8) + (int32_t)airAddrBuff->ubuff[i * 10 + 1]);
					if(tempApplianceInfo)
					{
						//找到这个中央空调了
						int32_t tempStatus = 0;
						if(airAddrBuff->ubuff[i * 10 + 2] == 0x01) tempStatus |= 0x01;
						if(airAddrBuff->ubuff[i * 10 + 4] == 0x01) tempStatus |= 0x02 << 7;//制冷
						else if(airAddrBuff->ubuff[i * 10 + 4] == 0x08) tempStatus |= 0x03 << 7;//制热
						else if(airAddrBuff->ubuff[i * 10 + 4] == 0x04) tempStatus |= 0x04 << 7;//送风
						else tempStatus |= 0x05 << 7;//除湿
						if(airAddrBuff->ubuff[i * 10 + 4] == 0x01)//制冷
						{
							tempStatus |= (airAddrBuff->ubuff[i * 10 + 3] << 17);
						}
						else
						{
							tempStatus |= (airAddrBuff->ubuff[i * 10 + 3] << 12);
						}
						if(airAddrBuff->ubuff[i * 10 + 5] == 0x01) tempStatus |= (0x02 << 3);
						else if(airAddrBuff->ubuff[i * 10 + 5] == 0x02) tempStatus |= (0x01 << 3);

						if(tempApplianceInfo->value != tempStatus)
						{
							//更新家电状态
							tempApplianceInfo->value = tempStatus;
							ApplianceValueChangedNotification valueChangedNotification;
							valueChangedNotification.set_appliance_id(tempApplianceInfo->appID);
							valueChangedNotification.set_value(tempApplianceInfo->value);
							mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
						}
					}
				}
			}
				break;
			default:break;
		}*/
	}
}

TypeCentralAirConditioningMD::~TypeCentralAirConditioningMD()
{
	if(airAddrBuff)
	{
		delete airAddrBuff;
	}
};