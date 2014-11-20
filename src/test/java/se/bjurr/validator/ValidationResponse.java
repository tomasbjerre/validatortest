package se.bjurr.validator;

import java.util.List;

public class ValidationResponse {

	private List<ValidationResponseMessage> messages;

	private String url;

	public List<ValidationResponseMessage> getMessages() {
		return messages;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return url;
	}
}
