//
// Created by xia_w on 2017/9/14.
//
#include <sys/ioctl.h>
#include "../Main/WinobleMain.h"
#include <net/if.h>
#define UDPSERVER_MAXDATASIZE 1024
//机器人对接-公子小白
static void * mfTCPServerThread(void * arg);
static char * mfNetGetLocalIP();
static int onGetNumWithString(char * str);
static void * mfTCPConnectThread(void * arg);
static int mCurrentPort = 8899;
static int mUDPClientfd = -1;
static const char *onGetDeviceType(int devicetype);
static int mNetLogClientfd = -1;
static struct sockaddr_in mAirUpdateAddr;
static struct sockaddr_in mMasterLastAirUpdateAddr;
static int32_t mMasterLastAirDeviceID = 0;
static struct sockaddr_in mSlaveLastAirUpdateAddr;
static int32_t mSlaveLastAirDeviceID = 0;
static void onUDPDevVerTimerOutCB(int32_t par1, int32_t par2);
TypeLinkedList *mNetLogLinkList = new TypeLinkedList(ArrayTypeRobotDataInfo);
//#define ROBOT_GATEWAY_SERIAL  "HYG0018061Y0007A"//"HYG0018061V000FB"
void * mfRebotDockingThread(void * arg)
{
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	int retLen = 0;
	TypeChar *tempUDPReviceBuff = new TypeChar(UDPSERVER_MAXDATASIZE);
	struct sockaddr_in mNet_UDP_ServerAddr;
	int mNet_UDP_Status = NetStatusInit;
	//创建一个TCP服务器
	onAddThread("TCP S", mfTCPServerThread, (char *)"");//创建一个TCP服务器

	int len = sizeof(struct sockaddr_in);
	//创建一个UDP服务器
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	while(mIsExitFlag)
	{
		if((onGetTimeSec() - tempThreadInfo->lastSaveTime) >= 10)//10s 上报一次线程的健康状态
		{
			tempThreadInfo->lastSaveTime = onGetTimeSec();
			mPrintf(Log_NetWork, "thread %s:netStat=%d", tempThreadInfo->title->buff, mNet_UDP_Status);
		}
		switch((char) mNet_UDP_Status)
		{
			case NetStatusInit:
				mUDPClientfd = socket(AF_INET, SOCK_DGRAM, 0);
				if(mUDPClientfd == -1)
				{
					mPrintf(LOG_Robot, "socket() failure! ");
					mNet_UDP_Status = NetStatusClose;
				}
				else
				{
					tempThreadInfo->pNetFD = &mUDPClientfd;
					//mNet_UDP_Status = NetStatusConnect;
					/* init servaddr */
					bzero(&mNet_UDP_ServerAddr, sizeof(struct sockaddr_in));
					mNet_UDP_ServerAddr.sin_family = AF_INET;
					mNet_UDP_ServerAddr.sin_addr.s_addr = htonl(INADDR_ANY); //#define INADDR_ANY   ((unsigned long int) 0x00000000)
					mNet_UDP_ServerAddr.sin_port = htons(6666);
					if(bind(mUDPClientfd, (struct sockaddr *) &mNet_UDP_ServerAddr, sizeof(mNet_UDP_ServerAddr)) == -1)
					{
						mPrintf(LOG_Robot, "UDP bind() failure! ");
						mNet_UDP_Status = NetStatusClose;
						mUDPClientfd = -1;
					}
					else
					{
						mPrintf(LOG_Robot, "UDP bind() success! ");
						mNet_UDP_Status = NetStatusRec;
					}
				}
				break;
			case NetStatusRec://接收数据
				tempUDPReviceBuff->onClear();
				retLen = recvfrom(mUDPClientfd, tempUDPReviceBuff->buff, UDPSERVER_MAXDATASIZE, 0, (struct sockaddr *) &mNet_UDP_ServerAddr, (socklen_t *) &len);
				if((retLen == 0) || (mUDPClientfd == -1))
				{
					mPrintf(LOG_Robot, "recvfrom() failure! ");
					mNet_UDP_Status = NetStatusClose;
				}
				else if(retLen > 0)
				{
					mPrintf(LOG_Robot, "udp R:%s len=%d ip=%d port=%d ", tempUDPReviceBuff->buff, retLen, mNet_UDP_ServerAddr.sin_addr.s_addr, mNet_UDP_ServerAddr.sin_port);
					//解析一下UDP 数据
					//json 解析
					cJSON *jsonRec = cJSON_Parse(tempUDPReviceBuff->buff);
					if(!jsonRec)
					{
						mPrintf(LOG_Robot, "Err Json:[%s] ", cJSON_GetErrorPtr());
					}
					else
					{
						cJSON *tempJson;
						if(cJSON_GetObjectItem(jsonRec, "udp_type"))
						{
							//首先查找家庭ID 是否一样?
							tempJson = cJSON_GetObjectItem(jsonRec, "udp_familyid");
							if(tempJson && (tempJson->valueint == (uint64_t)pDataBase->onGetFamilyID()))
							{
								//再查找是否是本网关的网关
								tempJson = cJSON_GetObjectItem(jsonRec, "udp_gatewayid");
								if(tempJson && (tempJson->valueint != (uint64_t)pDataBase->onGetGateway_ID()))
								{
									//再查找对方是否没有网络
									tempJson = cJSON_GetObjectItem(jsonRec, "udp_status");
									//另外一个没有网络的网关触发了场景，本网关不管有没有网络肯定要执行的
									if(tempJson && ((tempJson->valueint == 0) || (!onGetConnectFlag())))
									{
										//对方没有网络 或者本网关没有网络都是需要执行的
										tempJson = cJSON_GetObjectItem(jsonRec, "udp_type");
										if(tempJson->valueint == 101)//执行场景
										{
											int64_t tempSceneID = 0;
											int32_t tempSceneStatus = 0;
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_scene_id");
											if(tempJson)
											{
												tempSceneID = tempJson->valueint;
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_scene_status");
												if(tempJson)
												{
													tempSceneStatus = (int32_t )tempJson->valueint;
													pDeviceList->onSetSceneStatus(pDeviceList->onFindSceneInfo(tempSceneID), tempSceneStatus, FALSE);
												}
											}
										}
										else if(tempJson->valueint == 102)//执行报警
										{
											//执行报警处理
											if(onGetConnectFlag())
											{
												//有网的话 直接发送一条报警命令给服务器就好了，设备肯定是不在这个网关的
												int32_t alarmSendFlag = 0;
												DeviceAlarmRequest  deviceAlarmRequest;
												DeviceAlarmInfo * deviceAlarmInfo = deviceAlarmRequest.mutable_device_alarm();
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_type");
												if(tempJson)
												{
													deviceAlarmInfo->set_alarm_type((DeviceAlarmType)tempJson->valueint);
													alarmSendFlag |= 0x01;
												}
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_familyid");
												if(tempJson)
												{
													deviceAlarmInfo->set_family_id((int32_t)tempJson->valueint);
													alarmSendFlag |= 0x02;
												}
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_gatewayid");
												if(tempJson)
												{
													deviceAlarmInfo->set_gateway_id(tempJson->valueint);
													alarmSendFlag |= 0x04;
												}
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_keyid");
												if(tempJson)
												{
													deviceAlarmInfo->set_device_id((int32_t)tempJson->valueint);
													alarmSendFlag |= 0x08;
												}
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_subid");
												if(tempJson)
												{
													deviceAlarmInfo->set_sub_id((int32_t)tempJson->valueint);
													alarmSendFlag |= 0x10;
												}
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_subtype");
												if(tempJson)
												{
													deviceAlarmInfo->set_sub_type((int32_t)tempJson->valueint);
													alarmSendFlag |= 0x20;
												}
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_time");
												if(tempJson)
												{
													deviceAlarmInfo->set_alarm_time((int32_t)tempJson->valueint);
													alarmSendFlag |= 0x40;
												}
												if(alarmSendFlag == 0x7F)
												{
													mfTCPCMDSend(CMD_ID_DEVICE_ALARM_REQ, deviceAlarmRequest.SerializeAsString().c_str(), deviceAlarmRequest.SerializeAsString().length());
												}
											}
											else
											{
												//没网的话 直接发送给网关和设备就好了
												tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_msg");
												if(tempJson)
												{
													TypeChar *tempUnicodeBuff = new TypeChar();
													TypeChar *alarmInfo = new TypeChar();
													alarmInfo->ubuff[0] = (uint8_t )(onConverUnicodeString(tempJson->valuestring, tempUnicodeBuff->buff, 0) + 5);
													alarmInfo->ubuff[1] = 1;
													alarmInfo->ubuff[2] = 0;
													alarmInfo->ubuff[3] = 0;
													alarmInfo->ubuff[4] = 0;
													if(alarmInfo->ubuff[0] > 64) alarmInfo->buff[0] = 64;
													alarmInfo->onAddUBuff(5, tempUnicodeBuff->ubuff, alarmInfo->ubuff[0] - 5);
													pmMasterSerialPort->onWriteZclCMD(SHORTADDR_BROADCAST, 0x01, CLUSTER_ID_PERSONAL, 0x01, alarmInfo->ubuff, alarmInfo->ubuff[0], 0);
													delete alarmInfo;
													delete tempUnicodeBuff;
													mIsAlarmingFlag = 60;//60s后停止

													//发送到app
													int32_t alarmSendFlag = 0;
													char *tempMsg = tempJson->valuestring;
													int64_t tempDevID = 0;
													int64_t tempSubID = 0;
													int64_t tempDevType = 0;
													tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_keyid");
													if(tempJson)
													{
														tempDevID = tempJson->valueint;
														alarmSendFlag |= 0x01;
													}
													tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_subid");
													if(tempJson)
													{
														tempSubID = tempJson->valueint;
														alarmSendFlag |= 0x02;
													}
													tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_subtype");
													if(tempJson)
													{
														tempDevType = tempJson->valueint;
														alarmSendFlag |= 0x04;
													}
													if(alarmSendFlag == 0x07)
													{
														//发送到app 发送一条报警
														onNotifyToJava(JNI_NOTIFY_ALARM, tempDevID, tempSubID, tempDevType, tempMsg);
													}
												}
											}
										}
										else if(tempJson->valueint == 103)//执行屏幕同步
										{
											//查找这个设备是否在本地设备
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_screen_keyid");
											if(tempJson)
											{
												TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempJson->valueint);
												if(tempDBDeviceInfo && tempDBDeviceInfo->shortAddr && tempDBDeviceInfo->onLineFlag.bits.status)
												{
													//发送屏幕共享
													tempJson = cJSON_GetObjectItem(jsonRec, "udp_screen_msg");
													if(tempJson)//发送命令
													{
														pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, 0x0000, new TypeZclAttribute(0x4006, ZCL_DATATYPE_CHAR_STR, (uint8_t *)tempJson->valuestring, (uint8_t)strlen(tempJson->valuestring)), 0);
													}
												}
											}
										}
										else if(tempJson->valueint == 104)//执行远程设备控制
										{
											int32_t tempDeviceID = 0;
											int32_t tempSubID = 0;
											int32_t tempStatus = 0;
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_device_keyid");
											if(tempJson)
											{
												tempDeviceID = (int32_t)tempJson->valueint;
											}
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_device_subid");
											if(tempJson)
											{
												tempSubID = (int32_t)tempJson->valueint;
											}
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_device_status");
											if(tempJson)
											{
												tempStatus = (int32_t)tempJson->valueint;
											}
											pDeviceList->onSetDeviceStatus(pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, tempDeviceID), tempSubID, tempStatus, false);
										}
										else if(tempJson->valueint == 105)//解除报警
										{
											int32_t tempDeviceID = 0;
											int32_t tempAlarmType = 0;
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_keyid");
											if(tempJson)
											{
												tempDeviceID = (int32_t)tempJson->valueint;
											}
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_alarm_type");
											if(tempJson)
											{
												tempAlarmType = (int32_t)tempJson->valueint;
											}
											if(pDeviceList) pDeviceList->onDisAlarmInfo(tempDeviceID, tempAlarmType, false);
										}
										else if(tempJson->valueint == 106)//更新设备状态
										{
											int32_t tempDeviceID = 0;
											int32_t tempSubID = 0;
											int32_t tempSubType = 0;
											int32_t tempDevStatus = 0;
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_keyid");
											if(tempJson)
											{
												tempDeviceID = (int32_t)tempJson->valueint;
											}
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_subid");
											if(tempJson)
											{
												tempSubID = (int32_t)tempJson->valueint;
											}
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_subtype");
											if(tempJson)
											{
												tempSubType = (int32_t)tempJson->valueint;
											}
											tempJson = cJSON_GetObjectItem(jsonRec, "udp_devstatus");
											if(tempJson)
											{
												tempDevStatus = (int32_t)tempJson->valueint;
											}
											//查找这个网关设备
											TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, tempDeviceID);
											if(tempDBDeviceInfo)
											{
												TypeDeviceTypeInfo *tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(tempSubID);
												if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == tempSubType))
												{
													//目前只同步了开关，调光、窗帘、插座、安防状态
													if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT)
													{
														tempDeviceTypeInfo->onGetSubInfo()->lightStatus->status = tempDevStatus;
													}
													else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_CURTAIN)
													{
														tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->status = tempDevStatus;
													}
													else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH)
													{
														tempDeviceTypeInfo->onGetSubInfo()->switchStatus->status = tempDevStatus;
													}
													else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DIMMER)
													{
														tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->status = tempDevStatus;
													}
													else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_DOOR_WINDOE)
													{
														//需要保存到数据库
														tempDeviceTypeInfo->onUpdateTypeInfo(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Name),
														                                     new TypeDeviceTypeInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->roomID, tempDeviceTypeInfo->iconID, tempDeviceTypeInfo->saveIconID, tempDeviceTypeInfo->name->buff, tempDeviceTypeInfo->saveName->buff,
														                                                            SUB_DEVICE_TYPE_DOOR_WINDOW, 0, new TypeDoorWindowStatus(tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->status, tempDeviceTypeInfo->onGetSubInfo()->doorWindowStatus->power, tempDBDeviceInfo->shortAddr_ex, tempDevStatus)));
													}
													else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_PIR)
													{
														tempDeviceTypeInfo->onUpdateTypeInfo(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Name),
														                                     new TypeDeviceTypeInfo(tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->roomID, tempDeviceTypeInfo->iconID, tempDeviceTypeInfo->saveIconID, tempDeviceTypeInfo->name->buff, tempDeviceTypeInfo->saveName->buff,
														                                                            SUB_DEVICE_TYPE_PIR, 0, new TypePIRStatus(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status, tempDeviceTypeInfo->onGetSubInfo()->pirStatus->power, tempDBDeviceInfo->shortAddr_ex, tempDevStatus, tempDeviceTypeInfo->onGetSubInfo()->pirStatus->outDelayTime)));
													}
													//直接把通知发送到应用层
													if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_DEVSTAUS)
													{
														onNotifyToJava(JNI_NOTIFY_UPDATE_DEVSTAUS, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->devType, tempDevStatus);
													}
												}
											}
										}
									}
								}
							}
						}
						else if(cJSON_GetObjectItem(jsonRec, "log_type"))
						{
							tempJson = cJSON_GetObjectItem(jsonRec, "log_type");
							if(tempJson)
							{
								//有log打印 相关命令
								if(tempJson->valueint == 1)
								{
									//返回本机网关相关信息
									cJSON *gatewayjson = cJSON_CreateObject();
									cJSON_AddLongNumberToObject(gatewayjson, "log_type", 2);//代表广播应答
									cJSON_AddStringToObject(gatewayjson, "log_serial", pDataBase->onGetSerial());
									cJSON_AddStringToObject(gatewayjson, "log_name", pDataBase->onGetName());
									cJSON_AddStringToObject(gatewayjson, "log_ip", mfNetGetLocalIP());
									cJSON_AddNumberToObject(gatewayjson, "log_port", (uint32_t) mCurrentPort);
									cJSON_AddNumberToObject(gatewayjson, "log_familyid", (uint32_t) pDataBase->onGetFamilyID());
									cJSON_AddNumberToObject(gatewayjson, "log_devcnt", (uint32_t) pDeviceList->dbDeviceInfoList->size());
									int tempDevCnt = 0;
									TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
									for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
									{
										tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
										if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
										{
											tempDevCnt++;
										}
									}
									cJSON_AddNumberToObject(gatewayjson, "log_gwcnt", (uint32_t) tempDevCnt);
									char *retJsonChars = cJSON_Print(gatewayjson);
									sendto(mUDPClientfd, retJsonChars,
									       strlen(retJsonChars), 0,
									       (struct sockaddr *) (&mNet_UDP_ServerAddr),
									       sizeof(struct sockaddr_in));
									cJSON_Delete(gatewayjson);
									free(retJsonChars);
								}
								else if(tempJson->valueint == 3)
								{
									pDataBase->onToString();
									pDeviceList->onPrintfRoomInfo();
								}
								else if(tempJson->valueint == 5)//搜索没有注册的网关
								{
									//这里最好请求更新一下，更新一下服务器信息
									if(pDataBase->onGetIEEE() && (strlen(pDataBase->onGetSerial()) != 16))
									{
										//请求更新一下序列号
										mfHttpGetGatewayInfo();
									}
									//返回本机网关注册相关信息
									cJSON *gatewayjson = cJSON_CreateObject();
									cJSON_AddNumberToObject(gatewayjson, "log_type", 6);//代表广播应答
									cJSON_AddStringToObject(gatewayjson, "log_serial", pDataBase->onGetSerial());
									cJSON_AddStringToObject(gatewayjson, "log_name", pDataBase->onGetName());
									cJSON_AddNumberToObject(gatewayjson, "log_gwtype", (uint32_t)mGatewayType);
									cJSON_AddStringToObject(gatewayjson, "log_ip", mfNetGetLocalIP());
									cJSON_AddStringToObject(gatewayjson, "log_softver", GATEWAY_SOFTVER);
									cJSON_AddLongNumberToObject(gatewayjson, "log_ieee", (uint64_t)pDataBase->onGetIEEE());
									cJSON_AddLongNumberToObject(gatewayjson, "log_ieee_ex", (uint64_t)pDataBase->onGetIEEE_EX());
									cJSON_AddNumberToObject(gatewayjson, "log_familyid", (uint32_t) pDataBase->onGetFamilyID());
									int tempDevCnt = 0;
									TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
									for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
									{
										tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
										if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
										{
											tempDevCnt++;
										}
									}
									cJSON_AddNumberToObject(gatewayjson, "log_devcnt", (uint32_t) tempDevCnt);
									char *retJsonChars = cJSON_Print(gatewayjson);
									sendto(mUDPClientfd, retJsonChars,
									       strlen(retJsonChars), 0,
									       (struct sockaddr *) (&mNet_UDP_ServerAddr),
									       sizeof(struct sockaddr_in));
									cJSON_Delete(gatewayjson);
									free(retJsonChars);

								}
								else if(tempJson->valueint == 7)
								{
									int32_t tempFamilyID = 0;
									tempJson = cJSON_GetObjectItem(jsonRec, "log_family");
									if(tempJson != NULL)
									{
										tempFamilyID = (int32_t)tempJson->valueint;
										if(tempFamilyID == pDataBase->onGetFamilyID())
										{
											//是这个家庭的就响应一下
											tempJson = cJSON_GetObjectItem(jsonRec, "log_status");
											if(tempJson != NULL)
											{
												TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
												for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
												{
													tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
													if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
													{
														for(int j = 1; j < tempDBDeviceInfo->subCount; ++ j)
														{
															if(tempJson->valueint)
															{
																//全开
																pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, 100, TRUE);
															}
															else
															{
																//全关
																pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, j, 0, TRUE);
															}

														}
													}
												}
											}
										}
									}
								}
								else if(tempJson->valueint == 9)
								{
									//返回设备一个可供升级的设备列表
									cJSON *tempJson = cJSON_CreateObject();
									cJSON *subArray = cJSON_CreateArray();
									//添加设备到列表
									if(pDeviceList != NULL)
									{
										TypeDBDeviceInfo *dbDeviceInfo = NULL;
										//获取整个家庭的所有安防设备
										for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++i)
										{
											dbDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
											if(dbDeviceInfo && (dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
											{
												cJSON *subJson = cJSON_CreateObject();
												cJSON_AddNumberToObject(subJson, "id", (uint32_t)dbDeviceInfo->deviceID);
												cJSON_AddStringToObject(subJson, "serial", dbDeviceInfo->serial->buff);
												cJSON_AddNumberToObject(subJson, "online", (uint32_t)dbDeviceInfo->onLineFlag.bits.status);
												cJSON_AddNumberToObject(subJson, "devtype", (uint32_t)dbDeviceInfo->devType);
												cJSON_AddNumberToObject(subJson, "attr", (uint32_t)dbDeviceInfo->attr->value);
												cJSON_AddStringToObject(subJson, "ver", dbDeviceInfo->swVer->buff);
												cJSON_AddItemToArray(subArray, subJson);
											}
										}
									}
									cJSON_AddNumberToObject(tempJson, "log_type", 10);
									cJSON_AddItemToObject(tempJson, "devlist", subArray);
									char *retJsonChars = cJSON_Print(tempJson);
									sendto(mUDPClientfd, retJsonChars,
									       strlen(retJsonChars), 0,
									       (struct sockaddr *) (&mNet_UDP_ServerAddr),
									       sizeof(struct sockaddr_in));
									free(retJsonChars);
									cJSON_Delete(tempJson);
								}
								else if(tempJson->valueint == 11)
								{
									tempJson = cJSON_GetObjectItem(jsonRec, "devid");
									if(tempJson != NULL)
									{
										//找到对应的设备，然后发送版本检查的命令
										TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempJson->valueint);
										if(tempDBDeviceInfo)
										{
											bool ismaster = TRUE;
											tempJson = cJSON_GetObjectItem(jsonRec, "ismaster");
											if(tempJson != NULL)
											{
												if(tempJson->valueint == 0)
												{
													ismaster = false;
												}
											}
											//设置一下需要应答的网络信息
											memcpy(&mAirUpdateAddr, &mNet_UDP_ServerAddr, sizeof(struct sockaddr_in));
											onTimerAdd(TIMER_AIR_TIMEOUT, 6000, false, onUDPDevVerTimerOutCB, 0, 0);
											if(ismaster)
											{
												//默认只检查主模块
												pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDBDeviceInfo->shortAddr, 1, 0x0000, 0x0001, 0);
											}
											else
											{
												//默认只检查主模块
												if(DUALZIGBEECHIP)
												{
													pmSlaveSerialPort->onReadAttributeGeneric((uint32_t)tempDBDeviceInfo->shortAddr_ex, 1, 0x0000, 0x0001, 0);
												}
											}
										}
									}
								}
								else if(tempJson->valueint == 13)
								{
									//向设备发送一条开始升级命令
									tempJson = cJSON_GetObjectItem(jsonRec, "shortaddr");
									if(tempJson != NULL)
									{
										uint32_t shortAddr = (uint32_t)tempJson->valueint;
										bool ismaster = TRUE;
										tempJson = cJSON_GetObjectItem(jsonRec, "ismaster");
										if(tempJson != NULL)
										{
											if(tempJson->valueint == 0)
											{
												ismaster = false;
											}
										}
										uint8_t sendBuff[2];
										sendBuff[0] = 0;
										sendBuff[1] = 0;
										if(ismaster)
										{
											memcpy(&mMasterLastAirUpdateAddr, &mNet_UDP_ServerAddr, sizeof(struct sockaddr_in));
											//启动一个超时计时器
											mMasterLastAirDeviceID = 0;
											tempJson = cJSON_GetObjectItem(jsonRec, "devid");
											if(tempJson != NULL)
											{
												mMasterLastAirDeviceID = (int32_t)tempJson->valueint;
											}
											//默认只检查主模块
											//设置一下需要应答的网络信息
											if(shortAddr && mMasterLastAirDeviceID)
											{
												uint8_t rspSeq = 0;
												tempJson = cJSON_GetObjectItem(jsonRec, "seq");
												if(tempJson != NULL)
												{
													rspSeq = (uint8_t)tempJson->valueint;
												}
												pmMasterSerialPort->onAirDLCMD(shortAddr, 1, 0x0019, 0, rspSeq, sendBuff, 2, 0);
											}
										}
										else
										{
											if(DUALZIGBEECHIP)
											{
												memcpy(&mSlaveLastAirUpdateAddr,&mNet_UDP_ServerAddr,sizeof(struct sockaddr_in));
												//启动一个超时计时器
												mSlaveLastAirDeviceID = 0;
												tempJson = cJSON_GetObjectItem(jsonRec, "devid");
												if (tempJson != NULL) {
													mSlaveLastAirDeviceID = (int32_t) tempJson->valueint;
												}
												//默认只检查主模块
												//设置一下需要应答的网络信息
												if (shortAddr && mSlaveLastAirDeviceID) {
													uint8_t rspSeq = 0;
													tempJson = cJSON_GetObjectItem(jsonRec, "seq");
													if (tempJson != NULL) {
														rspSeq = (uint8_t) tempJson->valueint;
													}
													pmSlaveSerialPort->onAirDLCMD(shortAddr, 1,0x0019, 0, rspSeq,sendBuff, 2, 0);
												}
											}
										}
									}
								}
								else if(tempJson->valueint == 15)
								{
									tempJson = cJSON_GetObjectItem(jsonRec, "shortaddr");
									if(tempJson != NULL)
									{
										//默认只检查主模块
										//Next Image Response
										uint32_t tempShortAddr = (uint32_t)tempJson->valueint;
										bool ismaster = TRUE;
										tempJson = cJSON_GetObjectItem(jsonRec, "ismaster");
										if(tempJson != NULL)
										{
											if(tempJson->valueint == 0)
											{
												ismaster = false;
											}
										}
										uint32_t tempVer = 0;
										uint32_t tempMaxCnt = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "ver");
										if(tempJson != NULL)
										{
											tempVer = (uint32_t)tempJson->valueint;
										}
										tempJson = cJSON_GetObjectItem(jsonRec, "size");
										if(tempJson != NULL)
										{
											tempMaxCnt = (uint32_t)tempJson->valueint;
										}
										uint8_t rspSeq = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "seq");
										if(tempJson != NULL)
										{
											rspSeq = (uint8_t)tempJson->valueint;
										}
										uint16_t image = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "image");
										if(tempJson != NULL)
										{
											image = (uint16_t)tempJson->valueint;
										}
										TypeChar *tempSendBuff = new TypeChar(13);
										tempSendBuff->ubuff[0] = 0;
										tempSendBuff->onAddInt16Ex(1, ZCL_MANUSPCIFICID);
										tempSendBuff->onAddInt16Ex(3, image);
										tempSendBuff->onAddInt32Ex(5, tempVer);
										tempSendBuff->onAddInt32Ex(9, tempMaxCnt);
										if(ismaster)
										{
											pmMasterSerialPort->onAirDLCMD(tempShortAddr, 1, 0x0019, 2, rspSeq, tempSendBuff->ubuff, 13, 0);
										}
										else
										{
											if(DUALZIGBEECHIP) {
												pmSlaveSerialPort->onAirDLCMD(tempShortAddr, 1,0x0019, 2, rspSeq,tempSendBuff->ubuff,13, 0);
											}
										}
										delete tempSendBuff;
									}
								}
								else if(tempJson->valueint == 17)
								{
									//这个由上位机发送直接写命令
									tempJson = cJSON_GetObjectItem(jsonRec, "shortaddr");
									if(tempJson != NULL)
									{
										//得到偏移 最大值 数据 短地址
										//默认只检查主模块
										uint32_t shortAddr = (uint32_t)tempJson->valueint;
										bool ismaster = TRUE;
										tempJson = cJSON_GetObjectItem(jsonRec, "ismaster");
										if(tempJson != NULL)
										{
											if(tempJson->valueint == 0)
											{
												ismaster = false;
											}
										}
										uint32_t tempVer = 0;
										uint32_t tempMaxCnt = 0;
										uint32_t offset = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "ver");
										if(tempJson != NULL)
										{
											tempVer = (uint32_t)tempJson->valueint;
										}
										tempJson = cJSON_GetObjectItem(jsonRec, "maxcnt");
										if(tempJson != NULL)
										{
											tempMaxCnt = (uint32_t)tempJson->valueint;
										}
										uint16_t image = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "image");
										if(tempJson != NULL)
										{
											image = (uint16_t)tempJson->valueint;
										}
										uint8_t rspSeq = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "seq");
										if(tempJson != NULL)
										{
											rspSeq = (uint8_t)tempJson->valueint;
										}
										tempJson = cJSON_GetObjectItem(jsonRec, "offset");
										if(tempJson != NULL)
										{
											offset = (uint32_t)tempJson->valueint;
										}
										//mPrintf(LOG_Robot, "0019 rec offset=%d max=%d ", offset, tempMaxCnt);
										TypeChar *tempSendBuff = new TypeChar(13 + tempMaxCnt + 1);
										tempSendBuff->ubuff[0] = 0;
										tempSendBuff->onAddInt16Ex(1, ZCL_MANUSPCIFICID);
										tempSendBuff->onAddInt16Ex(3, image);
										tempSendBuff->onAddInt32Ex(5, tempVer);
										tempSendBuff->onAddInt32Ex(9, offset);
										tempSendBuff->ubuff[13] = (uint8_t)tempMaxCnt;
										tempJson = cJSON_GetObjectItem(jsonRec, "str");
										if(tempJson != NULL)
										{
											uint8_t tempChar = 0;
											for(uint32_t i = 0; i < tempMaxCnt; ++ i)
											{
												tempChar = (uint8_t)(mfHexToChar((uint8_t)tempJson->valuestring[i * 3]) & 0x0F);
												tempChar <<= 4;
												tempChar |= (uint8_t)(mfHexToChar((uint8_t)tempJson->valuestring[i * 3 + 1]) & 0x0F);
												tempSendBuff->ubuff[14 + i] = tempChar;
											}
										}
										if(ismaster)
										{
											pmMasterSerialPort->onAirDLCMD(shortAddr, 1, 0x0019, 5, rspSeq, tempSendBuff->ubuff, (uint8_t)(tempMaxCnt + 14), 0);
										}
										else
										{
											if(DUALZIGBEECHIP) {
												pmSlaveSerialPort->onAirDLCMD(shortAddr, 1, 0x0019, 5, rspSeq, tempSendBuff->ubuff, (uint8_t)(tempMaxCnt + 14), 0);
											}
										}
										delete tempSendBuff;
									}
								}
								else if(tempJson->valueint == 19)
								{
									//升级结束应答
									tempJson = cJSON_GetObjectItem(jsonRec, "shortaddr");
									if(tempJson != NULL)
									{
										//默认只检查主模块
										//Next Image Response
										uint32_t tempShortAddr = (uint32_t)tempJson->valueint;
										bool ismaster = TRUE;
										tempJson = cJSON_GetObjectItem(jsonRec, "ismaster");
										if(tempJson != NULL)
										{
											if(tempJson->valueint == 0)
											{
												ismaster = false;
											}
										}
										uint32_t tempVer = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "ver");
										if(tempJson != NULL)
										{
											tempVer = (uint32_t)tempJson->valueint;
										}
										uint8_t rspSeq = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "seq");
										if(tempJson != NULL)
										{
											rspSeq = (uint8_t)tempJson->valueint;
										}
										uint16_t image = 0;
										tempJson = cJSON_GetObjectItem(jsonRec, "image");
										if(tempJson != NULL)
										{
											image = (uint16_t)tempJson->valueint;
										}
										TypeChar *tempSendBuff = new TypeChar(16);
										tempSendBuff->onAddInt16Ex(0, ZCL_MANUSPCIFICID);
										tempSendBuff->onAddInt16Ex(2, image);
										tempSendBuff->onAddInt32Ex(4, tempVer);
										tempSendBuff->onAddInt32Ex(8, 200);
										tempSendBuff->onAddInt32Ex(12, 10);
										memcpy(&mAirUpdateAddr, &mNet_UDP_ServerAddr, sizeof(struct sockaddr_in));
										onTimerAdd(TIMER_AIR_TIMEOUT, 6000, false, onUDPDevVerTimerOutCB, 0, 0);
										if(ismaster)
										{
											memset(&mMasterLastAirUpdateAddr, 0, sizeof(struct sockaddr_in));
											mMasterLastAirDeviceID = 0;
											pmMasterSerialPort->onAirDLCMD(tempShortAddr, 1, 0x0019, 7, rspSeq, tempSendBuff->ubuff, 16, 0);
										}
										else
										{
											if(DUALZIGBEECHIP) {
												memset(&mSlaveLastAirUpdateAddr, 0, sizeof(struct sockaddr_in));
												mSlaveLastAirDeviceID = 0;
												pmSlaveSerialPort->onAirDLCMD(tempShortAddr, 1, 0x0019, 7, rspSeq, tempSendBuff->ubuff, 16, 0);
											}
										}
										delete tempSendBuff;
									}
								}
							}
						}
						else if(cJSON_GetObjectItem(jsonRec, "type"))
						{
							tempJson = cJSON_GetObjectItem(jsonRec, "type");
							if(tempJson != NULL)
							{
								if(memcmp(tempJson->valuestring, "REQUEST_TCP", strlen("REQUEST_TCP")) == 0)
								{
									//相等
									tempJson = cJSON_GetObjectItem(jsonRec, "sn");
									if(tempJson != NULL)
									{
										//判断一下当前家庭有没有这个小白
										if(pDeviceList->onFindApplianceInfoTypeAndSerial(APPLIANCE_TYPE_ROBOT_GOWILD, tempJson->valuestring))
										{
											cJSON *snJson = cJSON_CreateObject();
											cJSON *jsonArray = cJSON_CreateObject();
											cJSON_AddStringToObject(snJson, "type", "RESPONSE_TCP");
											cJSON_AddStringToObject(jsonArray, "ip", mfNetGetLocalIP());
											cJSON_AddNumberToObject(jsonArray, "port", (uint32_t) mCurrentPort);
											cJSON_AddStringToObject(jsonArray, "company", "hwellyi");
											cJSON_AddItemToObject(snJson, "data", jsonArray);

											char *retJsonChars = cJSON_Print(snJson);
											//发送出去
											if(mUDPClientfd != - 1)
											{
												int ret = sendto(mUDPClientfd, retJsonChars,
												                 strlen(retJsonChars), 0,
												                 (struct sockaddr *) (&mNet_UDP_ServerAddr),
												                 sizeof(struct sockaddr_in));
												if(ret != - 1)
												{
													mPrintf(LOG_Robot, "UDP Send:%s ", retJsonChars);
												}
												else
												{
													mPrintf(LOG_Robot, "UDP Send Time out! ");
												}
											}
											cJSON_Delete(snJson);
											free(retJsonChars);
										}
									}
									else
									{
										mPrintf(LOG_Robot, "UDP Error SN ");
									}
								}
								else
								{
									mPrintf(LOG_Robot, "UDP unkonw Type=%s ", jsonRec->valuestring);
								}
							}
							else
							{
								mPrintf(LOG_Robot, "UDP Error Type ");
							}
						}
					}
					cJSON_Delete(jsonRec);
				}
				break;
			case NetStatusClose:
				if(mUDPClientfd != -1)
				{
					//断开所有无效连接
					close(mUDPClientfd);
					mUDPClientfd = -1;
				}
				sleep(5);//延时5s再重新连接
				mNet_UDP_Status = NetStatusInit;
				break;
		}
	}
	if(mUDPClientfd != -1)
	{
		//断开所有无效连接
		close(mUDPClientfd);
		mUDPClientfd = -1;
	}
	mNet_UDP_Status = NetStatusClose;
	delete tempUDPReviceBuff;
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

