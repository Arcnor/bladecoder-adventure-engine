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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bladecoder.engine.actions.Param;
import com.bladecoder.engine.loader.XMLConstants;
import com.bladecoder.engineeditor.model.BaseDocument;
import com.bladecoder.engineeditor.model.ChapterDocument;
import com.bladecoder.engineeditor.ui.components.EditElementDialog;
import com.bladecoder.engineeditor.ui.components.InputPanel;
import com.bladecoder.engineeditor.ui.components.InputPanelFactory;
import com.bladecoder.engineeditor.ui.components.OptionsInputPanel;

public class EditActorDialog extends EditElementDialog {

	public static final String TYPES_INFO[] = {
			"Background actors don't have sprites or animations. The are used to use objects drawed in the background",
			"Sprite actors have one or several sprites or animations",
			"Character actors have dialogs and stand, walk and talk animations",
			"Obstacle actors forbids zones for walking actors"
			};
	
	public static final String RENDERERS_INFO[] = {
			"Atlas actor allows 2d image and animations",
			"Spine actors allow Spine 2d skeletal animations",
			"3d actors allow 3d models and animations",
			"Image actors show image files"
			};

	public static final int INPUT_ACTOR_TYPE = 0;
	public static final int INPUT_ACTOR_ID = 1;
	public static final int INPUT_ACTOR_LAYER = 2;
	public static final int INPUT_VISIBLE = 3;
	public static final int INPUT_INTERACTION = 4;
	public static final int INPUT_DESCRIPTION = 5;
	public static final int INPUT_STATE = 6;
	public static final int INPUT_ACTOR_RENDERER = 7;
	public static final int INPUT_DEPTH_TYPE = 8;
	public static final int INPUT_SCALE = 9;
	public static final int INPUT_ZINDEX = 10;
	public static final int INPUT_WALKING_SPEED = 11;
	public static final int INPUT_SPRITE_DIMENSIONS = 12;
	public static final int INPUT_CAMERA_NAME = 13;
	public static final int INPUT_CAMERA_FOV = 14;

	private InputPanel typePanel;
	private InputPanel rendererPanel;

	String attrs[] = { XMLConstants.TYPE_ATTR, XMLConstants.ID_ATTR, XMLConstants.LAYER_ATTR, XMLConstants.VISIBLE_ATTR, XMLConstants.INTERACTION_ATTR, XMLConstants.DESC_ATTR, XMLConstants.STATE_ATTR,
			 XMLConstants.RENDERER_ATTR, XMLConstants.DEPTH_TYPE_ATTR, XMLConstants.SCALE_ATTR, XMLConstants.ZINDEX_ATTR, XMLConstants.WALKING_SPEED_ATTR, XMLConstants.SPRITE_SIZE_ATTR, XMLConstants.CAMERA_NAME_ATTR, XMLConstants.FOV_ATTR };
	
	private InputPanel[] inputs = new InputPanel[attrs.length];

