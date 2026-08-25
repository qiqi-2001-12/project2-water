/*
 * DeviceTypeInfo.cpp
 *
 *  Created on: Jul 16, 2017
 *      Author: root
 */
#include "../Main/WinobleMain.h"
#include "TypeDefine.h"

TypeDeviceTypeInfo::TypeDeviceTypeInfo(int32_t xkey_id, int32_t xsub_id, int32_t xroom_id, int32_t xiconid, int32_t xsaveiconid, const char *xname, const char *xsavename, SubDeviceType subtype, int32_t inittype, void *obj)
{
	mMemNewFreeCount++;
	statusUpdateFlag = 0;
	deviceID = xkey_id;
	subID = xsub_id;
	roomID = xroom_id;
	saveIconID = xsaveiconid;
	iconID = xiconid;
	name = new TypeChar(xname);
	saveName = new TypeChar(xsavename);
	pShortAddr = NULL;
	devType = subtype;
	switch(devType)
	{
		case SUB_DEVICE_TYPE_LIGHT:
			if(inittype)
			{
				subInfo.lightStatus = new TypeLightStatus(atoi(((char **)obj)[0]), mfPublicGetInt64(((char **)obj)[1]));
			}
			else
			{
				subInfo.lightStatus = (TypeLightStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_DIMMER:
			if(inittype)
			{
				subInfo.dimmingStatus = new TypeDimmingStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.dimmingStatus = (TypeDimmingStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_CURTAIN:
			if(inittype)
			{
				subInfo.curtainStatus = new TypeCurtainStatus(atoi(((char **)obj)[0]));
			}
			else
			{
				subInfo.curtainStatus = (TypeCurtainStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_SWITCH:
			if(inittype)
			{
				subInfo.switchStatus = new TypeSwitchStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]), atoi(((char **)obj)[3]));
			}
			else
			{
				subInfo.switchStatus = (TypeSwitchStatus *)obj;
			}
			break;
		case SUB_DEVICE_TYPE_GAS:
			if(inittype)
			{
				subInfo.gasStatus = new TypeGasStatus(atoi(((char **)obj)[0]));
			}
			else
			{
				subInfo.gasStatus = (TypeGasStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_IR_REMOTE:
			if(inittype)
			{
				subInfo.irRemoteStatus = new TypeIRRemoteStatus(atoi(((char **)obj)[0]));
			}
			else
			{
				subInfo.irRemoteStatus = (TypeIRRemoteStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_PIR:
			if(inittype)
			{
				subInfo.pirStatus = new TypePIRStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]), atoi(((char **)obj)[3]), atoi(((char **)obj)[4]));
			}
			else
			{
				subInfo.pirStatus = (TypePIRStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_SMOKE:
			if(inittype)
			{
				subInfo.smokeStatus = new TypeSmokeStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.smokeStatus = (TypeSmokeStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_FLOOD:
			if(inittype)
			{
				subInfo.floodStatus = new TypeFloodStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.floodStatus = (TypeFloodStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_SOS:
			if(inittype)
			{
				subInfo.sosStatus = new TypeSOSStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.sosStatus = (TypeSOSStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_DOOR_LOCK:
			if(inittype)
			{
				subInfo.doorLockStatus = new TypeDoorLockStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.doorLockStatus = (TypeDoorLockStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_DOOR_WINDOW:
			if(inittype)
			{
				subInfo.doorWindowStatus = new TypeDoorWindowStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]), atoi(((char **)obj)[3]));
			}
			else
			{
				subInfo.doorWindowStatus = (TypeDoorWindowStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_ENV_DETECTOR:
			if(inittype)
			{
				subInfo.env_detectorStatus = new TypeENV_DetectorStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]), atoi(((char **)obj)[3]), atoi(((char **)obj)[4]),atoi(((char **)obj)[5]));
			}
			else
			{
				subInfo.env_detectorStatus = (TypeENV_DetectorStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:
			if(inittype)
			{
				subInfo.waterLeakStatus = new TypeWaterLeakStatus(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.waterLeakStatus = (TypeWaterLeakStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_GAS_ARM:
			if(inittype)
			{
				subInfo.gasArmStatus = new TypeGasArmStatus(atoi(((char **)obj)[0]));
			}
			else
			{
				subInfo.gasArmStatus = (TypeGasArmStatus *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_CLOTHES_HANGER:
			if(inittype)
			{
				subInfo.clothesHangerStatus = new TypeClothesHanger(atoi(((char **)obj)[0]));
			}
			else
			{
				subInfo.clothesHangerStatus = (TypeClothesHanger *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_RS485_TRANSFER:
			if(inittype)
			{
				subInfo.rs485Status = new TypeRS485Status(atoi(((char **)obj)[0]), atoi(((char **)obj)[1]));
			}
			else
			{
				subInfo.rs485Status = (TypeRS485Status *) obj;
			}
			break;
		case SUB_DEVICE_TYPE_OFFLINE_VOICE:
			if(inittype)
			{
				subInfo.offLineVoiceStatus = new TypeOffLineVoiceStatus(0, atoi(((char **)obj)[0]), atoi(((char **)obj)[1]), atoi(((char **)obj)[2]));
			}
			else
			{
				subInfo.offLineVoiceStatus = (TypeOffLineVoiceStatus *) obj;
			}
			break;
		default:
			mPrintf(Log_Error, "Error:未定义的设备类型 ");
			break;
	}
}

char *TypeDeviceTypeInfo::onGetStatusSql(TypeChar *sql)
{
	switch(devType)
	{
		case SUB_DEVICE_TYPE_LIGHT:
			sprintf(sql->buff, "%d, %lld, 0, 0, 0, 0, 0, 0", subInfo.lightStatus->status,
			        subInfo.lightStatus->sceneID);
			break;
		case SUB_DEVICE_TYPE_DIMMER:
			sprintf(sql->buff, "%d, %d, %d, 0, 0, 0, 0, 0", subInfo.dimmingStatus->status, subInfo.dimmingStatus->paraValue, subInfo.dimmingStatus->saveParaValue);
			break;
		case SUB_DEVICE_TYPE_CURTAIN:
			sprintf(sql->buff, "%d, 0, 0, 0, 0, 0, 0, 0", subInfo.curtainStatus->status);
			break;
		case SUB_DEVICE_TYPE_SWITCH:
			sprintf(sql->buff, "%d, %d, %d, %d, 0, 0, 0, 0", subInfo.switchStatus->status,
			        subInfo.switchStatus->currentPower, subInfo.switchStatus->tadayEnergy,
			        subInfo.switchStatus->lastEnergyDate);
			break;
		case SUB_DEVICE_TYPE_GAS:
			sprintf(sql->buff, "%d, 0, 0, 0, 0, 0, 0, 0", subInfo.gasStatus->gasValue);
			break;
		case SUB_DEVICE_TYPE_IR_REMOTE:
			sprintf(sql->buff, "%d, 0, 0, 0, 0, 0, 0, 0", subInfo.irRemoteStatus->status);
			break;
		case SUB_DEVICE_TYPE_PIR:
			sprintf(sql->buff, "%d, %d, %d, %d, %d, 0, 0, 0", subInfo.pirStatus->status,
			        subInfo.pirStatus->power, subInfo.pirStatus->zoneID,
			        subInfo.pirStatus->securityStatus, subInfo.pirStatus->outDelayTime);
			break;
		case SUB_DEVICE_TYPE_SMOKE:
			sprintf(sql->buff, "%d, %d, %d, 0, 0, 0, 0, 0", subInfo.smokeStatus->status,
			        subInfo.smokeStatus->power, subInfo.smokeStatus->zoneID);
			break;
		case SUB_DEVICE_TYPE_FLOOD:
			sprintf(sql->buff, "%d, %d, %d, 0, 0, 0, 0, 0", subInfo.floodStatus->status,
			        subInfo.floodStatus->power, subInfo.floodStatus->zoneID);
			break;
		case SUB_DEVICE_TYPE_SOS:
			sprintf(sql->buff, "%d, %d, %d, 0, 0, 0, 0, 0", subInfo.sosStatus->status,
			        subInfo.sosStatus->power, subInfo.sosStatus->zoneID);
			break;
		case SUB_DEVICE_TYPE_DOOR_LOCK:
			sprintf(sql->buff, "%d, %d, %d, 0, 0, 0, 0, 0", subInfo.doorLockStatus->status,
			        subInfo.doorLockStatus->power, subInfo.doorLockStatus->zoneID);
			break;
		case SUB_DEVICE_TYPE_DOOR_WINDOW:
			sprintf(sql->buff, "%d, %d, %d, %d, 0, 0, 0, 0", subInfo.doorWindowStatus->status,
			        subInfo.doorWindowStatus->power, subInfo.doorWindowStatus->zoneID, subInfo.doorWindowStatus->securityStatus);
			break;
		case SUB_DEVICE_TYPE_ENV_DETECTOR:
			sprintf(sql->buff, "%d, %d, %d, %d, %d, 0, 0, 0",
			        subInfo.env_detectorStatus->tempSensorValue,
			        subInfo.env_detectorStatus->humiSensorValue,
			        subInfo.env_detectorStatus->illumSensorValue,
			        subInfo.env_detectorStatus->pm25Value, subInfo.env_detectorStatus->airLevel);
			break;
		case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:
			sprintf(sql->buff, "%d, %d, %d, %d, 0, 0, 0, 0", subInfo.waterLeakStatus->value_status,
			        subInfo.waterLeakStatus->flux, subInfo.waterLeakStatus->threshold,
			        subInfo.waterLeakStatus->alarm_status);
			break;
		case SUB_DEVICE_TYPE_GAS_ARM:
			sprintf(sql->buff, "%d, 0, 0, 0, 0, 0, 0, 0", subInfo.gasArmStatus->value_status);
			break;
		case SUB_DEVICE_TYPE_CLOTHES_HANGER:
			sprintf(sql->buff, "%d, 0, 0, 0, 0, 0, 0, 0", subInfo.clothesHangerStatus->status);
			break;
		case SUB_DEVICE_TYPE_RS485_TRANSFER:
			sprintf(sql->buff, "%d, 0, 0, 0, 0, 0, 0, 0", subInfo.rs485Status->status);
			break;
		case SUB_DEVICE_TYPE_OFFLINE_VOICE:
			sprintf(sql->buff, "%d, %d, 0, 0, 0, 0, 0", subInfo.offLineVoiceStatus->wakeup_id, subInfo.offLineVoiceStatus->duration);
			break;
		default:
			sprintf(sql->buff, "0, 0, 0, 0, 0, 0, 0, 0");
			break;
	}
	return sql->buff;
}

//需要判断这个设备是不是当前网关的设备
bool TypeDeviceTypeInfo::onUpdateTypeInfo(bool currentgateway, bool todeviceflag, TypeDeviceTypeInfo *devicetypeinfo)
{
	if(devicetypeinfo != NULL)
	{
		DeviceValue *tempDeviceValue = NULL;
		UpdateDeviceValueRequest updateRequest;
		if((devType == SUB_DEVICE_TYPE_LIGHT) && devicetypeinfo->subInfo.lightStatus->sceneID)//它是一个添加了场景同步的灯光
		{
			if(subInfo.lightStatus->status != devicetypeinfo->subInfo.lightStatus->status)
			{
				if(currentgateway)
				{
					//更新服务器状态
					tempDeviceValue = updateRequest.add_values();
					tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
					tempDeviceValue->set_value(subInfo.lightStatus->status);
				}
				else
				{
					subInfo.lightStatus->status = devicetypeinfo->subInfo.lightStatus->status;
				}
			}
			if(subInfo.lightStatus->sceneID != devicetypeinfo->subInfo.lightStatus->sceneID)
			{
				subInfo.lightStatus->sceneID = devicetypeinfo->subInfo.lightStatus->sceneID;
				//直接保存
				pDataBase->onUpdateSubDeviceInfo(this, SubLightSceneID, subInfo.lightStatus->sceneID);
			}
			//找到这个场景
			TypeSceneNameInfo *tempSceneNameInfo = pDeviceList->onFindSceneInfo(devicetypeinfo->subInfo.lightStatus->sceneID);
			if(tempSceneNameInfo)
			{
				onSetName(currentgateway, todeviceflag, tempSceneNameInfo->name->buff);
				onSetIconID(currentgateway, todeviceflag, tempSceneNameInfo->icon_id);
				onSetRoomID(tempSceneNameInfo->room_id);
			}
		}
		else
		{
			onSetName(currentgateway, todeviceflag, devicetypeinfo->name->buff);//更新名称
			onSetIconID(currentgateway, todeviceflag, devicetypeinfo->iconID);//更新图标
			onSetRoomID(devicetypeinfo->roomID);//更新房间号
			switch(devType)
			{
				case SUB_DEVICE_TYPE_LIGHT:
				{
					if(subInfo.lightStatus->status != devicetypeinfo->subInfo.lightStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
							tempDeviceValue->set_value(subInfo.lightStatus->status);
						}
						else
						{
							subInfo.lightStatus->status = devicetypeinfo->subInfo.lightStatus->status;
						}
					}
					if(subInfo.lightStatus->sceneID != devicetypeinfo->subInfo.lightStatus->sceneID)
					{
						subInfo.lightStatus->sceneID = devicetypeinfo->subInfo.lightStatus->sceneID;
						//直接保存
						pDataBase->onUpdateSubDeviceInfo(this, SubLightSceneID, subInfo.lightStatus->sceneID);
					}
				}
					break;
				case SUB_DEVICE_TYPE_DIMMER:
					if(subInfo.dimmingStatus->status != devicetypeinfo->subInfo.dimmingStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_LEVEL);
							tempDeviceValue->set_value(subInfo.dimmingStatus->status);
						}
						else
						{
							subInfo.dimmingStatus->status = devicetypeinfo->subInfo.dimmingStatus->status;
						}
					}
					//检查调光配置
					onSetDimmingParaValue(currentgateway, devicetypeinfo->subInfo.dimmingStatus->paraValue);
					break;
				case SUB_DEVICE_TYPE_CURTAIN:
					if(subInfo.curtainStatus->status != devicetypeinfo->subInfo.curtainStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_LEVEL);
							tempDeviceValue->set_value(subInfo.curtainStatus->status);
						}
						else
						{
							subInfo.curtainStatus->status = devicetypeinfo->subInfo.curtainStatus->status;
						}
					}
					break;
				case SUB_DEVICE_TYPE_SWITCH:
				{
					if(subInfo.switchStatus->status != devicetypeinfo->subInfo.switchStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器开关状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
							tempDeviceValue->set_value(subInfo.switchStatus->status);
						} else
						{
							subInfo.switchStatus->status = devicetypeinfo->subInfo.switchStatus->status;
						}
					}
					if(subInfo.switchStatus->currentPower != devicetypeinfo->subInfo.switchStatus->currentPower)
					{
						if(currentgateway)
						{
							//更新服务器当前功率值
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_POWER);
							tempDeviceValue->set_value(subInfo.switchStatus->currentPower);
						}
						else
						{
							subInfo.switchStatus->currentPower = devicetypeinfo->subInfo.switchStatus->currentPower;
							pDataBase->onUpdateSubDeviceInfo(this, SubPower, subInfo.switchStatus->currentPower);
						}
					}
				}
					break;
				case SUB_DEVICE_TYPE_GAS:
					if(subInfo.gasStatus->gasValue != devicetypeinfo->subInfo.gasStatus->gasValue)
					{
						if(currentgateway)
						{
							//更新服务器燃气值
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_GAS_DENSITY);
							tempDeviceValue->set_value(subInfo.gasStatus->gasValue);
						} else
						{
							subInfo.gasStatus->gasValue = devicetypeinfo->subInfo.gasStatus->gasValue;
						}
					}
					break;
				case SUB_DEVICE_TYPE_IR_REMOTE://红外伴侣
					if(subInfo.irRemoteStatus->status != devicetypeinfo->subInfo.irRemoteStatus->status)
					{
						//更新服务器开关状态
					}
					break;
				case SUB_DEVICE_TYPE_PIR:
				{
					if(subInfo.pirStatus->securityStatus != devicetypeinfo->subInfo.pirStatus->securityStatus)
					{
						subInfo.pirStatus->securityStatus = devicetypeinfo->subInfo.pirStatus->securityStatus;
						if(currentgateway)
						{
							subInfo.pirStatus->status = 0;//清除当前防区即将的响应
							mfPIRAlarmCB(deviceID, subID);
						}
						pDataBase->onUpdateSubDeviceInfo(this, SubSecurity, subInfo.pirStatus->securityStatus);
						//直接把通知发送到应用层
						if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_DEVSTAUS)
						{
							onNotifyToJava(JNI_NOTIFY_UPDATE_DEVSTAUS, deviceID, subID, devType, subInfo.pirStatus->securityStatus);
						}
					}
					if(subInfo.pirStatus->status != devicetypeinfo->subInfo.pirStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器红外状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_PIR_STATUS);
							tempDeviceValue->set_value(subInfo.pirStatus->status);
						} else
						{
							subInfo.pirStatus->status = devicetypeinfo->subInfo.pirStatus->status;
						}
					}
					if(subInfo.pirStatus->power != devicetypeinfo->subInfo.pirStatus->status)
					{
						if(currentgateway)
						{
							//更新到服务器
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
							tempDeviceValue->set_value(subInfo.pirStatus->power);

						} else
						{
							//保存
							subInfo.pirStatus->power = devicetypeinfo->subInfo.pirStatus->status;
							pDataBase->onUpdateSubDeviceInfo(this, SubPower, subInfo.pirStatus->power);
						}
					}

					if(subInfo.pirStatus->outDelayTime != devicetypeinfo->subInfo.pirStatus->outDelayTime)
					{
						subInfo.pirStatus->outDelayTime = devicetypeinfo->subInfo.pirStatus->outDelayTime;
						pDataBase->onUpdateSubDeviceInfo(this, SubPIRDelayTime, subInfo.pirStatus->outDelayTime);
					}
				}
					break;
				case SUB_DEVICE_TYPE_SMOKE:
				{
					if(subInfo.smokeStatus->status != devicetypeinfo->subInfo.smokeStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器烟雾状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_SMOKE_STATUS);
							tempDeviceValue->set_value(subInfo.smokeStatus->status);
						} else
						{
							subInfo.smokeStatus->status = devicetypeinfo->subInfo.smokeStatus->status;
						}
					}
					if(subInfo.smokeStatus->power != devicetypeinfo->subInfo.smokeStatus->status)
					{
						if(currentgateway)
						{
							//更新到服务器
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
							tempDeviceValue->set_value(subInfo.smokeStatus->power);

						} else
						{
							//保存
							subInfo.smokeStatus->power = devicetypeinfo->subInfo.smokeStatus->status;
							pDataBase->onUpdateSubDeviceInfo(this, SubPower, subInfo.smokeStatus->power);
						}
					}
				}
					break;
				case SUB_DEVICE_TYPE_FLOOD:
				{
					if(subInfo.floodStatus->status != devicetypeinfo->subInfo.floodStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器一键报警状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_FLOOD_STATUS);
							tempDeviceValue->set_value(subInfo.floodStatus->status);
						} else
						{
							subInfo.floodStatus->status = devicetypeinfo->subInfo.floodStatus->status;
						}
					}
					if(subInfo.floodStatus->power != devicetypeinfo->subInfo.floodStatus->status)
					{
						if(currentgateway)
						{
							//更新到服务器
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
							tempDeviceValue->set_value(subInfo.floodStatus->power);

						} else
						{
							//保存
							subInfo.floodStatus->power = devicetypeinfo->subInfo.floodStatus->status;
							pDataBase->onUpdateSubDeviceInfo(this, SubPower, subInfo.floodStatus->power);
						}
					}
				}
					break;
				case SUB_DEVICE_TYPE_SOS:
				case SUB_DEVICE_TYPE_DOOR_LOCK:
				{
					//一键报警没有保存的状态
				}
					break;
				case SUB_DEVICE_TYPE_DOOR_WINDOW:
				{
					if(subInfo.doorWindowStatus->status != devicetypeinfo->subInfo.doorWindowStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器门磁状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_DOOR_WINDOW_STATUS);
							tempDeviceValue->set_value(subInfo.doorWindowStatus->status);
						} else
						{
							subInfo.doorWindowStatus->status = devicetypeinfo->subInfo.doorWindowStatus->status;
						}
					}
					if(subInfo.doorWindowStatus->power != devicetypeinfo->subInfo.doorWindowStatus->status)
					{
						if(currentgateway)
						{
							//更新到服务器
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_BATTERY_LEVEL);
							tempDeviceValue->set_value(subInfo.doorWindowStatus->power);

						} else
						{
							//保存
							subInfo.doorWindowStatus->power = devicetypeinfo->subInfo.doorWindowStatus->status;
							pDataBase->onUpdateSubDeviceInfo(this, SubPower, subInfo.doorWindowStatus->power);
						}
					}
					if(subInfo.doorWindowStatus->securityStatus != devicetypeinfo->subInfo.doorWindowStatus->securityStatus)
					{
						subInfo.doorWindowStatus->securityStatus = devicetypeinfo->subInfo.doorWindowStatus->securityStatus;
						pDataBase->onUpdateSubDeviceInfo(this, SubSecurity, subInfo.doorWindowStatus->securityStatus);
						//直接把通知发送到应用层
						if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_DEVSTAUS)
						{
							onNotifyToJava(JNI_NOTIFY_UPDATE_DEVSTAUS, deviceID, subID, devType, subInfo.pirStatus->securityStatus);
						}
					}
				}
					break;
				case SUB_DEVICE_TYPE_ENV_DETECTOR:
					//环境探测暂时没有哦
				{
					if(subInfo.env_detectorStatus->tempSensorValue != devicetypeinfo->subInfo.env_detectorStatus->tempSensorValue)
					{
						if(currentgateway)
						{
							//更新服务器温度值
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_TEMPERATURE);
							tempDeviceValue->set_value(subInfo.env_detectorStatus->tempSensorValue);
						} else
						{
							subInfo.env_detectorStatus->tempSensorValue = devicetypeinfo->subInfo.env_detectorStatus->tempSensorValue;
						}
					}
					if(subInfo.env_detectorStatus->humiSensorValue != devicetypeinfo->subInfo.env_detectorStatus->humiSensorValue)
					{
						if(currentgateway)
						{
							//更新服务器湿度值
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_HUMIDITY);
							tempDeviceValue->set_value(subInfo.env_detectorStatus->humiSensorValue);
						} else
						{
							subInfo.env_detectorStatus->humiSensorValue = devicetypeinfo->subInfo.env_detectorStatus->humiSensorValue;
						}
					}
					if(subInfo.env_detectorStatus->illumSensorValue != devicetypeinfo->subInfo.env_detectorStatus->illumSensorValue)
					{
						if(currentgateway)
						{
							//更新服务器光照值
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_ILLUM_INTENSITY);
							tempDeviceValue->set_value(subInfo.env_detectorStatus->illumSensorValue);
						} else
						{
							subInfo.env_detectorStatus->illumSensorValue = devicetypeinfo->subInfo.env_detectorStatus->illumSensorValue;
						}
					}
					if(subInfo.env_detectorStatus->pm25Value != devicetypeinfo->subInfo.env_detectorStatus->pm25Value)
					{
						if(currentgateway)
						{
							//更新服务器PM2.5
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_PM25);
							tempDeviceValue->set_value(subInfo.env_detectorStatus->pm25Value);
						} else
						{
							subInfo.env_detectorStatus->pm25Value = devicetypeinfo->subInfo.env_detectorStatus->pm25Value;
						}
					}
					if(subInfo.env_detectorStatus->airLevel != devicetypeinfo->subInfo.env_detectorStatus->airLevel)
					{
						if(currentgateway)
						{
							//更新服务器空气质量等级
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_QUALITY);
							tempDeviceValue->set_value(subInfo.env_detectorStatus->airLevel);
						} else
						{
							subInfo.env_detectorStatus->airLevel = devicetypeinfo->subInfo.env_detectorStatus->airLevel;
						}
					}
					if(subInfo.env_detectorStatus->CO2Value != devicetypeinfo->subInfo.env_detectorStatus->CO2Value)
					{
						if(currentgateway)
						{
							//更新服务器空气质量等级
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ENV_CO2);
							tempDeviceValue->set_value(subInfo.env_detectorStatus->CO2Value);
						} else
						{
							subInfo.env_detectorStatus->CO2Value = devicetypeinfo->subInfo.env_detectorStatus->CO2Value;
						}
					}
				}
					break;
				case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:
					//这个真没有 不做任何事
					break;
				case SUB_DEVICE_TYPE_GAS_ARM:
					if(subInfo.gasArmStatus->value_status != devicetypeinfo->subInfo.gasArmStatus->value_status)
					{
						if(currentgateway)
						{
							//更新服务器燃气壁状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
							tempDeviceValue->set_value(subInfo.gasArmStatus->value_status);
						} else
						{
							subInfo.gasArmStatus->value_status = devicetypeinfo->subInfo.gasArmStatus->value_status;
						}
					}
					break;
				case SUB_DEVICE_TYPE_CLOTHES_HANGER:
					if(subInfo.clothesHangerStatus->status != devicetypeinfo->subInfo.clothesHangerStatus->status)
					{
						if(currentgateway)
						{
							//更新服务器晾衣架状态
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ONOFF);
							tempDeviceValue->set_value(subInfo.clothesHangerStatus->onGetLight());
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_UPDOWN);
							tempDeviceValue->set_value(subInfo.clothesHangerStatus->onGetUpDown());
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_ANION);
							tempDeviceValue->set_value(subInfo.clothesHangerStatus->onGetAnion());
							tempDeviceValue = updateRequest.add_values();
							tempDeviceValue->set_flag(DEVICE_VALUE_FLAG_DISINFECTION);
							tempDeviceValue->set_value(subInfo.clothesHangerStatus->onGetDisinfection());
						} else
						{
							subInfo.clothesHangerStatus->status = devicetypeinfo->subInfo.clothesHangerStatus->status;
						}
					}
					break;
				case SUB_DEVICE_TYPE_RS485_TRANSFER:
				{
					if(devicetypeinfo->subInfo.rs485Status->status == 0)
					{
						devicetypeinfo->subInfo.rs485Status->status = 0x1000801;//9600-8-1-无
					}
					//判断并修改一下波特率
					if(subInfo.rs485Status->status != devicetypeinfo->subInfo.rs485Status->status)
					{
						subInfo.rs485Status->status = devicetypeinfo->subInfo.rs485Status->status;
						//这里还要检查一下当前数据是否是正确的
						if(currentgateway)
						{
							if((subInfo.rs485Status->status != subInfo.rs485Status->saveStatus) && (onCheckRS485BaudIsOK(subInfo.rs485Status->status)) && onGetShortAddr())
							{
								//判断一下在不在线
								TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, deviceID);
								if(tempDBDeviceInfo)
								{
									//设置一下设备的波特率
									TypeChar *tempRS485Para = new TypeChar(16);
									tempRS485Para->onAddInt64Ex(0, subInfo.rs485Status->status);//9600 even cs8 1
									pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000D, ZCL_DATATYPE_UINT64, tempRS485Para->ubuff, (uint8_t)8), 0);
									delete tempRS485Para;
									//延时读取一下
									pmMasterSerialPort->onReadAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, 0x0D, 200);
								}
							}
						}
						else
						{
							subInfo.rs485Status->saveStatus = devicetypeinfo->subInfo.rs485Status->saveStatus;
							//同时也写到设备里面
							pDataBase->onUpdateSubDeviceInfo(this, SubRS485Para, subInfo.rs485Status->saveStatus);
						}
					}
				}
					break;
				case SUB_DEVICE_TYPE_OFFLINE_VOICE:
					if(subInfo.offLineVoiceStatus->status != devicetypeinfo->subInfo.offLineVoiceStatus->status)
					{
						subInfo.offLineVoiceStatus->status = devicetypeinfo->subInfo.offLineVoiceStatus->status;
					}
					if(subInfo.offLineVoiceStatus->wakeup_id != devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id)
					{
						if(currentgateway)
						{
							//发送给设备
							uint8_t sendChars[3];
							sendChars[0] = 2;
							sendChars[1] = 0x14;
							if(devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id < 0) devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id = 0;
							else if(devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id > 3) devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id = 3;
							sendChars[2] = (uint8_t)devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id;
							pmMasterSerialPort->onWriteAttribute((uint32_t)onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0013, ZCL_DATATYPE_CHAR_STR, sendChars, 3), 0);

						} else
						{
							subInfo.offLineVoiceStatus->wakeup_id = devicetypeinfo->subInfo.offLineVoiceStatus->wakeup_id;
						}
					}
					if(subInfo.offLineVoiceStatus->duration != devicetypeinfo->subInfo.offLineVoiceStatus->duration)
					{
						if(currentgateway)
						{
							//更新离线语音的唤醒时长
							//发送给设备
							uint8_t sendChars[3];
							sendChars[0] = 2;
							sendChars[1] = 0x18;
							if(devicetypeinfo->subInfo.offLineVoiceStatus->duration < 3) devicetypeinfo->subInfo.offLineVoiceStatus->duration = 3;
							else if(devicetypeinfo->subInfo.offLineVoiceStatus->duration > 60) devicetypeinfo->subInfo.offLineVoiceStatus->duration = 60;
							sendChars[2] = (uint8_t)devicetypeinfo->subInfo.offLineVoiceStatus->duration;
							pmMasterSerialPort->onWriteAttribute((uint32_t)onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0013, ZCL_DATATYPE_CHAR_STR, sendChars, 3), 0);
						}
						else
						{
							subInfo.offLineVoiceStatus->duration = devicetypeinfo->subInfo.offLineVoiceStatus->duration;
						}
					}
					//声音调节
					if(subInfo.offLineVoiceStatus->volume != devicetypeinfo->subInfo.offLineVoiceStatus->volume)
					{
						if(currentgateway)
						{
							//更新离线语音的声音大小
							//发送给设备
							uint8_t sendChars[3];
							sendChars[0] = 2;
							sendChars[1] = 0x1C;
							if(devicetypeinfo->subInfo.offLineVoiceStatus->volume < 0) devicetypeinfo->subInfo.offLineVoiceStatus->volume = 0;
							else if(devicetypeinfo->subInfo.offLineVoiceStatus->volume > 100) devicetypeinfo->subInfo.offLineVoiceStatus->volume = 100;
							sendChars[2] = (uint8_t)devicetypeinfo->subInfo.offLineVoiceStatus->volume;
							pmMasterSerialPort->onWriteAttribute((uint32_t)onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0013, ZCL_DATATYPE_CHAR_STR, sendChars, 3), 0);
						}
						else
						{
							subInfo.offLineVoiceStatus->volume = devicetypeinfo->subInfo.offLineVoiceStatus->volume;
						}
					}
					break;
				default:
					mPrintf(Log_Error, "Error:更新设备, 发现未定义的设备类型 ");
					break;
			}
		}

		if(updateRequest.values_size() > 0)
		{
			updateRequest.set_device_id(deviceID);
			updateRequest.set_sub_id(subID);
			updateRequest.set_sub_type(devType);
			mfTCPCMDSend(CMD_ID_DEVICE_VALUE_UPDATE_REQ, updateRequest.SerializeAsString().c_str(), updateRequest.SerializeAsString().length());
		}
		//释放这个内存
		delete devicetypeinfo;
	}
	return TRUE;
}

