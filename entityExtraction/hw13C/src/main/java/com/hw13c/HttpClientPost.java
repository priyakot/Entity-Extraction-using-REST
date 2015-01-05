package com.hw13c;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;

import info.aduna.iteration.Iterations;

import java.io.*;
import java.net.URL;

/************************************************************
	- Simple Calais client to process file or files in a folder
	- Takes 2 arguments
		1. File or folder name to process
		2. Output folder name to store response from Calais
	- Please specify the correct web service location url for CALAIS_URL variable
	- Please adjust the values of different request parameters in the createPostMethod

 **************************************************************/


public class HttpClientPost {

	private static final String CALAIS_URL = "http://api.opencalais.com/tag/rs/enrich";

	private static File input;
	private static File output;
	private HttpClient client;

	private PostMethod createPostMethod() {

		PostMethod method = new PostMethod(CALAIS_URL);

		// Set mandatory parameters
		method.setRequestHeader("x-calais-licenseID", "44vkyby2f4b6xcvphtum3xzc");

		// Set input content type
		// method.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
		method.setRequestHeader("Content-Type", "text/html; charset=UTF-8");
		//method.setRequestHeader("Content-Type", "text/raw; charset=UTF-8");

		// Set response/output format
		method.setRequestHeader("Accept", "xml/rdf");
		//method.setRequestHeader("Accept", "application/json");

		// Enable Social Tags processing
		method.setRequestHeader("enableMetadataType", "SocialTags");

		return method;
	}

