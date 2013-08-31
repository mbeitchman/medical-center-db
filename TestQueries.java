import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.FileInputStream;


/**
	Runs the test query workload, based on the UWMC synthetic database.
	@author Nodira Khoussainova, Michael Ratanapintha
 */
public class TestQueries 
{
	private static final int 	MAX_AGE = 100;

	private static Properties 	configProps = new Properties();
	private static String 		postgreSQLDriver;
	private static String 		postgreSQLUrl;
	private static String 		postgreSQLUser;
	private static String 		postgreSQLPassword;

	private static Connection 	conn;

	private static int dbSizeScale = 20; //scale at which the DatabaseGenerator was run.
	private static int scaleFactor = 1;  //controls how many times each query runs. relationship is linear.
	private static int numPatients = dbSizeScale * 1000;


	/* Examine these queries: you need to tune the database accordingly.
	 */


	//look up a doctor with a given specialty.
	private static final String docSpecialty 	=	"SELECT fname, lname " +
		"FROM Doctor WHERE specialty = ?";

	//look up all doctors for a patient
	private static final String docsForPatient 	= 	"SELECT D.fname, D.lname " +
		"FROM Doctor D, Sees S, Patient P " +
		"WHERE D.did = S.did AND S.pid = P.pid AND P.fname = ? AND P.lname = ?";
	//look up all patients for a doctor
	private static final String patientsForDoc 	= 	"SELECT P.fname, P.lname " +
		"FROM Doctor D, Sees S, Patient P " +
		"WHERE D.did = S.did AND S.pid = P.pid AND D.fname = ? AND D.lname = ?";

	//check how many patients have a certain disease.
	private static final String diseaseCount 	= 	"SELECT Di.disease, count(*) "  +
		"FROM Disease Di " +
		"WHERE Di.disease = ? " +
		"GROUP BY Di.disease";

	//count number of patients within an age range (used by Marketing team for targeted advertising)
	private static final String patientsAge 	= 	"SELECT count(*) " +
		"FROM Patient " +
		"WHERE age > ? AND age < ?";

	//count number of patients living in a specific zipcode (used by Marketing team for targeted advertising)
	private static final String patientsZipcode =	"SELECT count(*) " +
		"FROM Patient " +
		"WHERE zipcode = ? ";


	//corresponding preparedstatements:
	private static PreparedStatement docSpecialtyStmt;
	private static PreparedStatement docsForPatientStmt;
	private static PreparedStatement patientsForDocStmt;
	private static PreparedStatement diseaseCountStmt;
	private static PreparedStatement patientsAgeStmt;
	private static PreparedStatement patientsZipcodeStmt;

	public static void openConnection() throws Exception 
	{
		Class.forName(postgreSQLDriver).newInstance();
		conn = DriverManager.getConnection(	postgreSQLUrl, // conn
			postgreSQLUser, // user
			postgreSQLPassword); // password
	}

	public static void closeConnection() throws Exception 
	{
		conn.close();
	}

	public static void prepareStatements() throws Exception 
	{
		docSpecialtyStmt		= conn.prepareStatement(docSpecialty);
		docsForPatientStmt		= conn.prepareStatement(docsForPatient);
		patientsForDocStmt		= conn.prepareStatement(patientsForDoc);
		diseaseCountStmt		= conn.prepareStatement(diseaseCount);
		patientsAgeStmt	= conn.prepareStatement(patientsAge);
		patientsZipcodeStmt	= conn.prepareStatement(patientsZipcode);
	}



	/* 	Runs query, stores resultset into 2d array.
	 *	@param sql 			the sql query string - must select only String columns.
	 *	@param colCount		number of columns selected by sql.
	 *	@returns 			2d array containing the results of the query.
	 */
	private static String[][] getResults(String sql, int colCount) throws Exception
	{
		Statement nameStmt 	= conn.createStatement();
		ResultSet rs 		= nameStmt.executeQuery(sql);

		int count = 0;
		while(rs.next())
		{
			count++;
		}

		String[][] values 	= new String[count][colCount];
		rs.close();
		rs = nameStmt.executeQuery(sql);

		int index = 0;
		while(rs.next())
		{
			for(int i=0; i<colCount; i++)
			{
				values[index][i] = rs.getString(i+1);
			}
			index++;
		}
		return values;
	}



	/*
	 *	Runs the workload.
	 */

	public static long runQueries () throws Exception 
	{
		String[][] patientNames = getResults("SELECT distinct fname, lname FROM Patient", 2);
		String[][] doctorNames  = getResults("SELECT distinct fname, lname FROM Doctor", 2);
		String[][] specialties  = getResults("SELECT distinct specialty FROM Doctor", 1);
		String[][] zipcodes = getResults("SELECT distinct zipcode FROM Patient", 1);
		String[][] diseases = getResults("SELECT distinct disease FROM Disease", 1);

		long totalTime = 0;
		for (int i = 1; i <= 1000 * scaleFactor; i++) 
		{
			docSpecialtyStmt.setString(1, specialties[(int)(Math.random() * specialties.length)][0]);

			int randPatientIndex = (int)(Math.random() * patientNames.length);
			String fname = patientNames[randPatientIndex][0];
			String lname = patientNames[randPatientIndex][1];
			docsForPatientStmt.setString(1, fname);
			docsForPatientStmt.setString(2, lname);

			int randDoctorIndex = (int)(Math.random() * doctorNames.length);
			fname = doctorNames[randDoctorIndex][0];
			lname = doctorNames[randDoctorIndex][1];
			patientsForDocStmt.setString(1, fname);
			patientsForDocStmt.setString(2, lname);

			diseaseCountStmt.setString(1, diseases[(int)(Math.random() * diseases.length)][0]);

			int randomAge = (int)(Math.random() * MAX_AGE);
			patientsAgeStmt.setInt(1, randomAge);
			patientsAgeStmt.setInt(2, randomAge + 2);

			patientsZipcodeStmt.setString(1, zipcodes[(int)(Math.random() * zipcodes.length)][0]);


			//Run queries, time them and add to totalTime.
			long startTime = System.currentTimeMillis();
			docSpecialtyStmt.executeQuery().close();
			docsForPatientStmt.executeQuery().close();
			patientsForDocStmt.executeQuery().close();

			diseaseCountStmt.executeQuery().close();
			patientsAgeStmt.executeQuery().close();
			patientsZipcodeStmt.executeQuery().close();
			long endTime = System.currentTimeMillis();
			totalTime += (endTime - startTime);
		}

		return totalTime;
	}

	public static void main (String args[]) throws Exception
	{
		// get the scale factor (if it is specified)
		if (args.length == 0)
		{
			scaleFactor = 1; //i.e. the default
		}
		else if (args.length == 1)
		{
			try
			{
				scaleFactor = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				System.err.println ("Unable to parse scale factor given as: \"" + args[0] + "\"");
				System.exit(2);
			}
		}
		else
		{
			System.err.println ("Usage: java TestQueries [scale factor]");
			System.exit(1);
		}


		configProps.load(new FileInputStream("dbconn.config"));
		postgreSQLDriver   = configProps.getProperty("postgreSQLDriver");
		postgreSQLUrl	   = configProps.getProperty("postgreSQLUrl");
		postgreSQLUser	   = configProps.getProperty("postgreSQLUser");
		postgreSQLPassword = configProps.getProperty("postgreSQLPassword");

		openConnection();
		prepareStatements();

		System.out.println ("Running the test query workload in database. \r\n");
		long queryTime = runQueries();
		System.out.println ("Queries complete. \r\n Total time: " + queryTime + " ms.");

		closeConnection();
	}
}
