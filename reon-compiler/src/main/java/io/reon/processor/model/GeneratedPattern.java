package io.reon.processor.model;

import java.util.List;

public class GeneratedPattern {

	private final String pattern;
	private final List<GroupMapping> groupMappings;

	public GeneratedPattern(String pattern, List<GroupMapping> groupMappings) {
		this.pattern = pattern;
		this.groupMappings = groupMappings;
	}

	public String getPattern() {
		return pattern;
	}

	public List<GroupMapping> getGroupMappings() {
		return groupMappings;
	}
}
