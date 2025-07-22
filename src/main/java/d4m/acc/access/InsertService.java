package d4m.acc.access;

import java.nio.charset.StandardCharsets;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InsertService extends BaseService {

	private static final Logger log = LoggerFactory.getLogger(InsertService.class);
    
	InsertService() {
        super();
	}

 // 	public void insert(RCVs rcvs, String tableName) {

// 		if (!client.tableOperations().exists(tableName)) {
// 			createTable(tableName);
// 		}
// 		try {
// 			AccumuloInsert accIns = new AccumuloInsert(client.properties().getProperty("instance.name"), client.properties().getProperty("instance.zookeepers"), tableName, client.properties().getProperty("auth.principal"), client.properties().getProperty("auth.token"));
// //			accIns.doProcessing(rcvs.getRows(), rcvs.getCols(), rcvs.getVals(),rcvs.getFamily(), "PUBLIC");
// 		} catch (Exception e) {
// 			log.error("", e);
// 		}
// 	}

	public void insertPair(RCVs rcvs, String tableName) {

		log.trace("insertPair=={}", tableName);

		try {
			// if (!client.tableOperations().exists(tableName)) {
			// 	createTablePair(tableName);
			// }
			
			insertIntoTable(rcvs.getRows(), rcvs.getCols(), rcvs.getVals(), tableName, rcvs.getFamily());
			insertIntoTable(rcvs.getCols(), rcvs.getRows(), rcvs.getVals(), tableName + pairDecor, rcvs.getFamily());
			
		} catch (Exception e) {
			log.error("Failed to insert into table pair", e);
		}
	}

	private void insertIntoTable(String[] rows, String[] cols, String[] vals, String table, String family)
			throws TableNotFoundException, MutationsRejectedException {

		log.trace("insertIntoTable=={}", table);
				
		try (BatchWriter writer = client.createBatchWriter(table)) {
			for (int i = 0; i < rows.length; i++) {
				Mutation mutation = new Mutation(rows[i]);
				mutation.put(new Text(family), new Text(cols[i]), new Value(vals[i].getBytes(StandardCharsets.UTF_8)));
				writer.addMutation(mutation);
			}
		}
	}
}
