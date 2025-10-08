package d4m.acc.access;

import java.util.Objects;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.hadoop.io.Text;

public abstract class BaseService {

	protected final AccumuloClient client;

	final static String pairDecor = "T";
	final static String degreeDecor = "Deg";
	protected String USER = "root";
	public static final Text FAMILY = new Text(""); // We are not using family and maybe never will, but just in case,

  protected BaseService(AccumuloClient client) {
	this.client = Objects.requireNonNull(client, "AccumuloClient must not be null");
  }
}
