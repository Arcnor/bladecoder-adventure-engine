package com.bladecoder.engine.actions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class AbstractControlAction implements Action {
	@JsonProperty
	protected String caID;

	public String getControlActionID() {
		return caID;
	}

	protected int skipControlIdBlock(List<Action> actions, int ip) {
		final String caID = getControlActionID();

		do {
			ip++;
		} while (!(actions.get(ip) instanceof AbstractControlAction)
				|| !((AbstractControlAction) actions.get(ip)).getControlActionID().equals(caID));

		return ip;
	}
}
