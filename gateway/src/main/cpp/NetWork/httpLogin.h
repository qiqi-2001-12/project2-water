/*
 * httpLogin.h
 *
 *  Created on: 2017年6月25日
 *      Author: root
 */

#ifndef HTTPLOGIN_H_
#define HTTPLOGIN_H_

int mfHttpCertification();
int mfHttpGetGatewayInfo();
int mfHttpDlFile(char *file, char *filepath, char *savepath, char *filemd5, uint32_t *filelen, void* (*__start_routine)(void*));
int mfHttpCheckAppUpdate();
char *mHttpGetServerIPAddress();
char *mHttpGetAccessToken();
int mHttpGetServerIPPort();
int mfHttpRegistered(int64_t ieee, int64_t ieee_ex, char * model, char *defaultname);
//int mfHttpDevRegistered(uint64_t ieee, uint64_t ieee_ex, int devicetype, const char *name);
#endif /* HTTPLOGIN_H_ */
