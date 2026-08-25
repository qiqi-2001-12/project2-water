/*
 * NetWorkCMD.cpp
 *
 *  Created on: Jul 18, 2017
 *      Author: root
 */
#include "../Main/WinobleMain.h"
#include "../DataType/TypeDefine.h"

void onNetCMDGetDeviceReq(uint32_t flag)
{
	if(pDataBase->onGetFamilyID() != 0)//已经组网成功才能下载设备
	{
		GetDevicesRequest deviceRequest;
		deviceRequest.Clear();
		deviceRequest.set_get_mask(flag);
		mfTCPCMDSend(CMD_ID_DEVICE_GET_ALL_REQ, deviceRequest.SerializeAsString().c_str(), deviceRequest.SerializeAsString().length());
	}
}

int onGetCMDGetDeviceRes(GetDevicesResponse *response)
{
	uint32_t tempGetMask = (uint32_t)response->get_mask();
	int index = 0;
	while((index < 32) && (tempGetMask > 0))
	{
		switch(tempGetMask & (1 << index))
		{
			case DEVICE_GET_MASK_DEVICE:
			{
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_DEVICE)
				{
					//先清除所有设备列表信息
					int32_t tempRandomValue = (int32_t)random();
					//mPrintf(Log_Error, "random() = %d", tempRandomValue);  这个随机是OK的
					for(int i = 0; i < response->devices_size(); i++)
					{
						pDeviceList->onAddDeviceInfo(new TypeDBDeviceInfo(response->devices(i).device_id(), response->devices(i).gateway_id(), response->devices(i).device_type(), response->devices(i).ieee(), response->devices(i).shortaddr(), response->devices(i).ieeeex(), response->devices(i).shortaddr_ex(), response->devices(i).rgbw(), 1, response->devices(i).status(), response->devices(i).protocol(), response->devices(i).protocol_version(), response->devices(i).target_screen(),
						                                                new TypeDeviceAttr(response->devices(i).attrs().screen(), response->devices(i).attrs().key(), response->devices(i).attrs().pird(), response->devices(i).attrs().temp(), 0, response->devices(i).attrs().illu(), response->devices(i).attrs().rgb()), (char *)response->devices(i).serial().c_str(), (char *)response->devices(i).sw_version().c_str(), (char *)response->devices(i).hw_version().c_str(),
						                                                (char *)response->devices(i).manufacturer().c_str(), response->devices(i).sub_device_count()), tempRandomValue);
					}

					//再检查一下数据库中有的白名单，但设备列表中已经不存在的
					pDeviceList->onAddDeviceInfoCheck(tempRandomValue);
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_DEVICE;
				}
			}
				break;
			case DEVICE_GET_MASK_ROOM:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_ROOM)
				{
					//清除所有房间信息
					int32_t tempRandValue = (int32_t)random();
					for(int i = 0; i < response->rooms_size(); i++)
					{
						pDeviceList->onAddRoomInfo(new TypeRoomInfo(response->rooms(i).room_id(), response->rooms(i).icon_id(), response->rooms(i).temp(), response->rooms(i).illum(), (char *)response->rooms(i).name().c_str()), tempRandValue);
					}
					TypeRoomInfo *tempRoomInfo = NULL;
					for(int i = 0; i < pDeviceList->roomList->size(); )
					{
						tempRoomInfo = (TypeRoomInfo *)pDeviceList->roomList->get(i);
						if(tempRoomInfo->room_id && (tempRoomInfo->randValue != tempRandValue))
						{
							pDeviceList->onDeleteRoomInfo(tempRoomInfo);
							continue;
						}
						i++;
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_ROOM;
				}
				break;
			case DEVICE_GET_MASK_LIGHT:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_LIGHT)
				{
					for(int i = 0; i < response->lights_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->lights(i).device_id(), response->lights(i).sub_id(), response->lights(i).room_id(), response->lights(i).icon_id(), 121111, response->lights(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_LIGHT, 0, new TypeLightStatus(response->lights(i).value(), response->lights(i).scene_id())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_LIGHT;
				}
				break;
			case DEVICE_GET_MASK_DIMMER:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_DIMMER)
				{
					for(int i = 0; i < response->dimmers_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->dimmers(i).device_id(), response->dimmers(i).sub_id(), response->dimmers(i).room_id(), response->dimmers(i).icon_id(), 121111, response->dimmers(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_DIMMER, 0, new TypeDimmingStatus(response->dimmers(i).value(), onGetDimmingParaValue(response->dimmers(i).min_value(), response->dimmers(i).max_value(), response->dimmers(i).duration()), onGetDimmingParaValue(10, 90, 1500))), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_DIMMER;
				}
				break;
			case DEVICE_GET_MASK_CURTAIN:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_CURTAIN)
				{
					for(int i = 0; i < response->curtains_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->curtains(i).device_id(), response->curtains(i).sub_id(), response->curtains(i).room_id(), response->curtains(i).icon_id(), 121111, response->curtains(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_CURTAIN, 0, new TypeCurtainStatus(response->curtains(i).value())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_CURTAIN;
				}
				break;
			case DEVICE_GET_MASK_SWITCH:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_SWITCH)
				{
					for(int i = 0; i < response->smart_switchs_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->smart_switchs(i).device_id(), response->smart_switchs(i).sub_id(), response->smart_switchs(i).room_id(), response->smart_switchs(i).icon_id(), 121111, response->smart_switchs(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_SWITCH, 0, new TypeSwitchStatus(response->smart_switchs(i).value(), response->smart_switchs(i).power(), response->smart_switchs(i).power_of_day(), response->smart_switchs(i).power_date())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_SWITCH;
				}
				break;
			case DEVICE_GET_MASK_GAS:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_GAS)
				{
					for(int i = 0; i < response->gases_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->gases(i).device_id(), response->gases(i).sub_id(), response->gases(i).room_id(), response->gases(i).icon_id(), 121111, response->gases(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_GAS, 0, new TypeGasStatus(response->gases(i).value())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_GAS;
				}
				break;
			case DEVICE_GET_MASK_IR_REMOTE:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_IR_REMOTE)
				{
					for(int i = 0; i < response->ir_remotes_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->ir_remotes(i).device_id(), response->ir_remotes(i).sub_id(), response->ir_remotes(i).room_id(), response->ir_remotes(i).icon_id(), 121111, response->ir_remotes(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_IR_REMOTE, 0, new TypeIRRemoteStatus(response->ir_remotes(i).value())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_IR_REMOTE;
				}
				break;
			case DEVICE_GET_MASK_PIR:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_PIR)
				{
					for(int i = 0; i < response->pirs_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->pirs(i).device_id(), response->pirs(i).sub_id(), response->pirs(i).room_id(), response->pirs(i).icon_id(), 121111, response->pirs(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_PIR, 0, new TypePIRStatus(response->pirs(i).value(), response->pirs(i).battery_level(), 0, response->pirs(i).protection_status(), response->pirs(i).dectection_timeout())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_PIR;
				}
				break;
			case DEVICE_GET_MASK_SMOKE:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_SMOKE)
				{
					for(int i = 0; i < response->smokes_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->smokes(i).device_id(), response->smokes(i).sub_id(), response->smokes(i).room_id(), response->smokes(i).icon_id(), 121111, response->smokes(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_SMOKE, 0, new TypeSmokeStatus(response->smokes(i).value(), response->smokes(i).battery_level(), 0)), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_SMOKE;
				}
				break;
			case DEVICE_GET_MASK_FLOOD:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_FLOOD)
				{
					for(int i = 0; i < response->floods_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->floods(i).device_id(), response->floods(i).sub_id(), response->floods(i).room_id(), response->floods(i).icon_id(), 121111, response->floods(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_FLOOD, 0, new TypeFloodStatus(response->floods(i).value(), response->floods(i).battery_level(), 0)), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_FLOOD;
				}
				break;
			case DEVICE_GET_MASK_DOOR_LOCK:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_DOOR_LOCK)
				{
					for(int i = 0; i < response->door_locks_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->door_locks(i).device_id(), response->door_locks(i).sub_id(), response->door_locks(i).room_id(), response->door_locks(i).icon_id(), 121111, response->door_locks(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_DOOR_LOCK, 0, new TypeDoorLockStatus(response->door_locks(i).value(), response->door_locks(i).battery_level(), 0)), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_DOOR_LOCK;
				}
				break;
			case DEVICE_GET_MASK_SOS:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_SOS)
				{
					for(int i = 0; i < response->soses_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->soses(i).device_id(), response->soses(i).sub_id(), response->soses(i).room_id(), response->soses(i).icon_id(), 121111, response->soses(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_SOS, 0, new TypeSOSStatus(response->soses(i).value(), response->soses(i).battery_level(), 0)), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_SOS;
				}
				break;
			case DEVICE_GET_MASK_DOOR_WINDOW:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_DOOR_WINDOW)
				{
					for(int i = 0; i < response->door_windows_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->door_windows(i).device_id(), response->door_windows(i).sub_id(), response->door_windows(i).room_id(), response->door_windows(i).icon_id(), 121111, response->door_windows(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_DOOR_WINDOW, 0, new TypeDoorWindowStatus(response->door_windows(i).value(), response->door_windows(i).battery_level(), 0, response->door_windows(i).protection_status())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_DOOR_WINDOW;
				}
				break;
			case DEVICE_GET_MASK_ENV_DETECTOR:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_ENV_DETECTOR)
				{
					for(int i = 0; i < response->env_detectors_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->env_detectors(i).device_id(), response->env_detectors(i).sub_id(), response->env_detectors(i).room_id(), response->env_detectors(i).icon_id(), 121111, response->env_detectors(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_ENV_DETECTOR, 0, new TypeENV_DetectorStatus(response->env_detectors(i).temp(), response->env_detectors(i).humidity(), response->env_detectors(i).illum(), response->env_detectors(i).pm25(), response->env_detectors(i).quality(),response->env_detectors(i).co2())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_ENV_DETECTOR;
				}
				break;
			case DEVICE_GET_MASK_WATER_LEAKAGE_DETECTOR:
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_WATER_LEAKAGE_DETECTOR)
				{
					for(int i = 0; i < response->water_leakage_detectors_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->water_leakage_detectors(i).device_id(), response->water_leakage_detectors(i).sub_id(), response->water_leakage_detectors(i).room_id(), response->water_leakage_detectors(i).icon_id(), 121111, response->water_leakage_detectors(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR, 0, new TypeWaterLeakStatus(response->water_leakage_detectors(i).valve_status(), response->water_leakage_detectors(i).flux(), response->water_leakage_detectors(i).threshold(), response->water_leakage_detectors(i).alarm_status())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_WATER_LEAKAGE_DETECTOR;
				}
				break;
			case DEVICE_GET_MASK_GAS_ARM://燃气臂
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_GAS_ARM)
				{
					for(int i = 0; i < response->gas_arms_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->gas_arms(i).device_id(), response->gas_arms(i).sub_id(), response->gas_arms(i).room_id(), response->gas_arms(i).icon_id(), 121111, response->gas_arms(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_GAS_ARM, 0, new TypeGasArmStatus(response->gas_arms(i).value())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_GAS_ARM;
				}
				break;
			case DEVICE_GET_MASK_CLOTHES_HANGER://晾衣架
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_CLOTHES_HANGER)
				{
					for(int i = 0; i < response->clothes_hangers_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->clothes_hangers(i).device_id(), response->clothes_hangers(i).sub_id(), response->clothes_hangers(i).room_id(), response->clothes_hangers(i).icon_id(), 121111, response->clothes_hangers(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_CLOTHES_HANGER, 0, new TypeGasArmStatus(response->clothes_hangers(i).value())), 1);
					}
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_CLOTHES_HANGER;
				}
				break;
			case DEVICE_GET_MASK_RS485_TRANSFER://得到485转接器
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_RS485_TRANSFER)//485转接器列表
				{
					for(int i = 0; i < response->rs485_transfer_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->rs485_transfer(i).device_id(), response->rs485_transfer(i).sub_id(), response->rs485_transfer(i).room_id(), response->rs485_transfer(i).icon_id(), 121111, response->rs485_transfer(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_RS485_TRANSFER, 0, new TypeRS485Status(response->rs485_transfer(i).value(), 0)), 1);
					}
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_RS485_TRANSFER;
				}
				break;
			case DEVICE_GET_MASK_APPLIANCE://得到家电列表
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_APPLIANCE)//家电列表
				{
					int32_t tempRandomValue = (int32_t)random();
					//添加新的家电列表
					for(int i = 0; i < response->appliances_size(); i++)
					{
						pDeviceList->onAddAppliancesInfo(new TypeApplianceInfo(response->appliances(i).id(),response->appliances(i).device_id(),
										response->appliances(i).sub_id(),
										(char *)response->appliances(i).name().c_str(),
										(char *)response->appliances(i).manufacturer().c_str(),
										(char *)response->appliances(i).model().c_str(),
										(char *)response->appliances(i).version().c_str(),
										(char *)response->appliances(i).serial().c_str(),
						                response->appliances(i).room_id(),
										response->appliances(i).type(),
										response->appliances(i).value(),
										response->appliances(i).addr(),
										(char *)response->appliances(i).value1().c_str(),
										(char *)response->appliances(i).config().c_str()
								), tempRandomValue);
					}
					//判断一下已经被删除的家电
					TypeApplianceInfo *tempApplianceInfo = NULL;
					for(int i = 0; i < pDeviceList->applianceList->size(); )
					{
						tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(i);
						if(tempApplianceInfo->randValue != tempRandomValue)
						{
							//删除这个家电
							pDeviceList->onDeleteApplianceInfo(tempApplianceInfo);
							continue;
						}
						i++;
					}
					//获取一下家庭所有的家电指令
					ListApplianceCmdRequest applianceCmdRequest;
					mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CMD_LIST_REQ, applianceCmdRequest.SerializeAsString().c_str(), applianceCmdRequest.SerializeAsString().length());
					//清除正在下载中标志  代表已经下载完成
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_APPLIANCE;
				}
				break;
			case DEVICE_GET_MASK_OFFLINE_VOICE://离线语音
			{
				if(pDeviceList->mDownLoadingFlag & DEVICE_GET_MASK_OFFLINE_VOICE)//485转接器列表
				{
					for(int i = 0; i < response->offline_voices_size(); i++)
					{
						pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(response->offline_voices(i).device_id(), response->offline_voices(i).sub_id(), response->offline_voices(i).room_id(), response->offline_voices(i).icon_id(), 121111, response->offline_voices(i).name().c_str(), "~!$2!", SUB_DEVICE_TYPE_OFFLINE_VOICE, 0, new TypeOffLineVoiceStatus(response->offline_voices(i).status(), response->offline_voices(i).wakeup_id(), response->offline_voices(i).duration(), response->offline_voices(i).volume())), 1);
					}
					pDeviceList->mDownLoadingFlag &= ~DEVICE_GET_MASK_OFFLINE_VOICE;
				}
			}
				break;
			case 0:break;
			default:
				pDeviceList->mDownLoadingFlag &= ~(1 << index);
				mPrintf(Log_NetWork, "have download request, but unkown type = %d", index);
				break;
		}
		tempGetMask &= ~(1 << index);
		index++;
	}

	//这里提示一下更新所有列表
	onNotifyToJava(JNI_NOTIFY_UPDATE_DEVLIST, tempGetMask, 0, 0, "");
	pDataBase->onSetStatus(HWELLYI_DB_VER);//代表更新完成
	pDeviceList->onPrintfRoomInfo();
	mIsUpdateRobotFlag = true;
	//这里获取一下燃气与燃气臂的绑定关系
	ListGasArmBindingRequest gasArmBindingRequest;
	gasArmBindingRequest.set_flag(0);
	mfTCPCMDSend(CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_REQ, gasArmBindingRequest.SerializeAsString().c_str(), gasArmBindingRequest.SerializeAsString().length());
	return 0;
}

void onCheckDBGatewayInfo(TypeDBDeviceInfo *dbdeviceinfo, int64_t newgatewayid)
{
	if(dbdeviceinfo)
	{
		if(dbdeviceinfo->gatewayID != newgatewayid)
		{
			//如果是强制让设备重新入网
			if(dbdeviceinfo->gatewayID)
			{
				if(dbdeviceinfo->gatewayID == pDataBase->onGetGateway_ID())
				{
					//需要让设备离网
					//让设备离网
					if(dbdeviceinfo->shortAddr)
					{
						pmMasterSerialPort->onLeaveWithIEEE(dbdeviceinfo->shortAddr, dbdeviceinfo->ieee);
					}
					if(dbdeviceinfo->ieee_ex)
					{
						if(DUALZIGBEECHIP)
						{
							pmSlaveSerialPort->onLeaveWithIEEE(dbdeviceinfo->shortAddr_ex, dbdeviceinfo->ieee_ex);
						}
					}
				}
				//更新本地信息
				pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, IGatewayID, newgatewayid);
				pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, IShortAddr, 0);
				pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, IShortAddr_Ex, 0);
				pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, IOnline, dbdeviceinfo->onLineFlag.onSetValue(DEVICE_STATUS_OFFLINE, DEVICE_STATUS_OFFLINE));
			}
			else
			{
				pDataBase->onUpdateDeviceInfoSqlValue(dbdeviceinfo, IGatewayID, newgatewayid);
			}
		}
	}
}

