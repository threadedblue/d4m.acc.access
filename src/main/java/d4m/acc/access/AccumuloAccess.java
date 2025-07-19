package d4m.acc.access;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Injector;

import d4m.acc.query.D4MQueryStandaloneSetup;
import d4m.acc.query.d4MQuery.AxisExpr;
import d4m.acc.query.d4MQuery.D4MQuery;

@Service
public class AccumuloAccess {

	private static final Logger log = LoggerFactory.getLogger(AccumuloAccess.class);

	protected static AccumuloClient client;

	final static String pairDecor = "T";
	final static String degreeDecor = "Deg";
	private String USER = "root";
	public static final Text FAMILY = new Text(""); // We are not using family and maybe never will, but just in case,

	AccumuloAccess() {
		AccumuloAccess.client = Accumulo.newClient()
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
			if (!client.tableOperations().exists(tableName)) {
				createTablePair(tableName);
			}
			
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

	public ObjectNode query(D4MRequest qry) {

			log.trace("query=={}", qry.getPayload().toString());

			D4MQuery model = (D4MQuery) parseQuery(qry.getPayload().asText());

			log.debug("model=={}", model);
			
			return executeParsedQuery(model, qry.getTableName());
		}

	public static ObjectNode scanTable(AxisExpr query, String tableName) {
		log.trace("scanTable=={}", query.toString());
		List<Range> ranges = new ScanCriteriaBuilder().doSwitch(query);
		for (Range range : ranges) {
			log.trace("range==Start=={} End=={}", range.getStartKey().toString(), range.getEndKey().toString());
		}
		log.trace("ranges size=={}", ranges.size());
		ObjectNode result = JsonNodeFactory.instance.objectNode();
		ArrayNode rows = result.putArray("rows");

		try {
			String user = client.whoami();
			Authorizations auths = client.securityOperations().getUserAuthorizations(user);
			BatchScanner scanner = client.createBatchScanner(tableName, auths);
			scanner.setRanges(ranges);
			for (Map.Entry<Key, Value> entry : scanner) {
				ObjectNode row = JsonNodeFactory.instance.objectNode();
				row.put("row", entry.getKey().getRow().toString());
				row.put("col", entry.getKey().getColumnFamily().toString());
				row.put("val", entry.getValue().toString());
				rows.add(row);
			}
			scanner.close();
		} catch (Exception e) {
			throw new RuntimeException("Accumulo scan failed", e);
		}

		return result;
	}

	public D4MQuery parseQuery(String queryString) {
		// Set up Xtext
		Injector injector = new D4MQueryStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);

		IResourceServiceProvider provider = injector.getInstance(IResourceServiceProvider.class);
		Resource.Factory factory = provider.get(Resource.Factory.class);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
			.put("d4mq", factory);

		Resource resource = resourceSet.createResource(URI.createURI("dummy:/query.qry"));
		ByteArrayInputStream input = new ByteArrayInputStream(queryString.getBytes(StandardCharsets.UTF_8));

		try {
			resource.load(input, resourceSet.getLoadOptions());
			            // Log parse errors if any
			if (!resource.getErrors().isEmpty()) {
				log.error("=== Parse Errors ===");
				for (Resource.Diagnostic diag : resource.getErrors()) {
					log.error("Line {}, Col {}: {}", diag.getLine(), diag.getColumn(), diag.getMessage());
				}
				return null;
			}

			// Log root EObject
			EObject model = resource.getContents().get(0);
			log.debug("Parsed model: {}", model.getClass().getSimpleName());

			TreeIterator<EObject> it = model.eAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				log.debug("EObject: {} → {}", obj.eClass().getName(), obj.toString());
			}

			// Dump grammar nodes if available
			if (resource instanceof XtextResource) {
				IParseResult parseResult = ((XtextResource) resource).getParseResult();
				INode rootNode = parseResult.getRootNode();
				log.debug("=== Grammar Trace ===");
				for (INode node : rootNode.getAsTreeIterable()) {
					String element = node.getGrammarElement() != null ? node.getGrammarElement().toString() : "null";
					log.debug("Node: {}  [Text: '{}']", element, node.getText().replace("\n", "\\n"));
				}
				}
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse query", e);
		}

		// Get the parsed root object
		EObject eObject = resource.getContents().get(0);
log.debug("Parsed model type: {}", eObject.getClass().getName());
TreeIterator<EObject> it = eObject.eAllContents();
while (it.hasNext()) {
    EObject child = it.next();
    log.debug("Child: {} — {}", child.eClass().getName(), child.toString());
}
		if (eObject instanceof D4MQuery) {
			return (D4MQuery) eObject;
		} else {
			throw new RuntimeException("Parsed root is not a D4MQuery");
		}
	}

	public ObjectNode executeParsedQuery(D4MQuery model, String tableName) {
		return new QueryExecutor(tableName).doSwitch(model);
	}
}
