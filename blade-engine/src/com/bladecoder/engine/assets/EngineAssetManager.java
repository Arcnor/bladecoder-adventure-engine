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
package com.bladecoder.engine.assets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;
import com.bladecoder.engine.util.Config;
import com.bladecoder.engine.util.EngineLogger;

public class EngineAssetManager extends AssetManager {
	public static final String DESKTOP_PREFS_DIR = "BladeEngine";
	public static final String NOT_DESKTOP_PREFS_DIR = "data/";

	public static final String ATLASES_DIR = "atlases/";
	public static final String MODEL_DIR = "model/";
	public static final String MUSIC_DIR = "music/";
	public static final String IMAGE_DIR = "images/";
	public static final String SOUND_DIR = "sounds/";
	private static final String MODEL3D_DIR = "3d/";
	private static final String SPINE_DIR = "spine/";

	private static final String MODEL3D_EXT = ".g3db";
	private static final String SPINE_EXT = ".skel";

	private static EngineAssetManager instance = null;

	private float scale = 1;

	private EngineResolutionFileResolver resResolver;

	protected EngineAssetManager() {
		this(new InternalFileHandleResolver());
//		getLogger().setLevel(Application.LOG_DEBUG);
	}

	protected EngineAssetManager(FileHandleResolver resolver) {
		super(resolver);

		resResolver = new EngineResolutionFileResolver(resolver);
		setLoader(Texture.class, new TextureLoader(resResolver));
		setLoader(TextureAtlas.class, new TextureAtlasLoader(resResolver));
		setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		Texture.setAssetManager(this);
	}

	public float getScale() {
		return scale;
	}

	public void setScale(int worldWidth, int worldHeight) {
		Resolution r[] = getResolutions(resResolver.getBaseResolver(), worldWidth, worldHeight);

		if (r == null || r.length == 0) {
			EngineLogger.error("No resolutions defined. Maybe your 'assets' folder doesn't exists or it's empty");
			return;
		}

		resResolver.setResolutions(r);
		resResolver.selectResolution();
		scale = resResolver.getResolution().portraitWidth / (float) worldWidth;

		EngineLogger.debug("Setting ASSETS SCALE: " + scale);
	}

	public static EngineAssetManager getInstance() {
		if (instance == null) {
			instance = new EngineAssetManager();
		}

		return instance;
	}

	/**
	 * Creates a EngineAssetManager instance for edition. That is:
	 * 
	 * - Puts a PathResolver to locate the assets through an absolute path -
	 * Puts assets scale to "1"
	 * 
	 * @param base is the project base folder
	 * @param worldWidth the width
	 */
	public static void createEditInstance(String base, int worldWidth, int worldHeight) {
		if (instance != null)
			instance.dispose();

		instance = new EngineAssetManager(new BasePathResolver(base));
		instance.forceResolution("1");
		instance.setScale(worldWidth, worldHeight);
	}

	public void forceResolution(String suffix) {
		resResolver.setFixedResolution(suffix);

		EngineLogger.debug("FORCING ASSETS RESOLUTION SCALE: " + suffix);
	}

	public Resolution getResolution() {
		return resResolver.getResolution();
	}

	public boolean isLoading() {
		return !update();
	}

	// public BitmapFont loadFont(String style) {
	// String key =Config.getProperty(style, null);
	//
	// if(key == null) {
	// EngineLogger.error("FONT STYLE NOT DEFINED IN PROJECT PROPERTIES: " +
	// style);
	//
	// return new BitmapFont();
	// }
	//
	// int size = Config.getProperty(style + "_SIZE", 14);
	//
	// return loadFont(key, size);
	// }

	// TODO: Add support for .fnt loading
	// public BitmapFont loadFont(String filename, int size) {
	// FreeTypeFontLoaderParameter param = new FreeTypeFontLoaderParameter();
	// param.fontFileName = FONTS_DIR + filename;
	// param.fontParameters.size = size;
	// param.fontParameters.flip = false;
	// param.fontParameters.genMipMaps = false;
	//
	// // For small screens we use small fonts to limit the space used for the
	// // text in the screen
	// if (Gdx.graphics.getWidth() < 800)
	// param.fontParameters.size *= 0.7;
	//
	// String name = FONTS_DIR + filename + "_" + size + ".ttf";
	// load(name, BitmapFont.class, param);
	// finishLoading();
	// return get(name, BitmapFont.class);
	// }
	//
	// public void disposeFont(BitmapFont font) {
	// if (isLoaded(getAssetFileName(font)))
	// unload(getAssetFileName(font));
	// }

