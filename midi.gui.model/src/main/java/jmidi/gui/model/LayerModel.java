/**
 * Copyright (C) 2019 Oliver Schünemann
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
 * @since 31.10.2019
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.model;

/**
 * @author oliver
 *
 */
public class LayerModel {

	final private IntegerModel[] layers;

	public LayerModel(final int number) {
		layers = new IntegerModel[number];
		for (int i = 0; i < layers.length; i++) {
			layers[i] = new IntegerModel(0, number, i == 0 ? 1 : 0);
		}
	}

	public int getLayer(final int column) {
		if (0 <= column && column < layers.length) {
			return layers[column].getValue();
		}
		return 0;
	}

	public void increment(final int column) {
		if (0 <= column && column < layers.length) {
			final IntegerModel integerModel = layers[column];
			final int currentValue = integerModel.getValue();
			integerModel.increment();
			if (integerModel.getValue() == currentValue) {
				integerModel.setValue(integerModel.getMinValue());
			}
		}
	}
}
