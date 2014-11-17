/*
 * m24lr_xx_i2c.c
 *
 *  Created on: Oct 28, 2014
 *      Author: Kirill
 */

#include "m24lr_xx_i2c.h"

volatile uint8_t aligned[3][6];
volatile uint8_t TXByteCtr;
volatile uint8_t * PTxData;


void NFC_ALIGN_SEND (uint16_t bpm, uint16_t temp,
					 uint16_t transit, uint16_t hydro,
					 uint8_t time, uint8_t day, uint16_t addr,
					 uint8_t PWR_PIN, uint8_t PULL_PIN)
{
	/*
	 * Data alignment:
	 *
	 * 		BPM_H    | BPM_L   | TRANSIT TIME_H   | TRANSIT TIME_L
	 * 		TEMP_H   | TEMP_L  | HYDRO_H 		  | HYDRO_L
	 * 		TIME	 | DAY     | 0x00			  | 0xFF
	 *
	 */

	uint16_t l_addr = addr;

	aligned[0][0] = (uint8_t)(l_addr >> 8);
	aligned[0][1] = (uint8_t)l_addr;
	aligned[0][2] = (uint8_t)(bpm >> 8);
	aligned[0][3] = (uint8_t)bpm;
	aligned[0][4] = (uint8_t)(transit >> 8);
	aligned[0][5] = (uint8_t)transit;

	l_addr += 0x0004;

	aligned[1][0] = (uint8_t)(l_addr >> 8);
	aligned[1][1] = (uint8_t)(l_addr);
	aligned[1][2] = (uint8_t)(temp >> 8);
	aligned[1][3] = (uint8_t)temp;
	aligned[1][4] = (uint8_t)(hydro >> 8);
	aligned[1][5] = (uint8_t)hydro;

	l_addr += 0x0004;

	aligned[2][0] = (uint8_t)(l_addr >> 8);
	aligned[2][1] = (uint8_t)(l_addr);
	aligned[2][2] = time;
	aligned[2][3] = day;
	aligned[2][4] = 0x00;
	aligned[2][5] = 0xFF;

	P2DIR |= (PULL_PIN); P2OUT |= (PULL_PIN);
	P2DIR |= (PWR_PIN); P2OUT |= (PWR_PIN);

	I2C_TO_M24LRXX(aligned[0], PWR_PIN);
	_delay_cycles(16000);
	I2C_TO_M24LRXX(aligned[1], PWR_PIN);
	_delay_cycles(16000);
	I2C_TO_M24LRXX(aligned[2], PWR_PIN);
	_delay_cycles(16000);

	P2OUT &= ~(PWR_PIN);  P2DIR &= ~(PWR_PIN);
	P2OUT &= ~(PULL_PIN); P2DIR &= ~(PULL_PIN);
}

void I2C_INIT(uint16_t SDA_PIN, uint16_t SCL_PIN) {

	P1SEL |= SDA_PIN + SCL_PIN;               // Assign I2C pins to USCI_B0
	P1SEL2|= SDA_PIN + SCL_PIN;               // Assign I2C pins to USCI_B0

	UCB0CTL1 |= UCSWRST;                      // Enable SW reset

	UCB0CTL0 = UCMST + UCMODE_3 + UCSYNC;     // I2C Master, synchronous mode
	UCB0CTL1 = UCSSEL_2 + UCSWRST;            // Use SMCLK, keep SW reset

	UCB0BR0 = 10;                             // fSCL = SMCLK/12 = ~100kHz
	UCB0BR1 = 0;

	UCB0I2CSA = 0x53;                         // Slave Address is 0AEh
	UCB0CTL1 &= ~UCSWRST;                     // Clear SW reset, resume operation

	_EINT();

	IE2 |= UCB0TXIE;                          // Enable TX interrupt

}

void I2C_TO_M24LRXX(uint8_t TxDataWord[], uint8_t PWR_PIN) {

	I2C_INIT(DEFAULT_SDA, DEFAULT_SCL);

	_delay_cycles(50);

	PTxData = (unsigned char *)TxDataWord;      	// TX array start address

	TXByteCtr = 6; 									// Load TX byte counter

	UCB0CTL1 |= UCTR | UCTXSTT; 					// I2C TX, start condition

}

#pragma vector = USCIAB0TX_VECTOR					// And this is crazy

__interrupt void i2c_TX(void)
{
	while (TXByteCtr)
			{
				UCB0TXBUF = *PTxData++; 					// Load TX buffer\

				if(UCB0CTL1 & UCTXSTT) 						// on first loop, the start byte is still sending
				{
					while(UCB0CTL1 & UCTXSTT); 				// Wait for start byte done
				}
				while ((IFG2 & UCB0TXIFG) != UCB0TXIFG);	// Wait for bit to send
				TXByteCtr--;
			}

			UCB0CTL1 |= UCTXSTP; 							// I2C stop condition
			while ((UCB0STAT & UCSTPIFG) == UCSTPIFG);

			if(TXByteCtr == 0) {
				IFG2 &= 0UL;
				IE2 &= ~(UCB0TXIE);
			}
	// Decrement TX byte counter
	__bic_SR_register_on_exit(LPM0_bits);
}




