/*
 * PublicDefine.h
 *
 *  Created on: Jun 30, 2017
 *      Author: root
 */

#ifndef MAIN_PUBLICDEFINE_H_
#define MAIN_PUBLICDEFINE_H_
#include <stddef.h>
#include <string.h>
#include "stdint.h"
#include <stdlib.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/time.h>
#include <fcntl.h>
#include <net/if.h>
#include <netinet/in.h>
#include <sys/prctl.h>

#define Log_Slave                      0
#define Log_Master					   1
#define Log_NetWork                    2
#define Log_Error                      3
#define Log_DataBase                   4
#define LOG_Robot                      5

#define JNI_NOTIFY_UPDATE_DEVLIST       0x0001
#define JNI_NOTIFY_UPDATE_DEVSTAUS      0x0002
#define JNI_NOTIFY_UPDATE_DEVNAME       0x0004
#define JNI_NOTIFY_UPDATE_SCENELIST     0x0008
#define JNI_NOTIFY_UPDATE_SCENESTATUS   0x0010
#define JNI_NOTIFY_UPDATE_SCENENAME     0x0020
#define JNI_NOTIFY_UPDATE_ROOMLIST      0x0040
#define JNI_NOTIFY_UPDATE_ROOMNAME      0x0080
#define JNI_NOTIFY_NET_STATUS           0x0100
#define JNI_NOTIFY_ALARM                0x0200
const char mTAGLogString[6][10]={"Slave", "Master", "NetWork", "Error", "DataBase", "Robot"};
//#define mPrintf(TAG, fmt, args...) {char log[255];sprintf(log, fmt, ##args); onPrintf(TAG, log);}
bool mPrintf(int TAG, const char *fmt, ...);
bool onCheckPrint();
bool onCheckDebugMode();
void onSetDebugMode(bool flag);
#ifdef WINOBLE_LINUX
#include <sys/ioctl.h>
#include <termios.h>
#ifdef HWELLYI_MT7688
#include "../Main/LinuxGPIO.h"
#define MASTER_SERIALPORT_DEV_NAME    "/dev/ttyS1"//主模块串口名称
#define SLAVE_SERIALPORT_DEV_NAME     "/dev/ttyS2"//从模块串口名称
#define PAD_PATH_SOFT          		  "/data/"//数据库存放目录
#elif defined LINUX_MAC_TEST
#define MASTER_SERIALPORT_DEV_NAME    "/dev/cu.SLAB_USBtoUART1"//主模块串口名称
#define SLAVE_SERIALPORT_DEV_NAME     "/dev/cu.SLAB_USBtoUART"//从模块串口名称
#define PAD_PATH_SOFT          ""//数据库存放目录
#elif defined H202_UK_SHA0
#include "../Main/LinuxGPIO.h"
#define MASTER_SERIALPORT_DEV_NAME    "/dev/ttyAMA1"//主模块串口名称
#define SLAVE_SERIALPORT_DEV_NAME     "/dev/ttyAMA2"//从模块串口名称
#define PAD_PATH_SOFT          		  "/data/"//数据库存放目录
#else
#define MASTER_SERIALPORT_DEV_NAME    "/dev/ttyUSB1"//主模块串口名称
#define SLAVE_SERIALPORT_DEV_NAME     "/dev/ttyUSB0"//从模块串口名称
#define PAD_PATH_SOFT          "/home/lex/"//数据库存放目录
#endif

#else
#include <jni.h>
#include "android/log.h"
#include "../lib_include/sqlite3/termios.h"    /*PPSIX 终端控制定义*/
#define PAD_PATH_SOFT                 "/sdcard/"
#endif
#include "../protobuf/api_cmd.pb.h"
#include "../protobuf/basic.pb.h"
#include "../protobuf/push.pb.h"
#include "../protobuf/device.pb.h"
#include "../protobuf/family.pb.h"
#include "../protobuf/gateway.pb.h"
#include "../protobuf/login.pb.h"
#include "../protobuf/room.pb.h"
#include "../protobuf/scene.pb.h"
#include "../protobuf/hfonts.pb.h"
#include "../protobuf/appliance.pb.h"
#include "../protobuf/ota.pb.h"

using namespace wbapi;
#define TRUE                   1
#define FALSE                  0

#define DEFAULT_GATEWAY_NAME       "Gateway"
#define GATEWAY_SOFTVER            "2.003"

#define DUALZIGBEECHIP             FALSE//TRUE

#define VALUE_TCP_DELEAY_REPEART     3000//

#define Android_IP                     "127.0.0.1"
#define Android_Port                   36688
#define SERIAL_BAUD  				   115200