void onUDPSend(int port, char *value)
{
	int sock = -1;
	if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) == -1)
	{
		return;
	}
	const int opt = 1;
	//设置该套接字为广播类型，
	int nb = 0;
	nb = setsockopt(sock, SOL_SOCKET, SO_BROADCAST, (char *)&opt, sizeof(opt));
	if(nb == -1)
	{
		return ;
	}
	struct sockaddr_in addrto;
	bzero(&addrto, sizeof(struct sockaddr_in));
	addrto.sin_family=AF_INET;
	addrto.sin_addr.s_addr = htonl(INADDR_BROADCAST);
	addrto.sin_port=htons(port);
	int nlen=sizeof(addrto);

	//从广播地址发送消息
	int ret=sendto(sock, value, strlen(value), 0, (sockaddr*)&addrto, nlen);
	if(ret<0)
	{
		mPrintf(LOG_Robot, "UDP Broadcast failed" );
	}
	else
	{
		mPrintf(LOG_Robot, "UDP Broadcast Send:%d->%s ", port, value);
	}
	close(sock);
}

static void * mfTCPServerThread(void * arg)
{
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	int mTcpClientfd = -1;
	struct sockaddr_in addrServer;
	int mNet_Tcp_Status = NetStatusInit;
	//int len = (int)sizeof(struct sockaddr_in);
	//创建一个UDP服务器
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	while(mIsExitFlag)
	{
		switch((char)mNet_Tcp_Status)
		{
			case NetStatusInit:
				mTcpClientfd = socket(AF_INET, SOCK_STREAM, 0);
				if(mTcpClientfd == -1)
				{
					mPrintf(LOG_Robot, "socket() failure! ");
					mNet_Tcp_Status = NetStatusClose;
				}
				else
				{
					tempThreadInfo->pNetFD = &mTcpClientfd;
					//mNet_UDP_Status = NetStatusConnect;
					/* init servaddr */
					bzero(&addrServer, sizeof(addrServer));
					addrServer.sin_family = AF_INET;
					addrServer.sin_addr.s_addr = htonl(INADDR_ANY); //#define INADDR_ANY   ((unsigned long int) 0x00000000)
					addrServer.sin_port = htons(mCurrentPort);
					if(bind(mTcpClientfd, (struct sockaddr *)&addrServer, sizeof(addrServer)) == -1)
					{
						mPrintf(LOG_Robot,"tcp bind() failure! ");
						mNet_Tcp_Status = NetStatusClose;
						mTcpClientfd = -1;
					}
					else
					{
						mPrintf(LOG_Robot,"tcp bind() success! currentPort = %d ", mCurrentPort);
						mNet_Tcp_Status = NetStatusRec;
					}
				}
				break;
			case NetStatusRec://接收数据
			{
				//监听
				listen(mTcpClientfd, 5);
				int connectfd = -1;
				struct sockaddr_in client;
				socklen_t addrlen = 0;
				if((connectfd=accept(mTcpClientfd,(struct sockaddr *)&client, &addrlen))==-1)
				{
					mPrintf(LOG_Robot,"tcp recive error! ");
					mNet_Tcp_Status = NetStatusClose;
				}
				else
				{
					//创建一个线程用于数据通信
					TypeChar * tempPara = new TypeChar(32);
					sprintf(tempPara->buff, "%d", connectfd);
					//查找一下 如果有就不再创建
					//if(onFindThreadTitle((char *)"Log C") == FALSE)
					{
						onAddThread("Log C", mfTCPConnectThread, tempPara->buff);//开启一个TCP连接线程
					}
					delete tempPara;
				}
			}
			break;
			case NetStatusClose:
				if(mTcpClientfd != -1)
				{
					//断开所有无效连接
					close(mTcpClientfd);
					mTcpClientfd = -1;
				}
				sleep(5);//延时5s再重新连接
				mCurrentPort++;
				if(mCurrentPort > 9999)
				{
					mCurrentPort = 8899;
				}
				mNet_Tcp_Status = NetStatusInit;
				break;
		}
	}
	if(mTcpClientfd != -1)
	{
		//断开所有无效连接
		close(mTcpClientfd);
		mTcpClientfd = -1;
	}
	mNet_Tcp_Status = NetStatusClose;
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

static const char *onGetDeviceType(int devicetype)
{
	switch(devicetype)
	{
		case SUB_DEVICE_TYPE_LIGHT:return "LIGHT";
		case SUB_DEVICE_TYPE_SWITCH:return "SOCKET";
		case SUB_DEVICE_TYPE_DIMMER:return "LIGHT_COLOR_TEMP";
		case SUB_DEVICE_TYPE_CURTAIN:return "CURTAIN";
		//case TYPE_DEVICE_SUB_DOOR_WINDOW:return "DOOR";
		default:
			break;
	}
	return NULL;
}

bool onCheckNetPrint()
{
	if(mNetLogClientfd != -1)
	{
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

bool onNetPrint(int TAG, const char *fmt, va_list *arg)
{
	if(mNetLogClientfd != -1)
	{
		static bool isSending = FALSE;
		TypeChar *tempLog = new TypeChar(512);
		vsprintf(tempLog->buff, fmt, *arg);
		int tempSendLen = strlen(tempLog->buff);
		if(tempSendLen > 500) tempSendLen = 500;
		TypeRobotDataInfo *tempSendInfo = new TypeRobotDataInfo(0x6801 + TAG, tempLog->buff, tempSendLen);
		mNetLogLinkList->add(tempSendInfo);
		delete tempLog;
		if(isSending == FALSE)
		{
			isSending = TRUE;
			while(mNetLogLinkList->onGetCount())//避免数据穿插出错
			{
				tempSendInfo = (TypeRobotDataInfo *)mNetLogLinkList->get();
				if(tempSendInfo)
				{
					int ret = send(mNetLogClientfd, tempSendInfo->dataBuff->buff, (size_t)(tempSendLen + 8 + 1), 0);
					if((ret == 0) || (mNetLogClientfd == -1))
					{
						//关闭连接
						mNetLogClientfd = -1;
						mNetLogLinkList->clear();
						delete tempSendInfo;
						break;
					}
				}
				delete tempSendInfo;
			}
			isSending = FALSE;
		}
		else
		{
			mNetLogLinkList->clear();
		}
	}
	return true;
}

static void * mfTCPConnectThread(void * arg)
{
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	int connectfd = atoi(tempThreadInfo->threadPara->buff);
	tempThreadInfo->pNetFD = &connectfd;
	bool flag = true;
	bool isLogPrint = FALSE;//判断是否是打印的连接
	int retLen = 0;
	int ret = 0;
	int tempSendLen = 0;
	mIsUpdateRobotFlag = false;
	mPrintf(LOG_Robot, "创建新的连接:%d ", connectfd);
	TypeChar *tempTcpReviceBuff = new TypeChar(1024);
	while(flag && mIsExitFlag)
	{
		tempTcpReviceBuff->onClear();
		retLen = recv(connectfd, tempTcpReviceBuff->buff, 1024, 0);
		if((retLen == 0) || (connectfd == -1))
		{
			flag = false;
		}
		else if(retLen > 0)
		{
			if((isLogPrint == FALSE) && mIsUpdateRobotFlag)
			{
				mPrintf(LOG_Robot, "need update device Info. = %d closed! ", connectfd);
				flag = false;
			}
			//收到 数据
			TypeRobotDataInfo *tempRobotInfo = new TypeRobotDataInfo(tempTcpReviceBuff->ubuff, retLen);
			if(tempRobotInfo->cmdID != 0x0001)
			{
				mPrintf(LOG_Robot, "收到命令=%d value=%s ", tempRobotInfo->cmdID, tempRobotInfo->dataBuff->buff);
			}
			if(tempRobotInfo->cmdID != 0)
			{
				switch(tempRobotInfo->cmdID)
				{
					case 0x6800://netLog 设置
					{
						isLogPrint = TRUE;
						mNetLogClientfd = connectfd;
						//修改一下线程名称
						//结束所有叫"LOG C LOG" 的线程
						for(int i = 0; i < mThreadInfoList->size(); ++ i)
						{
							TypeThreadInfo *threadInfo = (TypeThreadInfo *)mThreadInfoList->get(i);
							if(threadInfo && threadInfo->title->onStringCMP("LOG C LOG"))
							{
								threadInfo->onCloseReq();
							}
						}
						TypeChar *deleteChars = tempThreadInfo->title;
						tempThreadInfo->title = new TypeChar("LOG C LOG");
						delete deleteChars;
						mPrintf(LOG_Robot, "参数设置成功!=%d ", connectfd);
					}
						break;
					case 0x0001://心跳请求
					{
						cJSON *json001 = cJSON_CreateObject();
						cJSON_AddStringToObject(json001, "type", "RESPONSE_HEART_BEAT");
						char *retJsonChars = cJSON_Print(json001);
						int len = strlen(retJsonChars);
						mPrintf(LOG_Robot, "TCP Send:%s len=%d", retJsonChars, len);
						TypeRobotDataInfo *sendCMD = new TypeRobotDataInfo(0x0002, retJsonChars, len);
						ret = send(connectfd, sendCMD->dataBuff->buff, (size_t)(len + 6), 0);
						if(ret == 0)
						{
							mPrintf(LOG_Robot, "send error!disconnect!=%d ", connectfd);
							flag = false;
						}
						cJSON_Delete(json001);
						delete sendCMD;
						free(retJsonChars);
					}
						break;
					case 0x0003://发送控制命令
					{
						int sendCode = 0;
						TypeChar *sendVoice = NULL;
						cJSON *json003 = cJSON_Parse(tempRobotInfo->dataBuff->buff);
						if(!json003)
						{
							mPrintf(LOG_Robot, "Err Json:[%s] ", cJSON_GetErrorPtr());
						}
						else
						{
							cJSON *tempDataJson = cJSON_GetObjectItem(json003, "data");
							if(tempDataJson != NULL)
							{
								cJSON *tempJson;
								for(int i = 0; i < cJSON_GetArraySize(tempDataJson); ++i)
								{
									int32_t keyID = 0;
									int32_t roomID = 0;
									cJSON *subJson = cJSON_GetArrayItem(tempDataJson, i);
									if(subJson != NULL)
									{
										tempJson = cJSON_GetObjectItem(subJson, "id");
										if(tempJson != NULL)
										{
											keyID = mfPublicGetInt32(tempJson->valuestring);
										}
										tempJson = cJSON_GetObjectItem(subJson, "room");
										if(tempJson != NULL)
										{
											//roomID = mfPublicGetUInt32(tempJson->valuestring);
										}
										tempJson = cJSON_GetObjectItem(subJson, "name");
										if(tempJson != NULL)
										{
											if(strlen(tempJson->valuestring) > 3)
											{
												//执行设备
												int subKeyID = keyID >> 8;
												int subID = keyID & 0xFF;
												TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, subKeyID);
												TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
												if(tempDBDeviceInfo)
												{
													tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(subID);
												}
												if(tempDeviceTypeInfo != NULL)
												{
													switch(tempDeviceTypeInfo->devType)
													{
														case SUB_DEVICE_TYPE_LIGHT:
														{
															tempJson = cJSON_GetObjectItem(subJson, "state");
															if(tempJson != NULL)
															{
																if(memcmp(tempJson->valuestring, "STATE_ON", strlen("STATE_ON")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 1, TRUE);
																}
																else if(memcmp(tempJson->valuestring, "STATE_OFF", strlen("STATE_OFF")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 0, TRUE);
																}
																else
																{
																	sendCode = 2;
																	sendVoice = new TypeChar((char *)"灯光目前不支持这个操作哦");
																}
															}
														}
															break;
														case SUB_DEVICE_TYPE_SWITCH:
														{
															tempJson = cJSON_GetObjectItem(subJson, "state");
															if(tempJson != NULL)
															{
																if(memcmp(tempJson->valuestring, "STATE_ON", strlen("STATE_ON")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 1, TRUE);
																}
																else if(memcmp(tempJson->valuestring, "STATE_OFF", strlen("STATE_OFF")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 0, TRUE);
																}
																else
																{
																	sendCode = 2;
																	sendVoice = new TypeChar((char *)"智能插座目前不支持这个操作哦!");
																}
															}
														}
															break;
														case SUB_DEVICE_TYPE_DIMMER:
														{
															tempJson = cJSON_GetObjectItem(subJson, "state");
															if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
															{
																if(memcmp(tempJson->valuestring, "STATE_ON", strlen("STATE_ON")) == 0)
																{
																	if(tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->status > 0)
																	{
																		sendCode = 2;
																		sendVoice = new TypeChar((char *)"灯还是开着的,试试调整亮度!");
																	}
																	else
																	{
																		pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 501, TRUE);
																	}
																}
																else if(memcmp(tempJson->valuestring, "STATE_OFF", strlen("STATE_OFF")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 0, TRUE);
																}
																else
																{
																	sendCode = 2;
																	sendVoice = new TypeChar((char *)"灯光目前不支持这个操作哦!");
																}
															}
															else
															{
																tempJson = cJSON_GetObjectItem(subJson, "action");
																if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																{
																	if(memcmp(tempJson->valuestring, "ACTION_TO", strlen("ACTION_TO")) == 0)
																	{
																		tempJson = cJSON_GetObjectItem(subJson, "attributeValue");
																		if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																		{
																			if(memcmp(tempJson->valuestring, "VALUE_MAX", strlen("VALUE_MAX")) == 0)
																			{
																				pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 100, TRUE);
																			}
																			else if(memcmp(tempJson->valuestring, "VALUE_MIN", strlen("VALUE_MIN")) == 0)
																			{
																				pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 30, TRUE);
																			}
																			else if(memcmp(tempJson->valuestring, "0.5", strlen("0.5")) == 0)
																			{
																				pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 50, TRUE);
																			}
																			else
																			{
																				int toValue = onGetNumWithString(tempJson->valuestring);
																				if(toValue >= 0)
																				{
																					if(toValue >= 100) toValue = 100;
																					if(toValue <= 0) toValue = 0;
																					pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, toValue, TRUE);
																				}
																				else
																				{
																					sendCode = 2;
																					sendVoice = new TypeChar((char *)"灯光目前不支持这个操作哦!");
																				}
																			}
																		}
																	}
																	else if(memcmp(tempJson->valuestring, "ACTION_ADD", strlen("ACTION_ADD")) == 0)
																	{
																		tempJson = cJSON_GetObjectItem(subJson, "attributeValue");
																		int sendValue = tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->status;
																		if(sendValue >= 100)
																		{
																			sendCode = 0;
																			sendVoice = new TypeChar((char *)"亮度已经是最大的啦");
																		}
																		else
																		{
																			if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																			{
																				int toValue = onGetNumWithString(tempJson->valuestring);
																				if(toValue >= 0)
																				{
																					sendValue += toValue;
																				}
																				else
																				{
																					sendValue += 30;
																				}
																			}
																			if(sendValue > 100) sendValue = 100;
																			pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, sendValue, TRUE);
																		}
																	}
																	else if(memcmp(tempJson->valuestring, "ACTION_REDUCE", strlen("ACTION_REDUCE")) == 0)
																	{
																		tempJson = cJSON_GetObjectItem(subJson, "attributeValue");
																		int sendValue = tempDeviceTypeInfo->onGetSubInfo()->dimmingStatus->status;
																		if(sendValue <= 0)
																		{
																			sendCode = 0;
																			sendVoice = new TypeChar((char *)"亮度不能再小啦");
																		}
																		else
																		{
																			if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																			{
																				int toValue = onGetNumWithString(tempJson->valuestring);
																				if(toValue >= 0)
																				{
																					sendValue -= toValue;
																				}
																				else
																				{
																					sendValue -= 30;
																				}
																			}
																			if(sendValue < 0) sendValue = 0;
																			pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, sendValue, TRUE);
																		}
																	}
																}
															}
														}
															break;
														case SUB_DEVICE_TYPE_CURTAIN:
															tempJson = cJSON_GetObjectItem(subJson, "state");
															if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
															{
																if(memcmp(tempJson->valuestring, "STATE_ON", strlen("STATE_ON")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 100, TRUE);
																}
																else if(memcmp(tempJson->valuestring, "STATE_OFF", strlen("STATE_OFF")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 0, TRUE);
																}
																else if(memcmp(tempJson->valuestring, "STATE_STOP", strlen("STATE_STOP")) == 0)
																{
																	pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 501, TRUE);
																}
																else
																{
																	sendCode = 2;
																	sendVoice = new TypeChar((char *)"窗帘目前不支持这个操作哦!");
																}
															}
															else
															{
																tempJson = cJSON_GetObjectItem(subJson, "action");
																if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																{
																	if(memcmp(tempJson->valuestring, "ACTION_TO", strlen("ACTION_TO")) == 0)
																	{
																		tempJson = cJSON_GetObjectItem(subJson, "attributeValue");
																		if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																		{
																			if(memcmp(tempJson->valuestring, "VALUE_MAX", strlen("VALUE_MAX")) == 0)
																			{
																				pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 100, TRUE);
																			}
																			else if(memcmp(tempJson->valuestring, "VALUE_MIN", strlen("VALUE_MIN")) == 0)
																			{
																				pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 0, TRUE);
																			}
																			else if(memcmp(tempJson->valuestring, "0.5", strlen("0.5")) == 0)
																			{
																				pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, 50, TRUE);
																			}
																			else
																			{
																				int toValue = onGetNumWithString(tempJson->valuestring);
																				if(toValue >= 0)
																				{
																					if(toValue >= 100) toValue = 100;
																					if(toValue <= 0) toValue = 0;
																					pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, toValue, TRUE);
																				}
																				else
																				{
																					sendCode = 2;
																					sendVoice = new TypeChar((char *)"窗帘目前不支持这个操作哦!");
																				}
																			}
																		}
																	}
																	else if(memcmp(tempJson->valuestring, "ACTION_ADD", strlen("ACTION_ADD")) == 0)
																	{
																		tempJson = cJSON_GetObjectItem(subJson, "attributeValue");
																		int sendValue = tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->status;
																		if(sendValue >= 100)
																		{
																			sendCode = 0;
																			sendVoice = new TypeChar((char *)"已经开到最大的啦");
																		}
																		else
																		{
																			if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																			{
																				int toValue = onGetNumWithString(tempJson->valuestring);
																				if(toValue >= 0)
																				{
																					sendValue += toValue;
																				}
																				else
																				{
																					sendValue += 30;
																				}
																			}
																			if(sendValue > 100) sendValue = 100;
																			pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, sendValue, TRUE);
																		}
																	}
																	else if(memcmp(tempJson->valuestring, "ACTION_REDUCE", strlen("ACTION_REDUCE")) == 0)
																	{
																		tempJson = cJSON_GetObjectItem(subJson, "attributeValue");
																		int sendValue = tempDeviceTypeInfo->onGetSubInfo()->curtainStatus->status;
																		if(sendValue <= 0)
																		{
																			sendCode = 0;
																			sendVoice = new TypeChar((char *)"不能再关啦");
																		}
																		else
																		{
																			if((tempJson != NULL) && (strlen(tempJson->valuestring) > 0))
																			{
																				int toValue = onGetNumWithString(tempJson->valuestring);
																				if(toValue >= 0)
																				{
																					sendValue -= toValue;
																				}
																				else
																				{
																					sendValue -= 30;
																				}
																			}
																			if(sendValue < 0) sendValue = 0;
																			pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, subID, sendValue, TRUE);
																		}
																	}
																}
															}
															break;
														default:
															break;
													}

												}
												else
												{
													sendCode = 1;
													sendVoice = new TypeChar((char *)"啊呀, 找不到这个设备。试试对我说更新设备列表!");
												}
											}
										}
										tempJson = cJSON_GetObjectItem(subJson, "scene");
										if(tempJson != NULL)
										{
											if(strlen(tempJson->valuestring) > 3)
											{
												//执行场景
												tempJson = cJSON_GetObjectItem(subJson, "state");
												if(tempJson != NULL)
												{
													TypeSceneNameInfo *tempSceneNameInfo = pDeviceList->onFindSceneInfo((int64_t)keyID);
													if(tempSceneNameInfo != NULL)
													{
														sendCode = 0;
														if(memcmp(tempJson->valuestring, "STATE_ON", strlen("STATE_ON")) == 0)
														{
															pDeviceList->onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
														}
														else if(memcmp(tempJson->valuestring, "STATE_OFF", strlen("STATE_OFF")) == 0)
														{
															pDeviceList->onSetSceneStatus(tempSceneNameInfo, 0, TRUE);
														}
														else
														{
															pDeviceList->onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
														}
													}
													else
													{
														sendCode = 1;
														sendVoice = new TypeChar((char *)"啊呀, 找不到这个场景。试试对我说更新设备列表!");
													}
												}
											}
										}
									}
								}
								//在这应答一下

								cJSON *sendResjson = cJSON_CreateObject();
								cJSON *sendResubArray = cJSON_CreateArray();
								cJSON_AddStringToObject(sendResjson, "type", "RESPONSE_CONTROL");
								if(sendVoice != NULL)
								{
									cJSON_AddStringToObject(sendResjson, "msg", sendVoice->buff);
									cJSON_AddStringToObject(sendResjson, "msgVoice", sendVoice->buff);
									cJSON_AddNumberToObject(sendResjson, "code", 7);
								}
								else
								{
									cJSON_AddNumberToObject(sendResjson, "code", (uint32_t)sendCode);
								}

								cJSON_AddItemToObject(sendResjson, "data", sendResubArray);
								char *retJsonChars = cJSON_Print(sendResjson);
								tempSendLen = strlen(retJsonChars);
								TypeRobotDataInfo *tempSendInfo = new TypeRobotDataInfo(0x0004, retJsonChars, tempSendLen);
								mPrintf(LOG_Robot, "TCP Send:%s ", retJsonChars);
								ret = send(connectfd, tempSendInfo->dataBuff->buff, (size_t)(tempSendLen + 6), 0);
								if(ret == 0)
								{
									mPrintf(LOG_Robot, "send error!disconnect!=%d ", connectfd);
									flag = false;
								}
								cJSON_Delete(sendResjson);
								free(retJsonChars);
								if(sendVoice != NULL)
								{
									delete sendVoice;
								}
								delete tempSendInfo;

							}
						}
						cJSON_Delete(json003);
					}
						break;
					case 0x0005://请求设备列表
					{
						cJSON *json005 = cJSON_CreateObject();
						cJSON *subArray = cJSON_CreateArray();
						TypeChar *tempChars = new TypeChar();
						cJSON_AddStringToObject(json005, "type", "RESPONSE_DEVICE");

						TypeDBDeviceInfo *dbDeviceInfo = NULL;
						TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
						for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); i++)
						{
							dbDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
							if(dbDeviceInfo)
							{
								for(int j = 1; j <= dbDeviceInfo->subCount; ++j)
								{
									tempDeviceTypeInfo = dbDeviceInfo->onGetSubInfo(j);
									if((tempDeviceTypeInfo != NULL) && (onGetDeviceType(tempDeviceTypeInfo->devType) != NULL))
									{
										cJSON *subDeviceJSON = cJSON_CreateObject();
										sprintf(tempChars->buff, "%d", (tempDeviceTypeInfo->deviceID << 8) + tempDeviceTypeInfo->subID);
										cJSON_AddStringToObject(subDeviceJSON, "id", tempChars->buff);
										cJSON_AddStringToObject(subDeviceJSON, "name", tempDeviceTypeInfo->name->buff);
										cJSON_AddStringToObject(subDeviceJSON, "type", (char *)onGetDeviceType(tempDeviceTypeInfo->devType));
										sprintf(tempChars->buff, "%d", 0);
										cJSON_AddStringToObject(subDeviceJSON, "state", tempChars->buff);
										sprintf(tempChars->buff, "%d", tempDeviceTypeInfo->roomID);
										cJSON_AddStringToObject(subDeviceJSON, "roomId", tempChars->buff);
										cJSON_AddStringToObject(subDeviceJSON, "floorId", "");
										cJSON_AddItemToArray(subArray, subDeviceJSON);
									}
								}
							}
						}
						cJSON_AddItemToObject(json005, "data", subArray);
						char *retJsonChars = cJSON_Print(json005);
						tempSendLen = strlen(retJsonChars);
						TypeRobotDataInfo *tempSendInfo = new TypeRobotDataInfo(0x0006, retJsonChars, tempSendLen);
						mPrintf(LOG_Robot, "TCP Send:%s ", retJsonChars);
						ret = send(connectfd, tempSendInfo->dataBuff->buff, (size_t)(tempSendLen + 6), 0);
						if(ret == 0)
						{
							mPrintf(LOG_Robot, "send error!disconnect!=%d ", connectfd);
							flag = false;
						}
						delete tempChars;
						cJSON_Delete(json005);
						free(retJsonChars);
						delete tempSendInfo;
					}
						break;
					case 0x0007://请求场景列表
					{
						cJSON *json007 = cJSON_CreateObject();
						cJSON *subArray = cJSON_CreateArray();
						TypeChar *tempChars = new TypeChar();
						cJSON_AddStringToObject(json007, "type", "RESPONSE_SCENE");

						TypeSceneNameInfo *tempSceneNameInfo = NULL;
						for(int i = 0; i < pDeviceList->sceneList->size(); i++)
						{
							tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
							cJSON *subDeviceJSON = cJSON_CreateObject();
							sprintf(tempChars->buff, "%lld", tempSceneNameInfo->scene_id);
							cJSON_AddStringToObject(subDeviceJSON, "id", tempChars->buff);
							cJSON_AddStringToObject(subDeviceJSON, "name", tempSceneNameInfo->name->buff);
							cJSON_AddItemToArray(subArray, subDeviceJSON);
						}
						cJSON_AddItemToObject(json007, "data", subArray);
						char *retJsonChars = cJSON_Print(json007);
						tempSendLen = strlen(retJsonChars);
						TypeRobotDataInfo *tempSendInfo = new TypeRobotDataInfo(0x0008, retJsonChars, tempSendLen);
						//mPrintf(LOG_Robot, "TCP Send:%s ", retJsonChars);
						ret = send(connectfd, tempSendInfo->dataBuff->buff, (size_t)(tempSendLen + 6), 0);
						if(ret == 0)
						{
							mPrintf(LOG_Robot, "send error!disconnect!=%d ", connectfd);
							flag = false;
						}
						delete tempChars;
						cJSON_Delete(json007);
						free(retJsonChars);
						delete tempSendInfo;
					}
						break;
					case 0x0009://请求房间列表
					{
						cJSON *json009 = cJSON_CreateObject();
						cJSON *subArray = cJSON_CreateArray();
						TypeChar *tempChars = new TypeChar();
						cJSON_AddStringToObject(json009, "type", "RESPONSE_ROOM");

						TypeRoomInfo *tempRoomInfo = NULL;
						for(int i = 0; i < pDeviceList->roomList->size(); i++)
						{
							tempRoomInfo = (TypeRoomInfo *)pDeviceList->roomList->get(i);
							cJSON *subDeviceJSON = cJSON_CreateObject();
							sprintf(tempChars->buff, "%d", tempRoomInfo->room_id);
							cJSON_AddStringToObject(subDeviceJSON, "id", tempChars->buff);
							cJSON_AddStringToObject(subDeviceJSON, "name", tempRoomInfo->name->buff);
							cJSON_AddItemToArray(subArray, subDeviceJSON);
						}
						cJSON_AddItemToObject(json009, "data", subArray);
						char *retJsonChars = cJSON_Print(json009);
						tempSendLen = strlen(retJsonChars);
						TypeRobotDataInfo *tempSendInfo = new TypeRobotDataInfo(0x000A, retJsonChars, tempSendLen);
						//mPrintf(LOG_Robot, "TCP Send:%s ", retJsonChars);
						ret = send(connectfd, tempSendInfo->dataBuff->buff, (size_t)(tempSendLen + 6), 0);
						if(ret == 0)
						{
							mPrintf(LOG_Robot, "send error!disconnect!=%d ", connectfd);
							flag = false;
						}
						delete tempChars;
						cJSON_Delete(json009);
						free(retJsonChars);
						delete tempSendInfo;
					}
						break;
					case 0x000B://请求楼层列表
					{
						cJSON *json00B = cJSON_CreateObject();
						cJSON *subArray = cJSON_CreateArray();
						cJSON_AddStringToObject(json00B, "type", "RESPONSE_FLOOR");
						cJSON_AddItemToObject(json00B, "data", subArray);
						char *retJsonChars = cJSON_Print(json00B);
						tempSendLen = strlen(retJsonChars);
						TypeRobotDataInfo *tempSendInfo = new TypeRobotDataInfo(0x000C, retJsonChars, tempSendLen);
						//mPrintf(LOG_Robot, "TCP Send:%s ", retJsonChars);
						ret = send(connectfd, tempSendInfo->dataBuff->buff, (size_t)(tempSendLen + 6), 0);
						if(ret == 0)
						{
							mPrintf(LOG_Robot, "send error!disconnect!=%d ", connectfd);
							flag = false;
						}
						cJSON_Delete(json00B);
						free(retJsonChars);
						delete tempSendInfo;
					}
						break;
					case 0x000D://预留
						break;
					default:
						break;
				}
			}
			delete tempRobotInfo;
		}
	}
	if(isLogPrint)
	{
		if(mNetLogClientfd == connectfd)
		{
			mNetLogClientfd = -1;
		}
	}
	if(connectfd != -1)
	{
		close(connectfd);
	}
	mPrintf(Log_NetWork, "%s out ", tempThreadInfo->title->buff);
	delete tempTcpReviceBuff;
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

