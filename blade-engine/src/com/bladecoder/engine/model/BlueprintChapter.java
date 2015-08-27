package com.bladecoder.engine.model;

import com.bladecoder.engine.actions.ModelPropertyType;
import com.bladecoder.engine.actions.Param;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlueprintChapter extends AbstractModel {
	@JsonProperty
	@JsonPropertyDescription("The initial scene for this chapter")
	@ModelPropertyType(Param.Type.SCENE)
	private String initScene;

	private Map<String, Scene> scenes;

	@JsonProperty
	private Collection<Scene> getScenes() {
		return scenes.values();
	}

	private void setScenes(Collection<Scene> scenes) {
		this.scenes = new LinkedHashMap<>();
		for (Scene scene : scenes) {
			this.scenes.put(scene.getId(), scene);
		}
	}

	public String getInitScene() {
		return initScene;
	}

	@TrackPropertyChanges
	public void setInitScene(String initScene) {
		this.initScene = initScene;
	}

	@TrackPropertyChanges
	public void addScene(Scene scene) {
		scenes.put(scene.getId(), scene);
	}

	@TrackPropertyChanges
	public void removeScene(Scene scene) {
		scenes.remove(scene.getId());
	}

	@Nullable
	public Scene getScene(String id) {
		return scenes.get(id);
	}
}
