//
// Created by xia_w on 2018/3/6.
//
#include     <sys/stat.h>
#include "../Main/WinobleMain.h"
#define SB_SOF_STATE                0
#define SB_LEN_STATE                1
#define SB_FRAME_ID_STATE           2
#define SB_CMD_STATE                3
#define SB_DATA_STATE               4
#define SB_FCS_STATE                5
#define SB_LEN1_STATE               6
#define SB_LEN2_STATE               7
#define SB_LEN3_STATE               8
#define SB_LEN4_STATE               9

typedef struct TYPE_CMD_RECIVER__
{
	uint8_t sbSte;
	uint32_t sbLen;
	uint8_t sbIdx;
	uint8_t sbCmd;
	uint8_t sbFcs;
	uint8_t sbFrameId;
	bool isOK;
	char sbBuf[512];
	TYPE_CMD_RECIVER__()
	{
		onClear();
	};
	void onClear()
	{
		sbSte = SB_SOF_STATE;
		sbLen = 0;
		sbIdx = 0;
		sbCmd = 0;
		sbFcs = 0;
		sbFrameId = 0;
		isOK = FALSE;
	}
}TypeCMDRecicer;

static void onDowLoadTimerOut(int par1, int par2);
static uint32_t mCurrentPercent = 0;
static bool mDownLoadTimeOut = false;
static bool onCMDProcess(TypeCMDRecicer *reciver, uint8_t *buff, int len);

