//
// Created by xia_w on 2017/12/10.
//
#include     <sys/stat.h>
#include <errno.h>
#include "../Main/WinobleMain.h"
#include "../DataType/TypeDefine.h"

static void onTimerCoordinatorStatusCB(int par1, int par2);
TypeSerialDrive::TypeSerialDrive(const char *name, int baud, bool ismaster)
{
	mMemNewFreeCount++;
	bool initFlag = TRUE;
	isError = 0;
	isMasterFlag = ismaster;
	deviceProces = new TypeSerialProces(ismaster);
	deviceName = new TypeChar((char *)name);
	reciveBuff = new TypeChar(1024);
	deviceBaud = baud;
	getSrcEntryTime = 0;
	startNetFail = 0;
	checkDrivceErrorCnt = 0;
	pLastSendCMD = NULL;//没有任何命令可以发送
	pCMDSendList = new TypeLinkedList(ArrayTypeSerialProces);

	pDelaySendList = new TypeArrayList(ArrayTypeSerialProces);
	mPrintf(ismaster, "opening %s ", deviceName->buff);
	deviceHandle = open(deviceName->buff, O_RDWR);//读写方式打开
	if(deviceHandle == -1)
	{
		mPrintf(ismaster, "Error:%s SerialPort open failue! ", deviceName->buff);
		initFlag = FALSE;
	}
	else
	{
		mPrintf(ismaster, "%s SerialPort open OK! ", deviceName->buff);
	}
	if(initFlag)
	{
		struct termios newtio, oldtio;
		if (tcgetattr(deviceHandle, &oldtio) == 0)
		{
			bzero(&newtio, sizeof(newtio));
			newtio.c_cflag &= ~CSTOPB;
			newtio.c_cflag &= ~CSIZE;
			newtio.c_cflag |= (CLOCAL | CREAD);
			newtio.c_cflag &= ~CRTSCTS;

			/* set no software stream control */
			newtio.c_iflag &= ~(IXON | INLCR | ICRNL | IGNCR);
			/* set output mode with no define*/
			newtio.c_oflag &= ~OPOST;
			/* set input mode with non-format */
			newtio.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
			newtio.c_iflag |= IGNBRK|IGNPAR; //for 0xd,0x11,0x13
			newtio.c_cflag |= CS8;//8 bit
			newtio.c_cflag &= ~PARENB;//none

			switch (deviceBaud) {
				case 2400:
					cfsetispeed(&newtio, B2400);
					cfsetospeed(&newtio, B2400);
					break;
				case 4800:
					cfsetispeed(&newtio, B4800);
					cfsetospeed(&newtio, B4800);
					break;
				case 9600:
					cfsetispeed(&newtio, B9600);
					cfsetospeed(&newtio, B9600);
					break;
				case 19200:
					cfsetispeed(&newtio, B19200);
					cfsetospeed(&newtio, B19200);
					break;
				case 38400:
					cfsetispeed(&newtio, B38400);
					cfsetospeed(&newtio, B38400);
					break;
				case 115200:
					cfsetispeed(&newtio, B115200);
					cfsetospeed(&newtio, B115200);
					break;
				default:
					cfsetispeed(&newtio, B9600);
					cfsetospeed(&newtio, B9600);
					break;
			}
			newtio.c_cflag &= ~CSTOPB;//1 bit stop
			newtio.c_cc[VTIME] = 0;
			newtio.c_cc[VMIN] = 0;
			tcflush(deviceHandle, TCIOFLUSH);
			if (tcsetattr(deviceHandle, TCSANOW, &newtio) != 0)
			{
				initFlag = FALSE;
			}
		}
		else
		{
			initFlag = FALSE;
		}
	}
	if(initFlag == FALSE)
	{
		mPrintf(ismaster, "Error:%s serial init failed ", deviceName->buff);
		close(deviceHandle);
		deviceHandle = -1;
	}
	else
	{
		mPrintf(ismaster, "%s serial init OK ", deviceName->buff);
		//读取一下cc2538软件版本
		//onStartNewNetWork(0);
		onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_VERSION, NULL, 0));
		onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
		//创建一个定时器，定时去检查 当前网络状态 和允许组网的状态
		onTimerAdd(ismaster ? TIMER_MASTER_STATUS_CHECK : TIMER_SLAVE_STATUS_CHECK, 30000, TRUE, onTimerCoordinatorStatusCB, ismaster, 0);
	}
}

