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
package com.bladecoder.engineeditor.ui.components;

import com.bladecoder.engine.model.AbstractModel;
import com.bladecoder.engine.model.BaseActor;
import com.bladecoder.engine.model.Scene;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bladecoder.engine.actions.Param;
import com.bladecoder.engineeditor.Ctx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SceneActorInputPanel extends InputPanel {
	SelectBox<String> scene;
	EditableSelectBox<String> actor;
	Table panel;

	SceneActorInputPanel(Skin skin, String title, String desc, boolean mandatory, String defaultValue) {
		panel = new Table(skin);
		scene = new SelectBox<>(skin);
		actor = new EditableSelectBox<>(skin);

		panel.add(new Label(" Scene ", skin));
		panel.add(scene);
		panel.add(new Label("  Actor ", skin));
		panel.add(actor);

		NodeList scenes = Ctx.project.getSelectedChapter().getScenes();
		int l = scenes.getLength() + 1;
		
		String values[] = new String[l];

		values[0] = "";

		for (int i = 0; i < scenes.getLength(); i++) {
			values[i + 1] = ((Element) scenes.item(i)).getAttribute("id");
		}
		
		scene.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				sceneSelected();
			}
		});		

		init(skin, title, desc, panel, mandatory, defaultValue);
		scene.setItems(values);

		if (values.length > 0) {
			if (defaultValue != null)
				setText(defaultValue);
			else
				scene.setSelectedIndex(0);
		}
		
		
	}
	
	private void sceneSelected() {
		String s = scene.getSelected();

		// FIXME: When non mandatory, this check makes the actor selector full of values, but the scene one keeps being empty, which is confusing
		if(s == null || s.isEmpty()) {
			s = Ctx.project.getSelectedScene().getAttribute("id");
		}

		final Optional<Scene> scene = Ctx.project.getSelectedChapter().getSceneById(s);
		if (!scene.isPresent()) {
			actor.setItems();
			return;
		}
		final Collection<BaseActor> actors = scene.get().getActors().values();
		final List<String> values = actors.stream().map(AbstractModel::getId).collect(Collectors.toCollection(ArrayList::new));
		if (!isMandatory()) {
			values.add(0, "");
		}

		actor.setItems(values.toArray(new String[values.size()]));
	}
	
	public String getText() {
		return Param.toStringParam(scene.getSelected(), actor.getSelected());
	}

	public void setText(String s) {
		String out[] = Param.parseString2(s);
		
		int idx = scene.getItems().indexOf(out[0], false);
		if(idx != -1)
			scene.setSelectedIndex(idx);
		
//		idx = actor.getItems().indexOf(out[1], false);
//		if(idx != -1)
//			actor.setSelectedIndex(idx);
		sceneSelected();
		actor.setSelected(out[1]);
	}
}
