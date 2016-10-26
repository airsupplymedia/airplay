package de.airsupply.airplay.web.util;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
public class SafeObjectMapper extends ObjectMapper {

	public class SafeCharacterEscapes extends CharacterEscapes {

		private final int[] escapedCharacters;

		public SafeCharacterEscapes() {
			int[] escapedCharacters = CharacterEscapes.standardAsciiEscapesForJSON();
			escapedCharacters['<'] = CharacterEscapes.ESCAPE_STANDARD;
			escapedCharacters['>'] = CharacterEscapes.ESCAPE_STANDARD;
			escapedCharacters['&'] = CharacterEscapes.ESCAPE_STANDARD;
			escapedCharacters['\''] = CharacterEscapes.ESCAPE_STANDARD;
			this.escapedCharacters = escapedCharacters;
		}

		@Override
		public int[] getEscapeCodesForAscii() {
			return escapedCharacters;
		}

		@Override
		public SerializableString getEscapeSequence(int character) {
			return null;
		}

	}

	public SafeObjectMapper() {
		this.getFactory().setCharacterEscapes(new SafeCharacterEscapes());
	}

}