static int onGetNumWithString(char * str)
{
	int retInt = -1;
	//先判断一下是不是纯数字
	int tempNumLen = strlen(str);
	bool isNumFlag = false;
	for(int m = 0; m < tempNumLen; ++m)
	{
		if((str[m] >= '0') && (str[m] <= '9'))
		{
			isNumFlag = true;
		}
		else
		{
			isNumFlag = false;
			break;
		}
	}
	if(isNumFlag)
	{
		retInt = atoi(str);
	}
	return retInt;
}

bool onUDPSetSceneInfo(int64_t sceneid, int32_t status)
{
	if(pDeviceList && !onGetConnectFlag())
	{
		cJSON *tempJson = cJSON_CreateObject();
		cJSON_AddLongNumberToObject(tempJson, "udp_type", 101);//
		cJSON_AddNumberToObject(tempJson, "udp_status", (uint32_t)onGetConnectFlag());
		cJSON_AddLongNumberToObject(tempJson, "udp_familyid", (uint64_t)pDataBase->onGetFamilyID());
		cJSON_AddNumberToObject(tempJson, "udp_gatewayid", (uint32_t)pDataBase->onGetGateway_ID());
		cJSON_AddLongNumberToObject(tempJson, "udp_scene_id", (uint64_t)sceneid);
		cJSON_AddNumberToObject(tempJson, "udp_scene_status", (uint32_t)status);
		char *retJsonChars = cJSON_Print(tempJson);
		onUDPSend(6666, retJsonChars);
		cJSON_Delete(tempJson);
		free(retJsonChars);
	}
	return TRUE;
}

