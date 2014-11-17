/*
 * hydro.c
 *
 *  Created on: Nov 8, 2014
 *      Author: Kirill
 */
#include "main.h"
#include "hydro.h"

volatile uint16_t time_up;
volatile uint8_t overflows, rise_count;

uint16_t measHYDR(void) {

	BCSCTL1 = CALBC1_16MHZ;				// Set range   DCOCTL = CALDCO_1MHZ;
	DCOCTL  = CALDCO_16MHZ;				// SMCLK = DCO = 1MHz

	P1DIR = HYDRO_PULSE_OUT;
	P1SEL = HYDRO_PULSE_OUT;    		// Set P1.5 to TA0.0 output, controls charge/discharge

    CACTL1 = CARSEL + CAREF_1;   // 0.25 Vcc ref on - pin.
    CACTL2 = HYDRO_PULSE_IN;
    CACTL2 |= CAF;       // Input CA0 on + pin, filter output.

    rise_count = 0;
    time_up = 0;

    while(rise_count < 8) {

    	TACTL = TASSEL_2 + ID_0 + MC_0;     	// Use SMCLK (16 MHz Calibrated), no division,
    	                                        // stopped mode
		TACCTL0 = OUTMOD_1 + CCIE;          	// TA0 sets VCTL at TACCR0
		TACCTL1 = CCIS_1 + SCS + CAP + CCIE;    // Use CAOUT for input, synchronize, set to
												// capture mode, enable interrupt for TA1.
												// NOTE: Capturing mode not started.

        overflows = 0;      					// reset overflow counter

        _BIS_SR(GIE);							// Sleep, my dearest

        TACTL |= MC_2;      					// start timer, sets TA0.0 at overflow.

        while (overflows < 10);

        CACTL1 |= CAON;     					// Turn on comparator.
        TACCTL0 = OUTMOD_5 + CCIE; 				// start discharge on next overflow.
        TACCTL1 |= CM_2;    					// start TA1 capture on falling edge.

        overflows = -1;     					// reset overflow counter (accounting for overflow
                            					// to start discharge).
        _BIS_SR(LPM0_bits + GIE);				// Sleep, my dearest
    }

    TACTL &= ~MC_2;             // turn off Timer A

	BCSCTL1 = CALBC1_1MHZ;				// Set range   DCOCTL = CALDCO_1MHZ;
	DCOCTL  = CALDCO_1MHZ;				// SMCLK = DCO = 1MHz

    return (time_up >> 3);
}

/* Hydration ISR */

#pragma vector = TIMER0_A0_VECTOR
__interrupt void TA0_ISR(void) {
    overflows++;    // TA0 interrupt means TA has seen 2^16 counts without CA trigger.
                    // rollover to 0 on TAR, so mark overflow.
} // TA0_ISR

#pragma vector = TIMER0_A1_VECTOR
__interrupt void TA1_ISR(void) {
    TACCTL1 &= ~(CM_2 + CCIFG); // Stop TA1 captures, clear interrupt flag.
    TACTL &= ~MC_2;             // turn off Timer A.
    if(rise_count < 8) {
    	time_up += TAR;
    	rise_count++;
    }
    __low_power_mode_off_on_exit();    // continue program
} // TA1_ISR

/* End Hydration ISR */

/* End Hydration ISR */
