//
// Created by wenyu xia on 2019-08-13.
//

#include "../Main/WinobleMain.h"

TypeVoiceHW::TypeVoiceHW(uint8_t *tbuff, int32_t len)
{
	cmdID = 0;
	softVer = 0;
	if(len == 13)
	{
		if(tbuff[1] == 0x09)
		{
			softVer = 1;
		}
		cmdID = tbuff[4];
	}
}

void TypeVoiceHW::onToProcessCMD(int roomid)
{
	//命令处理
	TypeChar *tempLights = NULL;
	int status = 0;
	TypeChar *tempScenes = NULL;
	if(softVer)
	{
		//这个是华翌的测试版本 以下是灯光
		switch (cmdID)
		{
			case 0x02: tempLights = new TypeChar("顶灯"); status = 1; break;//打开顶灯
			case 0x03: tempLights = new TypeChar("顶灯"); status = 0; break;//关闭顶灯
			case 0x04: tempLights = new TypeChar("射灯"); status = 1; break;//打开射灯
			case 0x05: tempLights = new TypeChar("射灯"); status = 0; break;//关闭射灯
			case 0x08:case 0x09:case 0x39:case 0x3a:case 0x3b:case 0x3c://打开/关闭空调 制热模式 制热模式 调高一度 调低一度
			case 0x3d:case 0x3e:case 0x3f:case 0x40:case 0x41://上下摆风 左右摆风 停止摆风 调高风量 调低风量
			{
				TypeApplianceInfo *tempApplianceInfo = NULL;
				for(int i = 0; i < pDeviceList->applianceList->size(); ++ i)
				{
					tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(i);
					if((tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION) && (tempApplianceInfo->roomID == roomid) && (tempApplianceInfo->name->onStringContain("空调")))
					{
						switch(cmdID)
						{
							case 0x08:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 1, "");break;//打开空调
							case 0x09:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0, "");break;//关闭空调
							case 0x39:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 3, "");break;//制热
							case 0x3a:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 4, "");break;//制冷
							case 0x3b:case 0x3c://调高一度 调低一度
							{
								int tem = 0;
								//先获取模式
								if(((tempApplianceInfo->value >> 7) & 0x1F) == 0x02)
								{
									//制冷
									tem = (tempApplianceInfo->value >> 17) & 0x1F;
								}
								else if(((tempApplianceInfo->value >> 7) & 0x1F) == 0x03)
								{
									//制热
									tem = (tempApplianceInfo->value >> 12) & 0x1F;
								}
								if(tem)
								{
									if((cmdID == 0x3b) && (tem < 0x20))
									{
										tem ++;
									}
									else if((cmdID == 0x3c) && (tem > 0x10))
									{
										tem --;
									}
									if(((tempApplianceInfo->value >> 7) & 0x1F) == 0x02)
									{
										//制冷
										tem += 0x300;
									}
									else if(((tempApplianceInfo->value >> 7) & 0x1F) == 0x03)
									{
										//制热
										tem += 0x400;
									}
									pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, tem, "");
								}
							}
								break;
							case 0x3d:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0b, "");break;//上下摆风
							case 0x3e:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0c, "");break;//左右摆风
							case 0x3f:
								if(tempApplianceInfo->value & 0x02)
								{
									pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0d, "");//停止上下摆风
								}
								else if(tempApplianceInfo->value & 0x04)
								{
									pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0e, "");//停止左右摆风
								}
							    break;//停止摆风
					          case 0x40:case 0x41://调高风量 调低风量
					          {
						          int value = (tempApplianceInfo->value >> 3) & 0x0F;
						          if(cmdID == 0x40)
						          {
							          if(value < 3)
							          {
								          value ++;

							          }
							          else
							          {
								          value = 0;
							          }
						          }
						          else
						          {
							          if(value > 0)
							          {
								          value --;
							          }
							          else
							          {
								          value = 3;
							          }
						          }
						          pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, value + 7, "");
					          }
								break;
							default:break;
						}

					}
				}
			}
				break;
				case 0x0a:case 0x0b://打开/关闭窗帘 和 电动窗帘
			{
				// 响应窗帘面板
				TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
				TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo)
					{
						for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
						{
							tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
							if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN) && (tempDeviceTypeInfo->roomID == roomid) && (tempDeviceTypeInfo->name->onStringContain("窗帘")))
							{
								pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x0a) ? 100 : 0, false);
							}
						}
					}
				}
				//响应电动窗帘
				TypeApplianceInfo *tempApplianceInfo = NULL;
				for(int i = 0; i < pDeviceList->applianceList->size(); ++ i)
				{
					tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(i);
					if(tempApplianceInfo && (tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN) && (tempApplianceInfo->roomID == roomid) && (tempApplianceInfo->name->onStringContain("窗帘")))
					{
						pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, (cmdID == 0x0a) ? 100 : 0, "");
					}
				}
			}
				break;
			case 0x15: case 0x16://打开/关闭灯光
			{
				//在这个房间查找这个灯光名称
				TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
				TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo)
					{
						for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
						{
							tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
							if(tempDeviceTypeInfo && (tempDeviceTypeInfo->roomID == roomid))
							{
								if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT)
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x15) ? 1 : 0, false);
								}
								else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x15) ? 501 : 0, false);
								}
							}
						}
					}
				}
			}
				break;
			case 0x19: tempLights = new TypeChar("镜前灯"); status = 1; break;//打开镜前灯
			case 0x1a: tempLights = new TypeChar("镜前灯"); status = 0; break;//关闭镜前灯
			case 0x1b: tempLights = new TypeChar("可调灯"); status = 1; break;//打开可调灯
			case 0x1c: tempLights = new TypeChar("可调灯"); status = 0; break;//关闭可调灯
			case 0x1d: tempLights = new TypeChar("普通灯"); status = 1; break;//打开普通灯
			case 0x1e: tempLights = new TypeChar("普通灯"); status = 0; break;//关闭普通灯
			case 0x1F:case 0x20:case 0x21:case 0x22://打开/关闭推窗器 关闭/打开百叶窗
			{
				// 响应窗帘面板
				TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
				TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo)
					{
						for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
						{
							tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
							if(tempDeviceTypeInfo && (tempDeviceTypeInfo->roomID == roomid) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN))
							{
								if((cmdID < 0x21) && tempDeviceTypeInfo->name->onStringContain("推窗器"))
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x1F) ? 100 : 0, false);
								}
								else if((cmdID > 0x20) && tempDeviceTypeInfo->name->onStringContain("百叶窗"))
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x21) ? 0 : 100, false);
								}
							}
						}
					}
				}
			}
				break;
			case 0x28:case 0x29://打开/关闭插座
			{
				// 响应智能插座
				TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
				TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo)
					{
						for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
						{
							tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
							if(tempDeviceTypeInfo && (tempDeviceTypeInfo->roomID == roomid) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH))
							{
								pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x28) ? 1 : 0, false);
							}
						}
					}
				}
			}
				break;
			case 0x2a://停止动作 主要针对窗帘
			{
				if((lastCMDID == 0x0a) || (lastCMDID == 0x0b) || (lastCMDID == 0x1f) || (lastCMDID == 0x20) || (lastCMDID == 0x21) || (lastCMDID == 0x22))
				{
					//发送一个停止命令
					// 响应窗帘面板
					TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
					TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
					for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
					{
						tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
						if(tempDBDeviceInfo)
						{
							for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
							{
								tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
								if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN) && (tempDeviceTypeInfo->roomID == lastRoomID) && (tempDeviceTypeInfo->name->onStringContain("窗帘") || tempDeviceTypeInfo->name->onStringContain("百叶窗") || tempDeviceTypeInfo->name->onStringContain("推窗器")))
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, 501, false);
								}
							}
						}
					}
					//响应电动窗帘
					TypeApplianceInfo *tempApplianceInfo = NULL;
					for(int i = 0; i < pDeviceList->applianceList->size(); ++ i)
					{
						tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(i);
						if(tempApplianceInfo && (tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN) && (tempApplianceInfo->roomID == lastRoomID) && (tempApplianceInfo->name->onStringContain("窗帘")))
						{
							pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 103, "");
						}
					}
				}
			}
				break;
			case 0x2f: tempLights = new TypeChar("台灯"); status = 1; break;//打开台灯
			case 0x30: tempLights = new TypeChar("台灯"); status = 0; break;//关闭台灯
			case 0x31: tempLights = new TypeChar("吊灯"); status = 1; break;//打开吊灯
			case 0x32: tempLights = new TypeChar("吊灯"); status = 0; break;//关闭吊灯
			case 0x33: tempLights = new TypeChar("壁灯"); status = 1; break;//打开壁灯
			case 0x34: tempLights = new TypeChar("壁灯"); status = 0; break;//关闭壁灯
			case 0x35: tempLights = new TypeChar("氛围灯"); status = 1; break;//打开氛围灯
			case 0x36: tempLights = new TypeChar("氛围灯"); status = 0; break;//关闭氛围灯
			case 0x37: tempLights = new TypeChar("筒灯"); status = 1; break;//打开筒灯
			case 0x38: tempLights = new TypeChar("筒灯"); status = 0; break;//关闭筒灯
			//以下是场景
			case 0x0c: tempScenes = new TypeChar("睡眠"); break;//睡眠模式
			case 0x0d: tempScenes = new TypeChar("阅读"); break;//阅读模式
			case 0x0e: tempScenes = new TypeChar("夜起"); break;//夜起模式
			case 0x0f: tempScenes = new TypeChar("温馨"); break;//温馨模式
			case 0x10: tempScenes = new TypeChar("用餐"); break;//用餐模式
			case 0x11: tempScenes = new TypeChar("会客"); break;//会客模式
			case 0x12: tempScenes = new TypeChar("观影"); break;//观影模式
			case 0x13: tempScenes = new TypeChar("回家"); break;//回家模式
			case 0x14: tempScenes = new TypeChar("离家"); break;//离家模式
			case 0x18: tempScenes = new TypeChar("娱乐"); break;//娱乐模式
			case 0x23: tempScenes = new TypeChar("休息"); break;//休息模式
			case 0x24: tempScenes = new TypeChar("工作"); break;//工作模式
			case 0x25: tempScenes = new TypeChar("度假"); break;//度假模式
			case 0x26: tempScenes = new TypeChar("起床"); break;//起床模式
			case 0x27: tempScenes = new TypeChar("场景"); break;//场景模式

			case 0x42:case 0x43:
			{
				//全部打开  全部关闭 全部打开/全部关闭  只操作开关、调光、插座、窗帘(电动窗帘)、空调
				TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
				TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo)
					{
						for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
						{
							tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
							if(tempDeviceTypeInfo && (tempDeviceTypeInfo->roomID == roomid))
							{
								if((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT) || (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH))
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x42) ? 1 : 0, false);
								}
								else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x42) ? 501 : 0, false);
								}
								else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN)
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdID == 0x42) ? 100 : 0, false);
								}
							}
						}
					}
				}
				//响应电动窗帘
				TypeApplianceInfo *tempApplianceInfo = NULL;
				for(int i = 0; i < pDeviceList->applianceList->size(); ++ i)
				{
					tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(i);
					if(tempApplianceInfo && (tempApplianceInfo->roomID == roomid))
					{
						if(tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN)
						{
							pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, (cmdID == 0x42) ? 100 : 0, "");
						}
						else if(tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION)
						{
							pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, (cmdID == 0x42) ? 1 : 0, "");
						}
					}
				}
			}
				break;
			case 0x44:case 0x45:case 0x46:case 0x47://调亮一点 调暗一点 再暗一点 再亮一点
			{
				//只调节所有开着的调光设备
				TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
				TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo)
					{
						for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
						{
							tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
							if(tempDeviceTypeInfo && (tempDeviceTypeInfo->roomID == roomid) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER) && (tempDeviceTypeInfo->onGetStatus() > 0))
							{
								int32_t tempValue = tempDeviceTypeInfo->onGetStatus();
								int32_t dtValue = (int)(tempValue * 0.2);
								if(dtValue < 10) dtValue = 10;
								if((cmdID == 0x44) || (cmdID == 0x47))
								{
									tempValue += dtValue;
									if(tempValue > 100) tempValue = 100;
								}
								else
								{
									tempValue -= dtValue;
									if(tempValue < 5) tempValue = 5;
								}
								pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, tempValue, false);
							}
						}
					}
				}
			}
			break;
			default:
				break;
		}
		//用于响应连接命令
		lastRoomID = roomid;
		lastCMDID = cmdID;
		//mPrintf(Log_Error, "lroomID = %d, lastCMDID=%d", lastRoomID, lastCMDID);
		//还没有实现的语音命令词:打开/关闭净化器(0x2b/0x2c) 打开/关闭扫地机(0x2D/0x2E)
		//打开/关闭新风(0x48/0x49) 打开/关闭烟机(0x4A/0x4B) 打开/关闭风扇(0x4C/0x4D)
	}
	else
	{
		switch (cmdID)
		{
			case 2: tempLights = new TypeChar("厨房灯"); status = 1; break;//打开厨房灯
			case 3: tempLights = new TypeChar("厨房灯"); status = 0; break;//关闭厨房灯
			case 4: tempLights = new TypeChar("阳台灯"); status = 1; break;//打开阳台灯
			case 5: tempLights = new TypeChar("阳台灯"); status = 0; break;//关闭阳台灯
			case 6: tempLights = new TypeChar("洗手间灯"); status = 1; break;//打开洗手间灯
			case 7: tempLights = new TypeChar("洗手间灯"); status = 0; break;//关闭洗手间灯
			case 8: tempLights = new TypeChar("客厅灯"); status = 1; break;//打开客厅灯
			case 9: tempLights = new TypeChar("客厅灯"); status = 0; break;//关闭客厅灯
			case 10: tempLights = new TypeChar("走廊灯"); status = 1; break;//打开走廊灯
			case 11: tempLights = new TypeChar("走廊灯"); status = 0; break;//关闭走廊灯
			case 12: tempLights = new TypeChar("办公室灯"); status = 1; break;//打开办公司灯
			case 13: tempLights = new TypeChar("办公室灯"); status = 0; break;//关闭办公司灯
			case 14: tempLights = new TypeChar("车库灯"); status = 1; break;//打开车库灯
			case 15: tempLights = new TypeChar("车库灯"); status = 0; break;//关闭车库灯
			case 16: tempLights = new TypeChar("壁灯"); status = 1; break;//打开壁灯
			case 17: tempLights = new TypeChar("壁灯"); status = 0; break;//关闭壁灯
			case 18: tempLights = new TypeChar("会议室灯"); status = 1; break;//打开会议室灯
			case 19: tempLights = new TypeChar("会议室灯"); status = 0; break;//关闭会议室灯
			case 20: tempLights = new TypeChar("卧室灯"); status = 1; break;//打开卧室灯
			case 21: tempLights = new TypeChar("卧室灯"); status = 0; break;//关闭卧室灯
			case 22: tempLights = new TypeChar("氛围灯"); status = 1; break;//打开氛围灯
			case 23: tempLights = new TypeChar("氛围灯"); status = 0; break;//关闭氛围灯
			case 24: tempLights = new TypeChar("吧台灯"); status = 1; break;//打开吧台灯
			case 25: tempLights = new TypeChar("吧台灯"); status = 0; break;//关闭吧台灯
			case 26: tempLights = new TypeChar("筒灯"); status = 1; break;//打开筒灯
			case 27: tempLights = new TypeChar("筒灯"); status = 0; break;//关闭筒灯
			case 30: tempLights = new TypeChar("夜灯"); status = 1; break;//打开夜灯
			case 31: tempLights = new TypeChar("夜灯"); status = 0; break;//关闭夜灯
			case 32: tempLights = new TypeChar("射灯"); status = 1; break;//打开射灯
			case 33: tempLights = new TypeChar("射灯"); status = 0; break;//关闭射灯
			case 34: tempLights = new TypeChar("吊灯"); status = 1; break;//打开吊灯
			case 35: tempLights = new TypeChar("吊灯"); status = 0; break;//关闭吊灯
			case 36: tempScenes = new TypeChar("会客"); break;//会客模式
			case 37: tempScenes = new TypeChar("温馨"); break;//温馨模式
			case 38: tempScenes = new TypeChar("明亮"); break;//明亮模式
			default:
				break;
		}
		if(cmdID == 28)
		{
			//在这个房间查找这个灯光名称
			TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
			TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
			for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
			{
				tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
				if(tempDBDeviceInfo)
				{
					for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
					{
						tempDeviceTypeInfo = (TypeDeviceTypeInfo *)tempDBDeviceInfo->onGetSubInfo(j);
						if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT) && (tempDeviceTypeInfo->roomID == roomid))
						{
							pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, 1, false);
						}
					}
				}
			}
		}
		else if(cmdID == 29)
		{
			//在这个房间查找这个灯光名称
			TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
			TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
			for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
			{
				tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
				if(tempDBDeviceInfo)
				{
					for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
					{
						tempDeviceTypeInfo = (TypeDeviceTypeInfo *)tempDBDeviceInfo->onGetSubInfo(j);
						if(tempDeviceTypeInfo && ((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT) || (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)) && (tempDeviceTypeInfo->roomID == roomid))
						{
							pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, 0, false);
						}
					}
				}
			}
		}
	}

	if(tempLights)
	{
		//在这个房间查找这个灯光名称
		TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
		TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
		for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
		{
			tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
			if(tempDBDeviceInfo)
			{
				for(int j = 1; j <= tempDBDeviceInfo->subCount; ++ j)
				{
					tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
					if(tempDeviceTypeInfo && (tempDeviceTypeInfo->roomID == roomid))
					{
						if((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT) && tempDeviceTypeInfo->name->onStringContain(tempLights->buff))
						{
							pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, status, false);
						}
						else if((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER) && tempDeviceTypeInfo->name->onStringContain(tempLights->buff))
						{
							pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, status ? 501 : 0, false);
						}
					}
				}
			}
		}
		delete tempLights;
	}
	if(tempScenes)
	{
		//在这个房间查找这个场景名称
		TypeSceneNameInfo *tempSceneInfo = NULL;
		for(int i = 0; i < pDeviceList->sceneList->size(); ++ i)
		{
			tempSceneInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			if(tempSceneInfo && (tempSceneInfo->room_id == roomid))
			{
				if(tempSceneInfo->name->onStringContain(tempScenes->buff))
				{
					pDeviceList->onSetSceneStatus(tempSceneInfo, 1, TRUE);
					break;
				}
			}
		}
		delete tempScenes;
	}
}
