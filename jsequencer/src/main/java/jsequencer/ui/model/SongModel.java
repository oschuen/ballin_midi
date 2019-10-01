package jsequencer.ui.model;

import midi.instrument.model.GuitarModel;
import midi.instrument.model.PercussionModel;
import midi.loop.config.ChannelConfig;

public class SongModel {
	private final int LOOP_NUMBER = 8;
	private final GuitarModel guitarModel = new GuitarModel();
	private final PercussionModel percussionModel = new PercussionModel();


	private ChannelConfig[] channelConfig = new ChannelConfig[LOOP_NUMBER];
	
	public SongModel() {
		for (int i = 0; i < channelConfig.length; i++) {
			channelConfig[i] = new ChannelConfig();
		}
	}
	
	public GuitarModel getGuitarModel() {
		return guitarModel;
	}

	public PercussionModel getPercussionModel() {
		return percussionModel;
	}	
}
