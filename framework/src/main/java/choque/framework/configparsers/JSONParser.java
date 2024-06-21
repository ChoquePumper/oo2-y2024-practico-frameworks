package choque.framework.configparsers;

import choque.framework.ConfigParser;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class JSONParser implements ConfigParser {
	@Override
	public void procesarConfiguracion(File archivo, Map<String, Object> props) {
		// Lee el archivo json
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(new JSONTokener(new FileReader(archivo)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		props.putAll(jsonObject.toMap());
	}
}