	private void run() {
		try {
			if (input.isFile()) {
				postFile(input, createPostMethod());
			} else if (input.isDirectory()) {
				System.out.println("working on all files in " + input.getAbsolutePath());
				for (File file : input.listFiles()) {
					if (file.isFile())
						postFile(file, createPostMethod());
					else
						System.out.println("skipping "+file.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doRequest(File file, PostMethod method) {
		try {
			int returnCode = client.executeMethod(method);
			if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
				System.err.println("The Post method is not implemented by this URI");
				// still consume the response body
				method.getResponseBodyAsString();
			} else if (returnCode == HttpStatus.SC_OK) {
				System.out.println("File post succeeded: " + file);
				//   System.out.println(method.getResponseBodyAsString());

				saveResponse(file, method);
			} else {
				System.err.println("File post failed: " + file);
				System.err.println("Got code: " + returnCode);
				System.err.println("response: "+method.getResponseBodyAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}

	private void saveResponse(File file, PostMethod method) throws IOException {
		PrintWriter writer = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					method.getResponseBodyAsStream(), "UTF-8"));
			File out = new File(output, file.getName() + ".xml");
			writer = new PrintWriter(new BufferedWriter(new FileWriter(out)));
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) try {writer.close();} catch (Exception ignored) {}
		}
	}

	private void postFile(File file, PostMethod method) throws IOException {
		method.setRequestEntity(new FileRequestEntity(file, null));
		doRequest(file, method);
	}


	public static void main(String[] args) {
		output = new File("out");
		input = new File("in");
		try {
		//	URL urls = new URL("http://www.bbc.com/sport/0/sports-personality/30267315");
		// 1.	URL urls = new URL("http://abcnews.go.com/US/nypd-officer-indicted-eric-garner-choke-hold-death/story?id=27341079");
			URL urls = new URL("http://www.chron.com/news/us/article/UN-campaign-seeks-64-million-for-Syrian-refugees-5932973.php");

			
			//URL urls = new URL("http://www.bbc.com/news/health-30254697");
			FileUtils.copyURLToFile(urls, input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//  verifyArgs(args);
		HttpClientPost httpClientPost = new HttpClientPost();
		//httpClientPost.input = new File(args[0]);
		//httpClientPost.output = new File(args[1]);
		httpClientPost.client = new HttpClient();
		httpClientPost.client.getParams().setParameter("http.useragent", "Calais Rest Client");

		httpClientPost.run();
		try {
			queryRDFTriples();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**  private static void verifyArgs(String[] args) {
        if (args.length==0) {
            usageError("no params supplied");
        } else if (args.length < 2) {
            usageError("2 params are required");
        } else {
            if (!new File(args[0]).exists())
                usageError("file " + args[0] + " doesn't exist");
            File outdir = new File(args[1]);
            if (!outdir.exists() && !outdir.mkdirs())
                usageError("couldn't create output dir");
        }
    }**/

	private static void usageError(String s) {
		System.err.println(s);
		System.err.println("Usage: java " + (new Object() { }.getClass().getEnclosingClass()).getName() + " input_dir output_dir");
		System.exit(-1);
	}

	public static void queryRDFTriples() throws RepositoryException, IOException, RDFParseException{
		FileOutputStream file = new FileOutputStream("output.txt");
		// TODO Auto-generated method stub
		Repository rep = new SailRepository(new MemoryStore());
		rep.initialize();
		RepositoryConnection conn = rep.getConnection();
		File fileInput = new File("/Users/priyakotwal/Documents/interviews etc/testWorkspace/hw13C/out/in.xml");
		conn.add( fileInput, null, RDFFormat.RDFXML);

		try {
			RepositoryResult<Statement> statements =  conn.getStatements(null, null, null, true);
			Model model = Iterations.addAll(statements, new LinkedHashModel());
			model.setNamespace("rdf", RDF.NAMESPACE);



			//Rio.write(model, System.out, RDFFormat.TURTLE);
			String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ "	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "	PREFIX dbpprop:	<http://dbpedia.org/property/> "
					+ "	PREFIX dbpedia:	<http://dbpedia.org/resource/> "
					+ "	PREFIX dbpedia-owl:	<http://dbpedia.org/ontology/> "

				/**		+ "	SELECT DISTINCT (STR(?person1) as ?person)"
						+ "	WHERE { <rdf:Description rdf:about='http://d.opencalais.com/pershash-1/afac687f-d828-3aeb-99fa-138e9f55c6e0'> a <rdf:type rdf:resource='http://s.opencalais.com/1/type/em/e/Person'/> ."
						+ "<c:name> ?person1 </c:name> .}"; **/
			

			+ "	SELECT DISTINCT (STR(?person1) as ?person) (STR(?city1) as ?city) (STR(?org1) as ?org)"
			+ "	WHERE { ?per a <http://s.opencalais.com/1/type/em/e/Person/> ."
			+ "<c:name> ?person1 </c:name> ."
			+ "?type a <http://s.opencalais.com/1/type/er/Geo/City> ."
			+ "<c:name> ?city1 </c:name> ."
			+ "?type1 a <http://s.opencalais.com/1/type/em/e/Organization>"
			+ "<c:name> ?org1 </c:name>";
			
			
					//	+ "	WHERE {<c:name> ?person1 </c:name>}";
System.out.println(q);
		//	Query query = QueryFactory.create(q); //s2 = the query above
		//	QueryExecution qExe = QueryExecutionFactory.sparqlService( "http://dbpedia.org/sparql", query );		 	

			conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
			TupleQueryResult res = conn.prepareTupleQuery(QueryLanguage.SPARQL, q).evaluate();
			System.out.println("List : "+"\n");
			while(res.hasNext()) {
				System.out.println("1");
				BindingSet bs = res.next();
				System.out.println(" "+bs.toString());

			}

				//ResultSet results = qExe.execSelect();
				//ResultSetFormatter.out(file,results,query);
			} catch(Exception e) {
				System.out.println("error in executing query"+e.getMessage());
			}
		file.flush();
		file.close();
		
		}
	

	public static void makeRepo() throws RepositoryException{

		try {
			Repository repo = new SailRepository(new MemoryStore());
			repo.initialize();
			RepositoryConnection conn = repo.getConnection();
			File file = new File("/Users/priyakotwal/Documents/interviews etc/testWorkspace/hw13C/out/in.xml");
			conn.add( file, null, RDFFormat.RDFXML);
			getRdf();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getRdf(){

		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "	PREFIX dbpprop:	<http://dbpedia.org/property/> "
				+ "	PREFIX dbpedia:	<http://dbpedia.org/resource/> "
				+ "	PREFIX dbpedia-owl:	<http://dbpedia.org/ontology/> "

				+ "	SELECT DISTINCT (STR(?person1) as ?person)"
				+ "	WHERE {<rdf:type rdf:resource='http://s.opencalais.com/1/type/em/e/Person'/> <c:name> ?person}";

		try {
			FileOutputStream file = new FileOutputStream("outputHW13.txt");
			ResultSet results = queryDBPedia(queryString);
			Query query = QueryFactory.create(queryString);
			ResultSetFormatter.out(file,results,query);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ResultSet resultSet;
			resultSet = queryDBPedia(queryString);
			executeQueries();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static ResultSet queryDBPedia(String queryString) {

		Query query = QueryFactory.create(queryString);	
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://live.dbpedia.org/sparql", query);
		ResultSet results = null;
		try {
			int count =1;
			results = qexec.execSelect();
		}
		finally {
		}
		return results;
	} 

	private static void executeQueries()
			throws Exception {
		// Repository repo = manager.getRepository(REPO_ID);
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();
		File file = new File("/Users/priyakotwal/Documents/interviews etc/testWorkspace/hw13CSCI548/out/in.xml");
		conn.add( file, null, RDFFormat.RDFXML);
	}

}