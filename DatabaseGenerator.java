import java.io.*;
import java.util.*;

 /**
	Output is a set of generated tables into tab-separated files that can be inserted into
	a database using the DBMS's bulk import command.
	
	See importDatabase.sql for the database schema.
	@author Michael Ratanapintha
  */
public class DatabaseGenerator {
	/** Pseudo-random number generator used by all synthesis methods. */
	private Random rand;
	/** Scales the number of rows in each table, among other things below. */
	private int scale;
	/** Default value of the scale if not specified. */
	private static final int DEFAULT_SCALE = 20;
	
	// Counts controlled (directly or indirectly) by scale
	
	private int getCountPatients () { return 1000 * scale; }
	private int getCountVoters () { return 10000 * scale; }
	private long getTotalDiseases() 
		{ return Math.round(getCountPatients() * AVG_DISEASES_PER_PATIENT); }
	private int getCountDoctors () { return 50 * scale; }
	private long getTotalSees() 
		{ return Math.round(getCountPatients() * AVG_DOCTORS_PER_PATIENT); }
	private long getTotalSupplies()
		{ return Math.round(SUPPLIER_NAMES.length * AVG_SUPPLIERS_PER_PRODUCT); }
	private int getMaxHouseNumber() { return 100 * getMaxStreetNumber(); }	
	private int getMaxStreetNumber() { return MAX_STREET_NUMBER; }
	private int getMaxStock() { return MAX_STOCK; }
	
	// Various parameters of the generated data

	private static final int MAX_STOCK = 2000;
	private static final int MAX_STREET_NUMBER = 300;
	protected static final int MAX_AGE = 100;
	// Some people might be seeing the same doctor for multiple ailments
	private static final double AVG_DISEASES_PER_PATIENT = 1.4;
	private static final double AVG_DOCTORS_PER_PATIENT = 1.2;
	private static final double AVG_SUPPLIERS_PER_PRODUCT = 3.1;
	/** Max length of a street address in the SQL schema */
	private static final int MAX_STREET_LENGTH = 20;

	// Various base names for data
	
	// Max length of each name: 20
	private static final String[] FIRST_NAMES = new String [] {
		"Suzanne",
		"John",
		"David",
		"Ashley",
		"Leah",
		"Heather",
		// Top 10 girls', boys' names 
		// from http://www.socialsecurity.gov/OACT/babynames/ 
		// (visited on April 29, 2009)
		"Emily",
		"Isabella",
		"Emma",
		"Ava",
		"Madison",
		"Sophia",
		"Olivia",
		"Abigail",
		"Hannah",
		"Elizabeth",

		"Jacob",
		"Michael",
		"Ethan",
		"Joshua",
		"Daniel",
		"Christopher",
		"Anthony",
		"William",
		"Matthew",
		"Andrew",
	};
	
	// Max length of each: 20
	private static final String[] LAST_NAMES = new String[] {
		"Chin",
		"Smith",
		"Kim",
		"Jackson",
		"Roberts",
		"Achebe",
		"Baker",
		"Esteban",
		"MacDonald",
		"Lucas",
		"Hernandez",
		"Ramirez",
		"El-Baz",
		"Wilson",
		"Crichton",
		"Philips",
		"Carter"

	};