bool onUDPBroadcastDeviceStatus(int32_t keyid, int32_t subid, int32_t subtype, int32_t status)
{
	if(pDeviceList && !onGetConnectFlag())
	{
		cJSON *tempJson = cJSON_CreateObject();
		cJSON_AddLongNumberToObject(tempJson, "udp_type", 106);//
		cJSON_AddNumberToObject(tempJson, "udp_status", (uint32_t)onGetConnectFlag());
		cJSON_AddLongNumberToObject(tempJson, "udp_familyid", (uint64_t)pDataBase->onGetFamilyID());
		cJSON_AddNumberToObject(tempJson, "udp_gatewayid", (uint32_t)pDataBase->onGetGateway_ID());
		cJSON_AddNumberToObject(tempJson, "udp_keyid", (uint32_t)keyid);
		cJSON_AddNumberToObject(tempJson, "udp_subid", (uint32_t)subid);
		cJSON_AddNumberToObject(tempJson, "udp_subtype", (uint32_t)subtype);
		cJSON_AddNumberToObject(tempJson, "udp_devstatus", (uint32_t)status);
		char *retJsonChars = cJSON_Print(tempJson);
		onUDPSend(6666, retJsonChars);
		cJSON_Delete(tempJson);
		free(retJsonChars);
	}
	return TRUE;
}

