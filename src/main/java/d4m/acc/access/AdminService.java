package d4m.acc.access;

import java.util.SortedSet;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminService extends BaseService {

	private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    
	AdminService(AccumuloClient client) {
        super(client);
	}
    	
    public SortedSet<String> listTables() {
		log.info("listTables==>");
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
}
