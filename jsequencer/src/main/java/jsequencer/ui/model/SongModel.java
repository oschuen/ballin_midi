package jsequencer.ui.model;

import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import jmidi.gui.model.LayerModel;
import midi.instrument.model.GuitarModel;
import midi.instrument.model.PercussionModel;
import midi.instrument.model.SequencerModel;
import midi.loop.config.ChannelConfig;
import midi.loop.config.InputChannelConfig;
import midi.pad.ui.event.Runtime;

public class SongModel {
	private final GuitarModel[] guitarModel;
	private final PercussionModel[] percussionModel;
	private final SequencerModel[][] sequencerModel;
	private final LayerModel[] layerModel;

	private final ChannelConfig[] channelConfig;
	private final InputChannelConfig[] inputChannelConfig;

	public SongModel(final int numberOfLoops, final int numberOfLayer) {
		final int numberOfSequencer = numberOfLoops - 2;
		layerModel = new LayerModel[numberOfLoops];
		guitarModel = new GuitarModel[numberOfLayer];
		percussionModel = new PercussionModel[numberOfLayer];
		sequencerModel = new SequencerModel[numberOfSequencer][numberOfLayer];

		channelConfig = new ChannelConfig[numberOfLoops];
		inputChannelConfig = new InputChannelConfig[numberOfLoops];
		for (int i = 0; i < numberOfLoops; i++) {
			channelConfig[i] = new ChannelConfig();
			inputChannelConfig[i] = new InputChannelConfig();
			layerModel[i] = new LayerModel(numberOfLayer);
		}
		for (int i = 0; i < numberOfLayer; i++) {
			guitarModel[i] = new GuitarModel();
			percussionModel[i] = new PercussionModel();
			for (int j = 0; j < numberOfSequencer; ++j) {
				sequencerModel[j][i] = new SequencerModel();
			}
		}
		ChannelConfig config = channelConfig[0];
		config.setBank(128);
		config.setProgram(9);
		config.setChannel(0);
		config.setChoir(0);
		config.setReverb(0);
		config.setMidiOut(0);
		config.setVolume(127);
		Runtime.getRuntime().applyChannelConfig(config);

		config = channelConfig[1];
		config.setBank(0);
		config.setProgram(24);
		config.setChannel(4);
		config.setChoir(0);
		config.setReverb(127);
		config.setMidiOut(0);
		config.setVolume(127);
		Runtime.getRuntime().applyChannelConfig(config);
		final InputChannelConfig inConfig = inputChannelConfig[1];
		inConfig.setMidiIn(0);
		inConfig.setChannel(4);
	}

	public PercussionModel getPercussionModel(final int layer) {
		return percussionModel[layerModel[0].getLayer(layer)];
	}

	public GuitarModel getGuitarModel(final int layer) {
		return guitarModel[layerModel[1].getLayer(layer)];
	}

	public SequencerModel getSequencerModel(final int sequencer, final int layer) {
		return sequencerModel[sequencer][layerModel[2 + sequencer].getLayer(layer)];
	}

	public ChannelConfig getPercussionChannelConfig() {
		return channelConfig[0];
	}

	public ChannelConfig getGuitarChannelConfig() {
		return channelConfig[1];
	}

	public InputChannelConfig getGuitarInputConfig() {
		return inputChannelConfig[1];
	}

	public ChannelConfig getSequencerChannelConfig(final int sequencer) {
		return channelConfig[2 + sequencer];
	}

	public InputChannelConfig getSequencerInputChannelConfig(final int sequencer) {
		return inputChannelConfig[2 + sequencer];
	}

	public LayerModel getLayerModel(final int loop) {
		return layerModel[loop];
	}

	public JsonObject toJson() {
		final JsonArrayBuilder jconfig = Json.createArrayBuilder();
		final JsonArrayBuilder jInconfig = Json.createArrayBuilder();
		final JsonArrayBuilder jpercussion = Json.createArrayBuilder();
		final JsonArrayBuilder jguitar = Json.createArrayBuilder();
		final JsonArrayBuilder jsequencer = Json.createArrayBuilder();
		final JsonArrayBuilder jlayer = Json.createArrayBuilder();
		Arrays.stream(channelConfig).map(ChannelConfig::toJson).forEach(jconfig::add);
		Arrays.stream(inputChannelConfig).map(InputChannelConfig::toJson).forEach(jInconfig::add);
		Arrays.stream(percussionModel).map(PercussionModel::toJson).forEach(jpercussion::add);
		Arrays.stream(guitarModel).map(GuitarModel::toJson).forEach(jguitar::add);
		for (final SequencerModel[] sequencerModels : sequencerModel) {
			final JsonArrayBuilder jseq = Json.createArrayBuilder();
			Arrays.stream(sequencerModels).map(SequencerModel::toJson).forEach(jseq::add);
			jsequencer.add(jseq);
		}
		Arrays.stream(layerModel).map(LayerModel::toJson).forEach(jlayer::add);

		return Json.createObjectBuilder().add("config", jconfig).add("percussion", jpercussion)
				.add("guitar", jguitar).add("sequencer", jsequencer).add("layer", jlayer)
				.add("input", jInconfig).build();
	}

	public void fromJson(final JsonObject json) {
		final JsonArray jconfig = json.getJsonArray("config");
		for (int i = 0; i < jconfig.size(); ++i) {
			channelConfig[i].fromJson(jconfig.getJsonObject(i));
		}
		final JsonArray jInconfig = json.getJsonArray("input");
		for (int i = 0; jInconfig != null && i < jInconfig.size(); ++i) {
			inputChannelConfig[i].fromJson(jInconfig.getJsonObject(i));
		}
		final JsonArray jpercussion = json.getJsonArray("percussion");
		for (int i = 0; i < jpercussion.size(); ++i) {
			percussionModel[i].fromJson(jpercussion.getJsonObject(i));
		}
		final JsonArray jguitar = json.getJsonArray("guitar");
		for (int i = 0; i < jguitar.size(); ++i) {
			guitarModel[i].fromJson(jguitar.getJsonObject(i));
		}
		final JsonArray jsequencer = json.getJsonArray("sequencer");
		for (int i = 0; jsequencer != null && i < jsequencer.size(); ++i) {
			final JsonArray jseq = jsequencer.getJsonArray(i);
			for (int j = 0; jseq != null && j < jseq.size(); ++j) {
				sequencerModel[i][j].fromJson(jseq.getJsonObject(j));
			}
		}
		final JsonArray jlayer = json.getJsonArray("layer");
		for (int i = 0; i < jlayer.size(); ++i) {
			layerModel[i].fromJson(jlayer.getJsonObject(i));
		}
	}

}
