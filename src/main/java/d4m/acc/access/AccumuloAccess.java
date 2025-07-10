package d4m.acc.access;

import java.util.SortedSet;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import edu.mit.ll.d4m.db.cloud.D4mDataSearch;
import edu.mit.ll.d4m.db.cloud.D4mDbResultSet;
import edu.mit.ll.d4m.db.cloud.D4mException;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloInsert;

@Service
public class AccumuloAccess {

	private static final Logger log = LoggerFactory.getLogger(AccumuloAccess.class);

	protected AccumuloClient client;

	final String pairDecor = "T";
	final String degreeDecor = "Deg";
	private String USER = "root";
	public final String FAMILY = "";

	AccumuloAccess() {
		this.client = Accumulo.newClient()
		.to("accumulo", "localhost:2181")
		.as(USER, "D").build();
	}

	public SortedSet<String> listTables() {
		log.info("list==>");
		TableOperations ops = client.tableOperations();
		return ops.list();
	}

	public String currentUser() {
		log.info("user=whoami");
		return client.whoami();
	}

	public String createTable(String tableName) {
		log.trace("tableName={}", tableName);
		TableOperations ops = client.tableOperations();
		try {
			ops.create(tableName);
		} catch (AccumuloException | AccumuloSecurityException | TableExistsException e) {
			e.printStackTrace();
		}
		return tableName;
	}

	public String createTablePair(String tableName) {
		log.trace("tableName={} {}", 1, tableName);
		log.trace("auth.principal={}", client.properties().getProperty("auth.principal"));
		log.trace("auth.token={}", client.properties().getProperty("auth.token"));
		TableOperations ops = client.tableOperations();
		if (!ops.exists(tableName)) {
			try {
				ops.create(tableName);
				ops.create(tableName.concat(pairDecor));
				ops.create(tableName.concat(degreeDecor));
			} catch (AccumuloException | AccumuloSecurityException | TableExistsException e) {
				e.printStackTrace();
			}
			log.trace("tableName={} {}", 2, tableName);
		} else {
			log.info("Table {} exists", tableName);
		}
		return tableName;
	}

	public String dropTablePair(String tableName) {
		log.trace("tableName={} {}", 1, tableName);
		log.trace("auth.principal={}", client.properties().getProperty("auth.principal"));
		log.trace("auth.token={}", client.properties().getProperty("auth.token"));
		TableOperations ops = client.tableOperations();
		if (ops.exists(tableName)) {
				try {
					ops.delete(tableName);
					ops.delete(tableName.concat(pairDecor));
					ops.delete(tableName.concat(degreeDecor));
				} catch (AccumuloException | AccumuloSecurityException | TableNotFoundException e) {
					log.error("", e);
				}

			log.trace("tableName={} {}", 2, tableName);
		} else {
			log.info("Table {} does not exist", tableName);
		}
		return tableName;
	}
	public void insert(RCVs rcvs, String tableName) {

		if (!client.tableOperations().exists(tableName)) {
			createTable(tableName);
		}
		try {
			AccumuloInsert accIns = new AccumuloInsert(client.properties().getProperty("instance.name"), client.properties().getProperty("instance.zookeepers"), tableName, client.properties().getProperty("auth.principal"), client.properties().getProperty("auth.token"));
			accIns.doProcessing(rcvs.getRows(), rcvs.getCols(), rcvs.getVals(),rcvs.getF(), "PUBLIC");
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void insertPair(RCVs rcvs, String tableName) {

		if (!client.tableOperations().exists(tableName)) {
			createTablePair(tableName);
		}
		try {
			AccumuloInsert accIns = new AccumuloInsert(client.properties().getProperty("instance.name"), client.properties().getProperty("instance.zookeepers"), tableName, client.properties().getProperty("auth.principal"), client.properties().getProperty("auth.token"));
			accIns.doProcessing(rcvs.getRows(), rcvs.getCols(), rcvs.getVals(), rcvs.getF(), "PUBLIC");
			AccumuloInsert accInsT = new AccumuloInsert(client.properties().getProperty("instance.name"), client.properties().getProperty("instance.zookeepers"), tableName + pairDecor, client.properties().getProperty("auth.principal"), client.properties().getProperty("auth.token"));
			accInsT.doProcessing(rcvs.getCols(), rcvs.getRows(), rcvs.getVals(), rcvs.getF(), "PUBLIC");
		} catch (Exception e) {
			log.error("", e);
		}
	}	

	public D4mDbResultSet query(String row, String col, String tableName) {

		log.debug("query=={}:{}", row, col);

		String un = client.properties().getProperty("auth.principal");
		String pw = client.properties().getProperty("auth.token");
		String authorizations = String.format("%s, %s", un, pw);
log.debug(un, pw);
		D4mDataSearch accQry = new D4mDataSearch(client.properties().getProperty("instance.name"), client.properties().getProperty("instance.zookeepers"), tableName, client.properties().getProperty("auth.principal"), client.properties().getProperty("auth.token"));
		D4mDbResultSet result = null;
log.debug("accQry=={}", accQry.getTableName());
		try {
log.debug(authorizations, "accQry", result);
			result = accQry.doMatlabQuery(row, col, FAMILY, authorizations);
		} catch (D4mException e) {
			log.error("", e);
		}
		return result;
	}
}
