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

import com.bladecoder.engine.actions.Param.Type;
import com.bladecoder.engine.model.InteractiveActor;
import com.bladecoder.engine.model.Scene;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.util.EngineLogger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("RemoveActor")
@ModelDescription("Deletes an actor from the game")
public class RemoveActorAction extends AbstractAction {
	@JsonProperty("actor")
	@JsonPropertyDescription("The actor to remove")
	@ModelPropertyType(Type.SCENE_ACTOR)
	private SceneActorRef sceneActorRef;
	
	@Override
	public void setParams(HashMap<String, String> params) {
		String[] a = Param.parseString2(params.get("actor"));

		sceneActorRef = new SceneActorRef(a[0], a[1]);
	}

	@Override
	public boolean run(ActionCallback cb) {		
		final Scene s = sceneActorRef.getScene();

		final String actorId = sceneActorRef.getActorId();
		InteractiveActor a = (InteractiveActor)s.getActor(actorId, false);
		
		if(a == null) { // search in inventory
			a = World.getInstance().getInventory().removeItem(actorId);
			
			if(a != null) {
				a.dispose();
			} else {
				EngineLogger.error("RemoveActor - Actor not found: " + actorId);
			}
		} else {
			s.removeActor(a);
			if(s ==  World.getInstance().getCurrentScene())
				a.dispose();
		}		
		
		return false;
	}


}