void onUpdateOnOffLineRequest(TypeDBDeviceInfo *dbdeviceinfo, DeviceStatus status)
{
	if(dbdeviceinfo && onGetConnectFlag())
	{
		PushPrivateMsgRequest pushPrivateMsgRequest;
		pushPrivateMsgRequest.set_flag(0);
		TypeChar *pushMsg = new TypeChar(512);
		if(status == DEVICE_STATUS_OFFLINE)
		{
			mPrintf(Log_NetWork, "keyID=%d $%04x$ ieee=%llx 已经离线! ", dbdeviceinfo->deviceID, dbdeviceinfo->shortAddr, dbdeviceinfo->ieee);
			//发送一个离线通知
		}
		else
		{
			mPrintf(Log_NetWork, "keyID=%d $%04x$ ieee=%llx 已经恢复在线! = deleay=%d saveCheck=%d", dbdeviceinfo->deviceID, dbdeviceinfo->shortAddr, dbdeviceinfo->ieee, dbdeviceinfo->delayTime, dbdeviceinfo->saveCheckTime);
			onUpdateDeviceInfo(dbdeviceinfo->deviceID, dbdeviceinfo->shortAddr, dbdeviceinfo->shortAddr_ex);
			//发送一个在线通知
			if(pDeviceList->onCheckDeviceEvent(dbdeviceinfo, Event_Dev_RGB))
			{
				//添加一个RGB事件
				pDataBase->onAddDevEventInfo(dbdeviceinfo->deviceID, 1, Event_Dev_RGB, 0);
			}
			if(pDeviceList->onCheckDeviceEvent(dbdeviceinfo, Event_Dev_Icon))
			{
				//检查一下图标
				for(int i = 1; i <= dbdeviceinfo->subCount; ++ i)
				{
					pDataBase->onAddDevEventInfo(dbdeviceinfo->deviceID, i, Event_Dev_Icon, 0);
				}
			}
			if(pDeviceList->onCheckDeviceEvent(dbdeviceinfo, Event_Dev_Name))
			{
				//检查一下名称
				for(int i = 1; i <= dbdeviceinfo->subCount; ++ i)
				{
					pDataBase->onAddDevEventInfo(dbdeviceinfo->deviceID, i, Event_Dev_Name, 0);
				}
			}
		}
		UpdateDeviceStatusRequest statusRequest;
		statusRequest.set_device_id(dbdeviceinfo->deviceID);
		statusRequest.set_status(status);
		mfTCPCMDSend(CMD_ID_DEVICE_STATUS_UPDATE_REQ, statusRequest.SerializeAsString().c_str(), statusRequest.SerializeAsString().length());
		delete pushMsg;
	}
}

