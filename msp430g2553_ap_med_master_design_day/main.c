/*
 * main.c
 */
#include "main.h"
#include "math.h"
#include "sensors.h"
#include "m24lr_xx_i2c.h"

volatile uint16_t g_addr = 0;

uint32_t g_delay = 0;

uint8_t g_timestamp = 0, g_daystamp = 0, g_power;

volatile heart_data g_heart_data;

volatile uint16_t g_temp = 0, g_hyd = 0;

/* Design day demo */

volatile uint16_t time_up;
volatile uint8_t overflows, rise_count;

/* End Design day demo */

int main(void)
{
  WDTCTL = WDTPW + WDTHOLD;                 // Stop WDT

  /*g_power = test_power(PWR_SENSE, PWR_ADC_INCH);

  if(g_power == 0) {
	  g_delay = 2700000;			// 15 minutes
  }*/

  g_delay = 1500;		// 5 seconds


  WDTCTL = WDT_MDLY_32;						// I just met you

  IE1 |= WDTIE;								// Enable WatchDog Interrupt

  __bis_SR_register(LPM1_bits + GIE);        // Enter LPM1 w/ interrupt


	while(1)
	{
	  if(--g_delay==0)						    // Everything happens in here, woken from watchdog
		{
			WDTCTL = WDTPW + WDTHOLD;               // Stop WDT

			IE1 &= ~WDTIE;							// Disable WatchDog Interrupt

			/*g_power = test_power(PWR_SENSE, PWR_ADC_INCH);

			if(g_power == 0) {
				g_delay = 2700000;			// 15 minutes
				WDTCTL = WDT_MDLY_32;						// But heres my number
				IE1 |= WDTIE;								// Enable WatchDog Interrupt
				__bis_SR_register(LPM1_bits + GIE);        // Enter LPM1 w/ interrupts
			}*/

			_delay_cycles(50);					// And this is crazy

			g_heart_data = measHRTR(HEART_ON_TIME, HR_FOOT_SENSE, HR_LEG_SENSE,HR_ADC_INCH, HR_FOOT_PWR, HR_KNEE_PWR);

			_delay_cycles(50);

			g_temp = measTEMP(TEMP_ON_TME, TEMP_SENSE, TEMP_ADC_INCH, TEMP_PWR);

			_delay_cycles(50);

			g_hyd = measHYDR();

			_delay_cycles(50);


			NFC_ALIGN_SEND	(	g_heart_data.bpm,			// Measured Beats Per Minute
								g_temp,						// Measured Temp
								g_heart_data.transit,		// Measured Transit Time
								g_hyd,						// Measure Hydration
								g_timestamp,				// Accumulated Timestamp
								g_daystamp,					// Accumulated Daystamp
								g_power,					// Device Power
								g_addr,						// Pointer to next address
								I2C_PWR,					// Power the NFC Transponder
								PUR_PWR						// Power pullups
							);

			g_addr = 0x0000;
			//g_addr += 0x000C;
			g_timestamp = g_timestamp + 1;

			if(g_timestamp == 92) {
				g_daystamp = g_daystamp + 1;
				//g_addr = 0x0000;
				g_timestamp = 0;
			}


			/*g_delay = 15000;

			WDTCTL = WDT_MDLY_32;						// But heres my number

			IE1 |= WDTIE;								// Enable WatchDog Interrupt

			__bis_SR_register(LPM1_bits + GIE);        // Enter LPM1 w/ interrupts*/

			 break;
		}
	}

	/* Design day demo */

		P2DIR |= (HR_FOOT_PWR);
		P2DIR |= (HR_KNEE_PWR);
		P2DIR |= (TEMP_PWR);

		P2OUT |= (HR_FOOT_PWR);
		P2OUT |= (HR_KNEE_PWR);
		P2OUT |= (TEMP_PWR);

  		while(1) {
  			g_hyd = measHYDR();
  		}

  	/* End design day demo */
}

// WDT interrupt service routine

#pragma vector = WDT_VECTOR								// So call me maybe

__interrupt void watchdog_timer(void)
{
	__bic_SR_register_on_exit(LPM1_bits);
}

/* Design day demo */
/*

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
*/
/* Debug ---------------------------------------------

void configUART(void)
{
	 BCSCTL1 = CALBC1_1MHZ; // Set DCO to 1MHz
	 DCOCTL = CALDCO_1MHZ; // Set DCO to 1MHz

    P1SEL = BIT1 + BIT2 ; // P1.1 = RXD, P1.2=TXD
    P1SEL2 = BIT1 + BIT2 ; // P1.1 = RXD, P1.2=TXD

    UCA0CTL1 |= UCSSEL_2; // Use SMCLK
    UCA0BR0 = 104; // Set baud rate to 9600 with 1MHz clock (Data Sheet 15.3.13)
    UCA0BR1 = 0; // Set baud rate to 9600 with 1MHz clock
    UCA0MCTL = UCBRS0; // Modulation UCBRSx = 1
    UCA0CTL1 &= ~UCSWRST; // Initialize USCI state machine
}

*/