static void onTimerCoordinatorStatusCB(int par1, int par2)
{
	//定时发送网络状态检查 10s/次
	if(par1)
	{
		pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
		if(pmMasterSerialPort->checkDrivceErrorCnt < 10) pmMasterSerialPort->checkDrivceErrorCnt++;
		//检查一下关联表
		//pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_GET_INVAID_SUB_DEVLIST, NULL, 0));
	}
	else
	{
		if(DUALZIGBEECHIP) {
			pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_NWK_INFO, NULL, 0));
			if(pmSlaveSerialPort->checkDrivceErrorCnt < 10) pmSlaveSerialPort->checkDrivceErrorCnt++;
			//检查一下关联表
			//pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_APP, MT_GET_INVAID_SUB_DEVLIST, NULL, 0));
		}
	}
}

uint32_t TypeSerialDrive::onGetSendCount()
{
	return pCMDSendList->onGetCount();
}

void TypeSerialDrive::onWriteCMD(TypeSerialProces *proces)
{
	if(deviceHandle == -1)
	{
		delete proces;
		return;
	}
	if(proces != NULL)
	{
		proces->isMasterFlag = isMasterFlag;
		if(proces->delayTime > 0)
		{
			pDelaySendList->add(proces);
		}
		else
		{
			//写无线命令的时候要判断 当前指令中，是否有类似功能指令。否则就删除原有指令
			if(proces->onIsZigbeeCMD())
			{
                if(proces->shortAddr == 0)
                {
                    //短地址为零是无效的无线命令。直接删除
                    delete proces;
                }
                else
                {
                    //遍历一遍 是否有重复
	                pCMDSendList->onDeleteSame(ArrayTypeSerialProces, proces);
                    //添加这条指令
	                pCMDSendList->add(proces);
                }
			}
			else
			{
				//是直接发给网关的命令，不要延时  直接处理
				onWriteData(proces);
				delete proces;
			}
		}
	}
}

void TypeSerialDrive::onAddDelayReSend(TypeSerialProces *proces, int32_t delaytime)
{
	if(proces->onIsZigbeeCMD() && pDeviceList && pDeviceList->onCheckDevOnLine(proces->isMasterFlag, proces->shortAddr))
	{
		TypeSerialProces *sendProces = NULL;
		TypeAFAttribute * sendAFAttribute = NULL;
		TypeZclProfile *sendZclProfile = NULL;
		if(proces->afAttribute != NULL)
		{
			if(proces->afAttribute->sendZcl != NULL)
			{
				sendZclProfile = new TypeZclProfile(proces->afAttribute->sendZcl->toBuff->ubuff, proces->afAttribute->sendZcl->toBuffLen);
			}
			sendAFAttribute = new TypeAFAttribute(proces->shortAddr, proces->afAttribute->desEndPoint, proces->afAttribute->clusterID, sendZclProfile);
			//把序列号改一下
			sendAFAttribute->afSeqNum = proces->afAttribute->afSeqNum;
			if(sendAFAttribute->toDataLen > 6)
			{
				sendAFAttribute->toDataBuff->ubuff[6] = sendAFAttribute->afSeqNum;
			}
		}
		sendProces = new TypeSerialProces(pLastSendCMD->shortAddr, pLastSendCMD->cmdType | pLastSendCMD->subSystem, pLastSendCMD->subCMD, sendAFAttribute, delaytime);
		sendProces->repeatCount = proces->repeatCount;
		sendProces->reSendBit = proces->reSendBit;//标记是重发的命令
		onWriteCMD(sendProces);
	}
}

