/**
 * Copyright (C) 2017 Oliver Schünemann
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
 * @since 26.03.2017
 * @version 1.0
 * @author oliver
 */
package midi.loop;

import midi.loop.beat.Beat.BeatListener;
import midi.loop.config.ChannelConfig;

/**
 * @author oliver
 *
 */
public class Loop implements BeatListener {

	private final ChannelConfig config;
	private final LoopModel model;

	public Loop(final ChannelConfig config, final LoopModel model) {
		super();
		this.config = config;
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		// TODO Auto-generated method stub

	}

}