void onUpdateMasterStartGatewayInfo(int32_t channel)
{
	ModifyGatewayInfoRequest gatewayInfo;
	gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_CHANNEL);
	gatewayInfo.set_channel(channel);
	pDataBase->onSetChannel(channel);
	mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
}

void onUpdateDLStatus(OTAUpgradeStage type, int32_t percent, const char * msg)
{
	OTAUpdateUpgradeProgressRequest otaUpdateUpgradeProgressRequest;
	otaUpdateUpgradeProgressRequest.set_category("gateway");
	otaUpdateUpgradeProgressRequest.set_target_id(pDataBase->onGetGateway_ID());
	otaUpdateUpgradeProgressRequest.set_auto_restart(1);
	otaUpdateUpgradeProgressRequest.set_stage(type);
	otaUpdateUpgradeProgressRequest.set_percent(percent);
	mfTCPCMDSend(CMD_ID_OTA_UPGRADE_PROGRESS_UPDATE_REQ, otaUpdateUpgradeProgressRequest.SerializeAsString().c_str(), otaUpdateUpgradeProgressRequest.SerializeAsString().length());
}

void onUpdateMasterResetGatewayInfo(int32_t channel, char *name, int32_t room_id)
{
	ModifyGatewayInfoRequest gatewayInfo;
	gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_CHANNEL | GATEWAY_ATTR_MASK_NAME | GATEWAY_ATTR_MASK_ROOM_ID);
	gatewayInfo.set_channel(channel);
	pDataBase->onSetChannel(channel);
	gatewayInfo.set_name(name);
	pDataBase->onSetName(name);
	gatewayInfo.set_room_id(room_id);
	pDataBase->onSetRoomID(room_id);
	mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
}