void TypeSerialDrive::onSetSendStatus(uint8_t status, uint8_t type)
{
	if(pLastSendCMD != NULL)
	{
		pLastSendCMD->onPrintfError(status, type);
		if(type == 1)//res
		{
			if(status == 0)
			{
				//成功
				if(isError > 0) isError--;
				pLastSendCMD->retStatus = SEND_WAIT_CONFIRM;
				pLastSendCMD->delayTime = 200;
			}
			else
			{
				//打印一下错误 设备短地址  cluster ID
				if(isError < 10) isError++;
				if(pLastSendCMD->repeatCount > 0)
				{
					pLastSendCMD->repeatCount --;
					pLastSendCMD->retStatus = SEND_DELETE;//满了就等一下再发下一条
					if(status == 0xCD)
					{
						pLastSendCMD->reSendBit = 1;//标记成重发命令
						onAddDelayReSend(pLastSendCMD, 1000);//添加到延时发送列表
						pLastSendCMD->delayTime = 100;
					}
					else if(status == 0x11)
					{
						//重新添加到新的延时列表，防止卡顿
						pLastSendCMD->reSendBit = 1;//标记成重发命令
						onAddDelayReSend(pLastSendCMD, 1000);//添加到延时发送列表
						//mac层数据队列满了
						pLastSendCMD->delayTime = 500;
					}
					else
					{
						//重新添加到新的延时列表，防止卡顿
						pLastSendCMD->reSendBit = 1;//标记成重发命令
						onAddDelayReSend(pLastSendCMD, 1000);//添加到延时发送列表
						pLastSendCMD->delayTime = 100;
					}
				}
				else
				{
					pLastSendCMD->retStatus = SEND_DELETE;//立即删除
					pLastSendCMD->delayTime = 0;
				}
			}
		}
		else if(type == 2)//comfirm
		{
			if(status == 0)
			{
				if(isError > 0) isError--;
				pLastSendCMD->retStatus = SEND_DELETE;//立即删除
				pLastSendCMD->delayTime = 0;
			}//mPrintf(Log_Error, "%sR 写属性有错误返回!=%02x id=%02x", isMasterFlag ? "M" : "S", afAttribute->toDataBuff->ubuff[0], afAttribute->toDataBuff->ubuff[2]);
			else
			{
				if(isError < 10) isError++;
				//打印一下错误 设备短地址  cluster ID
				if(pLastSendCMD->repeatCount > 0)
				{
					pLastSendCMD->repeatCount --;
					TypeDBDeviceInfo *tempDBDeviceInfo = NULL;
					if(isMasterFlag)
					{
						tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IShortAddr, pLastSendCMD->shortAddr);
						if(tempDBDeviceInfo)//只要出现错误，我就检查一下路由表
						{
							tempDBDeviceInfo->checkSrcRouter = TRUE;
						}
					}
					else
					{
						tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IShortAddr_Ex, pLastSendCMD->shortAddr);
						if(tempDBDeviceInfo)//只要出现错误，我就检查一下路由表
						{
							tempDBDeviceInfo->checkSrcRouterEx = TRUE;
						}
					}
					//0xF1 mac 数据缓存区已满
					//0xE9 mac 没有应答  1s后重新发送  一般就能成功  设备不在线也报的是这个错误 所以，延时1s后发送  发送3次不成功可以标志设备不在线了
					//0xCD 找不到路由
					if(status == 0xF1)
					{
						pLastSendCMD->retStatus = SEND_INIT;//缓存区满的话，我就延时500ms再发送
						pLastSendCMD->delayTime = 500;
					}
					else if(status == 0xE9)
					{
						//查找一下这个设备是否在线
						if(tempDBDeviceInfo)
						{
							if(tempDBDeviceInfo->onLineFlag.bits.status)
							{
								pLastSendCMD->reSendBit = 1;//标记成重发命令
								onAddDelayReSend(pLastSendCMD, 2000);//添加到延时发送列表
								pLastSendCMD->delayTime = 100;
							}
							else
							{
								pLastSendCMD->retStatus = SEND_DELETE;//设备真的不在线，不再重发了
								pLastSendCMD->delayTime = 0;
							}
						}
					}
					if(status == 0xCD)
					{
						//由于这个是广播所以在一定时间内只能发送一次
						//路由请求失败，久延时一下等待新的路由请求成功后再发送
						TypeChar *route_discChars = new TypeChar(4);
						route_discChars->onAddInt16Ex(0, SHORTADDR_BROADCAST);
						route_discChars->ubuff[2] = 1;//Router option
						route_discChars->ubuff[3] = 0;//Broadcast radius
						if(isMasterFlag)
						{
							if(pmMasterSerialPort->getSrcEntryTime <= 0)
							{
								pmMasterSerialPort->getSrcEntryTime = 10;
								pmMasterSerialPort->onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_AREQ | MT_RPC_SYS_ZDO, MT_ZDO_EXT_ROUTE_DISC, new TypeAFAttribute(route_discChars->ubuff, 4), 0));
							}
						}
						else
						{
							if(DUALZIGBEECHIP)
							{
								if (pmSlaveSerialPort->getSrcEntryTime <= 0)
								{
									pmSlaveSerialPort->getSrcEntryTime = 30;
									pmSlaveSerialPort->onWriteCMD(new TypeSerialProces(0x0000,MT_RPC_CMD_AREQ |MT_RPC_SYS_ZDO,MT_ZDO_EXT_ROUTE_DISC,new TypeAFAttribute(route_discChars->ubuff, 4), 0));
								}
							}

						}
						delete route_discChars;
						pLastSendCMD->reSendBit = 2;//标记正在路由请求
						onAddDelayReSend(pLastSendCMD, 5000);//添加到延时发送列表 5s后再发送这次命令
						pLastSendCMD->retStatus = SEND_DELETE;//延时200ms再发送下一条命令  毕竟 还是比较忙的
						pLastSendCMD->delayTime = 200;
					}
					else
					{
						//重新添加到新的延时列表，防止卡顿
						pLastSendCMD->reSendBit = 1;//标记成重发命令
						onAddDelayReSend(pLastSendCMD, 1000);//添加到延时发送列表
						pLastSendCMD->retStatus = SEND_DELETE;//延时200ms再发送下一条命令  毕竟 还是比较忙的
						pLastSendCMD->delayTime = 100;
					}
				}
				else
				{
					pLastSendCMD->retStatus = SEND_DELETE;//立即删除
					pLastSendCMD->delayTime = 0;
				}
			}
		}
		else if(type == 3)//其它命令应答
		{
			pLastSendCMD->retStatus = SEND_DELETE;//立即删除
			pLastSendCMD->delayTime = 0;
		}
	}
}

