/*
 * NetWorkBase.h
 *
 *  Created on: 2017年6月28日
 *      Author: root
 */

#ifndef NETWORKBASE_H_
#define NETWORKBASE_H_
void * mfTCPNetWorkThread(void * arg);
void mfTCPCMDSend(uint32_t cmmandid, char const * value, int valuelen);
void mfTcpHeartbeatCMDCB(int par1, int par2);
bool onTcpCheckSeqNo(uint32_t seqno, uint32_t cmd_id);
bool onTcpAckCMDSend(TypeTcpCMD * ptcpcmd);
bool onTcpCheckRepeatList(uint32_t seqno);
bool onGetConnectFlag();
bool onResetTcpConnect();

//NetWorkCMD.cpp
void onNetCMDGetDeviceReq(uint32_t flag);
int onGetCMDGetDeviceRes(GetDevicesResponse *response);
void onUpdateMasterStartGatewayInfo(int32_t channel);
void onUpdateMasterResetGatewayInfo(int32_t channel, char * name, int32_t room_id);
void onUpdateDLStatus(OTAUpgradeStage type, int32_t percent, const char * msg);
void onUpdateSlaveStartGatewayInfo(int32_t channel_ex);
void onUpdateSlaveResetGatewayInfo(int32_t channel_ex, char * name, int32_t room_id);
void onModifyDeviceSecurityStatus(int32_t keyid, int32_t subid, int32_t type, int32_t security);
void onUpdateDeviceSoftVer(int32_t device_id, char *ver);
void onUpdateDeviceInfo(int32_t device_id, int32_t shortaddr, int32_t shortaddr_ex);
void onSendDevNoticeEvent(int32_t device_id, int32_t subid, EmunDeviceEventNotity event_type, uint8_t *buff, int32_t len);
void onUpdateOnOffLineRequest(TypeDBDeviceInfo *dbdeviceinfo, DeviceStatus status);
void onCheckDBGatewayInfo(TypeDBDeviceInfo *dbdeviceinfo, int64_t newgatewayid);
void onDeviceIEEERequest(int64_t ieee, int64_t ieee_ex);
void onGetSceneActionInfo(int64_t scene_id);
void onUpdateSceneStatusInfo(int64_t sceneid, int32_t status);
#endif /* NETWORKBASE_H_ */
