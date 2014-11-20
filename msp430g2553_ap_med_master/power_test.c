
#include "power_test.h"
#include "sensors.h"

uint8_t test_power(uint16_t TEST_PIN, uint16_t INCH) {

	uint8_t power = 0, samples = 5;
	uint16_t adc_val = 0;

	ADCChannelSelect_GEN(TEST_PIN, INCH);

	_delay_cycles(30);

	while(--samples != 0) {
	  ADC10CTL0 |= ENC + ADC10SC;     // Sampling and conversion start
	  _delay_cycles(100);
	  adc_val += (uint16_t)ADC10MEM;
	}

	adc_val = adc_val >> 2;

	_delay_cycles(5);

	power = (adc_val	<=	324	?	0			// Accounts for reserve energy to check level
			(adc_val	<=	331	?	1	:
			(adc_val	<=	338	?	2	:
			(adc_val	<=	345	?	3	:
			(adc_val	<=	352	?	4	:
			(adc_val	<=	359	?	5	:
			(adc_val	<=	366	?	6	:
			(adc_val	<=	373	?	7	:
			(adc_val	<=	380	?	8	:
			(adc_val	<=	387	?	9	:
			(adc_val	<=	394	?	10	:
			(adc_val	<=	401	?	11	:
			(adc_val	<=	408	?	12	:
			(adc_val	<=	415	?	13	:
			(adc_val	<=	422	?	14	:
			(adc_val	<=	429	?	15	:
			(adc_val	<=	436	?	16	:
			(adc_val	<=	443	?	17	:
			(adc_val	<=	450	?	18	:
			(adc_val	<=	457	?	19	:
			(adc_val	<=	464	?	20	:
			(adc_val	<=	471	?	21	:
			(adc_val	<=	478	?	22	:
			(adc_val	<=	485	?	23	:
			(adc_val	<=	492	?	24	:
			(adc_val	<=	499	?	25	:
			(adc_val	<=	506	?	26	:
			(adc_val	<=	513	?	27	:
			(adc_val	<=	520	?	28	:
			(adc_val	<=	527	?	29	:
			(adc_val	<=	534	?	30	:
			(adc_val	<=	541	?	31	:
			(adc_val	<=	548	?	32	:
			(adc_val	<=	555	?	33	:
			(adc_val	<=	562	?	34	:
			(adc_val	<=	569	?	35	:
			(adc_val	<=	576	?	36	:
			(adc_val	<=	583	?	37	:
			(adc_val	<=	590	?	38	:
			(adc_val	<=	597	?	39	:
			(adc_val	<=	604	?	40	:
			(adc_val	<=	611	?	41	:
			(adc_val	<=	618	?	42	:
			(adc_val	<=	625	?	43	:
			(adc_val	<=	632	?	44	:
			(adc_val	<=	639	?	45	:
			(adc_val	<=	646	?	46	:
			(adc_val	<=	653	?	47	:
			(adc_val	<=	660	?	48	:
			(adc_val	<=	667	?	49	:
			(adc_val	<=	674	?	50	:
			(adc_val	<=	681	?	51	:
			(adc_val	<=	688	?	52	:
			(adc_val	<=	695	?	53	:
			(adc_val	<=	702	?	54	:
			(adc_val	<=	709	?	55	:
			(adc_val	<=	716	?	56	:
			(adc_val	<=	723	?	57	:
			(adc_val	<=	730	?	58	:
			(adc_val	<=	737	?	59	:
			(adc_val	<=	744	?	60	:
			(adc_val	<=	751	?	61	:
			(adc_val	<=	758	?	62	:
			(adc_val	<=	765	?	63	:
			(adc_val	<=	772	?	64	:
			(adc_val	<=	779	?	65	:
			(adc_val	<=	786	?	66	:
			(adc_val	<=	793	?	67	:
			(adc_val	<=	800	?	68	:
			(adc_val	<=	807	?	69	:
			(adc_val	<=	814	?	70	:
			(adc_val	<=	821	?	71	:
			(adc_val	<=	828	?	72	:
			(adc_val	<=	835	?	73	:
			(adc_val	<=	842	?	74	:
			(adc_val	<=	849	?	75	:
			(adc_val	<=	856	?	76	:
			(adc_val	<=	863	?	77	:
			(adc_val	<=	870	?	78	:
			(adc_val	<=	877	?	79	:
			(adc_val	<=	884	?	80	:
			(adc_val	<=	891	?	81	:
			(adc_val	<=	898	?	82	:
			(adc_val	<=	905	?	83	:
			(adc_val	<=	912	?	84	:
			(adc_val	<=	919	?	85	:
			(adc_val	<=	926	?	86	:
			(adc_val	<=	933	?	87	:
			(adc_val	<=	940	?	88	:
			(adc_val	<=	947	?	89	:
			(adc_val	<=	954	?	90	:
			(adc_val	<=	961	?	91	:
			(adc_val	<=	968	?	92	:
			(adc_val	<=	975	?	93	:
			(adc_val	<=	982	?	94	:
			(adc_val	<=	989	?	95	:
			(adc_val	<=	996	?	96	:
			(adc_val	<=	1003?	97	:
			(adc_val	<=	1010?	98	:
			(adc_val	<=	1017?	99	:	100)
			))))))))))))))))))))))))))))))))))))
			))))))))))))))))))))))))))))))))))))
			)))))))))))))))))))))))))));


	ADC10CTL0 &= ~ENC;

	P1DIR &= ~TEST_PIN;
	P1REN &= ~TEST_PIN;

	return power;

}
