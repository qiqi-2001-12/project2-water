//
// Created by xia_w on 2017/12/11.
//


#ifndef SMARTHOME_SERIALCMDDEFINE_H
#define SMARTHOME_SERIALCMDDEFINE_H
#define DEFAULT_CHANLIST_26 0x04000000  // 26 - 0x1A
#define DEFAULT_CHANLIST_25 0x02000000  // 25 - 0x19
#define DEFAULT_CHANLIST_24 0x01000000  // 24 - 0x18
#define DEFAULT_CHANLIST_23 0x00800000  // 23 - 0x17
#define DEFAULT_CHANLIST_22 0x00400000  // 22 - 0x16
#define DEFAULT_CHANLIST_21 0x00200000  // 21 - 0x15
#define DEFAULT_CHANLIST_20 0x00100000  // 20 - 0x14
#define DEFAULT_CHANLIST_19 0x00080000  // 19 - 0x13
#define DEFAULT_CHANLIST_18 0x00040000  // 18 - 0x12
#define DEFAULT_CHANLIST_17 0x00020000  // 17 - 0x11
#define DEFAULT_CHANLIST_16 0x00010000  // 16 - 0x10
#define DEFAULT_CHANLIST_15 0x00008000  // 15 - 0x0F
#define DEFAULT_CHANLIST_14 0x00004000  // 14 - 0x0E
#define DEFAULT_CHANLIST_13 0x00002000  // 13 - 0x0D
#define DEFAULT_CHANLIST_12 0x00001000  // 12 - 0x0C
#define DEFAULT_CHANLIST_11 0x00000800  // 11 - 0x0B

#define DEFAULT_TC_SLAVE_LINK_KEY    {0x5d, 0x39, 0x37, 0xa2, 0x64, 0xd5, 0x47, 0x8c, 0x88, 0x99, 0x88, 0x4e, 0x65, 0xe5, 0x3e, 0x30}
#define DEFAULT_TC_MASTER_LINK_KEY   {0x5a, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6c, 0x6c, 0x69, 0x61, 0x6e, 0x63, 0x65, 0x30, 0x39}
#define DEFAULT_TC_MASTER_CHANNEL (DEFAULT_CHANLIST_19 | DEFAULT_CHANLIST_20 | DEFAULT_CHANLIST_24 | DEFAULT_CHANLIST_25 | DEFAULT_CHANLIST_26)
#define DEFAULT_TC_SLAVE_CHANNEL  (DEFAULT_CHANLIST_11 | DEFAULT_CHANLIST_14 | DEFAULT_CHANLIST_15)
#define SEC_KEY_LEN  16  // 128/8 octets (128-bit key is standard for ZigBee)
typedef enum {
	MT_RPC_CMD_POLL = 0x00,
	MT_RPC_CMD_SREQ = 0x20,
	MT_RPC_CMD_AREQ = 0x40,
	MT_RPC_CMD_SRSP = 0x60,
	MT_RPC_CMD_RES4 = 0x80,
	MT_RPC_CMD_RES5 = 0xA0,
	MT_RPC_CMD_RES6 = 0xC0,
	MT_RPC_CMD_RES7 = 0xE0
} mtRpcCmdType_t;

typedef enum {
	MT_RPC_SYS_RES0,   /* Reserved. */
	MT_RPC_SYS_SYS,
	MT_RPC_SYS_MAC,
	MT_RPC_SYS_NWK,
	MT_RPC_SYS_AF,
	MT_RPC_SYS_ZDO,
	MT_RPC_SYS_SAPI,   /* Simple API. */
	MT_RPC_SYS_UTIL,
	MT_RPC_SYS_DBG,
	MT_RPC_SYS_APP,
	MT_RPC_SYS_OTA,
	MT_RPC_SYS_ZNP,
	MT_RPC_SYS_SPARE_12,
	MT_RPC_SYS_UBL = 13,  // 13 to be compatible with existing RemoTI.
	MT_RPC_SYS_RES14,
	MT_RPC_SYS_APP_CNF,
	MT_RPC_SYS_RES16,
	MT_RPC_SYS_PROTOBUF,
	MT_RPC_SYS_RES18,  // RPC_SYS_PB_NWK_MGR
	MT_RPC_SYS_RES19,  // RPC_SYS_PB_GW
	MT_RPC_SYS_RES20,  // RPC_SYS_PB_OTA_MGR
	MT_RPC_SYS_GP = 21,
	MT_RPC_SYS_MAX     /* Maximum value, must be last */
	/* 22-32 available, not yet assigned. */
} mtRpcSysType_t;

