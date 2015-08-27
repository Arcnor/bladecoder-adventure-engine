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
package com.bladecoder.engine.model;

import java.util.ArrayList;
import java.util.List;

import com.bladecoder.engine.actions.ModelDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

@ModelDescription("Actors can have several dialogs defined. Dialogs have a tree of options to choose")
public class Dialog extends AbstractModel {
	public final static String DEFAULT_DIALOG_VERB = "dialog";

	@JsonProperty
	private List<DialogOption> options = new ArrayList<>();
	
	private int currentOption = -1;
	
	private String actor;
	
	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}
	
	public Dialog selectOption(DialogOption o) {
		
		currentOption = options.indexOf(o);
		
		String v = o.getVerbId();
		
		if(v == null) v = DEFAULT_DIALOG_VERB;
		
		// TODO: DELETE REFERENCE TO WORLD FROM DIALOG
		CharacterActor a = (CharacterActor)World.getInstance().getCurrentScene().getActor(actor, false);
		a.runVerb(v);
		
		if(o.isOnce())
			o.setVisible(false);
		
		currentOption = -1;
		
		if(o.getNext() != null) {
			String next = o.getNext();
			
			if(next.equals("this"))
				return this;
			else 
				return a.getDialog(next);
		}
		
		return null;
	}
	
	public void addOption(DialogOption o) {
		options.add(o);
	}

	public List<DialogOption> getOptions() {
		return options;
	}
	
	public List<DialogOption> getVisibleOptions() {
		List<DialogOption> visible = new ArrayList<>();
		
		for(DialogOption o: options) {
			if(o.isVisible()) visible.add(o);
		}
		
		return visible;
	}	
	
	public void reset() {
		currentOption = -1;
	}
	
	public int getNumVisibleOptions() {
		int num = 0;
		
		for(DialogOption o:getOptions()) {
			if(o.isVisible()) num++;
		}
		
		return num;
	}
	
	public DialogOption getCurrentOption() {
		return currentOption == -1?null: options.get(currentOption);
	}
}
