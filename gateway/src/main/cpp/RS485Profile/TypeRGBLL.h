//
// Created by wenyu xia on 2019/1/15.
//

#ifndef SMARTHOME_TYPERGBLL_H
#define SMARTHOME_TYPERGBLL_H


class TypeRGBLL
{
public:
	int32_t buffLen;
	TypeChar *buff;
	TypeRGBLL(int32_t *tcmdid);
	int32_t onAddRGBBuff(int32_t value, uint8_t percent, uint8_t *buff);
	~TypeRGBLL();
};

#endif //SMARTHOME_TYPERGBLL_H