typedef enum {
	SEND_INIT,
	SEND_WAIT_RES,
	SEND_WAIT_CONFIRM,
	SEND_DELETE
} StatusSerialProces;

#define APP_WHITE_LIST_SIZE                100
#define USER_ENDPOINTNUM                   1
#define SHORTADDR_BROADCAST                0xFFFC
#define AF_WILDCARD_PROFILEID              0x02   // Will force the message to use Wildcard ProfileID
#define AF_PREPROCESS                      0x04   // Will force APS to callback to preprocess before calling NWK layer
#define AF_LIMIT_CONCENTRATOR              0x08
#define AF_ACK_REQUEST                     0x10
#define AF_DISCV_ROUTE  			       0x20   // Supress Route Discovery for intermediate routes
// (route discovery preformed for initiating device)
#define AF_EN_SECURITY                     0x40
#define AF_SKIP_ROUTING                    0x80
#define BEACON_MAX_DEPTH           0x0F
#define DEF_NWK_RADIUS           BEACON_MAX_DEPTH

/***************************************************************************************************
 * APP COMMANDS
 ***************************************************************************************************/

/* SREQ/SRSP: */
#define MT_APP_MSG                           0x00
#define MT_APP_USER_TEST                     0x01
#define MT_APP_PB_ZCL_MSG                    0x02
#define MT_APP_PB_ZCL_CFG                    0x03
#define MT_APP_CHECK_WHITE_LIST              0x10

#define MT_APP_UGET_DEVINFO                  0x54
#define MT_USER_DELETE_SRC_ENTRY             0x55
#define MT_USER_DELETE_SUB_DEV               0x56
#define MT_USER_DELETE_NEIGHBOR              0x57
#define MT_USER_DELETE_ENTRY                 0x58
#define MT_USER_GET_SUB_IEEE                 0x59



/***************************************************************************************************
 * APP CONFIG COMMANDS
 ***************************************************************************************************/

#define MT_APP_CNF_SET_DEFAULT_REMOTE_ENDDEVICE_TIMEOUT    0x01
#define MT_APP_CNF_SET_ENDDEVICETIMEOUT                    0x02
#define MT_APP_CNF_SET_ALLOWREJOIN_TC_POLICY               0x03
#define MT_APP_CNF_BDB_ADD_INSTALLCODE                     0x04
#define MT_APP_CNF_BDB_START_COMMISSIONING                 0x05
#define MT_APP_CNF_BDB_SET_JOINUSESINSTALLCODEKEY          0x06
#define MT_APP_CNF_BDB_SET_ACTIVE_DEFAULT_CENTRALIZED_KEY  0x07
#define MT_APP_CNF_BDB_SET_CHANNEL                         0x08
#define MT_APP_CNF_BDB_SET_TC_REQUIRE_KEY_EXCHANGE         0x09
#define MT_APP_CNF_BDB_ZED_ATTEMPT_RECOVER_NWK             0x0A

#define MT_APP_CNF_BDB_COMMISSIONING_NOTIFICATION          0x80
//Application debug commands
#define MT_APP_CNF_SET_NWK_FRAME_COUNTER                   0xFF

/***************************************************************************************************
 * ZDO COMMANDS
 ***************************************************************************************************/

