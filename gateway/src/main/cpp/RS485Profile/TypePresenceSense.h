//
// Created by wenyu xia on 2019-07-02.
//

#ifndef SMARTHOME_TYPEPRESENCESENSE_H
#define SMARTHOME_TYPEPRESENCESENSE_H

class TypePresenceSense
{
public:
	TypeChar *buff;
	int32_t buffLen;
    uint8_t ReportType;		//上报类型
	uint8_t Value[5];
	char Config[17];

	TypePresenceSense(uint8_t *tbuff, int32_t len);
	TypePresenceSense(int32_t cmdid);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypePresenceSense();
};


#endif //SMARTHOME_TYPEPRESENCESENSE_H