int32_t TypeDeviceTypeInfo::onGetShortAddr()
{
	return *pShortAddr;
}

EnumSubInfo *TypeDeviceTypeInfo::onGetSubInfo()
{
	return &subInfo;
}

uint8_t TypeDeviceTypeInfo::onGetZoneID()
{
	uint8_t retZoneID = 0;
	switch(devType)
	{
		case SUB_DEVICE_TYPE_PIR:retZoneID = (uint8_t)subInfo.pirStatus->zoneID;break;//红外探测
		case SUB_DEVICE_TYPE_SMOKE:retZoneID = (uint8_t)subInfo.smokeStatus->zoneID;break;//烟雾探测
		case SUB_DEVICE_TYPE_FLOOD:retZoneID = (uint8_t)subInfo.floodStatus->zoneID;break;//水浸检测
		case SUB_DEVICE_TYPE_SOS:retZoneID = (uint8_t)subInfo.sosStatus->zoneID;break;//一键报警
		case SUB_DEVICE_TYPE_DOOR_LOCK:retZoneID = (uint8_t)subInfo.doorLockStatus->zoneID;break;//智能门锁
		case SUB_DEVICE_TYPE_DOOR_WINDOW:retZoneID = (uint8_t)subInfo.doorWindowStatus->zoneID;break;//门窗报警
		default:break;
	}
	return retZoneID;
}