/* SREQ/SRSP */
#define MT_ZDO_NWK_ADDR_REQ                  0x00//根据IEEE地址请求短地址
#define MT_ZDO_IEEE_ADDR_REQ                 0x01//根据短地址请求IEEE地址
#define MT_ZDO_NODE_DESC_REQ                 0x02//查询设备的端点描述符信息
#define MT_ZDO_POWER_DESC_REQ                0x03//查询设备的电源描述符信息
#define MT_ZDO_SIMPLE_DESC_REQ               0x04//
#define MT_ZDO_ACTIVE_EP_REQ                 0x05//得到活动的端点列表
#define MT_ZDO_MATCH_DESC_REQ                0x06//得到设备的匹配描述符
#define MT_ZDO_COMPLEX_DESC_REQ              0x07
#define MT_ZDO_USER_DESC_REQ                 0x08
#define MT_ZDO_END_DEV_ANNCE                 0x0A
#define MT_ZDO_USER_DESC_SET                 0x0B
#define MT_ZDO_SERVICE_DISC_REQ              0x0C
#define MT_ZDO_END_DEV_BIND_REQ              0x20
#define MT_ZDO_BIND_REQ                      0x21
#define MT_ZDO_UNBIND_REQ                    0x22

#define MT_ZDO_SET_LINK_KEY                  0x23//设置设备的链接密匙
#define MT_ZDO_REMOVE_LINK_KEY               0x24//删除设备的链接密匙
#define MT_ZDO_GET_LINK_KEY                  0x25
#define MT_ZDO_NWK_DISCOVERY_REQ             0x26//启动网络发现扫描
#define MT_ZDO_JOIN_REQ                      0x27//加网请求
#define MT_ZDO_SEND_DATA                     0x28//发送数据
#define MT_ZDO_NWK_ADDR_OF_INTEREST_REQ      0x29

#define MT_ZDO_MGMT_NWKDISC_REQ              0x30
#define MT_ZDO_MGMT_LQI_REQ                  0x31//设备LQI查询
#define MT_ZDO_MGMT_RTG_REQ                  0x32
#define MT_ZDO_MGMT_BIND_REQ                 0x33
#define MT_ZDO_MGMT_LEAVE_REQ                0x34
#define MT_ZDO_MGMT_DIRECT_JOIN_REQ          0x35
#define MT_ZDO_MGMT_PERMIT_JOIN_REQ          0x36
#define MT_ZDO_MGMT_NWK_UPDATE_REQ           0x37

/* AREQ optional, but no AREQ response. */
#define MT_ZDO_MSG_CB_REGISTER               0x3E
#define MT_ZDO_MSG_CB_REMOVE                 0x3F
#define MT_ZDO_STARTUP_FROM_APP              0x40

/* AREQ from host */
#define MT_ZDO_AUTO_FIND_DESTINATION_REQ     0x41
#define MT_ZDO_SEC_ADD_LINK_KEY              0x42
#define MT_ZDO_SEC_ENTRY_LOOKUP_EXT          0x43
#define MT_ZDO_SEC_DEVICE_REMOVE             0x44
#define MT_ZDO_EXT_ROUTE_DISC                0x45//启动路由发现
#define MT_ZDO_EXT_ROUTE_CHECK               0x46//路由检查
#define MT_ZDO_EXT_REMOVE_GROUP              0x47
#define MT_ZDO_EXT_REMOVE_ALL_GROUP          0x48
#define MT_ZDO_EXT_FIND_ALL_GROUPS_ENDPOINT  0x49
#define MT_ZDO_EXT_FIND_GROUP                0x4A
#define MT_ZDO_EXT_ADD_GROUP                 0x4B
#define MT_ZDO_EXT_COUNT_ALL_GROUPS          0x4C
#define MT_ZDO_EXT_RX_IDLE                   0x4D
#define MT_ZDO_EXT_UPDATE_NWK_KEY            0x4E
#define MT_ZDO_EXT_SWITCH_NWK_KEY            0x4F
#define MT_ZDO_EXT_NWK_INFO                  0x50
#define MT_ZDO_EXT_SEC_APS_REMOVE_REQ        0x51
#define MT_ZDO_FORCE_CONCENTRATOR_CHANGE     0x52
#define MT_ZDO_EXT_SET_PARAMS                0x53

