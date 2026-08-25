/*
 * SerialPortCMD.h
 *
 *  Created on: 2017年6月16日
 *      Author: root
 */

#ifndef SERIALPORTCMD_H_
#define SERIALPORTCMD_H_

int mfLeaveToGateway(int64_t ieee);
int mfZigbeeUpdateNetWork(int64_t gateway_id, int32_t newfamily, int32_t oldfamily);
void mfAllowToJoinCB(int par1, int par2);
void mfPIRAlarmCB(int par1, int par2);
extern bool mZigbeeUpdateFlag;
#endif /* SERIALPORTCMD_H_ */
