/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
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
 ******************************************************************************/
package com.bladecoder.engineeditor.ui;

import java.io.File;
import java.util.Arrays;

import org.w3c.dom.Element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.bladecoder.engine.actions.Param;
import com.bladecoder.engine.loader.XMLConstants;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.model.BaseDocument;
import com.bladecoder.engineeditor.model.Project;
import com.bladecoder.engineeditor.ui.components.EditElementDialog;
import com.bladecoder.engineeditor.ui.components.InputPanel;
import com.bladecoder.engineeditor.ui.components.InputPanelFactory;
import com.bladecoder.engineeditor.utils.EditorLogger;

public class EditSceneDialog extends EditElementDialog {

	public static final String INFO = "An adventure is composed of many scenes (screens).\n" +
			"Inside a scene there are actors and a 'player'.\nThe player/user can interact with the actors through 'verbs'.";

	private static final int SCENE_ID_INPUTPANEL = 0;
	private static final int BACK_ATLAS_INPUTPANEL = 1;
	private static final int BACK_REGION_ID_INPUTPANEL = 2;
	private static final int LIGHTMAP_ATLAS_INPUTPANEL = 3;
	private static final int LIGHTMAP_REGION_ID_INPUTPANEL = 4;
	private static final int DEPTH_VECTOR_INPUTPANEL = 5;
	private static final int STATE_INPUTPANEL = 6;
	private static final int MUSIC_INPUTPANEL = 7;
	private static final int LOOP_INPUTPANEL = 8;
	private static final int INITIAL_DELAY_INPUTPANEL = 9;
	private static final int REPEAT_INPUTPANEL = 10;

	private String atlasList[] = getAtlasList();
	private String musicList[] = getMusicList();
	
	private InputPanel[] inputs = new InputPanel[11];
	
	private Image bgImage;
	private Container<Image> infoContainer;
	private TextureAtlas atlas;
	
	String attrs[] = {XMLConstants.ID_ATTR, XMLConstants.BACKGROUND_ATLAS_ATTR, XMLConstants.BACKGROUND_REGION_ATTR, XMLConstants.LIGHTMAP_ATLAS_ATTR, 
			XMLConstants.LIGHTMAP_REGION_ATTR, XMLConstants.DEPTH_VECTOR_ATTR, XMLConstants.STATE_ATTR, XMLConstants.MUSIC_ATTR, 
			XMLConstants.LOOP_MUSIC_ATTR, XMLConstants.INITIAL_MUSIC_DELAY_ATTR, XMLConstants.REPEAT_MUSIC_DELAY_ATTR};