void onUpdateSlaveStartGatewayInfo(int32_t channel_ex)
{
	ModifyGatewayInfoRequest gatewayInfo;
	gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_CHANNEL_EX);
	gatewayInfo.set_channel(channel_ex);
	pDataBase->onSetChannel_Ex(channel_ex);
	mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
}

void onUpdateSlaveResetGatewayInfo(int32_t channel_ex, char *name, int32_t room_id)
{
	ModifyGatewayInfoRequest gatewayInfo;
	gatewayInfo.Clear();
	gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_CHANNEL_EX | GATEWAY_ATTR_MASK_NAME | GATEWAY_ATTR_MASK_ROOM_ID);
	gatewayInfo.set_channel(channel_ex);
	pDataBase->onSetChannel_Ex(channel_ex);
	gatewayInfo.set_name(name);
	pDataBase->onSetName(name);
	gatewayInfo.set_room_id(room_id);
	pDataBase->onSetRoomID(room_id);
	mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
}

void onUpdateDeviceSoftVer(int32_t device_id, char *ver)
{
	ModifyDeviceInfoRequest deviceInfoRequest;
	deviceInfoRequest.set_device_id(device_id);
	deviceInfoRequest.set_gateway_id(pDataBase->onGetGateway_ID());
	deviceInfoRequest.set_attr_mask(DEVICE_ATTR_MASK_SW_VERSION);
	deviceInfoRequest.set_sw_version(ver);
	mfTCPCMDSend(CMD_ID_DEVICE_MOD_INFO_REQ, deviceInfoRequest.SerializeAsString().c_str(), deviceInfoRequest.SerializeAsString().length());
}

