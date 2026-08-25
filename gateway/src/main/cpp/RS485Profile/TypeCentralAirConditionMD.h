//
// Created by wenyu xia on 2019/9/20.
//

#ifndef SMARTHOME_TYPECENTRALAIRCONDITIONINGMD_H
#define SMARTHOME_TYPECENTRALAIRCONDITIONINGMD_H


class TypeCentralAirConditioningMD
{
public:
	uint8_t deviceID;
	uint8_t cmdID;
	TypeChar *airAddrBuff;
	int32_t airAddrBuffLen;
	uint32_t crc16;
	TypeCentralAirConditioningMD(uint8_t *tbuff, int32_t len);
	TypeCentralAirConditioningMD(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeCentralAirConditioningMD();
};


#endif //SMARTHOME_TYPECENTRALAIRCONDITIONINGMD_H
