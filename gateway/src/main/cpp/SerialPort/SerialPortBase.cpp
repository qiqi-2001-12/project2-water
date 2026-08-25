/*
 * SerialPortBase.c
 *
 *  Created on: 2017年6月13日
 *      Author: root
 */

#include     <sys/stat.h>
#include "../Main/WinobleMain.h"
#include "../DataType/TypeDefine.h"

void * mfSerialPortThead(void *arg)
{
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
    int lastMS;
    int currentMS;
    int32_t tempInt = 0;
	int32_t sceneTime = 0;
	//启动一个请允许组网检查 10s
	onTimerAdd(TIMER_RPEAT_1_S, 1000, true, mfAllowToJoinCB, 0, 0);
	TypeCarriedSceneList * tempCarriedSceneList = NULL;
	TypeSceneActionInfo * tempSceneActionInfo = NULL;
	TypeSceneNameInfo *tempSceneInfo = NULL;
	lastMS = mGetTimeMs();
	while (mIsExitFlag) //循环读取数据
	{
		//这里定时上报线程信息
		if((onGetTimeSec() - tempThreadInfo->lastSaveTime) >= 10)//10s 上报一次线程的健康状态
		{
			tempThreadInfo->lastSaveTime = onGetTimeSec();
            if(DUALZIGBEECHIP) {
                mPrintf(Log_NetWork, "thread %s:ms=%d ss=%d evt=%d ", tempThreadInfo->title->buff,
                        pmMasterSerialPort->onGetSendCount(), pmSlaveSerialPort->onGetSendCount(),
                        pDataBase->devEventList->size());
            }
		}
		currentMS = mGetTimeMs();
        if(currentMS > lastMS) tempInt = currentMS - lastMS;
        else tempInt = 1000 - lastMS + currentMS;
		sceneTime += tempInt;
		lastMS = currentMS;
		if(!mIsDownLoadingFlag)//如果正在下载更新，不做任何操作
		{
			pmMasterSerialPort->onReviceData(pmMasterSerialPort->deviceProces);
			pmMasterSerialPort->onCheckSendCMD(tempInt);
			if(DUALZIGBEECHIP) {
				pmSlaveSerialPort->onReviceData(pmSlaveSerialPort->deviceProces);
				pmSlaveSerialPort->onCheckSendCMD(tempInt);
			}
			if(sceneTime >= 100)
			{
				//100ms执行一次
				for(int i = 0; i < pDeviceList->carriedOutSceneList->size(); )
				{
					tempCarriedSceneList = (TypeCarriedSceneList *)pDeviceList->carriedOutSceneList->get(i);
					tempCarriedSceneList->addTime += sceneTime;
					for(int j = 0; j < tempCarriedSceneList->actionList->size(); )
					{
						tempSceneActionInfo = (TypeSceneActionInfo *)tempCarriedSceneList->actionList->get(j);
						if(tempSceneActionInfo->onGetDelayTime() <= tempCarriedSceneList->addTime)
						{
							//场景动作执行
							if(tempSceneActionInfo->type == 0)
							{
								//执行设备
								TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempSceneActionInfo->device_id);
								if(tempDBDeviceInfo)//其它家庭的网关在场景里面不管它
								{
									pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, tempSceneActionInfo->sub_id, tempSceneActionInfo->action, TRUE);
								}
							}
							else if(tempSceneActionInfo->type == 1)
							{
								//执行场景
								TypeSceneNameInfo *tempSubSceneNameInfo = pDeviceList->onFindSceneInfo(tempSceneActionInfo->device_id);
								if(tempSubSceneNameInfo != NULL)
								{
									pDeviceList->onSetSceneStatus(tempSubSceneNameInfo, tempSceneActionInfo->action, TRUE);
								}
							}
							else if(tempSceneActionInfo->type == 2)
							{
								//执行家电
								pDeviceList->onSetApplianceStatus(tempSceneActionInfo->device_id, tempSceneActionInfo->sub_id, tempSceneActionInfo->action_desc->buff);
							}
							else
							{
								//未知
								mPrintf(Log_NetWork, "Error:未知的场景动作类型！=%d ", tempSceneActionInfo->type);
							}
							tempCarriedSceneList->actionList->UnFreeRemoveObject(tempSceneActionInfo);
							continue;
						}
						j++;
					}
					if(tempCarriedSceneList->actionList->size() == 0)
					{
						if(tempCarriedSceneList->IsAutoExit)
						{
							//需要定时退出
							if(tempCarriedSceneList->addTime >= tempCarriedSceneList->totalTime)
							{
								//delete
								tempSceneInfo = pDeviceList->onFindSceneInfo(tempCarriedSceneList->scene_id);
								if(tempSceneInfo != NULL)
								{
									pDeviceList->carriedOutSceneList->removeObject(tempCarriedSceneList);
									pDeviceList->onSetSceneStatus(tempSceneInfo, 0, TRUE);
									continue;
								}
							}
						}
						else
						{
							//delete
							pDeviceList->carriedOutSceneList->removeObject(tempCarriedSceneList);
						}
					}
					i++;
				}
				sceneTime = 0;
			}
		}
		usleep(5000);//5ms check once
	}
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