void TypeSerialDrive::onCheckSendCMD(int mstime)
{
	TypeSerialProces * sendCMD = NULL;
	//检查延时列表
	for (int i = 0; i < pDelaySendList->size();)
	{
		sendCMD = (TypeSerialProces *)pDelaySendList->get(i);
		if(sendCMD != NULL)
		{
			if(sendCMD->onCheckTime(mstime))
			{
				pDelaySendList->UnFreeRemoveObject(sendCMD);
				onWriteCMD(sendCMD);
				continue;
			}
		}
		i++;
	}

	if(pLastSendCMD == NULL)
	{
		//取一条命令发送出去
		if(pCMDSendList->onGetCount() > 0)
		{
			pLastSendCMD = (TypeSerialProces *)pCMDSendList->get();
		}
	}

	if(pLastSendCMD)
	{
		pLastSendCMD->delayTime -= mstime;
		if(pLastSendCMD->delayTime <= 0)
		{
			switch(pLastSendCMD->retStatus)
			{
				case SEND_INIT:
					if(deviceHandle > 0)
					{
						onWriteData(pLastSendCMD);
						if((pLastSendCMD->subSystem == MT_RPC_SYS_AF) && (pLastSendCMD->subCMD == MT_AF_DATA_REQUEST))
						{
							pLastSendCMD->retStatus = SEND_WAIT_RES;
							pLastSendCMD->delayTime = 1000;
						}
						else
						{
							pLastSendCMD->retStatus = SEND_WAIT_RES;
							pLastSendCMD->delayTime = 200;//其它命令等待200ms再发送下一条命令
						}
					}
					else
					{
						mPrintf(isMasterFlag, "Error 串口没有工作! ");
						pLastSendCMD->retStatus = SEND_DELETE;
					}
					break;//send
				case SEND_WAIT_RES: pLastSendCMD->retStatus = SEND_DELETE; break;//到这里来了肯定是超时了，直接delete
				case SEND_WAIT_CONFIRM:pLastSendCMD->retStatus = SEND_DELETE;break;//到这里来了肯定是超时了，直接delete
				case SEND_DELETE:
					if(pLastSendCMD)
					{
						delete pLastSendCMD;
					}
					pLastSendCMD = NULL;
					break;
			}
		}
	}
}