bool onUDPDisAlarmInfo(int32_t keyid, int32_t subid, int32_t subtype, int32_t alarmtype)
{
	if(pDeviceList && !onGetConnectFlag())
	{
		cJSON *tempJson = cJSON_CreateObject();
		cJSON_AddLongNumberToObject(tempJson, "udp_type", 105);//
		cJSON_AddNumberToObject(tempJson, "udp_status", (uint32_t)onGetConnectFlag());
		cJSON_AddLongNumberToObject(tempJson, "udp_familyid", (uint64_t)pDataBase->onGetFamilyID());
		cJSON_AddNumberToObject(tempJson, "udp_gatewayid", (uint32_t)pDataBase->onGetGateway_ID());
		cJSON_AddNumberToObject(tempJson, "udp_alarm_msgtype", 0);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_keyid", (uint32_t)keyid);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_subid", (uint32_t)subid);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_subtype", (uint32_t)subtype);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_type", (uint32_t)alarmtype);
		char *retJsonChars = cJSON_Print(tempJson);
		onUDPSend(6666, retJsonChars);
		cJSON_Delete(tempJson);
		free(retJsonChars);
	}
	return TRUE;
}

bool onUDPSetAlarmInfo(int32_t keyid, int32_t subid, int32_t subtype, int32_t alarmtype, int64_t time, char *string)
{
	if(pDeviceList && !onGetConnectFlag())
	{
		cJSON *tempJson = cJSON_CreateObject();
		cJSON_AddLongNumberToObject(tempJson, "udp_type", 102);//
		cJSON_AddNumberToObject(tempJson, "udp_status", (uint32_t)onGetConnectFlag());
		cJSON_AddLongNumberToObject(tempJson, "udp_familyid", (uint64_t)pDataBase->onGetFamilyID());
		cJSON_AddNumberToObject(tempJson, "udp_gatewayid", (uint32_t)pDataBase->onGetGateway_ID());
		cJSON_AddNumberToObject(tempJson, "udp_alarm_keyid", (uint32_t)keyid);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_subid", (uint32_t)subid);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_subtype", (uint32_t)subtype);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_msgtype", 1);
		cJSON_AddNumberToObject(tempJson, "udp_alarm_type", (uint32_t)alarmtype);
		cJSON_AddLongNumberToObject(tempJson, "udp_alarm_time", (uint64_t)time);
		cJSON_AddStringToObject(tempJson, "udp_alarm_msg", string);
		char *retJsonChars = cJSON_Print(tempJson);
		onUDPSend(6666, retJsonChars);
		cJSON_Delete(tempJson);
		free(retJsonChars);
	}
	return TRUE;
}

