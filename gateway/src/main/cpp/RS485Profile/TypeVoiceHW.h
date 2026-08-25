//
// Created by wenyu xia on 2019-08-13.
//

#ifndef SMARTHOME_TYPEVOICEHW_H
#define SMARTHOME_TYPEVOICEHW_H


class TypeVoiceHW
{
public:
	int cmdID;
	int softVer;
	int lastRoomID;
	int lastCMDID;
	TypeVoiceHW(uint8_t *tbuff, int32_t len);
	void onToProcessCMD(int roomid);
	void onToProcessCMDEx(int roomid);
};


#endif //SMARTHOME_TYPEVOICEHW_H
