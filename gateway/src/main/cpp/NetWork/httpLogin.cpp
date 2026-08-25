/*
 * httpLogin.c
 *
 *  Created on: 2017年6月25日
 *      Author: root
 */
/*******   http客户端程序   httpclient.c   ************/
#include  "../Main/WinobleMain.h"
#include  <openssl/md5.h>
#include  <openssl/base64.h>

static char * mfGetGatewayBase64(uint64_t appid, const char *appsecret, char *gatewayBase64);
static int mfHttpConnect(const char *ip, int port);
static char * mfHttpSend(int sockfd, TypeChar *buff, int len);
static char mIPInfo[64];
static char mToken[256];
static int  mIPPort;
//////////////////////////////httpclient.c   开始///////////////////////////////////////////

char *mHttpGetServerIPAddress()
{
	return mIPInfo;
}

int mHttpGetServerIPPort()
{
	return mIPPort;
}

char *mHttpGetAccessToken()
{
	return mToken;
}

int mfHttpDlFile(char *filename, char *filepath, char *savepath, char *filemd5, uint32_t *filelen, void* (*__start_routine)(void*))
{
	int retInt = 6;//其它错误信息
	int sockfd = mfHttpConnect(SERVER_IPINFO, SERVER_IPPORT);
	if(sockfd >= 0)
	{
		TypeChar *request = new TypeChar(1024);
		sprintf(request->buff,"GET %s%s HTTP/1.1\r\n"
				//"Accept: */*\r\n"
				"Accept-Language: zh-cn\r\n"
				//"User-Agent:Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n"
				"Host: %s\r\n"
				"Connection: Close\r\n"
				"Content-Length: %d\r\n"
				"Content-Type: application/json\r\n"
				"\r\n%s", SERVER_DEBUG, filepath, SERVER_IPINFO, 0, "");

		int tempIndex = 0;
		int totalLen = strlen(request->buff);

		int tempRet = 0;
		int mTimeOut = 3000000;
		while((tempIndex < totalLen) && (mTimeOut > 0))
		{
			tempRet = write(sockfd, request->buff + tempIndex, (size_t)(totalLen - tempIndex));
			if(tempRet > 0)
			{
				tempIndex += tempRet;
			}
			else if(tempRet == -1)
			{
				return retInt;
			}
			if(tempIndex < totalLen)
			{
				usleep(1000);
				mTimeOut -= 1000;
			}
		}
		//开始启动接收
		request->onClear();
		//读取并存储为文件
		totalLen = 0;
		tempRet = 1;
		int dataLen = 0;
		int findLenIndex = 0;
		bool retBool = true;
		int tempStatus = 0;
		tempIndex = 0;
		mTimeOut = 3000000;
		int tempPercent = 0;
		int32_t tempSaveFile = 0;
		TypeChar *saveBuff = new TypeChar(1024);
		while(retBool && (mTimeOut > 0))
		{
			tempRet = read(sockfd, request->buff, request->size);
			if(tempRet > 0)
			{
				//mPrintf(Log_NetWork, "%s\n", pTempChar->buff);
				tempIndex = 0;
				while(tempIndex < tempRet)
				{
					if(tempStatus == 0)
					{
						saveBuff->buff[totalLen++] = request->buff[tempIndex++];
						if(request->buff[tempIndex - 1] == '\r' || request->buff[tempIndex - 1] == '\n')
						{
							findLenIndex++;
							if(findLenIndex >= 4)
							{
								//在这里算是结束了
								//pDataBuff = &buff->buff[totalLen];
								tempStatus = 1;
								//find data len
								findLenIndex = 0;
								while(findLenIndex < totalLen)
								{
									if(saveBuff->buff[findLenIndex] == 'C')
									{
										if(memcmp(&saveBuff->buff[findLenIndex], "Content-Length:", strlen("Content-Length:")) == 0)
										{
											findLenIndex += strlen("Content-length");
											dataLen = 0;
											findLenIndex++;
											while(!(saveBuff->buff[findLenIndex] == '\r' || saveBuff->buff[findLenIndex] == '\n'))
											{
												if(saveBuff->buff[findLenIndex] != ' ')
												{
													dataLen = dataLen * 10 + (saveBuff->buff[findLenIndex] - 0x30);
													*filelen = (uint32_t)dataLen;
												}
												findLenIndex++;
											}
											findLenIndex = 0;
											totalLen = 0;
											if(dataLen == 0)
											{
												retBool = false;
											}
											break;
										}
									}
									findLenIndex++;
								}
							}
						}
						else
						{
							findLenIndex = 0;
						}
					}
					else
					{
						if(tempSaveFile == 0)
						{
							//先判断这个文件是否存在
							mPrintf(LOG_Robot, "先判断这个文件是否存在！%s ", savepath);
							int ret = access(savepath, F_OK);
							//删除临时文件  如果以前存在
							if(ret == 0)
							{
								mPrintf(LOG_Robot, "存在! 删除这个文件!%s ", savepath);
								sprintf(saveBuff->buff, "rm %s", savepath);
								system(saveBuff->buff);
							}
							tempSaveFile = open(savepath, O_RDWR | O_CREAT, S_IRWXU);
							if(__start_routine)
							{
								__start_routine((void *)&tempPercent);
							}
						}
						findLenIndex += tempRet - tempIndex;
						if(tempSaveFile >= 0)
						{
							//一次性写入所有数据到文件
							write(tempSaveFile, &request->buff[tempIndex], (size_t)(tempRet - tempIndex));
							tempIndex = tempRet;
							tempPercent = (findLenIndex * 100) / dataLen;
							if(__start_routine)
							{
								__start_routine((void *)&tempPercent);
							}
							if(findLenIndex >= dataLen)
							{
								//文件下载完成
								mPrintf(LOG_Robot, "文件下载完成@!%s ", savepath);
								close(tempSaveFile);
								uint8_t m5[16] = {0};
								char hash[33] = {0};
								int n = md5sum(savepath, m5);
								if( n < 0) {
									mPrintf(LOG_Robot, "文件MD5校验失败");
								}
								md5sum_hex_encode(m5, hash);
								if( strcmp(filemd5, hash) == 0 ) {
									//检验正确
									retInt = 0;
								} else {
									mPrintf(LOG_Robot, "文件MD5错误: '%s', 期望是: '%s'", hash, filemd5);
								}
							}
						}
					}
				}
			}
			else
			{
				usleep(1000);
				mTimeOut -= 1000;
			}
		}
		mPrintf(Log_NetWork, "http end ");
		delete saveBuff;
		delete request;
		/*   结束通讯   */
		close(sockfd);
	}
	return retInt;
}

