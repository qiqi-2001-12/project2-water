/*
 * DataBase.c
 *
 *  Created on: 2017年6月18日
 *      Author: root
 */

#include "../Main/WinobleMain.h"
#include "../DataType/TypeDefine.h"

//{"SN":6066005668826351,"ID":53370394685882369,"TYPE":1}
enum _DATA_BASE_TYPE_
{
	tgateway_id = 0,
	tstatus = 1,
	tdbGateway = 2,
	tserial = 3,
	tieee = 4,
	tieee_ex = 5,
	tfamily_id = 6,
	tex_panid = 7,
	tex_panid_ex = 8,
	tpanid = 9,
	tpanid_ex = 10,
	tchannel = 11,
	tchannel_ex = 12,
	troom_id = 13,
	ttime_zone = 14,
	tlanguage = 15,
	tname = 16,
	//twhileieee = 17,
	//tzoneid = 18,
	tmodel = 19,
	//trgb = 20,
	tdevname = 21,
	ticon = 22,
	tcc2538ver = 23,
	tcc2538md5 = 24,
	tchiptype = 25,
	tgasarmbing = 26,
};

TypeDataBase::TypeDataBase()
{
	char **dbresult = NULL;
	int nrow = 0, ncolumn = 0;
	char *zErrMsg;//保存错误信息
	int retInt = 0;
	gateway_id = 0;
	status = 0;
	dbGateway = 0;
	serial = new TypeChar();
	ieee = 0;
	ieee_ex = 0;
	family_id = 0;
	ex_panid = 0;
	ex_panid_ex = 0;
	panid = 0;
	panid_ex = 0;
	channel = 0;
	channel_ex = 0;
	room_id = 0;
	cc2538Ver = 0;
	chipType = 0;
	pSqlHandle = NULL;
	time_zone = new TypeChar();
	language = new TypeChar();
	name = new TypeChar();
	cc2538md5 = new TypeChar();
	TypeChar *tempBuff = new TypeChar(256);
	sprintf(tempBuff->buff, "%sxwydb", PAD_PATH_SOFT);
	devEventList = new TypeArrayList(ArrayTypeDevEventInfo);
	gasArmBingList = new TypeArrayList(ArrayTypeGasArmBingInfo);
	while(true)
	{
		retInt = sqlite3_open(tempBuff->buff, &pSqlHandle);
		if(retInt)
		{
			mPrintf(Log_DataBase, "Error:Database: Can not open database:%s, err = %d ", sqlite3_errmsg(pSqlHandle), retInt);
			sqlite3_close(pSqlHandle);
			pSqlHandle = NULL;
			sleep(5);
		}
		else
		{
            //good
			mPrintf(Log_DataBase, "Database: open %s successfully! ", tempBuff->buff);
			break;
		}
	}
	//数据库创建成功  创建表格
	sqlite3_exec(pSqlHandle, "create table if not exists gateway(id INTEGER PRIMARY KEY, type INTEGER, intvalue INTEGER, intvalue_ex INTEGER, strvalue VARCHAR(128))", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);

	//读取网关信息
	retInt = sqlite3_get_table(pSqlHandle, "select * from gateway", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				switch(atoi(dbresult[ncolumn + ncolumn * i + 1]))
				{
					case tgateway_id:
						gateway_id = mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "GatewayID:%lld ", gateway_id);
						break;
					case tstatus:
						status = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "Status:%d ", status);
						break;
					case tdbGateway:
						dbGateway = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "DBGateway:%d ", dbGateway);
						break;
					case tserial:
						serial->onAddString(0, dbresult[ncolumn + ncolumn * i + 4]);
						mPrintf(Log_DataBase, "Serial:%s ", serial->buff);
						break;
					case tieee:
						ieee = mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "Ieee:%llx ", ieee);
						break;
					case tieee_ex:
						ieee_ex = mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "IeeeEx:%llx ", ieee_ex);
						break;
					case tfamily_id:
						family_id = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "FamilyID:%d ", family_id);
						break;
					case tex_panid:
						ex_panid = mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "Ex_PanID:%llx ", ex_panid);
						break;
					case tex_panid_ex:
						ex_panid_ex = mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "Ex_PanIDEx:%llx ", ex_panid_ex);
						break;
					case tpanid:
						panid = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "PanID:%x ", panid);
						break;
					case tpanid_ex:
						panid_ex = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "PanIDEx:%x ", panid_ex);
						break;
					case tchannel:
						channel = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "Channel:%d ", channel);
						break;
					case tchannel_ex:
						channel_ex = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "ChannelEx:%d ", channel_ex);
						break;
					case troom_id:
						room_id = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "RoomID:%d ", room_id);
						break;
					case ttime_zone:
						time_zone->onAddString(0, dbresult[ncolumn + ncolumn * i + 4]);
						mPrintf(Log_DataBase, "time_zone:%s ", time_zone->buff);
						break;
					case tlanguage:
						language->onAddString(0, dbresult[ncolumn + ncolumn * i + 4]);
						mPrintf(Log_DataBase, "language:%s ", language->buff);
						break;
					case tname:
						name->onAddString(0, dbresult[ncolumn + ncolumn * i + 4]);
						mPrintf(Log_DataBase, "name:%s ", name->buff);
						break;
					case tgasarmbing://燃气安全绑定表
					{
						TypeGasArmBingInfo *tempGasArmBingInfo = new TypeGasArmBingInfo(mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 2]), mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 3]), 0);
						gasArmBingList->add(tempGasArmBingInfo);
						mPrintf(Log_DataBase, "gasArmBing:%lld-%lld ", tempGasArmBingInfo->gasKeyID, tempGasArmBingInfo->gasArmKeyID);
					}
						break;
					case tcc2538ver://cc2538版本
					{
						cc2538Ver = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						mPrintf(Log_DataBase, "cc2538Ver:%d ", cc2538Ver);
					}
						break;
					case tchiptype://芯片类型
					{
						chipType = atoi(dbresult[ncolumn + ncolumn * i + 2]);
						if(chipType == 2538)
						{
							mPrintf(Log_DataBase, "chipType:cc2538 ");
						}
						else if(chipType == 2530)
						{
							mPrintf(Log_DataBase, "chipType:cc2530 ");
						}
						else
						{
							mPrintf(Log_DataBase, "chipType:unknow ");
						}
					}
						break;
					case tcc2538md5://cc2538md5值
					{
						cc2538md5->onAddString(dbresult[ncolumn + ncolumn * i + 4]);
						mPrintf(Log_DataBase, "cc2538md5:%s ", cc2538md5->buff);
					}
						break;
					default:
						mPrintf(Log_DataBase, "Error:未知数据库类型!=%d ", atoi(dbresult[ncolumn + ncolumn * i + 1]));
						//onDeleteSqlValue(atoi(dbresult[ncolumn + ncolumn * i + 1]), mfPublicGetUInt64(dbresult[ncolumn + ncolumn * i + 2]));
						break;
				}
			}
		}
	}
	else
	{
		mPrintf(Log_DataBase, "Error:DataBase Non-existent! on Init ");
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);

	if(onGetDBGateway() < HWELLYI_DB_VER)
	{
		mPrintf(Log_DataBase, "Update DataBase!Ver=%d ", HWELLYI_DB_VER);
		//删除以下所有表，再重新创建
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'roominfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'dbdeviceinfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'subdeviceinfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'scenenameinfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'sceneactioninfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'sceneacondinfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'applianceinfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'applianceinfoex'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'appliancecodeinfo'", 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);
		//删除所有设备名称  设备图标
		sprintf(tempBuff->buff, "delete from gateway where type=%d or type=%d", ticon, tdevname);
		sqlite3_exec(pSqlHandle, tempBuff->buff, 0, 0, &zErrMsg);
		sqlite3_free(zErrMsg);

		//更新一下新的数据库版本
		onSetStatus(1);//设置初始化标志
		onSetDBGateway(HWELLYI_DB_VER);
	}

	//创建房间信息表
	sqlite3_exec(pSqlHandle, "create table if not exists roominfo(roomid INTEGER PRIMARY KEY, iconid INTEGER, tempvalue INTEGER, illuvalue INTEGER, name VARCHAR(64))", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//得到房间列表
	retInt = sqlite3_get_table(pSqlHandle, "select * from roominfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				pDeviceList->onAddRoomInfo(new TypeRoomInfo(atoi(dbresult[ncolumn + ncolumn * i + 0]), atoi(dbresult[ncolumn + ncolumn * i + 1]), atoi(dbresult[ncolumn + ncolumn * i + 2]), atoi(dbresult[ncolumn + ncolumn * i + 3]), dbresult[ncolumn + ncolumn * i + 4]), 0);
			}
		}
	}
	//添加一个默认房间
	pDeviceList->onAddRoomInfo(new TypeRoomInfo(0, 0, 0, 0, (char *)"默认房间"), 0);
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	//创建设备信息表
	sqlite3_exec(pSqlHandle, "create table if not exists dbdeviceinfo(id INTEGER PRIMARY KEY, gatewayid INTEGER, devtype INTEGER, ieee INTEGER, addr INTEGER, ieee_ex INTEGER, addr_ex INTEGER, rgb INTEGER, savergb INTEGER, online INTEGER,"
			"protocol INTEGER, protocolver INTEGER, targetscreen INTEGER,  attr INTEGER, serial VARCHAR(64), swver  VARCHAR(64), hwver VARCHAR(64), manufacturer VARCHAR(64), subcount INTEGER)", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//读取设备列表信息
	retInt = sqlite3_get_table(pSqlHandle, "select * from dbdeviceinfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				pDeviceList->onAddDeviceInfo(new TypeDBDeviceInfo(atoi(dbresult[ncolumn + ncolumn * i + IDeviceID]), //deviceID
				                                     mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + IGatewayID]),
				                                     atoi(dbresult[ncolumn + ncolumn * i + IDevType]), //devType
				                                     mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + IIeee]),//ieee
				                                     atoi(dbresult[ncolumn + ncolumn * i + IShortAddr]),//shortAddr
				                                     mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + IIeee_Ex]),//ieee_ex
				                                     atoi(dbresult[ncolumn + ncolumn * i + IShortAddr_Ex]),//shortAddr_ex
				                                     mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + IRgb]),//rgb
				                                     mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + ISaveRgb]),//savergb
				                                     atoi(dbresult[ncolumn + ncolumn * i + IOnline]),//online
				                                     atoi(dbresult[ncolumn + ncolumn * i + IProtocol]),//protocol
				                                     atoi(dbresult[ncolumn + ncolumn * i + IProtocolVe]),//protocolVer
				                                     atoi(dbresult[ncolumn + ncolumn * i + ITargetScreen]),//targetScreen
				                                     new TypeDeviceAttr(atoi(dbresult[ncolumn + ncolumn * i + IAttr])),//attr
				                                     dbresult[ncolumn + ncolumn * i + ISerial],
				                                     dbresult[ncolumn + ncolumn * i + ISwVer],//swVer
				                                     dbresult[ncolumn + ncolumn * i + IHwVer],//hwVer
				                                     dbresult[ncolumn + ncolumn * i + IManufacturer],//manufacturer
				                                     atoi(dbresult[ncolumn + ncolumn * i + ISubCount])//subcount
				), 0);
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	//创建子设备信息列表
	sqlite3_exec(pSqlHandle, "create table if not exists subdeviceinfo(devid INTEGER, subid INTEGER, roomid INTEGER, iconid INTEGER, saveiconid INTEGER, name VARCHAR(128), savename VARCHAR(128), subtype INTEGER, "
			"intvalue1 INTEGER, intvalue2 INTEGER, intvalue3 INTEGER, intvalue4 INTEGER, intvalue5 INTEGER, intvalue6 INTEGER, strvalue1 VARCHAR(128), strvalue2 VARCHAR(128))", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//读取子设备列表
	retInt = sqlite3_get_table(pSqlHandle, "select * from subdeviceinfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				//添加子设备到设备列表
				pDeviceList->onAddSubDeviceInfo(new TypeDeviceTypeInfo(atoi(dbresult[ncolumn + ncolumn * i + 0]), //devID
				                                                     atoi(dbresult[ncolumn + ncolumn * i + 1]), //subID
				                                                     atoi(dbresult[ncolumn + ncolumn * i + 2]), //roomID
				                                                     atoi(dbresult[ncolumn + ncolumn * i + 3]), //iconid
				                                                     atoi(dbresult[ncolumn + ncolumn * i + 4]), //iconid
				                                                     dbresult[ncolumn + ncolumn * i + 5], //name
				                                                     dbresult[ncolumn + ncolumn * i + 6], //name
				                                                     (SubDeviceType)atoi(dbresult[ncolumn + ncolumn * i + 7]), //subtype
				                                                     1,
				                                                     &dbresult[ncolumn + ncolumn * i + 8]),
				                                                     		0);
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	//创建场景信息列表
	sqlite3_exec(pSqlHandle, "create table if not exists scenenameinfo(sceneid INTEGER PRIMARY KEY, name VARCHAR(128), roomid INTEGER, iconid INTEGER, specialized INTEGER, disabled INTEGER, hidden INTEGER, status INTEGER, "
			"period VARCHAR(64), enable_time VARCHAR(64))", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//读取场景列表
	retInt = sqlite3_get_table(pSqlHandle, "select * from scenenameinfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				pDeviceList->onAddSceneInfo(new TypeSceneNameInfo(mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 0]), //sceneID
				                                                  dbresult[ncolumn + ncolumn * i + 1], //name
				                                                  atoi(dbresult[ncolumn + ncolumn * i + 2]), //roomID
				                                                  atoi(dbresult[ncolumn + ncolumn * i + 3]), //iconID
				                                                  atoi(dbresult[ncolumn + ncolumn * i + 4]), //specialized
				                                                  atoi(dbresult[ncolumn + ncolumn * i + 5]), //disabled
				                                                  atoi(dbresult[ncolumn + ncolumn * i + 6]), //hidden
				                                                  atoi(dbresult[ncolumn + ncolumn * i + 7]), //status
				                                                  dbresult[ncolumn + ncolumn * i + 8], //period
				                                                  dbresult[ncolumn + ncolumn * i + 9]  //enable_time
				), 0);
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);

	//创建场景动作信息列表
	sqlite3_exec(pSqlHandle, "create table if not exists sceneactioninfo(actionid INTEGER PRIMARY KEY, sceneid INTEGER, type INTEGER, devid INTEGER, subid INTEGER, actiontype INTEGER, action INTEGER, "
			"actiondesc VARCHAR(64), delaytime INTEGER)", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//得到场景动作信息列表
	retInt = sqlite3_get_table(pSqlHandle, "select * from sceneactioninfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				//首先要找到这个场景
				TypeSceneNameInfo *tempSceneNameInfo = pDeviceList->onFindSceneInfo(mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 1]));
				if(tempSceneNameInfo)
				{
					pDeviceList->onAddSceneActionInfo(tempSceneNameInfo, new TypeSceneActionInfo(mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 0]), //actionID
					                                                           mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 1]), //sceneID
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 2]), //type
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 3]), //device_id
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 4]), //sub_id
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 5]), //action_type
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 6]), //action
					                                                           dbresult[ncolumn + ncolumn * i + 7], //action_desc
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 8]) / 1000  //delayTime
					), 0);
				}
				else
				{
					sqlite3_free(zErrMsg);
					sprintf(tempBuff->buff, "delete from sceneactioninfo where sceneid = %lld", mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 1]));
					sqlite3_exec(pSqlHandle, tempBuff->buff, 0, 0, &zErrMsg);//执行删除这条记录
				}
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	//创建场景条件信息列表
	sqlite3_exec(pSqlHandle, "create table if not exists scenecondinfo(condid INTEGER PRIMARY KEY, sceneid INTEGER, type INTEGER, condtype INTEGER, condexpre VARCHAR(128), devid INTEGER, subid INTEGER, action INTEGER, "
			"actiondesc VARCHAR(64), delaytime INTEGER)", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//得到场景条件信息表
	retInt = sqlite3_get_table(pSqlHandle, "select * from scenecondinfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				//首先要找到这个场景
				TypeSceneNameInfo *tempSceneNameInfo = pDeviceList->onFindSceneInfo(mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 1]));
				if(tempSceneNameInfo)
				{
					pDeviceList->onAddSceneCondInfo(tempSceneNameInfo, new TypeSceneCondInfo(mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 0]), //condID
					                                                           mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 1]), //sceneID
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 2]), //type
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 3]), //condType
					                                                           dbresult[ncolumn + ncolumn * i + 4], //condexpre
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 5]), //device_id
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 6]), //sub_id
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 7]), //action
					                                                           dbresult[ncolumn + ncolumn * i + 8], //actiondesc
					                                                           atoi(dbresult[ncolumn + ncolumn * i + 9]) / 1000  //delayTime
					), 0);
				}
				else
				{
					sqlite3_free(zErrMsg);
					sprintf(tempBuff->buff, "delete from scenecondinfo where sceneid = %lld", mfPublicGetInt64(dbresult[ncolumn + ncolumn * i + 1]));
					sqlite3_exec(pSqlHandle, tempBuff->buff, 0, 0, &zErrMsg);//执行删除这条记录
				}
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	//创建家电信息列表
	sqlite3_exec(pSqlHandle, "create table if not exists applianceinfoex(appid INTEGER PRIMARY KEY, irid INTEGER, subid INTEGER, name VARCHAR(128), manu VARCHAR(64), model VARCHAR(64), version VARCHAR(64), serial VARCHAR(64), roomid INTEGER, type INTEGER, value INTEGER, addr INTEGER, value1 VARCHAR(256), config VARCHAR(256))", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//读取家电信息列表
	retInt = sqlite3_get_table(pSqlHandle, "select * from applianceinfoex", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		sqlite3_free(zErrMsg);
		sqlite3_exec(pSqlHandle, "DROP TABLE IF EXISTS 'applianceinfo'", 0, 0, &zErrMsg);
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				pDeviceList->onAddAppliancesInfo(new TypeApplianceInfo(atoi(dbresult[ncolumn + ncolumn * i + 0]), //appid
				                                                       atoi(dbresult[ncolumn + ncolumn * i + 1]), //irid
				                                                       atoi(dbresult[ncolumn + ncolumn * i + 2]), //irsubid
				                                                       dbresult[ncolumn + ncolumn * i + 3], //name
				                                                       dbresult[ncolumn + ncolumn * i + 4], //namu
				                                                       dbresult[ncolumn + ncolumn * i + 5], //model
				                                                       dbresult[ncolumn + ncolumn * i + 6], //version
				                                                       dbresult[ncolumn + ncolumn * i + 7], //serial
				                                                       atoi(dbresult[ncolumn + ncolumn * i + 8]), //roomID
				                                                       atoi(dbresult[ncolumn + ncolumn * i + 9]), //type
				                                                       atoi(dbresult[ncolumn + ncolumn * i + 10]), //value
				                                                       atoi(dbresult[ncolumn + ncolumn * i + 11]),  //addr
                                                                       dbresult[ncolumn + ncolumn * i + 12],  //value1
                                                                       dbresult[ncolumn + ncolumn * i + 13]  //config


				), 0);
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	//创建家电指令列表
	sqlite3_exec(pSqlHandle, "create table if not exists appliancecodeinfo(appid INTEGER, keyid INTEGER, codeid INTEGER, status INTEGER)", 0, 0, &zErrMsg);
	sqlite3_free(zErrMsg);
	//读取家电指令列表
	retInt = sqlite3_get_table(pSqlHandle, "select * from appliancecodeinfo", &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(retInt == SQLITE_OK)
	{
		if(nrow > 0)
		{
			for(int i = 0; i < nrow; i++)
			{
				//先找到这个家电
				TypeApplianceInfo *tempApplianceInfo = pDeviceList->onFindApplianceInfo(atoi(dbresult[ncolumn + ncolumn * i + 0]));
				if(tempApplianceInfo)
				{
					pDeviceList->onAddAppliancesCodeInfo(tempApplianceInfo, new TypeApplianceCodeInfo(atoi(dbresult[ncolumn + ncolumn * i + 0]), //apid
					                                                     atoi(dbresult[ncolumn + ncolumn * i + 1]), //keyid
					                                                     atoi(dbresult[ncolumn + ncolumn * i + 2]), //codeid
					                                                     atoi(dbresult[ncolumn + ncolumn * i + 3])  //status
					), 0);
				}
				else
				{
					sqlite3_free(zErrMsg);
					sprintf(tempBuff->buff, "delete from appliancecodeinfo where appid = %d", atoi(dbresult[ncolumn + ncolumn * i + 0]));
					sqlite3_exec(pSqlHandle, tempBuff->buff, 0, 0, &zErrMsg);//执行删除这条记录
				}
			}
		}
	}
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	delete tempBuff;
}

void TypeDataBase::onToString()
{
	mPrintf(Log_DataBase, "GatewayID: %lld ", gateway_id);
	mPrintf(Log_DataBase, "Status: %d ", status);
	mPrintf(Log_DataBase, "DBGateway:%d ", dbGateway);
	mPrintf(Log_DataBase, "Serial: %s ", serial->buff);
	mPrintf(Log_DataBase, "Ieee: %llx ", ieee);
	mPrintf(Log_DataBase, "IeeeEx: %llx ", ieee_ex);
	mPrintf(Log_DataBase, "FamilyID: %d ", family_id);
	mPrintf(Log_DataBase, "Ex_PanID: %llx ", ex_panid);
	mPrintf(Log_DataBase, "Ex_PanIDEx: %llx ", ex_panid_ex);
	mPrintf(Log_DataBase, "PanID: %04x ", panid);
	mPrintf(Log_DataBase, "PanIDEx: %04x ", panid_ex);
	mPrintf(Log_DataBase, "Channel: %d ", channel);
	mPrintf(Log_DataBase, "ChannelEx: %d ", channel_ex);
	mPrintf(Log_DataBase, "RoomID: %d ", room_id);
	mPrintf(Log_DataBase, "cc2538Ver: %d ", cc2538Ver);
	mPrintf(Log_DataBase, "jniVer: %s ", GATEWAY_SOFTVER);
	mPrintf(Log_DataBase, "cc2538md5: %s ", cc2538md5->buff);
	mPrintf(Log_DataBase, "time_zone: %s ", time_zone->buff);
	mPrintf(Log_DataBase, "language: %s ", language->buff);
	mPrintf(Log_DataBase, "name: %s ", name->buff);
	if(chipType == 2538)
	{
		mPrintf(Log_DataBase, "chipType:cc2538 ");
	}
	else if(chipType == 2530)
	{
		mPrintf(Log_DataBase, "chipType:cc2530 ");
	}
	else
	{
		mPrintf(Log_DataBase, "chipType:unknow ");
	}
	mPrintf(Log_DataBase, "SeverType: %s ", SERVER_DEBUG);
	mPrintf(Log_DataBase, "DEBUG: %s ", onCheckDebugMode() ? "TRUE" : "FALSE");
}

bool TypeDataBase::onClearDevEventInfo(int64_t keyid, int32_t subid, EmunEventFlag event)
{
	TypeDevEventInfo *tempDevEventInfo = NULL;
	for(int i = 0; i < devEventList->size(); ++i)
	{
		tempDevEventInfo = (TypeDevEventInfo *)devEventList->get(i);
		if((tempDevEventInfo->keyID == keyid) && (tempDevEventInfo->eventFlag == event))
		{
			if(subid < 1) subid = 1;
			if(subid > 4) subid = 4;
			tempDevEventInfo->subID &= ~(1 << subid);
			if(tempDevEventInfo->subID == 0)
			{
				tempDevEventInfo->eventFlag = Event_INIT;
			}
			break;
		}
	}
	return TRUE;
}

bool TypeDataBase::onAddDevEventInfo(int64_t keyid, int32_t subid, EmunEventFlag event, int32_t delaytime)
{
	bool retBool = FALSE;
	TypeDevEventInfo *tempDevEventInfo = NULL;
	for(int i = 0; i < devEventList->size(); ++i)
	{
		tempDevEventInfo = (TypeDevEventInfo *)devEventList->get(i);
		if((tempDevEventInfo->keyID == keyid) && (tempDevEventInfo->eventFlag == event))
		{
			if(subid < 1) subid = 1;
			tempDevEventInfo->subID |= (1 << subid);
			retBool = true;
			//mPrintf(Log_NetWork, "key = %d subID=%04x ", keyid, tempDevEventInfo->subID);
			break;
		}
		else
		{
			tempDevEventInfo = NULL;
		}
	}
	if(tempDevEventInfo == NULL)
	{
		tempDevEventInfo = new TypeDevEventInfo(keyid, subid, event);
		devEventList->add(tempDevEventInfo);
	}
	tempDevEventInfo->delyaTime = delaytime;
	return retBool;
}

int64_t TypeDataBase::onGetGateway_ID()
{
	return gateway_id;
}

bool TypeDataBase::onSetGateway_ID(int64_t value)
{
	if(gateway_id != value)
	{
		gateway_id = value;
		onSetGatewaySqlValue(tgateway_id, value, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetCC2538Ver()
{
	return cc2538Ver;
}

bool TypeDataBase::onSetCC2538Ver(int32_t ver)
{
	if(cc2538Ver != ver)
	{
		cc2538Ver = ver;
		onSetGatewaySqlValue(tcc2538ver, ver, 0, "");
		return true;
	}
	return false;
}
/*
bool TypeDataBase::onSetCC2538md5(char *md5)
{
	if(cc2538md5->onStringCMP(md5) == FALSE)
	{
		delete cc2538md5;
		cc2538md5 = new TypeChar(md5);
		onSetGatewaySqlValue(tcc2538md5, 0, 0, md5);
		return TRUE;
	}
	return FALSE;
}
*/
bool TypeDataBase::onSetChipType(int32_t type)
{
	if(chipType != type)
	{
		chipType = type;
		onSetGatewaySqlValue(tchiptype, type, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetChipType()
{
	return chipType;
}
/*
 bool TypeDataBase::onCMPSerial(const char *strvalue)
{
	if(serial->onStringCMP(strvalue))
	{
		return TRUE;
	}
	else
	{
		return FALSE;
	}
}

*/


int32_t TypeDataBase::onGetStatus()
{
	return status;
}

bool TypeDataBase::onSetStatus(int32_t value)
{
	if(status != value)
	{
		status = value;
		onSetGatewaySqlValue(tstatus, value, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetDBGateway()
{
	return dbGateway;
}

bool TypeDataBase::onSetDBGateway(int32_t value)
{
	if(dbGateway != value)
	{
		dbGateway = value;
		onSetGatewaySqlValue(tdbGateway, value, 0, "");
		return true;
	}
	return false;
}

char *TypeDataBase::onGetSerial()
{
	return serial->buff;
}

bool TypeDataBase::onSetSerial(char *strvalue)
{
	if(!serial->onStringCMP(strvalue))
	{
		serial->onClear();
		serial->onAddString(0, strvalue);
		onSetGatewaySqlValue(tserial, 0, 0, strvalue);
		return true;
	}
	return false;
}

int64_t TypeDataBase::onGetIEEE()
{
	return ieee;
}

bool TypeDataBase::onSetIEEE(int64_t value)
{
	if(ieee != value)
	{
		ieee = value;
		onSetGatewaySqlValue(tieee, value, 0, "");
		return true;
	}
	return false;
}

int64_t TypeDataBase::onGetIEEE_EX()
{
	return ieee_ex;
}

bool TypeDataBase::onSetIEEE_EX(int64_t value)
{
	if(ieee_ex != value)
	{
		ieee_ex = value;
		onSetGatewaySqlValue(tieee_ex, value, 0, "");
		return true;
	}
	return false;
}

char *TypeDataBase::onGetTime_Zone()
{
	return time_zone->buff;
}

bool TypeDataBase::onSetTime_Zone(char *strvalue)
{
	if(!time_zone->onStringCMP(strvalue))
	{
		time_zone->onClear();
		time_zone->onAddString(0, strvalue);
		onSetGatewaySqlValue(ttime_zone, 0, 0, strvalue);
		return true;
	}
	return false;
}

char *TypeDataBase::onGetLanguage()
{
	return language->buff;
}

bool TypeDataBase::onAddGasArmBingInfo(int64_t gaskey, int64_t gasarmkey, int32_t trandvalue)
{
	TypeGasArmBingInfo *tempGasArmBingInfo = NULL;
	for(int i = 0; i < gasArmBingList->size(); ++ i)
	{
		tempGasArmBingInfo = (TypeGasArmBingInfo *)gasArmBingList->get(i);
		if((tempGasArmBingInfo->gasKeyID == gaskey) && (tempGasArmBingInfo->gasArmKeyID == gasarmkey))
		{
			tempGasArmBingInfo->randValue = trandvalue;
			break;
		}
		else
		{
			tempGasArmBingInfo = NULL;
		}
	}
	if(tempGasArmBingInfo == NULL)
	{
		onSetGatewaySqlValue(tgasarmbing, gaskey, gasarmkey, "");//添加燃气对应关系
		gasArmBingList->add(new TypeGasArmBingInfo(gaskey, gasarmkey, trandvalue));
	}
	return TRUE;
}

bool TypeDataBase::onDeleteGasArmBingInfo(int32_t randvalue)
{
	TypeGasArmBingInfo *tempGasArmBingInfo = NULL;
	for(int i = 0; i < gasArmBingList->size(); )
	{
		tempGasArmBingInfo = (TypeGasArmBingInfo *)gasArmBingList->get(i);
		if(tempGasArmBingInfo->randValue != randvalue)
		{
			if(pSqlHandle == NULL) return FALSE;
			//删除咯
			//写个sql语句就好了
			TypeChar *retChars = new TypeChar();
			char *zErrMsg = NULL;
			sprintf(retChars->buff, "delete from gateway where type = %d and intvalue = %lld and intvalue_ex = %lld", tgasarmbing, tempGasArmBingInfo->gasKeyID, tempGasArmBingInfo->gasArmKeyID);
			sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//
			sqlite3_free(zErrMsg);
			delete retChars;
			//delete
			gasArmBingList->removeObject(tempGasArmBingInfo);
			continue;
		}
		i++;
	}
	return TRUE;
}

bool TypeDataBase::onDeleteGasArmBingInfo(int64_t gaskey, int64_t gasarmkey)
{
	TypeGasArmBingInfo *tempGasArmBingInfo = NULL;
	for(int i = 0; i < gasArmBingList->size(); ++ i)
	{
		tempGasArmBingInfo = (TypeGasArmBingInfo *)gasArmBingList->get(i);
		if((tempGasArmBingInfo->gasKeyID == gaskey) && (tempGasArmBingInfo->gasArmKeyID == gasarmkey))
		{
			if(pSqlHandle == NULL) return FALSE;
			//删除咯
			//写个sql语句就好了
			TypeChar *retChars = new TypeChar();
			char *zErrMsg = NULL;
			sprintf(retChars->buff, "delete from gateway where type = %d and intvalue = %lld and intvalue_ex = %lld", tgasarmbing, gaskey, gasarmkey);
			sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//
			sqlite3_free(zErrMsg);
			delete retChars;
			//delete
			gasArmBingList->removeObject(tempGasArmBingInfo);
			break;
		}
	}
	return TRUE;
}

bool TypeDataBase::onAlarmsGasArmBingInfo(int64_t gaskey)
{
	TypeGasArmBingInfo *tempGasArmBingInfo = NULL;
	for(int i = 0; i < gasArmBingList->size(); ++ i)
	{
		tempGasArmBingInfo = (TypeGasArmBingInfo *)gasArmBingList->get(i);
		if(tempGasArmBingInfo->gasKeyID == gaskey)
		{
			//找到对应的燃气臂 并发送关闭燃气臂
			TypeDBDeviceInfo *tempGasDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, tempGasArmBingInfo->gasKeyID);
			TypeDBDeviceInfo *tempArmDBDeviceInfo = pDeviceList->onCheckFamilyDeviceInfo(IDeviceID, tempGasArmBingInfo->gasArmKeyID);
			if(tempGasArmBingInfo && tempArmDBDeviceInfo && (tempGasDBDeviceInfo->devType == DEVICE_TYPE_GAS) && (tempArmDBDeviceInfo->devType == DEVICE_TYPE_GAS_ARM))
			{
				//可以关闭燃气臂了
				if(tempArmDBDeviceInfo->gatewayID == onGetGateway_ID())
				{
					if(tempArmDBDeviceInfo->shortAddr)
					{
						uint8_t tempStatus = 0;
						pmMasterSerialPort->onWriteZclCMD((uint32_t)tempArmDBDeviceInfo->shortAddr, 1, CLUSTER_ID_ONOFF, tempStatus, NULL, 0, 0);
					}
				}
				else
				{
					//发送一个远程控制指令
					pDeviceList->onSetDeviceStatus(tempArmDBDeviceInfo, 1, 0, TRUE);
				}
			}
		}
	}
	return TRUE;
}

bool TypeDataBase::onSetLanguage(char *strvalue)
{
	if(!language->onStringCMP(strvalue))
	{
		language->onClear();
		language->onAddString(0, strvalue);
		onSetGatewaySqlValue(tlanguage, 0, 0, strvalue);
		return true;
	}
	return false;
}

char *TypeDataBase::onGetName()
{
	return name->buff;
}

bool TypeDataBase::onSetName(char *strvalue)
{
	if(!name->onStringCMP(strvalue))
	{
		name->onClear();
		name->onAddString(0, strvalue);
		onSetGatewaySqlValue(tname, 0, 0, strvalue);
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetFamilyID()
{
	return family_id;
}

bool TypeDataBase::onSetFamilyID(int32_t value)
{
	if(family_id != value)
	{
		family_id = value;
		onSetGatewaySqlValue(tfamily_id, value, 0, "");
		return true;
	}
	return false;
}

int64_t TypeDataBase::onGetEx_PANID()
{
	return ex_panid;
}

bool TypeDataBase::onSetEx_PANID(int64_t value)
{
	if(ex_panid != value)
	{
		ex_panid = value;
		onSetGatewaySqlValue(tex_panid, value, 0, "");
		return true;
	}
	return false;
}
/*
int64_t TypeDataBase::onGetEx_PANID_Ex()
{
	return ex_panid_ex;
}*/

bool TypeDataBase::onSetEx_PANID_Ex(int64_t value)
{
	if(ex_panid_ex != value)
	{
		ex_panid_ex = value;
		onSetGatewaySqlValue(tex_panid_ex, value, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetPANID()
{
	return panid;
}

bool TypeDataBase::onSetPANID(int32_t value)
{
	if(panid != value)
	{
		panid = value;
		onSetGatewaySqlValue(tpanid, value, 0, "");
		return true;
	}
	return false;
}


int32_t TypeDataBase::onGetPANID_Ex()
{
	return panid_ex;
}

bool TypeDataBase::onSetPANID_Ex(int32_t value)
{
	if(panid_ex != value)
	{
		panid_ex = value;
		onSetGatewaySqlValue(tpanid_ex, value, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetChannel()
{
	return channel;
}

bool TypeDataBase::onSetChannel(int32_t value)
{
	if(channel != value)
	{
		channel = value;
		onSetGatewaySqlValue(tchannel, value, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetChannel_Ex()
{
	return channel_ex;
}

bool TypeDataBase::onSetChannel_Ex(int32_t value)
{
	if(channel_ex != value)
	{
		channel_ex = value;
		onSetGatewaySqlValue(tchannel_ex, value, 0, "");
		return true;
	}
	return false;
}

int32_t TypeDataBase::onGetRoomID()
{
	return room_id;
}

bool TypeDataBase::onSetRoomID(int32_t value)
{
	if(room_id != value)
	{
		room_id = value;
		onSetGatewaySqlValue(troom_id, value, 0, "");
		return true;
	}
	return false;
}

bool TypeDataBase::onDeleteDeviceInfoSqlValue(int32_t deviceid)
{
	char *zErrMsg = NULL;
	TypeChar *retChars = new TypeChar();
	sprintf(retChars->buff, "delete from dbdeviceinfo where id=%d", deviceid);
	sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//删除一条数据
	sqlite3_free(zErrMsg);
	//同时删除这个设备的子设备信息
	sprintf(retChars->buff, "delete from subdeviceinfo where devid=%d", deviceid);
	sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//删除一条数据
	sqlite3_free(zErrMsg);
	delete retChars;
	return TRUE;
}
/*
bool TypeDataBase::onDeleteGatewaySqlValue(int32_t type, int64_t intvalue)
{
	if(pSqlHandle == NULL) return FALSE;
	//删除咯
	//写个sql语句就好了
	TypeChar *retChars = new TypeChar();
	char *zErrMsg = NULL;
	sprintf(retChars->buff, "delete from 'gateway' where type = %d and intvalue = %lld", type, intvalue);
	sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//
	sqlite3_free(zErrMsg);
	delete retChars;
	return true;
}
*/
bool TypeDataBase::onUpdateApplianceCodeInfo(TypeApplianceCodeInfo *appliancecodeinfo, int32_t type, int32_t value)
{
	if(appliancecodeinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case ApplianceCMDInset://插入新的数据
			{
				//添加记录到场景
				sprintf(tempSql->buff, "insert into appliancecodeinfo(appid, keyid, codeid, status) values "
						"(%d, %d, %d, %d)", appliancecodeinfo->appID, appliancecodeinfo->key_id, appliancecodeinfo->ir_code, appliancecodeinfo->status);
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			case ApplianceCMDKeyID: appliancecodeinfo->key_id = value; tempDBName = new TypeChar("keyid"); break;//
			case ApplianceCMDIrCode: appliancecodeinfo->ir_code = value; tempDBName = new TypeChar("codeid"); break;//
			case ApplianceCMDStatus : appliancecodeinfo->status = value; tempDBName = new TypeChar("status"); break;//
			default:mPrintf(Log_DataBase, "Error: appliancecmdinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update appliancecodeinfo set %s = %d where appID = %d and keyid = %d", tempDBName->buff, value, appliancecodeinfo->appID, appliancecodeinfo->key_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateApplianceInfo(TypeApplianceInfo *applianceinfo, int32_t type, int32_t value)
{
	if(applianceinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case ApplianceInset://插入新的数据
			{
				//添加记录到场景
				sprintf(tempSql->buff, "insert into applianceinfoex(appid, irid, subid, name, manu, model, version, serial, roomid, type, value, addr) values "
						"(%d, %d, %d, '%s', '%s', '%s', '%s', '%s', %d, %d, %d, %d)", applianceinfo->appID, applianceinfo->ir_id, applianceinfo->ir_sub_id, applianceinfo->name->buff, applianceinfo->manufacturer->buff, applianceinfo->model->buff, applianceinfo->version->buff, applianceinfo->serial->buff, applianceinfo->roomID, applianceinfo->type, applianceinfo->value, applianceinfo->addr);
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			case ApplianceIrID: applianceinfo->ir_id = value; tempDBName = new TypeChar("irid"); break;//
			case ApplianceIrSubID: applianceinfo->ir_sub_id = value; tempDBName = new TypeChar("subid"); break;//
			case ApplianceRoomID: applianceinfo->roomID = value; tempDBName = new TypeChar("roomid"); break;//
			case ApplianceTType : applianceinfo->type = value; tempDBName = new TypeChar("type"); break;//
			case ApplianceValue: applianceinfo->value = value; tempDBName = new TypeChar("value"); break;//
			case ApplianceAddr: applianceinfo->addr = value; tempDBName = new TypeChar("addr"); break;//
			default:mPrintf(Log_DataBase, "Error: applianceinfoex 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update applianceinfoex set %s = %d where appid = %d", tempDBName->buff, value, applianceinfo->appID);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateApplianceInfo(TypeApplianceInfo *applianceinfo, int32_t type, const char *value)
{
	if(applianceinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case ApplianceName: delete applianceinfo->name; applianceinfo->name = new TypeChar(value); tempDBName = new TypeChar("name"); break;//
			case ApplianceManufacturer: delete applianceinfo->manufacturer; applianceinfo->manufacturer = new TypeChar(value); tempDBName = new TypeChar("manu"); break;//
			case ApplianceModelType: delete applianceinfo->model; applianceinfo->model = new TypeChar(value); tempDBName = new TypeChar("model"); break;//
			case ApplianceVersion: delete applianceinfo->version; applianceinfo->version = new TypeChar(value); tempDBName = new TypeChar("version"); break;//
			case ApplianceSerial:  delete applianceinfo->serial; applianceinfo->serial = new TypeChar(value); tempDBName = new TypeChar("serial"); break;//
			default:mPrintf(Log_DataBase, "Error: applianceinfoex 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update applianceinfoex set %s = '%s' where appid = %d", tempDBName->buff, value, applianceinfo->appID);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSceneActionInfo(TypeSceneActionInfo *sceneactioninfo, int32_t type, int32_t value)
{
	if(sceneactioninfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SceneActionInset://插入新的数据
			{
				//添加记录到场景
				sprintf(tempSql->buff, "insert into sceneactioninfo(actionid ,sceneid, type, devid, subid, actiontype, action, actiondesc, delaytime) values "
						"(%lld, %lld, %d, %d, %d, %d, %d, '%s', %d)", sceneactioninfo->scene_action_id, sceneactioninfo->scene_id, sceneactioninfo->type, sceneactioninfo->device_id, sceneactioninfo->sub_id, sceneactioninfo->action_type, sceneactioninfo->action, sceneactioninfo->action_desc->buff, sceneactioninfo->onGetDelayTime());
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			//case SceneActionType: sceneactioninfo->type = value; tempDBName = new TypeChar("type"); break;//
			//case SceneActionDeviceID: sceneactioninfo->device_id = value; tempDBName = new TypeChar("devid"); break;//
			case SceneActionSubID: sceneactioninfo->sub_id = value; tempDBName = new TypeChar("subid"); break;//
			//case SceneActionActionType: sceneactioninfo->action_type = value; tempDBName = new TypeChar("actiontype"); break;//
			case SceneActionAction: sceneactioninfo->action = value; tempDBName = new TypeChar("action"); break;//
			case SceneActionDelayTime: sceneactioninfo->onSetDelayTime(value); tempDBName = new TypeChar("delaytime"); break;//
			default:mPrintf(Log_DataBase, "Error: sceneactioninfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update sceneactioninfo set %s = %d where actionid = %lld", tempDBName->buff, value, sceneactioninfo->scene_action_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSceneActionInfo(TypeSceneActionInfo *sceneactioninfo, int32_t type, char *value)
{
	if(sceneactioninfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SceneActionActionDesc: delete sceneactioninfo->action_desc; sceneactioninfo->action_desc = new TypeChar(value); tempDBName = new TypeChar("actiondesc"); break;//
			default:mPrintf(Log_DataBase, "Error: sceneactioninfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update sceneactioninfo set %s = '%s' where actionid = %lld", tempDBName->buff, value, sceneactioninfo->scene_action_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onDeleteDataBase(const char *dbname, const char *fieldname, int64_t value)
{
	char *zErrMsg = NULL;
	TypeChar *tempSql = new TypeChar();
	sprintf(tempSql->buff, "delete from %s where %s = %lld", dbname, fieldname, value);
	sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//执行删除这条记录
	sqlite3_free(zErrMsg);
	delete tempSql;
	return TRUE;
}

bool TypeDataBase::onDeleteApplianceCode(int32_t appid, int32_t keyid)
{
	char *zErrMsg = NULL;
	TypeChar *tempSql = new TypeChar();
	sprintf(tempSql->buff, "delete from appliancecodeinfo where appid = %d and keyid = %d", appid, keyid);
	sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//执行删除这条记录
	sqlite3_free(zErrMsg);
	delete tempSql;
	return TRUE;
}

bool TypeDataBase::onUpdateSceneCondInfo(TypeSceneCondInfo *sceneacondinfo, int32_t type, int32_t value)
{
	if(sceneacondinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SceneCondInset://插入新的数据
			{
				//添加记录到场景
				sprintf(tempSql->buff, "insert into scenecondinfo(condid, sceneid, type, condtype, condexpre, devid, subid, action, actiondesc, delaytime) values "
						"(%lld, %lld, %d, %d, '%s', %d, %d, %d, '%s', %d)", sceneacondinfo->scene_cond_id, sceneacondinfo->scene_id, sceneacondinfo->type, sceneacondinfo->cond_type, sceneacondinfo->cond_expre->buff, sceneacondinfo->device_id, sceneacondinfo->sub_id, sceneacondinfo->action, sceneacondinfo->action_desc->buff, sceneacondinfo->onGetDelayTime());
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			case SceneCondAction: sceneacondinfo->action = value; tempDBName = new TypeChar("action"); break;//
			case SceneCondDelayTime: sceneacondinfo->onSetDelayTime(value); tempDBName = new TypeChar("delaytime"); break;//
			default:mPrintf(Log_DataBase, "Error: scenecondinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update scenecondinfo set %s = %d where condid = %lld", tempDBName->buff, value, sceneacondinfo->scene_cond_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSceneCondInfo(TypeSceneCondInfo *sceneacondinfo, int32_t type, char *value)
{
	if(sceneacondinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SceneCondActionDesc: delete sceneacondinfo->action_desc; sceneacondinfo->action_desc = new TypeChar(value); tempDBName = new TypeChar("actiondesc"); break;//
			default:mPrintf(Log_DataBase, "Error: scenecondinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update scenecondinfo set %s = '%s' where condid = %lld", tempDBName->buff, value, sceneacondinfo->scene_cond_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSceneNameInfo(TypeSceneNameInfo *scenenameinfo, int32_t type, int32_t value)
{
	if(scenenameinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SceneInset://插入新的数据
			{
				//添加记录到场景
				sprintf(tempSql->buff, "insert into scenenameinfo(sceneid, name, roomid, iconid, specialized, disabled, hidden, status, period, enable_time ) values "
						"(%lld, '%s', %d, %d, %d, %d, %d, %d, '%s', '%s')", scenenameinfo->scene_id, scenenameinfo->name->buff, scenenameinfo->room_id, scenenameinfo->icon_id, scenenameinfo->specialized, scenenameinfo->disabled, scenenameinfo->hidden, scenenameinfo->status, scenenameinfo->period->buff, scenenameinfo->enabledTime->buff);
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			case SceneRoomID: scenenameinfo->room_id = value; tempDBName = new TypeChar("roomid"); break;//修改房间ID
			case SceneIconID: scenenameinfo->icon_id = value; tempDBName = new TypeChar("iconid"); break;//修改图标ID
			case SceneSpecialized: scenenameinfo->specialized = value; tempDBName = new TypeChar("specialized"); break;//修改特殊标志
			case SceneDisabled: scenenameinfo->disabled = value; tempDBName = new TypeChar("disabled"); break;//修改使用值
			case SceneHidden: scenenameinfo->hidden = value; tempDBName = new TypeChar("hidden"); break;//修改使用值
			default:mPrintf(Log_DataBase, "Error: scenenameinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update scenenameinfo set %s = %d where sceneid = %lld", tempDBName->buff, value, scenenameinfo->scene_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSceneNameInfo(TypeSceneNameInfo *scenenameinfo, int32_t type, const char *value)
{
	if(scenenameinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SceneName: delete scenenameinfo->name; scenenameinfo->name = new TypeChar(value); tempDBName = new TypeChar("name"); break;//修改场景名称
			case ScenePeriod: delete scenenameinfo->period; scenenameinfo->period = new TypeChar(value); tempDBName = new TypeChar("period"); break;//
			case SceneEnableTime: delete scenenameinfo->enabledTime; scenenameinfo->enabledTime = new TypeChar(value); tempDBName = new TypeChar("enable_time"); break;//
			default:mPrintf(Log_DataBase, "Error: scenenameinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update scenenameinfo set %s = '%s' where sceneid = %lld", tempDBName->buff, value, scenenameinfo->scene_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateRoomInfo(TypeRoomInfo * roominfo, int32_t type, int32_t value)
{
	if(roominfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case RoomInset://插入新的数据
			{
				//再添加这条数据
				sprintf(tempSql->buff, "insert into roominfo(roomid, iconid, tempvalue, illuvalue, name) values "
						"(%d, %d, %d, %d, '%s')", roominfo->room_id, roominfo->iconID, roominfo->temp_value, roominfo->illum_value, roominfo->name->buff);
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			case RoomIcon: roominfo->iconID = value; tempDBName = new TypeChar("iconid");break;//
			default:mPrintf(Log_DataBase, "Error: roominfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update roominfo set %s = %d where roomid = %d", tempDBName->buff, value, roominfo->room_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateRoomInfo(TypeRoomInfo * roominfo, int32_t type, const char *value)
{
	if(roominfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case RoomName: delete roominfo->name; roominfo->name = new TypeChar(value); tempDBName = new TypeChar("name");break;//
			default:mPrintf(Log_DataBase, "Error: roominfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update roominfo set %s = '%s' where roomid = %d", tempDBName->buff, value, roominfo->room_id);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSubDeviceInfo(TypeDeviceTypeInfo *devicetypeinfo, int32_t type, const char *value)
{
	if(devicetypeinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar();
		switch(type)
		{
			case SubName: delete devicetypeinfo->name; devicetypeinfo->name = new TypeChar(value); tempDBName = new TypeChar("name"); break;//
			case SubSaveName: delete devicetypeinfo->saveName; devicetypeinfo->saveName = new TypeChar(value); tempDBName = new TypeChar("savename"); break;//
			default:mPrintf(Log_DataBase, "Error: subdeviceinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update subdeviceinfo set %s = '%s' where devid = %d and subid = %d", tempDBName->buff, value, devicetypeinfo->deviceID, devicetypeinfo->subID);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

bool TypeDataBase::onUpdateSubDeviceInfo(TypeDeviceTypeInfo *devicetypeinfo, int32_t type, int64_t value)
{
	if(devicetypeinfo)
	{
		TypeChar *tempDBName = NULL;
		char *zErrMsg = NULL;
		TypeChar *tempSql = new TypeChar(512);
		switch(type)
		{
			case SubInset://插入新的数据
			{
				TypeChar *statusSql = new TypeChar(512);
				//再添加这条数据
				sprintf(tempSql->buff, "insert into subdeviceinfo(devid, subid, roomid, iconid, saveiconid, name, savename, subtype, intvalue1, intvalue2, intvalue3, intvalue4, intvalue5, intvalue6, strvalue1, strvalue2) values "
						"(%d, %d, %d, %d, %d, '%s', '%s', %d, %s)", devicetypeinfo->deviceID, devicetypeinfo->subID, devicetypeinfo->roomID, devicetypeinfo->iconID, devicetypeinfo->saveIconID, devicetypeinfo->name->buff, devicetypeinfo->saveName->buff, devicetypeinfo->devType, devicetypeinfo->onGetStatusSql(statusSql));
				delete statusSql;
				sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//添加这条记录
				sqlite3_free(zErrMsg);
			}
				break;
			case SubRoomID: devicetypeinfo->roomID = ((int32_t) value); tempDBName = new TypeChar("roomid"); break;//修改房间ID
			case SubIconID: devicetypeinfo->iconID = ((int32_t) value); tempDBName = new TypeChar("iconid"); break;//修改图标ID
			case SubSaveIconID: devicetypeinfo->saveIconID = ((int32_t) value); tempDBName = new TypeChar("saveiconid"); break;//修改图标ID
			case SubDevStatus: tempDBName = new TypeChar("intvalue1");break;//SubDevStatus
			case SubLightSceneID:tempDBName = new TypeChar("intvalue2");break;//SubPower
			case SubZone:tempDBName = new TypeChar("intvalue3");break;//
			case SubSecurity:tempDBName = new TypeChar("intvalue4");break;//
			case SubPIRDelayTime:tempDBName = new TypeChar("intvalue5");break;//
			default:mPrintf(Log_DataBase, "Error: subDeviceInfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			sprintf(tempSql->buff, "update subdeviceinfo set %s = %lld where devid = %d and subid = %d", tempDBName->buff, value, devicetypeinfo->deviceID, devicetypeinfo->subID);
			sqlite3_exec(pSqlHandle, tempSql->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete tempDBName;
		}
		delete tempSql;
	}
	return TRUE;
}

//所有低功耗都有zoneID
uint8_t TypeDataBase::onGetZoneID(int64_t tieee)
{
	//先随机一个8位数值
	uint8_t tempZoneID = 0;
	TypeDBDeviceInfo *tempDBDeviceInfo = pDeviceList->onCheckGatewayDeviceInfo(IIeee, tieee);
	if(tempDBDeviceInfo && onCheckBattery(tempDBDeviceInfo->devType))
	{
		tempZoneID = (uint8_t)tempDBDeviceInfo->shortAddr_ex;
		if(tempDBDeviceInfo->shortAddr_ex == 0)
		{
			//循环从列表中找个合适的值
			while(!tempZoneID)
			{
				tempZoneID = (uint8_t)random();
				if(tempZoneID)
				{
					for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
					{
						tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
						if(tempDBDeviceInfo && onCheckBattery(tempDBDeviceInfo->devType))
						{
							if((tempDBDeviceInfo->shortAddr_ex & 0xFF) == tempZoneID)
							{
								break;
							}
						}
					}
				}
			}
			//这里出来应该是找到了一个正确的zoneID了
			if(tempZoneID)
			{
				//写入相应的设备中
				for(int i = 0; i < pDeviceList->dbDeviceInfoList->size(); ++ i)
				{
					tempDBDeviceInfo = (TypeDBDeviceInfo *)pDeviceList->dbDeviceInfoList->get(i);
					if(tempDBDeviceInfo && onCheckBattery(tempDBDeviceInfo->devType) && (tieee == tempDBDeviceInfo->ieee))
					{
						//并且同时保存在shortAddr_ex里面
						onUpdateDeviceInfoSqlValue(tempDBDeviceInfo, IShortAddr_Ex, tempZoneID & 0xFF);
						//同时更新到服务器
						onUpdateDeviceInfo(tempDBDeviceInfo->deviceID, tempDBDeviceInfo->shortAddr, tempDBDeviceInfo->shortAddr_ex);
						break;
					}
				}
			}
		}
	}
	return tempZoneID;
}

bool TypeDataBase::onUpdateDeviceInfoSqlValue(TypeDBDeviceInfo *dbdeviceinfo, int32_t type, int64_t value)
{
	//先更新下数据临时表
	if(dbdeviceinfo)
	{
		TypeChar *tempDBName = NULL;
		switch(type)
		{
			case IInset://插入数据
			{
				//数据库中没有这份白名单 添加这个白名单
				//不存在就添加
				TypeChar *retChars = new TypeChar(512);
				char *zErrMsg = NULL;
				sprintf(retChars->buff, "insert into dbdeviceinfo(id , gatewayid, devtype, ieee, addr, ieee_ex, addr_ex, rgb, savergb, online, protocol, protocolver, targetscreen,  attr, serial, swver, hwver, manufacturer, subcount) values (%d, %lld, %d, %lld, %d, %lld, %d, %lld, %lld, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', %d)", dbdeviceinfo->deviceID, dbdeviceinfo->gatewayID, dbdeviceinfo->devType, dbdeviceinfo->ieee, dbdeviceinfo->shortAddr, dbdeviceinfo->ieee_ex, dbdeviceinfo->shortAddr_ex,
				        dbdeviceinfo->rgb, dbdeviceinfo->saveRgb, dbdeviceinfo->onLineFlag.value, dbdeviceinfo->protocol, dbdeviceinfo->protocolVer, dbdeviceinfo->targetScreen, dbdeviceinfo->attr->value, dbdeviceinfo->serial->buff, dbdeviceinfo->swVer->buff, dbdeviceinfo->hwVer->buff, dbdeviceinfo->manufacturer->buff, dbdeviceinfo->subCount);
				//mPrintf(Log_DataBase, "%s", retChars->buff);
				sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//插入一条数据
				sqlite3_free(zErrMsg);
				delete retChars;
			}
				break;
			case IGatewayID:dbdeviceinfo->gatewayID = (uint64_t)value; tempDBName = new TypeChar("gatewayid");break;
			case IShortAddr:dbdeviceinfo->shortAddr = (int32_t)value; tempDBName = new TypeChar("addr");break;
			case IShortAddr_Ex:dbdeviceinfo->shortAddr_ex = (int32_t)value; tempDBName = new TypeChar("addr_ex");break;
			case IRgb:dbdeviceinfo->rgb = (uint64_t)value; tempDBName = new TypeChar("rgb");break;
			case ISaveRgb:dbdeviceinfo->saveRgb = (uint64_t)value; tempDBName = new TypeChar("savergb");break;
			case IOnline:dbdeviceinfo->onLineFlag.value = (int32_t)value; tempDBName = new TypeChar("online");break;
			case IProtocol:dbdeviceinfo->protocol = (int32_t)value; tempDBName = new TypeChar("protocol");break;
			case IProtocolVe:dbdeviceinfo->protocolVer = (int32_t)value; tempDBName = new TypeChar("protocolver");break;
			case ITargetScreen:dbdeviceinfo->targetScreen = (int32_t)value; tempDBName = new TypeChar("targetscreen");break;
			case IAttr:dbdeviceinfo->attr->value = (int32_t)value; tempDBName = new TypeChar("attr");break;
			case ISubCount:dbdeviceinfo->subCount = (int32_t)value; tempDBName = new TypeChar("subcount");break;
			default:mPrintf(Log_DataBase, "Error: dbdeviceinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			TypeChar *retChars = new TypeChar();
			char *zErrMsg = NULL;
			sprintf(retChars->buff, "update dbdeviceinfo set %s = %lld where id = %d", tempDBName->buff, (int64_t)value, dbdeviceinfo->deviceID);
			sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete retChars;
			delete tempDBName;
		}
	}
	return TRUE;
}
/*
bool TypeDataBase::onUpdateDeviceInfoSqlValue(TypeDBDeviceInfo *dbdeviceinfo, int32_t type, const char *value)
{
	//先更新下数据临时表
	if(dbdeviceinfo)
	{
		TypeChar *tempDBName = NULL;
		switch(type)
		{
			case ISerial:delete dbdeviceinfo->serial; dbdeviceinfo->serial = new TypeChar(value); tempDBName = new TypeChar("serial");break;//
			case ISwVer:delete dbdeviceinfo->swVer; dbdeviceinfo->swVer = new TypeChar(value); tempDBName = new TypeChar("swver");break;//
			case IHwVer:delete dbdeviceinfo->hwVer; dbdeviceinfo->hwVer = new TypeChar(value); tempDBName = new TypeChar("hwver");break;//
			case IManufacturer:delete dbdeviceinfo->manufacturer; dbdeviceinfo->manufacturer = new TypeChar(value); tempDBName = new TypeChar("manufacturer");break;//
			default:mPrintf(Log_DataBase, "Error: dbdeviceinfo 未处理类型的更新操作! ");break;
		}
		if(tempDBName)
		{
			TypeChar *retChars = new TypeChar();
			char *zErrMsg = NULL;
			sprintf(retChars->buff, "update dbdeviceinfo set '%s' = '%s' where id = %d", tempDBName->buff, (char *)value, dbdeviceinfo->deviceID);
			sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//更新一条数据
			sqlite3_free(zErrMsg);
			delete retChars;
			delete tempDBName;
		}
	}
	return TRUE;
}
*/
bool TypeDataBase::onSetGatewaySqlValue(int32_t type, int64_t intvalue, int64_t intvalue_ex, const char *strvalue)
{
	if(pSqlHandle == NULL) return FALSE;
	char **dbresult;
	int nrow, ncolumn;
	int ret = 0;
	char *zErrMsg = NULL;
	TypeChar *retChars = new TypeChar();
	if(onGetGatewayType(type) == 1)
	{
		sprintf(retChars->buff, "select * from gateway where type = %d", type);
	}
	else if(onGetGatewayType(type) == 2)
	{
		sprintf(retChars->buff, "select * from gateway where type = %d and intvalue = %lld", type, intvalue);
	}
	else
	{
		sprintf(retChars->buff, "select * from gateway where type = %d and intvalue = %lld and intvalue_ex = %lld", type, intvalue, intvalue_ex);
	}
	ret = sqlite3_get_table(pSqlHandle, retChars->buff, &dbresult, &nrow, &ncolumn, &zErrMsg);
	if(ret == SQLITE_OK)
	{
		sqlite3_free(zErrMsg);
		if(nrow > 0)
		{
			if(onGetGatewayType(type) == 1)
			{
				sprintf(retChars->buff, "update gateway set intvalue = %lld, intvalue_ex = %lld, strvalue = '%s' where type = %d", intvalue, intvalue_ex, strvalue, type);
			}
			else if(onGetGatewayType(type) == 2)
			{
				sprintf(retChars->buff, "update gateway set intvalue_ex = %lld, strvalue = '%s' where type = %d and intvalue = %lld", intvalue_ex, strvalue, type, intvalue);
			}
			else
			{
				sprintf(retChars->buff, "update gateway set strvalue = '%s' where type = %d and intvalue = %lld and intvalue_ex = %lld", strvalue, type, intvalue, intvalue_ex);
			}
			sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//更新一条数据
		}
		else
		{
			//unexit add
			sprintf(retChars->buff, "insert into gateway(id, type, intvalue, intvalue_ex, strvalue) values (null, %d, %lld, %lld, '%s')", type, intvalue, intvalue_ex, strvalue);
			sqlite3_exec(pSqlHandle, retChars->buff, 0, 0, &zErrMsg);//插入一条数据
		}
	}
	else
	{
		mPrintf(Log_DataBase, "Error:DataBase Non-existent! type=%d ", type);
	}
	delete retChars;
	sqlite3_free_table(dbresult);
	sqlite3_free(zErrMsg);
	return true;
}

int32_t TypeDataBase::onGetGatewayType(int32_t type)
{
	int32_t retInt = 1;
	switch(type)
	{
		case tgateway_id:
		case tstatus:
		case tdbGateway:
		case tserial:
		case tieee:
		case tieee_ex:
		case tfamily_id:
		case tex_panid:
		case tex_panid_ex:
		case tpanid:
		case tpanid_ex:
		case tchannel:
		case tchannel_ex:
		case troom_id:
		case ttime_zone:
		case tlanguage:
		case tname:
		case tmodel:
			retInt = 1;
			break;
		case tdevname://设备名称
		case ticon://设备图标
		case tgasarmbing://燃气绑定表
			retInt = 3;
			break;
		default:
			break;
	}
	return retInt;
}

bool TypeDataBase::onCloseDataBase()
{
	if(pSqlHandle != NULL)
	{
		sqlite3_close(pSqlHandle);
		pSqlHandle = NULL;
	}
	return false;
}

TypeDataBase::~TypeDataBase()
{
	delete serial;
	delete name;
	delete time_zone;
	delete language;
	delete devEventList;
	delete cc2538md5;
	delete gasArmBingList;
	onCloseDataBase();
}