int mfZigbeeUpdateNetWork(int64_t gateway_id ,int32_t newfamily ,int32_t oldfamily)
{
	int32_t retError = 0;
	if(gateway_id == pDataBase->onGetGateway_ID())
	{
		if(newfamily != 0)
		{
			if(oldfamily == 0)
			{
				pDataBase->onSetFamilyID(newfamily);
				pmMasterSerialPort->onStartNewNetWork(TRUE);
				if(DUALZIGBEECHIP) {
					pmSlaveSerialPort->onStartNewNetWork(TRUE);
				}
				mPrintf(Log_NetWork ,"Gateway Zigbee start  start^^^^^^^^^^^^_______ ");
			}
			else
			{
				if(newfamily != oldfamily)
				{
					retError = ERROR_GATEWAY_IS_IN_ANOTHER_FAMILY;
				}
				else
				{
					retError = ERROR_GATEWAY_IS_ALAREADY_EXIT;
				}
			}
		}
		else
		{
			//这里要清除所有数据库信息
			pDeviceList->onDeleteGateway();
			if(oldfamily != 0)
			{
				//reset
				pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_SYS_RESET, NULL, 2000));
				if(DUALZIGBEECHIP) {
					pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_SYS_RESET, NULL, 2000));
				}
				pDataBase->onSetFamilyID(newfamily);
				mPrintf(Log_NetWork ,"Gateway Zigbee reset reset^^^^^^^^^^^^_______ ");
			}
			else
			{
				retError = ERROR_PARA_WRONG;
			}
		}
	}
	else
	{
		retError = ERROR_PARA_WRONG;
	}
	return retError;
}


//从网关中删除设备
int mfLeaveToGateway(int64_t ieee)
{
	TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IIeee, ieee);
	if(tempDBDeviceInfo != NULL)
	{
		int32_t tempShortAddr = tempDBDeviceInfo->shortAddr;
		int64_t tempIEEE = tempDBDeviceInfo->ieee;
		int32_t tempShortAddr_ex = tempDBDeviceInfo->shortAddr_ex;
		int64_t tempIEEE_ex = tempDBDeviceInfo->ieee_ex;
		int64_t tempDBGatewayID = tempDBDeviceInfo->gatewayID;
		mPrintf(Log_NetWork ,"Delete device=%d " ,tempDBDeviceInfo->deviceID);
		pDeviceList->onDeleteDeviceInfo(tempDBDeviceInfo->deviceID);//删除设备
		if(tempDBGatewayID == pDataBase->onGetGateway_ID())
		{
			pmMasterSerialPort->onLeaveWithIEEE(tempShortAddr, tempIEEE);
			if(DUALZIGBEECHIP) {
				pmSlaveSerialPort->onLeaveWithIEEE(tempShortAddr_ex, tempIEEE_ex);
			}
		}
		//重新获取一下所有场景
		pDeviceList->onDownLoadWithFlag(DEVICE_GET_MASK_SCENE);
	}
	return 0;
}

void mfPIRAlarmCB(int par1, int par2)
{
	if(pDeviceList != NULL)
	{
		TypeDeviceTypeInfo *tempDeviceTypeInfo = pDeviceList->onFindDeviceTypeInfo(par1, par2);
		if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_PIR) && (tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status > 0))
		{
			//如果这个红外撤防了，要立即停止这个定时器并上报
			tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status--;
			if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status <= 0)
			{
				tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status = 0;
				UpdateDeviceValueRequest updateRequest;
				DeviceValue *tempDeviceValue = NULL;
				tempDeviceValue = updateRequest.add_values();
				tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_PIR_STATUS);
				tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status);
				updateRequest.set_device_id(tempDeviceTypeInfo->deviceID);
				updateRequest.set_sub_id(tempDeviceTypeInfo->subID);
				updateRequest.set_sub_type(tempDeviceTypeInfo->devType);
				mfTCPCMDSend(CMD_ID_DEVICE_VALUE_UPDATE_REQ, updateRequest.SerializeAsString().c_str(), updateRequest.SerializeAsString().length());
			}
			else
			{
				//延时暂时不上报了
			}

			//mPrintf(Log_SerialPort, "%s 退出延时:%d", tempDeviceTypeInfo->onGetName(), tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status);
			if(tempDeviceTypeInfo->onGetSubInfo()->pirStatus->status == 0)
			{
				//要去检查联动信息
				uint8_t tempBuff[128] = {0x00, 0x00, 0x00, 0x05, 0x88, 0x14, 0x01, 0x01, 0x00, 0x3f, 0x00, 0x66, 0xa9, 0xc9, 0x00, 0x00, 0x09, 0x19, 0x39, 0x00, 0x01, 0x00, 0x00, 0x02, 0x00, 0x00, 0xd0, 0xef, 0x1c};
				//修改一下短地址
				tempBuff[4] = (uint8_t)(tempDeviceTypeInfo->onGetShortAddr() & 0xFF);
				tempBuff[5] = (uint8_t)((tempDeviceTypeInfo->onGetShortAddr()  >> 8) & 0xFF);
				tempBuff[20] = 0x04;
				tempBuff[21] = 0x00;

				//修改zoneID
				//TypeDBDeviceInfo *dbDeviceInfo = pDataBase->onCheckDeviceInfo(IDeviceID, par1);
				/*if(dbDeviceInfo != NULL)
				{
					tempBuff[23] = 1;//暂时没有检查这个属性(uint8_t)(pDataBase->onGetZoneIDInfo(dbDeviceInfo->ieee));
				}
				else
				{
					tempBuff[23] = 0;
				}*/
				tempBuff[23] = 1;
				TypeAFINComming *tempAFInComming = new TypeAFINComming(tempBuff);
				tempAFInComming->onZclProcess(TRUE);
				delete tempAFInComming;
			}
		}
		else
		{
			//删除这个定时器
			onTimerDelete((par1 << 8) + par2);
		}
	}
}