void TypeSerialDrive::onWriteData(TypeSerialProces *proces)
{
	if(proces)
	{
		uint8_t tempLen = 0;
		if(proces->afAttribute != NULL)
		{
			tempLen = proces->afAttribute->toDataLen;
		}
		TypeChar *sendBuff = new TypeChar(tempLen + 5);
		sendBuff->ubuff[0] = 0xFE;
		sendBuff->ubuff[1] = tempLen;
		sendBuff->ubuff[2] = (uint8_t)(proces->cmdType | proces->subSystem);
		sendBuff->ubuff[3] = proces->subCMD;
		if(tempLen > 0)
		{
			memcpy(&sendBuff->ubuff[4], proces->afAttribute->toDataBuff->ubuff, tempLen);
		}

		//填写 xor
		proces->checkSum = 0;
		for(int i = 1; i < tempLen + 4; ++i)
		{
			proces->checkSum ^= sendBuff->ubuff[i];
		}
		sendBuff->ubuff[tempLen + 4] = proces->checkSum;
		if(deviceHandle > 0)
		{
			write(deviceHandle, sendBuff->ubuff,(size_t)(tempLen + 5));
		}
		proces->toString(TRUE);
		delete sendBuff;
	}
}

void TypeSerialDrive::onReviceData(TypeSerialProces *proces)
{
	if(deviceHandle > 0)
	{
		reciveBuff->onClear();
		ssize_t len = read(deviceHandle, reciveBuff->ubuff, (size_t)reciveBuff->size);
		if(len > 0)
		{
//			mPrintf(isMasterFlag, "air status len in");
			//mPrintf(Log_Error, "%sR recive len = %d", isMasterFlag ? "M" : "S", len);
			ssize_t tempLen = 0;
			uint8_t tempChar = 0;
			while(tempLen < len)
			{
				tempChar = reciveBuff->ubuff[tempLen++];
				switch(proces->status)
				{
					case 0:if(tempChar == 0xFE){proces->onClear(); proces->status = 1;}break;
					case 1:
						if(proces->afAttribute != NULL)
						{
							delete proces->afAttribute;
						}
						proces->afAttribute = new TypeAFAttribute(NULL, tempChar);
						proces->status = 2;
						proces->checkSum ^= tempChar;
						break;
					case 2:
						proces->cmdType = (uint8_t)(tempChar & 0xE0);
						proces->subSystem = (uint8_t)(tempChar & 0x1F);
						proces->status = 3; proces->checkSum ^= tempChar;
						break;
					case 3:proces->subCMD = tempChar; proces->status = 4; proces->checkSum ^= tempChar;break;
					default:
					{
						if(proces->afAttribute != NULL)
						{
							if(proces->status < (proces->afAttribute->toDataLen + 4))
							{
								proces->afAttribute->toDataBuff->ubuff[proces->status - 4] = tempChar;
								proces->status++;
								proces->checkSum ^= tempChar;
							}
							else
							{
								//结束了
								if(proces->checkSum == tempChar)
								{
									//这是一条正确的命令 请解析
//									mPrintf(isMasterFlag, "air status check success");
									proces->toString(FALSE);
									proces->onProces();
								}
								else
								{
									mPrintf(isMasterFlag, "Error:serial cmd check error@ ");
								}
								proces->status = 0;
								if(proces->afAttribute != NULL)
								{
									delete proces->afAttribute;
								}
								proces->afAttribute = NULL;
							}
						}
							//不加else 下有bug
						else
						{
							proces->status = 0;
						}
					}
					break;
				}
			}
		}
	}
}

