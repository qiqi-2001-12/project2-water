//
// Created by wenyu xia on 2019-07-02.
//

#ifndef SMARTHOME_TYPEAIRSWITCH_H
#define SMARTHOME_TYPEAIRSWITCH_H

/* 协议分析 */
typedef enum
{
	STATE_FRAME_HEADER_H    = 0x00,     //帧头
	STATE_FRAME_HEADER_L    = 0x01,     //帧头
	STATE_ADDRESS    		= 0x02,     //地址
	STATE_CMD    			= 0x03,     //命令
	STATE_TYPE    			= 0x04,     //上报类型
	STATE_LENGTH    		= 0x05,     //数据长度
	STATE_DATA    			= 0x06,     //数据
	STATE_CRC    			= 0x07,     //CRC校验
}GM_Rx_t;

/* 上报类型 */
typedef enum
{
	TYPE_VALUE    = 0x01,
	TYPE_CONFIG   = 0x02,
}GM_type_t;

class TypeAirSwitch
{
public:
	TypeChar *buff;
	int32_t buffLen;
	uint8_t SwitchAddr;		//开关地址
	uint16_t PassParameter;	//传递参数
	uint8_t SerialCMD;		//指令序号
	char Value_1[16];	//显示值
	char config[14];		//设定值
	uint8_t ReportType;		//上报类型

	TypeAirSwitch(uint8_t *tbuff, int32_t len);
	TypeAirSwitch(int32_t cmdid);
	void onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo);
	~TypeAirSwitch();
};


#endif //SMARTHOME_TYPEAIRSWITCH_H
