//
// Created by xia_w on 2017/12/10.
//

#include "../Main/WinobleMain.h"
#include "../DataType/TypeDefine.h"

static char mTableCMDType[8][10] = {"POLL", "SREQ", "AREQ", "SRSP", "RES4", "RES5", "RES6", "RES7"};
static char mTableCMDSubSystem[22][16] = {"RES0", "SYS", "MAC", "NWK", "AF", "ZDO", "SAPI", "UTIL", "DBG", "APP", "OTA", "ZNP", "SPARE", "UBL", "RES14", "APP_CNF",
										  "RES16", "PROTOBUF", "RES18", "RES19", "RES20", "GP"};
TypeSerialProces::TypeSerialProces(bool ismater)
{
	onMemAdd();
	isMasterFlag = ismater;
	onClear();
	afAttribute = NULL;
	reSendBit = 0;
	repeatCount = 3;
}

void TypeSerialProces::onMemAdd()
{
	mMemNewFreeCount++;
}


TypeSerialProces::TypeSerialProces(uint32_t shortaddr, uint8_t cmd1, uint8_t cmd2, TypeAFAttribute * afattribute, int32_t delaytime)
{
	onMemAdd();
	afAttribute = afattribute;
	cmdType = (uint8_t)(cmd1 & 0xE0);
	subSystem = (uint8_t)(cmd1 & 0x1F);
	subCMD = cmd2;
	status = 0;
	checkSum = 0;
	repeatCount = 3;//重发3次
	delayTime = delaytime;
	shortAddr = shortaddr;
	reSendBit = 0;
	retStatus = SEND_INIT;
}

void TypeSerialProces::onClear()
{
	cmdType = 0;
	subSystem = 0;
	subCMD = 0;
	status = 0;
	checkSum = 0;
	delayTime = 0;
	retStatus = SEND_INIT;
}

