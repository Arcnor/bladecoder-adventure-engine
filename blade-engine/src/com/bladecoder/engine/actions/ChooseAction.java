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
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.bladecoder.engine.actions.Param.Type;
import com.bladecoder.engine.loader.XMLConstants;
import com.bladecoder.engine.model.VerbRunner;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Choose")
@ModelDescription("Execute only one action inside the Choose/EndChoose block.")
public class ChooseAction extends AbstractControlAction {
	public enum ChooseCriteria {
		ITERATE, RANDOM, CYCLE
	}

	/**
	 * When the verb is a comma separated verb list, we use chooseCriteria as criteria to choose the verb to execute.
	 */
	@JsonProperty(required = true, defaultValue = "CYCLE")
	@JsonPropertyDescription("The action to execute will be selected following this criteria.")
	@ModelPropertyType(Type.OPTION)
	private ChooseCriteria chooseCriteria = ChooseCriteria.CYCLE;

	/** Used when choose_criteria is 'iterate' or 'cycle' */
	int chooseCount = -1;

	@Override
	public void setParams(HashMap<String, String> params) {
		chooseCriteria = ChooseCriteria.valueOf(params.get("chooseCriteria").toUpperCase());
		caID = params.get(XMLConstants.CONTROL_ACTION_ID_ATTR);
	}

	@Override
	public boolean run(ActionCallback cb) {
		VerbRunner v = (VerbRunner) cb;

		int startIp = v.getIP();
		int ip0 = startIp + 1;
		final List<AbstractAction> actions = v.getActions();

		int ip = skipControlIdBlock(actions, startIp);
		int numActions = ip - startIp;

		if(numActions <= 0)
			return false;

		switch (chooseCriteria) {
			case ITERATE:
				chooseCount++;
				break;
			case RANDOM:
				chooseCount = MathUtils.random(0, numActions - 1);
				break;
			case CYCLE:
				chooseCount = (chooseCount + 1) % numActions;
				break;
		}

		v.setIP(ip);

		if(chooseCount < numActions) {
			return actions.get(ip0 + chooseCount).run(v);
		}

		return false;
	}
}