int mfHttpCheckAppUpdate()
{
	//TypeChar *xxx = new TypeChar();
	//delete xxx;
	//delete xxx;
	int retInt = 6;//其它错误信息
	int sockfd = mfHttpConnect(SERVER_IPINFO, SERVER_IPPORT);
	if(sockfd >= 0)
	{
		TypeChar *request = new TypeChar(2048);
		sprintf(request->buff,"GET %s/dists/gateway/winoble.txt HTTP/1.1\r\n"
				//"Accept: */*\r\n"
				"Accept-Language: zh-cn\r\n"
				//"User-Agent:Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n"
				"Host: %s\r\n"
				"Connection: Close\r\n"
				"Content-Length: %d\r\n"
				"Content-Type: application/json\r\n"
				"\r\n%s", SERVER_DEBUG, SERVER_IPINFO, 0, "");

		mPrintf(Log_NetWork, "检查zigbee芯片版本！ ");
		char *pDataBuff = mfHttpSend(sockfd, request, strlen(request->buff));
		if(pDataBuff != NULL)
		{
			//json 解析
			cJSON *json = cJSON_Parse(pDataBuff);
			cJSON *tempJson = NULL;
			if(json)
			{
				tempJson = cJSON_GetObjectItem(json, "znp_cc2538_ver");//检查cc2538 是否有更新
				if(tempJson)
				{
					if(onFindThreadTitle((char *)"cc2538 DL") == FALSE)
					{
						//当前没有cc2538升级线程在运行
						if(pDataBase->onGetCC2538Ver() && (pDataBase->onGetCC2538Ver() != (int32_t)(tempJson->valueint)))
						{
							TypeChar *zigbeeChipName = new TypeChar(32);
							sprintf(zigbeeChipName->buff, "znp_cc%04d", pDataBase->onGetChipType());
							TypeChar *zigbeeBinMD5 = new TypeChar(32);
							sprintf(zigbeeBinMD5->buff, "znp_cc%04d_md5", pDataBase->onGetChipType());

							//说明有升级
							if(cJSON_GetObjectItem(json, zigbeeChipName->buff) && cJSON_GetObjectItem(json, zigbeeBinMD5->buff) && (mIsDownLoadingFlag == FALSE))
							{
								mPrintf(LOG_Robot, "zigbee新版本!%d->%d ", pDataBase->onGetCC2538Ver(), (int32_t)tempJson->valueint);
								//把MD5和文件名称  传送到线程里面去
								TypeChar *tempBuff = new TypeChar();
								cJSON *sendJason = cJSON_CreateObject();
								tempJson = cJSON_GetObjectItem(json, zigbeeChipName->buff);
								cJSON_AddStringToObject(sendJason, "filename", tempJson->valuestring);
								sprintf(tempBuff->buff, "/dists/gateway/%s", tempJson->valuestring);
								cJSON_AddStringToObject(sendJason, "filepath", tempBuff->buff);
#ifdef WINOBLE_LINUX
								sprintf(tempBuff->buff, "/tmp/%s", tempJson->valuestring);
#else
								sprintf(tempBuff->buff, "/sdcard/%s", tempJson->valuestring);
#endif
								cJSON_AddStringToObject(sendJason, "savepath", tempBuff->buff);
								tempJson = cJSON_GetObjectItem(json, zigbeeBinMD5->buff);
								cJSON_AddStringToObject(sendJason, "filemd5", tempJson->valuestring);
								char * tempChars = cJSON_Print(sendJason);
								//启动下载
								onAddThread("cc2538 DL", mfZnpDownLoadThread, tempChars);
								cJSON_Delete(sendJason);
								free(tempChars);
								delete tempBuff;
							}
							delete zigbeeBinMD5;
							delete zigbeeChipName;
						}
						else
						{
							mPrintf(LOG_Robot, "zigbee已经是最新版本！%d ", pDataBase->onGetCC2538Ver());
						}
					}
					else
					{
						mPrintf(Log_NetWork, "检查zigbee版本！程序正在下载中…… ");
					}
				}
			}
			else
			{
				mPrintf(Log_NetWork, "检查zigbee版本！版本文件无法解析! ");
			}
			cJSON_Delete(json);
		}
		else
		{
			mPrintf(Log_NetWork, "检查zigbee版本！http请求版本文件错误! ");
		}
		/*   结束通讯   */
		close(sockfd);
		delete request;
	}
	return retInt;
}