bool TypeSerialProces::onSysProces()
{
	switch(cmdType)
	{
		case MT_RPC_CMD_SREQ://设备请求
			switch(subCMD)
			{
				default:
				mPrintf(isMasterFlag, "Error:SYS->SREQ subCMD=%02x un proces ", subCMD);
				break;
			}
			break;
		case MT_RPC_CMD_SRSP://设备响应
		{
			switch(subCMD)
			{
				case MT_SYS_VERSION://获取版本号返回
				{
					//发送给app
					if((afAttribute != NULL) && (afAttribute->toDataLen == 5))
					{
						if(afAttribute->toDataBuff->ubuff[0] == 2)
						{
							pDataBase->onSetChipType(2538);//cc2538
						}
						else if(afAttribute->toDataBuff->ubuff[0] == 4)
						{
							pDataBase->onSetChipType(2530);//cc2530
						}
						//版本号也更新一下吧
						int32_t tempVer = (afAttribute->toDataBuff->ubuff[2] & 0x0F);
						tempVer = tempVer * 10 + (afAttribute->toDataBuff->ubuff[3] & 0x0F);
						tempVer = tempVer * 10 + (afAttribute->toDataBuff->ubuff[4] & 0x0F);
						pDataBase->onSetCC2538Ver(tempVer);
					}
				}
					break;
				case MT_SYS_OSAL_NV_WRITE:
					break;
				case MT_SYS_OSAL_NV_READ://读取NV内容返回
				{

				}
				break;
				default:
					mPrintf(isMasterFlag, "Error:SYS->SRSP subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		case MT_RPC_CMD_AREQ://设备同步请求
		{
			switch(subCMD)
			{
				case MT_SYS_RESET_IND:
				{
					if(isMasterFlag)
					{
						mPrintf(isMasterFlag, "master reset! ");
						//请求一下网络数据
						pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
						//检查 一下版本号
						pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_VERSION, NULL, 100));
					}
					else
					{
						if(DUALZIGBEECHIP) {
							mPrintf(isMasterFlag, "slave reset! ");
							//请求一下网络数据
							pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
							//检查 一下版本号
							pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_VERSION, NULL, 100));
						}
					}
				}
					break;
				default:
					mPrintf(isMasterFlag, "Error:SYS->AREQ subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		default:mPrintf(isMasterFlag, "Error:SYS cmdType=[%s] un proces ", mTableCMDType[cmdType >> 5]);break;
	}
	return true;
}

bool TypeSerialProces::onAPPProces()
{
	switch(cmdType)
	{
		case MT_RPC_CMD_SREQ://设备请求
			switch(subCMD)
			{
				case MT_APP_CHECK_WHITE_LIST://白名单检查返回
				{
					int64_t tempIEEE = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[0], 8);
					TypeChar *sendChars = new TypeChar(9);
					sendChars->ubuff[0] = (uint8_t )pDeviceList->onCheckWhiteList(isMasterFlag, tempIEEE);
					if(sendChars->ubuff[0])
					{
						mPrintf(isMasterFlag, "WHITE=%llx 白名单请求!", tempIEEE);
					}
					//
					sendChars->onAddUBuff(1, &afAttribute->toDataBuff->ubuff[0], 8);
					//应答一下
					if(isMasterFlag)
					{
						pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SRSP | MT_RPC_SYS_APP, MT_APP_CHECK_WHITE_LIST, new TypeAFAttribute(sendChars->ubuff, 9), 0));
					}
					else
					{
						if(DUALZIGBEECHIP) {
							pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SRSP | MT_RPC_SYS_APP, MT_APP_CHECK_WHITE_LIST, new TypeAFAttribute(sendChars->ubuff, 9), 0));
						}
					}
					delete sendChars;
				}
					break;
				case MT_APP_MSG:
				{

				}
					break;
				default:
					mPrintf(isMasterFlag, "Error:APP->SREQ subCMD=%02x un proces ", subCMD);
					break;
			}
			break;
		case MT_RPC_CMD_SRSP://设备响应
		{
			switch(subCMD)
			{
				case MT_USER_GET_SUB_IEEE://这里再次检查一下有没有这个设备   没有就删除
				{
					if(afAttribute->toDataLen == 12)
					{
						int64_t tempIEEE = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[2], 8);
						TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
						if(tempIEEE != 0)
						{
							if(isMasterFlag)
							{
								tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IIeee, tempIEEE);
								if(tempDBDeviceInfo == NULL)
								{
									mPrintf(isMasterFlag, "UGET IEEE %llx unexist! ", tempIEEE);
									pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_SUB_DEV, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[0], 2), 0));
								}
							}
							else
							{
								if(DUALZIGBEECHIP) {
									tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IIeee_Ex, tempIEEE);
									if (tempDBDeviceInfo == NULL) {
										mPrintf(isMasterFlag, "UGET IEEE %llx unexist! ", tempIEEE);
										pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_SUB_DEV, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[0], 2), 0));
									}
								}
							}
						}
					}

				}
					break;
				case MT_APP_UGET_DEVINFO:
				{
					static uint8_t mSubDevCnt = 0, sSubDevCnt = 0, mNeighborCnt = 0, sNeighborCnt = 0, mRtgEntriesCnt = 0, sRtgEntriesCnt = 0;
					static uint8_t mSrcEntriesCnt = 0, sSrcEntriesCnt = 0, mMemPer = 0, mMaxMemPer = 0, sMemPer = 0, sMaxMemPer = 0;
					if(isMasterFlag)
					{
						if(afAttribute->toDataLen >= 29)
						{
							//0:内存剩余百分比 1:当前最大使用百分比
							//2：子节点大小 3:index 4-5:短地址  6:age 7:nodeRelation
							//8：邻节点大小 9:index 10-11:短地址  12-13：panid 14:txCnt 15:rxLqi
							//16:路由表大小 17:index 18-19:目的地址 20-21：下一跳地址 22:expiretime 23:status 24:option
							//25:源路由表大小26:index 27-28:目的地址 29:expiretime 30:cnt 31-32:下一跳地址

							mMemPer = afAttribute->toDataBuff->ubuff[0];
							mMaxMemPer = afAttribute->toDataBuff->ubuff[1];
							int32_t tempSubShortAddr = onGetInt32(&afAttribute->toDataBuff->ubuff[4], 2);
							int32_t tempNeighborAddr = onGetInt32(&afAttribute->toDataBuff->ubuff[10], 2);
							int32_t tempRtgAddr = onGetInt32(&afAttribute->toDataBuff->ubuff[18], 2);
							int32_t tempSrcShortAddr = onGetInt32(&afAttribute->toDataBuff->ubuff[27], 2);
							static uint8_t tempSubDevCnt = 0;
							if(afAttribute->toDataBuff->ubuff[3] == 0)
							{
								mSubDevCnt = afAttribute->toDataBuff->ubuff[2] - tempSubDevCnt;
								tempSubDevCnt = 0;
							}
							static uint8_t tempNeighborCnt = 0;
							if(afAttribute->toDataBuff->ubuff[9] == 0)
							{
								mNeighborCnt = afAttribute->toDataBuff->ubuff[8] - tempNeighborCnt;
								tempNeighborCnt = 0;
							}
							static uint8_t tempRtgEntriesCnt = 0;
							if(afAttribute->toDataBuff->ubuff[17] == 0)
							{
								mRtgEntriesCnt = afAttribute->toDataBuff->ubuff[16] - tempRtgEntriesCnt;
								tempRtgEntriesCnt = 0;
							}
							static uint8_t tempSrcEntriesCnt = 0;
							if(afAttribute->toDataBuff->ubuff[26] == 0)
							{
								mSrcEntriesCnt = afAttribute->toDataBuff->ubuff[25] - tempSrcEntriesCnt;
								tempSrcEntriesCnt = 0;
							}
							//打印一下当前设备网络的情况
							if(onCheckPrint())
							{
								mPrintf(isMasterFlag, "UGET sub=%d/%d nei=%d/%d rtg=%d/%d src=%d/%d mem=%d/%d maxmem=%d/%d ", mSubDevCnt, sSubDevCnt, mNeighborCnt, sNeighborCnt,
								        mRtgEntriesCnt, sRtgEntriesCnt, mSrcEntriesCnt, sSrcEntriesCnt, mMemPer, sMemPer, mMaxMemPer, sMaxMemPer);
							}
							TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
							for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
							{
								tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
								if(tempDBDeviceInfo && tempDBDeviceInfo->shortAddr && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))
								{
									if( tempSubShortAddr && (tempDBDeviceInfo->shortAddr == tempSubShortAddr))
									{
										tempSubShortAddr = 0;
										tempSubDevCnt++;
									}
									if(tempNeighborAddr && (tempDBDeviceInfo->shortAddr == tempNeighborAddr))
									{
										tempNeighborAddr = 0;
										tempNeighborCnt++;
									}
									if(tempRtgAddr && (tempDBDeviceInfo->shortAddr == tempRtgAddr))
									{
										tempRtgAddr = 0;
										tempRtgEntriesCnt++;
									}
									if(tempSrcShortAddr && (tempDBDeviceInfo->shortAddr == tempSrcShortAddr))
									{
										tempSrcShortAddr = 0;
										tempSrcEntriesCnt++;
										if(tempDBDeviceInfo->checkSrcRouter)
										{
											tempDBDeviceInfo->checkSrcRouter = false;
											//设备不在线。这个时候要检查一下路由路径是否失效
											int32_t deleayListCnt = (afAttribute->toDataLen - 30) / 2;
											for(int i = 0; i < deleayListCnt; ++ i)
											{
												TypeDBDeviceInfo *tempCheckSrc = pDeviceList->onCheckFamilyDeviceInfo(IShortAddr, onGetInt32(&afAttribute->toDataBuff->ubuff[30 + i * 2], 2));
												if((tempCheckSrc == NULL) || (tempCheckSrc && (tempCheckSrc->onLineFlag.bits.status == FALSE)))
												{
													//找不到路由节点，或者节点不在线
													tempSrcShortAddr = onGetInt32(&afAttribute->toDataBuff->ubuff[27], 2);
													if(tempSrcEntriesCnt) tempSrcEntriesCnt--;
													mPrintf(isMasterFlag, "UGET 无效的路由路径!$%04x$ ", tempSubShortAddr);
													break;
												}
											}
										}
									}
								}
							}
							if(tempSubShortAddr)
							{
								pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_GET_SUB_IEEE, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[4], 2), 1000));
							}
							if(tempNeighborAddr && (tempNeighborAddr != 0xFFFE))
							{
								mPrintf(isMasterFlag, "UGET neighbor unknow shortAddr = %04x ", tempNeighborAddr);
								pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_NEIGHBOR, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[10], 4), 0));
							}
							if(tempRtgAddr && (tempRtgAddr != 0xFFFE))
							{
								mPrintf(isMasterFlag, "UGET rtg entries unknow shortAddr = %04x ", tempRtgAddr);
								pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_ENTRY, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[18], 2), 0));
							}
							if(tempSrcShortAddr && (tempSrcShortAddr != 0xFFFE))
							{
								mPrintf(isMasterFlag, "UGET src entries unknow shortAddr = %04x ", tempSrcShortAddr);
								//找不到这个短地址 并且已经超时就删除掉
								if(afAttribute->toDataBuff->ubuff[29] == 0)
								{
									//发送删除命令
									pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_SRC_ENTRY, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[27], 2), 0));
								}
							}
						}
					}
					else
					{
						if(DUALZIGBEECHIP) {
							if (afAttribute->toDataLen >= 31) {
								//0：子节点大小 1:index 2-3:短地址  4:age 5:nodeRelation
								//6：邻节点大小 7:index 8-9:短地址  10-11：panid 12:txCnt 13:rxLqi
								//14:路由表大小 15:index 16-17:目的地址 18-19：下一跳地址 20:expiretime 21:status 22:option
								//23:源路由表大小24:index 25-26:目的地址 27:expiretime 28:cnt 29-30:下一跳地址
								//31:内存剩余百分比 32:当前最大使用百分比
								sMemPer = afAttribute->toDataBuff->ubuff[0];
								sMaxMemPer = afAttribute->toDataBuff->ubuff[1];
								int32_t tempSubShortAddr = onGetInt32(
										&afAttribute->toDataBuff->ubuff[4], 2);
								int32_t tempNeighborAddr = onGetInt32(
										&afAttribute->toDataBuff->ubuff[10], 2);
								int32_t tempRtgAddr = onGetInt32(
										&afAttribute->toDataBuff->ubuff[18], 2);
								int32_t tempSrcShortAddr = onGetInt32(
										&afAttribute->toDataBuff->ubuff[27], 2);
								static uint8_t tempSubDevCnt = 0;
								if (afAttribute->toDataBuff->ubuff[3] == 0) {
									sSubDevCnt = afAttribute->toDataBuff->ubuff[2] - tempSubDevCnt;
									tempSubDevCnt = 0;
								}
								static uint8_t tempNeighborCnt = 0;
								if (afAttribute->toDataBuff->ubuff[9] == 0) {
									sNeighborCnt =
											afAttribute->toDataBuff->ubuff[8] - tempNeighborCnt;
									tempNeighborCnt = 0;
								}
								static uint8_t tempRtgEntriesCnt = 0;
								if (afAttribute->toDataBuff->ubuff[17] == 0) {
									sRtgEntriesCnt =
											afAttribute->toDataBuff->ubuff[16] - tempRtgEntriesCnt;
									tempRtgEntriesCnt = 0;
								}
								static uint8_t tempSrcEntriesCnt = 0;
								if (afAttribute->toDataBuff->ubuff[26] == 0) {
									sSrcEntriesCnt =
											afAttribute->toDataBuff->ubuff[25] - tempSrcEntriesCnt;
									tempSrcEntriesCnt = 0;
								}
								//打印一下当前设备网络的情况
								TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
								for (int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++i) {
									tempDBDeviceInfo = (TypeDBDeviceInfo *) pDeviceList->dbDeviceInfoList->get(
											i);
									if (tempDBDeviceInfo && tempDBDeviceInfo->shortAddr_ex &&
										(tempDBDeviceInfo->gatewayID ==
										 pDataBase->onGetGateway_ID())) {
										if (tempSubShortAddr &&
											(tempDBDeviceInfo->shortAddr_ex == tempSubShortAddr)) {
											tempSubShortAddr = 0;
											tempSubDevCnt++;
										}
										if (tempNeighborAddr &&
											(tempDBDeviceInfo->shortAddr_ex == tempNeighborAddr)) {
											tempNeighborAddr = 0;
											tempNeighborCnt++;
										}
										if (tempRtgAddr &&
											(tempDBDeviceInfo->shortAddr_ex == tempRtgAddr)) {
											tempRtgAddr = 0;
											tempRtgEntriesCnt++;
										}
										if (tempSrcShortAddr &&
											(tempDBDeviceInfo->shortAddr_ex == tempSrcShortAddr)) {
											tempSrcShortAddr = 0;
											tempSrcEntriesCnt++;
											if (tempDBDeviceInfo->checkSrcRouterEx) {
												tempDBDeviceInfo->checkSrcRouterEx = false;
												//设备不在线。这个时候要检查一下路由路径是否失效
												int32_t deleayListCnt =
														(afAttribute->toDataLen - 30) / 2;
												for (int i = 0; i < deleayListCnt; ++i) {
													TypeDBDeviceInfo *tempCheckSrc = pDeviceList->onCheckFamilyDeviceInfo(
															IShortAddr_Ex, onGetInt32(
																	&afAttribute->toDataBuff->ubuff[
																			30 + i * 2], 2));
													if ((tempCheckSrc == NULL) || (tempCheckSrc &&
																				   (tempCheckSrc->onLineFlag.bits.status ==
																					FALSE))) {
														//找不到路由节点，或者节点不在线
														tempSrcShortAddr = onGetInt32(
																&afAttribute->toDataBuff->ubuff[27],
																2);
														if (tempSrcEntriesCnt) tempSrcEntriesCnt--;
														mPrintf(isMasterFlag,
																"UGET 无效的路由路径!$%04x$ ",
																tempSubShortAddr);
														break;
													}
												}
											}
										}
									}
								}
								if (tempSubShortAddr) {
									pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_GET_SUB_IEEE, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[4], 2), 1500));
								}
								if (tempNeighborAddr && (tempNeighborAddr != 0xFFFE)) {
									mPrintf(isMasterFlag, "UGET neighbor unknow shortAddr = %04x ",
											tempNeighborAddr);
									pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_NEIGHBOR, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[10], 4), 0));
								}
								if (tempRtgAddr && (tempRtgAddr != 0xFFFE)) {
									mPrintf(isMasterFlag,
											"UGET rtg entries unknow shortAddr = %04x ",
											tempRtgAddr);
									pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_ENTRY, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[18], 2), 0));
								}
								if (tempSrcShortAddr && (tempSrcShortAddr != 0xFFFE)) {
									mPrintf(isMasterFlag,
											"UGET src entries unknow shortAddr = %04x ",
											tempSrcShortAddr);
									//找不到这个短地址 并且已经超时就删除掉
									if (afAttribute->toDataBuff->ubuff[29] == 0) {
										//发送删除命令
										pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_USER_DELETE_SRC_ENTRY, new TypeAFAttribute(&afAttribute->toDataBuff->ubuff[27], 2), 0));
									}
								}
							}
						}
					}
				}
					break;
				default:
					mPrintf(isMasterFlag, "Error:APP->SRSP subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		default:mPrintf(isMasterFlag, "Error:APP cmdType=[%s] un proces ", mTableCMDType[cmdType >> 5]);break;
	}
	return true;
}

