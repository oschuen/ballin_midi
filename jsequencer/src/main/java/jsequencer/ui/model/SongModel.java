package jsequencer.ui.model;

import jmidi.gui.model.LayerModel;
import midi.instrument.model.GuitarModel;
import midi.instrument.model.PercussionModel;
import midi.loop.config.ChannelConfig;
import midi.pad.ui.event.Runtime;

public class SongModel {
	private final GuitarModel[] guitarModel;
	private final PercussionModel[] percussionModel;
	private final LayerModel[] layerModel;

	private final ChannelConfig[] channelConfig;

	public SongModel(final int numberOfLoops, final int numberOfLayer) {
		layerModel = new LayerModel[numberOfLoops];
		guitarModel = new GuitarModel[numberOfLayer];
		percussionModel = new PercussionModel[numberOfLayer];

		channelConfig = new ChannelConfig[numberOfLoops];
		for (int i = 0; i < numberOfLoops; i++) {
			channelConfig[i] = new ChannelConfig();
			layerModel[i] = new LayerModel(4);
		}
		for (int i = 0; i < numberOfLayer; i++) {
			guitarModel[i] = new GuitarModel();
			percussionModel[i] = new PercussionModel();
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

	public GuitarModel getGuitarModel(final int layer) {
		return guitarModel[layer];
	}

	public PercussionModel getPercussionModel(final int layer) {
		return percussionModel[layer];
	}

	public ChannelConfig getGuitarChannelConfig() {
		return channelConfig[1];
	}

	public ChannelConfig getPercussionChannelConfig() {
		return channelConfig[0];
	}

	public LayerModel getLayerModel(final int loop) {
		return layerModel[loop];
	}
}
