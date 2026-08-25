//
// Created by wenyu xia on 2019-07-02.
//
#include "../Main/WinobleMain.h"

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

TypePresenceSense::TypePresenceSense(uint8_t *tbuff, int32_t len)
{
    buffLen = 0;
    buff = NULL;
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
                    RxState = STATE_CMD;
                }
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
                if((tbuff[idx] == 0x05) && (ReportType == TYPE_VALUE))
                {
                    RxState = STATE_DATA;
                }
                else if((tbuff[idx] == 0x10) && (ReportType == TYPE_CONFIG))
                {
                    RxState = STATE_DATA;
                }
                DataLength = tbuff[idx];
                break;

            case STATE_DATA:
                if(ReportType == TYPE_VALUE)
                {
                    Value[DataIdx++] = tbuff[idx];
                }
                else if(ReportType == TYPE_CONFIG)
                {
                    Config[DataIdx++] = tbuff[idx];
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

TypePresenceSense::TypePresenceSense(int32_t cmdid)
{

    buff = NULL;
    buffLen = 0;

    buffLen = 5;
    buff = new TypeChar((uint32_t)buffLen);
    buff->ubuff[0] = buffLen - 1;
    buff->ubuff[1] = (cmdid >> 24) & 0xff;
    buff->ubuff[2] = (cmdid >> 16) & 0xff;
    buff->ubuff[3] = (cmdid >> 8) & 0xff;
    buff->ubuff[4] = cmdid & 0xff;
}

void TypePresenceSense::onToProcessCMD(int32_t tshortaddr, TypeApplianceInfo *appinfo)
{
    TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfoKeyIDAndAddr(appinfo->ir_id,0);
    switch(ReportType)
    {
        case TYPE_CONFIG:
            if(tempApplianceInfo->config->onStringCMP(Config) != 0)
            {
                tempApplianceInfo->config->onClear();
                tempApplianceInfo->config->onAddString(Config);
                ApplianceValueChangedNotification valueChangedNotification;
                valueChangedNotification.set_appliance_id(tempApplianceInfo->appID);
                valueChangedNotification.set_value1(tempApplianceInfo->value1->buff);
                valueChangedNotification.set_config(tempApplianceInfo->config->buff);
                mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
            }
            break;

        case TYPE_VALUE:
            appinfo->value = ((uint32_t)Value[1] << 24) | ((uint32_t)Value[2] << 16) | ((uint32_t)Value[3] << 8) | Value[4];
            ApplianceValueChangedNotification valueChangedNotification;
            valueChangedNotification.set_appliance_id(appinfo->appID);
            valueChangedNotification.set_value(appinfo->value);
            mfTCPCMDSend(CMD_ID_DEVICE_APPLIANCE_VALUE_UPDATE_REQ, valueChangedNotification.SerializeAsString().c_str(), valueChangedNotification.SerializeAsString().length());
            break;
    }
}

TypePresenceSense::~TypePresenceSense()
{
    if(buff)
    {
        delete buff;
    }
}