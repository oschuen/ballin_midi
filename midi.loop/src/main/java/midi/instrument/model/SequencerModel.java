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
 * @since 16.12.2019
 * @version 1.0
 * @author oliver
 */
package midi.instrument.model;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import midi.loop.LoopModel;

/**
 * @author oliver
 *
 */
public class SequencerModel {
	private final LoopModel model;

	public SequencerModel() {
		model = new LoopModel();
	}

	/**
	 * @return
	 * @see midi.loop.LoopModel#getQuarterPerPage()
	 */
	public int getQuarterPerPage() {
		return model.getQuarterPerPage();
	}

	/**
	 * @param quarterPerPage
	 * @see midi.loop.LoopModel#setQuarterPerPage(int)
	 */
	public void setQuarterPerPage(final int quarterPerPage) {
		model.setQuarterPerPage(quarterPerPage);
	}

	/**
	 * @return
	 * @see midi.loop.LoopModel#getNumberOfPages()
	 */
	public int getNumberOfPages() {
		return model.getNumberOfPages();
	}

	/**
	 * @param numberOfPages
	 * @see midi.loop.LoopModel#setNumberOfPages(int)
	 */
	public void setNumberOfPages(final int numberOfPages) {
		model.setNumberOfPages(numberOfPages);
	}

	/**
	 * @return
	 * @see midi.loop.LoopModel#getQuarterDivision()
	 */
	public int getQuarterDivision() {
		return model.getQuarterDivision();
	}

	/**
	 * @param quarterDivision
	 * @see midi.loop.LoopModel#setQuarterDivision(int)
	 */
	public void setQuarterDivision(final int quarterDivision) {
		model.setQuarterDivision(quarterDivision);
	}

	/**
	 * @return the model
	 */
	public LoopModel getModel() {
		return model;
	}

	public void clear() {
		model.clear();
	}

	public JsonObject toJson() {
		final JsonObjectBuilder json = Json.createObjectBuilder();
		json.add("model", model.toJson());

		return json.build();
	}

	public void fromJson(final JsonObject json) {
		model.fromJson(json.getJsonObject("model"));
	}
}
