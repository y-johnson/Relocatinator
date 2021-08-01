package yjohnson;

public enum FileOperation {

	MOVE_FILE_ATOMICALLY, COPY_FILE_AND_DELETE_SRC;

	public static String[] toStringArray() {
		String[] strings = new String[values().length];
		FileOperation[] values = values();
		for (int i = 0; i < values.length; i++) {
			FileOperation fop = values[i];
			strings[i] = fop.toString().replace('_', ' ');
		}
		return strings;
	}
}
