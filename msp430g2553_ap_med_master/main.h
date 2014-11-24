/*
 * main.h
 *
 *  Created on: Oct 28, 2014
 *      Author: Kirill
 */

#ifndef MAIN_H_
#define MAIN_H_
// Times

#include <msp430.h>
#include <stdint.h>
#include "msp430g2553.h"

#include "sensors.h"
#include "hydro.h"
#include "power_test.h"
#include "m24lr_xx_i2c.h"

#define HEART_ON_TIME		15000
#define TEMP_ON_TME			8

// Port two PWR Pins

#define HR_FOOT_PWR		BIT0	// 2.0
#define HR_KNEE_PWR		BIT1	// 2.1

#define TEMP_PWR		BIT2	// 2.2

#define I2C_PWR			BIT5	// 2.5
#define PUR_PWR			BIT4	// 2.4

// Port one ADC pins

#define HYDRO_PULSE_OUT		BIT5	// 1.5
#define HYDRO_PULSE_IN		P2CA0	// 1.0

/* Port 1 bit 6 and 7 reserved for I2C comm  */

#define HR_FOOT_SENSE   BIT1	//	1.1

#define HR_LEG_SENSE	BIT2	// 1.2

#define TEMP_SENSE		BIT3	// 1.3

#define PWR_SENSE		BIT4    // 1.4

#define HR_ADC_INCH		INCH_3

#define TEMP_ADC_INCH	INCH_3

#define PWR_ADC_INCH	INCH_4

#endif /* MAIN_H_ */
