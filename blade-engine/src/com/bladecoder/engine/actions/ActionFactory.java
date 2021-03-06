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
package com.bladecoder.engine.actions;

import java.util.HashMap;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.bladecoder.engine.util.EngineLogger;

public class ActionFactory {

	private static final HashMap<String, String> actions = new HashMap<String, String>();

	static {
		
		actions.put("Lookat",
				"com.bladecoder.engine.actions.LookAtAction");
		actions.put("Pickup",
				"com.bladecoder.engine.actions.PickUpAction");
		actions.put("Goto",
				"com.bladecoder.engine.actions.GotoAction");
		actions.put("Leave",
				"com.bladecoder.engine.actions.LeaveAction");	
		actions.put("State",
				"com.bladecoder.engine.actions.SetStateAction");
		actions.put("Cutmode",
				"com.bladecoder.engine.actions.SetCutmodeAction");
		actions.put("ShowInventory",
				"com.bladecoder.engine.actions.ShowInventoryAction");			
		actions.put("Animation",
				"com.bladecoder.engine.actions.AnimationAction");
		actions.put("PositionAnim",
				"com.bladecoder.engine.actions.PositionAnimAction");
		actions.put("ScaleAnim",
				"com.bladecoder.engine.actions.ScaleAnimAction");
		actions.put("Position",
				"com.bladecoder.engine.actions.PositionAction");
		actions.put("Scale",
				"com.bladecoder.engine.actions.ScaleAction");		
		actions.put("RemoveInventoryItem",
				"com.bladecoder.engine.actions.RemoveInventoryItemAction");	
		actions.put("Say",
				"com.bladecoder.engine.actions.SayAction");	
		actions.put("DropItem",
				"com.bladecoder.engine.actions.DropItemAction");
		actions.put("Wait",
				"com.bladecoder.engine.actions.WaitAction");
		actions.put("Talkto",
				"com.bladecoder.engine.actions.TalktoAction");
		actions.put("DialogOptionAttr",
				"com.bladecoder.engine.actions.SetDialogOptionAttrAction");
		actions.put("SayDialog",
				"com.bladecoder.engine.actions.SayDialogAction");
		actions.put("RunVerb",
				"com.bladecoder.engine.actions.RunVerbAction");
		actions.put("CancelVerb",
				"com.bladecoder.engine.actions.CancelVerbAction");
		actions.put("Sound",
				"com.bladecoder.engine.actions.SoundAction");
		actions.put("Music",
				"com.bladecoder.engine.actions.MusicAction");
		actions.put("Camera",
				"com.bladecoder.engine.actions.CameraAction");
		actions.put("Transition",
				"com.bladecoder.engine.actions.TransitionAction");
		actions.put("LoadChapter",
				"com.bladecoder.engine.actions.LoadChapterAction");
		actions.put("SceneState",
				"com.bladecoder.engine.actions.SetSceneStateAction");
		actions.put("RemoveActor",
				"com.bladecoder.engine.actions.RemoveActorAction");		
		actions.put("ActorAttr",
				"com.bladecoder.engine.actions.SetActorAttrAction");
		actions.put("Repeat",
				"com.bladecoder.engine.actions.RepeatAction");
		actions.put("IfAttr",
				"com.bladecoder.engine.actions.IfAttrAction");
		actions.put("IfSceneAttr",
				"com.bladecoder.engine.actions.IfSceneAttrAction");	
		actions.put("IfProperty",
				"com.bladecoder.engine.actions.IfPropertyAction");
		actions.put("Property",
				"com.bladecoder.engine.actions.PropertyAction");
		actions.put("Choose",
				"com.bladecoder.engine.actions.ChooseAction");
		actions.put("RunOnce",
				"com.bladecoder.engine.actions.RunOnceAction");
		actions.put("MoveToScene",
				"com.bladecoder.engine.actions.MoveToSceneAction");
	}
	
	public static String []getActionList() {
		return  actions.keySet().toArray(new String[actions.size()]);
	}

	public static AbstractAction create(String name,
	                                    HashMap<String, String> params) {
		String className = actions.get(name);

		if (className == null) {
			EngineLogger.error( "Action with name '" + name
					+ "' not found.");

			return null;
		}

		return ActionFactory.createByClass(className, params);
	}	
	
	
	
	public static AbstractAction createByClass(String className,
	                                           HashMap<String, String> params) {

		AbstractAction a = null;

		try {
			Class<?> c = ClassReflection.forName(className);
			a = (AbstractAction) ClassReflection.newInstance(c);
			
			if(params != null)
				a.setParams(params);
		} catch (ReflectionException e) {
			EngineLogger.error(e.getMessage());
		}

		return a;
	}
}