bool onUDPSynScreebInfo(int32_t deviceid, int32_t subid, char *string)
{
	if(pDeviceList && !onGetConnectFlag())
	{
		cJSON *tempJson = cJSON_CreateObject();
		cJSON_AddLongNumberToObject(tempJson, "udp_type", 103);//
		cJSON_AddNumberToObject(tempJson, "udp_status", (uint32_t)onGetConnectFlag());
		cJSON_AddLongNumberToObject(tempJson, "udp_familyid", (uint64_t)pDataBase->onGetFamilyID());
		cJSON_AddNumberToObject(tempJson, "udp_gatewayid", (uint32_t)pDataBase->onGetGateway_ID());
		cJSON_AddNumberToObject(tempJson, "udp_screen_keyid", (uint32_t)deviceid);
		cJSON_AddNumberToObject(tempJson, "udp_screen_subid", (uint32_t)subid);
		cJSON_AddStringToObject(tempJson, "udp_screen_msg", string);
		char *retJsonChars = cJSON_Print(tempJson);
		onUDPSend(6666, retJsonChars);
		cJSON_Delete(tempJson);
		free(retJsonChars);
	}
	return TRUE;
}

static void onUDPDevVerTimerOutCB(int32_t par1, int32_t par2)
{
	memset(&mAirUpdateAddr, 0, sizeof(struct sockaddr_in));
}

