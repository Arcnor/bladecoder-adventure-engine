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
import com.bladecoder.engine.model.World;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Property")
@ModelDescription("Sets a global game property")
public class PropertyAction extends AbstractAction {
	@JsonProperty(required = true)
	@JsonPropertyDescription("Property name")
	@ModelPropertyType(Type.STRING)
	private String prop;

	@JsonProperty(required = true)
	@JsonPropertyDescription("Property value")
	@ModelPropertyType(Type.STRING)
	private String value;

	@Override
	public void setParams(HashMap<String, String> params) {
		prop = params.get("prop");
		value = params.get("value");
	}

	@Override
	public boolean run(ActionCallback cb) {

		World.getInstance().setCustomProperty(prop, value);

		return false;
	}

}