bool TypeDeviceTypeInfo::onSetIconID(bool currentgateway, bool todeviceflag, int32_t newiconid)
{
	bool retBool = FALSE;
	if(iconID != newiconid)
	{
		//直接修改数据库
		pDataBase->onUpdateSubDeviceInfo(this, SubIconID, newiconid);
		if(todeviceflag)
		{
			if(currentgateway)
			{
				//同步到设备
				pDataBase->onAddDevEventInfo(deviceID, subID, Event_Dev_Icon, 0);
			} else
			{
				pDataBase->onUpdateSubDeviceInfo(this, SubSaveIconID, newiconid);
			}
		}
		retBool = true;
	}
	return retBool;
}

bool TypeDeviceTypeInfo::onSetRoomID(int32_t newroomid)
{
	bool retBool = FALSE;
	if(roomID != newroomid)
	{
		pDataBase->onUpdateSubDeviceInfo(this, SubRoomID, newroomid);
		retBool = true;
	}
	return retBool;
}

int TypeDeviceTypeInfo::onGetStatus()
{
	int retStatus = 0;
	switch(devType)
	{
		case SUB_DEVICE_TYPE_LIGHT:retStatus = subInfo.lightStatus->status;break;
		case SUB_DEVICE_TYPE_DIMMER:retStatus = subInfo.dimmingStatus->status;break;
		case SUB_DEVICE_TYPE_CURTAIN:retStatus = subInfo.curtainStatus->status;break;
		case SUB_DEVICE_TYPE_SWITCH:retStatus = subInfo.switchStatus->status;break;
		case SUB_DEVICE_TYPE_GAS:retStatus = subInfo.gasStatus->gasValue;break;
		case SUB_DEVICE_TYPE_IR_REMOTE:retStatus = subInfo.irRemoteStatus->status;break;
		case SUB_DEVICE_TYPE_PIR:retStatus = subInfo.pirStatus->securityStatus;break;
		case SUB_DEVICE_TYPE_SMOKE:retStatus = subInfo.smokeStatus->status;break;
		case SUB_DEVICE_TYPE_FLOOD:retStatus = subInfo.floodStatus->status;break;
		case SUB_DEVICE_TYPE_SOS:retStatus = subInfo.sosStatus->status;break;
		case SUB_DEVICE_TYPE_DOOR_LOCK:retStatus = subInfo.doorLockStatus->status;break;
		case SUB_DEVICE_TYPE_DOOR_WINDOW:retStatus = subInfo.doorWindowStatus->securityStatus;break;
		case SUB_DEVICE_TYPE_ENV_DETECTOR:retStatus = subInfo.env_detectorStatus->tempSensorValue;break;
		case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:retStatus = subInfo.waterLeakStatus->value_status;break;
		case SUB_DEVICE_TYPE_GAS_ARM:retStatus = subInfo.gasArmStatus->value_status;break;
		case SUB_DEVICE_TYPE_CLOTHES_HANGER:retStatus = subInfo.clothesHangerStatus->status;break;
		default:retStatus = 0;break;
	}
	return retStatus;
}