/* AREQ to host */
#define MT_ZDO_AREQ_TO_HOST                  0x80 /* Mark the start of the ZDO CId AREQs to host. */
#define MT_ZDO_NWK_ADDR_RSP                  0x80 // ((uint8)NWK_addr_req | 0x80)
#define MT_ZDO_IEEE_ADDR_RSP                 0x81 // ((uint8)IEEE_addr_req | 0x80)
#define MT_ZDO_NODE_DESC_RSP                 0x82 // ((uint8)Node_Desc_req | 0x80)
#define MT_ZDO_POWER_DESC_RSP                0x83 // ((uint8)Power_Desc_req | 0x80)
#define MT_ZDO_SIMPLE_DESC_RSP               0x84 // ((uint8)Simple_Desc_req | 0x80)
#define MT_ZDO_ACTIVE_EP_RSP              /* 0x85 */ ((uint8)Active_EP_req | 0x80)
#define MT_ZDO_MATCH_DESC_RSP             /* 0x86 */ ((uint8)Match_Desc_req | 0x80)

#define MT_ZDO_COMPLEX_DESC_RSP              0x87
#define MT_ZDO_USER_DESC_RSP                 0x88
//                                        /* 0x92 */ ((uint8)Discovery_Cache_req | 0x80)
#define MT_ZDO_USER_DESC_CONF                0x89
#define MT_ZDO_SERVER_DISC_RSP               0x8A

#define MT_ZDO_END_DEVICE_BIND_RSP        /* 0xA0 */ ((uint8)End_Device_Bind_req | 0x80)
#define MT_ZDO_BIND_RSP                   /* 0xA1 */ ((uint8)Bind_req | 0x80)
#define MT_ZDO_UNBIND_RSP                 /* 0xA2 */ ((uint8)Unbind_req | 0x80)

#define MT_ZDO_MGMT_NWK_DISC_RSP          0xB0 // ((uint8)Mgmt_NWK_Disc_req | 0x80)
#define MT_ZDO_MGMT_LQI_RSP               0xB1 // ((uint8)Mgmt_Lqi_req | 0x80)
#define MT_ZDO_MGMT_RTG_RSP               0xB2 // ((uint8)Mgmt_Rtg_req | 0x80)
#define MT_ZDO_MGMT_BIND_RSP              0xB3 // ((uint8)Mgmt_Bind_req | 0x80)
#define MT_ZDO_MGMT_LEAVE_RSP             0xB4 // ((uint8)Mgmt_Leave_req | 0x80)
#define MT_ZDO_MGMT_DIRECT_JOIN_RSP       0xB5 // ((uint8)Mgmt_Direct_Join_req | 0x80)
#define MT_ZDO_MGMT_PERMIT_JOIN_RSP       0xB6 //((uint8)Mgmt_Permit_Join_req | 0x80)

//                                        /* 0xB8 */ ((uint8)Mgmt_NWK_Update_req | 0x80)

#define MT_ZDO_STATE_CHANGE_IND              0xC0
#define MT_ZDO_END_DEVICE_ANNCE_IND          0xC1
#define MT_ZDO_MATCH_DESC_RSP_SENT           0xC2
#define MT_ZDO_STATUS_ERROR_RSP              0xC3
#define MT_ZDO_SRC_RTG_IND                   0xC4
#define MT_ZDO_BEACON_NOTIFY_IND             0xC5
#define MT_ZDO_JOIN_CNF                      0xC6
#define MT_ZDO_NWK_DISCOVERY_CNF             0xC7
#define MT_ZDO_CONCENTRATOR_IND_CB           0xC8
#define MT_ZDO_LEAVE_IND                     0xC9
#define MT_ZDO_TC_DEVICE_IND                 0xCA
#define MT_ZDO_PERMIT_JOIN_IND               0xCB
#define MT_ZDO_SET_REJOIN_PARAMS             0xCC

#define MT_ZDO_MSG_CB_INCOMING               0xFF


/***************************************************************************************************
 * SYS COMMANDS
 ***************************************************************************************************/

/* AREQ from host */
#define MT_SYS_RESET_REQ                     0x00