void * mfZnpDownLoadThread(void *arg)
{
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	if(mIsDownLoadingFlag == FALSE)
	{
		mIsDownLoadingFlag = TRUE;//正在下载标志设置
		//解析jason
		cJSON *json = cJSON_Parse(tempThreadInfo->threadPara->buff);
		if(json)
		{
			cJSON *tempJson;
			TypeChar *fileName = NULL;
			TypeChar *filemd5 = NULL;
			TypeChar *filepath = NULL;
			TypeChar *fileSavePath = NULL;
			tempJson = cJSON_GetObjectItem(json, "filemd5");
			if(tempJson != NULL)
			{
				filemd5 = new TypeChar(tempJson->valuestring);
			}
			tempJson = cJSON_GetObjectItem(json, "filename");
			if(tempJson != NULL)
			{
				fileName = new TypeChar(tempJson->valuestring);
			}
			tempJson = cJSON_GetObjectItem(json, "filepath");
			if(tempJson != NULL)
			{
				filepath = new TypeChar(tempJson->valuestring);
			}
			tempJson = cJSON_GetObjectItem(json, "savepath");
			if(tempJson != NULL)
			{
				fileSavePath = new TypeChar(tempJson->valuestring);
			}
			uint32_t initPercent = 0;
			mCurrentPercent = 0;
			mPrintf(LOG_Robot, "ZNP download->json parse ok! ");
			if(fileName && filemd5 && filepath && fileSavePath)
			{
				//开始去下载文件
				uint32_t fileTotalLen = 0;//文件总长度
				int retInt = mfHttpDlFile(fileName->buff, filepath->buff, fileSavePath->buff, filemd5->buff, &fileTotalLen, NULL);
				//如果成功就开始下载
				if(retInt == 0)
				{
					//进来了最好多下几次
					int32_t downLoadCnt = 10;
					while(mIsDownLoadingFlag && downLoadCnt)
					{
						downLoadCnt--;
						mDownLoadTimeOut = false;
						//发送一条复位命令
						uint8_t resetSubCMD = 9;
						pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_AREQ | MT_RPC_SYS_SYS, MT_SYS_RESET_REQ, new TypeAFAttribute(&resetSubCMD, 1), 0));
						if(DUALZIGBEECHIP) {
							pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_AREQ | MT_RPC_SYS_SYS, MT_SYS_RESET_REQ, new TypeAFAttribute(&resetSubCMD, 1), 0));
						}
						sleep(2);//延时2s
						onTimerAdd(TIMER_NXP_DOWN_TIMEOUT, 5000, false, onDowLoadTimerOut, 0, 0);
						mPrintf(LOG_Robot, "download->into download! ");
						//正在下载
						mCurrentPercent = 0;
						uint32_t downLoadStatus = 0;
						//清除一下串口接收区数据
						TypeChar *sendBuff = new TypeChar(255);
						uint32_t  sendBuffLen = 0;
						uint8_t sendXorValue = 0;

						uint32_t fileIndex = 0;//当前文件下载位置
						//打开需要下载的文件
						mPrintf(LOG_Robot, "download->read %s! ", fileSavePath->buff);
						FILE * fileHanle = fopen(fileSavePath->buff, "rb");
						if(fileHanle)
						{
							//在这里边读取边下载
							//文件打开成功
							pmMasterSerialPort->reciveBuff->onClear();
							ssize_t masterLen = 0;
							ssize_t slaveLen = 0;
							read(pmMasterSerialPort->deviceHandle, pmMasterSerialPort->reciveBuff->ubuff, (size_t)pmMasterSerialPort->reciveBuff->size);
							if(DUALZIGBEECHIP) {
								pmSlaveSerialPort->reciveBuff->onClear();

								read(pmSlaveSerialPort->deviceHandle, pmSlaveSerialPort->reciveBuff->ubuff, (size_t)pmSlaveSerialPort->reciveBuff->size);
							}
							bool sendFlag = TRUE;
							TypeCMDRecicer *masterRecicer = new TypeCMDRecicer();
							TypeCMDRecicer *slaveReciver = new TypeCMDRecicer();
							int32_t tempTimeOunt = onGetTimeSec();
							uint32_t tempNeedSendLen = 0;
							mPrintf(LOG_Robot, "start send cmd ");
							while(mIsDownLoadingFlag && (!mDownLoadTimeOut) && (mCurrentPercent <= 100)) //循环读取数据
							{
								if(sendFlag)
								{
									sendBuff->onClear();
									sendBuffLen = 0;
									sendXorValue = 0;
									switch(downLoadStatus)
									{
										case 0:
										{
											fileIndex = 0;//从第一个数据开始发送
											sendBuff->ubuff[sendBuffLen++] = 0xFE;//SOF
											sendBuff->ubuff[sendBuffLen++] = 0x00;//LEN
											sendBuff->ubuff[sendBuffLen++] = 0x4D;//Frame ID
											sendBuff->ubuff[sendBuffLen++] = 0x04;//SB_HANDSHAKE_CMD
											sendXorValue = (uint8_t)(sendXorValue ^ sendBuff->ubuff[1] ^ sendBuff->ubuff[2] ^ sendBuff->ubuff[3]);
											sendBuff->ubuff[sendBuffLen++] = sendXorValue;
											downLoadStatus++;
											TypeChar *startLog = new TypeChar();
											mPrintf(LOG_Robot, "send %s ", onPrintfUBuff(sendBuff->ubuff, sendBuffLen, startLog->buff));
											delete startLog;
										}
											break;//SB_HANDSHAKE_CMD
										case 1://SB_WRITE_CMD
										{
											if(fileIndex < fileTotalLen)
											{
												tempNeedSendLen = 64;//
												sendBuff->ubuff[sendBuffLen++] = 0xFE;//SOF
												if(pDataBase->onGetChipType() == 2538)
												{
													if((fileTotalLen - fileIndex) < 64)
													{
														tempNeedSendLen = fileTotalLen - fileIndex;
													}
													sendBuff->ubuff[sendBuffLen++] = (uint8_t)(tempNeedSendLen + 8);//LEN
													sendBuff->ubuff[sendBuffLen++] = 0x4D;//Frame ID
													sendBuff->ubuff[sendBuffLen++] = 0x01;//SB_WRITE_CMD
													sendBuff->onAddInt32Ex(sendBuffLen, 0x00200000 + fileIndex);
													sendBuffLen += 4;
													sendBuff->onAddInt32Ex(sendBuffLen, tempNeedSendLen);
													sendBuffLen += 4;
													//具体数据区
													fread(&sendBuff->ubuff[sendBuffLen], sizeof(uint8_t), tempNeedSendLen, fileHanle);//读取文件
													sendBuffLen += tempNeedSendLen;//发送长度增加
												}
												else if(pDataBase->onGetChipType() == 2530)
												{
													sendBuff->ubuff[sendBuffLen++] = (uint8_t)(tempNeedSendLen + 2);//LEN
													sendBuff->ubuff[sendBuffLen++] = 0x4D;//Frame ID
													sendBuff->ubuff[sendBuffLen++] = 0x01;//SB_WRITE_CMD
													sendBuff->onAddInt16Ex(sendBuffLen, fileIndex / 4);
													sendBuffLen += 2;
													//具体数据区
													if((fileTotalLen - fileIndex) < 64)
													{
														tempNeedSendLen = fileTotalLen - fileIndex;
														for(int tail = tempNeedSendLen; tail < 64; tail++)
														{
															sendBuff->ubuff[sendBuffLen + tail] = 0xFF;
														}
													}
													fread(&sendBuff->ubuff[sendBuffLen], sizeof(uint8_t), tempNeedSendLen, fileHanle);//读取文件
													tempNeedSendLen = 64;
													sendBuffLen += tempNeedSendLen;//发送长度增加
												}
												fileIndex += tempNeedSendLen;//总长度增加
												for(uint32_t i = 1; i < sendBuffLen; ++i)
												{
													sendXorValue ^= sendBuff->ubuff[i];
												}
												sendBuff->ubuff[sendBuffLen++] = sendXorValue;
											}
											if(fileIndex >= fileTotalLen)
											{
												downLoadStatus++;
											}
										}
											break;
										case 2://SB_READ_CMD
										{
											//暂时不校验，直接通过
											downLoadStatus++;
										}
											break;
										case 3://SB_ENABLE_CMD
										{
											sendBuff->ubuff[sendBuffLen++] = 0xFE;//SOF
											sendBuff->ubuff[sendBuffLen++] = 0x00;//LEN
											sendBuff->ubuff[sendBuffLen++] = 0x4D;//Frame ID
											sendBuff->ubuff[sendBuffLen++] = 0x03;//SB_ENABLE_CMD
											sendXorValue = (uint8_t)(sendXorValue ^ sendBuff->ubuff[1] ^ sendBuff->ubuff[2] ^ sendBuff->ubuff[3]);
											sendBuff->ubuff[sendBuffLen++] = sendXorValue;
											downLoadStatus++;
										}
											break;
										default:
										{
											mCurrentPercent = 101;//下载成功
											sendBuffLen = 0;
											downLoadCnt = 0;
										}
											break;
									}
									if(sendBuffLen != 0)
									{
										//TypeChar *tempPrintfBuffs = new TypeChar();
										//mPrintf(LOG_Robot, "send %s ", onPrintfUBuff(sendBuff->ubuff, sendBuffLen, tempPrintfBuffs->buff));
										//delete tempPrintfBuffs;
										//发送主模块数据
										if(pmMasterSerialPort->deviceHandle > 0)
										{
											write(pmMasterSerialPort->deviceHandle, sendBuff->ubuff, sendBuffLen);
										}
										if(DUALZIGBEECHIP) {
											//发送从模块数据
											if(pmSlaveSerialPort->deviceHandle > 0)
											{
												write(pmSlaveSerialPort->deviceHandle, sendBuff->ubuff, sendBuffLen);
											}
										}
										sendFlag = FALSE;
										masterRecicer->onClear();
										slaveReciver->onClear();
										//计算一下百分比
										if(fileTotalLen)
										{
											int32_t tempPercent = (fileIndex * (100 - initPercent)) / fileTotalLen;
											if((mCurrentPercent <= 100) && ((tempPercent + initPercent) != mCurrentPercent))
											{
												mCurrentPercent = tempPercent + initPercent;
												mPrintf(LOG_Robot, "cc%04d DL %%%d M=%d S=%d ", pDataBase->onGetChipType(), mCurrentPercent, masterRecicer->isOK, slaveReciver->isOK);
												onTimerUpdate(TIMER_NXP_DOWN_TIMEOUT, 5000);
											}
										}
									}
									tempTimeOunt = onGetTimeSec();
								}
								else
								{
									//计算时间
									if((onGetTimeSec() - tempTimeOunt) > 1)
									{
										//超时重发
										downLoadStatus = 0;
										if(masterRecicer->isOK)
										{
											mPrintf(LOG_Robot, "主模块有返回! ");
										}
										if(slaveReciver->isOK)
										{
											mPrintf(LOG_Robot, "从模块有返回! ");
										}
										sendFlag = TRUE;
									}
									else
									{
										//读取模块命令应答  设计一个超时  失败就重新下载
										pmMasterSerialPort->reciveBuff->onClear();
										masterLen = read(pmMasterSerialPort->deviceHandle, pmMasterSerialPort->reciveBuff->ubuff, 250);
										//分析一下协议
										onCMDProcess(masterRecicer, pmMasterSerialPort->reciveBuff->ubuff, masterLen);
										if(DUALZIGBEECHIP) {
											pmSlaveSerialPort->reciveBuff->onClear();
											slaveLen = read(pmSlaveSerialPort->deviceHandle, pmSlaveSerialPort->reciveBuff->ubuff, 250);
											//分析一下协议
											onCMDProcess(slaveReciver, pmSlaveSerialPort->reciveBuff->ubuff, slaveLen);
											if(masterRecicer->isOK && slaveReciver->isOK)
											{
												sendFlag = TRUE;//发送下一帧数据
											}
										} else
										{
											if(masterRecicer->isOK)
											{
												sendFlag = TRUE;//发送下一帧数据
											}
										}

									}
								}
							}
							delete masterRecicer;
							delete slaveReciver;
							fclose(fileHanle);
						}
						else
						{
							mPrintf(LOG_Robot, "open file failed! ");
						}
						delete sendBuff;
						sleep(3);
					}
					//清除一下数据区
					read(pmMasterSerialPort->deviceHandle, pmMasterSerialPort->reciveBuff->ubuff, (size_t)pmMasterSerialPort->reciveBuff->size);
					pmMasterSerialPort->reciveBuff->onClear();
					if(DUALZIGBEECHIP) {
						read(pmSlaveSerialPort->deviceHandle, pmSlaveSerialPort->reciveBuff->ubuff, (size_t)pmSlaveSerialPort->reciveBuff->size);
						pmSlaveSerialPort->reciveBuff->onClear();
					}
				}
				else
				{
					mPrintf(LOG_Robot, "file download failed! ");
				}
			}
			delete fileName;
			delete filemd5;
			delete filepath;
			delete fileSavePath;
		}
		cJSON_Delete(json);
		mPrintf(LOG_Robot, "download->out thread! ");
		mIsDownLoadingFlag = false;
	}
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

