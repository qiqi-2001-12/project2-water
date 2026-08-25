/*
 * gpio.c
 *
 *  Created on: 2018年3月26日
 *      Author: derry6
 */
#include "../Main/LinuxGPIO.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <poll.h>

static int __write_file(const char* path, const char* s, int len) {
	int fd = open(path, O_WRONLY);
	if (fd < 0) return -1;
	if ( write(fd, s, (size_t)len) < 0)
		return -1;
	close(fd);
	return 0;
}

int gpio_export(int pin) {
	char tmp[64];
	int len  = snprintf(tmp, sizeof(tmp), "%d", pin);
	return __write_file("/sys/class/gpio/export", tmp, len);
}

int gpio_unexport(int pin){
	char tmp[64];
	int len  = snprintf(tmp, sizeof(tmp), "%d", pin);
	return __write_file("/sys/class/gpio/unexport", tmp, len);
	return 0;
}

//dir: 0-->IN, 1-->OUT
int gpio_set_direction(int pin, int dir)
{
	char tmp[32] = {0};
	char path[256] = {0};
	int len = snprintf(tmp, sizeof tmp, "%s", dir?"out":"in");
	snprintf(path, sizeof path, "/sys/class/gpio/gpio%d/direction", pin);
	return __write_file(path, tmp, len);
}

//value: 0-->LOW, 1-->HIGH
int gpio_write(int pin, int value)
{
	char tmp[32] = {0};
	char path[256] = {0};
	int len = snprintf(tmp, sizeof tmp, "%s", value?"1":"0");
	snprintf(path, sizeof path, "/sys/class/gpio/gpio%d/value", pin);
	return __write_file(path, tmp, len);
}

int gpio_read(int pin)
{
	char path[64];
	char value_str[3];
	int fd;
	snprintf(path, sizeof(path), "/sys/class/gpio/gpio%d/value", pin);
	fd = open(path, O_RDONLY);
	if (fd < 0) {
		fprintf(stderr,"Failed to open gpio value for reading!\n");
		return -1;
	}
	if (read(fd, value_str, 3) < 0) {
		fprintf(stderr,"Failed to read value!\n");
		return -1;
	}
	close(fd);
	return (atoi(value_str));
}

// none表示引脚为输入，不是中断引脚
// rising表示引脚为中断输入，上升沿触发
// falling表示引脚为中断输入，下降沿触发
// both表示引脚为中断输入，边沿触发
// 0-->none, 1-->rising, 2-->falling, 3-->both
static const char* gpio_edge[4] = {"none", "rising", "falling", "both"};

int gpio_set_edge(int pin, int edge)
{
	char path[64];
	if( edge < 0 || edge > 3 ) return -1;
	snprintf(path, sizeof(path), "/sys/class/gpio/gpio%d/edge", pin);
	return __write_file(path, gpio_edge[edge], strlen(gpio_edge[edge]));
}

static void led_set(const char* led, int brigtness) {
	char cmd[100] = {0};
	snprintf(cmd, sizeof cmd, "echo none > /sys/class/leds/%s/trigger", led);
	system(cmd);
	snprintf(cmd, sizeof cmd, "echo %d > /sys/class/leds/%s/brightness", brigtness, led);
	system(cmd);
}

void led_on(const char* led) {
	led_set(led, 255);
}

void led_off(const char* led) {
	led_set(led, 0);
}

void led_timer(const char* led, int delay_on, int delay_off) {
	char cmd[100] = {0};
	snprintf(cmd, sizeof cmd, "echo timer > /sys/class/leds/%s/trigger", led);
	system(cmd);
	snprintf(cmd, sizeof cmd, "echo %d > /sys/class/leds/%s/delay_on", delay_on, led);
	system(cmd);
	snprintf(cmd, sizeof cmd, "echo %d > /sys/class/leds/%s/delay_off", delay_off, led);
	system(cmd);
}