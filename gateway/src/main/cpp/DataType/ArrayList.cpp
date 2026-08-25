/*
 * ArrayList.cpp
 *
 *  Created on: Jun 30, 2017
 *      Author: root
 */
#include "../Main/WinobleMain.h"
static void onRemoveObj(int arraytype, void *obj);
TypeArrayList::TypeArrayList(ArrayListType type)
{
	mallocSize = 10;
	ppData = (void **) malloc(mallocSize * sizeof(void *));
	memset(ppData, 0, mallocSize * sizeof(void *));
	strLength = 0;
	arrayType = type;
	mMemNewFreeCount++;
}

int TypeArrayList::size()
{
	return this->strLength;
}

void TypeArrayList::add(void *obj)
{
	//首先判断定义的空间是否足够
	if((this->strLength + 1) > this->mallocSize)
	{
		//如果超过了 就重新分配 一段内存
		mChangeMem(true);
	}
	this->ppData[this->strLength] = obj;
	this->strLength++;
	//
}

void *TypeArrayList::get(int index)
{
	if(this->strLength > index)//做安全判断
		return this->ppData[index];
	else
	{
		mPrintf(Log_Error, "Error:ArrayList get(%d) > strLength(%d)! ", index, this->strLength);
		return this->ppData[0];
	}
}

bool TypeArrayList::update(int index, void *obj)
{
	if((this->strLength > index) && (this->ppData[index] == NULL))
	{
		this->ppData[index] = obj;
		return true;
	}else
	{
		return false;
	}
}
/*
bool TypeArrayList::deleteIndex(int index)
{
	if(this->strLength > index)
	{
		if(this->ppData[index] != NULL)
		{
			//释放这个区域的内存
			mfRemoveValue(this->ppData[index]);
		}
		this->ppData[index] = NULL;
		return true;
	}else
	{
		return false;
	}
}*/

void TypeArrayList::mChangeMem(bool flag)
{
	//可以释放内存了
	if(flag)
	{
		//增加空间
		this->mallocSize += 10;
	}else
	{
		//减少空间
		if(this->mallocSize >= 20)
			this->mallocSize -= 10;
	}
	void **tempPPData = (void **) malloc(this->mallocSize * sizeof(void *));
	memset(tempPPData, 0, this->mallocSize * sizeof(void *));
	memcpy(tempPPData, this->ppData, this->strLength * sizeof(void *));
	void **savePPData = this->ppData;//添加临时保存  为避免  出错
	this->ppData = tempPPData;
	free(savePPData);
}

/**
 * 根据索引删除数据
 */

void TypeArrayList::remove(int index, bool flag)
{
	if(this->strLength > index)//做安全判断
	{
		void *saveData = this->ppData[index];
		if((this->strLength - index) > 1)//判断要不要移动位置
		{
			//全部移动一下
			while((index + 1) < this->strLength)
			{
				this->ppData[index] = this->ppData[index + 1];
				index++;
			}
			this->ppData[index] = this->ppData[this->strLength - 1];
		}
		if(this->strLength > 0)
			this->strLength--;
		if(flag)
		{
			mfRemoveValue(saveData);
		}
		if((this->mallocSize - this->strLength) > 15)
		{
			mChangeMem(false);
		}
	}
}

/**
 * 根据句柄删除数据
 */
void TypeArrayList::removeObject(void *obj)
{
	int i = 0;
	for(i = 0; i < this->strLength; i++)
	{
		if(this->ppData[i] == obj)
		{
			remove(i, true);
			break;
		}
	}
}

void TypeArrayList::UnFreeRemoveObject(void *obj)
{
	int i = 0;
	for(i = 0; i < this->strLength; i++)
	{
		if(this->ppData[i] == obj)
		{
			remove(i, false);
			break;
		}
	}
}


void TypeArrayList::UnFreeClear()
{
	while(this->strLength > 0)
	{
		remove(this->strLength - 1, false);
	}
}

/**
 * 删除所有数据
 */
void TypeArrayList::clear()
{
	//you hua
	while(this->strLength > 0)
	{
		remove(this->strLength - 1, true);
	}
}

