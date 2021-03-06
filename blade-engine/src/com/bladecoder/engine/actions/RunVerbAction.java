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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.actions.Param.Type;
import com.bladecoder.engine.model.InteractiveActor;
import com.bladecoder.engine.model.Scene;
import com.bladecoder.engine.model.Verb;
import com.bladecoder.engine.model.VerbManager;
import com.bladecoder.engine.model.VerbRunner;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.util.EngineLogger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("RunVerb")
@ModelDescription("Runs an actor verb")
public class RunVerbAction extends BaseCallbackAction implements VerbRunner {
	@JsonProperty("actor")
	@JsonPropertyDescription("The target actor")
	@ModelPropertyType(Type.ACTOR)
	private String actorId;

	@JsonProperty(required = true)
	@JsonPropertyDescription("The 'verbId' to run")
	@ModelPropertyType(Type.STRING)
	private String verb;

	@JsonProperty
	@JsonPropertyDescription("Aditional actor for 'use' verb")
	@ModelPropertyType(Type.STRING)
	private String target;

	private String state;
	private int ip = -1;

	@Override
	public void setParams(HashMap<String, String> params) {
		actorId = params.get("actor");
		verb = params.get("verb");
		target = params.get("target");

		if (params.get("wait") != null) {
			setWait(Boolean.parseBoolean(params.get("wait")));
		}
	}

	@Override
	public boolean run(ActionCallback cb) {
		setVerbCb(cb);
		
		run();
		
		return getWait();
	}

	private Verb getVerb(String verb, String target, String state) {
		Verb v = null;

		final World world = World.getInstance();
		if (actorId != null) {
			final InteractiveActor a = (InteractiveActor) world.getCurrentScene().getActor(actorId, true);

			v = a.getVerbManager().getVerb(verb, state, target);
		}

		if (v == null) {
			v = world.getCurrentScene().getVerb(verb);
		}

		if (v == null) {
			v = world.getVerbManager().getVerbs().get(verb);
		}

		if (v == null)
			EngineLogger.error("Cannot find VERB: " + verb + " for ACTOR: " + actorId);

		return v;
	}

	private void nextStep() {

		boolean stop = false;

		List<AbstractAction> actions = getActions();

		while (ip < actions.size() && !stop) {
			AbstractAction a = actions.get(ip);

			if (EngineLogger.debugMode())
				EngineLogger.debug("RunVerbAction: " + verb + "(" + ip + ") " + a.getClass().getSimpleName());

			try {
				if (a.run(this))
					stop = true;
				else
					ip++;
			} catch (Exception e) {
				EngineLogger.error("EXCEPTION EXECUTING ACTION: " + a.getClass().getSimpleName(), e);
			}
		}

		if (getWait() && !stop) {
			super.resume();
		}
	}

	@Override
	public void resume() {
		ip++;
		nextStep();
	}

	@Override
	public void cancel() {
		List<AbstractAction> actions = getActions();

		for (AbstractAction c : actions) {
			if (c instanceof VerbRunner)
				((VerbRunner) c).cancel();
		}

		ip = actions.size();
	}
	

	@Override
	public List<AbstractAction> getActions() {
		Verb v = getVerb(verb, target, state);

		if (v == null) {
			EngineLogger.error(MessageFormat.format("Verb ''{0}'' not found for actor ''{1}({3})'' and target ''{2}''",
					verb, actorId, target, ((InteractiveActor)World.getInstance().getCurrentScene().getActor(actorId, true)).getState()));

			return new ArrayList<>(0);
		}

		return v.getActions();
	}

	@Override
	public void run() {
		ip = 0;
		
		Scene s = World.getInstance().getCurrentScene();

		// Gets the actor/scene state.
		if (actorId != null
				&& ((InteractiveActor)s.getActor(actorId, true)).getVerb(verb, target) != null) {
			state = ((InteractiveActor)s.getActor(actorId, true)).getState();
		} else if (s.getVerb(verb) != null) {
			state = s.getState();
		}

		nextStep();
	}

	@Override
	public int getIP() {
		return ip;
	}

	@Override
	public void setIP(int ip) {
		this.ip = ip;
	}	

	@Override
	public void write(Json json) {
		json.writeValue("actorId", actorId);
		json.writeValue("ip", ip);
		json.writeValue("verb", verb);
		json.writeValue("target", target);
		json.writeValue("state", state);
		super.write(json);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		actorId = json.readValue("actorId", String.class, jsonData);
		ip = json.readValue("ip", Integer.class, jsonData);
		verb = json.readValue("verb", String.class, jsonData);
		target = json.readValue("target", String.class, jsonData);
		state = json.readValue("state", String.class, jsonData);
		super.read(json, jsonData);
	}

}