	private static final String[] DIRECTIONS = new String[] 
		{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
	
	// Max length of each: 10	
	protected static final String[] CITIES = new String[] {
		"Kirkland",
		"Seattle",
		"Vancouver",
		"Bremerton",
		"Brier",
		"Mukilteo",
		"Sumner",
		"Puyallup",
		"Sequim",
		"Renton",
		"Moses Lake",
		"Bothell"
	};
	
	// Max length of each: 20
	protected static final String[] DISEASES = new String[] {
		"heart failure",
		"chemical burn",
		"lung cancer",
		"breast cancer",
		"other cancer",
		"repetitive stress",
		"insomnia",
		"hearing loss",
		"arthritis",
		"glaucoma",
		"broken limb",
		"sickle-cell anemia",
		"kidney failure"
	};
	
	// Max length of each: 20
	protected static final String[] SPECIALTIES = new String[] {
		"cardiology",
		"pulmonology",
		"orthopedics",
		"obstretics",
		"gynecology",
		"urology",
		"otology",
		"pediatrics",
		"neurology",
		"neurosurgery",
		"surgery",
		"opthamology",
		"dentistry",
		"immunology",
		"psychology",
		"dermatology"
	};
	
	// Max length of each: 20
	private static final String[] PRODUCT_DESCRIPTIONS = new String[] {
		"stethoscope",
		"scalpel",
		"blanket",
		"bedsheet",
		"towel",
		"X-ray film",
		"clipboard",
		"blank medical chart",
		"pipette",
		"test tube",
		"saline solution",
		"type O- blood",
		"meal tray",
		"linen bandage",
		"plaster bandage",
		"paper cup",
		"hospital robe",
		"soap",
		"cleaning fluid"
		// Other ideas?
	};
	
	// Max length of each: 20
	private static final String[] SUPPLIER_NAMES = new String[] {
		// (hopefully) avoiding trademark infringement here...
		"Quantum Pharma.",
		"New Medical Supply",
		"Intercon. Linens",
		"American Plastics",
		"Everett Chemical",
		"Blood Centers",
		"Wilson Electronics",
		"Western Diagnostics",
		"Hippocratic Systems"
	};
	
	/** String used to separate fields of a record in the data file */
	private static final String FIELD_SEP = "\t";
	
	public static void main (String[] args) throws Exception {
		if (args.length != 0 && args.length != 1) {
			System.out.println ("Usage: java DatabaseGenerator [scale]");
			System.exit (1);
		}
		
		DatabaseGenerator dbgen = null;
		if (args.length == 1) {
			// Will throw NumberFormatException if invalid format
			int scale = Integer.parseInt(args[0]);
			dbgen = new DatabaseGenerator(scale);
		} else {
			dbgen = new DatabaseGenerator();
		}
		
		dbgen.writePatientTable("tablePatient.txt");
		dbgen.writeDiseaseTable("tableDisease.txt");
		dbgen.writeDoctorTable("tableDoctor.txt");
		dbgen.writeSeesTable("tableSees.txt");
		dbgen.writeProductTable("tableProduct.txt");
		dbgen.writeStockTable("tableStock.txt");
		dbgen.writeSupplierTable("tableSupplier.txt");
		dbgen.writeSuppliesTable("tableSupplies.txt");
		dbgen.writeVoterTable("tableHealthyVoter.txt", true);
	}

	public DatabaseGenerator () {
		this(DEFAULT_SCALE);
	}

	public DatabaseGenerator (int scale) {
		this.rand = new Random();
		this.scale = scale;
	}
	
	
	// Generating patients
	
	/** Generates, writes out the data for the Patient table */
	public void writePatientTable(String filename) throws FileNotFoundException {
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (int ipatient = 1; ipatient <= getCountPatients(); ipatient++) {
				outFile.println(makePatientRecord(ipatient));
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	
	/** Generates a patient record 
	  (form: id, fname, lname, age, street, city, zipcode)*/
	private String makePatientRecord (int id) {
		return makePerson(id) + FIELD_SEP +
			// assumes uniform age distribution in 1..MAX_AGE
			// (not realistic)
			(1+rand.nextInt(MAX_AGE)) + FIELD_SEP +
			makeAddress(MAX_STREET_LENGTH);
	}
	

	// Disease-patient associations
	
	/** Generates, writes out the data for the Disease table */
	public void writeDiseaseTable(String filename) throws Exception {
		// Use this table to map patient IDs to diseases, so
		// no patient has "multiple copies" of the same disease.
		// Note: this may cause the actual total number of diseases to be less than
		// the return value of getTotalDiseases().
		Map<Integer, List<String>> patientsToDiseases = new HashMap<Integer, List<String>>();
		
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (long iDisease = 1; iDisease <= getTotalDiseases(); iDisease++) {
				int diseasedPatientId = 1 + rand.nextInt(getCountPatients());
				String disease = DISEASES[rand.nextInt(DISEASES.length)];
				
				List<String> existingDiseases = patientsToDiseases.get(diseasedPatientId);
				if (existingDiseases == null) {
					assert ! patientsToDiseases.containsKey(diseasedPatientId);
					existingDiseases = new ArrayList<String>();
					patientsToDiseases.put(diseasedPatientId, existingDiseases);
				}
				
				if ( ! existingDiseases.contains(disease) ) {
					existingDiseases.add(disease);
					outFile.println (diseasedPatientId + FIELD_SEP + disease);
				}
				// else (if the patient already has the disease), skip it
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	
	
	// Doctors
	
	/** Generates, writes out the data for the Doctor table */
	public void writeDoctorTable(String filename) throws FileNotFoundException {
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (int idoctor = 1; idoctor <= getCountDoctors(); idoctor++) {
				outFile.println(makeDoctorRecord(idoctor));
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}

	/** Generates a doctor record (form: id, fname, lname) */
	private String makeDoctorRecord (int id) {
		return makePerson(id) + FIELD_SEP + 
			SPECIALTIES[rand.nextInt(SPECIALTIES.length)];
	}
	
	
	// Doctor-patient associations
	
	/** Generates, writes out the data for the Sees table */
	public void writeSeesTable(String filename) throws Exception {
		// Use this table to map patients to doctors, to avoid duplicate (patient, doctor) tuples.
		// Note: this may cause the actual total number of doctor-patient relationships
		// to be less than the return value of getTotalSees().
		Map<Integer, List<Integer>> patientsToDoctors = new HashMap<Integer, List<Integer>>();
		
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (long iSees = 1; iSees <= getTotalSees(); iSees++) {
				int patientId = 1 + rand.nextInt(getCountPatients());
				int doctorId = 1 + rand.nextInt(getCountDoctors());
				
				List<Integer> existingDoctors = patientsToDoctors.get(patientId);
				if (existingDoctors == null) {
					assert ! patientsToDoctors.containsKey(patientId);
					existingDoctors = new ArrayList<Integer>();
					patientsToDoctors.put(patientId, existingDoctors);
				}
				
				if ( ! existingDoctors.contains(doctorId) ) {
					existingDoctors.add(doctorId);
					outFile.println (patientId + FIELD_SEP + doctorId);
				}
				// else (if the patient already sees that doctor), skip it
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	
	
	// Product records
	/** Generates, writes out the data for the Product table */
	public void writeProductTable(String filename) throws FileNotFoundException {
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (int iproduct = 0; iproduct < PRODUCT_DESCRIPTIONS.length; iproduct++) {
				outFile.println((iproduct+1) + FIELD_SEP + PRODUCT_DESCRIPTIONS[iproduct]);
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}

	
	// Product-in-stock records
	/** Generates, writes out the data for the Stock table */
	public void writeStockTable(String filename) throws FileNotFoundException {
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (int iproduct = 0; iproduct < PRODUCT_DESCRIPTIONS.length; iproduct++) {
				outFile.println((iproduct+1) + FIELD_SEP + (1+rand.nextInt(getMaxStock())));
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	
	
	// Supplier records
	
	/** Generates, writes out the data for the Supplier table */
	public void writeSupplierTable(String filename) throws FileNotFoundException {
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (int isupplier = 0; isupplier < SUPPLIER_NAMES.length; isupplier++) {
				outFile.println(
					makeSupplierRecord(isupplier+1, SUPPLIER_NAMES[isupplier]));
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	
	/** Generates a supplier record 
	  (form: id, name, street, city, zipcode)*/
	public String makeSupplierRecord (int id, String name) {
		//(isupplier+1) + FIELD_SEP + SUPPLIER_NAMES[isupplier]
		String supplierStr = id + FIELD_SEP;
		supplierStr += name + FIELD_SEP;
		supplierStr += makeAddress(MAX_STREET_LENGTH);
		return supplierStr;
	}
	
	
	// Who-supplies-what records
	/** Generates, writes out the data for the Supplies table */
	public void writeSuppliesTable(String filename) throws Exception {
		// Use this table to map products to suppliers, to avoid duplicate (product, supplier) tuples.
		// Note: this may cause the actual total number of product-supplier relationships
		// to be less than the return value of getTotalSupplies().
		Map<Integer, List<Integer>> productsToSuppliers = new HashMap<Integer, List<Integer>>();
		
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (long iproduct = 1; iproduct <= getTotalSupplies(); iproduct++) {
				int productId = 1 + rand.nextInt(PRODUCT_DESCRIPTIONS.length);
				int supplierId = 1 + rand.nextInt(SUPPLIER_NAMES.length);
				
				List<Integer> existingSuppliers = productsToSuppliers.get(productId);
				if (existingSuppliers == null) {
					assert ! productsToSuppliers.containsKey(productId);
					existingSuppliers = new ArrayList<Integer>();
					productsToSuppliers.put(productId, existingSuppliers);
				}
				
				if ( ! existingSuppliers.contains(supplierId) ) {
					existingSuppliers.add(supplierId);
					outFile.println (productId + FIELD_SEP + supplierId);
				}
				// else (if the supplier already sells that product), skip it
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	

	// Voter registration
	// Unlike the other tables this generator does not write an ID number
	// separate from the name of the voter.
	// We could have omitted the ID with other tables if we made the types
	// of ID field in the schema serial rather than int.

	public void writeVoterTable(String filename, boolean disjointIdWithPatient)
	throws FileNotFoundException {
		if (disjointIdWithPatient) 
			writeVoterTable(filename, getCountPatients() + 1);
		else
			writeVoterTable(filename, 1);
	}

	/** Generates, writes out the data for the Voter table */
	public void writeVoterTable(String filename, int minID) 
   	throws FileNotFoundException {
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter (filename);
			for (int ivoter = minID; ivoter <= minID + getCountVoters(); ivoter++) {
				outFile.println(makeVoterRecord(ivoter));
			}
		} finally {
			if (outFile != null) outFile.close();
		}
	}
	
	/** Generates a voter record 
	  (form: fname, lname, age, zipcode)*/
	private String makeVoterRecord (int id) {
		return FIRST_NAMES[rand.nextInt(FIRST_NAMES.length)] 
			// use id number as name uniquifier
			+ " (" + id + ")" + FIELD_SEP
			+ LAST_NAMES[rand.nextInt(LAST_NAMES.length)] + FIELD_SEP
			// assumes uniform age distribution in 0..MAX_AGE
			// (not realistic)
			+ (rand.nextInt(MAX_AGE+1)) + FIELD_SEP
			+ makeZipCode();
	}
	

	// Utility functions
	
	/** Generates the series of fields (id, fname, lname) */
	private String makePerson (int id) {
		return id + FIELD_SEP
			+ FIRST_NAMES[rand.nextInt(FIRST_NAMES.length)] 
			// use id number as name uniquifier
			+ " (" + id + ")" + FIELD_SEP
			+ LAST_NAMES[rand.nextInt(LAST_NAMES.length)];
	}
	
	/** Generates the series of fields (street_address, city, zip) */
	private String makeAddress (int streetLimit) {
		return makeStreetAddress(MAX_STREET_LENGTH) + FIELD_SEP
			+ CITIES[rand.nextInt(CITIES.length)] + FIELD_SEP
			+ makeZipCode();
	}
	
	/** Generates street address of form "1000 53rd St NE" 
	  (truncated to be at most limit characters) */
	private String makeStreetAddress (int limit) {
		String addressStr = "";
		
		int houseNumber = 1 + rand.nextInt(getMaxHouseNumber());
		if (houseNumber == 1) { 
			addressStr += "One ";
		} else {
			addressStr += houseNumber + " ";
		}
		
		addressStr += getOrdinalFormOf(1 + rand.nextInt(getMaxStreetNumber())) + " ";
		addressStr += rand.nextBoolean() ? "St " : "Ave ";
		addressStr += DIRECTIONS[rand.nextInt(DIRECTIONS.length)];
		
		if (addressStr.length() > limit) {
			return addressStr.substring(0, limit);
		} else {
			return addressStr;
		}
	}

	/** Generates a plausible zip code for the state of Washington */
	public int makeZipCode() {
		return 98000 + rand.nextInt(1000);
	}

	/** Stringifies number in English ordinal form */
	public static String getOrdinalFormOf (int value) {
		return value + getOrdinalSuffixFor(value);
	}
	
	/** Returns English ordinal suffix for the value
	  (from http://www.javalobby.org/java/forums/t16906.html) */
	public static String getOrdinalSuffixFor(int value) {
		int hundredRemainder = value % 100;
		if(hundredRemainder >= 10 && hundredRemainder <= 20) {
			return "th";
		}
		int tenRemainder = value % 10;
		switch (tenRemainder) {
			case 1:
			return "st";
			case 2:
			return "nd";
			case 3:
			return "rd";
			default:
			return "th";
		}
	}
}