bool TypeSerialProces::onAFProces()
{

	switch(cmdType)
	{
		case MT_RPC_CMD_SREQ://设备请求
			switch(subCMD)
			{
				case MT_AF_DATA_REQUEST:
					break;
				default:
					mPrintf(isMasterFlag, "Error:AF->SREQ subCMD=%02x un proces ", subCMD);
					break;
			}
			break;
		case MT_RPC_CMD_SRSP://设备响应
		{
			switch(subCMD)
			{
				case MT_AF_REGISTER://注册端点成功返回
					break;
				case MT_AF_DATA_REQUEST://写数据应答
					if(isMasterFlag)
					{
						pmMasterSerialPort->onSetSendStatus(afAttribute->toDataBuff->ubuff[0], 1);
					}
					else
					{
						if(DUALZIGBEECHIP) {
							pmSlaveSerialPort->onSetSendStatus(afAttribute->toDataBuff->ubuff[0], 1);
						}
					}
					break;
				default:
					mPrintf(isMasterFlag, "Error:AF->SRSP subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		case MT_RPC_CMD_AREQ:
			{
				switch(subCMD)
				{
					case MT_AF_INCOMING_MSG://cluster 数据来了
					{
						TypeAFINComming *tempAFInComming = new TypeAFINComming(afAttribute->toDataBuff->ubuff);
						//分析一下cluster
						tempAFInComming->onZclProcess(isMasterFlag);
						delete tempAFInComming;
					}
						break;
					case MT_AF_DATA_CONFIRM://AF 数据确认
						if(isMasterFlag)
						{
							pmMasterSerialPort->onSetSendStatus(afAttribute->toDataBuff->ubuff[0], 2);
						}
						else
						{
							if(DUALZIGBEECHIP) {
								pmSlaveSerialPort->onSetSendStatus(afAttribute->toDataBuff->ubuff[0], 2);
							}
						}
						break;
					default:
						mPrintf(isMasterFlag, "Error:AF->AREQ subCMD=%02x un proces ", subCMD);
						break;
				}
			}
			break;
		default:mPrintf(isMasterFlag, "Error:AF cmdType=[%s] un proces ", mTableCMDType[cmdType >> 5]);break;
	}
	return true;
}

bool TypeSerialProces::onZDOProces()
{
	switch(cmdType)
	{
		case MT_RPC_CMD_SREQ://设备请求
			switch(subCMD)
			{
				case MT_ZDO_EXT_NWK_INFO:
					break;
				default:
					mPrintf(isMasterFlag, "Error:ZDO->SREQ subCMD=%02x un proces ", subCMD);
					break;
			}
			break;
		case MT_RPC_CMD_SRSP://设备响应
		{
			switch(subCMD)
			{
				case MT_ZDO_EXT_NWK_INFO://读取设备信息返回
				{
					if((afAttribute->toDataBuff->ubuff[2] == 0x09) || (afAttribute->toDataBuff->ubuff[2] == 0x00))//当前网络为工作或者初始化状态
					{
						//int32_t tempShortAddr = (int32_t)onGetInt32Ex(afAttribute->toDataBuff->ubuff, 2);
						uint32_t channel = afAttribute->toDataBuff->ubuff[23];
						uint64_t ieeeAddr = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[7], 8);
						uint64_t ieeeAddrEx = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[15], 8);
						uint32_t pandID = onGetInt32Ex(&afAttribute->toDataBuff->ubuff[3], 2);
						if(afAttribute->toDataBuff->ubuff[2] == 0x00)
						{
							pandID = 0;
							channel = 0;
						}
						if(isMasterFlag)//主模块
						{
							pmMasterSerialPort->checkDrivceErrorCnt = 0;
							//主模块处理方式
							if((afAttribute->toDataBuff->ubuff[2] == 0x00) && (pDataBase->onGetFamilyID() != 0))
							{
								pmMasterSerialPort->onStartNewNetWork(false);

							}
							else if((afAttribute->toDataBuff->ubuff[2] == 0x09) && (pDataBase->onGetFamilyID() == 0))
							{
								pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_SYS_RESET, NULL, 0));
								mPrintf(isMasterFlag, "reset gateway net work! ");
								onUpdateMasterResetGatewayInfo(channel ,(char *) DEFAULT_GATEWAY_NAME ,0);
							}
							else
							{
								pDataBase->onSetIEEE(ieeeAddr);
								pDataBase->onSetEx_PANID(ieeeAddrEx);
								if((int32_t)pandID != pDataBase->onGetPANID())
								{
									pDataBase->onSetPANID(pandID);

								}

								if(pDataBase->onSetChannel(channel))
								{
									onUpdateMasterStartGatewayInfo(channel);
								}
								if(afAttribute->toDataBuff->ubuff[2] == 0x09)
								{
									if((1 << channel) & DEFAULT_TC_MASTER_CHANNEL)
									{
										if(pmMasterSerialPort->startNetFail)
										{
											pmMasterSerialPort->startNetFail = 0;
										}
										{
											//已经组网  定时允许入网
											TypeChar *tempSend = new TypeChar(4);
											tempSend->onAddInt16Ex(0 ,SHORTADDR_BROADCAST);
											tempSend->ubuff[2] = 0x32;//允许入网50s
											pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_PMT_JOIN_REQ, new TypeAFAttribute(tempSend->ubuff, 3), 0));
											delete tempSend;
										}
									}
									else
									{
										pmMasterSerialPort->onStartNewNetWork(TRUE);
									}

								}
							}
						}
						else
						{
							if(DUALZIGBEECHIP) {
								pmSlaveSerialPort->checkDrivceErrorCnt = 0;
								//从模块处理方式
								if((afAttribute->toDataBuff->ubuff[2] == 0x00) && (pDataBase->onGetFamilyID() != 0))
								{
									pmSlaveSerialPort->onStartNewNetWork(false);
								}
								else if((afAttribute->toDataBuff->ubuff[2] == 0x09) && (pDataBase->onGetFamilyID() == 0))
								{
									pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_SYS_RESET, NULL, 0));
									mPrintf(isMasterFlag, "reset gateway net work! ");
									onUpdateSlaveResetGatewayInfo(channel ,(char *) DEFAULT_GATEWAY_NAME ,0);
								}
								else
								{
									pDataBase->onSetIEEE_EX(ieeeAddr);
									pDataBase->onSetEx_PANID_Ex(ieeeAddrEx);
									pDataBase->onSetPANID_Ex(pandID);
									if(pDataBase->onSetChannel_Ex(channel))
									{
										onUpdateSlaveStartGatewayInfo(channel);
									}
									if(afAttribute->toDataBuff->ubuff[2] == 0x09)
									{
										if((1 << channel) & DEFAULT_TC_SLAVE_CHANNEL)
										{
											if(pmSlaveSerialPort->startNetFail)
											{
												pmSlaveSerialPort->startNetFail = 0;
											}
											{
												//已经组网  定时允许入网
												TypeChar *tempSend = new TypeChar(4);
												tempSend->onAddInt16Ex(0 ,SHORTADDR_BROADCAST);
												tempSend->ubuff[2] = 0x32;//允许入网50s
												pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_PMT_JOIN_REQ, new TypeAFAttribute(tempSend->ubuff, 3), 0));
												delete tempSend;
											}
										}
										else
										{
											pmSlaveSerialPort->onStartNewNetWork(TRUE);
										}
									}
								}
							}
						}
					}
					else
					{
						if(isMasterFlag)
						{
							if((pmMasterSerialPort->startNetFail) && ((onGetTimeSec() - pmMasterSerialPort->startNetFail) > 60))
							{
								//启动网络失败
								pmMasterSerialPort->startNetFail = 0;
								pmMasterSerialPort->onStartNewNetWork(FALSE);
							}
						}
						else
						{
							if(DUALZIGBEECHIP) {
								if((pmSlaveSerialPort->startNetFail) && ((onGetTimeSec() - pmSlaveSerialPort->startNetFail) > 60))
								{
									//启动网络失败
									pmSlaveSerialPort->startNetFail = 0;
									pmSlaveSerialPort->onStartNewNetWork(FALSE);
								}
							}
						}
					}
				}
					break;
				case MT_ZDO_IEEE_ADDR_REQ://地址请求 命令是否响应返回
					break;
				case MT_ZDO_MGMT_LEAVE_REQ://离网返回
					break;
				case MT_ZDO_EXT_ROUTE_DISC://路由发现返回
					break;
				default:
					mPrintf(isMasterFlag, "Error:ZDO->SRSP subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		case MT_RPC_CMD_AREQ:
			switch(subCMD)
			{
				case MT_ZDO_SRC_RTG_IND://源地址
				{
					TypeChar *tempChars = new TypeChar();//
					for(int i = 0; i < afAttribute->toDataBuff->ubuff[2]; ++ i)
					{
						sprintf(&tempChars->buff[i * 5], "%04x,", (afAttribute->toDataBuff->ubuff[3 + 2 * i + 1] << 8) + afAttribute->toDataBuff->ubuff[3 + 2 * i]);
					}

					mPrintf(isMasterFlag, "源路由地址:$%04x$<%s> ", (afAttribute->toDataBuff->ubuff[1] << 8) + afAttribute->toDataBuff->ubuff[0], tempChars->buff);
					delete tempChars;
				}
					break;
				case MT_ZDO_PERMIT_JOIN_IND:break;
				case MT_ZDO_STATE_CHANGE_IND:
					if((afAttribute != NULL) && (afAttribute->toDataLen > 0) && (afAttribute->toDataBuff->ubuff[0] == 9))
					{
						//注册一个端点
						TypeChar *sendChar = new TypeChar(9);
						sendChar->ubuff[0] = 0x01;//注册端点1
						sendChar->onAddInt16Ex(1, HA_PROFILE_ID);//0x04=HA profile
						sendChar->onAddInt16Ex(3, 0x0100);//device id
						sendChar->ubuff[5] = 0x01;
						sendChar->ubuff[6] = 0x00;
						sendChar->ubuff[7] = 0x00;
						sendChar->ubuff[8] = 0x00;

						if(isMasterFlag)
						{
							pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_REGISTER, new TypeAFAttribute(sendChar->ubuff, 9), 0));
							//请求一下网络数据
							pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
							mPrintf(Log_Master, "master start ok! ");
						}
						else
						{
							if(DUALZIGBEECHIP) {
								pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_REGISTER, new TypeAFAttribute(sendChar->ubuff, 9), 0));
								//请求一下网络数据
								pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
								mPrintf(Log_Slave, "slave start ok! ");
							}
						}
						delete sendChar;
					}
					break;
				case MT_ZDO_MGMT_LEAVE_RSP:
					break;
				case MT_ZDO_LEAVE_IND://设备离网指示
				{
					//短地址清除
					uint64_t ieeeAddr = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[4], 8);
					TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
					if(isMasterFlag)
					{
						tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IIeee, ieeeAddr);
						if(tempDBDeviceInfo != NULL)
						{
							pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr, (int64_t)0);
						}
						//
					}
					else
					{
						if(DUALZIGBEECHIP) {
							tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IIeee_Ex, ieeeAddr);
							if(tempDBDeviceInfo != NULL)
							{
								pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr_Ex, (int64_t)0);
							}
						}
					}
				}
					break;
				case MT_ZDO_MGMT_PERMIT_JOIN_RSP://允许组网返回
					break;
				case MT_ZDO_TC_DEVICE_IND://请求连接
				{
					mPrintf(isMasterFlag, "有设备正在找网 ");
				}
					break;
				case MT_ZDO_END_DEVICE_ANNCE_IND://设备入网通知
				{
					//设备短地址更新
					int32_t tempShortAddr = onGetInt32Ex(&afAttribute->toDataBuff->ubuff[2], 2);
					int64_t tempIEEEAddr = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[4], 8);
					TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
					if(isMasterFlag)
					{
						//更新数据库中短地址信息
						tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IIeee, tempIEEEAddr);
						if(tempDBDeviceInfo != NULL)
						{
							if((tempDBDeviceInfo->shortAddr != tempShortAddr) && tempShortAddr)
							{
								mPrintf(Log_Master, "Master:短地址信息修改:keyID=%d(%llx) %04x->%04x ", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->ieee, tempDBDeviceInfo->shortAddr, tempShortAddr);
								pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr, tempShortAddr);
								//更新一下服务器地址
								onUpdateDeviceInfo(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex);
							}
							tempDBDeviceInfo->gatewayID = pDataBase->onGetGateway_ID();

							//如果是设备更新了短地址
							pDeviceList->onResetDeviceDBInfo(tempDBDeviceInfo);

							if(onCheckBattery(tempDBDeviceInfo->devType))
							{
								//写CIE 和 zoneID
								//需要注册IEEE地址
								TypeChar *tempSend = new TypeChar(8);
								tempSend->onAddInt64Ex(0, pDataBase->onGetIEEE());
								pmMasterSerialPort->onWriteAttributeGeneric((uint32_t)tempShortAddr, 0x01, 0x0500, new TypeZclAttribute(0x0010, ZCL_DATATYPE_IEEE_ADDR, tempSend->ubuff, 8), 0);

								//写zoneID
								tempSend->onClear();
								tempSend->ubuff[0] = 0x00;
								tempSend->ubuff[1] = pDataBase->onGetZoneID(tempIEEEAddr);
								pmMasterSerialPort->onWriteZclCMD((uint32_t)tempShortAddr, 0x01, 0x0500, 0x00, tempSend->ubuff, 2, 200);
								delete tempSend;
							}
						}
					}
					else
					{
						if(DUALZIGBEECHIP) {
							//更新数据库中短地址信息
							tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IIeee_Ex, tempIEEEAddr);
							if(tempDBDeviceInfo != NULL)
							{
								if(tempDBDeviceInfo->shortAddr_ex != tempShortAddr)
								{
									mPrintf(Log_Slave, "Slave:短地址信息修改:keyID=%d(%llx) %04x->%04x ", tempDBDeviceInfo->deviceID, tempDBDeviceInfo->ieee_ex, tempDBDeviceInfo->shortAddr_ex, tempShortAddr);
									pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr_Ex, tempShortAddr);
								}
							}
						}
					}
				}
					break;
				case MT_ZDO_IEEE_ADDR_RSP://地址请求响应
				{
					if(afAttribute->toDataBuff->ubuff[0] == 0)
					{
						//请求成功
						int32_t tempShortAddr = onGetInt32Ex(&afAttribute->toDataBuff->ubuff[9], 2);
						int64_t tempIeee = onGetInt64Ex(&afAttribute->toDataBuff->ubuff[1], 8);
						if(isMasterFlag)
						{
							//先判断一下数据库
							TypeDBDeviceInfo *dbDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IIeee, tempIeee);
							if(dbDeviceInfo != NULL)
							{
								if(dbDeviceInfo->shortAddr != tempShortAddr)
								{
									//更新数据库地址
									pDataBase->onUpdateDeviceInfoSqlValue(dbDeviceInfo, IShortAddr, tempShortAddr);
									//如果是设备更新了短地址
									pDeviceList->onResetDeviceDBInfo(dbDeviceInfo);
									//更新服务器地址
									onUpdateDeviceInfo(dbDeviceInfo->deviceID, dbDeviceInfo->shortAddr, dbDeviceInfo->shortAddr_ex);
								}
							}
							else
							{
								pmMasterSerialPort->onLeaveWithIEEE(tempShortAddr, tempIeee);
							}
						}
						else
						{
							if(DUALZIGBEECHIP) {
								//先判断一下数据库
								TypeDBDeviceInfo *dbDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(
										IIeee_Ex, tempIeee);
								if (dbDeviceInfo != NULL) {
									if (dbDeviceInfo->shortAddr_ex != tempShortAddr) {
										//更新数据库地址
										pDataBase->onUpdateDeviceInfoSqlValue(dbDeviceInfo,
																			  IShortAddr_Ex,
																			  tempShortAddr);
										//更新服务器地址
										onUpdateDeviceInfo(dbDeviceInfo->deviceID,
														   dbDeviceInfo->shortAddr,
														   dbDeviceInfo->shortAddr_ex);
									}
								} else {
									pmSlaveSerialPort->onLeaveWithIEEE(tempShortAddr, tempIeee);
								}
							}
						}
					}
				}
					break;
				default:
					mPrintf(isMasterFlag, "Error:ZDO->AREQ subCMD=%02x un proces ", subCMD);
					break;
			}
			break;
		default:
			mPrintf(isMasterFlag, "Error:ZDO cmdType=[%s] un proces ", mTableCMDType[cmdType >> 5]);
			break;
	}
	return true;
}