void mfAllowToJoinCB(int par1, int par2)
{
	static TypeDevEventInfo *tempDevEventInfo = NULL;
	static TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
	static TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
	static int32_t timerStatusValue = 30;
	static int32_t lastStatusCheckIndex = 0;
	static uint32_t tempUInt = 0;
	static uint32_t lastSceneCheckTime = 0;
	//static uint32_t lastCheckUpdate = 259200 - 180;//180;//上电稳定3分钟才启动检查升级
	static int32_t lastCheckRtgCnt = 0;
	//每5s检查一次主从模块的路由表
	if(pDataBase->onGetCC2538Ver() > 305)
	{
		lastCheckRtgCnt++;
		if(lastCheckRtgCnt >= 5)
		{
			lastCheckRtgCnt = 0;
			pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_APP_UGET_DEVINFO, NULL, 0));
			if(DUALZIGBEECHIP) {
				pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_APP_UGET_DEVINFO, NULL, 0));
			}
		}
	}
/*
	//3天检查一次升级或者检测到主从模块6 * 30 = 180s 都没有返回再升级
	if(lastCheckUpdate < (259200 - 180))
	{
		//发个通知给JNI界面吗？串口工作不正常呢
		if(DUALZIGBEECHIP)
		{
			//双模块
			if((pmMasterSerialPort->checkDrivceErrorCnt > 5) || (pmSlaveSerialPort->checkDrivceErrorCnt > 5))
			{
				lastCheckUpdate = 259200 - 180;//串口不正常  3分钟后再检查一下升级
				//发个通知jni 串口通信失败
				onNotifyToJava(JNI_NOTIFY_NET_STATUS, 21, 0, 0, "主从模块通信异常");
			}
		}
		else
		{
			//单模块
			if(pmMasterSerialPort->checkDrivceErrorCnt > 5)
			{
				lastCheckUpdate = 259200 - 180;//串口不正常  3分钟后再检查一下升级
			}
		}
	}
	lastCheckUpdate++;
	if(lastCheckUpdate > 259200)
	{
		lastCheckUpdate = 0;//3天后再检查是否有新版本
		//检查一下zigbee固件版本 串口打开失败那就不检查了
		//if((pmMasterSerialPort->deviceHandle != -1) && (pmSlaveSerialPort->deviceHandle != -1))
		{
			//mfHttpCheckAppUpdate(); //屏蔽cc2530检查
		}
	}
*/
	onGetCurrentTime(&tempUInt, NULL);
	if(tempUInt != lastSceneCheckTime)//一分钟执行一次
	{
		lastSceneCheckTime = tempUInt;
		TypeSceneNameInfo *sceneNameInfo = NULL;
		//一分钟检查一次  检查一下场景定时情况
		for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
		{
			sceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			//检查场景定时器是否满足执行条件
			if(sceneNameInfo->onCheckTimeOnCond(lastSceneCheckTime))
			{
				//执行开
				pDeviceList->onSetSceneStatus(sceneNameInfo, 1, TRUE);
			}
			else if(sceneNameInfo->onCheckTimeOffCond(lastSceneCheckTime))
			{
				//执行关  不能同时执行
				pDeviceList->onSetSceneStatus(sceneNameInfo, 0, TRUE);
			}
		}

		//检查一下插座的情况
		if(pDeviceList != NULL)
		{
			for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); i++)
			{
				//只检测属于当前网关的插座情况
				tempDBDeviceInfo = (TypeDBDeviceInfo *) pDeviceList->dbDeviceInfoList->get(i);
				if(tempDBDeviceInfo && (tempDBDeviceInfo->devType == DEVICE_TYPE_SWITCH) && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))//10s 智能插座
				{
					tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(1);
					if(tempDeviceTypeInfo != NULL)
					{
						//检测插座当天能耗是否保存
						if(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->onCheckLastEnergyDate(onGetTimeDate()))
						{
							//更新一下日期
							//更新到服务器
							UpdateDeviceValueRequest updateRequest;
							DeviceValue *tempDeviceValue = NULL;
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_POWER_CONSUMPTION_DATE);
							tempDeviceValue->set_value(tempDeviceTypeInfo->onGetSubInfo()->switchStatus->lastEnergyDate);
							if(updateRequest.values_size() > 0)
							{
								updateRequest.set_device_id(tempDeviceTypeInfo->deviceID);
								updateRequest.set_sub_id(tempDeviceTypeInfo->subID);
								updateRequest.set_sub_type(tempDeviceTypeInfo->devType);
								mfTCPCMDSend(CMD_ID_DEVICE_VALUE_UPDATE_REQ, updateRequest.SerializeAsString().c_str(), updateRequest.SerializeAsString().length());
								tempDeviceTypeInfo->onGetSubInfo()->switchStatus->onClearTadayEnergy();

							}
						}
					}
				}
			}
		}
	}

	if(mIsAlarmingFlag > 0)//报警提示处理
	{
		mIsAlarmingFlag --;
	}
	{
		//设备在线离线 1s检查一次  如果超过心跳包时间还没有收到心跳，就主动写一次属性并回读属性值，直到正常！！
		TypeDBDeviceInfo * saveDBDeviceInfo = NULL;
		bool tempSendFlag = FALSE;
		for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++i)
		{
			tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
			if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
			{
				//这里检查一下设备在线状态和服务器是否一样
				if(tempDBDeviceInfo->onLineFlag.bits.status != tempDBDeviceInfo->onLineFlag.bits.saveStatus)
				{
					mPrintf(Log_Error, "与服务器在线状态不同步! %d-%d ", tempDBDeviceInfo->onLineFlag.bits.status, tempDBDeviceInfo->onLineFlag.bits.saveStatus);
					//更新到服务器
					onUpdateOnOffLineRequest(tempDBDeviceInfo, tempDBDeviceInfo->onLineFlag.bits.status);
				}
				if(onCheckBattery(tempDBDeviceInfo->devType) == FALSE)
				{
					if(tempDBDeviceInfo->saveCheckTime > 0)
					{
						tempDBDeviceInfo->saveCheckTime--;
					}
					if(tempDBDeviceInfo->delayTime > 0)
					{
						tempDBDeviceInfo->delayTime --;
					}
					if(tempDBDeviceInfo->saveCheckTime <= 0)//只检查准备好发送的项
					{
						if(tempDBDeviceInfo->onLineFlag.bits.status)
						{
							if(tempDBDeviceInfo->delayTime > 0)
							{
								//找一个最小的时间值 用于更新
								if(tempDBDeviceInfo->delayTime > pDeviceList->onGetBaseHeartCount())//只剩 1/3 的时间了还没有收到心跳包
								{
									if(tempSendFlag == FALSE)
									{
										if(saveDBDeviceInfo)
										{
											if(saveDBDeviceInfo->delayTime > tempDBDeviceInfo->delayTime)
											{
												saveDBDeviceInfo = tempDBDeviceInfo;
											}
										}
										else
										{
											saveDBDeviceInfo = tempDBDeviceInfo;
										}
									}
								}
								else
								{
									//抢救心跳包事件
									tempSendFlag = TRUE;
									pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, 1, Event_Dev_Heartbeat, 0);
									tempDBDeviceInfo->saveCheckTime = pDeviceList->onGetBaseHeartCount() / 3;//4s内不再重复发送 只抢救3次
								}
							}
							else
							{
								//直接更新到本地
								tempDBDeviceInfo->delayTime = 0;
								pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, (int64_t)tempDBDeviceInfo->onLineFlag.onSetValue(DEVICE_STATUS_OFFLINE, tempDBDeviceInfo->onLineFlag.bits.saveStatus));
								onUpdateOnOffLineRequest(tempDBDeviceInfo, tempDBDeviceInfo->onLineFlag.bits.status);
							}
							//mPrintf(Log_SerialPort, "tick keyID=%d addr=%04x tick=%d", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->delayTime);
						}
						else
						{
							//已经离线
							//发送检查心跳包事件
							tempDBDeviceInfo->saveCheckTime = pDeviceList->onGetBaseHeartCount() * 3;//3倍设备数量s内不再重复发送;
							pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, 1, Event_Dev_Heartbeat, 0);
							tempSendFlag = TRUE;
						}
					}
				}
				else
				{
					//低功耗只设置倒计时
					if(tempDBDeviceInfo->delayTime > 0)
					{
						tempDBDeviceInfo->delayTime --;
					}
					if(tempDBDeviceInfo->onLineFlag.bits.status && (tempDBDeviceInfo->delayTime <= 0))
					{
						//直接更新状态
						pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, (int64_t)tempDBDeviceInfo->onLineFlag.onSetValue(DEVICE_STATUS_OFFLINE, tempDBDeviceInfo->onLineFlag.bits.saveStatus));
						onUpdateOnOffLineRequest(tempDBDeviceInfo, tempDBDeviceInfo->onLineFlag.bits.status);
					}
					tempSendFlag = TRUE;
				}
			}
		}
		if((tempSendFlag == FALSE) && saveDBDeviceInfo)
		{
			if(pmMasterSerialPort->onGetSendCount() <= 0)//当前发送不繁忙才发送指令
			{
				pDataBase->onAddDevEventInfo(saveDBDeviceInfo->deviceID, 1, Event_Dev_Heartbeat, 0);
				saveDBDeviceInfo->saveCheckTime = pDeviceList->onGetBaseHeartCount() / 3;//最低设备数量时间内可以不再发送
			}
		}
	}

	timerStatusValue--;
	if(timerStatusValue <= 0)
	{
		timerStatusValue = 30;
		//状态标志清除 30s 一次 也只检查本网关的设备
		if(pDeviceList != NULL)
		{
			for(; lastStatusCheckIndex < pDeviceList->dbDeviceInfoList->size(); lastStatusCheckIndex++)
			{
				tempDBDeviceInfo = (TypeDBDeviceInfo *) pDeviceList->dbDeviceInfoList->get(lastStatusCheckIndex);
				if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))//10s
				{
					if(pDeviceList->onClearDeviceStatusFlag(tempDBDeviceInfo))
					{
						timerStatusValue = 1;
						lastStatusCheckIndex++;
						break;
					}
				}
			}
			if(lastStatusCheckIndex >= pDeviceList->dbDeviceInfoList->size())
            {
                lastStatusCheckIndex = 0;
            }
		}
	}
	if(pmMasterSerialPort->isError > 0) pmMasterSerialPort->isError--;
	if(pmMasterSerialPort->getSrcEntryTime > 0) pmMasterSerialPort->getSrcEntryTime--;
	if(DUALZIGBEECHIP) {
		if(pmSlaveSerialPort->getSrcEntryTime > 0) pmSlaveSerialPort->getSrcEntryTime--;
	}
	if((pmMasterSerialPort->onGetSendCount() < 10) && (pDataBase->devEventList->size() > 0) && (pmMasterSerialPort->isError <= 0))//1s检查一次  太忙就拜拜
	{
		//mPrintf(Log_Error, "devEventList size=%d sendCount=%d", pDataBase->devEventList->size(), pmMasterSerialPort->onGetSendCount());
		for(int i = 0; i < pDataBase->devEventList->size(); )
		{
			tempDevEventInfo = (TypeDevEventInfo *)pDataBase->devEventList->get(i);
			if(tempDevEventInfo->onCheckDelayTime())
			{
				//只处理本网关的事件
				tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempDevEventInfo->keyID);
				if(tempDBDeviceInfo)
				{
					//心跳包一定要优先，设备数量很多的时候，其它的命令都先等一等
					if(tempDevEventInfo->eventFlag & Event_Dev_Heartbeat)
					{
						//这个不需要网关在线
						if(tempDBDeviceInfo->shortAddr)
						{
							bool defalutSend = FALSE;
							//如果这个设备有温度属性 但温度值无效 需要去读取温度值
							if((tempDBDeviceInfo->lastCheckEndPoint & 0x0F) == 0)
							{
							    //uint8_t CheckEndPointflag = tempDBDeviceInfo->lastCheckEndPoint & 0xF0;
								if((tempDBDeviceInfo->lastCheckEndPoint & 0xF0) == 0x00)
								{
									if(tempDBDeviceInfo->attr->bits.temp)//如果有温度就读取一下温度
									{
										defalutSend = TRUE;
										pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDBDeviceInfo->shortAddr, (uint8_t)1, CLUSTER_ID_TEMPCONFIG, 0x0000, 0);
									}
								}
								else if((tempDBDeviceInfo->lastCheckEndPoint & 0xF0) == 0x10)
								{
									//如果从模块没有心跳包，就使用主模块心跳去设置时间和温度值
									if(tempDBDeviceInfo->attr->bits.screen && ((onGetTimeSec() - tempDBDeviceInfo->slaveTickTime) > (3 * pDeviceList->onGetBaseHeartCount())))
									{
										tempDBDeviceInfo->slaveTickTime = onGetTimeSec();
										//通过主模块发送温度、时间给设备
										TypeChar *tempAttributeBuf = new TypeChar(8);
										tempAttributeBuf->ubuff[5] = (uint8_t)tempDBDeviceInfo->tempperature;//温度
										tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(1);
										if((tempDeviceTypeInfo != NULL) && (tempDeviceTypeInfo->roomID != 0))
										{
											TypeRoomInfo * tempRoomInfo = pDeviceList->onFindRoomInfo(tempDeviceTypeInfo->roomID);
											if(tempRoomInfo != NULL)
											{
												tempAttributeBuf->ubuff[5] = (uint8_t)tempRoomInfo->temp_value;//温度
											}
										}
										if(onIsGoodTemp(tempAttributeBuf->ubuff[5]) == FALSE)
										{
											tempAttributeBuf->ubuff[5] = 0;
										}
										tempAttributeBuf->ubuff[4] = (uint8_t)mTimerNow->tm_hour;//时
										tempAttributeBuf->ubuff[3] = (uint8_t)mTimerNow->tm_min;//分
										tempAttributeBuf->ubuff[2] = (uint8_t)mTimerNow->tm_sec;//秒
										tempAttributeBuf->ubuff[1] = 0;//超时次数
										tempAttributeBuf->ubuff[0] = (uint8_t)pDeviceList->onGetBaseHeartCount();//延时值
										pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, 0x0000, new TypeZclAttribute(0x4002, ZCL_DATATYPE_UINT64, tempAttributeBuf->ubuff, 8), 0);
										delete tempAttributeBuf;
										defalutSend = TRUE;
									}
								}
								else if((tempDBDeviceInfo->lastCheckEndPoint & 0xF0) == 0x20)
								{
									defalutSend = TRUE;
									pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDBDeviceInfo->shortAddr, 1, 0x0000, 0x0001, 0);
								}
								tempDBDeviceInfo->lastCheckEndPoint += 0x10;
								if((tempDBDeviceInfo->lastCheckEndPoint & 0xF0) > 0x20)
								{
									tempDBDeviceInfo->lastCheckEndPoint &= 0x0F;
								}
							}
							else
							{
								//根据不同设备类型发送不同的状态读取命令，考虑每个子节点
								tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(tempDBDeviceInfo->lastCheckEndPoint & 0x0F);
								if(tempDeviceTypeInfo)
								{
									//直接在这里检查一下名称和图标是否同步 检查RGB是否同步
									if(tempDBDeviceInfo->onLineFlag.bits.status)
									{
										//检查一下名称和图标是否同步
										if(pDeviceList->onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Name) && (tempDeviceTypeInfo->name->onStringCMP(tempDeviceTypeInfo->saveName->buff) == FALSE))
										{
											defalutSend = TRUE;
											pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, (uint8_t)tempDeviceTypeInfo->subID, Event_Dev_Name, 0);
										}
										else if(pDeviceList->onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_Icon) && (tempDeviceTypeInfo->iconID != tempDeviceTypeInfo->saveIconID))
										{
											defalutSend = TRUE;
											pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, (uint8_t)tempDeviceTypeInfo->subID, Event_Dev_Icon, 0);
										}
										else if(pDeviceList->onCheckDeviceEvent(tempDBDeviceInfo, Event_Dev_RGB) && (tempDBDeviceInfo->rgb != tempDBDeviceInfo->saveRgb))
										{
											//检查RGB
											defalutSend = TRUE;
											pDataBase->onAddDevEventInfo(tempDBDeviceInfo->deviceID, 1, Event_Dev_RGB, 0);
										}
										else
										{
											//读取基本属性内容   版本号
											switch(tempDeviceTypeInfo->devType)
											{
												case SUB_DEVICE_TYPE_LIGHT:
												case SUB_DEVICE_TYPE_SWITCH://read on/off cluster
												{
													defalutSend = TRUE;
													pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDeviceTypeInfo->onGetShortAddr(), (uint8_t)tempDeviceTypeInfo->subID, CLUSTER_ID_ONOFF, 0x0000, 0);
												}
													break;
												case SUB_DEVICE_TYPE_DIMMER:
												{
													defalutSend = TRUE;//在这里慢慢写配置信息
													if(tempDeviceTypeInfo->onSetDimmingParaValue(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), tempDeviceTypeInfo->subInfo.dimmingStatus->paraValue) == FALSE)
													{
														pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDeviceTypeInfo->onGetShortAddr(), (uint8_t)tempDeviceTypeInfo->subID, CLUSTER_ID_LEVELCONTROL, 0x0000, 0);
													}
												}
													break;
												case SUB_DEVICE_TYPE_CURTAIN:
												{
													defalutSend = TRUE;
													pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDeviceTypeInfo->onGetShortAddr(), (uint8_t)tempDeviceTypeInfo->subID, CLUSTER_ID_LEVELCONTROL, 0x0000, 0);
												}
													break;//read level cluster
												case SUB_DEVICE_TYPE_CLOTHES_HANGER:
												{
													defalutSend = TRUE;
													pmMasterSerialPort->onReadAttribute((uint32_t)tempDeviceTypeInfo->onGetShortAddr(), (uint8_t)tempDeviceTypeInfo->subID, CLUSTER_ID_PERSONAL, 0x000F, 0);
												}
													break;
												case SUB_DEVICE_TYPE_RS485_TRANSFER:
												{
													if(tempDeviceTypeInfo->onGetSubInfo()->rs485Status->status != tempDeviceTypeInfo->onGetSubInfo()->rs485Status->saveStatus)
													{
														if(onCheckRS485BaudIsOK(tempDeviceTypeInfo->onGetSubInfo()->rs485Status->status))
														{
															defalutSend = TRUE;
															//设置一下设备的波特率
															TypeChar *tempRS485Para = new TypeChar(16);
															tempRS485Para->onAddInt64Ex(0, tempDeviceTypeInfo->onGetSubInfo()->rs485Status->status);//9600 even cs8 1
															pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000D, ZCL_DATATYPE_UINT64, tempRS485Para->ubuff, (uint8_t)8), 0);
															delete tempRS485Para;
															//延时读取一下
															pmMasterSerialPort->onReadAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, 0x0D, 200);
														}
													}
												}
													break;
												default:break;
											}
										}
									}
								}
							}
							if(defalutSend == FALSE)
							{
								pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDBDeviceInfo->shortAddr, 1, 0x0000, 0x0001, 0);
							}
							tempDBDeviceInfo->lastCheckEndPoint++;
							if((tempDBDeviceInfo->lastCheckEndPoint & 0x0F) > tempDBDeviceInfo->subCount)
							{
								tempDBDeviceInfo->lastCheckEndPoint &= 0xF0;
							}
						}
						pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, 1, Event_Dev_Heartbeat);//清除标志
					}
					else
					{
						if(pmMasterSerialPort->onGetSendCount() < 10)
						{
							if(tempDevEventInfo->eventFlag == 0)//事件已经处理完成
							{
								pDataBase->devEventList->removeObject(tempDevEventInfo);
								continue;
							}
							if(tempDBDeviceInfo->onLineFlag.bits.status && tempDBDeviceInfo->shortAddr)//这些都是必须在线、入网了且不忙的时候处理
							{
								//如果当前命令太繁忙  就等一下再发送
								if(tempDevEventInfo->eventFlag & Event_Dev_RGB)
								{
									if(tempDBDeviceInfo->attr->bits.rgb && (tempDBDeviceInfo->rgb != tempDBDeviceInfo->saveRgb))
									{
										tempDevEventInfo->delyaTime = pmMasterSerialPort->onGetSendCount() / 5 + 3;
										TypeChar *tempSendChars = new TypeChar(8);
										tempSendChars->onAddInt64Ex(0, tempDBDeviceInfo->rgb);
										//远程设置RGB值
										pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0000, ZCL_DATATYPE_UINT64, tempSendChars->ubuff, 8), 0);
										delete tempSendChars;
										//再延时读取一下这个属性进行确认
										pmMasterSerialPort->onReadAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, 0x0000, 1000);
									}
									else
									{
										pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, 1, Event_Dev_RGB);//清除标志
									}
								}
								else
								{
									switch(tempDevEventInfo->eventFlag)
									{
										case Event_Dev_Name:
										{
											//所有名称都要检查
											for(int j = 1; j <= tempDBDeviceInfo->subCount; ++j)
											{
												if(tempDevEventInfo->subID & (1 << j))
												{
													tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
													if(tempDeviceTypeInfo != NULL)
													{
														if(tempDeviceTypeInfo->name->onStringCMP(tempDeviceTypeInfo->saveName->buff) == FALSE)
														{
															//修改设备名称
															tempDevEventInfo->delyaTime = pmMasterSerialPort->onGetSendCount() / 5 + 3;
															//向设备也更新一下
															TypeChar *sendNameBuff = new TypeChar(16);
															TypeChar *pSendBuff = new TypeChar(17);
															pSendBuff->ubuff[0] = (uint8_t)onConverUnicodeString(tempDeviceTypeInfo->name->buff, sendNameBuff->buff, 8);
															pSendBuff->onAddUBuff(1, sendNameBuff->ubuff, pSendBuff->ubuff[0]);
															pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, (uint8_t)tempDeviceTypeInfo->subID, 0x0000, new TypeZclAttribute(0x4001, ZCL_DATATYPE_CHAR_STR, pSendBuff->ubuff, (uint8_t)(pSendBuff->ubuff[0] + 1)), 0);
															delete sendNameBuff;
															delete pSendBuff;
															//再延时读取一下这个属性进行确认
															pmMasterSerialPort->onReadAttribute((uint32_t)tempDBDeviceInfo->shortAddr, (uint8_t)tempDeviceTypeInfo->subID, 0x0000, 0x4001, 1000);
														}
														else
														{
															//名称已经相同了
															pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, tempDeviceTypeInfo->subID, Event_Dev_Name);
														}
													}
													else
													{
														pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, j, Event_Dev_Name);
													}
												}
											}
										}
											break;
										case Event_Dev_Icon:
										{
											//所有图标都要检查
											for(int j = 1; j <= tempDBDeviceInfo->subCount; ++j)
											{
												if(tempDevEventInfo->subID & (1 << j))
												{
													tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
													if(tempDeviceTypeInfo != NULL)
													{
														if(tempDeviceTypeInfo->iconID != tempDeviceTypeInfo->saveIconID)
														{
															//修改设备图标
															tempDevEventInfo->delyaTime = pmMasterSerialPort->onGetSendCount() / 5 + 3;
															TypeChar *tempSendChars = new TypeChar(2);
															tempSendChars->onAddInt16Ex(0, tempDeviceTypeInfo->iconID);
															//远程设置RGB值
															pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, (uint8_t)tempDeviceTypeInfo->subID, 0x0000, new TypeZclAttribute(0x4000, ZCL_DATATYPE_UINT16, tempSendChars->ubuff, 2), 0);
															delete tempSendChars;
															//再延时读取一下这个属性进行确认
															pmMasterSerialPort->onReadAttribute((uint32_t)tempDBDeviceInfo->shortAddr, (uint8_t)tempDeviceTypeInfo->subID, 0x0000, 0x4000, 1000);
														}
														else
														{
															//图标已经相同了
															pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, tempDeviceTypeInfo->subID, Event_Dev_Icon);
														}
													}
													else
													{
														pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, j, Event_Dev_Icon);
													}
												}
											}
										}
											break;
										case Event_Dev_Status:
										{
											//所有状态都要检查
											for(int j = 1; j <= tempDBDeviceInfo->subCount; ++j)
											{
												if(tempDevEventInfo->subID & (1 << j))
												{
													tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(j);
													if(tempDeviceTypeInfo)
													{
														//保证设备是在线的，否则也不再发送
														if(tempDBDeviceInfo->onLineFlag.bits.status && (((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT) && ((tempDeviceTypeInfo->onGetSubInfo()->lightStatus->sceneID == 0) && (tempDeviceTypeInfo->onGetSubInfo()->lightStatus->needSetStatus != tempDeviceTypeInfo->onGetSubInfo()->lightStatus->status)))
														   || ((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH) && (tempDeviceTypeInfo->onGetSubInfo()->switchStatus->needSetStatus != tempDeviceTypeInfo->onGetSubInfo()->switchStatus->status))))
														{
															mPrintf(Log_NetWork, "Error:开关状态事件抢救![$%04x$ subID=%02d] keyID=%d name=%s ", tempDeviceTypeInfo->onGetShortAddr(), tempDeviceTypeInfo->subID, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->name->buff);
															tempDevEventInfo->delyaTime = pmMasterSerialPort->onGetSendCount() / 5 + 3;
															if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT)
															{
																pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->onGetSubInfo()->lightStatus->needSetStatus, TRUE);
															}
															else if(tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_SWITCH)
															{
																pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->onGetSubInfo()->switchStatus->needSetStatus, TRUE);
															}
															//再延时读取一下这个属性进行确认
															pmMasterSerialPort->onReadAttributeGeneric((uint32_t)tempDeviceTypeInfo->onGetShortAddr(), (uint8_t)tempDeviceTypeInfo->subID, CLUSTER_ID_ONOFF, 0x0000, 1000);
														}
														else
														{
															pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, tempDeviceTypeInfo->subID, Event_Dev_Status);
														}
													}
													else
													{
														pDataBase->onClearDevEventInfo(tempDevEventInfo->keyID, j, Event_Dev_Status);
													}
												}
											}
										}
											break;
										default:
											break;
									}
								}
							}
							else
							{
								//设备不在线  那这些事件都可以直接删除了吧？
							}
						}
					}
				}
				else
				{
					//直接清除这个事件
					mPrintf(Log_Error, "添加了一个不是本网关的事件！请查证@ evt=%d ", tempDevEventInfo->eventFlag);
					pDataBase->devEventList->removeObject(tempDevEventInfo);
					continue;
				}
			}
			i++;
		}
	}
}