int mfHttpRegistered(int64_t ieee, int64_t ieee_ex, char * model, char *defaultname)
{
	int retInt = 6;//其它错误信息
	int sockfd = mfHttpConnect(SERVER_IPINFO, SERVER_IPPORT);
	if(sockfd >= 0)
	{
		cJSON *json = cJSON_CreateObject();
		cJSON_AddLongNumberToObject(json, "ieee", ieee);
		cJSON_AddLongNumberToObject(json, "ieee_ex", ieee_ex);
		cJSON_AddLongNumberToObject(json, "mfd", 4);
		cJSON_AddNumberToObject(json, "protocol", 1);
		cJSON_AddStringToObject(json, "model", model);
		cJSON_AddStringToObject(json, "serial", "");
		cJSON_AddStringToObject(json, "sw_version", "v1.111");
		cJSON_AddStringToObject(json, "hw_version", "v1.111");
		cJSON_AddNumberToObject(json, "db_version", 1);
		cJSON_AddStringToObject(json, "name", defaultname);
		cJSON_AddNumberToObject(json, "room_id", 0);
		cJSON_AddStringToObject(json, "language", "zh_CN.UTF-8");
		cJSON_AddStringToObject(json, "time_zone", "Asia/Shanghai");
		char *retJsonChars = cJSON_Print(json);
		cJSON_Delete(json);
		TypeChar *gatewayBase64 = new TypeChar(128);
		TypeChar *request = new TypeChar(1024);
		sprintf(request->buff,"POST %s/v1/gateway/register HTTP/1.1\r\n"
		                      //"Accept: */*\r\n"
		                      "Accept-Language: zh-cn\r\n"
		                      //"User-Agent:Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n"
		                      "Host: %s\r\n"
		                      "Authorization: Basic %s\r\n"
		                      "Connection: Close\r\n"
		                      "Content-Length: %d\r\n"
		                      "Content-Type: application/json\r\n"
		                      "\r\n%s", SERVER_DEBUG, SERVER_IPINFO, mfGetGatewayBase64(APPID, APPSERIAL, gatewayBase64->buff), (int)strlen(retJsonChars), retJsonChars);
		free(retJsonChars);
		delete gatewayBase64;
		int httpSendLen = strlen(request->buff);
		char *pDataBuff = mfHttpSend(sockfd, request, httpSendLen);
		if(pDataBuff != NULL)
		{
			//json 解析
			cJSON *json = cJSON_Parse(pDataBuff);
			if (!json)
			{
				retInt = 5;//Json 格式错误
			}
			else
			{
				//注册返回
				retInt = 0;
			}
			cJSON_Delete(json);
			json = NULL;
		}
		// 结束通信
		close(sockfd);
		delete request;
	}
	return retInt;
}

