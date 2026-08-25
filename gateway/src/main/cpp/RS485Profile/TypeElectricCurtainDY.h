//
// Created by wenyu xia on 2018/10/30.
//

#ifndef SMARTHOME_TYPEELECTRICCURTAINDY_H
#define SMARTHOME_TYPEELECTRICCURTAINDY_H


class TypeElectricCurtainDY
{
public:
	int32_t addr;//默认为0xFEFE
	int32_t cmdID;
	int32_t buffLen;
	TypeChar *buff;
	TypeElectricCurtainDY(uint8_t *tbuff, int32_t len);
	TypeElectricCurtainDY(int32_t taddr, int32_t tcmdid);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeElectricCurtainDY();
};


#endif //SMARTHOME_TYPEELECTRICCURTAINDY_H
