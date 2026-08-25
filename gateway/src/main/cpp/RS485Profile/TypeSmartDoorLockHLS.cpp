//
// Created by wenyu xia on 2018/10/16.
//

#include "../Main/WinobleMain.h"

TypeSmartDoorLockHLS::TypeSmartDoorLockHLS(uint8_t *tbuff, int32_t len)
{
	buff = NULL;
	cmdID = 0;
	buffLen = 0;
	addr = 0;
	int32_t tempIndex = 0;
	int32_t status = 0;
	int32_t checkValue = 0;
	while(tempIndex < len)
	{
		switch(status)
		{
			case 0: if(tbuff[tempIndex] == 0x02) {status = 1;}break;//头固定
			case 1:addr = tbuff[tempIndex];status = 2; checkValue += tbuff[tempIndex];break;
			case 2:addr = (addr << 8) + tbuff[tempIndex];status = 3; checkValue += tbuff[tempIndex];break;
			case 3:addr = (addr << 8) + tbuff[tempIndex];status = 4; checkValue += tbuff[tempIndex];break;
			case 4:cmdID = tbuff[tempIndex];status = 5; checkValue += tbuff[tempIndex]; break;
			case 5:
			{
				if(tbuff[tempIndex] == 0xFF)//尾固定
				{
					status = 0;
					if(buffLen > 0)
					{
						buff = new TypeChar((uint32_t)(buffLen -  1));
						for(int i = 0; i < (buffLen - 1); ++ i)
						{
							buff->ubuff[i] = tbuff[tempIndex - buffLen + i];
							checkValue += buff->ubuff[i];
						}
						if((checkValue & 0xFF) == tbuff[tempIndex - 1])
						{
							//校验成功 其它的先丢弃
							return;
						}
						else
						{
							if(buff) delete buff;
							buff = NULL;
							cmdID = 0;
							buffLen = 0;
							addr = 0;
							checkValue = 0;
						}
					}
				}
				else
				{
					buffLen++;
				}
			}
				break;
			default:break;
		}
		tempIndex++;
	}
}

void TypeSmartDoorLockHLS::onToProcessCMD(TypeApplianceInfo *appinfo)
{
	switch(cmdID)
	{
		case 0x07://电量足 锁开
		case 0x47://电量足 锁关
		case 0x87://电量不足 锁开
		case 0xc7://电量不足 锁关
		case 0x44://也是关锁
		{
			//检查地址是否需要更新
			if(appinfo->addr != addr)
			{
				//更新一下地址
				appinfo->addr = addr;
				ModifyApplianceRequest modifyApplianceRequest;
				modifyApplianceRequest.set_id(appinfo->appID);
				modifyApplianceRequest.set_device_id(appinfo->ir_id);
				modifyApplianceRequest.set_sub_id(appinfo->ir_sub_id);
				modifyApplianceRequest.set_attr_mask(APPLIANCE_ATTR_MASK_ADDR);
				modifyApplianceRequest.set_addr(addr);
				mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_MODIFY_REQ, modifyApplianceRequest.SerializeAsString().c_str(), modifyApplianceRequest.SerializeAsString().length());
			}

			appinfo->value &= 0xFFFF00;
			if((cmdID == 0x07) || (cmdID == 0x87))
			{
				appinfo->value |= 1;
			}
			ApplianceValueChangedNotification valueChangedNotification;
			valueChangedNotification.set_appliance_id(appinfo->appID);
			valueChangedNotification.set_value(appinfo->value);
			mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
			//检查是否是用户开锁上报 而不是远程的
			if(buffLen > 2)
			{
				int32_t userID = buff->ubuff[1] & 0x0F;
				userID = userID * 10 + ((buff->ubuff[0] >> 4) & 0x0F);
				userID = userID * 10 + (buff->ubuff[0] & 0x0F);
				int32_t status = appinfo->value & 0xFF;
				//查找场景执行条件
				//检查一个所有用户执行的条件
				pDeviceList->onCheckSceneCarried(2, appinfo->appID, 0, status);
				//检查一个只有这个用户才有的执行条件
				pDeviceList->onCheckSceneCarried(2, appinfo->appID, userID, status);
			}
		}
			break;
		case 0xDD:
		{
			//更新一下地址
			appinfo->addr = addr;
			ModifyApplianceRequest modifyApplianceRequest;
			modifyApplianceRequest.set_id(appinfo->appID);
			modifyApplianceRequest.set_device_id(appinfo->ir_id);
			modifyApplianceRequest.set_sub_id(appinfo->ir_sub_id);
			modifyApplianceRequest.set_attr_mask(APPLIANCE_ATTR_MASK_ADDR);
			modifyApplianceRequest.set_addr(addr);
			mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_MODIFY_REQ, modifyApplianceRequest.SerializeAsString().c_str(), modifyApplianceRequest.SerializeAsString().length());
		}
			break;//智能门锁模块 序列号上报
		case 0x05:
		{
			//更新门锁的状态  把用户ID存到里面
			/*
			appinfo->value &= 0xFF;
			appinfo->value |= (((temp485CMDInfo->buff->ubuff[1] << 8) + temp485CMDInfo->buff->ubuff[0]) << 8);
			ApplianceValueChangedNotification valueChangedNotification;
			valueChangedNotification.set_appliance_id(appinfo->id);
			valueChangedNotification.set_value(appinfo->value);
			mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
			*/
		}
			break;//门锁与模块关联成功报告
			//case 0x01:break;//开锁命令
			//case 0x02:break;//关锁命令
			//case 0x03:break;//查询命令
		case 0x04:break;//关锁成功返回
			//case 0x09:break;//删除用户
		case 0x1C:break;//删除用户成功返回
		case 0x1D:
			break;//按门铃上报
		case 0x08:break;//非法用户开锁报警  0x11=指纹 0x0E=密码 0x0A=卡
		case 0x12:break;//门被撬了
		case 0x14:break;//锁被撬了
		default:
			mPrintf(Log_Error, "Error:智能门锁有未解析的命令类型!");
			break;
	}
}

TypeSmartDoorLockHLS::TypeSmartDoorLockHLS(int32_t taddr, int32_t tcmdid, uint8_t *tbuff, int32_t len)
{
	//目前门锁就只有0 和 1两个开关命令
	if((tcmdid == 0) || (tcmdid == 1))
	{
		uint8_t andCheck = 0;
		if(tcmdid == 1)
		{
			cmdID = 1;
		}
		else
		{
			cmdID = 2;
		}
		addr = taddr;
		buff = new TypeChar(8);
		buffLen = 8;
		buff->buff[0] = 7;
		buff->buff[1] = 0x02;//固定头
		buff->buff[2] = (uint8_t)((addr >> 16) & 0xFF);
		andCheck += buff->buff[2];
		buff->buff[3] = (uint8_t)((addr >> 8) & 0xFF);
		andCheck += buff->buff[3];
		buff->buff[4] = (uint8_t)(addr & 0xFF);
		andCheck += buff->buff[4];
		buff->buff[5] = (uint8_t)cmdID;
		andCheck += buff->buff[5];
		buff->buff[6] = andCheck;
		buff->buff[7] = (uint8_t)0xFF;//固定尾
	}
}

TypeSmartDoorLockHLS::~TypeSmartDoorLockHLS()
{
	if(buff)
	{
		delete buff;
	}
}
