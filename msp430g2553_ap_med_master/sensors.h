/*
 * sensors.h
 *
 *  Created on: Oct 28, 2014
 *      Author: Kirill
 */

#ifndef SENSORS_H_
#define SENSORS_H_

#include "hydro.h"

typedef struct {
	uint16_t bpm;
	uint16_t transit;
} heart_data;

void ADCChannelSelect_GEN(uint16_t BIT, uint16_t INCH);

uint16_t measTEMP(uint16_t delay, uint16_t BIT, uint16_t INCH, uint16_t PWR_PIN);

heart_data measHRTR(uint32_t delay, uint16_t BIT_F, uint16_t BIT_K, uint16_t INCH,
					uint16_t FOOT_PWR_PIN, uint16_t KNEE_PWR_PIN);

#endif /* SENSORS_H_ */
