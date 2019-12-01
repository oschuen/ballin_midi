package jsequencer.ui.model;

import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import jmidi.gui.model.LayerModel;
import midi.instrument.model.GuitarModel;
import midi.instrument.model.PercussionModel;
import midi.loop.config.ChannelConfig;
import midi.loop.config.InputChannelConfig;
import midi.pad.ui.event.Runtime;

public class SongModel {
	private final GuitarModel[] guitarModel;
	private final PercussionModel[] percussionModel;
	private final LayerModel[] layerModel;

	private final ChannelConfig[] channelConfig;
	private final InputChannelConfig[] inputChannelConfig;

	public SongModel(final int numberOfLoops, final int numberOfLayer) {
		layerModel = new LayerModel[numberOfLoops];
		guitarModel = new GuitarModel[numberOfLayer];
		percussionModel = new PercussionModel[numberOfLayer];

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

	public ChannelConfig getPercussionChannelConfig() {
		return channelConfig[0];
	}

	public ChannelConfig getGuitarChannelConfig() {
		return channelConfig[1];
	}

	public InputChannelConfig getGuitarInputConfig() {
		return inputChannelConfig[1];
	}

	public LayerModel getLayerModel(final int loop) {
		return layerModel[loop];
	}

	public JsonObject toJson() {
		final JsonArrayBuilder jconfig = Json.createArrayBuilder();
		final JsonArrayBuilder jInconfig = Json.createArrayBuilder();
		final JsonArrayBuilder jpercussion = Json.createArrayBuilder();
		final JsonArrayBuilder jguitar = Json.createArrayBuilder();
		final JsonArrayBuilder jlayer = Json.createArrayBuilder();
		Arrays.stream(channelConfig).map(ChannelConfig::toJson).forEach(jconfig::add);
		Arrays.stream(inputChannelConfig).map(InputChannelConfig::toJson).forEach(jInconfig::add);
		Arrays.stream(percussionModel).map(PercussionModel::toJson).forEach(jpercussion::add);
		Arrays.stream(guitarModel).map(GuitarModel::toJson).forEach(jguitar::add);
		Arrays.stream(layerModel).map(LayerModel::toJson).forEach(jlayer::add);

		return Json.createObjectBuilder().add("config", jconfig).add("percussion", jpercussion)
				.add("guitar", jguitar).add("layer", jlayer).add("input", jInconfig).build();
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
		final JsonArray jlayer = json.getJsonArray("layer");
		for (int i = 0; i < jlayer.size(); ++i) {
			layerModel[i].fromJson(jlayer.getJsonObject(i));
		}
	}

}