bool onSendAlarmInfo(time_t time, bool todevice, char *name, int32_t roomid, int32_t alarmtype, int32_t deviceid, int32_t subid, int32_t subtype, TypeChar *retstr)
{
	//这里解析下报警
	//发送报警 时:分:秒 xx-xxx 报警
	TypeChar *alarmRoomName = NULL;
	char *needNameBuff = name;
	TypeRoomInfo *tempRoomInfo = pDeviceList->onFindRoomInfo(roomid);
	if(tempRoomInfo == NULL)
	{
		alarmRoomName = new TypeChar((char *)"未知");
	}
	else
	{
		alarmRoomName = new TypeChar(tempRoomInfo->name->buff);
	}
	switch(alarmtype)
	{
		case DEVICE_ALARM_TYPE_PIR_INTRUSION:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "有陌生人来访!");break;
		case DEVICE_ALARM_TYPE_GAS_GAS_LEAK:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "燃气泄露!");break;
		case DEVICE_ALARM_TYPE_SMOKE_OUTBREAK_OF_FIRE:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "发生火灾!");break;
		case DEVICE_ALARM_TYPE_FLOOD_SOS:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "紧急求救!");break;
		case DEVICE_ALARM_TYPE_FLOOD_WATER_LEAK:
		{
			if(subtype == SUB_DEVICE_TYPE_FLOOD)
			{
				sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "房屋漏水!");
			}
			else if(subtype == SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR)
			{
				sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "水龙头漏水!");
			}
		}
			break;
		case DEVICE_ALARM_TYPE_DOOR_WINDOW_OPEN:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "门窗开!");break;
		case DEVICE_ALARM_TYPE_DOOR_WINDOW_CLOSE:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "门窗关!");break;
		case DEVICE_ALARM_TYPE_LOW_BATTERY:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "电量过低!");break;
		case DEVICE_ALARM_TYPE_WATER_PIPE_BURST:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "水管爆裂!");break;
		case DEVICE_ALARM_TYPE_DISMANTLED:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "防拆报警!");break;
		case DEVICE_ALARM_TYPE_SYSTEM_LOCKED: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "系统锁定!");break;
		case DEVICE_ALARM_TYPE_SYSTEM_BE_RESET: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "系统被重置!");break;
		case DEVICE_ALARM_TYPE_ARM: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "布防!");break;
		case DEVICE_ALARM_TYPE_DISARM: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "撤防!");break;
		case DEVICE_ALARM_TYPE_BE_COERCED: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "被胁迫报警!");break;
		case DEVICE_ALARM_TYPE_UNCLOSED: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "未关闭!");break;
		case DEVICE_ALARM_TYPE_CLOSED: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "已关闭!");break;
		case DEVICE_ALARM_TYPE_FAKE_LOCKED: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "假锁!");break;
		case DEVICE_ALARM_TYPE_DOOR_BELL_RANG: sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "门铃!");break;
		default:sprintf(retstr->buff, "%s %s-%s %s", onChangeTimeS(time), alarmRoomName->buff, needNameBuff, "未知报警类型!");break;
	}
	if(todevice)
	{
		TypeChar *tempUnicodeBuff = new TypeChar();
		TypeChar *alarmInfo = new TypeChar();
		alarmInfo->ubuff[0] = (uint8_t )(onConverUnicodeString(retstr->buff, tempUnicodeBuff->buff, 0) + 5);
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
	}

	//发送到app 发送一条报警
	onNotifyToJava(JNI_NOTIFY_ALARM, deviceid, subid, subtype, retstr->buff);
	delete alarmRoomName;
	return TRUE;
}