bool onUDPDevVerReturn(bool ismaster, int32_t devid, int32_t shortaddr, int32_t shortaddr_ex, char *ver)
{
	if(mAirUpdateAddr.sin_port != 0)
	{
		cJSON *gatewayjson = cJSON_CreateObject();
		cJSON_AddNumberToObject(gatewayjson, "log_type", 12);//代表广播应答
		cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
		cJSON_AddStringToObject(gatewayjson, "ver", ver);
		cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
		if(ismaster)
		{
			cJSON_AddNumberToObject(gatewayjson, "shortaddr", (uint32_t)(shortaddr & 0xFFFF));
		}
		else
		{
			cJSON_AddNumberToObject(gatewayjson, "shortaddr", (uint32_t)(shortaddr_ex & 0xFFFF));
		}
		char *retJsonChars = cJSON_Print(gatewayjson);
		sendto(mUDPClientfd, retJsonChars,
		       strlen(retJsonChars), 0,
		       (struct sockaddr *) (&mAirUpdateAddr),
		       sizeof(struct sockaddr_in));
		cJSON_Delete(gatewayjson);
		free(retJsonChars);
	}
	return TRUE;
}

bool onUDPDevNextImageReq(bool ismaster, uint8_t rspseq, uint16_t image, int32_t devid, int32_t filever)
{
	if(ismaster)
	{
		if(mMasterLastAirUpdateAddr.sin_port && (mMasterLastAirDeviceID == devid))
		{
			cJSON *gatewayjson = cJSON_CreateObject();
			cJSON_AddNumberToObject(gatewayjson, "log_type", 14);//代表广播应答
			cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
			cJSON_AddNumberToObject(gatewayjson, "ver", (uint32_t)filever);
			cJSON_AddNumberToObject(gatewayjson, "seq", (uint32_t)rspseq);
			cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
			cJSON_AddNumberToObject(gatewayjson, "image", (uint32_t)image);
			char *retJsonChars = cJSON_Print(gatewayjson);
			sendto(mUDPClientfd, retJsonChars,
			       strlen(retJsonChars), 0,
			       (struct sockaddr *) (&mMasterLastAirUpdateAddr),
			       sizeof(struct sockaddr_in));
			cJSON_Delete(gatewayjson);
			free(retJsonChars);
		}
		else
		{
			mPrintf(ismaster, "0019 port=0 ");
		}
	}
	else
	{
		if(mSlaveLastAirUpdateAddr.sin_port && (mSlaveLastAirDeviceID == devid))
		{
			cJSON *gatewayjson = cJSON_CreateObject();
			cJSON_AddNumberToObject(gatewayjson, "log_type", 14);//代表广播应答
			cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
			cJSON_AddNumberToObject(gatewayjson, "ver", (uint32_t)filever);
			cJSON_AddNumberToObject(gatewayjson, "seq", (uint32_t)rspseq);
			cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
			cJSON_AddNumberToObject(gatewayjson, "image", (uint32_t)image);
			char *retJsonChars = cJSON_Print(gatewayjson);
			sendto(mUDPClientfd, retJsonChars,
			       strlen(retJsonChars), 0,
			       (struct sockaddr *) (&mSlaveLastAirUpdateAddr),
			       sizeof(struct sockaddr_in));
			cJSON_Delete(gatewayjson);
			free(retJsonChars);
		}
		else
		{
			mPrintf(ismaster, "0019 port=0 ");
		}
	}
	return TRUE;
}

