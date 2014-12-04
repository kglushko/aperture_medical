################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Each subdirectory must supply rules for building sources it contributes
m24lr_xx_i2c.obj: ../m24lr_xx_i2c.c $(GEN_OPTS) $(GEN_HDRS)
	@echo 'Building file: $<'
	@echo 'Invoking: MSP430 Compiler'
	"D:/TexasInstruments/ccsv6/tools/compiler/msp430_4.3.3/bin/cl430" -vmsp --abi=eabi -O4 --opt_for_speed=5 --include_path="D:/TexasInstruments/ccsv6/ccs_base/msp430/include" --include_path="D:/TexasInstruments/ccsv6/tools/compiler/msp430_4.3.3/include" --advice:power=all -g --define=__MSP430G2553__ --diag_warning=225 --display_error_number --diag_wrap=off --printf_support=minimal --preproc_with_compile --preproc_dependency="m24lr_xx_i2c.pp" $(GEN_OPTS__FLAG) "$<"
	@echo 'Finished building: $<'
	@echo ' '

main.obj: ../main.c $(GEN_OPTS) $(GEN_HDRS)
	@echo 'Building file: $<'
	@echo 'Invoking: MSP430 Compiler'
	"D:/TexasInstruments/ccsv6/tools/compiler/msp430_4.3.3/bin/cl430" -vmsp --abi=eabi -O4 --opt_for_speed=5 --include_path="D:/TexasInstruments/ccsv6/ccs_base/msp430/include" --include_path="D:/TexasInstruments/ccsv6/tools/compiler/msp430_4.3.3/include" --advice:power=all -g --define=__MSP430G2553__ --diag_warning=225 --display_error_number --diag_wrap=off --printf_support=minimal --preproc_with_compile --preproc_dependency="main.pp" $(GEN_OPTS__FLAG) "$<"
	@echo 'Finished building: $<'
	@echo ' '

sensors.obj: ../sensors.c $(GEN_OPTS) $(GEN_HDRS)
	@echo 'Building file: $<'
	@echo 'Invoking: MSP430 Compiler'
	"D:/TexasInstruments/ccsv6/tools/compiler/msp430_4.3.3/bin/cl430" -vmsp --abi=eabi -O4 --opt_for_speed=5 --include_path="D:/TexasInstruments/ccsv6/ccs_base/msp430/include" --include_path="D:/TexasInstruments/ccsv6/tools/compiler/msp430_4.3.3/include" --advice:power=all -g --define=__MSP430G2553__ --diag_warning=225 --display_error_number --diag_wrap=off --printf_support=minimal --preproc_with_compile --preproc_dependency="sensors.pp" $(GEN_OPTS__FLAG) "$<"
	@echo 'Finished building: $<'
	@echo ' '


