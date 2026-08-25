//
// Created by wenyu xia on 2018/10/16.
//

#ifndef SMARTHOME_TYPECENTRALAIRCONDITIONINGZH_H
#define SMARTHOME_TYPECENTRALAIRCONDITIONINGZH_H


class TypeCentralAirConditioningZH
{
public:
	uint8_t gatewayAddr;
	uint8_t cmdID;
	uint8_t ctlValue;
	uint8_t airCnt;
	TypeChar *airAddrBuff;
	int32_t airAddrBuffLen;
	TypeCentralAirConditioningZH(uint8_t *tbuff, int32_t len);
	TypeCentralAirConditioningZH(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeCentralAirConditioningZH();
};


#endif //SMARTHOME_TYPECENTRALAIRCONDITIONINGZH_H