void onModifyDeviceSecurityStatus(int32_t keyid, int32_t subid, int32_t type, int32_t security)
{
	ModifyDeviceInfoRequest modifyDeviceInfo;
	modifyDeviceInfo.set_device_id(keyid);
	modifyDeviceInfo.set_sub_id(subid);
	modifyDeviceInfo.set_sub_type(type);
	modifyDeviceInfo.set_attr_mask(DEVICE_ATTR_MASK_PROTECTION_STATUS);
	modifyDeviceInfo.set_protection_status(security);
	mfTCPCMDSend(CMD_ID_DEVICE_MOD_INFO_REQ, modifyDeviceInfo.SerializeAsString().c_str(), modifyDeviceInfo.SerializeAsString().length());
}

void onSendDevNoticeEvent(int32_t device_id, int32_t subid, EmunDeviceEventNotity event_type, uint8_t *buff, int32_t len)
{
    DeviceEventBroadcastRequest deviceEventBroadcastRequest;
    deviceEventBroadcastRequest.set_device_id(device_id);
    deviceEventBroadcastRequest.set_family_id(pDataBase->onGetFamilyID());
    deviceEventBroadcastRequest.set_sub_id(subid);
    deviceEventBroadcastRequest.set_event(event_type);
    deviceEventBroadcastRequest.set_value(buff, len);
    mfTCPCMDSend(CMD_ID_DEVICE_EVENT_BROADCAST_REQ, deviceEventBroadcastRequest.SerializeAsString().c_str(), deviceEventBroadcastRequest.SerializeAsString().length());
	//同时也广播给所有网关
	if(event_type == DEV_EVENT_SCREEN_SHARE)
	{
		onUDPSynScreebInfo(device_id, subid, (char *)buff);
	}
}

