//
// Created by wenyu xia on 2018/10/16.
//

#ifndef SMARTHOME_TYPETEMPERATURECONTROL_H
#define SMARTHOME_TYPETEMPERATURECONTROL_H


class TypeTemperatureControl
{
public:
	uint8_t gatewayAddr;
	uint8_t cmdID;
	uint8_t ctlValue;
	uint8_t airCnt;
	TypeChar *airAddrBuff;
	int32_t airAddrBuffLen;
	TypeTemperatureControl(uint8_t *tbuff, int32_t len);
	TypeTemperatureControl(int32_t tshortaddr, int32_t tcmdid, TypeApplianceInfo *appinfo);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeTemperatureControl();
};


#endif //SMARTHOME_TYPETEMPERATURECONTROL_H
