package d4m.acc.access;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.hadoop.io.Text;

public abstract class BaseService {

	protected static AccumuloClient client;

	final static String pairDecor = "T";
	final static String degreeDecor = "Deg";
	protected String USER = "root";
	public static final Text FAMILY = new Text(""); // We are not using family and maybe never will, but just in case,

	BaseService() {
		BaseService.client = Accumulo.newClient()
		.to("accumulo", "localhost:2181")
		.as(USER, "D").build();
	}
}