void TypeDeviceTypeInfo::onSetShortAddr(int32_t *shortaddr)
{
	pShortAddr = shortaddr;
}

bool TypeDeviceTypeInfo::onSetName(bool currentgateay, bool todeviceflag, char *newname)
{
	bool retBool = FALSE;
	if(name != NULL)
	{
		if(name->onStringCMP(newname) == FALSE)
		{
			//保存到数据库
			pDataBase->onUpdateSubDeviceInfo(this, SubName, newname);
			if(todeviceflag)
			{
				if(currentgateay)
				{
					//更新一下设备
					pDataBase->onAddDevEventInfo(deviceID, subID, Event_Dev_Name, 0);
				} else
				{
					//直接更新到数据库
					pDataBase->onUpdateSubDeviceInfo(this, SubSaveName, newname);
				}
			}
			//更新到服务器
			if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_DEVNAME)
			{
				onNotifyToJava(JNI_NOTIFY_UPDATE_DEVNAME, deviceID, subID, devType, newname);
			}
			retBool = true;
		}
	}
	return retBool;
}

bool TypeDeviceTypeInfo::onSetDimmingParaValue(bool currentgateway, int32_t paravalue)
{
	bool retBool = FALSE;
	if(devType == SUB_DEVICE_TYPE_DIMMER)
	{
		if(paravalue != subInfo.dimmingStatus->paraValue)
		{
			subInfo.dimmingStatus->paraValue = paravalue;
			pDataBase->onUpdateSubDeviceInfo(this, SubDimmingParaValue, paravalue);
			if(!currentgateway)
			{
				//不是这个网关的设备就不检查这个参数
				subInfo.dimmingStatus->saveParaValue = subInfo.dimmingStatus->paraValue;
				pDataBase->onUpdateSubDeviceInfo(this, SubDimmingSaveParaValue, subInfo.dimmingStatus->saveParaValue);
			}
		}
		if(currentgateway && onGetShortAddr() && (subInfo.dimmingStatus->paraValue != subInfo.dimmingStatus->saveParaValue))
		{
			//启动设置命令
			if((subInfo.dimmingStatus->paraValue & 0xFF) != (subInfo.dimmingStatus->saveParaValue & 0xFF))
			{
				retBool = TRUE;
				//修改最小值 并读取最小值命令
				uint8_t tempValue = (uint8_t)(subInfo.dimmingStatus->paraValue & 0xFF);
				pmMasterSerialPort->onWriteAttributeGeneric((uint32_t)onGetShortAddr(), (uint8_t)subID, CLUSTER_ID_LEVELCONTROL, new TypeZclAttribute(0x0015, ZCL_DATATYPE_UINT8, &tempValue, 1), 0);
				//200ms后读取一下这个属性
				pmMasterSerialPort->onReadAttributeGeneric((uint32_t)onGetShortAddr(), (uint8_t)subID, CLUSTER_ID_LEVELCONTROL, 0x0015, 200);
			}
			else if((subInfo.dimmingStatus->paraValue & 0xFF00) != (subInfo.dimmingStatus->saveParaValue & 0xFF00))
			{
				retBool = TRUE;
				//修改最大值 并读取最大值
				uint8_t tempValue = (uint8_t)((subInfo.dimmingStatus->paraValue >> 8) & 0xFF);
				pmMasterSerialPort->onWriteAttributeGeneric((uint32_t)onGetShortAddr(), (uint8_t)subID, CLUSTER_ID_LEVELCONTROL, new TypeZclAttribute(0x0016, ZCL_DATATYPE_UINT8, &tempValue, 1), 0);
				//200ms后读取一下这个属性
				pmMasterSerialPort->onReadAttributeGeneric((uint32_t)onGetShortAddr(), (uint8_t)subID, CLUSTER_ID_LEVELCONTROL, 0x0016, 200);
			}
			else if((subInfo.dimmingStatus->paraValue & 0xFFFF0000) != (subInfo.dimmingStatus->saveParaValue & 0xFFFF0000))
			{
				retBool = TRUE;
				//修改步进值 并读取步进值
				TypeChar *sendValue = new TypeChar(16);
				sendValue->onAddInt16Ex(0, subInfo.dimmingStatus->paraValue >> 16);
				pmMasterSerialPort->onWriteAttributeGeneric((uint32_t)onGetShortAddr(), (uint8_t)subID, CLUSTER_ID_LEVELCONTROL, new TypeZclAttribute(0x0010, ZCL_DATATYPE_UINT16, sendValue->ubuff, 2), 0);
				//200ms后读取一下这个属性
				pmMasterSerialPort->onReadAttributeGeneric((uint32_t)onGetShortAddr(), (uint8_t)subID, CLUSTER_ID_LEVELCONTROL, 0x0010, 200);
				delete sendValue;
			}
		}
	}
	return retBool;
}