int TypeSerialDrive::onStartNewNetWork(bool flag)
{
	TypeChar *pSendChar = new TypeChar(32);
	//设置一下连接密匙
	uint8_t *pTCLinkKey = NULL;
	uint32_t defaultChannles = 0;

	uint8_t defaultMasterTCLinkKey[SEC_KEY_LEN] = DEFAULT_TC_MASTER_LINK_KEY;
	uint8_t defaultSlaveTCLinkKey[SEC_KEY_LEN] = DEFAULT_TC_SLAVE_LINK_KEY;
	pSendChar->onClear();
	if(isMasterFlag)
	{
		pTCLinkKey = defaultMasterTCLinkKey;
		defaultChannles = DEFAULT_TC_MASTER_CHANNEL;
	}
	else
	{
		pTCLinkKey = defaultSlaveTCLinkKey;
		defaultChannles = DEFAULT_TC_SLAVE_CHANNEL;
	}
	//设置一下连接密匙
	pSendChar->onClear();
	pSendChar->onAddInt16Ex(0, 0x0101);
	pSendChar->ubuff[2] = 8;
	pSendChar->ubuff[3] = SEC_KEY_LEN;
	pSendChar->onAddUBuff(4, pTCLinkKey, SEC_KEY_LEN);
	onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_OSAL_NV_WRITE, new TypeAFAttribute(pSendChar->ubuff, SEC_KEY_LEN + 4), 0));
	//先修改一下channel
	pSendChar->onClear();
	pSendChar->onAddInt32Ex(0, defaultChannles);
	onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_UTIL, MT_UTIL_SET_CHANNELS, new TypeAFAttribute(pSendChar->ubuff, 4), 0));
	//先修改一下PANID
	pSendChar->onClear();
	pSendChar->onAddInt16Ex(0, 0xFFFF);
	onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_UTIL, MT_UTIL_SET_PANID, new TypeAFAttribute(pSendChar->ubuff, 2), 0));
	//设置成源路由模式
	pSendChar->onClear();
	pSendChar->onAddInt16Ex(0, 0x0032);
	pSendChar->ubuff[2] = 0;
	pSendChar->ubuff[3] = 1;
	pSendChar->ubuff[4] = 1;//使能源路由
	onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_OSAL_NV_WRITE, new TypeAFAttribute(pSendChar->ubuff, 5), 0));
	//使能源路由表
	pSendChar->onClear();
	pSendChar->onAddInt16Ex(0, 0x0036);
	pSendChar->ubuff[2] = 0;
	pSendChar->ubuff[3] = 1;
	pSendChar->ubuff[4] = 1;//使能源路由表
	onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_OSAL_NV_WRITE, new TypeAFAttribute(pSendChar->ubuff, 5), 0));
	//允许通过短地址获取IEEE
	pSendChar->onClear();
	pSendChar->onAddInt16Ex(0, 0x008F);
	pSendChar->ubuff[2] = 0;
	pSendChar->ubuff[3] = 1;
	pSendChar->ubuff[4] = 1;
	onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_OSAL_NV_WRITE, new TypeAFAttribute(pSendChar->ubuff, 5), 0));
	if(flag)
	{
		//先修改一下网络启动模式
		pSendChar->onClear();
		pSendChar->onAddInt16Ex(0, 0x0003);
		pSendChar->ubuff[2] = 0;
		pSendChar->ubuff[3] = 1;
		pSendChar->ubuff[4] = ZCD_STARTOPT_DEFAULT_NETWORK_STATE;
		onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SYS, MT_SYS_OSAL_NV_WRITE, new TypeAFAttribute(pSendChar->ubuff, 5), 0));
		//重新创建新的网络
		onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_SYS_RESET, NULL, 500));
	}
	else
	{
		onWriteCMD(new TypeSerialProces(0x0000, MT_RPC_CMD_SREQ | MT_RPC_SYS_SAPI, MT_SAPI_START_REQ, NULL, 100));
	}
	delete pSendChar;
	startNetFail = onGetTimeSec();
	return 0;
}

