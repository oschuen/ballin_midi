/**
 * Copyright (C) 2020 Oliver Schünemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @since 11.01.2020
 * @version 1.0
 * @author oliver
 */
package midi.message;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;

/**
 * @author oliver
 *
 */
public class Display {

	private final GpioController gpio;

	private final GpioPinDigitalOutput lcd_rs;
	private final GpioPinDigitalOutput lcd_e;
	private final GpioPinDigitalOutput lcd_data4;
	private final GpioPinDigitalOutput lcd_data5;
	private final GpioPinDigitalOutput lcd_data6;
	private final GpioPinDigitalOutput lcd_data7;

	public static final int LCD_WIDTH = 16;
	public static final int LCD_LINE_1 = 0x80;
	public static final int LCD_LINE_2 = 0x90;
	public static final int LCD_LINE_3 = 0x88;
	public static final int LCD_LINE_4 = 0x98;

	public static final PinState LCD_CHR = PinState.HIGH;
	public static final PinState LCD_CMD = PinState.LOW;
	public static final int E_PULSE = 500;
	public static final int E_DELAY = 250;

	public Display() {
		GpioFactory.setDefaultProvider(
				new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
		gpio = GpioFactory.getInstance();
		lcd_rs = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_04, "LCD_RS", PinState.LOW);
		lcd_e = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_17, "LCD_E", PinState.LOW);
		lcd_data4 = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_18, "LCD_DATA4", PinState.LOW);
		lcd_data5 = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_22, "LCD_DATA5", PinState.LOW);
		lcd_data6 = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_23, "LCD_DATA6", PinState.LOW);
		lcd_data7 = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_24, "LCD_DATA7", PinState.LOW);

		lcd_rs.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
		lcd_e.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
		lcd_data4.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
		lcd_data5.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
		lcd_data6.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
		lcd_data7.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
	}

	private void setBit(final GpioPinDigitalOutput out, final boolean set) {
		if (set) {
			out.high();
		} else {
			out.low();
		}
	}

	private void sleepMicros(final int ms) {
		try {
			TimeUnit.MICROSECONDS.sleep(ms);
		} catch (final InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	public void sendByte(final int b, final PinState mode) {

		lcd_rs.setState(mode);
		setBit(lcd_data4, (b & 0x10) == 0x10);
		setBit(lcd_data5, (b & 0x20) == 0x20);
		setBit(lcd_data6, (b & 0x40) == 0x40);
		setBit(lcd_data7, (b & 0x80) == 0x80);
		sleepMicros(E_DELAY);
		lcd_e.high();
		sleepMicros(E_PULSE);
		lcd_e.low();
		sleepMicros(E_DELAY);
		setBit(lcd_data4, (b & 0x01) == 0x01);
		setBit(lcd_data5, (b & 0x02) == 0x02);
		setBit(lcd_data6, (b & 0x04) == 0x04);
		setBit(lcd_data7, (b & 0x08) == 0x08);
		sleepMicros(E_DELAY);
		lcd_e.high();
		sleepMicros(E_PULSE);
		lcd_e.low();
		sleepMicros(E_DELAY);
	}

	public void display_init() {
		sendByte(0x33, LCD_CMD); // 00110011
		sendByte(0x32, LCD_CMD); // 00110010
		sendByte(0x28, LCD_CMD); // 00101000 4 bit interface, extended
		// instruction off
		sendByte(0x0C, LCD_CMD); // 00001100 Display = on, Cursor = 0ff, Blink =
									// 0ff
		sendByte(0x06, LCD_CMD); // 00000110
		sendByte(0x01, LCD_CMD); // 00000001 Clear display
	}

	public void sendString(final String str) {
		final byte[] b = str.getBytes(StandardCharsets.US_ASCII);
		for (int i = 0; i < b.length && i < LCD_WIDTH; i++) {
			sendByte(b[i], LCD_CHR);
		}
		for (int i = b.length; i < LCD_WIDTH; i++) {
			sendByte(' ', LCD_CHR);
		}
	}
}