uint32_t TypeDeviceTypeInfo::onSetStatus(int32_t status)
{
	uint32_t retValue = 0;
	switch(devType)
	{
		case SUB_DEVICE_TYPE_LIGHT://light
		{
			if(status != 0)
			{
				status = 1;
			}
			pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_ONOFF, (uint8_t)status, NULL, 0, 0);
			if(subInfo.lightStatus->needSetStatus != status)
			{
				subInfo.lightStatus->needSetStatus = status;
				pDataBase->onAddDevEventInfo(deviceID, subID, Event_Dev_Status, 5);//等命令重发还是失败再执行
			}
		}
		break;
		case SUB_DEVICE_TYPE_SWITCH://switch
		{
			if(status != 0)
			{
				status = 1;
			}
			pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_ONOFF, (uint8_t)status, NULL, 0, 0);
			if(subInfo.switchStatus->needSetStatus != status)
			{
				subInfo.switchStatus->needSetStatus = status;
				pDataBase->onAddDevEventInfo(deviceID, subID, Event_Dev_Status, 5);//等命令重发还是失败再执行
			}
		}
			break;
		case SUB_DEVICE_TYPE_CURTAIN://窗帘状态设置
		{
			if(status == 501)
			{
				//发送停止命令
				status = 3;
				pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_ONOFF, (uint8_t)status, NULL, 0, 0);
			} else
			{
				TypeChar *sendBuff = new TypeChar(3);
				if(status > 100)
					status = 100;
				else if(status < 0)
					status = 0;
				sendBuff->ubuff[0] = (uint8_t)status;
				sendBuff->onAddInt16Ex(1, 0x0000);
				pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_LEVELCONTROL, 0x04, sendBuff->ubuff, 3, 0);
				delete sendBuff;
				//暂时没有做状态事件检测
			}
		}
			break;
		case SUB_DEVICE_TYPE_OFFLINE_VOICE://离线语音
		{
			if(status < 0) status = 0;
			else if(status > 100) status = 100;
			uint8_t sendChars[3];
			sendChars[0] = 2;
			sendChars[1] = 0x1C;
			sendChars[2] = (uint8_t)status;
			pmMasterSerialPort->onWriteAttribute((uint32_t)onGetShortAddr(), 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0013, ZCL_DATATYPE_CHAR_STR, sendChars, 3), 0);
		}
			break;
		case SUB_DEVICE_TYPE_DIMMER://dim
			if((status == 501) || (status == 0))
			{
				if(status != 0)
				{
					status = 1;
				}
				pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_ONOFF, (uint8_t)status, NULL, 0, 0);
			}
			else if((status > 0) && (status <= 100))
			{
				TypeChar *sendBuff = new TypeChar(3);
				float value = (float)((status * 255.00) / 100.00);
				if(((status * 255) % 100) > 0) value += 1.0;
				status = (uint8_t) value;
				if(status > 254)
					status = 254;
				sendBuff->ubuff[0] = (uint8_t)status;
				sendBuff->onAddInt16Ex(1, (subInfo.dimmingStatus->paraValue >> 16) & 0xFFFF);
				pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_LEVELCONTROL, 0x04, sendBuff->ubuff, 3, 0);
				delete sendBuff;
			}
			else if((status > 100) && (status <= 200))
			{
				status -= 100;
				TypeChar *sendBuff = new TypeChar(3);
				float value = (float)((status * 255.00) / 100.00);
				if(((status * 255) % 100) > 0) value += 1.0;
				status = (uint8_t) value;
				if(status > 254)
					status = 254;
				sendBuff->ubuff[0] = (uint8_t)status;
				sendBuff->onAddInt16Ex(1, 0x0000);
				pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_LEVELCONTROL, 0x04, sendBuff->ubuff, 3, 0);
				delete sendBuff;
			}
			//暂时没有做状态事件检测
			break;
		case SUB_DEVICE_TYPE_GAS:break;
		case SUB_DEVICE_TYPE_IR_REMOTE:break;
		case SUB_DEVICE_TYPE_PIR://响应布防/撤防
		{
			if(subInfo.pirStatus->securityStatus != status)
			{
				//更新到服务器
				onModifyDeviceSecurityStatus(deviceID, subID, devType, status);
			}
		}
			break;
		case SUB_DEVICE_TYPE_SMOKE:break;
		case SUB_DEVICE_TYPE_FLOOD:break;
		case SUB_DEVICE_TYPE_SOS:break;
		case SUB_DEVICE_TYPE_DOOR_LOCK:break;
		case SUB_DEVICE_TYPE_DOOR_WINDOW://响应布防/撤防
			if(subInfo.doorWindowStatus->securityStatus != status)
			{
				//更新到服务器
				onModifyDeviceSecurityStatus(deviceID, subID, devType, status);
			}
			break;
		case SUB_DEVICE_TYPE_ENV_DETECTOR:break;
		case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:
		{
			//控制 开关
			uint32_t value_status = (uint32_t)(status & 0xFF);
			int32_t  threshold = (status >> 8) & 0xFFFFFF;
			if(value_status != 0xFF)
			{
				//设置阀门状态
				if((value_status == 0) || (value_status == 1))
				{
					if(value_status != 0)
					{
						value_status = 1;
					}
					pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_ONOFF, (uint8_t)value_status, NULL, 0, 0);
				}
			}
			else
			{
				//设置阀值
				TypeChar * tempSendBuff = new TypeChar(4);
				tempSendBuff->onAddInt32(0, threshold);
				pmMasterSerialPort->onWriteAttribute((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0006, ZCL_DATATYPE_UINT32, tempSendBuff->ubuff, 4), 0);
				delete tempSendBuff;
			}
		}
			break;
		case SUB_DEVICE_TYPE_GAS_ARM://燃气报警
			if(status != 0)
			{
				status = 1;
			}
			//关闭关联阀门
			pmMasterSerialPort->onWriteZclCMD((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_ONOFF, (uint8_t)status, NULL, 0, 0);
			break;
		case SUB_DEVICE_TYPE_CLOTHES_HANGER:
		{
			int32_t sendValue = -1;
			switch(status)
			{
				case 0:sendValue = 0x000F;break;//关闭所有灯光
				case 1:sendValue = 0x0F0F;break;//打开所有设备
				case 2:sendValue = 0x0101;break;//开灯
				case 3:sendValue = 0x0001;break;//关灯
				case 4:sendValue = 0x0202;break;//开消毒
				case 5:sendValue = 0x0002;break;//关消毒
				case 6:sendValue = 0x0404;break;//开风扇
				case 7:sendValue = 0x0004;break;//关风扇
				case 8:sendValue = 0x0808;break;//升
				case 9:sendValue = 0x0008;break;//停
				case 10:sendValue = 0x1008;break;//降
				default:break;//未知
			}
			if(sendValue >= 0)
			{
				TypeChar * tempSendBuff = new TypeChar(2);
				tempSendBuff->onAddInt16Ex(0, sendValue);
				pmMasterSerialPort->onWriteAttribute((uint32_t)onGetShortAddr(), (uint8_t) subID, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000e, ZCL_DATATYPE_UINT16, tempSendBuff->ubuff, 2), 0);
				delete tempSendBuff;
			}
		}
			break;
		default:
			mPrintf(Log_Error, "Error:onGetRoom_ID() unknow  devType%d ", devType);
			break;
	}
	return retValue;
}

