/*
 * PublicDefine.cpp
 *
 *  Created on: Jun 30, 2017
 *      Author: root
 */
#include <stdio.h>
#include <time.h>
#include<pthread.h>
#include "../Main/WinobleMain.h"

uint32_t mMemNewFreeCount = 0;//内存泄漏监控标志
TypeDeviceList *pDeviceList = NULL;
TypeDataBase *pDataBase = NULL;
TypeSerialDrive *pmMasterSerialPort = NULL;
TypeSerialDrive *pmSlaveSerialPort = NULL;
bool mIsUpdateRobotFlag = false;
bool mIsDownLoadingFlag = false;
bool mIsExitFlag = TRUE;
int32_t mGatewayType = 0;//1=带屏网关 2=mini 7688网关 3=mini 海思网关
int32_t mIsAlarmingFlag = 0;
bool mTcpReciveFlag = FALSE;
int32_t mNotifyRegisterFlag = 0;

TypeArrayList * mThreadInfoList = new TypeArrayList(ArrayTypeThreadInfo);
TypeLinkedList *mJniNotifyLinkList = new TypeLinkedList(ArrayTypeJniNotificationInfo);
void onPublicInit()
{

}

static bool mFlagPrintf = FALSE;
bool onCheckPrint()
{
	if((onCheckNetPrint()) || mFlagPrintf)
	{
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

bool onCheckDebugMode()
{
	return mFlagPrintf;
}

void onSetDebugMode(bool flag)
{
	mFlagPrintf = flag;
}

bool mPrintf(int TAG, const char *fmt, ...)
{
	//程序本身不再打印
	//只有网络连接的时候才打印
	if(onCheckPrint())
	{
		va_list arg;
		va_start(arg, fmt);
		if(onCheckNetPrint())
		{
			onNetPrint(TAG, fmt, &arg);
		}
		if(onCheckDebugMode())
		{
#ifdef WINOBLE_LINUX//Linux下暂时不打印
#ifdef HWELLYI_DEBUG
			printf("%s ", mTAGLogString[TAG]);
			vprintf(fmt, arg);
			printf("\n");
#endif
#else
			__android_log_vprint(ANDROID_LOG_INFO, mTAGLogString[TAG], fmt, arg);
#endif
		}
		va_end(arg);
	}
	return TRUE;
}

uint8_t onGetAFSendSeq()
{
	static uint8_t retSeq = 0;
	return ++retSeq;
}

bool onAddThread(const char *title, void* (*__start_routine)(void*), char *para)
{
	pthread_attr_t attr;
	pthread_attr_init (&attr);
	pthread_attr_setdetachstate (&attr, PTHREAD_CREATE_DETACHED);
	TypeThreadInfo *threadInfo = new TypeThreadInfo((char *)title, para);
	pthread_create(&threadInfo->threadID, &attr, __start_routine, threadInfo);//串口处理线程
	pthread_attr_destroy (&attr);
	mThreadInfoList->add(threadInfo);
	return TRUE;
}

uint8_t onGetZclSendSeq()
{
	static uint8_t retSeq = 0;
	return ++retSeq;
}

int32_t mfPublicGetInt32(char *string)
{
	int32_t retInt32 = 0;
	char tempChar = 0;
	bool flag = FALSE;
	if(string)
	{
		if(strlen(string) > 0)
		{
			if(*string == '-')
			{
				flag = true;
				string++;
			}
		}
		while(*string)
		{
			tempChar = *string++;
			if((tempChar >= '0') && (tempChar <= '9'))
				tempChar -= '0';
			else
				tempChar = 0;
			retInt32 = retInt32 * 10 + tempChar;
		}
	}
	if(flag)
	{
		retInt32 = 0 - retInt32;
	}
	return (retInt32 & 0xFFFFFFFF);
}

int64_t mfPublicGetInt64(char *string)
{
	int64_t retInt64 = 0;
	char tempChar = 0;
	bool flag = FALSE;
	if(string)
	{
		if(strlen(string) > 0)
		{
			if(*string == '-')
			{
				flag = true;
				string++;
			}
		}
		while(*string)
		{
			tempChar = *string++;
			if((tempChar >= '0') && (tempChar <= '9'))
				tempChar -= '0';
			else
				tempChar = 0;
			retInt64 = retInt64 * 10 + tempChar;
		}
	}
	if(flag)
	{
		retInt64 = 0 - retInt64;
	}
	return retInt64;
}

uint8_t mf4CharToHex(uint8_t value)
{
	uint8_t msRetChar = 0;
	value = (uint8_t) (value & 0x0F);
	if(value <= 9)
	{
		msRetChar = (uint8_t) (value + 0x30);
	}
	else
	{
		msRetChar = (uint8_t) (value + 0x57);
	}
	return msRetChar;
}

uint8_t mfHexToChar(uint8_t value)
{
	uint8_t retChar = 0;
	if((value >= '0') && (value <= '9'))
	{
		retChar = (uint8_t)(value - '0');
	}
	else if((value >= 'a') && (value <= 'f'))
	{
		retChar = (uint8_t)(value - 'a' + 10);
	}
	else if((value >= 'A') && (value <= 'F'))
	{
		retChar = (uint8_t)(value - 'A' + 10);
	}
	return retChar;
}

char *onPrintfUBuff(uint8_t *value, int len, char *outbuff)
{
	if(len > 80) len = 80;
	if(value != NULL)
	{
		for(int i = 0; i < len; i++)
		{
			outbuff[3 * i] = mf4CharToHex((uint8_t) ((value[i] >> 4) & 0x0F));
			outbuff[3 * i + 1] = mf4CharToHex((uint8_t) (value[i] & 0x0F));
			outbuff[3 * i + 2] = ' ';
		}
	}

	return outbuff;
}

uint32_t onGetInt32Ex(uint8_t *buff, uint32_t len)
{
	uint32_t retInt32_t = 0;
	if(len > 4)
		len = 4;
	for(uint32_t i = 0; i < len; ++i)
	{
		retInt32_t = (retInt32_t << 8) + buff[len - i - 1];
	}
	return retInt32_t;
}

uint32_t onGetInt32(uint8_t *buff, uint32_t len)
{
	uint32_t retInt32_t = 0;
	if(len > 4)
		len = 4;
	for(uint32_t i = 0; i < len; ++i)
	{
		retInt32_t = (retInt32_t << 8) + buff[i];
	}
	return retInt32_t;
}

uint64_t onGetInt64Ex(uint8_t *buff, uint32_t len)
{
	uint64_t retInt64_t = 0;
	if(len > 8)
		len = 8;
	for(uint32_t i = 0; i < len; ++i)
	{
		retInt64_t = (retInt64_t << 8) + buff[len - i - 1];
	}
	return retInt64_t;
}

uint64_t onGetInt64(uint8_t *buff, uint32_t len)
{
	uint64_t retInt64_t = 0;
	if(len > 8)
		len = 8;
	for(uint32_t i = 0; i < len; ++i)
	{
		retInt64_t = (retInt64_t << 8) + buff[i];
	}
	return retInt64_t;
}

bool onIsGoodTemp(int32_t value)
{
	if((value >= -50) && (value <= 60))
	{
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

const char *mGetNetCMDString(int32_t commandid)
{
	switch(commandid)
	{
		case CMD_ID_DEVICE_TEST_GET_ID_REQ:
			return "CMD_ID_DEVICE_TEST_GET_ID_REQ ";
		case CMD_ID_DEVICE_VALUE_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_VALUE_CHANGED_NOTIFY";
		case CMD_ID_DEVICE_TEST_GET_ID_RES:
			return "CMD_ID_DEVICE_TEST_GET_ID_RES ";
		case CMD_ID_GATEWAY_DEL_RES:
			return "CMD_ID_GATEWAY_DEL_RES";
		case CMD_ID_GATEWAY_TEST_GET_ID_REQ:
			return "CMD_ID_GATEWAY_TEST_GET_ID_REQ ";
		case CMD_ID_GATEWAY_TEST_GET_ID_RES:
			return "CMD_ID_GATEWAY_TEST_GET_ID_RES ";
		case CMD_ID_DEVICE_ADD_REQ:
			return "CMD_ID_DEVICE_ADD_REQ ";
		case CMD_ID_DEVICE_ADD_RES:
			return "CMD_ID_DEVICE_ADD_RES ";
		case CMD_ID_DEVICE_GET_INFO_REQ:
			return "CMD_ID_DEVICE_GET_INFO_REQ ";
		case CMD_ID_DEVICE_GET_ALL_REQ:
			return "CMD_ID_DEVICE_GET_ALL_REQ ";
		case CMD_ID_DEVICE_GET_INFO_RES:
			return "CMD_ID_DEVICE_GET_INFO_RES ";
		case CMD_ID_DEVICE_DEL_REQ :
			return "CMD_ID_DEVICE_DEL_REQ ";
		case CMD_ID_DEVICE_DEL_RES :
			return "CMD_ID_DEVICE_DEL_RES ";
		case CMD_ID_DEVICE_MOD_INFO_REQ :
			return "CMD_ID_DEVICE_MOD_INFO_REQ ";

		case CMD_ID_DEVICE_STATUS_UPDATE_REQ:
			return "CMD_ID_DEVICE_STATUS_UPDATE_REQ";

		case CMD_ID_DEVICE_STATUS_CHANGED_NOTIFY :
			return "CMD_ID_DEVICE_STATUS_CHANGED_NOTIFY ";

		case CMD_ID_DEVICE_VALUE_UPDATE_REQ :
			return "CMD_ID_DEVICE_VALUE_UPDATE_REQ ";

		case CMD_ID_ROOM_GET_INFO_RES  :
			return "CMD_ID_ROOM_GET_INFO_RES";

		case CMD_ID_ROOM_MOD_INFO_REQ  :
			return "CMD_ID_ROOM_MOD_INFO_REQ";

		case CMD_ID_ROOM_MOD_INFO_RES  :
			return "CMD_ID_ROOM_MOD_INFO_RES";

		case CMD_ID_ROOM_DEL_REQ  :
			return "CMD_ID_ROOM_DEL_REQ";

		case CMD_ID_ROOM_DEL_RES  :
			return "CMD_ID_ROOM_DEL_RES";

		case CMD_ID_GATEWAY_ADD_REQ  :
			return "CMD_ID_GATEWAY_ADD_REQ";

		case CMD_ID_GATEWAY_ADD_RES :
			return "CMD_ID_GATEWAY_ADD_RES";

		case CMD_ID_GATEWAY_GET_ALL_REQ :
			return "CMD_ID_GATEWAY_GET_ALL_REQ";

		case CMD_ID_GATEWAY_GET_ALL_RES  :
			return "CMD_ID_GATEWAY_GET_ALL_RES ";

		case CMD_ID_GATEWAY_GET_INFO_REQ  :
			return "CMD_ID_GATEWAY_GET_INFO_REQ ";

		case CMD_ID_GATEWAY_MOD_INFO_REQ   :
			return "CMD_ID_GATEWAY_MOD_INFO_REQ ";

		case CMD_ID_GATEWAY_STATUS_UPDATE_REQ :
			return "CMD_ID_GATEWAY_STATUS_UPDATE_REQ";

		case CMD_ID_GATEWAY_STATUS_UPDATE_RES :
			return "CMD_ID_GATEWAY_STATUS_UPDATE_RES";

		case CMD_ID_GATEWAY_DEL_REQ :
			return "CMD_ID_GATEWAY_DEL_REQ ";

		case CMD_ID_FAMILY_APPLY_JOIN_REQ :
			return "CMD_ID_FAMILY_APPLY_JOIN_REQ ";

		case CMD_ID_FAMILY_APPLY_JOIN_RES :
			return "CMD_ID_FAMILY_APPLY_JOIN_RES ";

		case CMD_ID_FAMILY_INVITE_JOIN_REQ  :
			return "CMD_ID_FAMILY_INVITE_JOIN_REQ ";

		case CMD_ID_FAMILY_INVITE_NOTIFY :
			return "CMD_ID_FAMILY_INVITE_NOTIFY ";

		case CMD_ID_FAMILY_INVITE_JOIN_RES :
			return "CMD_ID_FAMILY_INVITE_JOIN_RES ";

		case CMD_ID_FAMILY_DEL_REQ :
			return "CMD_ID_FAMILY_DEL_REQ ";

		case CMD_ID_FAMILY_DEL_RES :
			return "CMD_ID_FAMILY_DEL_RES ";

		case CMD_ID_ROOM_ADD_REQ :
			return "CMD_ID_ROOM_ADD_REQ ";

		case CMD_ID_ROOM_ADD_RES :
			return "CMD_ID_ROOM_ADD_RES ";

		case CMD_ID_ROOM_GET_ALL_REQ :
			return "CMD_ID_ROOM_GET_ALL_REQ ";

		case CMD_ID_ROOM_GET_INFO_REQ  :
			return "CMD_ID_ROOM_GET_INFO_REQ ";

		case CMD_ID_FAMILY_GET_MEMBERS_RES:
			return "CMD_ID_FAMILY_GET_MEMBERS_RES";

		case CMD_ID_FAMILY_GET_INFO_REQ :
			return "CMD_ID_FAMILY_GET_INFO_REQ ";

		case CMD_ID_FAMILY_GET_INFO_RES :
			return "CMD_ID_FAMILY_GET_INFO_RES ";

		case CMD_ID_FAMILY_MOD_INFO_REQ:
			return "CMD_ID_FAMILY_MOD_INFO_REQ ";

		case CMD_ID_FAMILY_MOD_INFO_RES :
			return "CMD_ID_FAMILY_MOD_INFO_RES ";

		case CMD_ID_FAMILY_SELECT_REQ :
			return "CMD_ID_FAMILY_SELECT_REQ ";

		case CMD_ID_FAMILY_SELECT_RES :
			return "CMD_ID_FAMILY_SELECT_RES ";

		case CMD_ID_FAMILY_GET_MEMBERS_REQ:
			return "CMD_ID_FAMILY_GET_MEMBERS_REQ ";

		case CMD_ID_FAMILY_GET_ALL_RES:
			return "CMD_ID_FAMILY_GET_ALL_RES ";

		case CMD_ID_FAMILY_GET_ALL_REQ:
			return "CMD_ID_FAMILY_GET_ALL_REQ ";

		case CMD_ID_FAMILY_ADD_RES:
			return "CMD_ID_FAMILY_ADD_RES ";

		case CMD_ID_FAMILY_ADD_REQ :
			return "CMD_ID_FAMILY_ADD_REQ ";

		case CMD_ID_KICK_USER_NOTIFY :
			return "CMD_ID_KICK_USER_NOTIFY ";

		case CMD_ID_AUTH_TOKEN_REQ:
			return "CMD_ID_AUTH_TOKEN_REQ ";
		case CMD_ID_FAMILY_ACTION_MSG_NOTIFY:
			return "CMD_ID_FAMILY_ACTION_MSG_NOTIFY";
		case CMD_ID_PING:
			return "CMD_ID_PING ";

		case CMD_ID_AUTH_TOKEN_RES:
			return "CMD_ID_AUTH_TOKEN_RES";

		case CMD_ID_GATEWAY_GET_INFO_RES:
			return "CMD_ID_GATEWAY_GET_INFO_RES";

		case CMD_ID_PONG:
			return "CMD_ID_PONG";

		case CMD_ID_DEVICE_GET_ALL_RES:
			return "CMD_ID_DEVICE_GET_ALL_RES";

		case CMD_ID_GATEWAY_STATUS_CHANGED_NOTIFY:
			return "CMD_ID_GATEWAY_STATUS_CHANGED_NOTIFY";

		case CMD_ID_DEVICE_MOD_INFO_RES:
			return "CMD_ID_DEVICE_MOD_INFO_RES";

		case CMD_ID_DEVICE_STATUS_UPDATE_RES:
			return "CMD_ID_DEVICE_STATUS_UPDATE_RES";

		case CMD_ID_GATEWAY_MOD_INFO_RES:
			return "CMD_ID_GATEWAY_MOD_INFO_RES";

		case CMD_ID_DEVICE_CTRL_RES:
			return "CMD_ID_DEVICE_CTRL_RES";

		case CMD_ID_DEVICE_VALUE_UPDATE_RES:
			return "CMD_ID_DEVICE_VALUE_UPDATE_RES";

		case CMD_ID_GATEWAY_INFO_CHNAGED_NOTIFY:
			return "CMD_ID_GATEWAY_INFO_CHNAGED_NOTIFY";

		case CMD_ID_GATEWAY_ADD_NOTIFY:
			return "CMD_ID_GATEWAY_ADD_NOTIFY";

		case CMD_ID_GATEWAY_DEL_NOTIFY:
			return "CMD_ID_GATEWAY_DEL_NOTIFY";

		case CMD_ID_DEVICE_ADD_NOTIFY:
			return "CMD_ID_DEVICE_ADD_NOTIFY";

		case CMD_ID_DEVICE_ADDED_NOTIFY:
			return "CMD_ID_DEVICE_ADDED_NOTIFY";

		case CMD_ID_DEVICE_DEL_NOTIFY:
			return "CMD_ID_DEVICE_DEL_NOTIFY";

		case CMD_ID_DEVICE_INFO_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_INFO_CHANGED_NOTIFY";

		case CMD_ID_DEVICE_CTRL_REQ :
			return "CMD_ID_DEVICE_CTRL_REQ";

		case CMD_ID_ACK:
			return "CMD_ID_ACK";

			//2017/08/30添加
		case CMD_ID_SCENE_ADD_REQ:
			return "CMD_ID_SCENE_ADD_REQ";

		case CMD_ID_SCENE_ADD_RES:
			return "CMD_ID_SCENE_ADD_RES";

		case CMD_ID_SCENE_GET_REQ:
			return "CMD_ID_SCENE_GET_REQ";

		case CMD_ID_SCENE_GET_RES:
			return "CMD_ID_SCENE_GET_RES";

		case CMD_ID_SCENE_LIST_REQ:
			return "CMD_ID_SCENE_LIST_REQ";

		case CMD_ID_SCENE_LIST_RES:
			return "CMD_ID_SCENE_LIST_RES";

		case CMD_ID_SCENE_MOD_REQ:
			return "CMD_ID_SCENE_MOD_REQ";

		case CMD_ID_SCENE_MOD_RES:
			return "CMD_ID_SCENE_MOD_RES";

		case CMD_ID_SCENE_DEL_REQ:
			return "CMD_ID_SCENE_DEL_REQ";

		case CMD_ID_SCENE_DEL_RES:
			return "CMD_ID_SCENE_DEL_RES";

		case CMD_ID_SCENE_DETAIL_GET_REQ:
			return "CMD_ID_SCENE_DETAIL_GET_REQ";

		case CMD_ID_SCENE_DETAIL_GET_RES:
			return "CMD_ID_SCENE_DETAIL_GET_RES";

		case CMD_ID_SCENE_ACTION_ADD_REQ:
			return "CMD_ID_SCENE_ACTION_ADD_REQ";

		case CMD_ID_SCENE_ACTION_ADD_RES:
			return "CMD_ID_SCENE_ACTION_ADD_RES";

		case CMD_ID_SCENE_ACTION_LIST_REQ:
			return "CMD_ID_SCENE_ACTION_LIST_REQ";

		case CMD_ID_SCENE_ACTION_LIST_RES:
			return "CMD_ID_SCENE_ACTION_LIST_RES";

		case CMD_ID_SCENE_ACTION_GET_REQ:
			return "CMD_ID_SCENE_ACTION_GET_REQ";

		case CMD_ID_SCENE_ACTION_GET_RES:
			return "CMD_ID_SCENE_ACTION_GET_RES";

		case CMD_ID_SCENE_ACTION_MOD_REQ:
			return "CMD_ID_SCENE_ACTION_MOD_REQ";

		case CMD_ID_SCENE_ACTION_MOD_RES:
			return "CMD_ID_SCENE_ACTION_MOD_RES";

		case CMD_ID_SCENE_ACTION_DEL_REQ:
			return "CMD_ID_SCENE_ACTION_DEL_REQ";

		case CMD_ID_SCENE_ACTION_DEL_RES:
			return "CMD_ID_SCENE_ACTION_DEL_RES";

		case CMD_ID_SCENE_COND_ADD_REQ:
			return "CMD_ID_SCENE_COND_ADD_REQ";

		case CMD_ID_SCENE_COND_ADD_RES:
			return "CMD_ID_SCENE_COND_ADD_RES";

		case CMD_ID_SCENE_COND_LIST_REQ:
			return "CMD_ID_SCENE_COND_LIST_REQ";

		case CMD_ID_SCENE_COND_LIST_RES:
			return "CMD_ID_SCENE_COND_LIST_RES";

		case CMD_ID_SCENE_COND_GET_REQ:
			return "CMD_ID_SCENE_COND_GET_REQ";

		case CMD_ID_SCENE_COND_GET_RES:
			return "CMD_ID_SCENE_COND_GET_RES";

		case CMD_ID_SCENE_COND_MOD_REQ:
			return "CMD_ID_SCENE_COND_MOD_REQ";

		case CMD_ID_SCENE_COND_MOD_RES:
			return "CMD_ID_SCENE_COND_MOD_RES";

		case CMD_ID_SCENE_COND_DEL_REQ:
			return "CMD_ID_SCENE_COND_DEL_REQ";

		case CMD_ID_SCENE_COND_DEL_RES:
			return "CMD_ID_SCENE_COND_DEL_RES";

		case CMD_ID_SCENE_INFO_CHNAGED_NOTIFY:
			return "CMD_ID_SCENE_INFO_CHNAGED_NOTIFY";

		case CMD_ID_SCENE_ACTION_INFO_CHANGED_NOTIFY:
			return "CMD_ID_SCENE_ACTION_INFO_CHANGED_NOTIFY";
		case CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_REQ:
			return "CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_REQ";
		case CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_RES:
			return "CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_RES";
		case CMD_ID_DEVICE_GAS_ARM_BINDING_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_GAS_ARM_BINDING_CHANGED_NOTIFY";
		case CMD_ID_SCENE_COND_INFO_CHANGED_NOTIFY:
			return "CMD_ID_SCENE_COND_INFO_CHANGED_NOTIFY";

		case CMD_ID_SCENE_DELETED_NOTIFY:
			return "CMD_ID_SCENE_DELETED_NOTIFY";

		case CMD_ID_SCENE_ACTION_DELETED_NOTIFY:
			return "CMD_ID_SCENE_ACTION_DELETED_NOTIFY";

		case CMD_ID_SCENE_COND_DELETED_NOTIFY:
			return "CMD_ID_SCENE_COND_DELETED_NOTIFY";
		case CMD_ID_ICON_FONT_BITMAP_GET_REQ:
			return "CMD_ID_ICON_FONT_BITMAP_GET_REQ";
		case CMD_ID_ICON_FONT_BITMAP_GET_RES:
			return "CMD_ID_ICON_FONT_BITMAP_GET_RES";
		case CMD_ID_SCENE_ADDED_NOTIFY:
			return "CMD_ID_SCENE_ADDED_NOTIFY";

		case CMD_ID_SCENE_ACTION_ADDED_NOTIFY:
			return "CMD_ID_SCENE_ACTION_ADDED_NOTIFY";

		case CMD_ID_SCENE_COND_ADDED_NOTIFY:
			return "CMD_ID_SCENE_COND_ADDED_NOTIFY";

		case CMD_ID_DEVICE_ALARM_REQ:
			return "CMD_ID_DEVICE_ALARM_REQ";

		case CMD_ID_DEVICE_ALARM_RES:
			return "CMD_ID_DEVICE_ALARM_RES";

		case CMD_ID_DEVICE_ALARM_NOTIFY:
			return "CMD_ID_DEVICE_ALARM_NOTIFY";
		case CMD_ID_DEVICE_ALARM_RELEASED_NOTIFY:
			return "CMD_ID_DEVICE_ALARM_RELEASED_NOTIFY";
		case CMD_ID_DEVICE_RGBW_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_RGBW_CHANGED_NOTIFY";
		case CMD_ID_ROOM_ENV_UPDATE_REQ:
			return "CMD_ID_ROOM_ENV_UPDATE_REQ";
		case CMD_ID_ROOM_ENV_UPDATE_RES:
			return "CMD_ID_ROOM_ENV_UPDATE_RES";
		case CMD_ID_ROOM_ENV_CHANGED_NOTIFY:
			return "CMD_ID_ROOM_ENV_CHANGED_NOTIFY";
		case CMD_ID_PUSH_MSG_PRIVATE_REQ:
			return "CMD_ID_PUSH_MSG_PRIVATE_REQ";
		case CMD_ID_PUSH_MSG_PRIVATE_RES:
			return "CMD_ID_PUSH_MSG_PRIVATE_RES";
		case CMD_ID_DEVICE_EVENT_BROADCAST_REQ:
			return "CMD_ID_DEVICE_EVENT_BROADCAST_REQ";
		case CMD_ID_DEVICE_EVENT_BROADCAST_RES:
			return "CMD_ID_DEVICE_EVENT_BROADCAST_RES";
		case CMD_ID_DEVICE_EVENT_BROADCAST_NOTIFY:
			return "CMD_ID_DEVICE_EVENT_BROADCAST_NOTIFY";
		case CMD_ID_DEVICE_APPLIANCE_CMD_LIST_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LIST_REQ";
		case CMD_ID_DEVICE_APPLIANCE_CMD_LIST_RES:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LIST_RES";
		case CMD_ID_DEVICE_APPLIANCE_ADD_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_ADD_REQ";
		case CMD_ID_DEVICE_APPLIANCE_ADD_RES:
			return "CMD_ID_DEVICE_APPLIANCE_ADD_RES";
		case CMD_ID_DEVICE_APPLIANCE_ADDED_NOTIFY:
			return "CMD_ID_DEVICE_APPLIANCE_ADDED_NOTIFY";
		case CMD_ID_DEVICE_APPLIANCE_DEL_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_DEL_REQ";
		case CMD_ID_DEVICE_APPLIANCE_DEL_RES:
			return "CMD_ID_DEVICE_APPLIANCE_DEL_RES";
		case CMD_ID_DEVICE_APPLIANCE_DELETED_NOTIFY:
			return "CMD_ID_DEVICE_APPLIANCE_DELETED_NOTIFY";
		case CMD_ID_DEVICE_APPLIANCE_LIST_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_LIST_REQ";
		case CMD_ID_DEVICE_APPLIANCE_LIST_RES:
			return "CMD_ID_DEVICE_APPLIANCE_LIST_RES";
		case CMD_ID_DEVICE_APPLIANCE_MODIFY_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_MODIFY_REQ";
		case CMD_ID_DEVICE_APPLIANCE_MODIFY_RES:
			return "CMD_ID_DEVICE_APPLIANCE_MODIFY_RES";
		case CMD_ID_DEVICE_APPLIANCE_INFO_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_APPLIANCE_INFO_CHANGED_NOTIFY";
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_REQ";
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_RES:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_RES";
		case  CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_UPDATE_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_UPDATE_REQ";
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_UPDATE_RES:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_UPDATE_RES";
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_CHANGED_NOTIFY";
		case CMD_ID_DEVICE_APPLIANCE_CMD_DEL_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_DEL_REQ";
		case CMD_ID_DEVICE_APPLIANCE_CMD_DEL_RES:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_DEL_RES";
		case CMD_ID_DEVICE_APPLIANCE_CMD_DELETED_NOTIFY:
			return "CMD_ID_DEVICE_APPLIANCE_CMD_DELETED_NOTIFY";
		case CMD_ID_DEVICE_APPLIANCE_CTRL_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_CTRL_REQ";
		case CMD_ID_DEVICE_APPLIANCE_CTRL_RES:
			return "CMD_ID_DEVICE_APPLIANCE_CTRL_RES";
		case CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ:
			return "CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ";
		case CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_RES:
			return "CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_RES";
		case CMD_ID_DEVICE_APPLIANCE_VALUE_CHANGED_NOTIFY:
			return "CMD_ID_DEVICE_APPLIANCE_VALUE_CHANGED_NOTIFY";
		case CMD_ID_OTA_UPGRADE_REQ:
			return "CMD_ID_OTA_UPGRADE_REQ";
		case CMD_ID_OTA_UPGRADE_RES:
			return "CMD_ID_OTA_UPGRADE_RES";
		case CMD_ID_OTA_UPGRADE_PROGRESS_UPDATE_REQ:
			return "CMD_ID_OTA_UPGRADE_PROGRESS_UPDATE_REQ";
		case CMD_ID_OTA_UPGRADE_PROGRESS_UPDATE_RES:
			return "CMD_ID_OTA_UPGRADE_PROGRESS_UPDATE_RES";
		case CMD_ID_OTA_UPGRADE_PROGRESS_CHANGED_NOTIFY:
			return "CMD_ID_OTA_UPGRADE_PROGRESS_CHANGED_NOTIFY";
		case CMD_ID_DEVICE_ALARM_LIST_REQ:
			return "CMD_ID_DEVICE_ALARM_LIST_REQ";
		case CMD_ID_DEVICE_ALARM_LIST_RES:
			return "CMD_ID_DEVICE_ALARM_LIST_RES";
		case CMD_ID_ROOM_INFO_CHANGED_NOTIFY:
			return "CMD_ID_ROOM_INFO_CHANGED_NOTIFY";
		case CMD_ID_ROOM_DELETED_NOTIFY:
			return "CMD_ID_ROOM_DELETED_NOTIFY";
		case CMD_ID_ROOM_ADDED_NOTIFY:
			return "CMD_ID_ROOM_ADDED_NOTIFY";
		default:
		{
			TypeChar *retString = new TypeChar();
			sprintf(retString->buff, "unknow command id = %d", commandid);
			mPrintf(Log_NetWork, "%s ", retString->buff);
			delete retString;
			return "unknow command";
		}
	}
}

const char *onGetZCLCMDID(uint8_t cmdid)
{
	switch(cmdid)
	{
		case 0x00:return "ZCL_CMD_READ";
		case 0x01:return "ZCL_CMD_READ_RSP";
		case 0x02:return "ZCL_CMD_WRITE";
		case 0x04:return "ZCL_CMD_WRITE_RSP";
		case 0x05:return "ZCL_CMD_WRITE_NO_RSP";
		case 0x0a:return "ZCL_CMD_REPORT";
		case 0x0b:return "ZCL_CMD_DEFAULT_RSP";
		default:
		{
			mPrintf(Log_Error, "Error:ZCL unknow command id = %02x ", cmdid);
			return "unknow command";
		}
			break;
	}
	return "unknow command";
}

const char *onGetSerialSubCMDString(uint8_t subsystem, uint8_t index)
{
	uint8_t tempSubSystem = (uint8_t)(subsystem & 0x1F);
	switch(subsystem)
	{
		case MT_RPC_SYS_SYS:
			switch(index)
			{
				case 0x02:return "MT_SYS_VERSION";
				case 0x08:return "MT_SYS_OSAL_NV_READ";
				case 0x09:return "MT_SYS_OSAL_NV_WRITE";
				case 0x80:return "MT_SYS_RESET_IND";
				default:break;
			}
			break;
		case MT_RPC_SYS_AF:
			switch(index)
			{
				case 0x00:return "MT_AF_REGISTER";
				case 0x01:return "MT_AF_DATA_REQUEST";
				case 0x80:return "MT_AF_DATA_CONFIRM";
				case 0x81:return "MT_AF_INCOMING_MSG";
				default:break;
			}
			break;
		case MT_RPC_SYS_ZDO:
			switch(index)
			{
				case 0x01:return "MT_ZDO_IEEE_ADDR_REQ";
				case 0x34:return "MT_ZDO_MGMT_LEAVE_REQ";
				case 0x45:return "MT_ZDO_EXT_ROUTE_DISC";
				case 0x50:return "MT_ZDO_EXT_NWK_INFO";
				case 0x81:return "MT_ZDO_IEEE_ADDR_RSP";
				case 0xB4:return "MT_ZDO_MGMT_LEAVE_RSP";
				case 0xB6:return "MT_ZDO_MGMT_PERMIT_JOIN_RSP";
				case 0xc0:return "MT_ZDO_STATE_CHANGE_IND";
				case 0xc1:return "MT_ZDO_END_DEVICE_ANNCE_IND";
				case 0xc4:return "MT_ZDO_SRC_RTG_IND";
				case 0xC9:return "MT_ZDO_LEAVE_IND";
				case 0xca:return "MT_ZDO_TC_DEVICE_IND";
				case 0xcb:return "MT_ZDO_PERMIT_JOIN_IND";
				default:break;
			}
			break;
		case MT_RPC_SYS_SAPI:
			switch(index)
			{
				case 0x00:return "MT_SAPI_START_REQ";
				case 0x08:return "MT_SAPI_PMT_JOIN_REQ";
				case 0x09:return "MT_SAPI_SYS_RESET";
				default:break;
			}
			break;
		case MT_RPC_SYS_UTIL:
			switch(index)
			{
				case 0x02:return "MT_UTIL_SET_PANID";
				case 0x03:return "MT_UTIL_SET_CHANNELS";
				case 0x05:return "MT_UTIL_SET_PRECFGKEY";
				default:break;
			}
			break;
		case MT_RPC_SYS_APP:
		{
			switch(index)
			{
				case MT_APP_CHECK_WHITE_LIST:return "MT_APP_CHECK_WHITE_LIST";
				case MT_APP_MSG:return "MT_APP_MSG";
				case MT_APP_UGET_DEVINFO:return "MT_APP_UGET_DEVINFO";
				case MT_USER_DELETE_SRC_ENTRY:return "MT_USER_DELETE_SRC_ENTRY";
				case MT_USER_DELETE_SUB_DEV:return "MT_USER_DELETE_SUB_DEV";
				case MT_USER_DELETE_NEIGHBOR:return "MT_USER_DELETE_NEIGHBOR";
				case MT_USER_DELETE_ENTRY:return "MT_USER_DELETE_ENTRY";
				case MT_USER_GET_SUB_IEEE:return "MT_USER_GET_SUB_IEEE";
				default:
					break;
			}
			break;
		}
		break;
		default:
			break;
	}
	static TypeChar *retChars = new TypeChar(32);
	retChars->onClear();
	sprintf(retChars->buff, "UNKNOW t=%02x c=%02x", tempSubSystem, index);
	return retChars->buff;
}

bool onCheckBattery(int32_t devtype)
{
	switch(devtype)
	{
		//case DEVICE_TYPE_GAS://燃气探测
		case DEVICE_TYPE_PIR://红外探测
		case DEVICE_TYPE_SMOKE://烟雾探测
		case DEVICE_TYPE_FLOOD://水浸检测
		case DEVICE_TYPE_SOS://一键报警
		case DEVICE_TYPE_DOOR_LOCK://智能门锁
		case DEVICE_TYPE_DOOR_WINDOW://门窗报警
			return true;
		default:
			return false;
	}
}

bool onCheckRS485BaudIsOK(int32_t value)
{
	bool retBool = TRUE;
	if(((value & 0xFF) < 1) || ((value & 0xFF) > 5))//忽略4800波特率
	{
		retBool = FALSE;
	}
	if(retBool && (((value & 0xFF00) < 0x500) || ((value & 0xFF) > 0x800)))
	{
		retBool = FALSE;
	}
	if(retBool && ((value & 0xFF0000) > 0x20000))
	{
		retBool = FALSE;
	}
	if(retBool && ((value & 0xFF000000) > 0x3000000))
	{
		retBool = FALSE;
	}
	return retBool;
}

int32_t onGetUtf8NameLen(char *tinname)
{
	uint8_t tempUChar = 0;
	int totalByte = 0;
	int utf8Len = 0;
	int retLen = 0;
	int totalDisplay = 0;
	for(int i = 0; i < (int) strlen(tinname); ++i)
	{
		tempUChar = (uint8_t)tinname[i];
		if(tempUChar < 0x80)
		{
			//一个字节
			totalByte = 1;
			utf8Len = 1;
		}
		else if(tempUChar <= 0xBF)
		{
			//多字节中的数据
			utf8Len++;
		}
		else if(tempUChar <= 0xDF)
		{
			totalByte = 2;
			//2字节开头
			utf8Len = 1;
		}
		else if(tempUChar <= 0xEF)
		{
			totalByte = 3;
			//3字节开头
			utf8Len = 1;
		}
		else if(tempUChar <= 0xF7)
		{
			totalByte = 4;
			//四字节开头
			utf8Len = 1;
		}
		else if(tempUChar <= 0xFB)
		{
			totalByte = 5;
			//五字节开头
			utf8Len = 1;
		}
		else if(tempUChar <= 0xFE)
		{
			totalByte = 6;
			//六字节开头
			utf8Len = 1;
		}
		if((utf8Len >= totalByte) && (utf8Len > 0))
		{
			switch(utf8Len)
			{
				case 1:
					totalDisplay += 1;
					break;
				case 2:
					totalDisplay += 2;
					break;
				case 3:
					totalDisplay += 2;
					break;
				default:
					totalDisplay += 2;
					break;
			}
			retLen += utf8Len;
			if(totalDisplay >= 8)
			{
				break;
			}
		}
	}
	return retLen;
}

//uft8 to uncoide-16
int32_t onConverUnicodeString(char *tinname, char *toutname, uint8_t maxoutlen)
{
	TypeChar *inName = new TypeChar(6);
	uint8_t tempUChar = 0;
	int totalByte = 0;
	int utf8Len = 0;
	int outLen = 0;
	int totalDisplay = 0;
	for(int i = 0; i < (int) strlen(tinname); ++i)
	{
		tempUChar = (uint8_t)tinname[i];
		if(tempUChar < 0x80)
		{
			//一个字节
			totalByte = 1;
			utf8Len = 0;
			inName->onClear();
			inName->buff[utf8Len++] = tempUChar;
		}
		else if(tempUChar <= 0xBF)
		{
			//多字节中的数据
			inName->buff[utf8Len++] = tempUChar;
		}
		else if(tempUChar <= 0xDF)
		{
			totalByte = 2;
			//2字节开头
			utf8Len = 0;
			inName->onClear();
			inName->buff[utf8Len++] = tempUChar;
		}
		else if(tempUChar <= 0xEF)
		{
			totalByte = 3;
			//3字节开头
			utf8Len = 0;
			inName->onClear();
			inName->buff[utf8Len++] = tempUChar;
		}
		else if(tempUChar <= 0xF7)
		{
			totalByte = 4;
			//四字节开头
			utf8Len = 0;
			inName->onClear();
			inName->buff[utf8Len++] = tempUChar;
		}
		else if(tempUChar <= 0xFB)
		{
			totalByte = 5;
			//五字节开头
			utf8Len = 0;
			inName->onClear();
			inName->buff[utf8Len++] = tempUChar;
		}
		else if(tempUChar <= 0xFE)
		{
			totalByte = 6;
			//六字节开头
			utf8Len = 0;
			inName->onClear();
			inName->buff[utf8Len++] = tempUChar;
		}
		if((utf8Len >= totalByte) && (utf8Len > 0))
		{
			switch(utf8Len)
			{
				case 1:
					toutname[outLen++] = 0;
					toutname[outLen++] = inName->buff[0];
					totalDisplay += 1;
					break;
				case 2:
					toutname[outLen++] = (char) ((inName->buff[0] >> 2) & 0x07);
					toutname[outLen++] = (char) ((inName->buff[0] << 6) + (inName->buff[1] & 0x3F));
					totalDisplay += 2;
					break;
				case 3:
					toutname[outLen++] = (char) ((inName->buff[0] << 4) + ((inName->buff[1] >> 2) & 0x0F));
					toutname[outLen++] = (char) ((inName->buff[1] << 6) + (inName->buff[2] & 0x3F));
					totalDisplay += 2;
					break;
				default:
					toutname[outLen++] = '*';
					toutname[outLen++] = '*';
					totalDisplay += 2;
					break;
			}
			if(maxoutlen > 0)
			{
				if(totalDisplay >= maxoutlen)
				{
					break;
				}
			}
			inName->onClear();
		}
	}
	delete inName;
	return outLen;
}

static const uint8_t aucCRCHi[] = {
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
		0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
		0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
		0x00, 0xC1, 0x81, 0x40
};

static const uint8_t aucCRCLo[] = {
		0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7,
		0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E,
		0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09, 0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9,
		0x1B, 0xDB, 0xDA, 0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC,
		0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
		0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32,
		0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4, 0x3C, 0xFC, 0xFD, 0x3D,
		0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38,
		0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF,
		0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
		0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60, 0x61, 0xA1,
		0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4,
		0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB,
		0x69, 0xA9, 0xA8, 0x68, 0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA,
		0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5,
		0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0,
		0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57, 0x97,
		0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E,
		0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89,
		0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
		0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42, 0x43, 0x83,
		0x41, 0x81, 0x80, 0x40
};

int32_t onGetCRC16( uint8_t * buff, int32_t len)
{
	uint8_t ucCRCHi = 0xFF;
	uint8_t ucCRCLo = 0xFF;
	int iIndex;
	while( len-- )
	{
		iIndex = ucCRCLo ^ *( buff++ );
		ucCRCLo = (uint8_t)( ucCRCHi ^ aucCRCHi[iIndex] );
		ucCRCHi = aucCRCLo[iIndex];
	}
	return (int32_t)( ucCRCHi | (ucCRCLo << 8));

}

int32_t onGetDimmingParaValue(int32_t minvalue, int32_t maxvalue, int32_t stepvalue)
{
	return (((stepvalue / 100) & 0xFFFF) << 16) + ((maxvalue & 0xFF) << 8) + (minvalue & 0xFF);
}

void onNotifyToJava(int32_t tnotifyid, int64_t tlcmd1, int64_t tlcmd2, int64_t tlvalue, int32_t intvalue)
{
	TypeChar * tempChars = new TypeChar(16);
	sprintf(tempChars->buff, "%d", intvalue);
	onNotifyToJava(tnotifyid, tlcmd1, tlcmd2, tlvalue, tempChars->buff);
	delete tempChars;
}

void onNotifyToJava(int32_t tnotifyid, int64_t tlcmd1, int64_t tlcmd2, int64_t tlvalue, const char *tstrvalue)
{
	if(mNotifyRegisterFlag & tnotifyid)
	{
		mJniNotifyLinkList->add(new TypeJniNotificationInfo(tnotifyid, tlcmd1, tlcmd2, tlvalue, tstrvalue));
	}
}

int32_t onGetGatewayModelInt(const char * tmodel)
{
	int retMoelInt = 0;
	TypeChar *tempModel = new TypeChar(tmodel);
	if(tempModel->onStringCMP("H201-ST-SH00"))
	{
		retMoelInt = 1;//代表android 带屏网关
	}
	else if(tempModel->onStringCMP("H202-UK-SH00"))
	{
		retMoelInt = 2;//代表linux mini 网关
	}
	else if(tempModel->onStringCMP("HY13-UK-SH00"))
	{
		retMoelInt = 3;//代表 声必可背景音乐网关
	}
	else if(tempModel->onStringCMP("H201-ST-SH10"))
	{
		retMoelInt = 4;//代表 4C网关/单模块网关
	}
	delete tempModel;
	return retMoelInt;
}

