/*
 * NetWorkCMDParsing.cpp
 *
 *  Created on: Jul 1, 2017
 *      Author: root
 */
#include "../Main/WinobleMain.h"
#include "../DataType/TypeDefine.h"

int mfTcpCMDParsing(TypeTcpCMD *pcmd)
{
	bool retBool = TRUE;
	int retError = 0;
	if(pcmd == NULL)
		return 0;
	mTcpReciveFlag = TRUE;
	if(onTcpAckCMDSend(pcmd))
	{
		if(onTcpCheckSeqNo(pcmd->seqNo, pcmd->commandID))
			return 0;
	}
	if(onCheckPrint())
	{
		if(pcmd->commandID != CMD_ID_ACK)
		{
			mPrintf(Log_NetWork, "TR:%s,seqNo=%d mem=%d Len=%d ", mGetNetCMDString(pcmd->commandID), pcmd->seqNo, mMemNewFreeCount, pcmd->packetLength);
		}
	}
	switch(pcmd->commandID)
	{
		case CMD_ID_AUTH_TOKEN_RES://tcp登录返回
		{
			AuthTokenResponse loginResponse;
			retBool = loginResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = loginResponse.error_code();
				if(retError == 0)
				{
					onNotifyToJava(JNI_NOTIFY_NET_STATUS, 0, 0, 0, "连接成功");
					onTimerAdd(TIMER_TCP_HEARTBEAT, loginResponse.idle_time() * 4000 / 5, true, mfTcpHeartbeatCMDCB, 0, 0);//启动心跳包
					//先获取当前网关的信息
					GetGatewayInfoRequest getGatewayInfoRequest;
					getGatewayInfoRequest.set_gateway_id(pDataBase->onGetGateway_ID());
					mfTCPCMDSend(CMD_ID_GATEWAY_GET_INFO_REQ, getGatewayInfoRequest.SerializeAsString().c_str(), getGatewayInfoRequest.SerializeAsString().length());
				}else
				{
					onTimerDelete(TIMER_TCP_HEARTBEAT);
				}
			}else
			{
				retError = 1;
			}
		}
			//start Heartbeat
			break;
		case CMD_ID_GATEWAY_GET_ALL_RES://得到家庭的网关列表
		{
			GetGatewaysResponse getGatewaysResponse;
			retBool = getGatewaysResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = getGatewaysResponse.error_code();
				if(retError == 0)
				{
					int32_t tempRandValue = (int32_t)random();
					for(int i = 0; i < getGatewaysResponse.gateways().size(); ++ i)
					{
						pDeviceList->onAddGatewayInfo(new TypeGatewayInfo(getGatewaysResponse.gateways(i).gateway_id(), getGatewaysResponse.gateways(i).room_id(), onGetGatewayModelInt(getGatewaysResponse.gateways(i).model().c_str()), getGatewaysResponse.gateways(i).status()), tempRandValue);
						if(getGatewaysResponse.gateways(i).gateway_id() == pDataBase->onGetGateway_ID())
						{
							//是本网关的需要更新一下网关信息
							//update info
							mfZigbeeUpdateNetWork(getGatewaysResponse.gateways(i).gateway_id(), getGatewaysResponse.gateways(i).family_id(), pDataBase->onGetFamilyID());
							if(pDataBase->onSetName((char *) getGatewaysResponse.gateways(i).name().c_str()))
							{
								mPrintf(Log_NetWork, "Gateway update name.%s->%s ", pDataBase->onGetName(), getGatewaysResponse.gateways(i).name().c_str());
								//update name
							}

							if(pDataBase->onSetRoomID(getGatewaysResponse.gateways(i).room_id()))
							{
								mPrintf(Log_NetWork, "Gateway update room_id.%d->%d ", pDataBase->onGetRoomID(), getGatewaysResponse.gateways(i).room_id());
								//update room_id
							}
							if(pDataBase->onSetTime_Zone((char *) getGatewaysResponse.gateways(i).time_zone().c_str()))
							{
								mPrintf(Log_NetWork, "Gateway update time_zone.%s->%s ", pDataBase->onGetTime_Zone(), getGatewaysResponse.gateways(i).time_zone().c_str());
								//update time_zone
							}
							if(pDataBase->onSetLanguage((char *) getGatewaysResponse.gateways(i).language().c_str()))
							{
								mPrintf(Log_NetWork, "Gateway update language.%s->%s ", pDataBase->onGetLanguage(), getGatewaysResponse.gateways(i).language().c_str());
								//update time_zone
							}
							if(pDataBase->onSetFamilyID(getGatewaysResponse.gateways(i).family_id()))
							{
								mPrintf(Log_NetWork, "Gateway update FamilyID.%d->%d ", pDataBase->onGetFamilyID(), getGatewaysResponse.gateways(i).family_id());
							}
							if(pDataBase->onSetSerial((char *)getGatewaysResponse.gateways(i).serial().c_str()))
							{
								mPrintf(Log_NetWork, "Gateway update Serial.%s->%s ", pDataBase->onGetSerial(), getGatewaysResponse.gateways(i).serial().c_str());
							}
							TypeChar *tempVer = new TypeChar();
							sprintf(tempVer->buff, "v%s", GATEWAY_SOFTVER);
							if(tempVer->onStringCMP(getGatewaysResponse.gateways(i).sw_version().c_str()) == FALSE)
							{
								//更新软件版本
								ModifyGatewayInfoRequest gatewayInfo;
								gatewayInfo.set_gateway_id(pDataBase->onGetGateway_ID());
								gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_SW_VERSION);
								gatewayInfo.set_sw_version(tempVer->buff);
								mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
							}
							delete tempVer;
						}
					}
					//如果网关不存在就删除
					TypeGatewayInfo *tempGatewayInfo = NULL;
					for(int i = 0; i < pDeviceList->gatewayList->size(); )
					{
						tempGatewayInfo = (TypeGatewayInfo *)pDeviceList->gatewayList->get(i);
						if((tempGatewayInfo->gatewayID != pDataBase->onGetGateway_ID()) && (tempGatewayInfo->randValue != tempRandValue))
						{
							pDeviceList->gatewayList->removeObject(tempGatewayInfo);
							continue;
						}
						i++;
					}
				}
				else
				{
					//获取设备信息错误  删除所有网关
					TypeGatewayInfo *tempGatewayInfo = NULL;
					for(int i = 0; i < pDeviceList->gatewayList->size(); )
					{
						tempGatewayInfo = (TypeGatewayInfo *)pDeviceList->gatewayList->get(i);
						if(tempGatewayInfo->gatewayID != pDataBase->onGetGateway_ID())
						{
							pDeviceList->gatewayList->removeObject(tempGatewayInfo);
							continue;
						}
						i++;
					}
				}
			}
		}
			break;
		case CMD_ID_GATEWAY_GET_INFO_RES://得到网关信息返回
		{
			//TODO 更新网关信息返回
			GetGatewayInfoResponse getGatewayResponse;
			retBool = getGatewayResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = getGatewayResponse.error_code();
				if(retError == 0)
				{
					if(getGatewayResponse.gateway().gateway_id() == pDataBase->onGetGateway_ID())
					{
						pDeviceList->onAddGatewayInfo(new TypeGatewayInfo(getGatewayResponse.gateway().gateway_id(), getGatewayResponse.gateway().room_id(), onGetGatewayModelInt(getGatewayResponse.gateway().model().c_str()), getGatewayResponse.gateway().status()), 0);
						//是本网关的需要更新一下网关信息
						//update info
						mfZigbeeUpdateNetWork(getGatewayResponse.gateway().gateway_id(), getGatewayResponse.gateway().family_id(), pDataBase->onGetFamilyID());
						if(pDataBase->onSetName((char *) getGatewayResponse.gateway().name().c_str()))
						{
							mPrintf(Log_NetWork, "Gateway update name.%s->%s ", pDataBase->onGetName(), getGatewayResponse.gateway().name().c_str());
							//update name
						}
						if(pDataBase->onSetRoomID(getGatewayResponse.gateway().room_id()))
						{
							mPrintf(Log_NetWork, "Gateway update room_id.%d->%d ", pDataBase->onGetRoomID(), getGatewayResponse.gateway().room_id());
							//update room_id
						}
						if(pDataBase->onSetTime_Zone((char *) getGatewayResponse.gateway().time_zone().c_str()))
						{
							mPrintf(Log_NetWork, "Gateway update time_zone.%s->%s ", pDataBase->onGetTime_Zone(), getGatewayResponse.gateway().time_zone().c_str());
							//update time_zone
						}
						if(pDataBase->onSetLanguage((char *) getGatewayResponse.gateway().language().c_str()))
						{
							mPrintf(Log_NetWork, "Gateway update language.%s->%s ", pDataBase->onGetLanguage(), getGatewayResponse.gateway().language().c_str());
							//update time_zone
						}
						if(pDataBase->onSetFamilyID(getGatewayResponse.gateway().family_id()))
						{
							mPrintf(Log_NetWork, "Gateway update FamilyID.%d->%d ", pDataBase->onGetFamilyID(), getGatewayResponse.gateway().family_id());
						}
						if(pDataBase->onSetSerial((char *)getGatewayResponse.gateway().serial().c_str()))
						{
							mPrintf(Log_NetWork, "Gateway update Serial.%s->%s ", pDataBase->onGetSerial(), getGatewayResponse.gateway().serial().c_str());
						}
						TypeChar *tempVer = new TypeChar();
						sprintf(tempVer->buff, "v%s", GATEWAY_SOFTVER);
						if(tempVer->onStringCMP(getGatewayResponse.gateway().sw_version().c_str()) == FALSE)
						{
							//更新软件版本
							ModifyGatewayInfoRequest gatewayInfo;
							gatewayInfo.set_gateway_id(pDataBase->onGetGateway_ID());
							gatewayInfo.set_attr_mask(GATEWAY_ATTR_MASK_SW_VERSION);
							gatewayInfo.set_sw_version(tempVer->buff);
							mfTCPCMDSend(CMD_ID_GATEWAY_MOD_INFO_REQ, gatewayInfo.SerializeAsString().c_str(), gatewayInfo.SerializeAsString().length());
						}
						delete tempVer;
						pDeviceList->onDownLoadResetAll();
						if(getGatewayResponse.family_id() != 0)
						{
							//获取这个家庭的所有网关
							GetGatewaysRequest gatewaysRequest;
							mfTCPCMDSend(CMD_ID_GATEWAY_GET_ALL_REQ, gatewaysRequest.SerializeAsString().c_str(), gatewaysRequest.SerializeAsString().length());
						}
						else
						{
							//删除所有网关
							TypeGatewayInfo *tempGatewayInfo = NULL;
							for(int i = 0; i < pDeviceList->gatewayList->size(); )
							{
								tempGatewayInfo = (TypeGatewayInfo *)pDeviceList->gatewayList->get(i);
								if(tempGatewayInfo->gatewayID != pDataBase->onGetGateway_ID())
								{
									pDeviceList->gatewayList->removeObject(tempGatewayInfo);
									continue;
								}
								i++;
							}
						}
					}
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_PONG://心跳包
		{
			Pong pong;
			retBool = pong.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//mPrintf(Log_NetWork, "%s mem=%d", onGetCurrentTimeMS(), mMemNewFreeCount);
			}else
			{
				retError = 1;
			}

			//在这里先请求一下有没有需要升级
			//mfHttpCheckAppUpdate();
		}
			break;
		case CMD_ID_DEVICE_GET_ALL_RES://设备下载返回
		{
			GetDevicesResponse getDeviceInfoRes;
			retBool = getDeviceInfoRes.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = getDeviceInfoRes.error_code();
				if(retError == 0)
				{
					onGetCMDGetDeviceRes(&getDeviceInfoRes);
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_TEST_GET_ID_RES://服务器设备IEEE信息查询返回
		{
			TestGetDeviceIdResponse getDeviceIdResponse;
			retBool = getDeviceIdResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = getDeviceIdResponse.error_code();
				if(retError == 0)
				{
					//查看 设备的家庭ID与当前网关是否一样
					DeviceInfo device = getDeviceIdResponse.device();
					//mPrintf(Log_NetWork, "设备服务器检查返回:DeviceID=%d FamilyID=%d GatewayID=%d", device.device_id(), device.family_id(), device.gateway_id());
					if(device.family_id() == pDataBase->onGetFamilyID())
					{
						//是这个家庭的设备
						if(pDataBase->onGetGateway_ID() == 0 || pDataBase->onGetSerial()[0] == 0)
						{
							//网关没有创建网络  没有意义
							mfLeaveToGateway(device.ieee());
						}
						else
						{
							//是这个网关的设备，没有组网 成功或者 出现没有下载成功！重新下载设备列表
							//更新一下当前数据库信息  如果不一样
							pDeviceList->onAddDeviceInfo(new TypeDBDeviceInfo(device.device_id(), device.gateway_id(), device.device_type(), device.ieee(), device.shortaddr(), device.ieeeex(), device.shortaddr_ex(), device.rgbw(), 1, device.status(), device.protocol(), device.protocol_version(), device.target_screen(),
							                                                  new TypeDeviceAttr(device.attrs().screen(), device.attrs().key(), device.attrs().pird(), device.attrs().temp(), 0, device.attrs().illu(), device.attrs().rgb()), (char *)device.serial().c_str(), (char *)device.sw_version().c_str(), (char *)device.hw_version().c_str(),
							                                                  (char *)device.manufacturer().c_str(), device.sub_device_count()), 1);
						}
					}
					else
					{
						//已经不在这个家庭了
						mfLeaveToGateway(device.ieee());
					}
				}
				else
				{
					//查询设备信息出错了
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_MOD_INFO_RES://修改设备信息响应
		{
			ModifyDeviceInfoResponse modifyDeviceInfoRes;
			retBool = modifyDeviceInfoRes.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = modifyDeviceInfoRes.error_code();
				if(retError == 0)
				{

				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_STATUS_UPDATE_RES://更新设备状态(在线/离线)响应
		{
			UpdateDeviceStatusResponse statusUpdadteRes;
			retBool = statusUpdadteRes.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = statusUpdadteRes.error_code();
				if(retError != 0)
				{
					if(retError == 20021)//提示没有入网
					{

					}
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_GATEWAY_MOD_INFO_RES://修改网关信息响应
		{
			ModifyGatewayInfoResponse modifyGatewayInfoResponse;
			retBool = modifyGatewayInfoResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = modifyGatewayInfoResponse.error_code();
				if(retError == 0)
				{
					//已经组网完成 更新下设备信息
					if(pDataBase->onGetChannel())
					{
						pDeviceList->onDownLoadResetAll();
					}
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_CTRL_RES://控制设备响应
		{
			CtrlDeviceResponse ctrlDeviceResponse;
			retBool = ctrlDeviceResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = ctrlDeviceResponse.error_code();
				if(retError == 0)
				{

				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_VALUE_UPDATE_RES://更新设备值响应
		{
			UpdateDeviceValueResponse updateDeviceValueResponse;
			retBool = updateDeviceValueResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = updateDeviceValueResponse.error_code();
				if(retError == 0)
				{

				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_GATEWAY_STATUS_CHANGED_NOTIFY://更新网关状态通知
		{
			//TODO(v1.401) 更新网关在线离线通知
		}
			break;
		case CMD_ID_DEVICE_STATUS_CHANGED_NOTIFY://更新设备在线/离线通知
		{
			//这里要实现啊
			DeviceStatusChangedNotification deviceStatusChangedNotification;
			retBool = deviceStatusChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, deviceStatusChangedNotification.device_id());
				if(tempDBDeviceInfo && (tempDBDeviceInfo->onLineFlag.bits.saveStatus != deviceStatusChangedNotification.status()))
				{
					//更新设备在线离线状态
                    mPrintf(Log_NetWork, "Device status update:%d ",deviceStatusChangedNotification.status());
					pDataBase->onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IOnline, tempDBDeviceInfo->onLineFlag.onSetValue(tempDBDeviceInfo->onLineFlag.bits.status, deviceStatusChangedNotification.status()));
				}
			}
		}
			break;
		case CMD_ID_DEVICE_VALUE_CHANGED_NOTIFY://更新设备状态通知
		{
			DeviceValueChangedNotification deviceValueChangedNotification;
			retBool = deviceValueChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, deviceValueChangedNotification.device_id());
				if(tempDBDeviceInfo)
				{
					TypeDeviceTypeInfo *tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(deviceValueChangedNotification.sub_id());
					if(tempDeviceTypeInfo && (tempDeviceTypeInfo->devType == deviceValueChangedNotification.sub_type()))
					{
						//根据具体情况去更新设备状态
						for(int i = 0; i < deviceValueChangedNotification.values_size(); ++ i)
						{
							tempDeviceTypeInfo->onUpdateDeviceStatus(deviceValueChangedNotification.values(i).flag(), deviceValueChangedNotification.values(i).value());
						}
					}
				}
			}
		}
			break;
		case CMD_ID_GATEWAY_INFO_CHNAGED_NOTIFY://网关信息更新通知
		{
			//TODO 网关信息更新 比如名称
			GatewayInfoChangedNotification gatewayInfoChanged;
			retBool = gatewayInfoChanged.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//如果是小网关，且是配置wifi的话，那就及时处理掉
				if(gatewayInfoChanged.attr_mask() & GATEWAY_ATTR_MASK_NET_INFO)
				{
#if defined(WINOBLE_LINUX) && (defined(HWELLYI_MT7688) || defined(H202_UK_SHA0))
					//设置wifi信息
					std::string tempWifi;
					std::string tempPsk;
					std::string tempMac;
					typedef ::google::protobuf::Map<std::string,std::string> GMap;

					GMap::const_iterator it = gatewayInfoChanged.net_infos().find("wifi");
					if (it != gatewayInfoChanged.net_infos().end()) {
						tempWifi = it->second;
						if(tempWifi.length() > 0)
						{
							it = gatewayInfoChanged.net_infos().find("psk");
							if (it != gatewayInfoChanged.net_infos().end()) {
								tempPsk = it->second;
							}
							it = gatewayInfoChanged.net_infos().find("mac");
							if (it != gatewayInfoChanged.net_infos().end()) {
								tempMac = it->second;
							}
							TypeChar *wifiChars = new TypeChar();
							sprintf(wifiChars->buff, "wifi_mode client \"%s\" \"%s\" \"%s\"", tempWifi.c_str(), tempPsk.c_str(), tempMac.c_str());
							mPrintf(Log_Error, "%s", wifiChars->buff);
							system(wifiChars->buff);
							delete wifiChars;
							//断开网络  重新连接，用于更新连接网络信息
							onResetTcpConnect();
						}
					}
#endif
				}
				//先获取当前网关的信息
				GetGatewayInfoRequest getGatewayInfoRequest;
				getGatewayInfoRequest.set_gateway_id(pDataBase->onGetGateway_ID());
				mfTCPCMDSend(CMD_ID_GATEWAY_GET_INFO_REQ, getGatewayInfoRequest.SerializeAsString().c_str(), getGatewayInfoRequest.SerializeAsString().length());
			}
		}
			break;
		case CMD_ID_GATEWAY_ADD_NOTIFY://添加网关通知
		{
			//TODO 添加网关通知
			mPrintf(Log_NetWork, "Gateway add notify! ");
			GatewayAddNotification addNotification;
			retBool = addNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				mfZigbeeUpdateNetWork(addNotification.gateway_id(), addNotification.family_id(), pDataBase->onGetFamilyID());
				pDeviceList->onDownLoadResetAll();
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_GATEWAY_DEL_NOTIFY://删除网关通知
		{
			//TODO 删除网关通知
			mPrintf(Log_NetWork, "Gateway delete notify! ");
			GatewayDeleteNotification deleteNotification;
			retBool = deleteNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//先获取当前网关的信息
				GetGatewayInfoRequest getGatewayInfoRequest;
				getGatewayInfoRequest.set_gateway_id(pDataBase->onGetGateway_ID());
				mfTCPCMDSend(CMD_ID_GATEWAY_GET_INFO_REQ, getGatewayInfoRequest.SerializeAsString().c_str(), getGatewayInfoRequest.SerializeAsString().length());
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_ADD_NOTIFY://添加设备通知
		{
			DeviceAddNotification addNotification;
			retBool = addNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//update device list
				pDeviceList->onDownLoadWithFlag(DEVICE_GET_MASK_DEVICE);
				//mPrintf(Log_NetWork, "Device add notify!=%d", addNotification.device_id());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_ADDED_NOTIFY://设备添加完成通知
		{
			DeviceAddedNotification addedNotification;
			retBool = addedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				mPrintf(Log_NetWork, "Device add finish notify==%d ", addedNotification.device_id());
				//重新获取一下设备信息
				//重新获取一下这个设备的所有子设备信息
				TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, addedNotification.device_id());
				if(tempDBDeviceInfo)
				{
					//检查一下设备是否已经切换网关
					onCheckDBGatewayInfo(tempDBDeviceInfo, addedNotification.gateway_id());
					for(int i = 1; i <= tempDBDeviceInfo->subCount; ++ i)
					{
						TypeDeviceTypeInfo *tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(i);
						if(tempDeviceTypeInfo)
						{
							GetDeviceInfoRequest deviceInfoRequest;
							deviceInfoRequest.set_family_id(pDataBase->onGetFamilyID());
							deviceInfoRequest.set_device_id(tempDeviceTypeInfo->deviceID);
							deviceInfoRequest.set_sub_id(tempDeviceTypeInfo->subID);
							deviceInfoRequest.set_sub_type(tempDeviceTypeInfo->devType);
							mfTCPCMDSend(CMD_ID_DEVICE_GET_INFO_REQ, deviceInfoRequest.SerializeAsString().c_str(), deviceInfoRequest.SerializeAsString().length());
						}
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_DEL_NOTIFY://删除设备通知
		{
			DeviceDeleteNotification deleteNotification;
			retBool = deleteNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				mPrintf(Log_NetWork, "Device delete notify!=%d ", deleteNotification.device_id());
				mfLeaveToGateway(deleteNotification.ieee());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_INFO_CHANGED_NOTIFY://设备信息变化通知
		{
			DeviceInfoChangedNotification deviceInfoChangeNofitfy;
			retBool = deviceInfoChangeNofitfy.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				if(deviceInfoChangeNofitfy.attr_mask() == DEVICE_ATTR_MASK_TARGET_SCREEN)
				{
					//修改一下设备信息  下载所有设备信息
					pDeviceList->onDownLoadResetAll();
				}
				else
				{
					if(deviceInfoChangeNofitfy.sub_id())
					{
						GetDeviceInfoRequest deviceInfoRequest;
						deviceInfoRequest.set_family_id(pDataBase->onGetFamilyID());
						deviceInfoRequest.set_device_id(deviceInfoChangeNofitfy.device_id());
						deviceInfoRequest.set_sub_id(deviceInfoChangeNofitfy.sub_id());
						deviceInfoRequest.set_sub_type(deviceInfoChangeNofitfy.sub_type());
						mfTCPCMDSend(CMD_ID_DEVICE_GET_INFO_REQ, deviceInfoRequest.SerializeAsString().c_str(), deviceInfoRequest.SerializeAsString().length());
					}
					else
					{
						TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, deviceInfoChangeNofitfy.device_id());
						if(tempDBDeviceInfo)
						{
							for(int i = 1; i <= tempDBDeviceInfo->subCount; ++ i)
							{
								TypeDeviceTypeInfo *tempDeviceTypeInfo = tempDBDeviceInfo->onGetSubInfo(i);
								if(tempDeviceTypeInfo)
								{
									GetDeviceInfoRequest deviceInfoRequest;
									deviceInfoRequest.set_family_id(pDataBase->onGetFamilyID());
									deviceInfoRequest.set_device_id(tempDeviceTypeInfo->deviceID);
									deviceInfoRequest.set_sub_id(tempDeviceTypeInfo->subID);
									deviceInfoRequest.set_sub_type(tempDeviceTypeInfo->devType);
									mfTCPCMDSend(CMD_ID_DEVICE_GET_INFO_REQ, deviceInfoRequest.SerializeAsString().c_str(), deviceInfoRequest.SerializeAsString().length());
								}
							}
						}
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_RGBW_CHANGED_NOTIFY://RGB 修改通知
		{
			DeviceRGBWChangedNotification deviceRGBWChangedNotification;
			retBool = deviceRGBWChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				if(deviceRGBWChangedNotification.flag() == DEVICE_RGBW_APPLY_FLAG_DEVICE)
				{
					//找到所在的设备  设置RGB值 只检查本网关的RGB
					TypeDBDeviceInfo *dbDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, deviceRGBWChangedNotification.device_id());
					if(dbDeviceInfo != NULL)
					{
						if(dbDeviceInfo->rgb != deviceRGBWChangedNotification.rgbw())
						{
							pDataBase->onUpdateDeviceInfoSqlValue(dbDeviceInfo, IRgb, deviceRGBWChangedNotification.rgbw());
							if(pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_RGB))
							{
								//远程设置RGB值
								pDataBase->onAddDevEventInfo(dbDeviceInfo->deviceID, 1, Event_Dev_RGB, 0);
							}
						}
					}
					else
					{
						//重新下载一下设备
						pDeviceList->onDownLoadWithFlag(DEVICE_GET_MASK_DEVICE);
					}
				}
				else
				{
					//重新下载一下设备
					pDeviceList->onDownLoadWithFlag(DEVICE_GET_MASK_DEVICE);
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_ALARM_RELEASE_RES://解除报警返回
			break;
		case CMD_ID_DEVICE_ALARM_NOTIFY://报警通知
		{
			DeviceAlarmedNotification alarmedNotification;
			retBool = alarmedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				if((alarmedNotification.device_alarm().family_id() == pDataBase->onGetFamilyID()) && (alarmedNotification.device_alarm().gateway_id() != pDataBase->onGetGateway_ID()))
				{
					//是本家庭的其它网关设备发出的报警
					TypeChar *retString = new TypeChar();
					onSendAlarmInfo((time_t)onGetTimeSec(), TRUE, (char *)alarmedNotification.device_alarm().device_name().c_str(), alarmedNotification.device_alarm().room_id(), alarmedNotification.device_alarm().alarm_type(),
					                alarmedNotification.device_alarm().device_id(), alarmedNotification.device_alarm().sub_id(), alarmedNotification.device_alarm().sub_type(), retString);
					delete retString;
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_ALARM_RELEASED_NOTIFY://设备解除报警通知
		{
			DeviceAlarmReleasedNotification releasedNotification;
			retBool = releasedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接给设备和app发送解除报警消息
				//发送到app
				onNotifyToJava(JNI_NOTIFY_ALARM, 0, 0, 0, "");
				TypeChar *alarmInfo = new TypeChar(7);//给设备发送解除报警
				alarmInfo->buff[0] = 6;
				alarmInfo->buff[1] = 0;
				alarmInfo->buff[2] = 0;
				alarmInfo->buff[3] = 0;
				alarmInfo->buff[4] = 0;
				alarmInfo->buff[5] = 0;
				alarmInfo->buff[6] = 0;
				pmMasterSerialPort->onWriteZclCMD((uint32_t)SHORTADDR_BROADCAST, (uint8_t)1, CLUSTER_ID_PERSONAL, 0x01, alarmInfo->ubuff, 7, 0);
				mIsAlarmingFlag = FALSE;
				delete alarmInfo;
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ROOM_ADDED_NOTIFY://添加房间通知
		{
			RoomAddedNotification roomAddedNotification;
			retBool = roomAddedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				if(roomAddedNotification.family_id() == pDataBase->onGetFamilyID())
				{
					//mPrintf(Log_NetWork, "Add roomInfo=%s %d ", roomAddedNotification.name().c_str(), roomAddedNotification.room_id());
					//添加这个房间信息
					pDeviceList->onAddRoomInfo(new TypeRoomInfo(roomAddedNotification.room_id(), roomAddedNotification.icon_id(), 0, 0, (char *)roomAddedNotification.name().c_str()), random());
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ROOM_ENV_UPDATE_RES://更新房间环境返回
		{
			UpdateRoomEnvResponse updateRoomEnvResponse;
			retBool = updateRoomEnvResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = updateRoomEnvResponse.error_code();
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ROOM_ENV_CHANGED_NOTIFY://房间温度变化通知
		{
			RoomEnvChangedNotification roomEnvChangedNotification;
			retBool = roomEnvChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeRoomInfo *tempRoomInfo = pDeviceList->onFindRoomInfo(roomEnvChangedNotification.room_id());
				if(tempRoomInfo != NULL)
				{//pDeviceList->onCheckSceneCarried(0, tempDeviceTypeInfo->deviceID, tempDeviceTypeInfo->subID, 1);
					if(roomEnvChangedNotification.env_mask() & ROOM_ENV_MASK_TEMP)
					{
						if(tempRoomInfo->temp_value != roomEnvChangedNotification.temp())
						{
							//查找一下所有场景条件
							TypeSceneNameInfo *tempSceneNameInfo = NULL;
							TypeSceneCondInfo *tempSceneCondInfo = NULL;
							for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
							{
								bool isProcess = FALSE;
								tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
								if(tempSceneNameInfo != NULL)
								{
									for(int j = 0; j < tempSceneNameInfo->onCondInfoList->size(); ++j)
									{
										tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->onCondInfoList->get(j);
										if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 4) && (tempSceneCondInfo->device_id == tempRoomInfo->room_id) && (tempSceneCondInfo->sub_id == 1))
										{
											//是这个设备
											if((tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP UP") && (tempSceneCondInfo->action > tempRoomInfo->temp_value) && (tempSceneCondInfo->action <= roomEnvChangedNotification.temp()))
											   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP DOWN") && (tempSceneCondInfo->action < tempRoomInfo->temp_value) && (tempSceneCondInfo->action >= roomEnvChangedNotification.temp())))
											{
												//执行这个场景
												isProcess = TRUE;
												pDeviceList->onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
												break;
											}
										}
									}
									if(isProcess == FALSE)
									{
										for(int j = 0; j < tempSceneNameInfo->offCondInfoList->size(); ++j)
										{
											tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->offCondInfoList->get(j);
											if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 4) && (tempSceneCondInfo->device_id == tempRoomInfo->room_id) && (tempSceneCondInfo->sub_id == 1))
											{
												//是这个设备
												if((tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP UP") && (tempSceneCondInfo->action > tempRoomInfo->temp_value) && (tempSceneCondInfo->action <= roomEnvChangedNotification.temp()))
												   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"TEMP DOWN") && (tempSceneCondInfo->action < tempRoomInfo->temp_value) && (tempSceneCondInfo->action >= roomEnvChangedNotification.temp())))
												{
													//执行这个场景
													pDeviceList->onSetSceneStatus(tempSceneNameInfo, 0, TRUE);
													break;
												}
											}
										}
									}
								}
							}
							tempRoomInfo->temp_value = roomEnvChangedNotification.temp();
						}
						mPrintf(Log_NetWork, "old tem=%d new=%d ", tempRoomInfo->temp_value, roomEnvChangedNotification.temp());
					}
					if(roomEnvChangedNotification.env_mask() & ROOM_ENV_MASK_ILLUM)
					{
						if(tempRoomInfo->illum_value != roomEnvChangedNotification.illum())
						{
							mPrintf(Log_NetWork, "roomID=%d ILLU = %d", roomEnvChangedNotification.room_id(), roomEnvChangedNotification.illum());
							//查找一下所有场景条件
							TypeSceneNameInfo *tempSceneNameInfo = NULL;
							TypeSceneCondInfo *tempSceneCondInfo = NULL;
							for(int i = 0; i < pDeviceList->sceneList->size(); ++i)
							{
								bool isProcess = FALSE;
								tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
								if(tempSceneNameInfo != NULL)
								{
									for(int j = 0; j < tempSceneNameInfo->onCondInfoList->size(); ++j)
									{
										tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->onCondInfoList->get(j);
										if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 4) && (tempSceneCondInfo->device_id == tempRoomInfo->room_id) && (tempSceneCondInfo->sub_id == 2))
										{
											//是这个设备
											if((tempSceneCondInfo->action_desc->onStringCMP((char *)"ILLU UP") && (tempSceneCondInfo->action > tempRoomInfo->illum_value) && (tempSceneCondInfo->action <= roomEnvChangedNotification.illum()))
											   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"ILLU DOWN") && (tempSceneCondInfo->action < tempRoomInfo->illum_value) && (tempSceneCondInfo->action >= roomEnvChangedNotification.illum())))
											{
												//执行这个场景
												isProcess = TRUE;
												pDeviceList->onSetSceneStatus(tempSceneNameInfo, 1, TRUE);
												break;
											}
										}
									}
									if(isProcess == FALSE)
									{
										for(int j = 0; j < tempSceneNameInfo->offCondInfoList->size(); ++j)
										{
											tempSceneCondInfo = (TypeSceneCondInfo *)tempSceneNameInfo->offCondInfoList->get(j);
											if((tempSceneCondInfo != NULL) && (tempSceneCondInfo->type == 4) && (tempSceneCondInfo->device_id == tempRoomInfo->room_id) && (tempSceneCondInfo->sub_id == 2))
											{
												//是这个设备
												if((tempSceneCondInfo->action_desc->onStringCMP((char *)"ILLU UP") && (tempSceneCondInfo->action > tempRoomInfo->illum_value) && (tempSceneCondInfo->action <= roomEnvChangedNotification.illum()))
												   || (tempSceneCondInfo->action_desc->onStringCMP((char *)"ILLU DOWN") && (tempSceneCondInfo->action < tempRoomInfo->illum_value) && (tempSceneCondInfo->action >= roomEnvChangedNotification.illum())))
												{
													//执行这个场景
													pDeviceList->onSetSceneStatus(tempSceneNameInfo, 0, TRUE);
													break;
												}
											}
										}
									}
								}
							}
							tempRoomInfo->illum_value = roomEnvChangedNotification.illum();
						}
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ROOM_GET_INFO_RES://得到房间信息返回
		{
			GetRoomInfoResponse getRoomInfoResponse;
			retBool = getRoomInfoResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = getRoomInfoResponse.error_code();
				if(retError == 0)
				{
					pDeviceList->onAddRoomInfo(new TypeRoomInfo(getRoomInfoResponse.info().room_id(), getRoomInfoResponse.info().icon_id(), getRoomInfoResponse.info().temp(), getRoomInfoResponse.info().humidity(), getRoomInfoResponse.info().name().c_str()), random());
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ROOM_INFO_CHANGED_NOTIFY://房间信息修改通知
		{
			RoomInfoChangedNotification roomInfoChangedNotification;
			retBool = roomInfoChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接通知房间ID去获取房间变化信息
				GetRoomInfoRequest getRoomInfoRequest;
				getRoomInfoRequest.set_family_id(pDataBase->onGetFamilyID());
				getRoomInfoRequest.set_room_id(roomInfoChangedNotification.room_id());
				mfTCPCMDSend(CMD_ID_ROOM_GET_INFO_REQ, getRoomInfoRequest.SerializeAsString().c_str(), getRoomInfoRequest.SerializeAsString().length());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ROOM_DELETED_NOTIFY://删除房间信息通知
		{
			RoomDeletedNotification roomDeletedNotification;
			retBool = roomDeletedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				if(roomDeletedNotification.family_id() == pDataBase->onGetFamilyID())
				{
					//有房间删除，重新下载设备
					pDeviceList->onDownLoadResetAll();
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_ALARM_RES://设备报警请求返回
		{
			DeviceAlarmResponse deviceAlarmResponse;
			retBool = deviceAlarmResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = deviceAlarmResponse.error_code();
				if(retError == 0)
				{

				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_GET_INFO_RES://得到设备详情
		{
			GetDeviceInfoResponse rsp;
			retBool = rsp.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = rsp.error_code();
				if(retError == 0)
				{
					//update device info
					TypeDBDeviceInfo *dbDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, rsp.device_id());
					if(dbDeviceInfo)
					{
						TypeDeviceTypeInfo *deviceTypeInfo = dbDeviceInfo->onGetSubInfo(rsp.sub_id());
						if(deviceTypeInfo != NULL)
						{
							switch(rsp.sub_type())
							{
								case SUB_DEVICE_TYPE_LIGHT:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.light().device_id(), rsp.light().sub_id(), rsp.light().room_id(), rsp.light().icon_id(), deviceTypeInfo->saveIconID, rsp.light().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_LIGHT, 0,new TypeLightStatus(rsp.light().value(), rsp.light().scene_id())));
									break;

								case SUB_DEVICE_TYPE_DIMMER:
								{
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.dimmer().device_id(), rsp.dimmer().sub_id(), rsp.dimmer().room_id(), rsp.dimmer().icon_id(), deviceTypeInfo->saveIconID, rsp.dimmer().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_DIMMER, 0, new TypeDimmingStatus(rsp.dimmer().value(), onGetDimmingParaValue(rsp.dimmer().min_value(), rsp.dimmer().max_value(), rsp.dimmer().duration()), deviceTypeInfo->onGetSubInfo()->dimmingStatus->saveParaValue)));
								}
									break;
								case SUB_DEVICE_TYPE_SWITCH:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.smart_switch().device_id(), rsp.smart_switch().sub_id(), rsp.smart_switch().room_id(), rsp.smart_switch().icon_id(), deviceTypeInfo->saveIconID, rsp.smart_switch().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_SWITCH, 0, new TypeSwitchStatus(rsp.smart_switch().value(), rsp.smart_switch().power(), rsp.smart_switch().power_of_day(), rsp.smart_switch().power_date())));
									break;
								case SUB_DEVICE_TYPE_CURTAIN:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.curtain().device_id(), rsp.curtain().sub_id(), rsp.curtain().room_id(), rsp.curtain().icon_id(), deviceTypeInfo->saveIconID, rsp.curtain().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_CURTAIN, 0, new TypeCurtainStatus(rsp.curtain().value())));
									break;
								case SUB_DEVICE_TYPE_GAS:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.gas().device_id(), rsp.gas().sub_id(), rsp.gas().room_id(), rsp.gas().icon_id(), deviceTypeInfo->saveIconID, rsp.gas().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_GAS, 0, new TypeGasStatus(rsp.gas().value())));
									break;
								case SUB_DEVICE_TYPE_IR_REMOTE:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.ir_remote().device_id(), rsp.ir_remote().sub_id(), rsp.ir_remote().room_id(), rsp.ir_remote().icon_id(), deviceTypeInfo->saveIconID, rsp.ir_remote().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_IR_REMOTE, 0, new TypeIRRemoteStatus(rsp.ir_remote().value())));
									break;
								case SUB_DEVICE_TYPE_PIR:
								{
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.pir().device_id(), rsp.pir().sub_id(), rsp.pir().room_id(), rsp.pir().icon_id(), deviceTypeInfo->saveIconID, rsp.pir().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_PIR, 0, new TypePIRStatus(rsp.pir().value(), rsp.pir().battery_level(), dbDeviceInfo->shortAddr_ex, rsp.pir().protection_status(), rsp.pir().dectection_timeout())));
								}
									break;
								case SUB_DEVICE_TYPE_SMOKE:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.smoke().device_id(), rsp.smoke().sub_id(), rsp.smoke().room_id(), rsp.smoke().icon_id(), deviceTypeInfo->saveIconID, rsp.smoke().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_SMOKE, 0, new TypeSmokeStatus(rsp.smoke().value(), rsp.smoke().battery_level(), dbDeviceInfo->shortAddr_ex)));
									break;
								case SUB_DEVICE_TYPE_FLOOD:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.flood().device_id(), rsp.flood().sub_id(), rsp.flood().room_id(), rsp.flood().icon_id(), deviceTypeInfo->saveIconID, rsp.flood().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_FLOOD, 0, new TypeFloodStatus(rsp.flood().value(), rsp.flood().battery_level(), dbDeviceInfo->shortAddr_ex)));
									break;
								case SUB_DEVICE_TYPE_DOOR_LOCK:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.door_lock().device_id(), rsp.door_lock().sub_id(), rsp.door_lock().room_id(), rsp.door_lock().icon_id(), deviceTypeInfo->saveIconID, rsp.door_lock().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_DOOR_LOCK, 0, new TypeFloodStatus(rsp.door_lock().value(), rsp.door_lock().battery_level(), dbDeviceInfo->shortAddr_ex)));
									break;
								case SUB_DEVICE_TYPE_SOS:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.sos().device_id(), rsp.sos().sub_id(), rsp.sos().room_id(), rsp.sos().icon_id(), deviceTypeInfo->saveIconID, rsp.sos().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_SOS, 0, new TypeFloodStatus(rsp.sos().value(), rsp.sos().battery_level(), dbDeviceInfo->shortAddr_ex)));
									break;
								case SUB_DEVICE_TYPE_DOOR_WINDOW:
								{
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.door_window().device_id(), rsp.door_window().sub_id(), rsp.door_window().room_id(), rsp.door_window().icon_id(), deviceTypeInfo->saveIconID, rsp.door_window().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_DOOR_WINDOW, 0, new TypeDoorWindowStatus(rsp.door_window().value(), rsp.door_window().battery_level(), dbDeviceInfo->shortAddr_ex, rsp.door_window().protection_status())));
								}
									break;
								case SUB_DEVICE_TYPE_ENV_DETECTOR:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.env_detector().device_id(), rsp.env_detector().sub_id(), rsp.env_detector().room_id(), rsp.env_detector().icon_id(), deviceTypeInfo->saveIconID, rsp.env_detector().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_ENV_DETECTOR, 0, new TypeENV_DetectorStatus(rsp.env_detector().temp(), rsp.env_detector().humidity(), rsp.env_detector().illum(), rsp.env_detector().pm25(), rsp.env_detector().quality(),rsp.env_detector().co2())));
									break;
								case SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR://更新设备信息
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.water_leakage_detector().device_id(), rsp.water_leakage_detector().sub_id(), rsp.water_leakage_detector().room_id(), rsp.water_leakage_detector().icon_id(), deviceTypeInfo->saveIconID, rsp.water_leakage_detector().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR, 0, new TypeWaterLeakStatus(rsp.water_leakage_detector().valve_status(), rsp.water_leakage_detector().flux(), rsp.water_leakage_detector().threshold(), rsp.water_leakage_detector().alarm_status())));
									break;
								case SUB_DEVICE_TYPE_GAS_ARM://更新燃气臂信息
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.gas_arm().device_id(), rsp.gas_arm().sub_id(), rsp.gas_arm().room_id(), rsp.gas_arm().icon_id(), deviceTypeInfo->saveIconID, rsp.gas_arm().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_GAS_ARM, 0, new TypeGasArmStatus(rsp.gas_arm().value())));
									break;
								case SUB_DEVICE_TYPE_CLOTHES_HANGER://晾衣架
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.clothes_hanger().device_id(), rsp.clothes_hanger().sub_id(), rsp.clothes_hanger().room_id(), rsp.clothes_hanger().icon_id(), deviceTypeInfo->saveIconID, rsp.clothes_hanger().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_CLOTHES_HANGER, 0, new TypeGasArmStatus(rsp.clothes_hanger().value())));
									break;
								case SUB_DEVICE_TYPE_RS485_TRANSFER:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.rs485_transfer().device_id(), rsp.rs485_transfer().sub_id(), rsp.rs485_transfer().room_id(), rsp.rs485_transfer().icon_id(), deviceTypeInfo->saveIconID, rsp.rs485_transfer().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_RS485_TRANSFER, 0, new TypeRS485Status(rsp.rs485_transfer().value(), 0)));
									break;
								case SUB_DEVICE_TYPE_OFFLINE_VOICE:
									deviceTypeInfo->onUpdateTypeInfo(dbDeviceInfo->gatewayID == pDataBase->onGetGateway_ID(), pDeviceList->onCheckDeviceEvent(dbDeviceInfo, Event_Dev_Name),
									                                 new TypeDeviceTypeInfo(rsp.offline_voice().device_id(), rsp.offline_voice().sub_id(), rsp.offline_voice().room_id(), rsp.offline_voice().icon_id(), deviceTypeInfo->saveIconID, rsp.offline_voice().name().c_str(), deviceTypeInfo->saveName->buff,
									                                                        SUB_DEVICE_TYPE_OFFLINE_VOICE, 0, new TypeOffLineVoiceStatus(rsp.offline_voice().status(), rsp.offline_voice().wakeup_id(), rsp.offline_voice().duration(), rsp.offline_voice().volume())));
									break;
								default:
									mPrintf(Log_NetWork, "设备信息更新，没有找到对应的设备类型! ");
									break;
							}
							//mPrintf(Log_NetWork, "New Info!name=%s roomID=%d imageID=%d ", deviceTypeInfo->name->buff, deviceTypeInfo->roomID, deviceTypeInfo->iconID);
						}
					}
					else
					{
						//设备信息表中没有这个设备  就请求服务器重新下载设备
						//update device list
						pDeviceList->onDownLoadWithFlag(DEVICE_GET_MASK_DEVICE);
					}
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_CTRL_REQ ://设备控制请求
		{
			CtrlDeviceRequest ctrlDeviceRequest;
			CtrlDeviceResponse ctrlDeviceResponse;
			ctrlDeviceResponse.Clear();
			ctrlDeviceRequest.Clear();
			retBool = ctrlDeviceRequest.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = 0;
				TypeDBDeviceInfo * tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, ctrlDeviceRequest.device_id());
				if(tempDBDeviceInfo && (tempDBDeviceInfo->gatewayID == pDataBase->onGetGateway_ID()))//是本网关的设备
				{
					retError = pDeviceList->onSetDeviceStatus(tempDBDeviceInfo, ctrlDeviceRequest.sub_id(), ctrlDeviceRequest.status(), TRUE);
					if(retError != 0)
					{
						if(ERROR_DEVICE_IS_UNEXIST == retError)
						{
							//找不到这个设备  重新下载所有设备
							//update device list
							pDeviceList->onDownLoadWithFlag(DEVICE_GET_MASK_DEVICE);
						}
					}
					ctrlDeviceResponse.set_error_code(retError);
				}
			}
			else
			{
				ctrlDeviceResponse.set_error_code(ERROR_PARA_WRONG);
				retError = 1;
			}
			mfTCPCMDSend(CMD_ID_DEVICE_CTRL_RES, ctrlDeviceResponse.SerializeAsString().c_str(), ctrlDeviceResponse.SerializeAsString().length());
		}
			break;
		case CMD_ID_ACK://消息确认包
		{
			Ack ack;
			retBool = ack.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				for(int i = 0; i < ack.seq_nos_size(); i++)
				{
					onTcpCheckRepeatList((uint32_t)ack.seq_nos(i));
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_LIST_RES://得到场景列表
		{
			ListSceneResponse listSceneResponse;
			retBool = listSceneResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				int32_t tempRandValue = (int32_t)random();
				retError = listSceneResponse.error_code();
				if(retError == 0)
				{
					TypeSceneNameInfo *sceneNameInfo = NULL;
					for(int namei = 0; namei < listSceneResponse.scenes_size(); namei++)
					{
						sceneNameInfo = pDeviceList->onAddSceneInfo(new TypeSceneNameInfo(listSceneResponse.scenes(namei).scene_id(), (char *) listSceneResponse.scenes(namei).name().c_str(), listSceneResponse.scenes(namei).room_id(), listSceneResponse.scenes(namei).icon_id(), listSceneResponse.scenes(namei).specialized(), listSceneResponse.scenes(namei).disabled(), listSceneResponse.scenes(namei).hidden(), listSceneResponse.scenes(namei).status(), (char *) listSceneResponse.scenes(namei).period().c_str(), (char *) listSceneResponse.scenes(namei).enabled_time().c_str()), tempRandValue);
						for(int i = 0; i < listSceneResponse.scenes(namei).actions_size(); ++i)
						{
							pDeviceList->onAddSceneActionInfo(sceneNameInfo, new TypeSceneActionInfo(listSceneResponse.scenes(namei).actions(i).scene_action_id(), listSceneResponse.scenes(namei).actions(i).scene_id(), listSceneResponse.scenes(namei).actions(i).type(), listSceneResponse.scenes(namei).actions(i).device_id(), listSceneResponse.scenes(namei).actions(i).sub_id(), listSceneResponse.scenes(namei).actions(i).action_type(), listSceneResponse.scenes(namei).actions(i).action(), (char *) listSceneResponse.scenes(namei).actions(i).action_desc().c_str(), listSceneResponse.scenes(namei).actions(i).delayed_time()), tempRandValue);
						}
						//删除已经被删除的指令
						TypeSceneActionInfo *tempSceneActionInfo = NULL;
						for(int i = 0; i < sceneNameInfo->onActionInfoList->size(); )
						{
							tempSceneActionInfo = (TypeSceneActionInfo *)sceneNameInfo->onActionInfoList->get(i);
							if(tempSceneActionInfo && (tempSceneActionInfo->randValue != tempRandValue))
							{
								//删除
								pDataBase->onDeleteDataBase("sceneactioninfo", "actionid", tempSceneActionInfo->scene_action_id);
								sceneNameInfo->onActionInfoList->removeObject(tempSceneActionInfo);
								continue;
							}
							i++;
						}
						for(int i = 0; i < sceneNameInfo->offActionInfoList->size(); )
						{
							tempSceneActionInfo = (TypeSceneActionInfo *)sceneNameInfo->offActionInfoList->get(i);
							if(tempSceneActionInfo && (tempSceneActionInfo->randValue != tempRandValue))
							{
								//删除
								pDataBase->onDeleteDataBase("sceneactioninfo", "actionid", tempSceneActionInfo->scene_action_id);
								sceneNameInfo->offActionInfoList->removeObject(tempSceneActionInfo);
								continue;
							}
							i++;
						}
						for(int i = 0; i < listSceneResponse.scenes(namei).conds_size(); ++i)
						{
							pDeviceList->onAddSceneCondInfo(sceneNameInfo, new TypeSceneCondInfo(listSceneResponse.scenes(namei).conds(i).scene_cond_id(), listSceneResponse.scenes(namei).conds(i).scene_id(), listSceneResponse.scenes(namei).conds(i).type(), listSceneResponse.scenes(namei).conds(i).cond_type(), (char *) listSceneResponse.scenes(namei).conds(i).cond_expr().c_str(), listSceneResponse.scenes(namei).conds(i).device_id(), listSceneResponse.scenes(namei).conds(i).sub_id(), listSceneResponse.scenes(namei).conds(i).action(), (char *) listSceneResponse.scenes(namei).conds(i).action_desc().c_str(), listSceneResponse.scenes(namei).conds(i).delayed_time()), tempRandValue);
						}
						//删除已经被删除的条件
						TypeSceneCondInfo *tempSceneCondInfo = NULL;
						for(int i = 0; i < sceneNameInfo->onCondInfoList->size(); )
						{
							tempSceneCondInfo = (TypeSceneCondInfo *)sceneNameInfo->onCondInfoList->get(i);
							if(tempSceneCondInfo && (tempSceneCondInfo->randValue != tempRandValue))
							{
								//删除
								pDataBase->onDeleteDataBase("scenecondinfo", "condid", tempSceneCondInfo->scene_cond_id);
								sceneNameInfo->onCondInfoList->removeObject(tempSceneCondInfo);
								continue;
							}
							i++;
						}
						for(int i = 0; i < sceneNameInfo->offCondInfoList->size(); )
						{
							tempSceneCondInfo = (TypeSceneCondInfo *)sceneNameInfo->offCondInfoList->get(i);
							if(tempSceneCondInfo && (tempSceneCondInfo->randValue != tempRandValue))
							{
								//删除
								pDataBase->onDeleteDataBase("scenecondinfo", "condid", tempSceneCondInfo->scene_cond_id);
								sceneNameInfo->offCondInfoList->removeObject(tempSceneCondInfo);
								continue;
							}
							i++;
						}
					}
					//删除已经被删除的场景
					TypeSceneNameInfo *tempSceneNameInfo = NULL;
					for(int i = 0; i < pDeviceList->sceneList->size(); )
					{
						tempSceneNameInfo = (TypeSceneNameInfo *)pDeviceList->sceneList->get(i);
						if(tempSceneNameInfo && (tempSceneNameInfo->randValue != tempRandValue))
						{
							//删除这个场景
							pDeviceList->onDeleteSceneInfo(tempSceneNameInfo);
							continue;
						}
						i++;
					}
				}
			}
		}
			break;
		case CMD_ID_SCENE_DETAIL_GET_RES://得到场景详情返回
		{
			GetSceneDetailResponse sceneDetailResponse;
			retBool = sceneDetailResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = sceneDetailResponse.error_code();
				if(retError == 0)
				{
					TypeSceneNameInfo *sceneNameInfo = pDeviceList->onAddSceneInfo(new TypeSceneNameInfo(sceneDetailResponse.scene().scene_id(), (char *) sceneDetailResponse.scene().name().c_str(), sceneDetailResponse.scene().room_id(), sceneDetailResponse.scene().icon_id(), sceneDetailResponse.scene().specialized(), sceneDetailResponse.scene().disabled(), sceneDetailResponse.scene().hidden(), sceneDetailResponse.scene().status(), (char *) sceneDetailResponse.scene().period().c_str(), (char *) sceneDetailResponse.scene().enabled_time().c_str()), random());
					//创建一个随机数
					int32_t tempRandValue = (int32_t)random();
					for(int i = 0; i < sceneDetailResponse.scene_actions_size(); ++i)
					{
						pDeviceList->onAddSceneActionInfo(sceneNameInfo, new TypeSceneActionInfo(sceneDetailResponse.scene_actions(i).scene_action_id(), sceneDetailResponse.scene_actions(i).scene_id(), sceneDetailResponse.scene_actions(i).type(), sceneDetailResponse.scene_actions(i).device_id(), sceneDetailResponse.scene_actions(i).sub_id(), sceneDetailResponse.scene_actions(i).action_type(), sceneDetailResponse.scene_actions(i).action(), (char *) sceneDetailResponse.scene_actions(i).action_desc().c_str(), sceneDetailResponse.scene_actions(i).delayed_time()), tempRandValue);
					}
					//删除已经被删除的指令
					TypeSceneActionInfo *tempSceneActionInfo = NULL;
					for(int i = 0; i < sceneNameInfo->onActionInfoList->size(); )
					{
						tempSceneActionInfo = (TypeSceneActionInfo *)sceneNameInfo->onActionInfoList->get(i);
						if(tempSceneActionInfo && (tempSceneActionInfo->randValue != tempRandValue))
						{
							//删除
							pDataBase->onDeleteDataBase("sceneactioninfo", "actionid", tempSceneActionInfo->scene_action_id);
							sceneNameInfo->onActionInfoList->removeObject(tempSceneActionInfo);
							continue;
						}
						i++;
					}
					for(int i = 0; i < sceneNameInfo->offActionInfoList->size(); )
					{
						tempSceneActionInfo = (TypeSceneActionInfo *)sceneNameInfo->offActionInfoList->get(i);
						if(tempSceneActionInfo && (tempSceneActionInfo->randValue != tempRandValue))
						{
							//删除
							pDataBase->onDeleteDataBase("sceneactioninfo", "actionid", tempSceneActionInfo->scene_action_id);
							sceneNameInfo->offActionInfoList->removeObject(tempSceneActionInfo);
							continue;
						}
						i++;
					}
					for(int i = 0; i < sceneDetailResponse.scene_conds_size(); ++i)
					{
						pDeviceList->onAddSceneCondInfo(sceneNameInfo, new TypeSceneCondInfo(sceneDetailResponse.scene_conds(i).scene_cond_id(), sceneDetailResponse.scene().scene_id(), sceneDetailResponse.scene_conds(i).type(), sceneDetailResponse.scene_conds(i).cond_type(), (char *) sceneDetailResponse.scene_conds(i).cond_expr().c_str(), sceneDetailResponse.scene_conds(i).device_id(), sceneDetailResponse.scene_conds(i).sub_id(), sceneDetailResponse.scene_conds(i).action(), (char *) sceneDetailResponse.scene_conds(i).action_desc().c_str(), sceneDetailResponse.scene_conds(i).delayed_time()), tempRandValue);
					}
					//删除已经被删除的条件
					TypeSceneCondInfo *tempSceneCondInfo = NULL;
					for(int i = 0; i < sceneNameInfo->onCondInfoList->size(); )
					{
						tempSceneCondInfo = (TypeSceneCondInfo *)sceneNameInfo->onCondInfoList->get(i);
						if(tempSceneCondInfo && (tempSceneCondInfo->randValue != tempRandValue))
						{
							//删除
							pDataBase->onDeleteDataBase("scenecondinfo", "condid", tempSceneCondInfo->scene_cond_id);
							sceneNameInfo->onCondInfoList->removeObject(tempSceneCondInfo);
							continue;
						}
						i++;
					}
					for(int i = 0; i < sceneNameInfo->offCondInfoList->size(); )
					{
						tempSceneCondInfo = (TypeSceneCondInfo *)sceneNameInfo->offCondInfoList->get(i);
						if(tempSceneCondInfo && (tempSceneCondInfo->randValue != tempRandValue))
						{
							//删除
							pDataBase->onDeleteDataBase("scenecondinfo", "condid", tempSceneCondInfo->scene_cond_id);
							sceneNameInfo->offCondInfoList->removeObject(tempSceneCondInfo);
							continue;
						}
						i++;
					}
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_INFO_CHNAGED_NOTIFY://场景状态变化通知
		{
			SceneInfoChangedNotification sceneInfoChangedNotification;
			retBool = sceneInfoChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				int index = 0;
				int tempMask = sceneInfoChangedNotification.mask();
				TypeSceneNameInfo *sceneNameInfo = pDeviceList->onFindSceneInfo(sceneInfoChangedNotification.scene().scene_id());
				if(sceneNameInfo != NULL)
				{
					while((index < 32) && (tempMask > 0))
					{
						switch(tempMask & (1 << index))
						{
							case SCENE_INFO_MASK_NAME:
								if(!sceneNameInfo->name->onStringCMP(sceneInfoChangedNotification.scene().name().c_str()))
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneName, sceneInfoChangedNotification.scene().name().c_str());
									//在灯光列表里面找一下是否有关联这个场景
									pDeviceList->onCheckLightToScene(sceneNameInfo->scene_id);
									//名称有更新，通知能上面。
									if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_SCENENAME)
									{
										onNotifyToJava(JNI_NOTIFY_UPDATE_SCENENAME, sceneNameInfo->scene_id, sceneNameInfo->room_id, sceneNameInfo->icon_id, sceneNameInfo->name->buff);
									}
								}
								break;
							case SCENE_INFO_MASK_ROOM_ID:
								if(sceneNameInfo->room_id != sceneInfoChangedNotification.scene().room_id())
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneRoomID, sceneInfoChangedNotification.scene().room_id());
								}
								break;
							case SCENE_INFO_MASK_ICON_ID:
								if(sceneNameInfo->icon_id != sceneInfoChangedNotification.scene().icon_id())
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneIconID, sceneInfoChangedNotification.scene().icon_id());
									//在灯光列表里面找一下是否有关联这个场景
									pDeviceList->onCheckLightToScene(sceneNameInfo->scene_id);
								}
								break;
							case SCENE_INFO_MASK_SPECIALIZED:
								if(sceneNameInfo->specialized != sceneInfoChangedNotification.scene().specialized())
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneSpecialized, sceneInfoChangedNotification.scene().specialized());
								}
								break;
							case SCENE_INFO_MASK_DISABLED:
								if(sceneNameInfo->disabled != sceneInfoChangedNotification.scene().disabled())
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneDisabled, sceneInfoChangedNotification.scene().disabled());
								}
								break;
							case SCENE_INFO_MASK_STATUS:
                            {
                            	//
                                mPrintf(Log_NetWork, "%s status=%d->%d ", sceneNameInfo->name->buff, sceneNameInfo->status, sceneInfoChangedNotification.scene().status());
	                            if(pDeviceList->onSetSceneStatus(sceneNameInfo, sceneInfoChangedNotification.scene().status(), FALSE))
	                            {
		                            if(mNotifyRegisterFlag & JNI_NOTIFY_UPDATE_SCENESTATUS)
		                            {
			                            onNotifyToJava(JNI_NOTIFY_UPDATE_SCENESTATUS, sceneNameInfo->scene_id, sceneNameInfo->room_id, sceneNameInfo->icon_id, sceneInfoChangedNotification.scene().status());
		                            }
	                            }
                            }
								break;
							case SCENE_INFO_MASK_PERIOD:
								if(!sceneNameInfo->period->onStringCMP(sceneInfoChangedNotification.scene().period().c_str()))
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, ScenePeriod, sceneInfoChangedNotification.scene().period().c_str());
									sceneNameInfo->onSetPeriod((char *)sceneInfoChangedNotification.scene().period().c_str());
								}
								break;
							case SCENE_INFO_MASK_ENABLED_TIME:
								if(!sceneNameInfo->enabledTime->onStringCMP(sceneInfoChangedNotification.scene().enabled_time().c_str()))
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneEnableTime, sceneInfoChangedNotification.scene().enabled_time().c_str());
									sceneNameInfo->onSetEnableTime((char *)sceneInfoChangedNotification.scene().enabled_time().c_str());
								}
								break;
							case SCENE_INFO_MASK_HIDDEN:
								if((sceneNameInfo->hidden & 0x0F) != sceneInfoChangedNotification.scene().hidden())
								{
									pDataBase->onUpdateSceneNameInfo(sceneNameInfo, SceneHidden, (sceneNameInfo->hidden & 0xF0) | (sceneInfoChangedNotification.scene().hidden() & 0x0F));
								}
								break;
							case 0://无效
								break;
							default:
								mPrintf(Log_NetWork, "found unkonw type（%d） when update scene info! ", 1 << index);
								break;
						}
						tempMask &= ~(1 << index);
						index++;
					}
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_RES://燃气臂绑定表返回
		{
			ListGasArmBindingResponse gasArmBindingResponse;
			retBool = gasArmBindingResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = gasArmBindingResponse.error_code();
				if(retError == 0)
				{
					if(gasArmBindingResponse.flag() == 0)
					{
						//得到一个随机数
						int32_t randValue = (int32_t)random();
						for(int i = 0; i < gasArmBindingResponse.bindings_size(); ++ i)
						{
							pDataBase->onAddGasArmBingInfo(gasArmBindingResponse.bindings(i).gas_id(), gasArmBindingResponse.bindings(i).arm_id(), randValue);
						}
						//清理一下
						pDataBase->onDeleteGasArmBingInfo(randValue);
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_GAS_ARM_BINDING_CHANGED_NOTIFY://燃气臂绑定表有更新
		{
			GasArmBindingChangedNotification armBindingChangedNotification;
			retBool = armBindingChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				if(armBindingChangedNotification.opcmd() == 0)
				{
					//修改
					//这里获取一下燃气与燃气臂的绑定关系
					ListGasArmBindingRequest gasArmBindingRequest;
					gasArmBindingRequest.set_flag(0);
					mfTCPCMDSend(CMD_ID_DEVICE_GAS_ARM_BINDING_LIST_REQ, gasArmBindingRequest.SerializeAsString().c_str(), gasArmBindingRequest.SerializeAsString().length());
				}
				else if(armBindingChangedNotification.opcmd() == 1)
				{
					pDataBase->onAddGasArmBingInfo(armBindingChangedNotification.gas_id(), armBindingChangedNotification.arm_id(), 0);
					//添加
				}
				else if(armBindingChangedNotification.opcmd() == 2)
				{
					pDataBase->onDeleteGasArmBingInfo(armBindingChangedNotification.gas_id(), armBindingChangedNotification.arm_id());
					//删除
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_ACTION_INFO_CHANGED_NOTIFY://场景动作信息变化通知
		{
			SceneActionInfoChangedNotification sceneActionInfoChangedNotification;
			retBool = sceneActionInfoChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接重新获取一下这个场景的所有信息算了
				onGetSceneActionInfo(sceneActionInfoChangedNotification.scene_action().scene_id());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_COND_INFO_CHANGED_NOTIFY://修改场景条件通知
		{
			SceneCondInfoChangedNotification sceneCondInfoChangedNotification;
			retBool = sceneCondInfoChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接重新获取一下这个场景的所有信息算了
				onGetSceneActionInfo(sceneCondInfoChangedNotification.scene_cond().scene_id());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_MOD_RES://修改场景信息返回
			break;
		case CMD_ID_SCENE_ADD_RES://添加场景返回
			break;
		case CMD_ID_SCENE_ADDED_NOTIFY://添加场景通知
		{
			SceneAddedNotification sceneAddedNotification;
			retBool = sceneAddedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//通过场景ID请求获取场景详情
				onGetSceneActionInfo(sceneAddedNotification.scene_id());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_DELETED_NOTIFY://删除场景通知
		{
			SceneDeletedNotification sceneDeletedNotification;
			retBool = sceneDeletedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接删除场景
				pDeviceList->onDeleteSceneInfo(pDeviceList->onFindSceneInfo(sceneDeletedNotification.scene_id()));
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_ACTION_DELETED_NOTIFY://删除场景动作通知
		{
			SceneActionDeletedNotification sceneActionDeletedNotification;
			retBool = sceneActionDeletedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接删除场景动作
				TypeSceneNameInfo *sceneNameInfo = pDeviceList->onFindSceneInfo(sceneActionDeletedNotification.scene_id());
				if(sceneNameInfo != NULL)
				{
					sceneNameInfo->onDeleteActionInfo(sceneActionDeletedNotification.scene_action_id());
					//同时删除数据库
					pDataBase->onDeleteDataBase("sceneactioninfo", "actionid", sceneActionDeletedNotification.scene_action_id());
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_COND_DELETED_NOTIFY://删除场景条件通知
		{
			SceneCondDeletedNotification sceneCondDeletedNotification;
			retBool = sceneCondDeletedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//直接删除场景条件
				TypeSceneNameInfo *sceneNameInfo = pDeviceList->onFindSceneInfo(sceneCondDeletedNotification.scene_id());
				if(sceneNameInfo != NULL)
				{
					sceneNameInfo->onDeleteCondInfo(sceneCondDeletedNotification.scene_cond_id());
					//同时删除数据库
					pDataBase->onDeleteDataBase("scenecondinfo", "condid", sceneCondDeletedNotification.scene_cond_id());
				}
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_ACTION_ADDED_NOTIFY://添加场景动作通知
		{
			SceneActionAddedNotification sceneActionAddedNotification;
			retBool = sceneActionAddedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//需要重新更新这个场景的所有信息
				onGetSceneActionInfo(sceneActionAddedNotification.scene_id());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_ICON_FONT_BITMAP_GET_RES:
		{
			IconFontBitmapGetResponse bitmapRsp;
			retBool = bitmapRsp.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = bitmapRsp.error_code();
				if(retError == 0)
				{
					//发送给设备
					TypeDBDeviceInfo *dbDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, bitmapRsp.device_id());
					if(dbDeviceInfo)//只要是设备 都可以请求图标数据
					{
						if((bitmapRsp.sub_id() > 0) && (bitmapRsp.sub_id() < 5))
						{
							TypeChar *sendIconBuff = new TypeChar();
							sendIconBuff->ubuff[0] = (uint8_t)bitmapRsp.code();
							memcpy(&sendIconBuff->ubuff[1], bitmapRsp.bitmap().c_str(), bitmapRsp.bitmap().length());
							pmMasterSerialPort->onWriteAttribute((uint32_t)dbDeviceInfo->shortAddr, (uint8_t)bitmapRsp.sub_id(), 0x0000, new TypeZclAttribute(0x4005, ZCL_DATATYPE_CHAR_STR, sendIconBuff->ubuff, (uint8_t)(bitmapRsp.bitmap().length() + 1)), 0);
							delete sendIconBuff;
						}
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_SCENE_COND_ADDED_NOTIFY://添加场景条件通知
		{
			SceneCondAddedNotification sceneCondAddedNotification;
			retBool = sceneCondAddedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//需要重新更新这个场景的所有信息
				onGetSceneActionInfo(sceneCondAddedNotification.scene_id());
			}else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_PUSH_MSG_PRIVATE_REQ://推送测试命令
		case CMD_ID_PUSH_MSG_PRIVATE_RES:
		{
			//
		}
			break;
		case CMD_ID_FAMILY_ACTION_MSG_NOTIFY://当前所在家庭信息变更
		{
			FamilyActionMsgNotification familyActionMsgNotification;
			retBool = familyActionMsgNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//
				if(familyActionMsgNotification.family_id() == pDataBase->onGetFamilyID())
				{
					if(familyActionMsgNotification.msg_type() == FAMILY_ACTION_MSG_TYPE_FAMILY_DELETE)
					{
						//当前家庭被删除
						//先获取当前网关的信息
						GetGatewayInfoRequest getGatewayInfoRequest;
						getGatewayInfoRequest.set_gateway_id(pDataBase->onGetGateway_ID());
						mfTCPCMDSend(CMD_ID_GATEWAY_GET_INFO_REQ, getGatewayInfoRequest.SerializeAsString().c_str(), getGatewayInfoRequest.SerializeAsString().length());
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_EVENT_BROADCAST_RES://添加一个设备事件返回
		{

		}
			break;
		case CMD_ID_DEVICE_EVENT_BROADCAST_NOTIFY://设备事件通知
		{
			DeviceEventBroadcastNotify deviceEventBroadcastNotify;
			retBool = deviceEventBroadcastNotify.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//
				if(deviceEventBroadcastNotify.family_id() == pDataBase->onGetFamilyID())
				{
                    //先查找有没有这个设备
                    TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, deviceEventBroadcastNotify.device_id());
                    if(tempDBDeviceInfo && tempDBDeviceInfo->attr->bits.screen)
                    {
                        switch(deviceEventBroadcastNotify.event())
                        {
                            case DEV_EVENT_SCREEN_SHARE:
                            {
                                pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, (uint8_t)deviceEventBroadcastNotify.sub_id(), 0x0000, new TypeZclAttribute(0x4006, ZCL_DATATYPE_CHAR_STR, (uint8_t *)deviceEventBroadcastNotify.value().c_str(), (uint8_t)deviceEventBroadcastNotify.value().length()), 0);
                            }
                            break;
	                        default:break;
                        }
                    }
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
			//以下是家电处理相关命令
		case CMD_ID_DEVICE_APPLIANCE_CMD_LIST_RES://家电指令详情列表
		{
			ListApplianceCmdResponse applianceCmdResponse;
			retBool = applianceCmdResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//
				retError = applianceCmdResponse.error_code();
				if(retError == 0)
				{
					TypeApplianceInfo *tempApplianceInfo = NULL;
					int32_t tempRandValue = (int32_t)random();
					int32_t lastApplianceID = 0;
					for(int i = 0; i < applianceCmdResponse.cmds_size(); ++ i)
					{
						if(lastApplianceID != applianceCmdResponse.cmds(i).appliance_id())
						{
							lastApplianceID = applianceCmdResponse.cmds(i).appliance_id();
							tempApplianceInfo = pDeviceList->onFindApplianceInfo(lastApplianceID);
						}
						if(tempApplianceInfo)
						{
							pDeviceList->onAddAppliancesCodeInfo(tempApplianceInfo, new TypeApplianceCodeInfo(applianceCmdResponse.cmds(i).appliance_id(), applianceCmdResponse.cmds(i).key_id(), applianceCmdResponse.cmds(i).ir_code(), applianceCmdResponse.cmds(i).status()), tempRandValue);
						}
					}
					//然后删掉已经删掉的指令
					TypeApplianceCodeInfo *tempApplianceCodeInfo = NULL;
					if(applianceCmdResponse.device_id() == 0)
					{
						if(applianceCmdResponse.appliance_id())
						{
							//这个家电的
							tempApplianceInfo = pDeviceList->onFindApplianceInfo(applianceCmdResponse.appliance_id());
							if(tempApplianceInfo)
							{
								for(int i = 0; i < tempApplianceInfo->codeList->size(); )
								{
									tempApplianceCodeInfo = (TypeApplianceCodeInfo *)tempApplianceInfo->codeList->get(i);
									if(tempApplianceCodeInfo && (tempApplianceCodeInfo->randValue != tempRandValue))
									{
										pDataBase->onDeleteApplianceCode(tempApplianceCodeInfo->appID, tempApplianceCodeInfo->key_id);
										tempApplianceInfo->codeList->removeObject(tempApplianceCodeInfo);
										continue;
									}
									i++;
								}
							}
						}
						else
						{
							//服务器返回的意思是找不到这个家电
							for(int j = 0; j < pDeviceList->applianceList->size(); ++ j)
							{
								tempApplianceInfo = (TypeApplianceInfo *)pDeviceList->applianceList->get(j);
								if(tempApplianceInfo)
								{
									for(int i = 0; i < tempApplianceInfo->codeList->size(); )
									{
										tempApplianceCodeInfo = (TypeApplianceCodeInfo *)tempApplianceInfo->codeList->get(i);
										if(tempApplianceCodeInfo && (tempApplianceCodeInfo->randValue != tempRandValue))
										{
											pDataBase->onDeleteApplianceCode(tempApplianceCodeInfo->appID, tempApplianceCodeInfo->key_id);
											tempApplianceInfo->codeList->removeObject(tempApplianceCodeInfo);
											continue;
										}
										i++;
									}
								}
							}
						}
					}
					else
					{
						//暂时没有使用通过红外伴侣获取指令的情况
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_CMD_DELETED_NOTIFY://删除家电指令通知
		{
			ApplianceCmdDeletedNotification applianceCmdDeletedNotification;
			retBool = applianceCmdDeletedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//查找到这个家电指令直接删除
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(applianceCmdDeletedNotification.appliance_id());
				if(tempApplianceInfo)
				{
					pDeviceList->onDeleteApplianceCodeInfo(applianceCmdDeletedNotification.appliance_id(), applianceCmdDeletedNotification.id());
				}
				else
				{
					//更新这个家电的指令
					ListApplianceCmdRequest applianceCmdRequest;
					applianceCmdRequest.set_appliance_id(applianceCmdDeletedNotification.appliance_id());
					mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CMD_LIST_REQ, applianceCmdRequest.SerializeAsString().c_str(), applianceCmdRequest.SerializeAsString().length());
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_REQ://家电指令学习
		{
			LearnApplianceCmdRequest learnApplianceCmdRequest;
			retBool = learnApplianceCmdRequest.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//先添加这条命令 然后再发送给红外伴侣
				LearnApplianceCmdResponse learnApplianceCmdResponse;
				learnApplianceCmdResponse.set_appliance_id(learnApplianceCmdRequest.appliance_id());
				learnApplianceCmdResponse.set_ir_code(learnApplianceCmdRequest.ir_code());
				learnApplianceCmdResponse.set_key_id(learnApplianceCmdRequest.key_id());
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(learnApplianceCmdRequest.appliance_id());
				if(tempApplianceInfo)
				{
					pDeviceList->onAddAppliancesCodeInfo(tempApplianceInfo, new TypeApplianceCodeInfo(learnApplianceCmdRequest.appliance_id(), learnApplianceCmdRequest.key_id(), learnApplianceCmdRequest.ir_code(), 1), random());
					//查找这个红外伴侣
					TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, tempApplianceInfo->ir_id);
					if(tempDBDeviceInfo)
					{
						if(tempDBDeviceInfo->gatewayID != pDataBase->onGetGateway_ID())
						{
							break;//不是这个网关的就退出 也不返回
						}
						if(tempDBDeviceInfo->onLineFlag.bits.status && tempDBDeviceInfo->shortAddr)
						{
							TypeChar *irCodeSend = new TypeChar(8);
							uint64_t tempUInt64 = (uint64_t)learnApplianceCmdRequest.appliance_id();
							tempUInt64 <<= 32;
							tempUInt64 |= learnApplianceCmdRequest.ir_code();
							irCodeSend->onAddInt64Ex(0, tempUInt64);
							pmMasterSerialPort->onWriteAttribute((uint32_t)tempDBDeviceInfo->shortAddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x0008, ZCL_DATATYPE_UINT64, irCodeSend->ubuff, 8), 0);
							delete irCodeSend;
							//发送成功
						}
						else
						{
							//红外伴侣不在线(或者没找到网 没找到网肯定不会在线)
							learnApplianceCmdResponse.set_error_code(20019);
						}
					}
					else
					{
						//红外伴侣不存在
						learnApplianceCmdResponse.set_error_code(20402);
					}
				}
				else
				{
					//家电不存在
					learnApplianceCmdResponse.set_error_code(20404);
				}

				//返回
				mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_RES, learnApplianceCmdResponse.SerializeAsString().c_str(), learnApplianceCmdResponse.SerializeAsString().length());
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_UPDATE_RES://更新红外学习状态返回  基本上是成功的
			break;
		case CMD_ID_DEVICE_APPLIANCE_CMD_LEARN_STATUS_CHANGED_NOTIFY://更新家电指令状态通知
		{
			ApplianceCmdStatusChangedNotification applianceCmdStatusChangedNotification;
			retBool = applianceCmdStatusChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//先查找这个家电  再更新这要指令的状态
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(applianceCmdStatusChangedNotification.appliance_id());
				if(tempApplianceInfo)
				{
					pDeviceList->onAddAppliancesCodeInfo(tempApplianceInfo, new TypeApplianceCodeInfo(applianceCmdStatusChangedNotification.appliance_id(), applianceCmdStatusChangedNotification.key_id(), applianceCmdStatusChangedNotification.ir_code(), applianceCmdStatusChangedNotification.status()), random());
				}
				else
				{
					//更新这个家电的指令
					ListApplianceCmdRequest applianceCmdRequest;
					applianceCmdRequest.set_appliance_id(applianceCmdStatusChangedNotification.appliance_id());
					mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CMD_LIST_REQ, applianceCmdRequest.SerializeAsString().c_str(), applianceCmdRequest.SerializeAsString().length());
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_INFO_CHANGED_NOTIFY://修改家电信息通知
		{
			//家电名称对我说讲好像没多少用。我暂时不修改
			ApplianceInfoChangedNotification applianceInfoChangedNotification;
			retBool = applianceInfoChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(applianceInfoChangedNotification.id());
				if(tempApplianceInfo)
				{
					//删除这个家电
					if(applianceInfoChangedNotification.attr_mask() == APPLIANCE_ATTR_MASK_IR_DEVICE_ID)
					{
						//修改红外伴侣绑定
						pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceIrID, applianceInfoChangedNotification.device_id());
						pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceIrSubID, applianceInfoChangedNotification.sub_id());
					}
					else if(applianceInfoChangedNotification.attr_mask() == APPLIANCE_ATTR_MASK_NAME)
					{
						pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceName, applianceInfoChangedNotification.name().c_str());
					}
					else if(applianceInfoChangedNotification.attr_mask() == APPLIANCE_ATTR_MASK_ROOM_ID)
					{
						pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceRoomID, applianceInfoChangedNotification.room_id());
					}
					else if(applianceInfoChangedNotification.attr_mask() == APPLIANCE_ATTR_MASK_ADDR)
					{
						pDataBase->onUpdateApplianceInfo(tempApplianceInfo, ApplianceAddr, applianceInfoChangedNotification.addr());
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_MODIFY_REQ:
		{

		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_DELETED_NOTIFY://删除家电信息通知
		{
			ApplianceDeletedNotification deletedNotification;
			retBool = deletedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//删除这个家电
				pDeviceList->onDeleteApplianceInfo(pDeviceList->onFindApplianceInfo(deletedNotification.id()));
			}
			else
			{
				retError = 1;
			}
		}
		break;
		case CMD_ID_DEVICE_APPLIANCE_ADDED_NOTIFY://添加家电通知
		{
			ApplianceAddedNotification addedNotification;
			retBool = addedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				//先查找这个家电 存在就删除
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(addedNotification.id());
				if(tempApplianceInfo)
				{
					//删除这个家电
					pDataBase->onDeleteDataBase("applianceinfoex", "appid", addedNotification.id());
					pDeviceList->applianceList->removeObject(tempApplianceInfo);
				}
				//然后添加一个新的家电
				pDeviceList->onAddAppliancesInfo(new TypeApplianceInfo(addedNotification.id(), addedNotification.device_id(), addedNotification.sub_id(), (char *)addedNotification.name().c_str(), (char *)addedNotification.manufacturer().c_str(), (char *)addedNotification.model().c_str(), (char *)addedNotification.version().c_str(), (char *)addedNotification.serial().c_str(), addedNotification.room_id(), addedNotification.type(), addedNotification.value(), addedNotification.addr(),(char *)addedNotification.value1().c_str(),(char *)addedNotification.config().c_str()), 1);
				//获取一下这个家电的所有指令信息
				ListApplianceCmdRequest applianceCmdRequest;
				applianceCmdRequest.set_appliance_id(addedNotification.id());
				mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CMD_LIST_REQ, applianceCmdRequest.SerializeAsString().c_str(), applianceCmdRequest.SerializeAsString().length());
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_CTRL_REQ://家电控制
		{
			CtrlApplianceRequest applianceRequest;
			retBool = applianceRequest.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(applianceRequest.appliance_id());
				if(tempApplianceInfo)
				{
					TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempApplianceInfo->ir_id);
					if(tempDBDeviceInfo)
					{
						retError = pDeviceList->onSetApplianceStatus(applianceRequest.appliance_id(), applianceRequest.key_id(), applianceRequest.key_data().c_str());
						CtrlApplianceResponse applianceResponse;
						applianceResponse.set_appliance_id(applianceRequest.appliance_id());
						applianceResponse.set_error_code(retError);
						applianceResponse.set_key_id(applianceRequest.key_id());
						applianceResponse.set_user_id(applianceRequest.user_id());
						mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_CTRL_RES, applianceResponse.SerializeAsString().c_str(), applianceResponse.SerializeAsString().length());
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_RES://家电改变
		case CMD_ID_DEVICE_APPLIANCE_CTRL_RES:
			break;
		case CMD_ID_DEVICE_APPLIANCE_VALUE_CHANGED_NOTIFY://家电状态改变通知
		{
			//就是因为这里没有及时更新家电状态
			ApplianceValueChangedNotification applianceValueChangedNotification;
			retBool = applianceValueChangedNotification.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(applianceValueChangedNotification.appliance_id());
				if(tempApplianceInfo)
				{
					TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IDeviceID, tempApplianceInfo->ir_id);
					if(tempDBDeviceInfo == NULL)
					{
						//暂时只让不是这个网关家电更新状态
						tempApplianceInfo->value = applianceValueChangedNotification.value();
					}
				}
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_DEVICE_APPLIANCE_MODIFY_RES:
			break;
		case CMD_ID_OTA_UPGRADE_REQ://升级请求
		{
			OTAUpgradeRequest otaUpgradeRequest;
			retBool = otaUpgradeRequest.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				TypeChar *fileName = new TypeChar(otaUpgradeRequest.path().c_str());
				int32_t retResonseValue = 1;
#ifdef WINOBLE_LINUX //检查网关有没有更新  只有不带屏的需要用这种方式更新
				TypeChar *categoryStr = new TypeChar(otaUpgradeRequest.category().c_str());
				if(categoryStr->onStringCMP("gateway") && (otaUpgradeRequest.target_id() == pDataBase->onGetGateway_ID()) &&
				(((mGatewayType == 2) && fileName->onStringCMP("gateway")) || ((mGatewayType == 3) && fileName->onStringCMP("gatewayNew"))))
				{
					if(onFindThreadTitle((char *)"APP DL") == FALSE)
					{
						//当前没有APP升级线程在运行
						TypeChar *tempChars = new TypeChar(otaUpgradeRequest.version().c_str());
						if(tempChars->onStringCMP(GATEWAY_SOFTVER) == FALSE)
						{
							//说明有升级
							//说明当前版本不一样，需要检查升级  也创建一个线程吧 这样就不会卡顿
							mPrintf(LOG_Robot, "Linux 网关有新版本!%s->%s ", GATEWAY_SOFTVER, tempChars->buff);
							//创建线程
							//把MD5和文件名称  传送到线程里面去
							TypeChar *tempBuff = new TypeChar();
							cJSON *sendJason = cJSON_CreateObject();
							cJSON_AddStringToObject(sendJason, "filename", otaUpgradeRequest.path().c_str());

							mPrintf(LOG_Robot, "网关信息：\nFilename:%s ", otaUpgradeRequest.path().c_str());

							sprintf(tempBuff->buff, "/dists/gateway/%s", otaUpgradeRequest.path().c_str());
							mPrintf(LOG_Robot, "filepath:%s ", tempBuff->buff);
							cJSON_AddStringToObject(sendJason, "filepath", tempBuff->buff);
							sprintf(tempBuff->buff, "/tmp/%s", otaUpgradeRequest.path().c_str());
							mPrintf(LOG_Robot, "savepath:%s ", tempBuff->buff);
							mPrintf(LOG_Robot, "filemd5:%s ", otaUpgradeRequest.md5sum().c_str());
							mPrintf(LOG_Robot, "filever:%s ", tempChars->buff);

							cJSON_AddStringToObject(sendJason, "savepath", tempBuff->buff);
							cJSON_AddStringToObject(sendJason, "filemd5", otaUpgradeRequest.md5sum().c_str());
							cJSON_AddStringToObject(sendJason, "filever", tempChars->buff);
							char * tempChars = cJSON_Print(sendJason);
							//启动下载
							onAddThread("APP DL", mfAppDownLoadThread, tempChars);
							retResonseValue = 0;
							cJSON_Delete(sendJason);
							free(tempChars);
							delete tempBuff;
						}
						else
						{
							onUpdateDLStatus(OTA_UPGRADE_STAGE_FAILED, 0, "版本已经是最新的了");
							mPrintf(LOG_Robot, "Linux 网关已经是最新版本!%s ", GATEWAY_SOFTVER);
						}
						delete tempChars;
					}
					else
					{
						onUpdateDLStatus(OTA_UPGRADE_STAGE_FAILED, 0, "当前正在升级中!");
					}
				}
				else
				{
					onUpdateDLStatus(OTA_UPGRADE_STAGE_FAILED, 0, "网关ID不正确!");
				}
				delete categoryStr;
#endif
				delete fileName;
				OTAVersionCheckResponse otaVersionCheckResponse;
				otaVersionCheckResponse.set_error_code(retResonseValue);
				mfTCPCMDSend(CMD_ID_OTA_UPGRADE_RES, otaVersionCheckResponse.SerializeAsString().c_str(), otaVersionCheckResponse.SerializeAsString().length());
				//应答
			}
			else
			{
				retError = 1;
			}
		}
			break;
		case CMD_ID_OTA_UPGRADE_PROGRESS_UPDATE_RES://升级状态返回
			break;
		case CMD_ID_OTA_UPGRADE_PROGRESS_CHANGED_NOTIFY://升级过程通知
			break;
		case CMD_ID_DEVICE_ALARM_LIST_RES://查看报警记录返回
		{
			ListDeviceAlarmsResponse alarmsResponse;
			retBool = alarmsResponse.ParseFromArray(pcmd->payLoadBuff->buff, pcmd->payLoadBuffLen);
			if(retBool)
			{
				retError = alarmsResponse.error_code();
				if(retError == 0)
				{
					if(alarmsResponse.alarms_size() >= 1)
					{
						DeviceAlarmInfo alarmInfo = alarmsResponse.alarms(0);
						if(alarmInfo.released() == 0)
						{
							TypeChar *retString = new TypeChar();
							onSendAlarmInfo((time_t)alarmInfo.alarm_time(), FALSE, (char *)alarmInfo.device_name().c_str(), alarmInfo.room_id(), alarmInfo.alarm_type(),
							                alarmInfo.device_id(), alarmInfo.sub_id(), alarmInfo.sub_type(), retString);
							delete retString;
						}
					}
				}
			}
		}
			break;
		default:
			mPrintf(Log_NetWork, "unknow commandID!->%s ", mGetNetCMDString(pcmd->commandID));
			break;
	}
	if(retError > 0)
	{
		mPrintf(Log_NetWork, "error parsing!->%s error=%d ", mGetNetCMDString(pcmd->commandID), retError);
	}
	return 0;
}