	public void loadAtlas(String name) {
		load(ATLASES_DIR + name + ".atlas", TextureAtlas.class);
	}

	public boolean isAtlasLoaded(String name) {
		return isLoaded(ATLASES_DIR + name + ".atlas");
	}

	public void disposeAtlas(String name) {
		if (isAtlasLoaded(name))
			unload(ATLASES_DIR + name + ".atlas");
	}

	public FileHandle getModelFile(String filename) {
		return resResolver.baseResolve(MODEL_DIR + filename);
	}

	/**
	 * Returns a file in the asset directory SEARCHING in the resolution
	 * directories
	 */
	public FileHandle getResAsset(String filename) {
		return resResolver.resolve(filename);
	}

	/**
	 * Returns a file in the asset directory without searching in the resolution
	 * directories
	 */
	public FileHandle getAsset(String filename) {
		return resResolver.baseResolve(filename);
	}

	public AtlasRegion getRegion(String atlas, String name) {
		TextureAtlas a = get(ATLASES_DIR + atlas + ".atlas", TextureAtlas.class);

		AtlasRegion region = a.findRegion(name);

		if (region == null) {
			EngineLogger.error("Region " + name + " not found in atlas " + atlas);
		}

		return region;
	}

	public TextureAtlas getTextureAtlas(String atlas) {
		TextureAtlas a = get(ATLASES_DIR + atlas + ".atlas", TextureAtlas.class);

		return a;
	}

	public Array<AtlasRegion> getRegions(String atlas, String name) {
		TextureAtlas a = get(ATLASES_DIR + atlas + ".atlas", TextureAtlas.class);

		Array<AtlasRegion> region = a.findRegions(name);

		if (region == null) {
			EngineLogger.error("Regions for " + name + " not found in atlas " + atlas);
		}

		return region;
	}

	public void loadTexture(String filename) {
		load(filename, Texture.class);
	}

	public void disposeTexture(Texture t) {
		if (isLoaded(getAssetFileName(t)))
			unload(getAssetFileName(t));
	}

	public Texture getTexture(String filename) {
		// TextureParameter param = new TextureParameter();
		// param.minFilter = TextureFilter.Linear;
		// param.genMipMaps = true;

		return get(filename, Texture.class);
	}

	public void dispose() {
		super.dispose();
		instance = null;
	}

	public void loadMusic(String filename) {
		if(Gdx.app.getType() == ApplicationType.iOS && filename.toLowerCase().endsWith(".ogg")) {
			EngineLogger.error("OGG files not supported in IOS");
			return;
		}
		
		load(MUSIC_DIR + filename, Music.class);
	}

	public void disposeMusic(String filename) {
		if(Gdx.app.getType() == ApplicationType.iOS && filename.toLowerCase().endsWith(".ogg")) {
			EngineLogger.error("OGG files not supported in IOS");
			return;
		}		
		
		if (isLoaded(MUSIC_DIR + filename))
			unload(MUSIC_DIR + filename);
	}

	public Music getMusic(String filename) {
		if(Gdx.app.getType() == ApplicationType.iOS && filename.toLowerCase().endsWith(".ogg")) {
			EngineLogger.error("OGG files not supported in IOS");
			return null;
		}		
		
		return get(MUSIC_DIR + filename, Music.class);
	}

	public void loadSound(String filename) {
		if(Gdx.app.getType() == ApplicationType.iOS && filename.toLowerCase().endsWith(".ogg")) {
			EngineLogger.error("OGG files not supported in IOS");
			return;
		}
		
		load(SOUND_DIR + filename, Sound.class);
	}

	public Sound getSound(String filename) {
		if(Gdx.app.getType() == ApplicationType.iOS && filename.toLowerCase().endsWith(".ogg")) {
			EngineLogger.error("OGG files not supported in IOS");
			return null;
		}		
		
		return get(SOUND_DIR + filename, Sound.class);
	}

