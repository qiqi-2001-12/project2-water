//
// Created by wenyu xia on 2019-12-18.
//
#include "../Main/WinobleMain.h"

void TypeOffLineVoiceStatus::onToProcessCMD(int roomid, uint8_t *ubuff, uint8_t len)
{
	if((ubuff == NULL) || (len != 12)) return;
	uint8_t cmdID = ubuff[1];
	uint8_t cmdValue = ubuff[4];
	//这个是华翌离线语音设备正式版本 以下是灯光
	switch(cmdID)
	{
		case 0x11://语音唤醒-主动上报
		{
			status = 1;
		}
			break;
		case 0x12://语音唤醒结束
		{
			status = 0;
		}
			break;
		case 0x13://语音ID执行
		{
			TypeChar *tempLights = NULL;
			int flag = 0;
			TypeChar *tempScenes = NULL;
			switch (cmdValue)
			{
				case 0x04: tempLights = new TypeChar("镜前灯"); flag = 1; break;//打开镜前灯
				case 0x05: tempLights = new TypeChar("镜前灯"); flag = 0; break;//关闭镜前灯
				case 0x06: tempLights = new TypeChar("顶灯"); flag = 0; break;//打开顶灯
				case 0x07: tempLights = new TypeChar("顶灯"); flag = 1; break;//关闭顶灯
				case 0x08: tempLights = new TypeChar("可调灯"); flag = 1; break;//打开可调灯
				case 0x09: tempLights = new TypeChar("可调灯"); flag = 0; break;//关闭可调灯
				case 0x0a: tempLights = new TypeChar("普通灯"); flag = 1; break;//打开普通灯
				case 0x0b: tempLights = new TypeChar("普通灯"); flag = 0; break;//关闭普通灯
				case 0x20: tempLights = new TypeChar("台灯"); flag = 1; break;//打开台灯
				case 0x21: tempLights = new TypeChar("台灯"); flag = 0; break;//关闭台灯
				case 0x22: tempLights = new TypeChar("吊灯"); flag = 1; break;//打开吊灯
				case 0x23: tempLights = new TypeChar("吊灯"); flag = 0; break;//关闭吊灯
				case 0x24: tempLights = new TypeChar("壁灯"); flag = 1; break;//打开壁灯
				case 0x25: tempLights = new TypeChar("壁灯"); flag = 0; break;//关闭壁灯
				case 0x26: tempLights = new TypeChar("射灯"); flag = 1; break;//打开射灯
				case 0x27: tempLights = new TypeChar("射灯"); flag = 0; break;//关闭射灯
				case 0x28: tempLights = new TypeChar("氛围灯"); flag = 1; break;//打开氛围灯
				case 0x29: tempLights = new TypeChar("氛围灯"); flag = 0; break;//关闭氛围灯
				case 0x2a: tempLights = new TypeChar("筒灯"); flag = 1; break;//打开筒灯
				case 0x2b: tempLights = new TypeChar("筒灯"); flag = 0; break;//关闭筒灯
					//以下是场景
				case 0x33: tempScenes = new TypeChar("睡眠"); break;//睡眠模式
				case 0x34: tempScenes = new TypeChar("阅读"); break;//阅读模式
				case 0x11: tempScenes = new TypeChar("夜起"); break;//夜起模式
				case 0x35: tempScenes = new TypeChar("温馨"); break;//温馨模式
				case 0x32: tempScenes = new TypeChar("用餐"); break;//用餐模式
				case 0x30: tempScenes = new TypeChar("会客"); break;//会客模式
				case 0x12: tempScenes = new TypeChar("观影"); break;//观影模式
				case 0x2e: tempScenes = new TypeChar("回家"); break;//回家模式
				case 0x2f: tempScenes = new TypeChar("离家"); break;//离家模式
				case 0x31: tempScenes = new TypeChar("娱乐"); break;//娱乐模式
				case 0x10: tempScenes = new TypeChar("休息"); break;//休息模式
				case 0x13: tempScenes = new TypeChar("工作"); break;//工作模式
				case 0x14: tempScenes = new TypeChar("度假"); break;//度假模式
				case 0x15: tempScenes = new TypeChar("起床"); break;//起床模式
				case 0x16: tempScenes = new TypeChar("场景"); break;//场景模式

				case 0x36:case 0x37:case 0x38:case 0x39:case 0x3a:case 0x3b://打开/关闭空调 制冷模式 制热模式 调高一度 调低一度
				case 0x3c:case 0x3d:case 0x3e:case 0x3f:case 0x40://上下摆风 左右摆风 停止摆风 调高风量 调低风量
				{
					TypeApplianceInfo *tempApplianceInfo = NULL;
					for(int i = 0; i < pDeviceList->applianceList->size(); ++ i)
					{
						tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(i);
						if((tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION) && (tempApplianceInfo->roomID == roomid) && (tempApplianceInfo->name->onStringContain("空调")))
						{
							switch(cmdValue)
							{
								case 0x36:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 1, "");break;//打开空调
								case 0x37:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0, "");break;//关闭空调
								case 0x38:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 3, "");break;//制热
								case 0x39:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 4, "");break;//制冷
								case 0x3a:case 0x3b://调高一度 调低一度
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
										if((cmdValue == 0x3a) && (tem < 0x20))
										{
											tem ++;
										}
										else if((cmdValue == 0x3b) && (tem > 0x10))
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
								case 0x3c:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0b, "");break;//上下摆风
								case 0x3d:pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0c, "");break;//左右摆风
								case 0x3e:
									if(tempApplianceInfo->value & 0x02)
									{
										pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0d, "");//停止上下摆风
									}
									else if(tempApplianceInfo->value & 0x04)
									{
										pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 0x0e, "");//停止左右摆风
									}
									break;//停止摆风
								case 0x3f:case 0x40://调高风量 调低风量
								{
									int value = (tempApplianceInfo->value >> 3) & 0x0F;
									if(cmdValue == 0x3f)
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
				case 0x2c:case 0x2d://打开/关闭窗帘 和 电动窗帘
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
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x2c) ? 100 : 0, false);
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
							pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, (cmdValue == 0x2c) ? 100 : 0, "");
						}
					}
				}
					break;
				case 0x1e: case 0x1f://打开/关闭灯光
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
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x1e) ? 1 : 0, false);
									}
									else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
									{
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x1e) ? 501 : 0, false);
									}
								}
							}
						}
					}
				}
					break;
				case 0x0c:case 0x0d:case 0x0e:case 0x0f://打开/关闭推窗器 关闭/打开百叶窗
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
									if((cmdValue < 0x0e) && tempDeviceTypeInfo->name->onStringContain("推窗器"))
									{
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x0c) ? 100 : 0, false);
									}
									else if((cmdValue > 0x0d) && tempDeviceTypeInfo->name->onStringContain("百叶窗"))
									{
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x0e) ? 0 : 100, false);
									}
								}
							}
						}
					}
				}
					break;
				case 0x17:case 0x18://打开/关闭插座
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
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x17) ? 1 : 0, false);
								}
							}
						}
					}
				}
					break;
				case 0x19://停止动作 主要针对窗帘
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
								if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN) && (tempDeviceTypeInfo->roomID == roomid) && (tempDeviceTypeInfo->name->onStringContain("窗帘") || tempDeviceTypeInfo->name->onStringContain("百叶窗") || tempDeviceTypeInfo->name->onStringContain("推窗器")))
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
						if(tempApplianceInfo && (tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN) && (tempApplianceInfo->roomID == roomid) && (tempApplianceInfo->name->onStringContain("窗帘")))
						{
							pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, 103, "");
						}
					}
				}
					break;
				case 0x41:case 0x42:
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
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x41) ? 1 : 0, false);
									}
									else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
									{
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x41) ? 501 : 0, false);
									}
									else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN)
									{
										pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, (cmdValue == 0x41) ? 100 : 0, false);
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
								pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, (cmdValue == 0x41) ? 100 : 0, "");
							}
							else if(tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION)
							{
								pDeviceList->onSetApplianceStatus(tempApplianceInfo->appID, (cmdValue == 0x41) ? 1 : 0, "");
							}
						}
					}
				}
					break;
				case 0x43:case 0x44:case 0x45:case 0x46://调亮一点 调暗一点 再暗一点 再亮一点
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
									if((cmdValue == 0x43) || (cmdValue == 0x46))
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
			//mPrintf(Log_Error, "lroomID = %d, lastCMDID=%d", lastRoomID, lastCMDID);
			//还没有实现的语音命令词:打开/关闭净化器(0x2b/0x2c) 打开/关闭扫地机(0x2D/0x2E)
			//打开/关闭新风(0x48/0x49) 打开/关闭烟机(0x4A/0x4B) 打开/关闭风扇(0x4C/0x4D)

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
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, flag, false);
								}
								else if((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER) && tempDeviceTypeInfo->name->onStringContain(tempLights->buff))
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, flag ? 501 : 0, false);
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
			break;
		case 0x15://设置唤醒词返回
			break;
		case 0x19://设置唤醒时长返回
			break;
	}
	//用于响应连接命令
	lastCMDValue = cmdValue;
	lastCMDID = cmdID;
}

