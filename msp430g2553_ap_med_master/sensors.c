/*
 * sensors.c
 *
 *  Created on: Oct 28, 2014
 *      Author: Kirill
 */

#include "main.h"
#include "sensors.h"

volatile uint16_t sen1, sen2, time1 = 0, time2 = 0, l_delay, pulse1[9], pulse2[9], ADC_RESULT[3];

volatile uint8_t i = 0, j = 0, beatfg = 0, tranfg = 0;

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

heart_data measHRTR(uint16_t delay, uint16_t BIT_F, uint16_t BIT_K, uint16_t INCH,
					uint16_t FOOT_PWR_PIN, uint16_t KNEE_PWR_PIN)
{

	heart_data metrics;

	l_delay = delay;

	volatile uint32_t BPM_TICKS = 0,
		              TRN_TICKS = 0;

	ADC10CTL0 = 0;
	ADC10CTL1 = 0;
	ADC10AE0 = 0;

	P2DIR |= (FOOT_PWR_PIN);
	P2DIR |= (KNEE_PWR_PIN);

	P2OUT |= (FOOT_PWR_PIN);
	P2OUT |= (KNEE_PWR_PIN);


	BCSCTL1 = CALBC1_1MHZ;				// Set range   DCOCTL = CALDCO_1MHZ;
	DCOCTL  = CALDCO_1MHZ;				// SMCLK = DCO = 1MHz

	// Timer A Config
	TA1CCTL0 = CCIE;       				// Enable Periodic interrupt
	TA1CCR0 = 1000;             		// period = 1ms
	TA1CTL = TASSEL_2 + MC_1;    		// source SMCLK, up mode

	ADC10CTL1 = INCH + ADC10SSEL_3 + CONSEQ_1; //INCH_3: Enable A3 first, Use SMCLK, Sequence of Channels
	ADC10CTL0 = ADC10ON + MSC + ADC10IE;  		// Turn on ADC,  Put in Multiple Sample and Conversion mode,  Enable Interrupt

	ADC10AE0 |= (BIT_F | BIT_K);            // Enable A1 and A2 which are P1.1,P1.2

	ADC10DTC1 = 0x04;

	ADC10SA = (short)&ADC_RESULT[0]; // ADC10 data transfer starting address.

	_BIS_SR(GIE); 		// Enable global interrupt

	while((i < 10 || j < 10) && l_delay > 0) {}	// ADC DELAY Controls Conversion time

	for(i = 1; i < 10; i++)
	{
		BPM_TICKS += pulse1[i];
		TRN_TICKS += pulse2[i];
	}
											// End of HR ADC
	P2OUT &= ~(FOOT_PWR_PIN);
	P2OUT &= ~(KNEE_PWR_PIN);

	i = 0; j = 0;

	metrics.bpm 	= (uint16_t)(BPM_TICKS >> 3);
	metrics.transit = (uint16_t)(TRN_TICKS >> 3);

	return metrics;
}

// Timer A0 interrupt service routine
#pragma vector=TIMER1_A0_VECTOR
__interrupt void Timer_A (void)
{
	time1++;
	time2++;
	l_delay--;
	ADC10CTL0 |= ENC + ADC10SC;
}

#pragma vector=ADC10_VECTOR
__interrupt void ADC10_ISR (void) {
	sen1 = ADC_RESULT[2];
	sen2 = ADC_RESULT[1];

	if (sen1 > 900 && beatfg == 0) { 				// Highest impulse means sensor intialized
		beatfg = 1;				 					// listen for low
	}

	else if (sen1 < 20 && beatfg == 1) {			  // When signal goes low
		beatfg = 2;
	}

	else if (sen1 > 900 && beatfg == 2 && i < 10) {
		pulse1[i] = time1;
		time1 = 0;
		i++;
		beatfg = 0;
	}

	if (sen2 > 900 && tranfg == 0) { 				// Highest impulse means sensor intialized
		tranfg = 1;				 					// listen for low
	}

	else if (sen2 < 20 && tranfg == 1) {			  // When signal goes low
		tranfg = 2;
	}

	else if (sen2 > 900 && tranfg == 2 && j < 10) {
		pulse2[j] = time2;
		time2 = 0;
		j++;
		tranfg = 0;
	}

	ADC10CTL0 &= ~ADC10IFG;  // clear interrupt flag

	ADC10SA = (short)&ADC_RESULT[0]; // ADC10 data transfer starting address.
}