void onUpdateDeviceInfo(int32_t device_id, int32_t shortaddr, int32_t shortaddr_ex)
{
	ModifyDeviceInfoRequest deviceInfoRequest;
	deviceInfoRequest.set_device_id(device_id);
	deviceInfoRequest.set_attr_mask(DEVICE_ATTR_MASK_GATEWAY_ID | DEVICE_ATTR_MASK_SHORTADDR | DEVICE_ATTR_MASK_SHORTADDR_EX);
	deviceInfoRequest.set_gateway_id(pDataBase->onGetGateway_ID());
	deviceInfoRequest.set_shortaddr(shortaddr);
	deviceInfoRequest.set_shortaddr_ex(shortaddr_ex);
	mfTCPCMDSend(CMD_ID_DEVICE_MOD_INFO_REQ, deviceInfoRequest.SerializeAsString().c_str(), deviceInfoRequest.SerializeAsString().length());
}

void onGetSceneActionInfo(int64_t scene_id)
{
	GetSceneDetailRequest sceneRequest;
	sceneRequest.set_family_id(pDataBase->onGetFamilyID());
	sceneRequest.set_scene_id(scene_id);
	mfTCPCMDSend(CMD_ID_SCENE_DETAIL_GET_REQ, sceneRequest.SerializeAsString().c_str(), sceneRequest.SerializeAsString().length());
}

//修改场景信息
void onUpdateSceneStatusInfo(int64_t sceneid, int32_t status)
{
	if(sceneid != 0)
	{
		ModifySceneRequest modifySceneRequest;
		SceneInfo *scene = modifySceneRequest.mutable_scene();
		scene->set_family_id(pDataBase->onGetFamilyID());
		scene->set_scene_id(sceneid);
		scene->set_status(status);
		mPrintf(Log_NetWork, "scene status req send=%d ", status);
		modifySceneRequest.set_mask(SCENE_INFO_MASK_STATUS);
		mfTCPCMDSend(CMD_ID_SCENE_MOD_REQ, modifySceneRequest.SerializeAsString().c_str(), modifySceneRequest.SerializeAsString().length());
		//这里也发送一个UDP广播
		onUDPSetSceneInfo(sceneid, status);
	}
}
