/*
 * PublicTimer.h
 *
 *  Created on: Jul 28, 2017
 *      Author: root
 */

#ifndef MAIN_PUBLICTIMER_H_
#define MAIN_PUBLICTIMER_H_

void * mfTimerThead(void *arg);
//定时器相关函数
void onTimerAdd(int timerid, int timervalue, bool repeatflag, void (*mfHandler)(int, int), int pa1, int pa2);
void onTimerDelete(int timerid);
void onTimerUpdate(int timerid, int timevalue);
//int onTimerCheckValueWithID(int timerid);
char * onGetCurrentTimeS();
char * onChangeTimeS(time_t time);
long onGetTimeSec();
int onGetTimeDate();
int mGetTimeMs();
extern struct tm *mTimerNow;
int32_t onGetCurrentTime(uint32_t *minute, uint32_t *week);

#endif /* MAIN_PUBLICTIMER_H_ */