int mfHttpGetGatewayInfo()
{
	int retInt = -1;//其它错误信息
	int sockfd = mfHttpConnect(SERVER_IPINFO, SERVER_IPPORT);
	if(sockfd >= 0)
	{
		TypeChar *gatewayBase64 = new TypeChar();
		TypeChar *request = new TypeChar(2048);
		sprintf(request->buff,"GET %s/v1/gateway/info?ieee=%lld HTTP/1.1\r\n"
				//"Accept: */*\r\n"
				"Accept-Language: zh-cn\r\n"
				//"User-Agent:Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n"
				"Host: %s\r\n"
				"Authorization: Basic %s\r\n"
				"Connection: Close\r\n"
				"Content-Length: %d\r\n"
				"Content-Type: application/json\r\n"
				"\r\n%s", SERVER_DEBUG, pDataBase->onGetIEEE(), SERVER_IPINFO, mfGetGatewayBase64(APPID, APPSERIAL, gatewayBase64->buff), 0, "");
		delete gatewayBase64;
		char *pDataBuff = mfHttpSend(sockfd, request, strlen(request->buff));
		retInt = 1;
		if(pDataBuff != NULL)
		{
			//json 解析
			cJSON *json = cJSON_Parse(pDataBuff);
			cJSON *tempJson = cJSON_GetObjectItem(json, "error_code");
			if(tempJson->valueint == 0)
			{
				cJSON *subJson = cJSON_GetObjectItem(json, "gateway");
				if(subJson != NULL)
				{
					tempJson = cJSON_GetObjectItem(subJson, "ieee");
					if(tempJson != NULL)
					{
						if(tempJson->valueint == (uint64_t)pDataBase->onGetIEEE())
						{
							//ieee 正确
							tempJson = cJSON_GetObjectItem(subJson, "gateway_id");
							if(tempJson != NULL)
							{
								pDataBase->onSetGateway_ID(tempJson->valueint);
							}
							tempJson = cJSON_GetObjectItem(subJson, "serial");
							if(tempJson != NULL)
							{
								pDataBase->onSetSerial(tempJson->valuestring);
							}
							retInt = 0;
						}
					}
				}
			}
			else
			{
				retInt = 2;
				tempJson = cJSON_GetObjectItem(json, "error_msg");
				if(tempJson != NULL)
				{
					mPrintf(Log_Error, "Error:%s ", tempJson->valuestring);
				}
			}
			cJSON_Delete(json);
		}
		/*   结束通讯   */
		close(sockfd);
		delete request;
	}
	return retInt;
}

