//
// Created by wenyu xia on 2019-10-09.
//

#ifndef SMARTHOME_TYPEELECTRICCURTAINSX_H
#define SMARTHOME_TYPEELECTRICCURTAINSX_H


class TypeElectricCurtainSX
{
public:
	int32_t cmdID;
	TypeChar *buff;
	int32_t buffLen;
	TypeElectricCurtainSX(uint8_t *tbuff, int32_t len);
	TypeElectricCurtainSX(int32_t cmdid, int32_t action, int32_t value);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeElectricCurtainSX();
};


#endif //SMARTHOME_TYPEELECTRICCURTAINSX_H
