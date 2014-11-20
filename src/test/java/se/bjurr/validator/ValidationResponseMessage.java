package se.bjurr.validator;

public class ValidationResponseMessage {

	private Integer firstColumn;
	private Integer firstLine;
	private Integer lastColumn;
	private Integer lastLine;
	String message;
	private String subType;
	private String type;

	public Integer getFirstColumn() {
		return firstColumn;
	}

	public Integer getFirstLine() {
		return firstLine;
	}

	public Integer getLastColumn() {
		return lastColumn;
	}

	public Integer getLastLine() {
		return lastLine;
	}

	public String getMessage() {
		return message;
	}

	public String getSubType() {
		return subType;
	}

	public String getType() {
		return type;
	}

	private Object ifNotNull(Object nullable, Object string) {
		if (nullable != null) {
			return string;
		}
		return "";
	}

	@Override
	public String toString() {
		return type + " " + ifNotNull(firstLine, " First line: " + firstLine)
				+ ifNotNull(lastLine, " Last line: " + lastLine)
				+ ifNotNull(firstColumn, " First column: " + firstColumn)
				+ ifNotNull(lastColumn, " Last column: " + lastColumn) + " " + message;
	}
}
