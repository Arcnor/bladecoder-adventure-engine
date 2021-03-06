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
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.bladecoder.engine.loader.XMLConstants;
import com.bladecoder.engine.util.EngineLogger;
import com.bladecoder.engineeditor.Ctx;
import com.bladecoder.engineeditor.model.Project;
import com.bladecoder.engineeditor.model.WorldDocument;
import com.bladecoder.engineeditor.ui.components.CellRenderer;
import com.bladecoder.engineeditor.ui.components.EditElementDialog;
import com.bladecoder.engineeditor.ui.components.ElementList;
import com.bladecoder.engineeditor.utils.EditorLogger;

public class SceneList extends ElementList {

	private ImageButton initBtn;
	private SelectBox<String> chapters;
	private HashMap<String, TextureRegion> bgIconCache = new HashMap<String, TextureRegion>();
	private boolean disposeBgCache = false;

	public SceneList(Skin skin) {
		super(skin, true);

		HorizontalGroup chapterPanel = new HorizontalGroup();
		chapters = new SelectBox<String>(skin);
		chapters.setFillParent(true);
		
		chapterPanel.addActor(new Label("CHAPTER ", skin, "big"));
		chapterPanel.addActor(chapters);
		
		clearChildren();
		
		add(chapterPanel).expandX().fillX();
		row();
		add(toolbar).expandX().fillX();
		row().fill();
		add(container).expandY().fill();

		initBtn = new ImageButton(skin);
		toolbar.addToolBarButton(initBtn, "ic_check",
				"Set init scene", "Set init scene");

		initBtn.setDisabled(true);

		list.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int pos = list.getSelectedIndex();

				if (pos == -1) {
					Ctx.project.setSelectedScene(null);
				} else {
					Element a = list.getItems().get(pos);
					Ctx.project.setSelectedScene(a);
				}

				toolbar.disableEdit(pos == -1);
				initBtn.setDisabled(pos == -1);
			}
		});

		list.setCellRenderer(listCellRenderer);

		initBtn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setDefault();
			}

		});

		Ctx.project.addPropertyChangeListener(Project.NOTIFY_PROJECT_LOADED,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						toolbar.disableCreate(Ctx.project.getProjectDir() == null);

						disposeBgCache = true;
						addChapters();
					}
				});

		chapters.addListener(chapterListener);

		Ctx.project.getWorld().addPropertyChangeListener(
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						EditorLogger.debug(evt.getPropertyName() + " NEW:"
								+ evt.getNewValue() + " OLD:"
								+ evt.getOldValue());

						if (evt.getPropertyName().equals(XMLConstants.CHAPTER_TAG)) {
							addChapters();
						} else if (evt.getPropertyName().equals(
								"ELEMENT_DELETED")) {
							Element e = (Element) evt.getNewValue();

							if (e.getTagName().equals(XMLConstants.CHAPTER_TAG)) {								
								addChapters();
							}
						}
					}
				});
		
	}

	ChangeListener chapterListener = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			String selChapter = (String) chapters.getSelected();

			if (selChapter != null && !selChapter.equals(Ctx.project.getSelectedChapter().getId())) {

				// Save the project when changing chapter
				try {
					Ctx.project.saveProject();
				} catch (IOException | TransformerException e1) {
					Ctx.msg.show(getStage(),
							"Error saving project", 3);
					EditorLogger.error(e1.getMessage());
				}

				try {
					
					if(selChapter != null)
						Ctx.project.loadChapter(selChapter);
					
					doc = Ctx.project.getSelectedChapter();
					
					addElements(doc, doc.getElement(), "scene");
				} catch (ParserConfigurationException | SAXException
						| IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	};

	public void addChapters() {
		WorldDocument w = Ctx.project.getWorld();
		String[] nl = w.getChapters();
		Array<String> array = new Array<String>();

		for (int i = 0; i < nl.length; i++) {
			array.add(nl[i]);
		}

		chapters.setItems(array);
		chapters.setSelected(Ctx.project.getSelectedChapter().getId());
		invalidate();
	}

	private void setDefault() {

		int pos = list.getSelectedIndex();

		if (pos == -1)
			return;

		String id = list.getItems().get(pos).getAttribute(XMLConstants.ID_ATTR);

		doc.setRootAttr((Element) list.getItems().get(pos).getParentNode(),
				XMLConstants.INIT_SCENE_ATTR, id);

	}
	
	@Override
	protected void delete() {
		int pos = list.getSelectedIndex();

		if (pos == -1)
			return;

		Element e = list.getItems().get(pos);
		
		// delete init_scene attr if the scene to delete is the chapter init_scene
		if(((Element)e.getParentNode()).getAttribute(XMLConstants.INIT_SCENE_ATTR).equals(e.getAttribute(XMLConstants.ID_ATTR))) {
			((Element)e.getParentNode()).removeAttribute(XMLConstants.INIT_SCENE_ATTR);
		}
		
		super.delete();
	}

	@Override
	protected EditElementDialog getEditElementDialogInstance(
			Element e) {
		return new EditSceneDialog(skin, doc, parent, e);
	}
	
	public TextureRegion getBgIcon(String atlas, String region) {
		
		// check here for dispose instead in project loading because the opengl context lost in new project thread		
		if(disposeBgCache) {
			dispose();
			disposeBgCache = false;
		}
		
		
		String s = atlas + "#" + region;
		TextureRegion icon = bgIconCache.get(s);
		
		if(icon == null) {
			try {
				Batch batch = getStage().getBatch();
				batch.end();
				bgIconCache.put(s, createBgIcon(atlas, region));
				batch.begin();
			} catch (Exception e) {
				EngineLogger.error("Error creating Background icon");
				return null;
			}
			
			icon = bgIconCache.get(s);
		}
		
		return icon;
	}
	
	public void dispose() {
		for(TextureRegion r:bgIconCache.values())
			r.getTexture().dispose();
		
		bgIconCache.clear();
	}
	
	private TextureRegion createBgIcon(String atlas, String region) {
		TextureAtlas a = new TextureAtlas(Gdx.files.absolute(Ctx.project.getProjectPath() + "/" + Project.ATLASES_PATH + 
				"/1/" + atlas + ".atlas"));
		AtlasRegion r = a.findRegion(region);	
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, 200, (int)(r.getRegionHeight() * 200f / r.getRegionWidth()), false);
		
		SpriteBatch fboBatch = new SpriteBatch();
		fboBatch.setColor(Color.WHITE);
		OrthographicCamera camera = new OrthographicCamera();
		camera.setToOrtho(false, fbo.getWidth(), fbo.getHeight());
		fboBatch.setProjectionMatrix(camera.combined);		

		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		fbo.begin();
		fboBatch.begin();
		fboBatch.draw(r, 0, 0, fbo.getWidth(), fbo.getHeight());
		fboBatch.end();

		TextureRegion tex = ScreenUtils.getFrameBufferTexture(0,0,fbo.getWidth(), fbo.getHeight());		
//		tex.flip(false, true);		
		
		fbo.end();
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		
		fbo.dispose();
		a.dispose();
		fboBatch.dispose();
		
		return tex;
	}

	// -------------------------------------------------------------------------
	// ListCellRenderer
	// -------------------------------------------------------------------------
	private final CellRenderer<Element> listCellRenderer = new CellRenderer<Element>() {

		@Override
		protected String getCellTitle(Element e) {
			String name = e.getAttribute(XMLConstants.ID_ATTR);

			Element chapter = (Element) e.getParentNode();

			String init = chapter.getAttribute(XMLConstants.INIT_SCENE_ATTR);

			if (init.equals(name))
				name += " <init>";

			return name;
		}

		@Override
		protected String getCellSubTitle(Element e) {
			return e.getAttribute(XMLConstants.BACKGROUND_ATLAS_ATTR);
		}

		@Override
		public TextureRegion getCellImage(Element e) {
			String atlas = e.getAttribute(XMLConstants.BACKGROUND_ATLAS_ATTR);
			String region = e.getAttribute(XMLConstants.BACKGROUND_REGION_ATTR);
			
			TextureRegion r = null;
			
			if(!atlas.isEmpty() && !region.isEmpty()) 
				r = getBgIcon(atlas,region);

			if (r == null)
				r =  Ctx.assetManager.getIcon("ic_no_scene");

			return r;
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
