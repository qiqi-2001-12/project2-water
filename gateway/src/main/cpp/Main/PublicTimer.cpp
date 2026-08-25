/*
 * PublicTimer.cpp
 *
 *  Created on: Jul 28, 2017
 *      Author: root
 */
#include "../Main/WinobleMain.h"
static TypeArrayList *pMgTimerHandle = NULL;
static void mTimerCheckValue(int value);
static struct timeval mTimerValue;
static int mCurrentMs;
struct tm *mTimerNow;
static TypeChar *mChangeValue = NULL;
void * mfTimerThead(void *arg)
{
	mChangeValue = new TypeChar(128);
	TypeThreadInfo *tempThreadInfo = (TypeThreadInfo *)arg;
	prctl(PR_SET_NAME,tempThreadInfo->title->buff);
	pMgTimerHandle = new TypeArrayList(ArrayTimer_Struct);
	int lastMs;
	int tempInt;
	mGetTimeMs();
	lastMs = (int)(mTimerValue.tv_usec % 1000000) / 1000 ;
	while (mIsExitFlag) //循环读取数据
	{
		mCurrentMs = mGetTimeMs();
		if(mCurrentMs > lastMs) tempInt = mCurrentMs - lastMs;
		else tempInt = 1000 - lastMs + mCurrentMs;
		if(tempInt >= 100)
		{
#if defined(WINOBLE_LINUX) && (defined(HWELLYI_MT7688) || defined(H202_UK_SHA0))
			static int8_t wanLEDFlag = 0;//网络指示灯标志
			if(onGetConnectFlag())
			{
				if(mTcpReciveFlag)
				{
					led_off(WAN_LED);
					wanLEDFlag = 1;
					mTcpReciveFlag = FALSE;
				}
				else
				{
					if(wanLEDFlag)
					{
						wanLEDFlag = 0;
						led_on(WAN_LED);
					}
				}

			}
			else
			{
				if(wanLEDFlag == 5)
				{
					led_on(WAN_LED);
				}
				else if(wanLEDFlag >= 6)
				{
					wanLEDFlag = 1;
					led_off(WAN_LED);
				}
				wanLEDFlag++;
			}
			//然后检查一下串口工作情况  只要有一个串口工作不正常就把灯灭掉
			if(DUALZIGBEECHIP)
			{
				if((pmMasterSerialPort && (pmMasterSerialPort->checkDrivceErrorCnt > 5)) ||
				(pmSlaveSerialPort && (pmSlaveSerialPort->checkDrivceErrorCnt > 5)))
				{
				    led_off(WAN_LED);
				}
			}
			else
			{
				if(pmMasterSerialPort && (pmMasterSerialPort->checkDrivceErrorCnt > 5))
				{
				    led_off(WAN_LED);
				}
			}

			static uint8_t buzzerAlarmFlag = 0;
			if(mIsAlarmingFlag > 0)
			{
				if(buzzerAlarmFlag == 5)
				{
					gpio_write(BUZZER_GPIO19, GPIO_VALUE_HIGH);
				}
				else if(buzzerAlarmFlag >= 7)
				{
					buzzerAlarmFlag = 0;
					gpio_write(BUZZER_GPIO19, GPIO_VALUE_LOW);
				}
				buzzerAlarmFlag++;
			}
			else
			{
				if(buzzerAlarmFlag)
				{
					buzzerAlarmFlag = 0;
					gpio_write(BUZZER_GPIO19, GPIO_VALUE_LOW);
				}
			}
#endif
			lastMs = mCurrentMs;
			//自定义定时器也在这里检查
			mTimerCheckValue(tempInt);
		}
		//这里更新一下时间
		mTimerNow = localtime(&mTimerValue.tv_sec);
		usleep(100000);//100ms check once
	}
	delete pMgTimerHandle;
	mThreadInfoList->removeObject(tempThreadInfo);
	return arg;
}

/*
 * 得到一个时:分:秒的时间
 */
/*
char * onGetCurrentTimeS()
{
	static TypeChar *pretString = new TypeChar(128);
	sprintf(pretString->buff, "%02d:%02d:%02d", mTimerNow->tm_hour, mTimerNow->tm_min, mTimerNow->tm_sec);
	return pretString->buff;
}*/

char * onChangeTimeS(time_t time)
{
	struct tm *tempTimer = localtime(&time);
	sprintf(mChangeValue->buff, "%02d:%02d:%02d", tempTimer->tm_hour, tempTimer->tm_min, tempTimer->tm_sec);
	return mChangeValue->buff;
}

int onGetTimeDate()
{
	return mTimerValue.tv_sec / 86400/*24 * 3600*/;
}

long onGetTimeSec()
{
	return mTimerValue.tv_sec;
}