int TypeSerialDrive::onLeaveWithIEEE(int32_t shortaddr ,int64_t ieee)
{
	if(pDataBase->onGetStatus() == HWELLYI_DB_VER)//数据库中没有这份白名单  直接离网并删除，并删除白名单
	{
		if(shortaddr && ieee)
		{
			TypeChar *deleteSend = new TypeChar(11);
			deleteSend->onAddInt16Ex(0, shortaddr);
			deleteSend->onAddInt64Ex(2 ,ieee);
			deleteSend->ubuff[10] = 0;
			onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_ZDO, MT_ZDO_MGMT_LEAVE_REQ, new TypeAFAttribute(deleteSend->ubuff, 11), 0));
			delete deleteSend;
			mPrintf(Log_NetWork ,"%sS 白名单已删除，让设备离网!(%04x),leave! ", isMasterFlag ? "M" : "S", (uint16_t) shortaddr);
			//这里等待100ms

		}
	}
	return 0;
}

/*
int TypeSerialDrive::onWriteAttributeNoRsp(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, TypeZclAttribute *attribute, uint32_t delaytime)
{
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(TRUE, ZCL_CMD_WRITE_NO_RSP, attribute));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	return 0;
}
 */

int TypeSerialDrive::onWriteAttribute(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, TypeZclAttribute *attribute, uint32_t delaytime)
{
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(TRUE, ZCL_CMD_WRITE, attribute));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	return 0;
}

int TypeSerialDrive::onWriteAttributeGeneric(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, TypeZclAttribute *attribute, uint32_t delaytime)
{
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(FALSE, ZCL_CMD_WRITE, attribute));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	return 0;
}

int TypeSerialDrive::onDefaultRspGeneric(uint8_t srcpoint, uint32_t shortaddr, uint32_t clusterid, uint8_t seqnum, uint8_t commandid, uint8_t status)
{
	bool manuFlag = FALSE;
	uint8_t sendLen = 0;
	if(clusterid == CLUSTER_ID_PERSONAL)//私有cluster
	{
		manuFlag = TRUE;//需要添加厂家ID
	}
	TypeChar *zclBuff = new TypeChar(7);
	TypeZclHead *zclHead = new TypeZclHead(ZCL_FRAME_TYPE_PROFILE_CMD, (uint8_t)manuFlag, ZCL_FRAME_CLIENT_SERVER_DIR, TRUE);
	zclBuff->ubuff[sendLen++] = zclHead->onToData();
	if(zclHead->manuSpecific)
	{
		zclBuff->onAddInt16Ex(sendLen, ZCL_MANUSPCIFICID);
		sendLen += 2;
	}
	zclBuff->ubuff[sendLen++] = seqnum;
	zclBuff->ubuff[sendLen++] = ZCL_CMD_DEFAULT_RSP;
	zclBuff->ubuff[sendLen++] = commandid;
	zclBuff->ubuff[sendLen++] = status;
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(zclBuff->ubuff, sendLen));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, 0));
	delete zclBuff;
	delete zclHead;
	return 0;
}