/* SREQ/SRSP */
#define MT_SYS_PING                          0x01
#define MT_SYS_VERSION                       0x02
#define MT_SYS_SET_EXTADDR                   0x03
#define MT_SYS_GET_EXTADDR                   0x04
#define MT_SYS_RAM_READ                      0x05
#define MT_SYS_RAM_WRITE                     0x06
#define MT_SYS_OSAL_NV_ITEM_INIT             0x07
#define MT_SYS_OSAL_NV_READ                  0x08
#define MT_SYS_OSAL_NV_WRITE                 0x09
#define MT_SYS_OSAL_START_TIMER              0x0A
#define MT_SYS_OSAL_STOP_TIMER               0x0B
#define MT_SYS_RANDOM                        0x0C
#define MT_SYS_ADC_READ                      0x0D
#define MT_SYS_GPIO                          0x0E
#define MT_SYS_STACK_TUNE                    0x0F
#define MT_SYS_SET_TIME                      0x10
#define MT_SYS_GET_TIME                      0x11
#define MT_SYS_OSAL_NV_DELETE                0x12
#define MT_SYS_OSAL_NV_LENGTH                0x13
#define MT_SYS_SET_TX_POWER                  0x14
#define MT_SYS_JAMMER_PARAMETERS             0x15
#define MT_SYS_SNIFFER_PARAMETERS            0x16
#define MT_SYS_ZDIAGS_INIT_STATS             0x17
#define MT_SYS_ZDIAGS_CLEAR_STATS            0x18
#define MT_SYS_ZDIAGS_GET_STATS              0x19
#define MT_SYS_ZDIAGS_RESTORE_STATS_NV       0x1A
#define MT_SYS_ZDIAGS_SAVE_STATS_TO_NV       0x1B
#define MT_SYS_OSAL_NV_READ_EXT              0x1C
#define MT_SYS_OSAL_NV_WRITE_EXT             0x1D

/* Extended Non-Vloatile Memory */
#define MT_SYS_NV_CREATE                     0x30
#define MT_SYS_NV_DELETE                     0x31
#define MT_SYS_NV_LENGTH                     0x32
#define MT_SYS_NV_READ                       0x33
#define MT_SYS_NV_WRITE                      0x34
#define MT_SYS_NV_UPDATE                     0x35
#define MT_SYS_NV_COMPACT                    0x36

/* AREQ to host */
#define MT_SYS_RESET_IND                     0x80
#define MT_SYS_OSAL_TIMER_EXPIRED            0x81
#define MT_SYS_JAMMER_IND                    0x82


#define MT_SYS_RESET_HARD     0
#define MT_SYS_RESET_SOFT     1
#define MT_SYS_RESET_SHUTDOWN 2

#define MT_SYS_SNIFFER_DISABLE       0
#define MT_SYS_SNIFFER_ENABLE        1
#define MT_SYS_SNIFFER_GET_SETTING   2


#define ZCD_STARTOPT_DEFAULT_CONFIG_STATE  0x01
#define ZCD_STARTOPT_DEFAULT_NETWORK_STATE 0x02
#define ZCD_STARTOPT_AUTO_START            0x04
/***************************************************************************************************
 * SAPI COMMANDS
 ***************************************************************************************************/

// SAPI MT Command Identifiers
/* AREQ from Host */
#define MT_SAPI_SYS_RESET                   0x09

/* SREQ/SRSP */
#define MT_SAPI_START_REQ                   0x00
#define MT_SAPI_BIND_DEVICE_REQ             0x01
#define MT_SAPI_ALLOW_BIND_REQ              0x02
#define MT_SAPI_SEND_DATA_REQ               0x03
#define MT_SAPI_READ_CFG_REQ                0x04
#define MT_SAPI_WRITE_CFG_REQ               0x05
#define MT_SAPI_GET_DEV_INFO_REQ            0x06
#define MT_SAPI_FIND_DEV_REQ                0x07
#define MT_SAPI_PMT_JOIN_REQ                0x08
#define MT_SAPI_APP_REGISTER_REQ            0x0a

/* AREQ to host */
#define MT_SAPI_START_CNF                   0x80
#define MT_SAPI_BIND_CNF                    0x81
#define MT_SAPI_ALLOW_BIND_CNF              0x82
#define MT_SAPI_SEND_DATA_CNF               0x83
#define MT_SAPI_READ_CFG_RSP                0x84
#define MT_SAPI_FIND_DEV_CNF                0x85
#define MT_SAPI_DEV_INFO_RSP                0x86
#define MT_SAPI_RCV_DATA_IND                0x87