	@SuppressWarnings("unchecked")
	public EditSceneDialog(Skin skin, BaseDocument doc, Element parent,
				Element e) {
		
		super(skin);
		
		inputs[SCENE_ID_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Scene ID",
				"The ID is mandatory for scenes. \nIDs can not contain '.' or '_' characters.");
		inputs[BACK_ATLAS_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Background Atlas",
				"The atlas where the background for the scene is located", atlasList, false);
		inputs[BACK_REGION_ID_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Background Region Id",
				"The region id for the background.", new String[0], false);
		inputs[LIGHTMAP_ATLAS_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Lightmap Atlas",
						"The atlas where the lightmap for the scene is located", atlasList, false);	
		inputs[LIGHTMAP_REGION_ID_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Lightmap Region Id",
				"The region id for the lightmap", new String[0], false);
		inputs[DEPTH_VECTOR_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Depth Vector",
						"X: the actor 'y' position for a 0.0 scale, Y: the actor 'y' position for a 1.0 scale.", Param.Type.VECTOR2, false);
		inputs[STATE_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "State",
				"The initial state for the scene.", false);
		inputs[MUSIC_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Music Filename",
				"The music for the scene", musicList, false);
		inputs[LOOP_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Loop Music",
				"If the music is playing in looping", Param.Type.BOOLEAN, false);
		inputs[INITIAL_DELAY_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Initial music delay",
				"The time to wait before playing", Param.Type.FLOAT, false);
		inputs[REPEAT_INPUTPANEL] = InputPanelFactory.createInputPanel(skin, "Repeat music delay",
				"The time to wait before repetitions", Param.Type.FLOAT, false);		
		
		bgImage = new Image();
		bgImage.setScaling(Scaling.fit);
		infoContainer = new Container<Image>(bgImage);
		setInfo(INFO);
		
		inputs[SCENE_ID_INPUTPANEL].setMandatory(true);

		inputs[BACK_ATLAS_INPUTPANEL].getField().addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					fillBGRegions(inputs[BACK_ATLAS_INPUTPANEL], inputs[BACK_REGION_ID_INPUTPANEL]);
				} catch(Exception e) {
					Ctx.msg.show(getStage(), "Error loading regions from selected atlas", 4);
				}
			}
		});
		

		inputs[BACK_REGION_ID_INPUTPANEL].getField()
			.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showBgImage(inputs[BACK_REGION_ID_INPUTPANEL].getText());
			}
		});
		
		inputs[LIGHTMAP_ATLAS_INPUTPANEL].getField().addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					fillLightmapRegions(inputs[LIGHTMAP_ATLAS_INPUTPANEL], inputs[LIGHTMAP_REGION_ID_INPUTPANEL]);
				} catch(Exception e) {
					Ctx.msg.show(getStage(), "Error loading regions from selected atlas", 4);
				}
			}
		});		
		
		try {
			fillBGRegions(inputs[BACK_ATLAS_INPUTPANEL], inputs[BACK_REGION_ID_INPUTPANEL]);
		} catch(Exception e2) {
			EditorLogger.error("Error loading regions from selected atlas");
		}
		
		init(inputs, attrs, doc, parent, XMLConstants.SCENE_TAG, e);
	}
	
	

	private void showBgImage(String r) {
		if(atlas == null)
			return;

		bgImage.setDrawable(new TextureRegionDrawable(atlas.findRegion(r)));
		

		infoContainer.prefWidth(250);
		infoContainer.prefHeight(250);
		setInfoWidget(infoContainer);
	}

	private void fillBGRegions(InputPanel atlasInput, InputPanel regionInput) {
		@SuppressWarnings("unchecked")
		SelectBox<String> cb = (SelectBox<String>) regionInput.getField();
		
//		cb.clearItems();
		cb.getItems().clear();
		
		if(atlas != null) {
			atlas.dispose();
			atlas = null;
		}
		
		if(inputs[BACK_ATLAS_INPUTPANEL].getText().isEmpty()) {
			setInfoWidget(new Label(INFO, getSkin()));
			return;
		}
		
		atlas = new TextureAtlas(Gdx.files.absolute(Ctx.project.getProjectPath() + Project.ATLASES_PATH + "/"
				+ Ctx.project.getResDir() + "/" + atlasInput.getText() + ".atlas"));

		Array<AtlasRegion> regions = atlas.getRegions();
		
		for (AtlasRegion r : regions)
			if(cb.getItems().indexOf(r.name, false) == -1)
				cb.getItems().add(r.name);

		cb.getList().setItems(cb.getItems());
		if (cb.getItems().size > 0)
			cb.setSelectedIndex(0);

		cb.invalidateHierarchy();

		showBgImage(regionInput.getText());
	}
	
	private void fillLightmapRegions(InputPanel atlasInput, InputPanel regionInput) {
		@SuppressWarnings("unchecked")
		SelectBox<String> cb = (SelectBox<String>) regionInput.getField();
		
//		cb.clearItems();
		cb.getItems().clear();
		
		TextureAtlas atlas = new TextureAtlas(Gdx.files.absolute(Ctx.project.getProjectPath() + Project.ATLASES_PATH + "/"
				+ Ctx.project.getResDir() + "/" + atlasInput.getText() + ".atlas"));

		Array<AtlasRegion> regions = atlas.getRegions();
		
		for (AtlasRegion r : regions)
			if(cb.getItems().indexOf(r.name, false) == -1)
				cb.getItems().add(r.name);

		cb.getList().setItems(cb.getItems());
		if (cb.getItems().size > 0)
			cb.setSelectedIndex(0);

		cb.invalidateHierarchy();

		atlas.dispose();
	}		
	
	@Override
	protected void create() {
		super.create();
		
		// CREATE DEFAULT LAYERS: BG, DYNAMIC, FG
		Element layer = doc.createElement(getElement(), "layer");
		layer.setAttribute("id", "foreground");
		layer.setAttribute("visible", "true");
		layer.setAttribute("dynamic", "false");
		getElement().appendChild(layer);
		
		layer = doc.createElement(getElement(), "layer");
		layer.setAttribute("id", "dynamic");
		layer.setAttribute("visible", "true");
		layer.setAttribute("dynamic", "true");
		getElement().appendChild(layer);
		
		layer = doc.createElement(getElement(), "layer");
		layer.setAttribute("id", "background");
		layer.setAttribute("visible", "true");
		layer.setAttribute("dynamic", "false");
		getElement().appendChild(layer);
	}

	private String[] getAtlasList() {
		String bgPath = Ctx.project.getProjectPath() + Project.ATLASES_PATH + "/"
				+ Ctx.project.getResDir();

		File f = new File(bgPath);

		String bgs[] = f.list((arg0, arg1) -> arg1.endsWith(".atlas"));

		Arrays.sort(bgs);

		for(int i = 0; i < bgs.length; i++) {
			int idx = bgs[i].lastIndexOf('.');
			if(idx != -1)
				bgs[i] = bgs[i].substring(0, idx);
		}
		
		return bgs;
	}
	
	private String[] getMusicList() {
		String path = Ctx.project.getProjectPath() + Project.MUSIC_PATH;

		File f = new File(path);

		String musicFiles[] = f.list((arg0, arg1) -> arg1.endsWith(".ogg") || arg1.endsWith(".mp3"));

		Arrays.sort(musicFiles);
		
		String musicFiles2[] = new String[musicFiles.length + 1];
		musicFiles2[0] = "";

		System.arraycopy(musicFiles, 0, musicFiles2, 1, musicFiles.length);

		return musicFiles2;
	}
	
	@Override
	protected void result(Object object) {
		if(atlas != null) {
			atlas.dispose();
		}
		
		super.result(object);
	}
}
