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

import com.bladecoder.engine.loader.XMLConstants;
import com.bladecoder.engine.model.SceneLayer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.ui.components.CellRenderer;
import com.bladecoder.engineeditor.ui.components.EditElementDialog;
import com.bladecoder.engineeditor.ui.components.ElementList;

// TODO: Visibility button

public class LayerList extends ElementList {
	
	private ImageButton upBtn;
	private ImageButton downBtn;
	private ImageButton visibilityBtn;

	public LayerList(Skin skin) {
		super(skin, false);

		list.setCellRenderer(listCellRenderer);
		
		visibilityBtn = new ImageButton(skin);
		toolbar.addToolBarButton(visibilityBtn, "ic_eye", "Toggle Visibility",
				"Toggle Visibility");

		visibilityBtn.setDisabled(false);

		visibilityBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				toggleVisibility();
			}
		});
		
		
		upBtn = new ImageButton(skin);
		downBtn = new ImageButton(skin);

		toolbar.addToolBarButton(upBtn, "ic_up", "Move up", "Move up");
		toolbar.addToolBarButton(downBtn, "ic_down", "Move down", "Move down");
		toolbar.pack();

		list.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int pos = list.getSelectedIndex();

				toolbar.disableEdit(pos == -1);
				upBtn.setDisabled(pos == -1 || pos == 0);
				downBtn.setDisabled(pos == -1 || pos == list.getItems().size - 1);
			}
		});

		upBtn.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				up();
			}
		});
		
		
		downBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				down();
			}
		});		
	}
	
	private void toggleVisibility() {

		Element e = list.getSelected();

		if (e == null)
			return;

		String value = e.getAttribute("visible");
		
		if(value.equals("true"))
			value = "false";
		else
			value = "true";
		
		e.setAttribute("visible", value);
		doc.setModified(e);
	}
	
	private void up() {
		int pos = list.getSelectedIndex();

		if (pos == -1 || pos == 0)
			return;

		Array<Element> items =  list.getItems();
		Element e = items.get(pos);
		Element e2 = items.get(pos - 1);

		Node parent = e.getParentNode();
		parent.removeChild(e);
		parent.insertBefore(e, e2);

		items.removeIndex(pos);
		items.insert( pos - 1, e);
		list.setSelectedIndex(pos - 1);
		upBtn.setDisabled(list.getSelectedIndex() == 0);
		downBtn.setDisabled(list.getSelectedIndex() == list.getItems().size - 1);

		doc.setModified(e);
	}

	private void down() {
		int pos = list.getSelectedIndex();
		Array<Element> items =  list.getItems();

		if (pos == -1 || pos == items.size - 1)
			return;

		Element e = items.get(pos);
		Element e2 = pos + 2 < items.size ? items.get(pos + 2) : null;

		Node parent = e.getParentNode();
		parent.removeChild(e);
		parent.insertBefore(e, e2);

		
		items.removeIndex(pos);
		items.insert(pos + 1, e);
		list.setSelectedIndex(pos + 1);
		upBtn.setDisabled(list.getSelectedIndex() == 0);
		downBtn.setDisabled(list.getSelectedIndex() == list.getItems().size - 1);

		doc.setModified(e);
	}	

	@Override
	protected EditAnnotatedDialog<SceneLayer> getEditElementDialogInstance(Element e) {
		return new EditAnnotatedDialog<>(skin, SceneLayer.class, doc, parent, XMLConstants.LAYER_TAG, e);
	}
	
	@Override
	protected void delete() {

		int pos = list.getSelectedIndex();

		if (pos == -1)
			return;

		if (list.getItems().size < 2) {
			String msg = "The layer will not be deleted, at least one layer must exist";
			Ctx.msg.show(getStage(), msg, 3);

			return;
		}
		
		// Check for actors inside this layer
		NodeList actors = parent.getElementsByTagName("actor");
		
		for(int i = 0; i < actors.getLength(); i++) {
			String layer = ((Element)(actors.item(i))).getAttribute("layer");
			if(layer.equals(list.getItems().get(pos).getAttribute("id"))) {
				String msg = "The layer will not be deleted, it is used by the actor " + 
						((Element)(actors.item(i))).getAttribute("id");
				Ctx.msg.show(getStage(), msg, 3);
				
				return;
			}
		}
		

		super.delete();
	}
	
	// -------------------------------------------------------------------------
	// ListCellRenderer
	// -------------------------------------------------------------------------
	private static final CellRenderer<Element> listCellRenderer = new CellRenderer<Element>() {

		@Override
		protected String getCellTitle(Element e) {
			String id  = e.getAttribute("id");

			return id;
		}

		@Override
		protected String getCellSubTitle(Element e) {
			String dynamic = e.getAttribute("dynamic");
			String visible = e.getAttribute("visible");

			StringBuilder sb = new StringBuilder();

			if (!dynamic.isEmpty())
				sb.append("dynamic: ").append(dynamic);
			if (!visible.isEmpty())
				sb.append(" visible: ").append(visible);
			
			return sb.toString();
		}
		
		@Override
		public TextureRegion getCellImage(Element e) {
			String visibility = e.getAttribute("visible");
			
			String u = null;

			if(visibility.equals("true")) {
				u = "eye";
			} else {
				u = "eye_disabled";
			}

			return Ctx.assetManager.getIcon(u);
		}
		
		@Override
		protected boolean hasSubtitle() {
			return true;
		}
		
		@Override
		protected boolean hasImage() {
			return true;
		}
	};	
}
