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
 * @since 03.11.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.junit.Test;

import midi.instrument.model.GuitarModel;
import midi.instrument.model.GuitarModel.GuitarInstrument;
import midi.instrument.model.PercussionModel;
import midi.instrument.model.PercussionModel.PercussionInstrument;
import midi.loop.LoopEvent;
import midi.loop.LoopEvent.COMMAND;

/**
 * @author oliver
 *
 */
public class SongModelTest {

	/**
	 * Test method for {@link jsequencer.ui.model.SongModel#toJson()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testToJson() throws IOException {
		final int numberOfLayer = 4;
		final int quarterPerPage = 3;
		final int pages = 2;
		final int quarterDivision = 5;
		final Random rand = new Random(System.currentTimeMillis());
		final SongModel model = new SongModel(8, numberOfLayer);
		final SongModel model2 = new SongModel(8, numberOfLayer);
		final Map<String, Object> properties = new HashMap<>();
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		final JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
		for (int i = 0; i < numberOfLayer; ++i) {
			model.getLayerModel(i).setLayer(i, i);
			final GuitarModel guitarModel = model.getGuitarModel(i);
			guitarModel.setNumberOfPages(pages);
			guitarModel.setQuarterDivision(quarterDivision);
			guitarModel.setQuarterPerPage(quarterPerPage);
			for (int step = 0; step < pages * quarterPerPage * quarterDivision; ++step) {
				final int currentStep = step;
				for (final GuitarInstrument guitarInstrument : GuitarInstrument.values()) {
					guitarModel.getLoopModel(guitarInstrument).ifPresent(it -> {
						if (currentStep % 3 == 0) {
							it.setStepEvent(null, currentStep);
						} else {
							it.setStepEvent(new LoopEvent(COMMAND.NOTE_ON, rand.nextInt(127),
									rand.nextInt(127), rand.nextInt(127)), currentStep);
						}
					});
				}
			}
			final PercussionModel percussionModel = model.getPercussionModel(i);
			percussionModel.setNumberOfPages(pages);
			percussionModel.setQuarterDivision(quarterDivision);
			percussionModel.setQuarterPerPage(quarterPerPage);
			for (int step = 0; step < pages * quarterPerPage * quarterDivision; ++step) {
				final int currentStep = step;
				for (final PercussionInstrument percussionInstrument : PercussionInstrument
						.values()) {
					percussionModel.getLoopModel(percussionInstrument).ifPresent(it -> {
						if (currentStep % 3 == 0) {
							it.setStepEvent(null, currentStep);
						} else {
							it.setStepEvent(new LoopEvent(COMMAND.NOTE_ON, rand.nextInt(127),
									rand.nextInt(127), rand.nextInt(127)), currentStep);
						}
					});
				}
			}
		}
		long millis = System.currentTimeMillis();
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			final JsonWriter writer = writerFactory.createWriter(out);
			writer.write(model.toJson());
			try (final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());) {
				final JsonReader reader = Json.createReader(in);
				model2.fromJson(reader.readObject());
				try (final ByteArrayOutputStream out2 = new ByteArrayOutputStream();) {
					final JsonWriter writer2 = writerFactory.createWriter(out2);
					writer2.write(model2.toJson());
					assertEquals(new String(out.toByteArray()), new String(out2.toByteArray()));
				}
			}
		}
		System.out.println(System.currentTimeMillis() - millis);
	}

}