int TypeSerialDrive::onWriteZclCMD(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint8_t commandid, uint8_t *databuff, uint8_t datalen, uint32_t delaytime)
{
	bool manuFlag = FALSE;
	uint8_t sendLen = 0;
	if(clusterid == CLUSTER_ID_PERSONAL)//私有cluster
	{
		manuFlag = TRUE;//需要添加厂家ID
	}
	TypeChar *zclBuff = new TypeChar(datalen + 5);
	TypeZclHead *zclHead = new TypeZclHead(ZCL_FRAME_TYPE_SPECIFIC_CMD, (uint8_t)manuFlag, ZCL_FRAME_CLIENT_SERVER_DIR, FALSE);
	zclBuff->ubuff[sendLen++] = zclHead->onToData();
	if(zclHead->manuSpecific)
	{
		zclBuff->onAddInt16Ex(sendLen, ZCL_MANUSPCIFICID);
		sendLen += 2;
	}
	zclBuff->ubuff[sendLen++] = onGetZclSendSeq();
	zclBuff->ubuff[sendLen++] = commandid;
	//添加数据
	zclBuff->onAddUBuff(sendLen, databuff, datalen);
	sendLen += datalen;
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(zclBuff->ubuff, sendLen));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	delete zclBuff;
	delete zclHead;
	return 0;
}

int TypeSerialDrive::onAirDLCMD(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint8_t commandid, uint8_t rspseq, uint8_t *databuff, uint8_t datalen, int32_t delaytime)
{
	uint8_t sendLen = 0;
	TypeChar *zclBuff = new TypeChar(datalen + 5);
	TypeZclHead *zclHead = new TypeZclHead(ZCL_FRAME_TYPE_SPECIFIC_CMD, (uint8_t)FALSE, ZCL_FRAME_SERVER_CLIENT_DIR, FALSE);
	zclBuff->ubuff[sendLen++] = zclHead->onToData();
	if(zclHead->manuSpecific)
	{
		zclBuff->onAddInt16Ex(sendLen, ZCL_MANUSPCIFICID);
		sendLen += 2;
	}
	zclBuff->ubuff[sendLen++] = rspseq;
	zclBuff->ubuff[sendLen++] = commandid;
	//添加数据
	zclBuff->onAddUBuff(sendLen, databuff, datalen);
	sendLen += datalen;
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(zclBuff->ubuff, sendLen));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	delete zclBuff;
	delete zclHead;
	return 0;
}

int TypeSerialDrive::onReadAttribute(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint32_t attributeid, uint32_t delaytime)
{
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(TRUE, ZCL_CMD_READ, new TypeZclAttribute(attributeid, 0, NULL, 0)));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	return 0;
}

int TypeSerialDrive::onReadAttributeGeneric(uint32_t shortaddr, uint8_t srcpoint, uint32_t clusterid, uint32_t attributeid, uint32_t delaytime)
{
	//创建AF 帧结构
	TypeAFAttribute *tempAFData = new TypeAFAttribute(shortaddr, srcpoint, clusterid, new TypeZclProfile(FALSE, ZCL_CMD_READ, new TypeZclAttribute(attributeid, 0, NULL, 0)));
	//发送数据
	onWriteCMD(new TypeSerialProces((uint16_t)shortaddr, MT_RPC_CMD_SREQ | MT_RPC_SYS_AF, MT_AF_DATA_REQUEST, tempAFData, delaytime));
	return 0;
}

TypeSerialDrive::~TypeSerialDrive()
{
	//把发送队列、延时队列的数据都清除掉
	delete deviceName;
	delete deviceProces;
	if(deviceHandle > 0)
	{
		close(deviceHandle);
	}
	if(mMemNewFreeCount > 0)
	{
		mMemNewFreeCount--;
	}
	delete pDelaySendList;
	delete pLastSendCMD;
	delete pCMDSendList;//清空指令发送列表
	delete reciveBuff;
}