int mGetTimeMs()
{
	gettimeofday(&mTimerValue, NULL);
	mCurrentMs = (int)((mTimerValue.tv_usec % 1000000) / 1000);
	return mCurrentMs;
}

//return second
int32_t onGetCurrentTime(uint32_t *minute, uint32_t *week)
{
	int32_t retInt = 0;
	retInt = (mTimerValue.tv_sec % 1000) * 1000 + (mTimerValue.tv_usec / 1000);
	if(minute || week)
	{
		if(minute)
		{
			*minute = 0;
			*minute = (uint32_t)(mTimerNow->tm_hour * 60 + mTimerNow->tm_min);
		}
		if(week)
		{
			*week = 0;
			*week = (uint32_t)mTimerNow->tm_wday;
		}
	}
	return retInt;
}

static void mTimerCheckValue(int value)
{
	static TypeTimerStruct *pTempTimer = NULL;
	static void (* handler)(int para1, int para2);
	static int para1;
	static int para2;
	static int repeatTime;
	static int alarmClock = 0;

	int i = 0;
	alarmClock += value;
	if(pMgTimerHandle)
	{
		for(i = 0; i < pMgTimerHandle->size();)
		{
			pTempTimer = (TypeTimerStruct *)pMgTimerHandle->get(i);
			//mPrintf("timerid = %d, value = %d", pTempTimer->timeID, pTempTimer->timeValue);
			if(pTempTimer->timeID > 0)
			{
				pTempTimer->timeValue -= value;
				if(pTempTimer->timeValue <= 0)
				{
					handler = NULL;
					repeatTime = 0;
					if(pTempTimer->mfHandler != NULL)
					{
						para1 = pTempTimer->para1;
						para2 = pTempTimer->para2;
						handler = pTempTimer->mfHandler;
					}
					repeatTime = pTempTimer->repeatTime;
					if(pTempTimer->repeatTime > 0)
					{
						pTempTimer->timeValue = pTempTimer->repeatTime;
					}
					else
					{
						onTimerDelete(pTempTimer->timeID);
					}
					if(handler != NULL)
					{
						handler(para1, para2);
					}
					if(repeatTime <= 0)
					{
						continue;
					}
				}
			}
			i++;
		}
	}
}

static TypeTimerStruct *mSearchTimerID(int timerid)
{
	if(pMgTimerHandle)
	{
		int i = 0;
		TypeTimerStruct * ptemptimer = NULL;
		for(i = 0; i < pMgTimerHandle->size();i++)
		{
			ptemptimer = (TypeTimerStruct *)pMgTimerHandle->get(i);
			if(ptemptimer->timeID == timerid)
			{
				return ptemptimer;
			}
		}
	}
	return NULL;
}

/*
int onTimerCheckValueWithID(int timerid)
{
	int retValue = 0;
	TypeTimerStruct * tempTimer = mSearchTimerID(timerid);
	if(tempTimer != NULL) retValue = tempTimer->timeValue;
	return retValue;
}*/

void onTimerAdd(int timerid, int timervalue, bool repeatflag, void (*mfHandler)(int, int), int pa1, int pa2)
{
	if(pMgTimerHandle)
	{
		bool newFlag = false;
		if(timerid == 0) {mPrintf(Log_Error, "Error:添加：错误的定时器ID！");return;}
		//首先查找  是否存在这样的ID
		TypeTimerStruct *pTempTimer = mSearchTimerID(timerid);
		if(pTempTimer == NULL) {newFlag = true;pTempTimer = new TypeTimerStruct(timerid);}
		pTempTimer->timeValue = timervalue;
		if(repeatflag) pTempTimer->repeatTime = timervalue;
		else pTempTimer->repeatTime = 0;
		pTempTimer->para1 = pa1;
		pTempTimer->para2 = pa2;
		pTempTimer->mfHandler = mfHandler;
		if(newFlag)
		{
			pMgTimerHandle->add(pTempTimer);
		}
	}
}

void onTimerDelete(int timerid)
{
	if(pMgTimerHandle)
	{
		if(timerid == 0) {mPrintf(Log_Error, "Error:删除：错误的定时器ID！");return;}
		TypeTimerStruct *pTempTimer = mSearchTimerID(timerid);
		if(pTempTimer != NULL) pMgTimerHandle->removeObject(pTempTimer);
	}
}

void onTimerUpdate(int timerid, int timevalue)
{
	if(pMgTimerHandle)
	{
		if(timerid == 0) {mPrintf(Log_Error, "Error:删除：错误的定时器ID！");return;}
		TypeTimerStruct *pTempTimer = mSearchTimerID(timerid);
		if(pTempTimer != NULL)
		{
			pTempTimer->timeValue = timevalue;
			if(pTempTimer->repeatTime > 0) pTempTimer->repeatTime = timevalue;
		}
	}
}
