//
// Created by wenyu xia on 2019-07-02.
//
#include "../Main/WinobleMain.h"


static uint16_t CRC_verify(uint8_t* data,uint8_t length);
/*************************************************
CRC校验函数 */
static uint16_t CRC_verify(uint8_t* data,uint8_t length)
{
    uint8_t idx = 0;
    uint8_t CRC_data = 0;

    for(idx = 0;idx < length;idx++)
    {
        CRC_data += data[idx];
    }
    return CRC_data;
}

TypeAirSwitch::TypeAirSwitch(uint8_t *tbuff, int32_t len)
{
    buffLen = 0;
    buff = NULL;
    SwitchAddr = 0;
    PassParameter = 0;
    SerialCMD = 0;

    //协议分析
    int32_t idx = 0;
    int32_t RxState = 0;
    int8_t DataIdx = 0;
    uint8_t DataLength = 0;

    while (idx < len)
    {
        switch (RxState) {
            case STATE_FRAME_HEADER_H:
                if (tbuff[idx] == 0xaa)
                {
                    RxState = STATE_FRAME_HEADER_L;
                }
                break;

            case STATE_FRAME_HEADER_L:
                if (tbuff[idx] == 0x55)
                {
                    RxState = STATE_ADDRESS;
                }
                break;

            case STATE_ADDRESS:
                SwitchAddr = tbuff[idx];
                RxState = STATE_CMD;
                break;

            case STATE_CMD:
                if(tbuff[idx] == 0x03)
                {
                    RxState = STATE_TYPE;
                }
                break;

            case STATE_TYPE:
                if((tbuff[idx] == 0x01) || (tbuff[idx] == 0x02))
                {
                    ReportType = tbuff[idx];
                    RxState = STATE_LENGTH;
                }
                break;

            case STATE_LENGTH:
                if((tbuff[idx] == 0x10) && (ReportType == TYPE_VALUE))
                {
                    RxState = STATE_DATA;
                }
                else if((tbuff[idx] == 0x0e) && (ReportType == TYPE_CONFIG))
                {
                    RxState = STATE_DATA;
                }
                DataLength = tbuff[idx];
                break;

            case STATE_DATA:
                if(ReportType == TYPE_VALUE)
                {
                    Value_1[DataIdx++] = tbuff[idx];
                }
                else if(ReportType == TYPE_CONFIG)
                {
                    config[DataIdx++] = tbuff[idx];
                }
                DataLength--;
                if(DataLength == 0x00)
                {
                    RxState = STATE_CRC;
                }
                break;

            case STATE_CRC:
                if(tbuff[idx] == CRC_verify(tbuff,idx))
                {

                }
                break;

            default:
                break;
        }
        idx++;
    }
}

TypeAirSwitch::TypeAirSwitch(int32_t cmdid)
{
    buffLen = 0;
    buff = NULL;

    SwitchAddr = (cmdid >> 24) & 0xff;
    PassParameter = (cmdid >> 8) & 0xffff;
    SerialCMD = cmdid & 0xff;

    buffLen = 5;
    buff = new TypeChar((uint32_t)buffLen);
    buff->ubuff[0] = buffLen - 1;
    buff->ubuff[1] = (cmdid >> 24) & 0xff;
    buff->ubuff[2] = (cmdid >> 16) & 0xff;
    buff->ubuff[3] = (cmdid >> 8) & 0xff;
    buff->ubuff[4] = cmdid & 0xff;
}

void TypeAirSwitch::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
    //命令处理
    TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfoKeyIDAndAddr(appinfo->ir_id,SwitchAddr);
    switch(ReportType)
    {
        case TYPE_VALUE:
            if(tempApplianceInfo->value1->onStringCMP(Value_1) != 0)
            {
                tempApplianceInfo->value1->onClear();
                tempApplianceInfo->value1->onAddString(Value_1);
                ApplianceValueChangedNotification valueChangedNotification;
                valueChangedNotification.set_appliance_id(tempApplianceInfo->appID);
                valueChangedNotification.set_value1(tempApplianceInfo->value1->buff);
                valueChangedNotification.set_config(tempApplianceInfo->config->buff);
                mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
            }
            break;

        case TYPE_CONFIG:
            if(tempApplianceInfo->config->onStringCMP(config) != 0)
            {
                tempApplianceInfo->config->onClear();
                tempApplianceInfo->config->onAddString(config);
                ApplianceValueChangedNotification valueChangedNotification;
                valueChangedNotification.set_appliance_id(tempApplianceInfo->appID);
                valueChangedNotification.set_value1(tempApplianceInfo->value1->buff);
                valueChangedNotification.set_config(tempApplianceInfo->config->buff);
                mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
            }
            break;
    }
}

TypeAirSwitch::~TypeAirSwitch()
{
    if(buff)
    {
        delete buff;
    }
}