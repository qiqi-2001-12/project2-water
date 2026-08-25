/*
 * SerialPortBase.h
 *
 *  Created on: 2017年6月13日
 *      Author: root
 */

#ifndef SERIALPORTBASE_H_
#define SERIALPORTBASE_H_

void * mfSerialPortThead(void *arg);
bool onSendAlarmInfo(time_t time, bool todevice, char *name, int32_t roomid, int32_t alarmtype, int32_t deviceid, int32_t subid, int32_t subtype, TypeChar *retstr);
extern TypeSerialDrive *pmMasterSerialPort;
extern TypeSerialDrive *pmSlaveSerialPort;
#endif /* SERIALPORTBASE_H_ */