bool TypeSerialProces::onSAPIProces()
{
	switch(cmdType)
	{
		case MT_RPC_CMD_SREQ://设备请求
			switch(subCMD)
			{
				default:
					mPrintf(isMasterFlag, "Error:SAPI->SREQ subCMD=%02x un proces ", subCMD);
					break;
			}
			break;
		case MT_RPC_CMD_SRSP://设备响应
		{
			switch(subCMD)
			{
				case MT_SAPI_PMT_JOIN_REQ://允许组网返回
					break;
				case MT_SAPI_START_REQ://网络启动返回
					break;
				default:
					mPrintf(isMasterFlag, "Error:SAPI->SRSP subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		default:mPrintf(isMasterFlag, "Error:SAPI cmdType=[%s] un proces ", mTableCMDType[cmdType >> 5]);break;
	}
	return true;
}

bool TypeSerialProces::onUTILProces()
{
	switch(cmdType)
	{
		case MT_RPC_CMD_SREQ://设备请求
			switch(subCMD)
			{
				default:
					mPrintf(isMasterFlag, "Error:UTIL->SREQ subCMD=%02x un proces ", subCMD);
					break;
			}
			break;
		case MT_RPC_CMD_SRSP://设备响应
		{
			switch(subCMD)
			{
				case MT_UTIL_SET_PANID:
					break;
				case MT_UTIL_SET_CHANNELS:
					break;
				case MT_UTIL_SET_PRECFGKEY:
					break;
				default:
					mPrintf(isMasterFlag, "Error:UTIL->SRSP subCMD=%02x un proces ", subCMD);
					break;
			}
		}
			break;
		default:mPrintf(isMasterFlag, "Error:UTIL cmdType=[%s] un proces ", mTableCMDType[cmdType >> 5]);break;
	}
	return true;
}

bool TypeSerialProces::onProces()
{
	switch(subSystem)
	{
		case MT_RPC_SYS_SYS:onSysProces();break;
		case MT_RPC_SYS_AF:onAFProces();break;
		case MT_RPC_SYS_ZDO:onZDOProces();break;
		case MT_RPC_SYS_APP:onAPPProces();break;
		case MT_RPC_SYS_SAPI:onSAPIProces();break;
		case MT_RPC_SYS_UTIL:onUTILProces();break;
		default:if(isMasterFlag) mPrintf(isMasterFlag, "Error:subSystem=[%s] un proces! ", mTableCMDSubSystem[subSystem]);break;
	}
	return true;
}

bool TypeSerialProces::onCheckTime(int32_t timems)
{
	delayTime -= timems;
	return (delayTime <= 0);
}

bool TypeSerialProces::onIsZigbeeCMD()
{
	//广播命令也不重发
	if(afAttribute && (shortAddr != SHORTADDR_BROADCAST) && afAttribute->sendZcl && afAttribute->sendZcl->attrubiteData && (subCMD == MT_AF_DATA_REQUEST))
	{
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

bool TypeSerialProces::onIsSameCMD(TypeSerialProces *proces)
{
	//同类指令定义:短地址相同、clusterID相同、端点相同、命令ID相同、属性ID相同
	if((shortAddr == proces->shortAddr) && afAttribute && afAttribute->sendZcl && afAttribute->sendZcl->attrubiteData && (afAttribute->desEndPoint == proces->afAttribute->desEndPoint) && (afAttribute->clusterID == proces->afAttribute->clusterID) &&
			(afAttribute->sendZcl->cmdID == proces->afAttribute->sendZcl->cmdID) && (afAttribute->sendZcl->attrubiteData->attributeID == proces->afAttribute->sendZcl->attrubiteData->attributeID))
	{
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

bool TypeSerialProces::onPrintfError(uint8_t status, uint8_t type)
{
	if(onCheckPrint() && (status != 0))
	{
		TypeChar *logChar = new TypeChar(256);
		if(type == 1)
		{
			logChar->buff[0] = 'G';
			logChar->buff[1] = 'W';
			logChar->buff[2] = 'C';
		}
		else
		{
			logChar->buff[0] = 'A';
			logChar->buff[1] = 'F';
			logChar->buff[2] = 'C';
		}
		logChar->buff[3] = ':';
		TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
		if(afAttribute != NULL)
		{
			TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
			if(isMasterFlag)
			{
				tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IShortAddr, shortAddr);
			}
			else
			{
				tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IShortAddr_Ex, shortAddr);
			}
			if(tempDBDeviceInfo)
			{
				tempDeviceTypeInfo = pDeviceList->onFindDeviceTypeInfo(tempDBDeviceInfo->deviceID, afAttribute->desEndPoint);
			}
		}

		if(afAttribute->sendZcl && afAttribute->sendZcl->attrubiteData)
		{
			if(tempDeviceTypeInfo)
			{
				sprintf(&logChar->buff[4], "%02x[%04x, %04x] $%04x$<%d, %s> %s%02x ", afAttribute->afSeqNum, afAttribute->clusterID, afAttribute->sendZcl->attrubiteData->attributeID, shortAddr, tempDeviceTypeInfo->subID, tempDeviceTypeInfo->name->buff, (status > 0) ? "#Error=" : "", status);
			}
			else
			{
				sprintf(&logChar->buff[4], "%02x[%04x, %04x] $%04x$<%d, %s> %s%02x ", afAttribute->afSeqNum, afAttribute->clusterID, afAttribute->sendZcl->attrubiteData->attributeID, shortAddr, 1, "NULL", (status > 0) ? "#Error=" : "", status);
			}
		}
		else
		{
			if(afAttribute != NULL)
			{
				sprintf(&logChar->buff[4], "%02x[%04x] NULL %s%02x ", afAttribute->afSeqNum, afAttribute->clusterID, (status > 0) ? "#Error=" : "", status);
			}
			else
			{
				sprintf(&logChar->buff[4], "NULL %s%02x ", (status > 0) ? "#Error=" : "", status);
			}
		}
		mPrintf(isMasterFlag, "%s ", logChar->buff);
		delete logChar;
	}
	return true;
}

bool TypeSerialProces::toString(bool sendflag)
{
//    if((subSystem == MT_RPC_SYS_AF) && (subCMD == MT_AF_INCOMING_MSG))
//    {
//        mPrintf(isMasterFlag, "air subSystem:%d subCMD:%d cluster:%x%x attr:%x%x len:%d", subSystem, subCMD,
//                afAttribute->toDataBuff->ubuff[3],afAttribute->toDataBuff->ubuff[2],afAttribute->toDataBuff->ubuff[5],
//                afAttribute->toDataBuff->ubuff[4],afAttribute->toDataBuff->ubuff[16]);
//    }
	if(onCheckPrint())
	{
		bool isSendFlag = TRUE;

		if(subSystem == MT_RPC_SYS_SAPI)
		{
			switch(subCMD)
			{
				case MT_SAPI_PMT_JOIN_REQ:
					//isSendFlag = FALSE;
					break;
				default:break;
			}
		}
		else if(subSystem == MT_RPC_SYS_APP)
		{
			if((subCMD == MT_APP_CHECK_WHITE_LIST) || (subCMD == MT_APP_UGET_DEVINFO) || (subCMD == MT_USER_GET_SUB_IEEE))
			{
				isSendFlag = FALSE;
			}

		}
		else if(subSystem == MT_RPC_SYS_ZDO)
		{
			switch(subCMD)
			{
				//case MT_ZDO_EXT_NWK_INFO:
				case MT_ZDO_SRC_RTG_IND:
				case MT_ZDO_MGMT_PERMIT_JOIN_RSP:
					//isSendFlag = FALSE;
					break;
				default:break;
			}
		}
		else if(subSystem == MT_RPC_SYS_AF)
		{
			switch(subCMD)
			{
				case MT_AF_DATA_CONFIRM:
					//case MT_AF_DATA_REQUEST:
					isSendFlag = FALSE;
					break;//写属性确认
				case MT_AF_DATA_REQUEST:
					if(cmdType != MT_RPC_CMD_SREQ)
						isSendFlag = FALSE;
					break;
				default:break;
			}

		}
		if(isSendFlag)
		{
			//然后解析一下zcl命令
			if(subSystem == MT_RPC_SYS_AF)
			{
				TypeChar *tempPrintfBuffs = new TypeChar();
				if((subCMD == MT_AF_INCOMING_MSG) && (afAttribute != NULL))
				{
					//解析一下数据接收
					TypeAFINComming *tempAFInComming = new TypeAFINComming(afAttribute->toDataBuff->ubuff);
//                    mPrintf(isMasterFlag, "3.air cmdid:%d clusterid:%x attr:%x", tempAFInComming->zclProfile->cmdID,tempAFInComming->clusterID, tempAFInComming->zclProfile->attrubiteData->attributeID);
					if((tempAFInComming->zclProfile != NULL) && (tempAFInComming->zclProfile->attrubiteData != NULL))
					{
						if(tempAFInComming->zclProfile->zclHead->type == ZCL_FRAME_TYPE_SPECIFIC_CMD)
						{
							//命令
							mPrintf(isMasterFlag, "R[RSSI:%d LQI:%d]:%s->[$%04x$ %02x][%04x %04x]->%02x %s ", tempAFInComming->radius, tempAFInComming->LinkQuality,
							        "SPECIAL", tempAFInComming->shortAddr, tempAFInComming->srcEndPoint, tempAFInComming->clusterID, tempAFInComming->zclProfile->cmdID, tempAFInComming->zclProfile->attrubiteData->dataType, onPrintfUBuff(tempAFInComming->zclProfile->attrubiteData->onGetDataBuff(), tempAFInComming->zclProfile->attrubiteData->dataBuffLen, tempPrintfBuffs->buff));
							isSendFlag = FALSE;
						}
						else
						{
							//属性
							switch(tempAFInComming->zclProfile->cmdID)
							{
								case ZCL_CMD_REPORT:
									mPrintf(isMasterFlag, "R[RSSI:%d LQI:%d]:%s->[$%04x$ %02x][%04x %04x]->%02x %s ", tempAFInComming->radius, tempAFInComming->LinkQuality, onGetZCLCMDID(tempAFInComming->zclProfile->cmdID), tempAFInComming->shortAddr, tempAFInComming->srcEndPoint, tempAFInComming->clusterID, tempAFInComming->zclProfile->attrubiteData->attributeID, tempAFInComming->zclProfile->attrubiteData->dataType, onPrintfUBuff(tempAFInComming->zclProfile->attrubiteData->onGetDataBuff(), tempAFInComming->zclProfile->attrubiteData->dataBuffLen, tempPrintfBuffs->buff));
									isSendFlag = FALSE;
									break;
								case ZCL_CMD_WRITE_RSP:
									mPrintf(isMasterFlag, "R[RSSI:%d LQI:%d]:%s->%02x [$%04x$ %02x][%04x]->%s ", tempAFInComming->radius, tempAFInComming->LinkQuality, onGetZCLCMDID(tempAFInComming->zclProfile->cmdID), tempAFInComming->zclProfile->seqNum, tempAFInComming->shortAddr, tempAFInComming->srcEndPoint, tempAFInComming->clusterID, onPrintfUBuff(tempAFInComming->zclProfile->attrubiteData->totalBuff->ubuff, tempAFInComming->zclProfile->attrubiteData->totalLen, tempPrintfBuffs->buff));
									if(tempAFInComming->zclProfile->attrubiteData->totalBuff->ubuff[0] != 0)
									{
										mPrintf(isMasterFlag, "Error: zcl 写属性无线应答出错=%02x! seq=%02x ", tempAFInComming->zclProfile->attrubiteData->totalBuff->ubuff[0], tempAFInComming->zclProfile->seqNum);
									}
									isSendFlag = FALSE;
									break;
								default:
									mPrintf(isMasterFlag, "R[RSSI:%d LQI:%d]:%s->[$%04x$ %02x][%04x %04x]->%02x %s ", tempAFInComming->radius, tempAFInComming->LinkQuality, onGetZCLCMDID(tempAFInComming->zclProfile->cmdID), tempAFInComming->shortAddr, tempAFInComming->srcEndPoint, tempAFInComming->clusterID, tempAFInComming->zclProfile->attrubiteData->attributeID, tempAFInComming->zclProfile->attrubiteData->dataType, onPrintfUBuff(tempAFInComming->zclProfile->attrubiteData->onGetDataBuff(), tempAFInComming->zclProfile->attrubiteData->dataBuffLen, tempPrintfBuffs->buff));
									isSendFlag = FALSE;
									break;
							}
						}
					}
					delete tempAFInComming;
				}
				else if((subCMD == MT_AF_DATA_REQUEST) && (afAttribute->sendZcl != NULL) && (afAttribute->sendZcl->attrubiteData != NULL))
				{
					//zcl 数据发送
					if(afAttribute->sendZcl->zclHead->type == ZCL_FRAME_TYPE_SPECIFIC_CMD)
					{
						//这个打印的方式有点不一样
						mPrintf(isMasterFlag, "%sS:%s->%02x [$%04x$ %02x][%04x %04x]->%s ", reSendBit ? "re " : "", "SPECIAL", afAttribute->sendZcl->seqNum, shortAddr, afAttribute->desEndPoint, afAttribute->clusterID, afAttribute->sendZcl->cmdID, afAttribute->sendZcl->attrubiteData->dataBuffLen ? onPrintfUBuff(afAttribute->sendZcl->attrubiteData->onGetDataBuff(), afAttribute->sendZcl->attrubiteData->dataBuffLen, tempPrintfBuffs->buff) : "NULL");
					}
					else
					{
						mPrintf(isMasterFlag, "%sS:%s->%02x [$%04x$ %02x][%04x %04x]->%02x %s ", reSendBit ? "re " : "", onGetZCLCMDID(afAttribute->sendZcl->cmdID), afAttribute->sendZcl->seqNum, shortAddr, afAttribute->desEndPoint, afAttribute->clusterID, afAttribute->sendZcl->attrubiteData->attributeID, afAttribute->sendZcl->attrubiteData->dataType, onPrintfUBuff(afAttribute->sendZcl->attrubiteData->onGetDataBuff(), afAttribute->sendZcl->attrubiteData->dataBuffLen, tempPrintfBuffs->buff));
					}
					isSendFlag = FALSE;
				}
				delete tempPrintfBuffs;
			}
		}

		if(isSendFlag)
		{
			TypeChar *logChar = new TypeChar(512);
			if(isMasterFlag)
			{
				logChar->buff[0] = 'M';
			}
			else
			{
				logChar->buff[0] = 'S';
			}
			if(sendflag)
			{
				logChar->buff[1] = 'S';
			}
			else
			{
				logChar->buff[1] = 'R';
			}
			logChar->buff[2] = ':';
			sprintf(&logChar->buff[3], "[%s, %s] ", mTableCMDType[cmdType >> 5], (char *)onGetSerialSubCMDString(subSystem, subCMD));
			//在这里直接处理一下
			int tempLogCharIndex = strlen(logChar->buff);
			if((afAttribute != NULL) && (afAttribute->toDataLen > 0))
			{
				for(int i = 0; i < afAttribute->toDataLen; i++)
				{
					logChar->ubuff[tempLogCharIndex + 3 * i] = mf4CharToHex((uint8_t)((afAttribute->toDataBuff->ubuff[i] >> 4) & 0x0F));
					logChar->ubuff[tempLogCharIndex + 3 * i + 1] = mf4CharToHex((uint8_t)(afAttribute->toDataBuff->ubuff[i] & 0x0F));
					logChar->ubuff[tempLogCharIndex + 3 * i + 2] = ' ';
				}
			}
			else
			{
				sprintf(&logChar->buff[tempLogCharIndex], "NULL");
			}

			mPrintf(isMasterFlag, "%s ", logChar->buff);
			delete logChar;
		}
	}
	return TRUE;
}

TypeSerialProces::~TypeSerialProces()
{
	if(afAttribute != NULL)
	{
		delete afAttribute;
	}
	afAttribute = NULL;
	if(mMemNewFreeCount > 0)
	{
		mMemNewFreeCount--;
	}
}