bool TypeDeviceTypeInfo::onUpdateDeviceStatus(DeviceValueFlag valueflag, int32_t value)
{
	bool retBool = false;
	bool jniNotifyFlag = false;
	//在这里主要还是更新其它网关的设备状态。本网关的设备肯定是最新的
	switch(devType)
	{
		case SUB_DEVICE_TYPE_LIGHT://light
		{
			if(valueflag == DEVICE_VALUE_FLAG_ONOFF)
			{
				if(subInfo.lightStatus->status != value)
				{
					subInfo.lightStatus->status = value;
				}
				jniNotifyFlag = true;
			}
		}
			break;
		case SUB_DEVICE_TYPE_SWITCH://switch
		{
			if(valueflag == DEVICE_VALUE_FLAG_ONOFF)
			{
				if(subInfo.switchStatus->status != value)
				{
					subInfo.switchStatus->status = value;
				}
				jniNotifyFlag = true;
			}
		}
			break;
		case SUB_DEVICE_TYPE_CURTAIN://窗帘状态设置
		{
			if(valueflag == DEVICE_VALUE_FLAG_LEVEL)
			{
				if(subInfo.curtainStatus->status != value)
				{
					subInfo.curtainStatus->status = value;
				}
				jniNotifyFlag = true;
			}
		}
			break;
		case SUB_DEVICE_TYPE_DIMMER://dim
			if(valueflag == DEVICE_VALUE_FLAG_LEVEL)
			{
				if(subInfo.dimmingStatus->status != value)
				{
					subInfo.dimmingStatus->status = value;
				}
				jniNotifyFlag = true;
			}
			//暂时没有做状态事件检测
			break;
		case SUB_DEVICE_TYPE_GAS:break;
		case SUB_DEVICE_TYPE_IR_REMOTE:break;
		case SUB_DEVICE_TYPE_PIR://响应布防/撤防
			if(valueflag == DEVICE_VALUE_FLAG_BATTERY_LEVEL)
			{
				if(subInfo.pirStatus->power != value)
				{
					subInfo.pirStatus->power = value;
				}
			}
			break;
		case SUB_DEVICE_TYPE_SMOKE:break;
		case SUB_DEVICE_TYPE_FLOOD:break;
		case SUB_DEVICE_TYPE_SOS:break;
		case SUB_DEVICE_TYPE_DOOR_LOCK:break;
		case SUB_DEVICE_TYPE_DOOR_WINDOW://响应布防/撤防
			if(valueflag == DEVICE_VALUE_FLAG_BATTERY_LEVEL)
			{
				if(subInfo.doorWindowStatus->power != value)
				{
					subInfo.doorWindowStatus->power = value;
				}
			}
			break;
		case SUB_DEVICE_TYPE_ENV_DETECTOR:break;
		case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:break;
		case SUB_DEVICE_TYPE_GAS_ARM:break;
		case SUB_DEVICE_TYPE_CLOTHES_HANGER:break;
		default:break;
	}
	if(jniNotifyFlag)
	{
		//直接把通知发送到应用层
		if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_DEVSTAUS)
		{
			onNotifyToJava(JNI_NOTIFY_UPDATE_DEVSTAUS, deviceID, subID, devType, value);
		}
	}
	return retBool;
}

