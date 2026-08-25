//
// Created by xia_w on 2017/9/14.
//

#ifndef SMARTHOME_ROBOTDOCKING_H
#define SMARTHOME_ROBOTDOCKING_H
void * mfRebotDockingThread(void * arg);
void onUDPSend(int port, char *value);
bool onUDPSetSceneInfo(int64_t sceneid, int32_t status);
bool onUDPSetAlarmInfo(int32_t keyid, int32_t subid, int32_t subtype, int32_t alarmtype, int64_t time, char *string);
bool onUDPDisAlarmInfo(int32_t keyid, int32_t subid, int32_t subtype, int32_t alarmtype);
bool onUDPBroadcastDeviceStatus(int32_t keyid, int32_t subid, int32_t subtype, int32_t status);
bool onUDPSynScreebInfo(int32_t deviceid, int32_t subid, char *string);
bool onUDPDevVerReturn(bool ismaster, int32_t devid, int32_t shortaddr, int32_t shortaddr_ex, char *ver);
bool onUDPDevNextImageReq(bool ismaster, uint8_t rspseq, uint16_t image, int32_t devid, int32_t filever);
bool onUDPDevImageBlockReq(bool ismaster, uint8_t rspseq, uint16_t image, int32_t devid, int32_t filever, int32_t offset, int32_t maxcnt);
bool onUDPDevUpgradeEnd(bool ismaster, uint8_t rspseq, uint16_t image, int32_t devid, int32_t filever);
bool onCheckNetPrint();
bool onNetPrint(int TAG, const char *fmt, va_list *arg);
extern TypeLinkedList *mNetLogLinkList;
#endif //SMARTHOME_ROBOTDOCKING_H