	@SuppressWarnings("unchecked")
	public EditActorDialog(Skin skin, BaseDocument doc, Element parent,
			Element e) {
		super(skin);

		inputs[INPUT_ACTOR_TYPE] = InputPanelFactory.createInputPanel(skin, "Actor Type",
				"Actors can be from different types",
				ChapterDocument.ACTOR_TYPES, true);

		inputs[INPUT_ACTOR_ID] = InputPanelFactory.createInputPanel(skin, "Actor ID",
				"IDs can not contain '.' or '_' characters.", true);

		inputs[INPUT_ACTOR_LAYER] = InputPanelFactory.createInputPanel(skin, "Actor Layer",
				"The layer for drawing order", getLayers(parent), true);
		
		inputs[INPUT_VISIBLE] = InputPanelFactory.createInputPanel(skin, "Visible", "The actor visibility.",
				Param.Type.BOOLEAN, false);
		
		
		inputs[INPUT_INTERACTION] = InputPanelFactory.createInputPanel(skin, "Interaction",
				"True when the actor reacts to the user input.",
				Param.Type.BOOLEAN, false);

		inputs[INPUT_DESCRIPTION] = InputPanelFactory.createInputPanel(skin, "Description",
				"The text showed when the cursor is over the actor.");
		inputs[INPUT_STATE] = InputPanelFactory.createInputPanel(
				skin,
				"State",
				"Initial state of the actor. Actors can be in differentes states during the game.");		
		
		inputs[INPUT_ACTOR_RENDERER] = InputPanelFactory.createInputPanel(skin, "Actor Renderer",
				"Actors can be renderer from several sources",
				ChapterDocument.ACTOR_RENDERERS, true);

		inputs[INPUT_DEPTH_TYPE] = InputPanelFactory.createInputPanel(skin, "Depth Type",
				"Scene fake depth for scaling", new String[] { "none",
						"vector"}, true);
		
		inputs[INPUT_SCALE] = InputPanelFactory.createInputPanel(skin, "Scale",
				"The sprite scale", Param.Type.FLOAT, false, "1",
				null);
		
		inputs[INPUT_ZINDEX] = InputPanelFactory.createInputPanel(skin, "zIndex",
				"The order to draw.", Param.Type.FLOAT, false, "0",
				null);
		
		inputs[INPUT_WALKING_SPEED] = InputPanelFactory.createInputPanel(skin, "Walking Speed",
				"The walking speed in pix/sec. Default 700.", Param.Type.FLOAT,
				false);
		
		inputs[INPUT_SPRITE_DIMENSIONS] = InputPanelFactory.createInputPanel(skin, "Sprite Dimensions",
				"The size of the 3d sprite", Param.Type.DIMENSION, true);
		inputs[INPUT_CAMERA_NAME] = InputPanelFactory.createInputPanel(skin, "Camera Name",
				"The name of the camera in the model", Param.Type.STRING, true,
				"Camera", null);
		inputs[INPUT_CAMERA_FOV] = InputPanelFactory.createInputPanel(skin, "Camera FOV",
				"The camera field of view", Param.Type.FLOAT, true, "49.3",
				null);
		

		setInfo(TYPES_INFO[0]);

		typePanel = inputs[INPUT_ACTOR_TYPE];
		rendererPanel = inputs[INPUT_ACTOR_ID];

		typePanel.getField()
				.addListener(new ChangeListener() {

					@Override
					public void changed(ChangeEvent event, Actor actor) {
						typeChanged();
					}
				});
		
		rendererPanel.getField()
			.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				rendererChanged();
			}
		});

		init(inputs, attrs, doc, parent, XMLConstants.ACTOR_TAG, e);

		typeChanged();

	}

	private String[] getLayers(Element parent) {
		NodeList layerList = parent.getElementsByTagName(XMLConstants.LAYER_TAG);
		
		String[] layers = new String[layerList.getLength()];
		
		for(int i = 0; i < layerList.getLength(); i++) {
			layers[i] = ((Element)(layerList.item(i))).getAttribute(XMLConstants.ID_ATTR);
		}
		
		return layers;
	}

	private void typeChanged() {
		int i = ((OptionsInputPanel)typePanel).getSelectedIndex();

		setInfo(TYPES_INFO[i]);
		
		hideAllInputs();
		
		if (!ChapterDocument.ACTOR_TYPES[i]
				.equals(XMLConstants.OBSTACLE_VALUE)) {
			setVisible(inputs[INPUT_INTERACTION],true);
			setVisible(inputs[INPUT_DESCRIPTION],true);
			setVisible(inputs[INPUT_STATE],true);
		}

		if (ChapterDocument.ACTOR_TYPES[i]
				.equals(XMLConstants.SPRITE_VALUE) || 
				ChapterDocument.ACTOR_TYPES[i]
						.equals(XMLConstants.CHARACTER_VALUE)) {
			setVisible(inputs[INPUT_ACTOR_RENDERER],true);
			setVisible(inputs[INPUT_DEPTH_TYPE],true);
			setVisible(inputs[INPUT_SCALE],true);
			setVisible(inputs[INPUT_ZINDEX],true);
		}
		
		if (ChapterDocument.ACTOR_TYPES[i]
						.equals(XMLConstants.CHARACTER_VALUE)) {
			setVisible(inputs[INPUT_WALKING_SPEED],true);
		}
		
		rendererChanged();
	}
	
	private void rendererChanged() {
		int i = ((OptionsInputPanel)rendererPanel).getSelectedIndex();

//		setInfo(RENDERERS_INFO[i]);

		setVisible(inputs[INPUT_SPRITE_DIMENSIONS],false);
		setVisible(inputs[INPUT_CAMERA_NAME],false);
		setVisible(inputs[INPUT_CAMERA_FOV],false);

		if (rendererPanel.isVisible() &&
				ChapterDocument.ACTOR_RENDERERS[i]
				.equals(XMLConstants.S3D_VALUE)) {
			setVisible(inputs[INPUT_SPRITE_DIMENSIONS],true);
			setVisible(inputs[INPUT_CAMERA_NAME],true);
			setVisible(inputs[INPUT_CAMERA_FOV],true);
		}
	}
	
	private void hideAllInputs() {
				
		for(int idx = 4; idx < inputs.length; idx ++) {
			InputPanel i = inputs[idx];
			
			setVisible(i, false);
		}
	}

	@Override
	protected void fill() {
		int i = ((OptionsInputPanel)typePanel).getSelectedIndex();
		if (e.getAttribute(XMLConstants.BBOX_ATTR).isEmpty() && ChapterDocument.ACTOR_TYPES[i]
				.equals(XMLConstants.BACKGROUND_VALUE) || ChapterDocument.ACTOR_TYPES[i]
						.equals(XMLConstants.OBSTACLE_VALUE)) {
			((ChapterDocument) doc).setBbox(e, null);
		}
		
		if(((ChapterDocument)doc).getPos(e) == null)
			((ChapterDocument) doc).setPos(e, new Vector2(0, 0));

		super.fill();
	}
}