static bool onCMDProcess(TypeCMDRecicer *reciver, uint8_t *buff, int len)
{
	int tempIndex = 0;
	uint8_t ch = 0;
	bool retBool = reciver->isOK;
	if(len > 0)
	{
		while(tempIndex < len)
		{
			ch = buff[tempIndex++];
			retBool = reciver->isOK = FALSE;
			switch (reciver->sbSte)
			{
				case SB_SOF_STATE:
					if (0xFE == ch)
					{
						reciver->sbSte = SB_LEN_STATE;
						reciver->sbIdx = 0;
					}
					break;

				case SB_LEN_STATE: // this field is kept for backward compatibility of the protocol.
					// In case the length is larger than 254, this fields is set to 0xFF,
					// and there is an additional 32bit length, located in another location
					// in this header.
					reciver->sbLen = ch;
					reciver->sbFcs = 0;
					reciver->sbSte = SB_FRAME_ID_STATE;
					break;

				case SB_FRAME_ID_STATE:
					reciver->sbFrameId = ch;
					reciver->sbSte = SB_CMD_STATE;
					break;

				case SB_CMD_STATE:
					reciver->sbCmd = ch;

					switch (reciver->sbLen)
					{
						case 0:
							reciver->sbSte = SB_FCS_STATE;
							break;
						case 0xFF:
							reciver->sbSte = SB_LEN1_STATE;
							break;
						default:
							reciver->sbSte = SB_DATA_STATE;
							break;
					}
					break;

				case SB_LEN1_STATE:
					reciver->sbLen = ch;
					reciver->sbSte = SB_LEN2_STATE;
					break;

				case SB_LEN2_STATE:
					reciver->sbLen += ch << 8;
					reciver->sbSte = SB_LEN3_STATE;
					break;

				case SB_LEN3_STATE:
					reciver->sbLen += ch << 16;
					reciver->sbSte = SB_LEN4_STATE;
					break;

				case SB_LEN4_STATE:
					reciver->sbLen += ch << 24;
					reciver->sbSte = (uint8_t)((reciver->sbLen) ? SB_DATA_STATE : SB_FCS_STATE);
					break;

				case SB_DATA_STATE:
					if (reciver->sbIdx >= 255)
					{
						reciver->sbSte = SB_SOF_STATE; //discard this packet. the payload is too long.
					}
					else
					{
						reciver->sbBuf[reciver->sbIdx++] = ch;

						if (reciver->sbIdx == reciver->sbLen)
						{
							reciver->sbSte = SB_FCS_STATE;
						}
					}
					break;

				case SB_FCS_STATE:
					if ((reciver->sbFcs == ch) && (reciver->sbFrameId == 0x4D))
					{
						//rtrn = sbCmnd(sbCmd, sbLen);
						retBool = reciver->isOK = TRUE;
						//TypeChar *tempPrintfmr = new TypeChar();
						//mPrintf(LOG_Robot, "R:%s ", onPrintfUBuff(buff, len, tempPrintfmr->buff));
						//delete tempPrintfmr;
					}

					reciver->sbSte = SB_SOF_STATE;
					break;

				default:
					break;
			}
			reciver->sbFcs ^= ch;
		}
	}
	return retBool;
}

