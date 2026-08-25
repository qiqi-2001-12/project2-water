//
// Created by xia_w on 2018/4/19.
//

#ifndef SMARTHOME_LINUXGPIO_H
#define SMARTHOME_LINUXGPIO_H

/*
 * gpio.h
 *
 *  Created on: 2018年3月26日
 *      Author: derry6
 */


#define GPIO_DIR_IN 0
#define GPIO_DIR_OUT 1

#define GPIO_VALUE_LOW 		0
#define GPIO_VALUE_HIGH  	1

#define GPIO_EDGE_NONE 		0
#define GPIO_EDGE_RISING 	1
#define GPIO_EDGE_FALLING 	2
#define GPIO_EDGE_BOTH  	3
#ifdef HWELLYI_MT7688
#define BUZZER_GPIO19       19
#else
#define BUZZER_GPIO19       54
#endif
// 蜂鸣器: GPIO 19

// 1. 导出（程序启动执行一次就行）
// 2. 设置方向，输入还是输出（程序启动执行一次就行）

// 3. 输出...

// 4. 释放(没做其他用途， 无所谓)

// 导出GPIO
int gpio_export(int pin);
// 释放GPIO
int gpio_unexport(int pin);

// 设置GPIO方向， 输入还是输出
int gpio_set_direction(int pin, int dir);

// GPIO输出
int gpio_write(int pin, int value);
int gpio_read(int pin);

// none表示引脚为输入，不是中断引脚
// rising表示引脚为中断输入，上升沿触发
// falling表示引脚为中断输入，下降沿触发
// both表示引脚为中断输入，边沿触发
// 0-->none, 1-->rising, 2-->falling, 3-->both
int gpio_set_edge(int pin, int edge);


/* GPIO_H_ */

#define WORK_LED "hwellyi:red:work" // 工作指示灯
#define WAN_LED  "hwellyi:blue:wan" // 网络指示灯


void led_on(const char* led);

void led_off(const char* led);

void led_timer(const char* led, int delay_on, int delay_off);


#endif //SMARTHOME_LINUXGPIO_H
