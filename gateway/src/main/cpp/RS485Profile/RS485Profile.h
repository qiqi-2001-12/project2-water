//
// Created by wenyu xia on 2018/7/2.
//

#ifndef SMARTHOME_RS485PROFILE_H
#define SMARTHOME_RS485PROFILE_H
class RS485Profile
{
public:
	TypeChar *sendBuff;
	int32_t sendLen;
	RS485Profile(int32_t attrid, uint8_t *buff, uint8_t len, TypeDeviceTypeInfo *deviceinfo);
	RS485Profile(int32_t tcmdid, uint8_t *tbuff, int32_t len, TypeApplianceInfo *applianceinfo, int32_t tshortaddr);
	~RS485Profile();
};

#define CENTRAL_AIR_MANU_MD        "MEDIA"
#define CENTRAL_AIR_MANU_ZH        "ZHONGHONG"
#define CENTRAL_AIR_MANU_HY        "HONYAR"
#define CENTRAL_AIR_MANU_LF        "LAFFEY"

#define EL_CURTAIN_AIR_AK          "H304-UK-AK01"
#define EL_CURTAIN_AIR_485_AK      "H304-UK-AK00"
#define PRESENCE_SENSE_AIR         "H404_UK_MI00"
#define AIR_SWITCH_MD_AIR          "HY10-UK-AS00"
#endif //SMARTHOME_RS485PROFILE_H