//int mfHttpCertification()
//{
//	int retInt = 6;//其它错误信息
//	mPrintf(Log_NetWork, "http connect=%s port=%d ", SERVER_IPINFO, SERVER_IPPORT);
//	int sockfd = mfHttpConnect(SERVER_IPINFO, SERVER_IPPORT);
//	if(sockfd >= 0)
//	{
//		mPrintf(Log_NetWork, "http connect %s success ", SERVER_IPINFO);
//		cJSON *json = cJSON_CreateObject();
//		cJSON_AddLongNumberToObject(json, "gateway_id", (uint64_t)pDataBase->onGetGateway_ID());
//		cJSON_AddLongNumberToObject(json, "ieee", (uint64_t)pDataBase->onGetIEEE());
//		cJSON_AddStringToObject(json, "serial", pDataBase->onGetSerial());
//		cJSON_AddStringToObject(json, "sw_version", GATEWAY_SOFTVER);
//		cJSON_AddNumberToObject(json, "db_version", (uint32_t)pDataBase->onGetDBGateway());
//		char * tempChars = cJSON_Print(json);
//		TypeChar *retJsonChars = new TypeChar(tempChars);
//        //mPrintf(Log_NetWork, "%s", retJsonChars);
//		cJSON_Delete(json);
//		free(tempChars);
//		TypeChar *request = new TypeChar(2048);
//		TypeChar *gatewayBase64 = new TypeChar();
//		sprintf(request->buff,"POST %s/v1/gateway/token HTTP/1.1\r\n"
//					//"Accept: */*\r\n"
//					"Accept-Language: zh-cn\r\n"
//					//"User-Agent:Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n"
//					"Host: %s\r\n"
//					"Authorization: Basic %s\r\n"
//					"Connection: Close\r\n"
//					"Content-Length: %d\r\n"
//					"Content-Type: application/json\r\n"
//					"\r\n%s", SERVER_DEBUG, SERVER_IPINFO, mfGetGatewayBase64(APPID, APPSERIAL, gatewayBase64->buff), (int)strlen(retJsonChars->buff), retJsonChars->buff);
//		//mPrintf(Log_NetWork, "%s\n", request->buff);
//		delete gatewayBase64;
//		delete retJsonChars;
//		char *pDataBuff = mfHttpSend(sockfd, request, strlen(request->buff));
//		//mPrintf(Log_NetWork, "%s", pDataBuff);
//		memset(mIPInfo, 0, 64);
//		memset(mToken, 0, 256);
//		mIPPort = 0;
//		if(pDataBuff != NULL)
//		{
//			//json 解析
//			cJSON *parJosn = cJSON_Parse(pDataBuff);
//			if (!parJosn)
//			{
//				mPrintf(Log_NetWork, "Err Json:[%s]\n ",cJSON_GetErrorPtr());
//				retInt = 5;//Json 格式错误
//			}
//			else
//			{
//				cJSON *tempJson;
//				tempJson = cJSON_GetObjectItem(parJosn, "error_code");
//				if(tempJson != NULL)
//				{
//					if(tempJson->valueint != 0)
//					{
//						if(tempJson->valueint == 20005)
//						{
//							pDeviceList->onDeleteGateway();
//							//删除/sdcard/winobleQrcode.png
//						}
//						mPrintf(Log_NetWork, "http request error!(%llu: ",tempJson->valueint);
//						tempJson = cJSON_GetObjectItem(parJosn, "error_msg");
//						if(tempJson)
//						{
//							mPrintf(Log_NetWork, "%s) ",tempJson->valuestring);
//						}
//						mPrintf(Log_NetWork, "\n ");
//					}
//					else
//					{
//						//注册成功 开启登录
//						tempJson = cJSON_GetObjectItem(parJosn, "access_addr");
//						if(tempJson != NULL)
//						{
//							memcpy(mIPInfo, tempJson->valuestring, strlen(tempJson->valuestring));
//							//分析出IP 和端口
//							int tempPortIndex = 0;
//							bool tempPortFlag = false;
//							while(*(mIPInfo + tempPortIndex))
//							{
//								if(*(mIPInfo + tempPortIndex) == ':')
//								{
//									tempPortFlag = true;
//									*(mIPInfo + tempPortIndex) = 0;
//								}
//								else
//								{
//									if(tempPortFlag)
//									{
//										mIPPort = mIPPort * 10 + (*(mIPInfo + tempPortIndex) - 0x30);
//										*(mIPInfo + tempPortIndex) = 0;
//									}
//								}
//
//								tempPortIndex++;
//							}
//						}
//						//get token
//						tempJson = cJSON_GetObjectItem(parJosn, "access_token");
//						if(tempJson != NULL)
//						{
//							memcpy(mToken, tempJson->valuestring, strlen(tempJson->valuestring));
//						}
//						retInt = 0;//sucess
//					}
//				}
//			}
//			cJSON_Delete(parJosn);
//		}
//		/*   结束通讯   */
//		close(sockfd);
//		delete request;
//	}
//	else
//	{
//		mPrintf(Log_NetWork, "http connect %s failed ", SERVER_IPINFO);
//		retInt = 7;//tcp 端口创建失败
//	}
//	return retInt;
//}
// 给上层调用的入口（UI线程安全）
int mfHttpCertification()
{
	// ====================== 【崩溃防护：必加】 ======================
	// 1. 全局对象空判断
	if (pDataBase == NULL) {
		mPrintf(Log_NetWork, "认证失败：pDataBase 为空");
		return 7;
	}

	// 2. 服务器配置空判断
	if (SERVER_IPINFO == NULL || strlen(SERVER_IPINFO) == 0 || SERVER_IPPORT <= 0) {
		mPrintf(Log_NetWork, "认证失败：服务器IP/端口未配置");
		return 7;
	}

	// 3. 内存分配失败防护
	TypeChar* gatewayBase64 = new (std::nothrow) TypeChar();
	if (gatewayBase64 == NULL) {
		mPrintf(Log_NetWork, "认证失败：内存分配失败");
		return 7;
	}
	// ==============================================================

	int retInt = 6; // 其它错误信息
	mPrintf(Log_NetWork, "http connect=%s port=%d ", SERVER_IPINFO, SERVER_IPPORT);
	int sockfd = mfHttpConnect(SERVER_IPINFO, SERVER_IPPORT);

	if (sockfd >= 0)
	{
		mPrintf(Log_NetWork, "http connect %s success ", SERVER_IPINFO);

		cJSON *json = cJSON_CreateObject();
		if (json == NULL) { // cJSON 分配失败
			delete gatewayBase64;
			close(sockfd);
			return 7;
		}

		cJSON_AddLongNumberToObject(json, "gateway_id", (uint64_t)pDataBase->onGetGateway_ID());
		cJSON_AddLongNumberToObject(json, "ieee", (uint64_t)pDataBase->onGetIEEE());
		cJSON_AddStringToObject(json, "serial", pDataBase->onGetSerial());
		cJSON_AddStringToObject(json, "sw_version", GATEWAY_SOFTVER);
		cJSON_AddNumberToObject(json, "db_version", (uint32_t)pDataBase->onGetDBGateway());

		char * tempChars = cJSON_Print(json);
		if (tempChars == NULL) {
			cJSON_Delete(json);
			delete gatewayBase64;
			close(sockfd);
			return 7;
		}

		TypeChar *retJsonChars = new (std::nothrow) TypeChar(tempChars);
		cJSON_Delete(json);
		free(tempChars);

		if (retJsonChars == NULL) {
			delete gatewayBase64;
			close(sockfd);
			return 7;
		}

		TypeChar *request = new (std::nothrow) TypeChar(2048);
		if (request == NULL) {
			delete retJsonChars;
			delete gatewayBase64;
			close(sockfd);
			return 7;
		}

		// ====================== 【致命错误修复】 ======================
		// 你原来重复定义了 gatewayBase64！！！我直接删掉重复定义！
		// ====================== 已修复 ===============================

		sprintf(request->buff,
				"POST %s/v1/gateway/token HTTP/1.1\r\n"
				"Accept-Language: zh-cn\r\n"
				"Host: %s\r\n"
				"Authorization: Basic %s\r\n"
				"Connection: Close\r\n"
				"Content-Length: %d\r\n"
				"Content-Type: application/json\r\n"
				"\r\n%s",
				SERVER_DEBUG,
				SERVER_IPINFO,
				mfGetGatewayBase64(APPID, APPSERIAL, gatewayBase64->buff),
				(int)strlen(retJsonChars->buff),
				retJsonChars->buff
		);

		// 安全释放
		delete gatewayBase64;
		delete retJsonChars;

		char *pDataBuff = mfHttpSend(sockfd, request, strlen(request->buff));
		memset(mIPInfo, 0, 64);
		memset(mToken, 0, 256);
		mIPPort = 0;

		if (pDataBuff != NULL)
		{
			cJSON *parJosn = cJSON_Parse(pDataBuff);
			if (!parJosn)
			{
				mPrintf(Log_NetWork, "Err Json:[%s]\n ", cJSON_GetErrorPtr());
				retInt = 5; // Json 格式错误
			}
			else
			{
				cJSON *tempJson = cJSON_GetObjectItem(parJosn, "error_code");
				if (tempJson != NULL)
				{
					if (tempJson->valueint != 0)
					{
						if (tempJson->valueint == 20005)
						{
							pDeviceList->onDeleteGateway();
						}
						mPrintf(Log_NetWork, "http request error!(%d: ", tempJson->valueint);
						tempJson = cJSON_GetObjectItem(parJosn, "error_msg");
						if (tempJson)
						{
							mPrintf(Log_NetWork, "%s) ", tempJson->valuestring);
						}
						mPrintf(Log_NetWork, "\n ");
					}
					else
					{
						tempJson = cJSON_GetObjectItem(parJosn, "access_addr");
						if (tempJson != NULL)
						{
							memcpy(mIPInfo, tempJson->valuestring, strlen(tempJson->valuestring));
							int tempPortIndex = 0;
							bool tempPortFlag = false;
							while (*(mIPInfo + tempPortIndex))
							{
								if (*(mIPInfo + tempPortIndex) == ':')
								{
									tempPortFlag = true;
									*(mIPInfo + tempPortIndex) = 0;
								}
								else
								{
									if (tempPortFlag)
									{
										mIPPort = mIPPort * 10 + (*(mIPInfo + tempPortIndex) - 0x30);
										*(mIPInfo + tempPortIndex) = 0;
									}
								}
								tempPortIndex++;
							}
						}

						tempJson = cJSON_GetObjectItem(parJosn, "access_token");
						if (tempJson != NULL)
						{
							memcpy(mToken, tempJson->valuestring, strlen(tempJson->valuestring));
						}
						retInt = 0; // success
					}
				}
				cJSON_Delete(parJosn);
			}
		}

		close(sockfd);
		delete request;
	}
	else
	{
		mPrintf(Log_NetWork, "http connect %s failed ", SERVER_IPINFO);
		retInt = 7; // tcp 端口创建失败
		delete gatewayBase64; // 退出前必须释放
	}

	return retInt;
}

