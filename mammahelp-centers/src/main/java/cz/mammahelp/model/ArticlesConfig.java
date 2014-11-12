package cz.mammahelp.model;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

@Root(name = "articleTypes")
public class ArticlesConfig {

	@ElementMap(data = false, inline = true, entry = "type", key = "name", attribute = true, keyType = String.class, valueType = PathList.class)
	public Map<String, PathList> types = new HashMap<String, PathList>();

}