	public void disposeSound(String filename) {
		if(Gdx.app.getType() == ApplicationType.iOS && filename.toLowerCase().endsWith(".ogg")) {
			EngineLogger.error("OGG files not supported in IOS");
			return;
		}		
		
		if (isLoaded(SOUND_DIR + filename))
			unload(SOUND_DIR + filename);
	}

	public FileHandle getSpine(String name) {
		return resResolver.baseResolve(SPINE_DIR + name + SPINE_EXT);
	}

	public void loadModel3D(String name) {
		load(MODEL3D_DIR + name + MODEL3D_EXT, Model.class);
	}

	public Model getModel3D(String name) {
		return get(MODEL3D_DIR + name + MODEL3D_EXT, Model.class);
	}

	public void disposeModel3D(String name) {
		if (isLoaded(MODEL3D_DIR + name + MODEL3D_EXT))
			unload(MODEL3D_DIR + name + MODEL3D_EXT);
	}

	public boolean assetExists(String filename) {
		return resResolver.exists(filename);
	}

	private Resolution[] getResolutions(FileHandleResolver resolver, int worldWidth, int worldHeight) {
		ArrayList<Resolution> rl = new ArrayList<Resolution>();

		String list[] = listAssetFiles("/atlases");

		for (String name : list) {
			try {
				float scale = Float.parseFloat(name);

				Resolution r = new Resolution((int) (worldWidth * scale), (int) (worldHeight * scale), name);

				rl.add(r);
			} catch (Exception e) {

			}
		}

		Collections.sort(rl, new Comparator<Resolution>() {
			public int compare(Resolution a, Resolution b) {
				return a.portraitWidth - b.portraitWidth;
			}
		});

		return rl.toArray(new Resolution[rl.size()]);
	}

	public String[] listAssetFiles(String base) {
		FileHandleResolver resolver = resResolver.getBaseResolver();

		String list[];

		URL u = EngineAssetManager.class.getResource(base);

		if (u != null && u.getProtocol().equals("jar")) {
			list = getFilesFromJar(base);

		} else {
			String n = base.substring(1);
			if (u != null)
				n = u.getFile();

			FileHandle f = null;

			try {
				f = resolver.resolve(URLDecoder.decode(n, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				EngineLogger.error("Error decoding URL", e);
				return new String[0];
			}

			FileHandle[] l = f.list();
			list = new String[l.length];

			for (int i = 0; i < l.length; i++)
				list[i] = l[i].name();

		}

		return list;
	}

	/**
	 * Returns the resolutions from a jar file.
	 */
	private String[] getFilesFromJar(String base) {
		URL dirURL = EngineAssetManager.class.getResource(base);

		Set<String> result = new HashSet<String>(); // avoid duplicates in case
													// it is a subdirectory

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); 
			JarFile jar;

			try {
				jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			} catch (Exception e) {
				EngineLogger.error("Locating jar file", e);
				return result.toArray(new String[result.size()]);
			}

			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
															// in jar

			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();

				int start = name.indexOf('/');
				int end = name.lastIndexOf('/');

				if (start == end)
					continue;

				String entry = name.substring(start + 1, end);

				result.add(entry);
			}

			try {
				jar.close();
			} catch (IOException e) {
				EngineLogger.error("Closing jar file", e);
				return result.toArray(new String[result.size()]);
			}

		}

		return result.toArray(new String[result.size()]);
	}

	public FileHandle getUserFile(String filename) {
		FileHandle file = null;

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.Applet) {
			String dir = Config.getProperty(Config.TITLE_PROP, DESKTOP_PREFS_DIR);
			dir = dir.replaceAll(" ", "");

			StringBuilder sb = new StringBuilder();
			sb.append(".").append(dir).append("/").append(filename);
			file = Gdx.files.external(sb.toString());
		} else {
			file = Gdx.files.local(NOT_DESKTOP_PREFS_DIR + filename);
		}

		return file;
	}
	
	public FileHandle getUserFolder() {
		FileHandle file = null;

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.Applet) {
			String dir = Config.getProperty(Config.TITLE_PROP, DESKTOP_PREFS_DIR);
			dir = dir.replaceAll(" ", "");

			StringBuilder sb = new StringBuilder(".");
			file = Gdx.files.external(sb.append(dir).toString());
		} else {
			file = Gdx.files.local(NOT_DESKTOP_PREFS_DIR);
		}

		return file;
	}
}
