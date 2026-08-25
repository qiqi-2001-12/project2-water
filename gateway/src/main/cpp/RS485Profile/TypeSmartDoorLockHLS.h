//
// Created by wenyu xia on 2018/10/16.
//

#ifndef SMARTHOME_TYPESMARTDOORLOCKHLS_H
#define SMARTHOME_TYPESMARTDOORLOCKHLS_H

class TypeSmartDoorLockHLS
{
public:
	int32_t addr;
	int32_t cmdID;
	int32_t buffLen;
	TypeChar *buff;
	TypeSmartDoorLockHLS(uint8_t *tbuff, int32_t len);
	TypeSmartDoorLockHLS(int32_t taddr, int32_t tcmdid, uint8_t *tbuff, int32_t len);
	void onToProcessCMD(TypeApplianceInfo *appinfo);
	~TypeSmartDoorLockHLS();
};


#endif //SMARTHOME_TYPESMARTDOORLOCKHLS_H
