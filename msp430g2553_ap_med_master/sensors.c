/*
 * sensors.c
 *
 *  Created on: Oct 28, 2014
 *      Author: Kirill
 */

#include "main.h"
#include "sensors.h"

volatile uint16_t ADC_RESULT[2];

void ADCChannelSelect_GEN(uint16_t BIT, uint16_t INCH) {

	while (ADC10CTL1 & ADC10BUSY);						// Wait for my demise

	ADC10AE0 = 0;
	ADC10CTL0 = 0;

	BCSCTL1 = CALBC1_1MHZ;				// Set range   DCOCTL = CALDCO_1MHZ;
	DCOCTL  = CALDCO_1MHZ;				// SMCLK = DCO = 1MHz

	ADC10CTL0 =  ADC10SHT_0
			   + ADC10SR			/* ADC10 Sampling Rate 0:200ksps / 1:50ksps */
			   + ADC10ON; 			// ADC10ON, interrupt enabled

	ADC10CTL1 = ADC10SSEL_2 + INCH;
	ADC10AE0 |= BIT;

}


uint16_t measTEMP(uint16_t delay, uint16_t BIT, uint16_t INCH, uint16_t PWR_PIN) {

	volatile uint16_t temp = 0;

	ADCChannelSelect_GEN(BIT, INCH);

	P2DIR |= PWR_PIN;	P2OUT |= PWR_PIN;				// Sensor On

	_delay_cycles(30);

	while(--delay != 0) {
	  ADC10CTL0 |= ENC + ADC10SC;     // Sampling and conversion start
	  _delay_cycles(100);
	  temp += (uint16_t)ADC10MEM;
	}

	_delay_cycles(5);

	P2OUT &= ~PWR_PIN;	P2DIR &= ~PWR_PIN;

	return (uint16_t)(temp >> 3);

}

heart_data measHRTR(uint32_t delay, uint16_t BIT_F, uint16_t BIT_K, uint16_t INCH,
					uint16_t FOOT_PWR_PIN, uint16_t KNEE_PWR_PIN)
{

	heart_data metrics;

	uint16_t ticks = 0;

	volatile uint32_t BPM_TICKS = 0,
		              TRN_TICKS = 0;

	uint8_t beatfg = 0, tranfg = 0, timefg = 0;

	uint8_t tran = 0,
			beat = 0;

	ADC10CTL0 = 0;
	ADC10CTL1 = 0;
	ADC10AE0 = 0;

	P2DIR |= (FOOT_PWR_PIN);
	P2DIR |= (KNEE_PWR_PIN);

	P2OUT |= (FOOT_PWR_PIN);
	P2OUT |= (KNEE_PWR_PIN);

	BCSCTL1 = CALBC1_1MHZ;				// Set range   DCOCTL = CALDCO_1MHZ;
	DCOCTL  = CALDCO_1MHZ;				// SMCLK = DCO = 1MHz

	ADC10CTL0 = ADC10ON + ADC10SHT_0
				+ ADC10SR			/* ADC10 Sampling Rate 0:200ksps / 1:50ksps */
				+ MSC;

	ADC10CTL1 = ADC10SSEL_2 + INCH + CONSEQ_1;

	ADC10AE0 = (BIT_F | BIT_K);

	ADC10DTC1 = 0x03;			// 2 COnversions

	ADC10SA = (uint16_t)ADC_RESULT;

	while(--delay != 0)				// ADC DELAY Controls Conversion time
	{
		if(timefg == 1) {
			ticks++;					// Count time ticks (Currently 16 thousand per sec)
		}

		ADC10CTL0 &= ~ENC;

		while(ADC10CTL1 & BUSY);

		ADC10SA = (int)ADC_RESULT;

		ADC10CTL0 |= ENC + ADC10SC;

		if (ADC_RESULT[1] > 0x0250 && ADC_RESULT[1] < 0xFFFF) { // Highest impulse means sensor intialized
			if(beatfg == 0) {
				beatfg = 1;							  // listen for low
			}
		}

		else if (ADC_RESULT[1] < 0x0A) {			  // When signal goes low
			if(beatfg == 1) {						  // And we have activated the sensor
				timefg = 1;						  // Start Timer
				beatfg = 2;
			}
		}

		else if (ADC_RESULT[1] > 0x0200 && ADC_RESULT[1] < 0x1FFF) {		// If we detect peak
			if(beatfg == 2) {									// and state machine says count
				if(beat > 2) {BPM_TICKS += (ticks);}
				ticks = 0;										// reset time
				if(beat == 6) {break;}							// After 6 beats, exit
				beat++;											// Count a beat
				beatfg = 0;										// Mark beat counted
			}
		}

		if (ADC_RESULT[0] > 0x0250 && ADC_RESULT[0] < 0xFFFF) { // Highest impulse means sensor intialized
			if(tranfg == 0) {
				tranfg = 1;							  // listen for low
			}
		}

		else if (ADC_RESULT[0] < 0x0A) {			  // When signal goes low
			if(tranfg == 1) {						  // And we have activated the sensor
				tranfg = 2;
			}
		}

		else if (ADC_RESULT[0] > 0x0200 && ADC_RESULT[0] < 0x1FFF) {	// If we detect peak
			if(tranfg == 2) {											// and state machine says count
			    TRN_TICKS += ticks;										// After 6 beats, exit
				tran++;													// Count a beat
				tranfg = 0;
			}
		}
	}
													// End of HR ADC

	P2OUT &= ~(FOOT_PWR_PIN);
	P2OUT &= ~(KNEE_PWR_PIN);

	metrics.bpm = (uint16_t)BPM_TICKS >> 2;

	metrics.transit = (uint16_t)((float)TRN_TICKS / (float)tran);

	return metrics;
}




