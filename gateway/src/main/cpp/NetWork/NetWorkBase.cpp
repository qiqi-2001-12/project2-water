/*
 * NetWorkBase.c
 *
 *  Created on: 2017年6月28日
 *      Author: root
 */
#include <errno.h>
#include "../Main/WinobleMain.h"
//protoc --cpp_out=. *.proto
//git pull origin master
//sqliteman /root/eclipse/WinobleGateway/Debug/winobleDB
//SN:69704260472160256
//ID:6066005668826351
static MNET_STATUS mNet_TCP_Status = NetStatusInit;
static struct sockaddr_in mNet_TCP_SockAddr;
static int mTCPClientfd = -1;
static TypeArrayList *mTcpCMDRepeatSendList = NULL;
static TypeArrayList *mTcpCMDRepeatReciveList = NULL;
static uint32_t onGetSeqNo();
static void mfTcpLoginCMD();
static bool mfTCPBuffSend(TypeTcpCMD * cmd);
static void mfTcpCheckRepeatSendCB(int par1, int par2);
#define MAXDATASIZE            10240 //缓冲区大小

void * mfTCPNetWorkThread(void * arg)
{
	struct hostent *mpHostentIP;
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	int cmdStatus = 0;
	TypeTcpCMD tcpCmd;
	int retLen = 0;
	int tempInt = 0;
	TypeChar *tempTCPReciveBuff = new TypeChar(MAXDATASIZE);
	tcpCmd.payLoadBuff = NULL;
	mTcpCMDRepeatSendList = new TypeArrayList(ArrayTypeTcpCMD);
	mTcpCMDRepeatReciveList = new TypeArrayList(ArrayTypeSeqNo);
	sleep(3);
	while(mIsExitFlag)
	{
		//这里定时上报线程信息
		if((onGetTimeSec() - tempThreadInfo->lastSaveTime) >= 10)//10s 上报一次线程的健康状态
		{
			tempThreadInfo->lastSaveTime = onGetTimeSec();
			mPrintf(Log_NetWork, "thread %s:netStat=%d reSendCnt=%d reReciveCnt=%d ", tempThreadInfo->title->buff, mNet_TCP_Status, mTcpCMDRepeatSendList->size(), mTcpCMDRepeatReciveList->size());
		}
		switch((char)mNet_TCP_Status)
		{
		case NetStatusInit:
			mTcpCMDRepeatSendList->clear();
			mTcpCMDRepeatReciveList->clear();
			cmdStatus = 0;
			if(pDataBase->onGetIEEE())
			{
				if(pDataBase->onGetGateway_ID() == 0 || pDataBase->onGetSerial()[0] == 0)
				{
					//去服务器请求一下
					if(mfHttpGetGatewayInfo() == 0)
					{
						mNet_TCP_Status = NetHttpCertification;
					}
					else
					{
						//设备没有注册
					//	onNotifyToJava(JNI_NOTIFY_NET_STATUS, 2, 0, 0, "设备没有注册");
						mNet_TCP_Status = NetStatusClose;
					}
				}
				else
				{
					mNet_TCP_Status = NetHttpCertification;
				}
			}
			else
			{
				mNet_TCP_Status = NetStatusClose;
				//设备正在等待zigbee模块响应
				//onNotifyToJava(JNI_NOTIFY_NET_STATUS, 1, 0, 0, "等待zigbee模块响应");
			}
			break;
		case NetHttpCertification://http 认证
			if(mfHttpCertification() == 0)
			{
				mPrintf(Log_NetWork, "http certification OK! ");
				if((mpHostentIP = gethostbyname(mHttpGetServerIPAddress())) == NULL)
				{
					mPrintf(Log_NetWork, "%s connot get ip! ", mHttpGetServerIPAddress());
					mNet_TCP_Status = NetStatusClose;
					//请检查网络连接
				//	onNotifyToJava(JNI_NOTIFY_NET_STATUS, 3, 0, 0, "请检查网络连接");
				}
				else
				{
					bzero(&mNet_TCP_SockAddr, sizeof(mNet_TCP_SockAddr));
					mNet_TCP_SockAddr.sin_family = AF_INET;
					mNet_TCP_SockAddr.sin_port = htons(mHttpGetServerIPPort());
					mNet_TCP_SockAddr.sin_addr = *((struct in_addr *)mpHostentIP->h_addr);
					mTCPClientfd = socket(AF_INET, SOCK_STREAM, 0);
					if(mTCPClientfd == -1)
					{
						mPrintf(Log_NetWork, "socket() failure! ");
						mNet_TCP_Status = NetStatusClose;
						//onNotifyToJava(JNI_NOTIFY_NET_STATUS, 4, 0, 0, "网络创建失败");
					}
					else
					{
						mNet_TCP_Status = NetStatusConnect;
					}
				}
			}
			else
			{
				mPrintf(Log_NetWork, "http 认证失败(%d)! ", tempInt);
				mNet_TCP_Status = NetStatusClose;
			//	onNotifyToJava(JNI_NOTIFY_NET_STATUS, 5, 0, 0, "服务器认证失败!");
			}
			break;
		case NetStatusConnect:
			mPrintf(Log_NetWork, "TCP Server connect to ip=%s port=%d ", mHttpGetServerIPAddress(), mHttpGetServerIPPort());
			if(connect(mTCPClientfd, (struct sockaddr *)&mNet_TCP_SockAddr, sizeof(struct sockaddr)) == -1)
			{
				mPrintf(Log_NetWork, "connect() failure! ");
				mNet_TCP_Status = NetStatusClose;
				//onNotifyToJava(JNI_NOTIFY_NET_STATUS, 6, 0, 0, "服务器连接失败!");
			}
			else
			{
				mPrintf(Log_NetWork, "Connected ");
				mNet_TCP_Status = NetStatusLogin;
				struct timeval timeout = {5,0};
				setsockopt(mTCPClientfd, SOL_SOCKET,SO_SNDTIMEO, (char *)&timeout, sizeof(struct timeval));
				setsockopt(mTCPClientfd, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(struct timeval));
			}
			break;
		case NetStatusLogin://登录
		{
			mNet_TCP_Status = NetStatusRec;
			mfTcpLoginCMD();
			onTimerAdd(TIMER_TCP_REPEATCHECK, 100, true, mfTcpCheckRepeatSendCB, 0, 0);
		}
			break;
		case NetStatusRec://接收数据
			tempTCPReciveBuff->onClear();
			retLen = recv(mTCPClientfd, tempTCPReciveBuff->buff, tempTCPReciveBuff->size, 0);
			if((retLen == 0) || (mTCPClientfd == -1))
			{
				mNet_TCP_Status = NetStatusClose;
			//	onNotifyToJava(JNI_NOTIFY_NET_STATUS, 7, 0, 0, "网络数据接收异常!");
				mPrintf(Log_NetWork, "recv() err! retLen = %d ", retLen);
			}
			else if(retLen > 0)
			{
				int tempRetLen = 0;
				while(tempRetLen < retLen)
				{
					switch(cmdStatus)
					{
					case 0:tcpCmd.packetLength = 0; tcpCmd.packetLength |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//packetLength 4/4
					case 1:tcpCmd.packetLength <<= 8; tcpCmd.packetLength |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//packetLength 3/4
					case 2:tcpCmd.packetLength <<= 8; tcpCmd.packetLength |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//packetLength 2/4
					case 3:tcpCmd.packetLength <<= 8; tcpCmd.packetLength |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//packetLength 1/4
					case 4:tcpCmd.headerLength = 0; tcpCmd.headerLength |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//headerLength 2/2
					case 5:tcpCmd.headerLength <<= 8; tcpCmd.headerLength |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//headerLength 1/2
					default:
						if(tcpCmd.headerLength == 10)
						{
							switch(cmdStatus)
							{
							case 6:tcpCmd.version = 0; tcpCmd.version |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//version 2/2
							case 7:tcpCmd.version <<= 8; tcpCmd.version |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//version 1/2
							case 8:tcpCmd.commandID = 0; tcpCmd.commandID |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//commandID 4/4
							case 9:tcpCmd.commandID <<= 8; tcpCmd.commandID |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//commandID 3/4
							case 10:tcpCmd.commandID <<= 8; tcpCmd.commandID |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//commandID 2/4
							case 11:tcpCmd.commandID <<= 8; tcpCmd.commandID |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//commandID 1/4
							case 12:tcpCmd.seqNo = 0; tcpCmd.seqNo |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//seqNo 4/4
							case 13:tcpCmd.seqNo <<= 8; tcpCmd.seqNo |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//seqNo 3/4
							case 14:tcpCmd.seqNo <<= 8; tcpCmd.seqNo |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++; break;//seqNo 2/4
							case 15:tcpCmd.seqNo <<= 8; tcpCmd.seqNo |= tempTCPReciveBuff->buff[tempRetLen++] & 0xFF; cmdStatus++;
							tcpCmd.payLoadBuffLen = 0;
							if(tcpCmd.payLoadBuff != NULL)//seqNo 1/4
							{
								delete tcpCmd.payLoadBuff;
								tcpCmd.payLoadBuff = NULL;
							}
                            tcpCmd.payLoadBuff = new TypeChar(tcpCmd.packetLength - tcpCmd.headerLength - 1);
							if(tcpCmd.payLoadBuffLen == (tcpCmd.packetLength - tcpCmd.headerLength - 2))
							{
								//cmd buff is NULL
								mfTcpCMDParsing(&tcpCmd);
								cmdStatus = 0;
							}
							break;
							case 16:
								if(tcpCmd.payLoadBuffLen < (tcpCmd.packetLength - tcpCmd.headerLength - 2))
								{
									tcpCmd.payLoadBuff->ubuff[tcpCmd.payLoadBuffLen++] = tempTCPReciveBuff->ubuff[tempRetLen++];
								}
								if(tcpCmd.payLoadBuffLen == (tcpCmd.packetLength - tcpCmd.headerLength - 2))
								{
									//cmd buff recived OK!
									mfTcpCMDParsing(&tcpCmd);
									cmdStatus = 0;
								}
								break;
								default:break;
							}
						}
						break;
					}
				}
			}
			else
			{
				//mPrintf(Log_NetWork, "Recive ret=%d errno=%d", retLen, errno);
				if((errno == EAGAIN) || (errno == EWOULDBLOCK) || (errno == EINTR))
				{
					//这些都是正常返回
				}
				else
				{
					//重新连接
					mNet_TCP_Status = NetStatusClose;
					mPrintf(Log_NetWork, "recv() err! retLen = %d ", retLen);
				//	onNotifyToJava(JNI_NOTIFY_NET_STATUS, 7, 0, 0, "网络数据接收异常!");
				}
			}
			break;
		case NetStatusClose:
		{
			//offline
			static int32_t delayCount = 0;
			if(delayCount == 0)
			{
				mTcpCMDRepeatSendList->clear();
				mTcpCMDRepeatReciveList->clear();
				onTimerDelete(TIMER_TCP_REPEATCHECK);//stop repeat check timer
				onTimerDelete(TIMER_TCP_HEARTBEAT);//stop heartbeat timer
				if(mTCPClientfd != -1)
				{
					close(mTCPClientfd);
					mTCPClientfd = -1;
				}
			}
			delayCount++;
			sleep(1);//延时1s
			if(delayCount >= 10)
			{
				mNet_TCP_Status = NetStatusInit;
				delayCount = 0;
			}
		}
			break;
		default:
			break;
		}
	}
	if(mTCPClientfd != -1)
	{
		close(mTCPClientfd);
		mTCPClientfd = -1;
	}
	mNet_TCP_Status = NetStatusClose;
	delete tempTCPReciveBuff;
	delete mTcpCMDRepeatSendList;mTcpCMDRepeatSendList = NULL;
	delete mTcpCMDRepeatReciveList;mTcpCMDRepeatReciveList = NULL;
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

bool onGetConnectFlag()
{
	return (mNet_TCP_Status == NetStatusRec) && (mTCPClientfd != -1);
}

bool onResetTcpConnect()
{
	if(onGetConnectFlag())
	{
		mNet_TCP_Status = NetStatusClose;
	}
	return TRUE;
}

static void setLoginNetInfo(AuthTokenRequest* req, const char* key, const char* val) {
	typedef ::google::protobuf::Map<std::string,std::string> GMap;
	GMap* netinfos = req->mutable_net_infos();
	assert(netinfos);
	netinfos->insert(GMap::value_type(key, val));
}

static void mfTcpLoginCMD()
{
	AuthTokenRequest login;
	login.Clear();
	login.set_appid(APPID);
	login.set_uid(pDataBase->onGetGateway_ID());
	login.set_client_type(2);
	login.set_platform(5);
	login.set_soft_type(1);
	//判断当前是有线还是无线
	bool isWifi = true;
	TypeChar *tempBuffs = new TypeChar(256);
	FILE * fileHanle = fopen("/proc/net/route", "rb");
	if(fileHanle)
	{
		fgets(tempBuffs->buff, 256,  fileHanle);//读取文件  这个是标题
		//先把这个文件全部变成字符串
		tempBuffs->onClear();
		fgets(tempBuffs->buff, 256,  fileHanle);//读取文件  这个是正文
		//在这个里面查找eth0 没有就代表是wifi
		int32_t index = 0;
		char tempChar = 0;
		uint8_t tempStatus = 0;
		while(tempBuffs->buff[index])
		{
			tempChar = tempBuffs->buff[index++];
			if((tempStatus == 0) && (tempChar == 'e'))
			{
				tempStatus++;
			}
			else if((tempStatus == 1) && (tempChar == 't'))
			{
				tempStatus++;
			}
			else if((tempStatus == 2) && (tempChar == 'h'))
			{
				tempStatus++;
			}
			else if((tempStatus == 3) && (tempChar == '0'))
			{
				tempStatus++;
				//已经完成了
				isWifi = false;
				break;
			}
			else
			{
				tempStatus = 0;
			}
		}

	}
#include <linux/wireless.h>
	fclose(fileHanle);
	tempBuffs->onClear();
	//获取网关网络详细信息
	{//想要获取当前网口网线插入状态，需要用到ifreq结构体，获取网卡的信息，然后socket结合网卡驱动的ioctl，就可以得到与网线插入状态相关的数据。
		int number;
		char *tmpbuf;
		struct ifconf ifc;          //用来保存所有接口信息的
		struct ifreq buf[256];       //这个结构定义在net/if.h，用来配置ip地址，激活接口，配置MTU等接口信息
		memset(buf, 0, 256);
		ifc.ifc_len = sizeof(buf);
		ifc.ifc_buf = (caddr_t)buf;

		int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
		if(-1 == sockfd)//#define ETH_P_ALL       0x0003
		{
			mPrintf(Log_Error, "socket build !");
		}

		if(-1 == ioctl(sockfd,SIOCGIFCONF,(char *)&ifc))//SIOCGIFCONF用来获取所有配置接口的信息，将所获取的信息保存到ifc里。
		{
			mPrintf(Log_Error, "SIOCGIFCONF !");
		}

		number = ifc.ifc_len / sizeof(struct ifreq);
		mPrintf(Log_Error, "the interface number is %d ",number);
		int tmp = 0;
		for(tmp = number;tmp > 0;tmp--)
		{
			bool getInfoFlag = false;
			tempBuffs->onClear();
			tempBuffs->onAddString(buf[tmp].ifr_name);
			mPrintf(Log_Error, "check interface %s ", tempBuffs->buff);
			if(!isWifi && (tempBuffs->onStringCMP("eth0")))
			{
				getInfoFlag = TRUE;
			}
			else if(isWifi)
			{
#ifdef WINOBLE_LINUX
				if(tempBuffs->onStringCMP("apcli0"))
#else
				if(tempBuffs->onStringCMP("wlan0"))
#endif
				{
					getInfoFlag = TRUE;
				}
			}
			if(getInfoFlag)
			{
				// IP地址的获取
				if(0 == ioctl(sockfd,SIOCGIFADDR,(char *)&buf[tmp]))
				{
					tmpbuf = inet_ntoa(((struct sockaddr_in*) (&buf[tmp].ifr_addr))->sin_addr);

					setLoginNetInfo(&login, "ipaddr", (const char*)tmpbuf);
					mPrintf(Log_Error, "IPAdress :%s ",tmpbuf);
				}

				// 子网掩码的获取
				if(0 == ioctl(sockfd,SIOCGIFNETMASK,(char *)&buf[tmp]))
				{
					tmpbuf = inet_ntoa(((struct sockaddr_in*) (&buf[tmp].ifr_addr))->sin_addr);

					setLoginNetInfo(&login, "netmask", (const char*)tmpbuf);
					mPrintf(Log_Error, "netmask:%s ",tmpbuf);
				}

				//获取wifi名称
				if(isWifi)
				{
					struct iwreq wreq;
					memset(&wreq, 0, sizeof(struct iwreq));
					char buffer[32];
					memset(buffer, 0, 32);
					wreq.u.essid.pointer = buffer;//if not write these codes , the program maybe wrong.
					wreq.u.essid.length = 32;
					memcpy(wreq.ifr_name, buf[tmp].ifr_name, strlen(buf[tmp].ifr_name));
					if(0 == ioctl(sockfd, SIOCGIWESSID, &wreq))
					{
						tmpbuf = (char *)wreq.u.essid.pointer;
						setLoginNetInfo(&login, "wifi", (const char*)tmpbuf);
						mPrintf(Log_Error, "wifi:%s ",tmpbuf);
					}
					else
					{
						setLoginNetInfo(&login, "wifi", "");
						mPrintf(Log_Error, "wifi get error! ");
					}
				}
				else
				{
					setLoginNetInfo(&login, "wifi", "");
					mPrintf(Log_Error, "isWifi = false! ");
				}

				tempBuffs->onClear();
				// MAC地址的获取
				if(0 == ioctl(sockfd,SIOCGIFHWADDR,(char *)&buf[tmp]))
				{
					sprintf(tempBuffs->buff, "%02x:%02x:%02x:%02x:%02x:%02x",
					        (unsigned char) buf[tmp].ifr_hwaddr.sa_data[0],
					        (unsigned char) buf[tmp].ifr_hwaddr.sa_data[1],
					        (unsigned char) buf[tmp].ifr_hwaddr.sa_data[2],
					        (unsigned char) buf[tmp].ifr_hwaddr.sa_data[3],
					        (unsigned char) buf[tmp].ifr_hwaddr.sa_data[4],
					        (unsigned char) buf[tmp].ifr_hwaddr.sa_data[5]);
					setLoginNetInfo(&login, "mac", (const char*)tempBuffs->buff);

					mPrintf(Log_Error, "mac:%s\n\n",tempBuffs->buff);
				}
				break;
			}
		}
		close(sockfd);
	}
	delete tempBuffs;
	login.set_access_token(mHttpGetAccessToken());
	mfTCPCMDSend(CMD_ID_AUTH_TOKEN_REQ, login.SerializeAsString().c_str(), login.SerializeAsString().length());
}

void mfTcpHeartbeatCMDCB(int par1, int par2)
{
	static Ping staticPing;
	staticPing.set_timestamp(123456);
	mfTCPCMDSend(CMD_ID_PING, staticPing.SerializeAsString().c_str(), staticPing.SerializeAsString().length());
}

static void mfTcpCheckRepeatSendCB(int par1, int par2)
{
	static uint32_t tempCount = 0;
	//100ms check once
	static TypeTcpCMD *pTcpCMD = NULL;
	static TypeSeqNo * pSeqNo = NULL;
	if(mTcpCMDRepeatSendList)
	{
		for(int i = 0; i < mTcpCMDRepeatSendList->size();)
		{
			pTcpCMD = (TypeTcpCMD *)mTcpCMDRepeatSendList->get(i);
			pTcpCMD->delayTime -= 100;
			if(pTcpCMD->delayTime <= 0)
			{
				if(pTcpCMD->sendCount > 0)
				{
					pTcpCMD->sendCount--;
					pTcpCMD->delayTime = VALUE_TCP_DELEAY_REPEART;
					mfTCPBuffSend(pTcpCMD);
				}
				if(pTcpCMD->sendCount <= 0)
				{
					mPrintf(Log_NetWork, "CMD Send Time Out! %s ", mGetNetCMDString(pTcpCMD->commandID));
					mTcpCMDRepeatSendList->removeObject(pTcpCMD);
					// ping time out
					mNet_TCP_Status = NetStatusClose;// reconnect server
					pTcpCMD = NULL;
					continue;
				}
			}
			i++;
		}
		tempCount++;
		if(tempCount >= 100)
		{
			tempCount = 0;
			//check seqNo
			for(int i = 0; i < mTcpCMDRepeatReciveList->size();)
			{
				pSeqNo = (TypeSeqNo *)mTcpCMDRepeatReciveList->get(i);
				pSeqNo->delayTime -= 10000;
				if(pSeqNo->delayTime <= 0)
				{
					//mPrintf("delete seqNo=%d", pSeqNo->seqNo);
					mTcpCMDRepeatReciveList->removeObject(pSeqNo);
					pSeqNo = NULL;
					continue;
				}
				i++;
			}
		}

		//check datalist status
		if(pDeviceList->mDownLoadFlag)
		{
			int32_t index = 0;
			uint32_t toDownLoadStatus = 0;
			int32_t tempFlag = 0;
			index = 0;
			while((index < 32) && (pDeviceList->mDownLoadFlag))
			{
				tempFlag = 1 << index;
				if((pDeviceList->mDownLoadFlag & tempFlag) && ((pDeviceList->mDownLoadingFlag & tempFlag) == 0))
				{
					toDownLoadStatus |= tempFlag;//使能下载中标志
					pDeviceList->mDownLoadingFlag |= tempFlag;//标志数据正在下载中
					pDeviceList->mDownLoadFlag &= ~tempFlag;//清除下载标志
				}
				index++;
			}
			//场景单独下载
			if(toDownLoadStatus & DEVICE_GET_MASK_SCENE)
			{
				ListSceneRequest listSceneRequest;
				listSceneRequest.set_family_id(pDataBase->onGetFamilyID());
				mfTCPCMDSend(CMD_ID_SCENE_LIST_REQ, listSceneRequest.SerializeAsString().c_str(), listSceneRequest.SerializeAsString().length());
				toDownLoadStatus &= ~DEVICE_GET_MASK_SCENE;
				pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_SCENE;
			}
			//然后再判断是否下载设备
			if(toDownLoadStatus & 0x3FFFFFFF)
			{
				onNetCMDGetDeviceReq(toDownLoadStatus);
			}
			toDownLoadStatus = 0;
		}
	}
}

bool onTcpCheckRepeatList(uint32_t seqno)
{
	static TypeTcpCMD *pTcpCMD = NULL;
	if(mTcpCMDRepeatSendList)
	{
		for(int i = 0; i < mTcpCMDRepeatSendList->size();)
		{
			pTcpCMD = (TypeTcpCMD *)mTcpCMDRepeatSendList->get(i);
			if(pTcpCMD->seqNo == seqno)
			{
				//mPrintf("delete to send List:%d", seqno);
				mTcpCMDRepeatSendList->removeObject(pTcpCMD);
				pTcpCMD = NULL;
				break;
			}
			i++;
		}
	}
	return TRUE;
}

bool onTcpAckCMDSend(TypeTcpCMD * ptcpcmd)
{
	static Ack tempAck;
	if(ptcpcmd->commandID != CMD_ID_ACK)
	{
		tempAck.Clear();
		tempAck.add_seq_nos(ptcpcmd->seqNo);
		mfTCPCMDSend(CMD_ID_ACK, tempAck.SerializeAsString().c_str(), tempAck.SerializeAsString().length());
		return TRUE;
	}
	return FALSE;
}

bool onTcpCheckSeqNo(uint32_t seqno, uint32_t cmd_id)
{
	static TypeSeqNo * pSeqNo = NULL;
	bool retBool = false;
	if(mTcpCMDRepeatReciveList)
	{
		for(int i = 0; i < mTcpCMDRepeatReciveList->size();i++)
		{
			pSeqNo = (TypeSeqNo *)mTcpCMDRepeatReciveList->get(i);
			if((pSeqNo->seqNo == seqno) && (pSeqNo->command_id == cmd_id))
			{
				retBool = true;
				break;
			}
		}
		if(!retBool)
		{
			mTcpCMDRepeatReciveList->add(new TypeSeqNo(seqno, cmd_id));
			//mPrintf("add seqNo=%d", seqno);
		}
	}
	return retBool;
}

static uint32_t onGetSeqNo()
{
	static uint32_t staticSeqNo = 0;
	staticSeqNo++;
	return staticSeqNo;
}

static bool mfTCPBuffSend(TypeTcpCMD * cmd)
{
	bool retBool = false;
	if((mNet_TCP_Status == NetStatusRec) && (cmd))
	{
		int ret = send(mTCPClientfd, cmd->payLoadBuff->buff, cmd->payLoadBuffLen, 0);
		if(ret > 0)
		{
			//mfCharsToHexString(cmd->payLoadBuff->buff, cmd->payLoadBuffLen);
			if(onCheckPrint())
			{
				if((cmd->commandID != CMD_ID_ACK) && (cmd->commandID != CMD_ID_PING))
				{
					mPrintf(Log_NetWork, "TS:%s,seqNo=%d Len=%d ", mGetNetCMDString(cmd->commandID), cmd->seqNo, cmd->payLoadBuffLen);
				}
			}

			retBool = true;
		}
		else if(ret == 0)
		{
			if(onCheckPrint())
			{
				mPrintf(Log_NetWork, "Error:p2p tcp send err:time out ret = %d ", ret);
			}
			mNet_TCP_Status = NetStatusClose;
		}
	}
	else
	{
		mPrintf(Log_NetWork, "Error:wrong p2p tcp status!! status = %d mTCPClientfd = %d ", mNet_TCP_Status, mTCPClientfd);
	}
	return retBool;
}

void mfTCPCMDSend(uint32_t cmmandid, char const * value, int valuelen)
{
	if(mTcpCMDRepeatSendList)
	{
		TypeTcpCMD *pTypeTcpCMD = new TypeTcpCMD(cmmandid, (char *)value, (uint32_t)valuelen, onGetSeqNo());
		if(mfTCPBuffSend(pTypeTcpCMD))
		{
			if(cmmandid != CMD_ID_ACK)
			{
				mTcpCMDRepeatSendList->add(pTypeTcpCMD);
				return;
			}
		}
		delete pTypeTcpCMD;
	}
}


