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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bladecoder.engine.loader.XMLConstants;
import com.bladecoder.engine.util.Config;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.model.Project;
import com.bladecoder.engineeditor.ui.components.PropertyTable;


public class WorldProps extends PropertyTable {
	public WorldProps(Skin skin) {
		super(skin);

		Ctx.project.addPropertyChangeListener(Project.NOTIFY_PROJECT_LOADED,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						setProject();
					}
				});
	}

	@Override
	protected void updateModel(String property, String value) {
		if (property.equals(XMLConstants.WIDTH_ATTR)) {
			Ctx.project.getWorld().setWidth(value);
		} else if (property.equals(Config.TITLE_PROP)) {
			Ctx.project.getProjectConfig().setProperty(Config.TITLE_PROP, value);
		} else if (property.equals(XMLConstants.HEIGHT_ATTR)) {
			Ctx.project.getWorld().setHeight(value);
		} else if (property.equals(Config.INVENTORY_POS_PROP)) {
			Ctx.project.getProjectConfig().setProperty(Config.INVENTORY_POS_PROP, value);
		} else if (property.equals(Config.INVENTORY_AUTOSIZE_PROP)) {
			Ctx.project.getProjectConfig().setProperty(Config.INVENTORY_AUTOSIZE_PROP, value);
		} else if (property.equals(Config.PIE_MODE_DESKTOP_PROP)) {
			Ctx.project.getProjectConfig().setProperty(Config.PIE_MODE_DESKTOP_PROP, value);
		} else if (property.equals(Config.DEBUG_PROP)) {
			Ctx.project.getProjectConfig().setProperty(Config.DEBUG_PROP, value);
		} else if (property.equals(Config.SHOW_DESC_PROP)) {
			Ctx.project.getProjectConfig().setProperty(Config.SHOW_DESC_PROP, value);			
		}
		
		Ctx.project.getWorld().setModified(); // TODO Add propertychange to Config
	}

	private void setProject() {
		clearProps();
		addProperty(XMLConstants.WIDTH_ATTR, Ctx.project.getWorld().getWidth());
		addProperty(XMLConstants.HEIGHT_ATTR, Ctx.project.getWorld().getHeight());		
		addProperty(Config.TITLE_PROP, Ctx.project.getTitle());
		addProperty(Config.INVENTORY_POS_PROP, Ctx.project.getProjectConfig().getProperty(Config.INVENTORY_POS_PROP, "down"));
		addProperty(Config.INVENTORY_AUTOSIZE_PROP, Boolean.parseBoolean(Ctx.project.getProjectConfig().getProperty(Config.INVENTORY_AUTOSIZE_PROP, "true")));
		addProperty(Config.PIE_MODE_DESKTOP_PROP, Boolean.parseBoolean(Ctx.project.getProjectConfig().getProperty(Config.PIE_MODE_DESKTOP_PROP, "false")));
		addProperty(Config.DEBUG_PROP, Boolean.parseBoolean(Ctx.project.getProjectConfig().getProperty(Config.DEBUG_PROP, "false")));
		addProperty(Config.SHOW_DESC_PROP, Boolean.parseBoolean(Ctx.project.getProjectConfig().getProperty(Config.SHOW_DESC_PROP, "true")));
		
		invalidateHierarchy();
	}
}