#define DB_GATEWAY_ID                   "gateway_id"

#define HWELLYI_DB_VER                 11//20220614

//#define TIMER_CHECK_GATEWAYINIT                           0x500000//测试定时器
#define TIMER_TCP_HEARTBEAT                               0x000001//timer for heartbeat
#define TIMER_TCP_REPEATCHECK                             0x000002//check repeat send when time out
#define TIMER_RPEAT_1_S                                   0x000003//1s一次中断处理
#define TIMER_NXP_DOWN_TIMEOUT                            0x000004//10s超时

#define TIMER_MASTER_STATUS_CHECK                         0x000006//主模块状态定时检查
#define TIMER_SLAVE_STATUS_CHECK                          0x000007//从模块状态定时检查
#define TIMER_WATCHDOG_TEST                               0x000008
#define TIMER_ALARM_RESET                                 0x000009
#define TIMER_AIR_TIMEOUT                                 0x00000a
#define APPID                                             25086810208075777
#define APPSERIAL                                         "7bca416fce1782b2783a6bdefaf38e80"
extern uint32_t mMemNewFreeCount;//内存泄漏监控标志
extern bool mIsDownLoadingFlag;
extern bool mTcpReciveFlag;
extern bool mIsExitFlag;
int32_t mfPublicGetInt32(char *string);
uint8_t onGetAFSendSeq();
uint8_t onGetZclSendSeq();
bool onAddThread(const char *title, void* (*__start_routine)(void*), char *para);
int64_t mfPublicGetInt64(char *string);
uint32_t onGetInt32(uint8_t *buff, uint32_t len);
uint64_t onGetInt64(uint8_t *buff, uint32_t len);
uint32_t onGetInt32Ex(uint8_t *buff, uint32_t len);
uint64_t onGetInt64Ex(uint8_t *buff, uint32_t len);
uint8_t mf4CharToHex(uint8_t value);
uint8_t mfHexToChar(uint8_t value);
bool onCheckBattery(int32_t devtype);
bool onIsGoodTemp(int32_t value);
int32_t onGetCRC16( uint8_t * buff, int32_t len);
int32_t onGetDimmingParaValue(int32_t minvalue, int32_t maxvalue, int32_t stepvalue);
char *onPrintfUBuff(uint8_t *value, int len, char *outbuff);
const char *mGetNetCMDString(int32_t commandid);
const char *onGetSerialSubCMDString(uint8_t subsystem, uint8_t index);
const char *onGetZCLCMDID(uint8_t cmdid);
int32_t onGetUtf8NameLen(char *tinname);
bool onCheckRS485BaudIsOK(int32_t value);
int32_t onConverUnicodeString(char *tinname, char *toutname, uint8_t maxoutlen);
void onNotifyToJava(int32_t tnotifyid, int64_t tlvalue1, int64_t tlvalue2, int64_t tlvalue3, const char *strvalue);
void onNotifyToJava(int32_t tnotifyid, int64_t tlcmd1, int64_t tlcmd2, int64_t tlvalue, int32_t intvalue);
int32_t onGetGatewayModelInt(const char * tmodel);
void onPublicInit();
extern int32_t mIsAlarmingFlag;
extern int32_t mGatewayType;//1=带屏网关 2=mini 7688网关 3=mini 海思网关 4=4C平板网关

#ifdef WINOBLE_LINUX
//#define SERVER_IPINFO                           "api.hwellyi.com"
#define SERVER_IPINFO                           "apinew.hwellyi.com"
#else
#ifdef RELEASE
#define SERVER_IPINFO                           "api.hwellyi.com"
//#define SERVER_IPINFO                           "apinew.hwellyi.com"
#define SERVER_DEBUG                            ""
#elif defined(DEBUG)
//#define SERVER_DEBUG                          "/debug/"
//#define SERVER_IPINFO                           "10.1.32.5"
#define SERVER_IPINFO                           "api.hwellyi.com"
//#define SERVER_IPINFO                           "test.api.hwellyi.com"
//#define SERVER_IPINFO                           "apinew.hwellyi.com"
#endif

#endif
#define SERVER_IPPORT                        8135

#define SERVER_DEBUG                            ""

#include "../DataType/TypeDefine.h"
#include "../Main/ErrorCodeDefine.h"
extern TypeDeviceList *pDeviceList;
extern TypeDataBase *pDataBase;
extern bool mIsUpdateRobotFlag;
extern TypeArrayList * mThreadInfoList;
extern TypeLinkedList *mJniNotifyLinkList;
extern int32_t mNotifyRegisterFlag;
#endif /* MAIN_PUBLICDEFINE_H_ */
