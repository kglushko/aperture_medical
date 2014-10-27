#include <msp430.h>
#include <stdint.h>
#include "msp430g2553.h"

#define HEART_ON		500000
#define TEMP_ON			8

volatile uint32_t g_delay;

volatile uint16_t g_bpm, g_temp;

void ADCChannelSelect(uint16_t BIT, uint16_t INCH);

uint16_t measTEMP(uint32_t delay, uint16_t BIT, uint16_t INCH);

uint16_t measHRTR(uint32_t delay, uint16_t BIT, uint16_t INCH);

int main(void)
{
  WDTCTL = WDT_ADLY_1000;						// Slowest watchdog ticks
  IE1 |= WDTIE;									// Enable WatchDog Interrupt

  /* --- Configure hardware UART --- */

	 BCSCTL1 = CALBC1_1MHZ; // Set DCO to 1MHz
	 DCOCTL = CALDCO_1MHZ; // Set DCO to 1MHz

     P1SEL = BIT1 + BIT2 ; // P1.1 = RXD, P1.2=TXD
     P1SEL2 = BIT1 + BIT2 ; // P1.1 = RXD, P1.2=TXD

     UCA0CTL1 |= UCSSEL_2; // Use SMCLK
     UCA0BR0 = 104; // Set baud rate to 9600 with 1MHz clock (Data Sheet 15.3.13)
     UCA0BR1 = 0; // Set baud rate to 9600 with 1MHz clock
     UCA0MCTL = UCBRS0; // Modulation UCBRSx = 1
     UCA0CTL1 &= ~UCSWRST; // Initialize USCI state machine

     /* -------- END UART -------- */

  P1DIR |= BIT0;                            	// Set P1.0 to output direction
  P1DIR |= BIT6;

  P1DIR |= BIT5;
  P1DIR |= BIT3;

  P1OUT &= ~BIT6;
  P1OUT &= ~BIT5;
  P1OUT &= ~BIT3;

  g_delay = 1;

  _BIS_SR(LPM3_bits + GIE);
}

// WDT interrupt service routine

#pragma vector = WDT_VECTOR

__interrupt void watchdog_timer(void)
{
	if(--g_delay==0)								// Everything happens in here, woken from watchdog
	{
		_delay_cycles(100);

		g_bpm = measHRTR(HEART_ON, BIT4, INCH_4);

		_delay_cycles(100);

		g_temp = measTEMP(TEMP_ON, BIT7, INCH_7);

		g_delay = 3;
	}

}

void ADCChannelSelect(uint16_t BIT, uint16_t INCH) {

	while (ADC10CTL1 & ADC10BUSY);						// Wait for my demise

	ADC10AE0 = 0;
	ADC10CTL0 = 0;

	ADC10CTL0 = ADC10SHT_0
			+ ADC10SR			/* ADC10 Sampling Rate 0:200ksps / 1:50ksps */
			+ ADC10ON; 			// ADC10ON, interrupt enabled

	ADC10CTL1 = INCH;
	ADC10AE0 |= BIT;

}

uint16_t measTEMP(uint32_t delay, uint16_t BIT, uint16_t INCH) {

	uint16_t temp;

	ADCChannelSelect(BIT, INCH);

	P1OUT |= BIT3;

	_delay_cycles(30);

	while(--delay != 0) {
	  ADC10CTL0 |= ENC + ADC10SC;     // Sampling and conversion start
	  _delay_cycles(100);
	  temp += (unsigned int)ADC10MEM;
	}

	_delay_cycles(30);

	P1OUT &= ~BIT3;

	temp = (unsigned int)((float)temp / 8);

	return temp;

}

uint16_t measHRTR(uint32_t delay, uint16_t BIT, uint16_t INCH) {

	uint32_t ticks, beat;

	uint16_t beat_time[6], bpm;

	uint8_t bFlag, tFlag;

	ADCChannelSelect(BIT, INCH);

	tFlag = 0;
	bFlag = 0;
	beat = 0;					// No heart beats
	ticks = 0;					// No ticks

	P1OUT |= BIT5;					// Power heart rate sensor

	while(--delay != 0)				// ADC DELAY Controls Conversion time
	{
		ADC10CTL0 |= ENC + ADC10SC;     // Sampling and conversion start
		// This section is Heart Rate
		if(tFlag == 1) {
			ticks++;					// Count time ticks (Currently 16 thousand per sec)
		}

		if (ADC10MEM > 0x01FF && ADC10MEM < 0xFFFF) { // Highest impulse means sensor intialized
			P1OUT |= BIT6;							  // Light GRN - debug
			if(bFlag == 0) {
				bFlag = 1;							  // listen for low
			}
		}

		else if (ADC10MEM < 0x0A) {					  // When signal goes low
			if(bFlag == 1) {						  // And we have activated the sensor
				tFlag = 1;							  // Start Timer
				bFlag = 2;							  // Stop Timer
				P1OUT &= ~BIT6;						  // Drop GRN - debug
				P1OUT |= 0x01;						  // Light RED - debug
			}
		}

		else if (ADC10MEM > 0x01F0 && ADC10MEM < 0x1FFF) {	// If we detect peak
		  if(bFlag == 2) {									// and state machine says count
			  P1OUT &= ~0x01;								// Drop RED - debug

			  beat_time[beat] = ticks;					// Record time
			  ticks = 0;								// reset time
			  if(beat == 6) {break;}					// After 6 beats, exit

			  beat++;									// Count a beat
			  bFlag = 0;								// Mark beat counted
		  }
		}
	}														// End of HR ADC

	P1OUT &= ~BIT5;

	uint8_t i = 0;
	double accum = 0;

	for(i = 2; i < 6; i++) {
	  accum += beat_time[i];
	}

	double time_delta = 0;

	time_delta = (((accum) / 4 ) / (16000));

	bpm = (uint16_t)(60 / time_delta);

	P1OUT &= ~0x01;	// DEBUG

	return bpm;


}
