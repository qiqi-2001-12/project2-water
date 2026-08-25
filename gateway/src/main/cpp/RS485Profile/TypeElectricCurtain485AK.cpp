//
// Created by wenyu xia on 2019-07-02.
//
#include "../Main/WinobleMain.h"

TypeElectricCurtain485AK::TypeElectricCurtain485AK(uint8_t *tbuff, int32_t len)
{
    NativeID = 0x00;
    NativeChannel = 0x00;
    Glystro_ID = 0;	//开合帘功能反馈
    Glystro_Status = 0;	//开合帘状态
    BSG_Site = 0x00;	//电机位置
    BSG_Status = 0x00; //电机状态
    RotateSpeed = 0x00; //转速
    buff = NULL;
    buffLen = 0x00;
    CheckPoint = 0; //查询点

    int32_t tempIndex = 0;
    int8_t checkSum = 0;
    int32_t status = 0;

    while(tempIndex < len)
    {
        switch (status)
        {
            case 0:if(tbuff[tempIndex] == 0xd8) {status = 1;}break;

            case 1:checkSum ^= tbuff[tempIndex];NativeID = tbuff[tempIndex];status = 2;break;   //本机ID

            case 2:checkSum ^= tbuff[tempIndex];NativeChannel = tbuff[tempIndex];status = 3;break;  //本机频道高8位

            case 3:checkSum ^= tbuff[tempIndex];NativeChannel = (NativeChannel << 8) | tbuff[tempIndex];status = 4;break; //本机频道低8位

            case 4:checkSum ^= tbuff[tempIndex];status = 5;break;

            case 5:checkSum ^= tbuff[tempIndex];status = 6;break;

            case 6:checkSum ^= tbuff[tempIndex];RotateSpeed = tbuff[tempIndex];status = 7;break; //转速

            case 7:
                checkSum ^= tbuff[tempIndex];
                status = 8;
                if(tbuff[tempIndex] == 0xca)
                {
                    Glystro_ID = tbuff[tempIndex]; //开合帘功能查询反馈
                    CheckPoint = 0x01;  //开合帘反馈
                }
                else
                {
                    if((tbuff[tempIndex - 1] <= 100) && (tbuff[tempIndex - 1] >= 0))
                    {
                        BSG_Site = tbuff[tempIndex]; //电机状态查询反馈位置
                        CheckPoint = 0x02;  //电机反馈
                    }
                    else
                    {
                        status = 0;
                    }
                }
                break; //开合帘功能反馈或者位置

            case 8:
                checkSum ^= tbuff[tempIndex];
                if(tbuff[tempIndex - 1] == 0xca)
                {
                    Glystro_Status = tbuff[tempIndex]; //开合帘状态
                }
                else if((tbuff[tempIndex - 1] <= 100) && (tbuff[tempIndex - 1] >= 0))
                {
                    BSG_Status = tbuff[tempIndex]; //电机状态
                }
                status = 9;
                break; //状态

            case 9:if(checkSum == tbuff[tempIndex]){status = 10;}break;

            default:
                break;
        }
        tempIndex++;
    }
}

TypeElectricCurtain485AK::TypeElectricCurtain485AK(int8_t ID,int16_t Channel,int32_t cmdid)
{
    NativeID = 0x00;
    NativeChannel = 0x00;
    Glystro_ID = 0;	//开合帘功能反馈
    Glystro_Status = 0;	//开合帘状态
    BSG_Site = 0x00;	//电机位置
    BSG_Status = 0x00; //电机状态
    RotateSpeed = 0x00; //转速
    buff = NULL;
    buffLen = 0x00;
    CheckPoint = 0; //查询点

    int32_t idx = 0;
    int32_t CRC_Data = 0;

    buffLen = 8;
    buff = new TypeChar((uint32_t)buffLen);

    if((cmdid >= 0) && (cmdid <= 100))    //调节百分比
    {
        buff->ubuff[2] = ID;
        buff->ubuff[3] = (Channel >> 8) & 0xff;
        buff->ubuff[4] = Channel & 0xff;
        buff->ubuff[5] = 0xdd;
        buff->ubuff[6] = cmdid & 0xff;
    }
    else if((cmdid >= 101) && (cmdid <= 103))  //控制
    {
        buff->ubuff[2] = ID;
        buff->ubuff[3] = (Channel >> 8) & 0xff;
        buff->ubuff[4] = Channel & 0xff;
        buff->ubuff[5] = 0x0a;

        switch(cmdid)
        {
            case 101:
                buff->ubuff[6] = 0xee;  //下行
            break;

            case 102:
                buff->ubuff[6] = 0xdd;  //上行
            break;

            case 103:
                buff->ubuff[6] = 0xcc;  //停止
            break;
        }
    }
    else if((cmdid >= 105) && (cmdid <= 108))
    {
        buff->ubuff[2] = 0x00;
        buff->ubuff[3] = 0x00;
        buff->ubuff[4] = 0x00;

        switch(cmdid)
        {
            case 106:
                buff->ubuff[5] = 0xd5;
                buff->ubuff[6] = 0x02; //设置反向
            break;

            case 107:
                buff->ubuff[5] = 0xd5;
                buff->ubuff[6] = 0x00; //默认方向
            break;

            case 108:
                buff->ubuff[5] = 0xcc;
                buff->ubuff[6] = 0xcc; //电机状态查询
            break;

            case 105:
                buff->ubuff[5] = 0xca;
                buff->ubuff[6] = 0xca; //开合帘功能状态查询
                break;
        }
    }
    else if(cmdid == 112) //设置ID、频道
    {
        buff->ubuff[2] = ID;
        buff->ubuff[3] = (Channel >> 8) & 0xff;
        buff->ubuff[4] = Channel & 0xff;
        buff->ubuff[5] = 0xaa;
        buff->ubuff[6] = 0xaa;
    }

    for(idx = 2;idx < 7;idx++)
    {
        CRC_Data ^= buff->ubuff[idx];
    }
    buff->ubuff[buffLen - 1] = CRC_Data;
    buff->ubuff[0] = buffLen - 1;
    buff->ubuff[1] = 0x9a;
}

void TypeElectricCurtain485AK::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
    if(CheckPoint == 0x01) //开合帘反馈
    {
        CheckPoint = 0; //清除标志
        int32_t tempSave = appinfo->saveValue;
        RS485Profile *temp485Profile = new RS485Profile(106 + ((Glystro_Status >> 1) & 0x01), NULL, 0, appinfo, tshortaddr); //设置反向
        pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
        delete temp485Profile;
    }
    else if(CheckPoint == 0x02) //电机反馈
    {
        CheckPoint = 0; //清除标志
        appinfo->value = BSG_Site; //位置
        ApplianceValueChangedNotification valueChangedNotification;
        valueChangedNotification.set_appliance_id(appinfo->appID);
        valueChangedNotification.set_value(appinfo->value);
        mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ,valueChangedNotification.SerializeAsString().c_str(),valueChangedNotification.SerializeAsString().length());
    }
}

TypeElectricCurtain485AK::~TypeElectricCurtain485AK()
{
    if(buff)
    {
        delete buff;
    }
}