//
// Created by wenyu xia on 2019-07-02.
//

#ifndef SMARTHOME_TYPEELECTRICCUTAIN485AK_H
#define SMARTHOME_TYPEELECTRICCUTAIN485AK_H

#define FIXED_ID_VALUE        	0x09 //固定ID值设定
#define FIXED_CHANNEL_VALUE        0x8000 //固定频道值设定

class TypeElectricCurtain485AK
{
public:
    uint8_t NativeID;
	uint16_t NativeChannel;
	int8_t Glystro_ID;	//开合帘功能反馈
	int8_t Glystro_Status;	//开合帘状态
	int8_t BSG_Site;	//电机位置
	int8_t BSG_Status;	//电机状态
	int8_t RotateSpeed; //转速
	uint8_t CheckPoint;	//查询点
	TypeChar *buff;
	int32_t buffLen;

	TypeElectricCurtain485AK(uint8_t *tbuff, int32_t len);
	TypeElectricCurtain485AK(int8_t ID,int16_t Channel,int32_t cmdid);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeElectricCurtain485AK();
};


#endif //SMARTHOME_TYPEELECTRICCUTAIN485AK_H
