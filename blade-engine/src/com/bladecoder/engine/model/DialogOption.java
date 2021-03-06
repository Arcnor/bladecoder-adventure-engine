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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.badlogic.gdx.graphics.Color;

public class DialogOption {
	private String text;
	@JsonProperty
	private String font;
	@JsonProperty
	private String responseText;
	@JsonProperty("verb")
	private String verbId;
	@JsonProperty
	private String next;
	@JsonProperty
	private boolean visible = true;
	@JsonProperty
	private boolean once = false;
	private Color color;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getVerbId() {
		return verbId;
	}

	public void setVerbId(String verbId) {
		this.verbId = verbId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	public String getResponseText() {
		return responseText;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public boolean isOnce() {
		return once;
	}

	public void setOnce(boolean once) {
		this.once = once;
	}
}
