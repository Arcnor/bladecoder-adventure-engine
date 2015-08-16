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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.model.Project;
import com.bladecoder.engineeditor.ui.components.CellRenderer;
import com.bladecoder.engineeditor.ui.components.EditList;
import com.bladecoder.engineeditor.utils.DesktopUtils;

public class ResolutionList extends EditList<String> {

	public ResolutionList(Skin skin) {
		super(skin);
		toolbar.hideCopyPaste();		

		list.setCellRenderer(listCellRenderer);
		
		list.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int pos = list.getSelectedIndex();

				toolbar.disableEdit(pos == -1);
			}
		});

		Ctx.project.addPropertyChangeListener(Project.NOTIFY_PROJECT_LOADED,
				arg0 -> {
					toolbar.disableCreate(Ctx.project.getProjectDir() == null);
					addResolutions();
				});
	}

	private void addResolutions() {
		if (Ctx.project.getProjectDir() != null) {

			list.getItems().clear();

			ArrayList<String> tmp = new ArrayList<>(Ctx.project.getResolutions());

			Collections.sort(tmp);

			list.getItems().addAll(tmp.toArray(new String[tmp.size()]));

			if (list.getItems().size > 0) {
				list.setSelectedIndex(0);
			}
		}

		toolbar.disableCreate(Ctx.project.getProjectDir() == null);
	}

	@Override
	public void create() {
		CreateResolutionDialog dialog = new CreateResolutionDialog(skin);
		dialog.show(getStage());
		dialog.setListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				addResolutions();
			}});
	}

	@Override
	public void edit() {

	}

	@Override
	public void delete() {
		int index = list.getSelectedIndex();
		String r = list.getItems().get(index);
		
		if(r.equals("1")) {
			Ctx.msg.show(getStage(),"Initial resolution cannot be deleted", 3);
			
			return;
		}

		removeDir(Ctx.project.getProjectDir() + "/" + Project.IMAGE_PATH
				+ "/" + r);
		removeDir(Ctx.project.getProjectDir() + "/" + Project.UI_PATH + "/"
				+ r);
		removeDir(Ctx.project.getProjectDir() + "/" + Project.ATLASES_PATH
				+ "/" + r);

		addResolutions();
	}

	private void removeDir(String dir) {
		try {
			DesktopUtils.removeDir(dir);
		} catch (IOException e) {
			String msg = "Something went wrong while deleting the resolution.\n\n"
					+ e.getClass().getSimpleName() + " - " + e.getMessage();
			Ctx.msg.show(getStage(),msg, 2);
			e.printStackTrace();
		}
	}
	
	@Override
	protected void copy() {
	
	}

	@Override
	protected void paste() {
	
	}

	// -------------------------------------------------------------------------
	// ListCellRenderer
	// -------------------------------------------------------------------------
	private static final CellRenderer<String> listCellRenderer = new CellRenderer<String>() {
		@Override
		protected String getCellTitle(String r) {
			return r;
		}
	};
}
