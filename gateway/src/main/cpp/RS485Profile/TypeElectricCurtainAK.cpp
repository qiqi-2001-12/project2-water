//
// Created by wenyu xia on 2019-07-02.
//
#include "../Main/WinobleMain.h"

TypeElectricCurtainAK::TypeElectricCurtainAK(uint8_t *tbuff, int32_t len)
{
    cmdID = 0;
    buffLen = 0;
    SerialNum = 0;
    buff = NULL;
    //协议分析
    int32_t tempIndex = 0;
    int32_t status = 0;
    int32_t checkSum = 0;
    int32_t FunctionLength = 0;
    int8_t  DataIdx = 0;
    while(tempIndex < len) {
        switch (status) {
            case 0:
                if (tbuff[tempIndex] == 0x55)
                {
                    checkSum += tbuff[tempIndex];
                    status = 1;
                }
                break;

            case 1:
                if (tbuff[tempIndex] == 0xAA)
                {
                    checkSum += tbuff[tempIndex];
                    status = 2;
                }
                else
                {
                    checkSum = 0;
                    status = 0;
                }
                break;

            case 2:
                if (tbuff[tempIndex] == 0x02) {
                    checkSum += tbuff[tempIndex];
                    status = 3;
                } else {
                    checkSum = 0;
                    status = 0;
                }
                break;

            case 3:checkSum += tbuff[tempIndex];SerialNum = tbuff[tempIndex];status = 4;break;//序列号高位
            case 4:checkSum += tbuff[tempIndex];SerialNum = (SerialNum << 8) | tbuff[tempIndex];status = 5;break;//序列号低位
            case 5:checkSum += tbuff[tempIndex];cmdID = tbuff[tempIndex];status = 6;break; //命令字
            case 6:checkSum += tbuff[tempIndex];status = 7;DataLength = tbuff[tempIndex];break;//返回长度高8位
            case 7:checkSum += tbuff[tempIndex];status = 8;DataLength = (buffLen << 8) | tbuff[tempIndex];break;//返回长度低8位
            case 8:checkSum += tbuff[tempIndex];status = 9;dpID = tbuff[tempIndex];break;//返回dpID;
            case 9:checkSum += tbuff[tempIndex];status = 10;DataType = tbuff[tempIndex];break;//返回数据类型;
            case 10:checkSum += tbuff[tempIndex];status = 11;FunctionLength = tbuff[tempIndex];break;//返回功能长度高位;
            case 11:checkSum += tbuff[tempIndex];status = 12;FunctionLength = (FunctionLength << 8) | tbuff[tempIndex];buff = new TypeChar(FunctionLength);break;//返回功能长度低位;
            case 12:
                if (FunctionLength == DataIdx)
                {
                    checkSum += tbuff[tempIndex];
                    status = 13;
                }
                else
                {
                    checkSum += tbuff[tempIndex];
                    buff->ubuff[DataIdx] = tbuff[tempIndex];
                    DataIdx++;
                    status = 12;
                }
                break;

            case 13:
                if (checkSum == tbuff[tempIndex]) {
                    return;
                }
                else
                {
                    status = 0;
                    dpID = 0;   //错误命令字
                }
                break;

            default:
                break;
        }
        tempIndex++;
    }
}