/***************************************************************************************************
 * UTIL COMMANDS
 ***************************************************************************************************/

/* SREQ/SRSP: */
#define MT_UTIL_GET_DEVICE_INFO              0x00
#define MT_UTIL_GET_NV_INFO                  0x01
#define MT_UTIL_SET_PANID                    0x02
#define MT_UTIL_SET_CHANNELS                 0x03
#define MT_UTIL_SET_SECLEVEL                 0x04
#define MT_UTIL_SET_PRECFGKEY                0x05
#define MT_UTIL_CALLBACK_SUB_CMD             0x06
#define MT_UTIL_KEY_EVENT                    0x07
#define MT_UTIL_TIME_ALIVE                   0x09
#define MT_UTIL_LED_CONTROL                  0x0A

#define MT_UTIL_TEST_LOOPBACK                0x10
#define MT_UTIL_DATA_REQ                     0x11

#define MT_UTIL_GPIO_SET_DIRECTION           0x14
#define MT_UTIL_GPIO_READ                    0x15
#define MT_UTIL_GPIO_WRITE                   0x16

#define MT_UTIL_SRC_MATCH_ENABLE             0x20
#define MT_UTIL_SRC_MATCH_ADD_ENTRY          0x21
#define MT_UTIL_SRC_MATCH_DEL_ENTRY          0x22
#define MT_UTIL_SRC_MATCH_CHECK_SRC_ADDR     0x23
#define MT_UTIL_SRC_MATCH_ACK_ALL_PENDING    0x24
#define MT_UTIL_SRC_MATCH_CHECK_ALL_PENDING  0x25

#define MT_UTIL_ADDRMGR_EXT_ADDR_LOOKUP      0x40
#define MT_UTIL_ADDRMGR_NWK_ADDR_LOOKUP      0x41
#define MT_UTIL_APSME_LINK_KEY_DATA_GET      0x44
#define MT_UTIL_APSME_LINK_KEY_NV_ID_GET     0x45
#define MT_UTIL_ASSOC_COUNT                  0x48
#define MT_UTIL_ASSOC_FIND_DEVICE            0x49
#define MT_UTIL_ASSOC_GET_WITH_ADDRESS       0x4A
#define MT_UTIL_APSME_REQUEST_KEY_CMD        0x4B
#ifdef MT_SRNG
#define MT_UTIL_SRNG_GENERATE                0x4C
#endif
#define MT_UTIL_BIND_ADD_ENTRY               0x4D

#define MT_UTIL_ZCL_KEY_EST_INIT_EST         0x80
#define MT_UTIL_ZCL_KEY_EST_SIGN             0x81

/* AREQ from/to host */
#define MT_UTIL_SYNC_REQ                     0xE0
#define MT_UTIL_ZCL_KEY_ESTABLISH_IND        0xE1
#ifdef FEATURE_GET_PRIMARY_IEEE
#define MT_UTIL_GET_PRIMARY_IEEE             0xEF
#endif

/***************************************************************************************************
 * AF COMMANDS
 ***************************************************************************************************/

/* SREQ/SRSP */
#define MT_AF_REGISTER                       0x00
#define MT_AF_DATA_REQUEST                   0x01  /* AREQ optional, but no AREQ response. */
#define MT_AF_DATA_REQUEST_EXT               0x02  /* AREQ optional, but no AREQ response. */
#define MT_AF_DATA_REQUEST_SRCRTG            0x03
#define MT_AF_DELETE                         0x04

#define MT_AF_INTER_PAN_CTL                  0x10
#define MT_AF_DATA_STORE                     0x11
#define MT_AF_DATA_RETRIEVE                  0x12
#define MT_AF_APSF_CONFIG_SET                0x13
#define MT_AF_APSF_CONFIG_GET                0x14

/* AREQ to host */
#define MT_AF_DATA_CONFIRM                   0x80
#define MT_AF_INCOMING_MSG                   0x81
#define MT_AF_INCOMING_MSG_EXT               0x82
#define MT_AF_REFLECT_ERROR                  0x83

#endif //SMARTHOME_SERIALCMDDEFINE_H
