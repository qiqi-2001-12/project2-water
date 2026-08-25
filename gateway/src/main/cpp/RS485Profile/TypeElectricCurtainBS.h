//
// Created by wenyu xia on 2019-07-02.
//

#ifndef SMARTHOME_TYPEELECTRICCUTAINBS_H
#define SMARTHOME_TYPEELECTRICCUTAINBS_H


class TypeElectricCurtainBS
{
public:
	int32_t cmdID;
	TypeChar *buff;
	int32_t buffLen;
	TypeElectricCurtainBS(uint8_t *tbuff, int32_t len);
	TypeElectricCurtainBS(int32_t cmdid, int32_t action, int32_t value);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeElectricCurtainBS();
};


#endif //SMARTHOME_TYPEELECTRICCUTAINBS_H