static void onRemoveObj(int arraytype, void *obj)
{
	if(obj == NULL)
		return;
	switch(arraytype)
	{
		case ArrayTypeChar:
			delete (TypeChar *) obj;
			break;
		case ArrayTypeArrayList:
			delete (TypeArrayList *) obj;
			break;
		case ArrayTypeTcpCMD:
			delete (TypeTcpCMD *) obj;
			break;
		case ArrayTimer_Struct:
			delete (TypeTimerStruct *) obj;
			break;
		case ArrayTypeSeqNo:
			delete (TypeSeqNo *) obj;
			break;
		case ArrayTypeDeviceTypeInfo:
			delete (TypeDeviceTypeInfo *) obj;
			break;
		case ArrayTypeRoomInfo:
			delete (TypeRoomInfo *) obj;
			break;
		case ArrayTypeDataBase:
			delete (TypeDataBase *) obj;
			break;
		case ArrayTypeSceneNameInfo:
			delete (TypeSceneNameInfo *) obj;
			break;
		case ArrayTypeSceneActionInfo:
			delete (TypeSceneActionInfo *) obj;
			break;
		case ArrayTypeSceneCondInfo:
			delete (TypeSceneCondInfo *) obj;
			break;
		case ArrayTypeCarriedSceneList:
			delete (TypeCarriedSceneList *) obj;
			break;
		case ArrayTypeDevEventInfo:
			delete (TypeDevEventInfo *)obj;
			break;
		case ArrayTypeWaterLeakStatus:
			delete (TypeWaterLeakStatus *)obj;
			break;
		case ArrayTypeSerialProces:
			delete (TypeSerialProces *)obj;
			break;
		case ArrayTypeDBDeviceInfo:
			delete (TypeDBDeviceInfo *)obj;
			break;
		case ArrayTypeApplianceCodeInfo:
			delete (TypeApplianceCodeInfo *)obj;
			break;
		case ArrayTypeApplianceInfo:
			delete (TypeApplianceInfo *)obj;
			break;
		case ArrayTypeThreadInfo:
			delete (TypeThreadInfo *)obj;
			break;
		case ArrayTypeRobotDataInfo:
			delete (TypeRobotDataInfo *)obj;
			break;
		case ArrayTypeSmartDoorLockHLSInfo:
			delete (TypeSmartDoorLockHLS *)obj;
			break;
		case ArrayTypeRS485Profile:
			delete (RS485Profile *)obj;
			break;
		case ArrayTypeCentralAirConditioningZH:
			delete (TypeCentralAirConditioningZH *)obj;
			break;
		case ArrayTypeCentralAirConditioningMD:
			delete (TypeCentralAirConditioningMD *)obj;
			break;
		case ArrayTypeElectricCurtainDY:
			delete (TypeElectricCurtainDY *)obj;
			break;
		case ArrayTypeJniNotificationInfo:
			delete (TypeJniNotificationInfo *)obj;
			break;
		case ArrayTypeGatewayInfo:
			delete (TypeGatewayInfo *)obj;
			break;
		default:
			mPrintf(Log_Error, "Error:unknow arrayType =%d ", arraytype);
			break;
	}
}

/**
 * 根据不同结构体做不同的内存释放
 */
void TypeArrayList::mfRemoveValue(void *obj)
{
	onRemoveObj(arrayType, obj);
}

TypeArrayList::~TypeArrayList()
{
	if(this->strLength > 0)
	{
		clear();
	}
	free(this->ppData);
	mMemNewFreeCount--;
}

TypeLinkedList::TypeLinkedList(ArrayListType type)
{
	linkedType = type;
	pHead = NULL;
	pTail = NULL;
	count = 0;
	pthread_mutex_init(&lockMutex, NULL);
	mMemNewFreeCount++;
}

uint32_t TypeLinkedList::onGetCount()
{
	return count;
}

//读取出来后必须销毁，否则会内存泄漏
void *TypeLinkedList::get()
{
	//先进先出规则 从pHead 开始取数据
	pthread_mutex_lock(&lockMutex);
	void *retData = NULL;
	if(pHead != NULL)
	{
		//如果头不为空，那至少有一个有效数据
		retData = pHead->data;
		TypeLinkedInfo *tempLinkedInfo = pHead;
		pHead = pHead->next;
		delete tempLinkedInfo;
	}
	else
	{
		mPrintf(Log_Error, "Error:TypeLinkedList::get() pHead== NULL! count=%d ", count);
	}
	if(count > 0) count--;
	pthread_mutex_unlock(&lockMutex);
	return retData;
}

bool TypeLinkedList::onDeleteSame(int ttype, void *tdata)
{
	pthread_mutex_lock(&lockMutex);
	bool retBool = FALSE;
	if(ttype == ArrayTypeSerialProces)
	{
		//查找一下列表是否有相同的命令
		TypeSerialProces *tempSerialProces = NULL;
		TypeLinkedInfo * tempLinkedInfo = pHead;
		while(tempLinkedInfo)
		{
			tempSerialProces = (TypeSerialProces *)tempLinkedInfo->data;
			if(tempSerialProces && tempSerialProces->onIsSameCMD((TypeSerialProces *)tdata))
			{
				tempSerialProces->retStatus = SEND_DELETE;
				tempSerialProces->delayTime = 0;
				mPrintf(tempSerialProces->isMasterFlag, "Error:same com cmd, SEND_DELETE!");
			}
			tempLinkedInfo = tempLinkedInfo->next;
		}
	}
	pthread_mutex_unlock(&lockMutex);
	return retBool;
}

void TypeLinkedList::add(void *obj)
{
	pthread_mutex_lock(&lockMutex);
	TypeLinkedInfo *tempLinkedInfo = new TypeLinkedInfo(obj);
	if(pHead == NULL)//添加到头部
	{
		//这里是第一次添加
		pHead = tempLinkedInfo;
	}
	else if(pHead->next == NULL)
	{
		//这里应该是第二次添加
		pTail = tempLinkedInfo;
		pHead->next = pTail;
	}
	else
	{
		//第三次添加
		pTail->next = tempLinkedInfo;
		pTail = pTail->next;
	}
	count ++;
	pthread_mutex_unlock(&lockMutex);
}

void TypeLinkedList::clear()
{
	while(count > 0)
	{
		onRemoveObj(linkedType, get());
	}
}

TypeLinkedList::~TypeLinkedList()
{
	mMemNewFreeCount--;
	clear();
	pthread_mutex_destroy(&lockMutex);
}