TypeElectricCurtainAK::TypeElectricCurtainAK(uint16_t serial,int32_t cmdid)
{
    cmdID = 0;
    dpID = 0;
    SerialNum = 0;
    DataLength = 0;
    DataType = 0;
    FunctionLength = 0;
    buff = NULL;
    buffLen = 0;
    uint8_t idx = 0;
    uint8_t CRC_Data = 0;

    if((cmdid > 0) && (cmdid < 100))    //调节百分比
    {
        cmdID = 0x04;//控制命令
        SerialNum = serial;
        DataLength = 0x0008;
        dpID = 0x02;
        DataType = 0x02;
        FunctionLength = 0x0004;
        buffLen = 18;
        buff = new TypeChar((uint32_t)buffLen);
        buff->ubuff[13] = 0x00;
        buff->ubuff[14] = 0x00;
        buff->ubuff[15] = 0x00;
        buff->ubuff[16] = cmdid & 0xff;
    }
    else if(((cmdid >= 100) && (cmdid <= 103)) || (cmdid == 0))  //控制
    {
        cmdID = 0x04;
        SerialNum = serial;
        DataLength = 0x0005;
        dpID = 0x01;
        DataType = 0x04;
        FunctionLength = 0x0001;
        buffLen = 15;
        buff = new TypeChar((uint32_t)buffLen);
        if(cmdid == 101)   //关
        {
            buff->ubuff[13] = 0x02;
        }
        else if(cmdid == 102)  //开
        {
            buff->ubuff[13] = 0x00;
        }
        else if(cmdid == 0)
        {
            buff->ubuff[13] = 0x00; //100%--关
        }
        else if(cmdid == 100)
        {
            buff->ubuff[13] = 0x02; //0%--开
        }
        else    //暂停
        {
            buff->ubuff[13] = 0x01;
        }
    }
    else if((cmdid == 105) || (cmdid == 106) || (cmdid == 107)) //电机方向
    {
        cmdID = 0x04;
        SerialNum = serial;
        DataLength = 0x0005;
        dpID = 0x05;
        DataType = 0x01;
        FunctionLength = 0x0001;
        buffLen = 15;
        buff = new TypeChar((uint32_t)buffLen);
        if(cmdid == 106)
        {
            buff->ubuff[13] = 0x00; //反方向
        }
        else// if(cmdid == 107)
        {
            buff->ubuff[13] = 0x01; //正方向
        }
    }

    buff->ubuff[0] = buffLen - 1;
    buff->ubuff[1] = 0x55;
    buff->ubuff[2] = 0xaa;
    buff->ubuff[3] = 0x02;
    buff->ubuff[4] = (SerialNum >> 8) & 0xff;
    buff->ubuff[5] = SerialNum & 0xff;
    buff->ubuff[6] = cmdID;
    buff->ubuff[7] = (DataLength >> 8) & 0xff;
    buff->ubuff[8] = DataLength & 0xff;
    buff->ubuff[9] = dpID;
    buff->ubuff[10] = DataType;
    buff->ubuff[11] = (FunctionLength >> 8) & 0xff;
    buff->ubuff[12] = FunctionLength & 0xff;

    for(idx = 1;idx < buffLen - 1;idx++)
    {
        CRC_Data += buff->ubuff[idx];
    }
    buff->ubuff[buffLen - 1] = CRC_Data;
}

void TypeElectricCurtainAK::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
    //命令处理
    switch(cmdID)
    {
        case 0x02:  //报告网络状态
//            int32_t tempSave = appinfo->saveValue;
//            RS485Profile *temp485Profile = new RS485Profile(106 + (bool)buff->ubuff[2], NULL, 0, appinfo, tshortaddr);
//            pmMasterSerialPort->onWriteAttribute((uint32_t)tshortaddr, 1, CLUSTER_ID_PERSONAL, new TypeZclAttribute(0x000C, ZCL_DATATYPE_CHAR_STR, temp485Profile->sendBuff->ubuff, (uint8_t)temp485Profile->sendLen), 0);
//            delete temp485Profile;
        break;

        case 0x06: //控制命令

            if((dpID == 0x02) || (dpID == 0x03))//百分比
            {
                appinfo->value = buff->ubuff[3];
                ApplianceValueChangedNotification valueChangedNotification;
                valueChangedNotification.set_appliance_id(appinfo->appID);
                valueChangedNotification.set_value(appinfo->value);
                mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
            }
            break;
    }
}

TypeElectricCurtainAK::~TypeElectricCurtainAK()
{
    if(buff)
    {
        delete buff;
    }
}