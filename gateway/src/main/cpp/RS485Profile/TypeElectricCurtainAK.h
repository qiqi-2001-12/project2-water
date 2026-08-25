//
// Created by wenyu xia on 2019-07-02.
//

#ifndef SMARTHOME_TYPEELECTRICCUTAINAK_H
#define SMARTHOME_TYPEELECTRICCUTAINAK_H


class TypeElectricCurtainAK
{
public:
    int8_t cmdID;
	int8_t dpID;
	int16_t SerialNum;
    int16_t DataLength;
    int8_t DataType;
    int16_t FunctionLength;
	TypeChar *buff;
	int32_t buffLen;

	TypeElectricCurtainAK(uint8_t *tbuff, int32_t len);
	TypeElectricCurtainAK(uint16_t serial,int32_t cmdid);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeElectricCurtainAK();
};


#endif //SMARTHOME_TYPEELECTRICCUTAINAK_H
