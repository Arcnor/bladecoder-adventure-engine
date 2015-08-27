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

import com.bladecoder.engine.model.BaseActor;
import com.bladecoder.engine.model.Scene;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bladecoder.engine.actions.Param;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.utils.ModelUtils;

import java.util.Collection;
import java.util.Optional;

public class SceneActorInputPanel extends InputPanel {
	private SelectBox<String> scene;
	private EditableSelectBox<String> actor;
	private Table panel;

	SceneActorInputPanel(Skin skin, String title, String desc, boolean mandatory, String defaultValue) {
		panel = new Table(skin);
		scene = new SelectBox<>(skin);
		actor = new EditableSelectBox<>(skin);

		panel.add(new Label(" Scene ", skin));
		panel.add(scene);
		panel.add(new Label("  Actor ", skin));
		panel.add(actor);

		scene.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				sceneSelected();
			}
		});

		final Collection<Scene> scenes = Ctx.project.getSelectedChapter().getScenes();

		scene.setItems(ModelUtils.listIds(scenes, true));

		// FIXME: This is also done by init(), except for the scene.setSelectedIndex() part
		if (defaultValue != null)
			setText(defaultValue);
		else
			scene.setSelectedIndex(0);

		init(skin, title, desc, panel, mandatory, defaultValue);
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

		actor.setItems(ModelUtils.listIds(actors, isMandatory()));
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