bool onUDPDevImageBlockReq(bool ismaster, uint8_t rspseq, uint16_t image, int32_t devid, int32_t filever, int32_t offset, int32_t maxcnt)
{
	if(ismaster)
	{
		if(mMasterLastAirUpdateAddr.sin_port && (mMasterLastAirDeviceID == devid))
		{
			cJSON *gatewayjson = cJSON_CreateObject();
			cJSON_AddNumberToObject(gatewayjson, "log_type", 16);//代表广播应答
			cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
			cJSON_AddNumberToObject(gatewayjson, "ver", (uint32_t)filever);
			cJSON_AddNumberToObject(gatewayjson, "offset", (uint32_t)offset);
			cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
			cJSON_AddNumberToObject(gatewayjson, "image", (uint32_t)image);
			cJSON_AddNumberToObject(gatewayjson, "maxcnt", (uint32_t)maxcnt);
			cJSON_AddNumberToObject(gatewayjson, "seq", (uint32_t)rspseq);
			char *retJsonChars = cJSON_Print(gatewayjson);
			sendto(mUDPClientfd, retJsonChars,
			       strlen(retJsonChars), 0,
			       (struct sockaddr *) (&mMasterLastAirUpdateAddr),
			       sizeof(struct sockaddr_in));
			cJSON_Delete(gatewayjson);
			free(retJsonChars);
		}
		else
		{
			mPrintf(ismaster, "0019 port=0 ");
		}
	}
	else
	{
		if(mSlaveLastAirUpdateAddr.sin_port && (mSlaveLastAirDeviceID == devid))
		{
			cJSON *gatewayjson = cJSON_CreateObject();
			cJSON_AddNumberToObject(gatewayjson, "log_type", 16);//代表广播应答
			cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
			cJSON_AddNumberToObject(gatewayjson, "ver", (uint32_t)filever);
			cJSON_AddNumberToObject(gatewayjson, "offset", (uint32_t)offset);
			cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
			cJSON_AddNumberToObject(gatewayjson, "image", (uint32_t)image);
			cJSON_AddNumberToObject(gatewayjson, "maxcnt", (uint32_t)maxcnt);
			cJSON_AddNumberToObject(gatewayjson, "seq", (uint32_t)rspseq);
			char *retJsonChars = cJSON_Print(gatewayjson);
			sendto(mUDPClientfd, retJsonChars,
			       strlen(retJsonChars), 0,
			       (struct sockaddr *) (&mSlaveLastAirUpdateAddr),
			       sizeof(struct sockaddr_in));
			cJSON_Delete(gatewayjson);
			free(retJsonChars);
		}
		else
		{
			mPrintf(ismaster, "0019 port=0 ");
		}
	}
	return TRUE;
}

bool onUDPDevUpgradeEnd(bool ismaster, uint8_t rspseq, uint16_t image, int32_t devid, int32_t filever)
{
	if(ismaster)
	{
		if(mMasterLastAirUpdateAddr.sin_port && (mMasterLastAirDeviceID == devid))
		{
			cJSON *gatewayjson = cJSON_CreateObject();
			cJSON_AddNumberToObject(gatewayjson, "log_type", 18);//代表广播应答
			cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
			cJSON_AddNumberToObject(gatewayjson, "ver", (uint32_t)filever);
			cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
			cJSON_AddNumberToObject(gatewayjson, "seq", (uint32_t)rspseq);
			cJSON_AddNumberToObject(gatewayjson, "image", (uint32_t)image);
			char *retJsonChars = cJSON_Print(gatewayjson);
			sendto(mUDPClientfd, retJsonChars,
			       strlen(retJsonChars), 0,
			       (struct sockaddr *) (&mMasterLastAirUpdateAddr),
			       sizeof(struct sockaddr_in));
			cJSON_Delete(gatewayjson);
			free(retJsonChars);
		}
		else
		{
			mPrintf(ismaster, "0019 port=0 ");
		}
	}
	else
	{
		if(mSlaveLastAirUpdateAddr.sin_port && (mSlaveLastAirDeviceID == devid))
		{
			cJSON *gatewayjson = cJSON_CreateObject();
			cJSON_AddNumberToObject(gatewayjson, "log_type", 18);//代表广播应答
			cJSON_AddNumberToObject(gatewayjson, "devid", (uint32_t)devid);
			cJSON_AddNumberToObject(gatewayjson, "ver", (uint32_t)filever);
			cJSON_AddNumberToObject(gatewayjson, "ismaster", (uint32_t)ismaster);
			cJSON_AddNumberToObject(gatewayjson, "seq", (uint32_t)rspseq);
			cJSON_AddNumberToObject(gatewayjson, "image", (uint32_t)image);
			char *retJsonChars = cJSON_Print(gatewayjson);
			sendto(mUDPClientfd, retJsonChars,
			       strlen(retJsonChars), 0,
			       (struct sockaddr *) (&mSlaveLastAirUpdateAddr),
			       sizeof(struct sockaddr_in));
			cJSON_Delete(gatewayjson);
			free(retJsonChars);
		}
		else
		{
			mPrintf(ismaster, "0019 port=0 ");
		}
	}
	return TRUE;
}

static char * mfNetGetLocalIP()
{
	int sockfd;
	struct ifconf ifconf;
	char buf[512] = {0};
	struct ifreq *ifreq;
	int len = 0;

	//更新一下
	if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) != -1)
	{                           //调用socket创建用于监听客户端的socket
		ifconf.ifc_len = (int)sizeof(buf);
		ifconf.ifc_buf = buf;
		ioctl(sockfd, SIOCGIFCONF, &ifconf);
		len = ifconf.ifc_len / sizeof(struct ifreq);
		ifreq = (struct ifreq *)buf;
		char *ipString;
		while(len-- > 0)
		{
			ipString = inet_ntoa(((struct sockaddr_in*)&(ifreq->ifr_addr))->sin_addr);
			if((ifreq->ifr_flags == AF_INET) && (strcmp(ipString, "127.0.0.1") != 0))
			{
				close(sockfd);
				return inet_ntoa(((struct sockaddr_in*)&(ifreq->ifr_addr))->sin_addr);
			}
			ifreq++;
		}
	}
	close(sockfd);
	return (char *)"192.168.3.168";
}