static void onDowLoadTimerOut(int par1, int par2)
{
	if(!mDownLoadTimeOut)
	{
		mDownLoadTimeOut = true;
		mPrintf(LOG_Robot, "download timeout! ");
	}
}

void *mfAppDownLoadThread(void *arg)
{
	//发送一些状态通知
	onUpdateDLStatus(OTA_UPGRADE_STAGE_START, 0, "开始更新！");
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	//解析jason
	cJSON *json = cJSON_Parse(tempThreadInfo->threadPara->buff);
	if(json)
	{
		cJSON *tempJson;
		TypeChar *fileName = NULL;
		TypeChar *filemd5 = NULL;
		TypeChar *filepath = NULL;
		TypeChar *fileSavePath = NULL;
		TypeChar *fileVer = NULL;
		tempJson = cJSON_GetObjectItem(json, "filemd5");
		if(tempJson != NULL)
		{
			filemd5 = new TypeChar(tempJson->valuestring);
		}
		tempJson = cJSON_GetObjectItem(json, "filename");
		if(tempJson != NULL)
		{
			fileName = new TypeChar(tempJson->valuestring);
		}
		tempJson = cJSON_GetObjectItem(json, "filepath");
		if(tempJson != NULL)
		{
			filepath = new TypeChar(tempJson->valuestring);
		}
		tempJson = cJSON_GetObjectItem(json, "savepath");
		if(tempJson != NULL)
		{
			fileSavePath = new TypeChar(tempJson->valuestring);
		}
		tempJson = cJSON_GetObjectItem(json, "filever");
		if(tempJson != NULL)
		{
			fileVer = new TypeChar(tempJson->valuestring);
		}
		mCurrentPercent = 0;
		mPrintf(LOG_Robot, "App download->json parse ok! ");
		if(fileName && filemd5 && filepath && fileSavePath)
		{
			//开始去下载文件
			onUpdateDLStatus(OTA_UPGRADE_STAGE_DOWNLOADING, 5, "开始下载！");
			uint32_t fileTotalLen = 0;//文件总长度
			int retInt = mfHttpDlFile(fileName->buff, filepath->buff, fileSavePath->buff, filemd5->buff, &fileTotalLen, NULL);
			//如果成功就开始下载
			if(retInt == 0)
			{
				onUpdateDLStatus(OTA_UPGRADE_STAGE_DOWNLOADED, 35, "下载成功!");
				TypeChar *tempChars = new TypeChar();
				//开始更新
				//1、删除以前文件
				sprintf(tempChars->buff, "rm /usr/bin/%s", fileName->buff);//我有没有权限直接删除
				system(tempChars->buff);
				//2、复制文件到 /user/bin/目录下
				sprintf(tempChars->buff, "cp %s /usr/bin/%s", fileSavePath->buff, fileName->buff);
				system(tempChars->buff);
				//3、添加权限
				sprintf(tempChars->buff, "chmod a+x /usr/bin/%s", fileName->buff);
				system(tempChars->buff);
				onUpdateDLStatus(OTA_UPGRADE_STAGE_DONE, 100, "升级完成!");
				//更新一下网关版本
				if(fileVer)
				{
					//更新软件版本
					ModifyGatewayInfoRequest gatewayInfo;
					gatewayInfo.set_gateway_id(pDataBase->onGetGateway_ID());
					gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_SW_VERSION);
					gatewayInfo.set_sw_version(fileVer->buff);
					mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
				}
				sleep(1);//延时1s后再启动
				//4、重新启动应用程序  //怎么重启?
				system("reboot");
			}
			else
			{
				onUpdateDLStatus(OTA_UPGRADE_STAGE_FAILED, 5, "下载失败!");
			}
		}
		else
		{
			onUpdateDLStatus(OTA_UPGRADE_STAGE_FAILED, 0, "升级参数不正确!");
		}
		if(fileName) delete fileName;
		if(filemd5) delete filemd5;
		if(filepath) delete filepath;
		if(fileSavePath) delete fileSavePath;
		if(fileVer) delete fileVer;
	}
	delete json;
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

bool onFindThreadTitle(char *str)
{
	TypeThreadInfo *tempThreadInfo = NULL;
	for(int i = 0; i < mThreadInfoList->size(); ++ i)
	{
		tempThreadInfo = (TypeThreadInfo *)mThreadInfoList->get(i);
		if(tempThreadInfo && tempThreadInfo->title->onStringCMP(str))
		{
			//找到了
			return TRUE;
		}
	}
	return FALSE;
}
