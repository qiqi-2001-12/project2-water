//
// Created by Administrator on 2017/6/28 0028.
//hwellyi created
//
#include <signal.h>
#include "../Main/WinobleMain.h"
#include "../NetWork/RobotDocking.h"
#include "../DataType/TypeDefine.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */
#ifdef WINOBLE_LINUX
//valgrind --leak-check=full --show-leak-kinds=all -v ./macSmartHome
//valgrind --leak-check=full ~/eclipse-workspace/SmartHome/Debug/SmartHome
//valgrind --leak-check=full --gen-suppressions=yes ~/Documents/SmartHome/app/src/main/cpp/macSmartHome
static void mfCtrlC(int s)
{
   printf("ctrl+c exit! free all memory %d\n",s);
   mIsExitFlag = FALSE;
   while(mThreadInfoList->size() > 0)
   {
	   sleep(1);
	   printf("count=%d\n", mThreadInfoList->size());
	   mPrintf(Log_Error, "{ ");
	   for(int i = 0; i < mThreadInfoList->size(); i++)
	   {
		   ((TypeThreadInfo *)mThreadInfoList->get(i))->onCloseReq();
	   }
	   printf("}\n");
   }
   printf("release protobuf!\n");
   google::protobuf::ShutdownProtobufLibrary();
   printf("delete pDataBase!\n");
   delete pDataBase;
   pDataBase = NULL;
   printf("delete pDeviceList!\n");
   delete pDeviceList;
   pDeviceList = NULL;
   printf("delete pmMasterSerialPort!\n");
   delete pmMasterSerialPort;
   pmMasterSerialPort = NULL;

   if(DUALZIGBEECHIP)
   {
		printf("delete pmSlaveSerialPort!\n");
		delete pmSlaveSerialPort;
		pmSlaveSerialPort = NULL;
   }
   printf("delete mThreadInfoList!\n");
   delete mThreadInfoList;
   delete mNetLogLinkList;
   printf("看清楚我再退出:memLeak mMemNewFreeCount = %d\n", mMemNewFreeCount);
   sleep(3);
#if (defined(HWELLYI_MT7688) || defined(H202_UK_SHA0))
   led_off(WAN_LED);
   gpio_unexport(BUZZER_GPIO19);
#endif
   exit(1);
}
int main()
{
#ifdef HWELLYI_DEBUG
	onSetDebugMode(TRUE);
#else
	onSetDebugMode(FALSE);
#endif
#ifdef H202_UK_SHA0
	mGatewayType = 3;//代表mini 海思网关
#else
	mGatewayType = 2;//代表mini 7688网关
#endif
	TypeChar * masterSName = new TypeChar(MASTER_SERIALPORT_DEV_NAME);
	TypeChar * slaveSName = new TypeChar(SLAVE_SERIALPORT_DEV_NAME);
	int32_t masterBaud = SERIAL_BAUD;
	int32_t slaveBaud = SERIAL_BAUD;
#else
#include "sys/stat.h"
static char* onJStringToChar(JNIEnv *env, jstring jstr, char *outbuf);
static void *onJniNotifiyJavaThread(void *arg);
static JavaVM *mGlobalJavaVM = NULL;
static jobject mGlobalObject = NULL;
JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onHYJniInit(JNIEnv *env, jobject thiz, jboolean printflag, jstring s1name, jint s1baud, jstring s2name, jint s2baud)
{
	onSetDebugMode(printflag);
	if(DUALZIGBEECHIP)
	{
		mGatewayType = 1;//代表带屏网关,4-代表4C网关(单模块网关)
	}
	else
	{
		mGatewayType = 4;//代表4C网关/单模块网关
	}
	TypeChar *masterSName = new TypeChar(64);
	TypeChar *slaveSName = new TypeChar(64);
	onJStringToChar(env, s1name, masterSName->buff);
	onJStringToChar(env, s2name, slaveSName->buff);
	int32_t masterBaud = s1baud;
	int32_t slaveBaud = s2baud;
#endif
	mPrintf(Log_Error, "mFlagPrintf = %s ", onCheckPrint() ? "TRUE" : "FALSE");
#if (defined(HWELLYI_MT7688) || defined(H202_UK_SHA0))
	//初始化MT7688的GPIO
	gpio_export(BUZZER_GPIO19);//
	gpio_set_direction(BUZZER_GPIO19, GPIO_DIR_OUT);//配置成输出
	gpio_write(BUZZER_GPIO19, GPIO_VALUE_LOW);
	led_on(WAN_LED);//程序启动  亮灯
#endif

	onAddThread("定时器", mfTimerThead, (char *)"");//定时器处理线程
	pDeviceList = new TypeDeviceList();
	pDataBase = new TypeDataBase();
	pmMasterSerialPort = new TypeSerialDrive((const char *)masterSName->buff, masterBaud, true);
	if(DUALZIGBEECHIP)
	{
		pmSlaveSerialPort = new TypeSerialDrive((const char *)slaveSName->buff, slaveBaud, false);

	}
	delete masterSName;
	delete slaveSName;
	onAddThread("串口", mfSerialPortThead, (char *)"");//串口处理线程
	onAddThread("TCP C", mfTCPNetWorkThread, (char *)"");//网络处理线程
	onAddThread("UDP S", mfRebotDockingThread, (char *)"");//机器人 公子小白对接
#ifdef WINOBLE_LINUX
	signal(SIGINT, mfCtrlC);
    while(mIsExitFlag)
    {
    	sleep(1);
    }
#endif
}
#ifndef WINOBLE_LINUX

static void *onJniNotifiyJavaThread(void *arg)
{
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	jmethodID mid = NULL;
	jclass cls = NULL;
	JNIEnv *mJniEnv = NULL;
	if(mGlobalJavaVM != NULL)
	{
		mGlobalJavaVM->AttachCurrentThread(&mJniEnv, NULL);
	}
	if(mJniEnv)
	{
		cls = mJniEnv->GetObjectClass(mGlobalObject);//mJniEnv->FindClass("com/hwellyi/smarthome/HYJniService");
		if(cls)
		{
			mid =mJniEnv->GetMethodID(cls,"onJniNotificationCB","(IJJJLjava/lang/String;)Z");
		}
		TypeJniNotificationInfo *tempJniNotify = NULL;

		while (!tempThreadInfo->outFlag && mIsExitFlag && mGlobalObject && mGlobalJavaVM) //循环读取数据
		{
			if(mJniNotifyLinkList->onGetCount() > 0)
			{
				tempJniNotify = (TypeJniNotificationInfo *)mJniNotifyLinkList->get();
				if(tempJniNotify)
				{
					if(mid && mGlobalObject && mGlobalJavaVM)
					{
						jstring tempJString = mJniEnv->NewStringUTF(tempJniNotify->strValue->buff);
						mJniEnv->CallBooleanMethod(mGlobalObject, mid, tempJniNotify->notifyID, tempJniNotify->lcmd1, tempJniNotify->lcmd2, tempJniNotify->lvalue, tempJString);
						mJniEnv->DeleteLocalRef(tempJString);
					}
				}
				delete tempJniNotify;
			}
			usleep(50000);//50ms check once
		}
	}
	mThreadInfoList->removeObject(tempThreadInfo);
	if(mGlobalJavaVM)
	{
		mGlobalJavaVM->DetachCurrentThread();
	}
	mGlobalJavaVM = NULL;
	mGlobalObject = NULL;
	mPrintf(Log_Error, "out jni-notify thread! ");
	return arg;
}

static char* onJStringToChar(JNIEnv *env, jstring jstr, char *buff)
{
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0)
	{
		memcpy(buff, ba, alen);
		buff[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	return buff;
}

JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onJYJniReRegisterEnvInfo(JNIEnv *env, jobject thiz)
{
	mPrintf(Log_Error, "reRegister Jni NotifyInfo! ");
	if(mGlobalJavaVM && mGlobalObject)
	{
		//先提出前面创建的线程
		if(mThreadInfoList)
		{
			TypeThreadInfo *tempThreadInfo = NULL;
			for(int i = 0; i < mThreadInfoList->size(); ++ i)
			{
				tempThreadInfo = (TypeThreadInfo *)mThreadInfoList->get(i);
				if(tempThreadInfo && (tempThreadInfo->title->onStringCMP("jni->java")))
				{
					//退出这个线程
					tempThreadInfo->outFlag = TRUE;
					break;
				}
			}
		}
	}
	//等待一下  再创建这个线程
	while(mGlobalObject)
	{
		usleep(10000);
	}
	env->GetJavaVM(&mGlobalJavaVM); //保存到全局变量中JVM
	mGlobalObject = env->NewGlobalRef(thiz);
	//这里创建一个线程，用来与给Java发送消息
	onAddThread("jni->java", onJniNotifiyJavaThread, (char *)"");//串口处理线程
}

JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onPrintLogToJni(JNIEnv *env, jobject thiz, jstring logstr)
{
	TypeChar *logChars = NULL;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr= (jbyteArray)env->CallObjectMethod(logstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0)
	{
		logChars = new TypeChar(alen + 1);

		memcpy(logChars->buff, ba, alen);
		logChars->buff[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	if(logChars)
	{
		mPrintf(LOG_Robot, "Log_View: %s ", logChars->buff);
	}
}

JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onRegisterNotifyFlag(JNIEnv *env, jobject thiz, jint flag)
{
	mNotifyRegisterFlag = flag;
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetZigbeeNetInfo(JNIEnv *env, jobject thiz)
{
	TypeChar * securityList = new TypeChar(256);
	sprintf(securityList->buff, "主 信道:%02d PANID:0x%04x %llx\n从 信道:%02d PANID:0x%04x %llx", pDataBase->onGetChannel(), pDataBase->onGetPANID(), pDataBase->onGetIEEE(),
	        pDataBase->onGetChannel_Ex(), pDataBase->onGetPANID_Ex(), pDataBase->onGetIEEE_EX());
	jstring jstr = env->NewStringUTF(securityList->buff);
	delete securityList;
	return jstr;
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetSerial(JNIEnv *env, jobject thiz)
{
	return env->NewStringUTF(pDataBase->onGetSerial());
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetToken(JNIEnv *env, jobject thiz)
{
	char tempBuff[256];
	memset(tempBuff, 0, 256);
	if(strlen(mHttpGetAccessToken()))
	{
		sprintf(tempBuff, "?access_token=%s", mHttpGetAccessToken());
	}
	return env->NewStringUTF(tempBuff);
}

JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onCheckSerial(JNIEnv *env, jobject thiz, jstring deviceuuid, jstring model, jstring defaultname)
{
	TypeChar * tempDevIEEE = new TypeChar(64);
	TypeChar * tempDevModel = new TypeChar(64);
	TypeChar * tempDevDefaultName = new TypeChar(64);
	onJStringToChar(env, deviceuuid, tempDevIEEE->buff);
	onJStringToChar(env, model, tempDevModel->buff);
	onJStringToChar(env, defaultname, tempDevDefaultName->buff);
	int64_t tempDeviceID = atol(tempDevIEEE->buff);
	if(pDataBase->onGetIEEE() != tempDeviceID)
	{
		pDataBase->onSetIEEE(tempDeviceID);
		pDataBase->onSetIEEE_EX(0);
	}
	//去服务器判断一下有没有注册
	if(mfHttpGetGatewayInfo() > 0)
	{
		//注册一下这个网关
		mfHttpRegistered(tempDeviceID, 0, tempDevModel->buff, tempDevDefaultName->buff);
	}
	delete tempDevIEEE;
	delete tempDevModel;
	delete tempDevDefaultName;
}

JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onDisAlarmInfo(JNIEnv *env, jobject thiz, jint devid, jint type)
{
	if(pDeviceList)
	{
		pDeviceList->onDisAlarmInfo(devid, type, true);
	}
}


JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onSetSceneStatus(JNIEnv *env, jobject thiz, jlong sceneid)
{
	//执行场景功能
	TypeSceneNameInfo *tempSceneNameInfo = NULL;
	if(sceneid == 100)
	{
		//执行所有叫回家的场景
		for(int i = 0; i < pDeviceList->sceneList->size(); ++ i)
		{
			tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			if(tempSceneNameInfo && tempSceneNameInfo->name->onStringCMP("回家"))
			{
				//执行这个场景
				pDeviceList->onSetSceneStatus(tempSceneNameInfo, !tempSceneNameInfo->status, TRUE);
			}
		}
	}
	else if(sceneid == 101)
	{
		//执行所有叫离家的场景
		for(int i = 0; i < pDeviceList->sceneList->size(); ++ i)
		{
			tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			if(tempSceneNameInfo && tempSceneNameInfo->name->onStringCMP("离家"))
			{
				//执行这个场景
				pDeviceList->onSetSceneStatus(tempSceneNameInfo, !tempSceneNameInfo->status, TRUE);
			}
		}
	}
	else if(sceneid == 102)
	{
		//执行所有叫睡觉的场景
		for(int i = 0; i < pDeviceList->sceneList->size(); ++ i)
		{
			tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			if(tempSceneNameInfo && tempSceneNameInfo->name->onStringCMP("睡觉"))
			{
				//执行这个场景
				pDeviceList->onSetSceneStatus(tempSceneNameInfo, !tempSceneNameInfo->status, TRUE);
			}
		}
	}
	else
	{
		for(int i = 0; i < pDeviceList->sceneList->size(); ++ i)
		{
			tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			if(tempSceneNameInfo && tempSceneNameInfo->scene_id == sceneid)
			{
				//执行这个场景
				pDeviceList->onSetSceneStatus(tempSceneNameInfo, !tempSceneNameInfo->status, TRUE);
				break;
			}
		}
	}
}

JNIEXPORT jboolean JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetNetWorkStatus(JNIEnv *env, jobject thiz)
{
	return (jboolean)onGetConnectFlag();
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetServerIP(JNIEnv *env, jobject thiz)
{
	TypeChar *tempChars = new TypeChar(100);
	sprintf(tempChars->buff, "%s:%d%s", SERVER_IPINFO, 80, SERVER_DEBUG);
	jstring jstr = env->NewStringUTF(tempChars->buff);
	delete tempChars;
	return jstr;
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetRoomList(JNIEnv *env, jobject thiz)
{
	TypeChar *tempChars = new TypeChar();
	cJSON *tempJson = cJSON_CreateObject();
	cJSON *subArray = cJSON_CreateArray();
	//添加设备到列表
	if(pDeviceList != NULL)
	{
		TypeRoomInfo *tempRoomInfo = NULL;
		//获取整个家庭的房间
		for(int i = 0; i < pDeviceList->roomList->size(); ++i)
		{
			tempRoomInfo = (TypeRoomInfo *)pDeviceList->roomList->get(i);
			if(tempRoomInfo)
			{
				cJSON *subJson = cJSON_CreateObject();
				cJSON_AddNumberToObject(subJson, "roomid", (uint32_t)tempRoomInfo->room_id);
				cJSON_AddNumberToObject(subJson, "iconid", (uint32_t)tempRoomInfo->iconID);
				cJSON_AddStringToObject(subJson, "name", tempRoomInfo->name->buff);
				cJSON_AddItemToArray(subArray, subJson);
			}
		}
	}
	cJSON_AddItemToObject(tempJson, "roomlist", subArray);
	char *retJsonChars = cJSON_Print(tempJson);
	TypeChar * roomList = new TypeChar(retJsonChars);
	cJSON_Delete(tempJson);
	free(retJsonChars);
	delete tempChars;
	jstring jstr = env->NewStringUTF(roomList->buff);
	delete roomList;
	return jstr;
}

JNIEXPORT jboolean JNICALL
Java_com_hwellyi_smarthome_HYJniService_onSetSceneGWHidden(JNIEnv *env, jobject thiz, jlong sceneid, jint value)
{
	TypeSceneNameInfo *tempSceneNameInfo = NULL;
	//找到这个场景
	for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
	{
		tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
		if(tempSceneNameInfo && (tempSceneNameInfo->scene_id == sceneid))
		{
			tempSceneNameInfo->hidden &= 0x0F;
			if(value)
			{
				tempSceneNameInfo->hidden |= 0x10;
			}
			//修改一下数据库
			pDataBase->onUpdateSceneNameInfo(tempSceneNameInfo, SceneHidden, tempSceneNameInfo->hidden);
			break;
		}
	}
	return TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetSceneList(JNIEnv *env, jobject thiz)
{
	TypeChar *tempChars = new TypeChar();
	cJSON *tempJson = cJSON_CreateObject();
	cJSON *subArray = cJSON_CreateArray();
	//添加设备到列表
	if(pDeviceList != NULL)
	{
		TypeSceneNameInfo *tempSceneNameInfo = NULL;
		//获取整个家庭的房间
		for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
		{
			tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
			if(tempSceneNameInfo && ((tempSceneNameInfo->hidden & 0x0F) == 0))
			{
				cJSON *subJson = cJSON_CreateObject();
				cJSON_AddLongNumberToObject(subJson, "id", (uint64_t)tempSceneNameInfo->scene_id);
				cJSON_AddNumberToObject(subJson, "iconid", (uint32_t)tempSceneNameInfo->icon_id);
				cJSON_AddNumberToObject(subJson, "roomid", (uint32_t)tempSceneNameInfo->room_id);
				cJSON_AddNumberToObject(subJson, "status", (uint32_t)tempSceneNameInfo->status);
				cJSON_AddNumberToObject(subJson, "hidden", (uint32_t)tempSceneNameInfo->hidden);
				cJSON_AddStringToObject(subJson, "name", tempSceneNameInfo->name->buff);
				cJSON_AddItemToArray(subArray, subJson);
			}
		}
	}
	cJSON_AddItemToObject(tempJson, "scenelist", subArray);
	char *retJsonChars = cJSON_Print(tempJson);
	TypeChar * roomList = new TypeChar(retJsonChars);
	cJSON_Delete(tempJson);
	free(retJsonChars);
	delete tempChars;
	jstring jstr = env->NewStringUTF(roomList->buff);
	delete roomList;
	return jstr;
}

JNIEXPORT void JNICALL
Java_com_hwellyi_smarthome_HYJniService_onCheckAlarmStatus(JNIEnv *env, jobject thiz)
{
	ListDeviceAlarmsRequest alarmsRequest;
	alarmsRequest.set_family_id(pDataBase->onGetFamilyID());
	alarmsRequest.set_offset(0);
	alarmsRequest.set_count(1);
	mfTCPCMDSend(CMD_ID_DEVICE_ALARM_LIST_REQ, alarmsRequest.SerializeAsString().c_str(), alarmsRequest.SerializeAsString().length());
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetApplianceList(JNIEnv *env, jobject thiz, jint flag)
{
	TypeChar *tempChars = new TypeChar();
	cJSON *tempJson = cJSON_CreateObject();
	cJSON *subArray = cJSON_CreateArray();
	//添加设备到列表
	if(pDeviceList != NULL)
	{
		TypeApplianceInfo *tempApplianceInfo = NULL;
		for(int i = 0; i < pDeviceList->applianceList->size(); ++ i)
		{
			tempApplianceInfo = (TypeApplianceInfo *) pDeviceList->applianceList->get(i);
			if(tempApplianceInfo && ((tempApplianceInfo->type == APPLIANCE_TYPE_AIR_CONDITION) || (tempApplianceInfo->type == APPLIANCE_TYPE_RGBW_LIGHT) || (tempApplianceInfo->type == APPLIANCE_TYPE_ELECTRIC_CURTAIN)))
			{
				cJSON *subJson = cJSON_CreateObject();
				cJSON_AddNumberToObject(subJson, "id", (uint32_t)tempApplianceInfo->appID);
				cJSON_AddNumberToObject(subJson, "apptype", (uint32_t)tempApplianceInfo->type);
				cJSON_AddNumberToObject(subJson, "roomid", (uint32_t)tempApplianceInfo->roomID);
				cJSON_AddStringToObject(subJson, "name", tempApplianceInfo->name->buff);
				cJSON_AddNumberToObject(subJson, "status", (uint32_t)tempApplianceInfo->value);
				cJSON_AddItemToArray(subArray, subJson);
			}
		}
	}
	cJSON_AddItemToObject(tempJson, "applist", subArray);
	char *retJsonChars = cJSON_Print(tempJson);
	TypeChar * securityList = new TypeChar(retJsonChars);
	cJSON_Delete(tempJson);
	free(retJsonChars);
	delete tempChars;
	jstring jstr = env->NewStringUTF(securityList->buff);
	delete securityList;
	return jstr;
}

JNIEXPORT jboolean JNICALL
Java_com_hwellyi_smarthome_HYJniService_onSetDeviceStatus(JNIEnv *env, jobject thiz, jint devid, jint subid, jint status)
{
	//先查找这个设备
	pDeviceList->onSetDeviceStatus(pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, devid), subid, status, TRUE);
	return TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_hwellyi_smarthome_HYJniService_onSetApplianceStatus(JNIEnv *env, jobject thiz, jint appid, jint status)
{
	//先查找这个家电
	TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(appid);
	if(tempApplianceInfo)
	{
		TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, tempApplianceInfo->ir_id);
		if(tempDBDeviceInfo && tempDBDeviceInfo->gatewayID)
		{
			if(tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID())
			{
				pDeviceList->onSetApplianceStatus(appid, status, "");
			}
			else
			{
				//发给服务器执行
				CtrlApplianceRequest ctrlApplianceRequest;
				ctrlApplianceRequest.set_appliance_id(tempApplianceInfo->appID);
				//ctrlApplianceRequest.set_user_id(pDataBase->onGetGateway_ID());
				ctrlApplianceRequest.set_key_id(status);
				mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CTRL_REQ, ctrlApplianceRequest.SerializeAsString().c_str(), ctrlApplianceRequest.SerializeAsString().length());
			}
		}
	}
	return TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetDeviceList(JNIEnv *env, jobject thiz, jint flag)
{
	TypeChar *tempChars = new TypeChar();
	cJSON *tempJson = cJSON_CreateObject();
	cJSON *subArray = cJSON_CreateArray();
	//添加设备到列表
	if(pDeviceList != NULL)
	{
		TypeDBDeviceInfo *dbDeviceInfo = NULL;
		TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
		//获取整个家庭的所有安防设备
		for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++i)
		{
			dbDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
			if(dbDeviceInfo && dbDeviceInfo->onLineFlag.bits.status)
			{
				for(int j = 1; j <= dbDeviceInfo->subCount; ++j)
				{
					tempDeviceTypeInfo = dbDeviceInfo->onGetSubInfo(j);
					if((tempDeviceTypeInfo != NULL) && ((1 << tempDeviceTypeInfo->devType) & flag))
					{
						//如果是灯光关联了场景 那这个灯光就不显示了
						if((tempDeviceTypeInfo->devType == SUB_DEVICE_TYPE_LIGHT) && (tempDeviceTypeInfo->subInfo.lightStatus->sceneID != 0))
						{
							continue;
						}
						cJSON *subJson = cJSON_CreateObject();
						cJSON_AddNumberToObject(subJson, "id", (uint32_t)tempDeviceTypeInfo->deviceID);
						cJSON_AddNumberToObject(subJson, "subid", (uint32_t)tempDeviceTypeInfo->subID);
						cJSON_AddNumberToObject(subJson, "iconid", (uint32_t)tempDeviceTypeInfo->iconID);
						cJSON_AddNumberToObject(subJson, "subtype", (uint32_t)tempDeviceTypeInfo->devType);
						cJSON_AddNumberToObject(subJson, "roomid", (uint32_t)tempDeviceTypeInfo->roomID);
						cJSON_AddStringToObject(subJson, "name", tempDeviceTypeInfo->name->buff);
						cJSON_AddNumberToObject(subJson, "status", (uint32_t)tempDeviceTypeInfo->onGetStatus());
						cJSON_AddItemToArray(subArray, subJson);
					}
				}
			}
		}
	}
	cJSON_AddItemToObject(tempJson, "devlist", subArray);
	char *retJsonChars = cJSON_Print(tempJson);
	TypeChar * securityList = new TypeChar(retJsonChars);
	cJSON_Delete(tempJson);
	free(retJsonChars);
	delete tempChars;
	jstring jstr = env->NewStringUTF(securityList->buff);
	delete securityList;
	return jstr;
}

JNIEXPORT jstring JNICALL
Java_com_hwellyi_smarthome_HYJniService_onGetDeviceTypeInfo(JNIEnv *env, jobject thiz, jint devid, jint type)
{
	TypeChar *tempChars = new TypeChar();
	cJSON *tempJson = cJSON_CreateObject();
	cJSON *subArray = cJSON_CreateArray();
	//添加设备到列表
	if(pDeviceList != NULL)
	{
		TypeDBDeviceInfo *dbDeviceInfo = NULL;
		TypeDeviceTypeInfo *tempDeviceTypeInfo = NULL;
		//获取整个家庭的所有安防设备
		for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++i)
		{
			dbDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
			if(dbDeviceInfo && dbDeviceInfo->onLineFlag.bits.status)
			{
				for(int j = 1; j <= dbDeviceInfo->subCount; ++j)
				{
					tempDeviceTypeInfo = dbDeviceInfo->onGetSubInfo(j);
					if((tempDeviceTypeInfo != NULL) && ((1 << tempDeviceTypeInfo->devType) & type))
					{
						cJSON *subJson = cJSON_CreateObject();
                        cJSON_AddNumberToObject(subJson, "id", (uint32_t)tempDeviceTypeInfo->deviceID);
                        cJSON_AddNumberToObject(subJson, "subtype", (uint32_t)tempDeviceTypeInfo->devType);
                        TypeRoomInfo *tempRoomInfo = NULL;
                        //获取整个家庭的房间
                        for(int i = 0; i < pDeviceList->roomList->size(); ++i)
                        {
                            tempRoomInfo = (TypeRoomInfo *)pDeviceList->roomList->get(i);
                            if(tempDeviceTypeInfo->roomID == tempRoomInfo->room_id)
                            {
                                cJSON_AddStringToObject(subJson, "room", tempRoomInfo->name->buff);
                                break;
                            }
                        }
						cJSON_AddStringToObject(subJson, "name", tempDeviceTypeInfo->name->buff);
						switch(tempDeviceTypeInfo->devType)
						{
//							case SUB_DEVICE_TYPE_LIGHT:retStatus = tempDeviceTypeInfo->subInfo.lightStatus->status;break;
							case SUB_DEVICE_TYPE_DIMMER:{
                                cJSON_AddNumberToObject(subJson, "Level", (uint32_t)tempDeviceTypeInfo->subInfo.dimmingStatus->paraValue);
                                cJSON_AddNumberToObject(subJson, "newLevel", (uint32_t)tempDeviceTypeInfo->subInfo.dimmingStatus->saveParaValue);
                            }break;
//							case SUB_DEVICE_TYPE_CURTAIN:retStatus = subInfo.curtainStatus->status;break;
//							case SUB_DEVICE_TYPE_SWITCH:retStatus = subInfo.switchStatus->status;break;
//							case SUB_DEVICE_TYPE_GAS:retStatus = subInfo.gasStatus->gasValue;break;
//							case SUB_DEVICE_TYPE_IR_REMOTE:retStatus = subInfo.irRemoteStatus->status;break;
//							case SUB_DEVICE_TYPE_PIR:retStatus = subInfo.pirStatus->securityStatus;break;
//							case SUB_DEVICE_TYPE_SMOKE:retStatus = subInfo.smokeStatus->status;break;
//							case SUB_DEVICE_TYPE_FLOOD:retStatus = subInfo.floodStatus->status;break;
//							case SUB_DEVICE_TYPE_SOS:retStatus = subInfo.sosStatus->status;break;
//							case SUB_DEVICE_TYPE_DOOR_LOCK:retStatus = subInfo.doorLockStatus->status;break;
//							case SUB_DEVICE_TYPE_DOOR_WINDOW:retStatus = subInfo.doorWindowStatus->securityStatus;break;
							case SUB_DEVICE_TYPE_ENV_DETECTOR:
							{
                                cJSON_AddNumberToObject(subJson, "temp", (uint32_t)tempDeviceTypeInfo->subInfo.env_detectorStatus->tempSensorValue);
                                cJSON_AddNumberToObject(subJson, "humi", (uint32_t)tempDeviceTypeInfo->subInfo.env_detectorStatus->humiSensorValue);
                                cJSON_AddNumberToObject(subJson, "illum", (uint32_t)tempDeviceTypeInfo->subInfo.env_detectorStatus->illumSensorValue);
                                cJSON_AddNumberToObject(subJson, "PM25", (uint32_t)tempDeviceTypeInfo->subInfo.env_detectorStatus->pm25Value);
                                cJSON_AddNumberToObject(subJson, "CO2", (uint32_t)tempDeviceTypeInfo->subInfo.env_detectorStatus->CO2Value);
                                cJSON_AddNumberToObject(subJson, "Airlevel", (uint32_t)tempDeviceTypeInfo->subInfo.env_detectorStatus->airLevel);
							}
                             break;
//							case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:retStatus = subInfo.waterLeakStatus->value_status;break;
//							case SUB_DEVICE_TYPE_GAS_ARM:retStatus = subInfo.gasArmStatus->value_status;break;
//							case SUB_DEVICE_TYPE_CLOTHES_HANGER:retStatus = subInfo.clothesHangerStatus->status;break;
							default:tempChars = 0;break;
						}
						cJSON_AddItemToArray(subArray, subJson);
					}
				}
			}
		}
	}
	cJSON_AddItemToObject(tempJson, "devlist", subArray);
	char *retJsonChars = cJSON_Print(tempJson);
	TypeChar * securityList = new TypeChar(retJsonChars);
	cJSON_Delete(tempJson);
	free(retJsonChars);
	delete tempChars;
	jstring jstr = env->NewStringUTF(securityList->buff);
	delete securityList;
	return jstr;
}

#endif

#ifdef __cplusplus
}
#endif /* __cplusplus */
