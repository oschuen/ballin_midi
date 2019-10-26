package jsequencer.ui.model;

import midi.instrument.model.GuitarModel;
import midi.instrument.model.PercussionModel;
import midi.loop.config.ChannelConfig;
import midi.pad.ui.event.Runtime;

public class SongModel {
	private final int LOOP_NUMBER = 8;
	private final GuitarModel guitarModel = new GuitarModel();
	private final PercussionModel percussionModel = new PercussionModel();

	private final ChannelConfig[] channelConfig = new ChannelConfig[LOOP_NUMBER];

	public SongModel() {
		for (int i = 0; i < channelConfig.length; i++) {
			channelConfig[i] = new ChannelConfig();
		}
		ChannelConfig config = channelConfig[0];
		config.setBank(128);
		config.setProgram(9);
		config.setChannel(0);
		config.setChoir(0);
		config.setReverb(0);
		config.setMidiOut(0);
		Runtime.getRuntime().applyChannelConfig(config);
 
		config = channelConfig[1];
		config.setBank(0);
		config.setProgram(24);
		config.setChannel(4);
		config.setChoir(0);
		config.setReverb(127);
		config.setMidiOut(0);
		Runtime.getRuntime().applyChannelConfig(config);
	}

	public GuitarModel getGuitarModel() {
		return guitarModel;
	}

	public PercussionModel getPercussionModel() {
		return percussionModel;
	}

	public ChannelConfig getGuitarChannelConfig() {
		return channelConfig[1];
	}

	public ChannelConfig getPercussionChannelConfig() {
		return channelConfig[0];
	}
}
