//
// Created by wenyu xia on 2018/10/16.
//

#ifndef SMARTHOME_TYPETEMPERATURECONTROLLF_H
#define SMARTHOME_TYPETEMPERATURECONTROLLF_H


class TypeTemperatureControlLF
{
public:
	uint8_t gatewayAddr;
	uint8_t cmdID;
	uint8_t ctlValue;
	uint8_t airCnt;
	TypeChar *airAddrBuff;
	int32_t airAddrBuffLen;
	TypeTemperatureControlLF(uint8_t *tbuff, int32_t len);
	TypeTemperatureControlLF(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeTemperatureControlLF();
};


#endif //SMARTHOME_TYPETEMPERATURECONTROL_H