static int mfHttpConnect(const char *ip, int port)
{
	int sockfd = 0;
	struct hostent * host;
	struct sockaddr_in   server_addr;
	mPrintf(Log_NetWork, "http translate %s ", ip);
	if((host = gethostbyname(ip)) == NULL)/*取得主机IP地址*/
	{
		mPrintf(Log_NetWork, "http translate %s failed ", ip);
		return -1;//获取主机IP地址失败
	}
	mPrintf(Log_NetWork, "http create socket to %s ", inet_ntoa(*((struct in_addr *)host->h_addr)));
	/*   客户程序开始建立   sockfd描述符   */
	if((sockfd = socket(AF_INET,SOCK_STREAM,0)) == -1)/*建立SOCKET连接*/
	{
		mPrintf(Log_NetWork, "http create socket to %s failed ", inet_ntoa(*((struct in_addr *)host->h_addr)));
		return -2;//无法创建socket描述符
	}
	mPrintf(Log_NetWork, "http connect to %s sockfd=%d ", inet_ntoa(*((struct in_addr *)host->h_addr)), sockfd);

	/*   客户程序填充服务端的资料   */
	bzero(&server_addr,sizeof(server_addr));
	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(port);
	server_addr.sin_addr.s_addr=inet_addr(inet_ntoa(*((struct in_addr *)host->h_addr)));

	if(connect(sockfd, (struct sockaddr *)&server_addr, sizeof(struct sockaddr)) == -1)
	{
		mPrintf(Log_NetWork, "http connect to %s failed ", inet_ntoa(*((struct in_addr *)host->h_addr)));
		close(sockfd);
		return -3;//无法与用服务器创建连接
	}
	mPrintf(Log_NetWork, "http connect to %s OK! ", inet_ntoa(*((struct in_addr *)host->h_addr)));
	return sockfd;
}