TypeDeviceTypeInfo::~TypeDeviceTypeInfo()
{
	mMemNewFreeCount--;
	delete name;
	if(saveName)
	{
		delete saveName;
	}
	switch(devType)
	{
		case SUB_DEVICE_TYPE_LIGHT:
			delete subInfo.lightStatus;
			break;
		case SUB_DEVICE_TYPE_DIMMER:
			delete subInfo.dimmingStatus;
			break;
		case SUB_DEVICE_TYPE_CURTAIN:
			delete subInfo.curtainStatus;
			break;
		case SUB_DEVICE_TYPE_SWITCH:
			delete subInfo.switchStatus;
			break;
		case SUB_DEVICE_TYPE_GAS:
			delete subInfo.gasStatus;
			break;
		case SUB_DEVICE_TYPE_IR_REMOTE:
			delete subInfo.irRemoteStatus;
			break;
		case SUB_DEVICE_TYPE_PIR:
			delete subInfo.pirStatus;
			break;
		case SUB_DEVICE_TYPE_SMOKE:
			delete subInfo.smokeStatus;
			break;
		case SUB_DEVICE_TYPE_FLOOD:
			delete subInfo.floodStatus;
			break;
		case SUB_DEVICE_TYPE_SOS:
			delete subInfo.sosStatus;
			break;
		case SUB_DEVICE_TYPE_DOOR_LOCK:
			delete subInfo.doorLockStatus;
			break;
		case SUB_DEVICE_TYPE_DOOR_WINDOW:
			delete subInfo.doorWindowStatus;
			break;
		case SUB_DEVICE_TYPE_ENV_DETECTOR:
			delete subInfo.env_detectorStatus;
			break;
		case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR:
			delete subInfo.waterLeakStatus;
			break;
		case SUB_DEVICE_TYPE_GAS_ARM:
			delete subInfo.gasArmStatus;
			break;
		case SUB_DEVICE_TYPE_CLOTHES_HANGER:
			delete subInfo.clothesHangerStatus;
			break;
		case SUB_DEVICE_TYPE_RS485_TRANSFER:
			delete subInfo.rs485Status;
			break;
		case SUB_DEVICE_TYPE_OFFLINE_VOICE:
			delete subInfo.offLineVoiceStatus;
			break;
		default:
			mPrintf(Log_Error, "Error:内存释放 未定义的设备类型 ");
			break;
	}
};


