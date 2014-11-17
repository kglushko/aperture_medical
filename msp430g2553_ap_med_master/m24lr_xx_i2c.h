/*
 * m24lr_xx_i2c.h
 *
 *  Created on: Oct 28, 2014
 *      Author: Kirill
 */

#ifndef M24LR_XX_I2C_H_
#define M24LR_XX_I2C_H_

#include "main.h"

#define DEFAULT_SCL		BIT6
#define DEFAULT_SDA		BIT7
#define NDEF_DATALN		0x09

/* Prototypes */

void I2C_INIT(uint16_t SDA_PIN, uint16_t SCL_PIN);

void NFC_ALIGN_SEND(uint16_t bpm, uint16_t temp,
					uint16_t transit, uint16_t hydro,
					uint8_t time, uint8_t day, uint16_t addr,
					uint8_t PWR_PIN, uint8_t PULL_PIN);

void I2C_TO_M24LRXX(uint8_t TxDataWord[], uint8_t PWR_PIN);

#endif /* M24LR_XX_I2C_H_ */