//static char *mfHttpSend(int sockfd, TypeChar *buff, int len)
//{
//	if(buff == NULL) return NULL;
//	int tempIndex = 0;
//	int totalLen = len;
//	int tempRet = 0;
//	int mTimeOut = 3000000;
//	while((tempIndex < totalLen) && (mTimeOut > 0))
//	{
//		tempRet = write(sockfd, buff->buff + tempIndex, (size_t)(totalLen - tempIndex));
//		if(tempRet > 0)
//		{
//			tempIndex += tempRet;
//		}
//		else if(tempRet == -1)
//		{
//			return NULL;
//		}
//		if(tempIndex < totalLen)
//		{
//			usleep(1000);
//			mTimeOut -= 1000;
//		}
//	}
//	TypeChar *pTempChar = new TypeChar(2048);
//	buff->onClear();
//	//读取数据并分析
//	//暂时最大只处理1024个数据
//	totalLen = 0;
//	tempRet = 1;
//	char *pDataBuff = NULL;
//	mTimeOut = 3000000;//这里这个超时可能会有点不合理
//	//首先把所有http内容读取出来
//	while(mTimeOut > 0)
//	{
//		pTempChar->onClear();
//		tempRet = read(sockfd, pTempChar->buff, pTempChar->size);
//		if(tempRet > 0)
//		{
//			buff->onAddUBuff((uint32_t) totalLen, pTempChar->ubuff, tempRet);
//			totalLen += tempRet;
//			//解析出数据区
//			pDataBuff = strstr(buff->buff, "\r\n\r\n");
//			if(pDataBuff)
//			{
//				pDataBuff += 4;
//				char *tempLenStr = strstr(buff->buff, "Content-Length: ");
//				if(tempLenStr)
//				{
//					tempLenStr += 16;
//					//分析出长度
//					int httpBufLen = atoi(tempLenStr);
//					if(httpBufLen == (int)strlen(pDataBuff))
//					{
//						break;
//					}
//				}
//			}
//		}
//		pDataBuff = NULL;
//		//设置一个最大超时时间 3s
//		usleep(1000);
//		mTimeOut -= 1000;
//	}
//	delete pTempChar;
//	return pDataBuff;
//}
static char *mfHttpSend(int sockfd, TypeChar *buff, int len)
{
    if (buff == NULL) return NULL;
    int tempIndex = 0;
    int totalLen = len;
    int tempRet = 0;
    int mTimeOut = 3000000; // 3秒超时

    // --- 发送部分 ---
    while ((tempIndex < totalLen) && (mTimeOut > 0)) {
        tempRet = write(sockfd, buff->buff + tempIndex, (size_t)(totalLen - tempIndex));
        if (tempRet > 0) {
            tempIndex += tempRet;
        } else if (tempRet == -1) {
            return NULL;
        }
        if (tempIndex < totalLen) {
            usleep(1000);
            mTimeOut -= 1000;
        }
    }

    // --- 接收部分 ---
    TypeChar *pTempChar = new TypeChar(2048);
    buff->onClear();
    totalLen = 0;
    char *pDataBuff = NULL;
    mTimeOut = 3000000;

    while (mTimeOut > 0) {
        pTempChar->onClear();
        // 留一个字节位用于补零，防止溢出
        tempRet = read(sockfd, pTempChar->buff, pTempChar->size - 1);
        if (tempRet > 0) {
            buff->onAddUBuff((uint32_t)totalLen, pTempChar->ubuff, tempRet);
            totalLen += tempRet;

            // 🛑 关键：确保 buff 始终是合法的 C 字符串
            if (totalLen < (int)buff->size) {
                buff->buff[totalLen] = '\0';
            }

            pDataBuff = strstr(buff->buff, "\r\n\r\n");
            if (pDataBuff) {
                pDataBuff += 4;
                char *tempLenStr = strstr(buff->buff, "Content-Length: ");
                if (tempLenStr) {
                    tempLenStr += 16;
                    int httpBufLen = atoi(tempLenStr);
                    // 🛑 加固：防止因 strlen 找不到终止符导致的 SIGSEGV
                    if (httpBufLen <= (int)strlen(pDataBuff)) {
                        break;
                    }
                }
            }
        } else if (tempRet <= 0) { // 连接断开或错误
            break;
        }
        usleep(1000);
        mTimeOut -= 1000;
    }

    delete pTempChar; // 释放临时缓冲区
    return pDataBuff;
}
static char * mfGetGatewayBase64(uint64_t appid, const char *appsecret, char *gatewayBase64)
{
	char tmp[128] = {0};
	int n = 0;
	char m5[16] = {0};
	if(appid == 0 || !appsecret || !gatewayBase64 ) {
		return gatewayBase64;
	}
	// build signature string
	n = sprintf(tmp, "%lld:", appid);
	md5_ctx_t ctx;
	md5_begin(&ctx);
	md5_hash(appsecret, strlen(appsecret), &ctx);
	md5_end(m5, &ctx);
	md5sum_hex_encode(m5, tmp+n);
	n += 32;

	// calculate base64 hash
	int outlen = b64_encode_len(n);
	if(outlen > 256) {
		mPrintf(LOG_Robot, "OO: base64 length too large");
	}
	// TODO: check gatewaybase64 space ??
	n = b64_encode(tmp, n, gatewayBase64, outlen);
	gatewayBase64[n] = '\0';
	if(strlen(gatewayBase64) == 0) {
		mPrintf(Log_NetWork, "md5 error! ");
	}
	return gatewayBase64;
}

//////////////////////////////httpclient.c   结束///////////////////////////////////////////
