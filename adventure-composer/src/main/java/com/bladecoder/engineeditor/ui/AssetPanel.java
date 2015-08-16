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

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.model.Project;
import com.bladecoder.engineeditor.ui.components.HeaderPanel;
import com.bladecoder.engineeditor.ui.components.TabPanel;

public class AssetPanel extends HeaderPanel {
	private TabPanel tabPanel;
	
	public AssetPanel(Skin skin) {
		super(skin, "ASSETS");	
		
		tabPanel = new TabPanel(skin);
		tabPanel.addTab("Assets", new AssetsList(skin));
		tabPanel.addTab("Resolutions", new ResolutionList(skin));
		
		setContent(tabPanel);

		Ctx.project.addPropertyChangeListener(Project.NOTIFY_PROJECT_LOADED, e -> setTile("ASSETS - " + Ctx.project.getProjectDir().getName()));
	}
}
