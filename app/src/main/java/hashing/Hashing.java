package hashing;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class Hashing {
	
	// Generates an sha1 hash from a given string.
	// Then it sums up all the numeric digits in the hash
	// and returns the modulo of the division by the offset.
	public static int Hash(String content, int offset) {
		/*** step 1, hash content using sha1 ***/
		SimpleSHA1 sha1 = new SimpleSHA1();
		String hashedContent = sha1.SHA1(content);
		System.out.println(hashedContent);
		
		/*** step 2, sum up the numeric values ***/
		int sum = 0;
		for (int i=0; i<hashedContent.length(); i++) {
			if (Character.isDigit(hashedContent.charAt(i))) {
				int digit = Character.getNumericValue(hashedContent.charAt(i));
				sum += digit;
			}
		}
		System.out.println("sum: " + sum);
		
		/*** step 3, get a modulo of the sum ***/
		int id = sum % offset; // values from 0 to offset - 1
		
		return id;
	}

	// test the hash function
	public static void main(String[] args) {
		/*
		String content = "["
				+ "{\"bounds\":"
				+ "{\"northeast\":{\"lat\":37.9941503,\"lng\":23.7704548},"
				+ "\"southwest\":{\"lat\":37.97779010000001,\"lng\":23.7314487}"
				+ "},"
				+ "\"copyrights\":\"Map data ©2016 Google\","
				+ "\"legs\":"
				+ "["
				+ "{\"distance\":{\"text\":\"4.6 km\",\"value\":4554},"
				+ "\"duration\":{\"text\":\"16 mins\",\"value\":931},"
				+ "\"end_address\":\"28is Oktovriou 78, Athina 104 34, Greece\","
				+ "\"end_location\":{\"lat\":37.9941503,\"lng\":23.7319537},"
				+ "\"start_address\":\"Leof. Stratarchou Alexandrou Papagou 93, Zografou 157 73, Greece\","
				+ "\"start_location\":{\"lat\":37.97779010000001,\"lng\":23.7704548"
				+ "},"
				+ "\"steps\":"
				+ "["
				+ "{\"distance\":{\"text\":\"0.8 km\",\"value\":844},"
				+ "\"duration\":{\"text\":\"4 mins\",\"value\":223},"
				+ "\"end_location\":{\"lat\":37.980216,\"lng\":23.7613649},"
				+ "\"html_instructions\":\"Head <b>west</b> on <b>Leof. Stratarchou Alexandrou Papagou</b> toward <b>Dimokratias</b>\","
				+ "\"polyline\":{\"points\":\"epxfFitapCSfACL]rBOz@Id@Gd@Q`AUxACJMr@UrAAD_@|Bc@fCa@~B]nBWrAm@xCGXMn@Qr@a@bBc@pBCF?DAP?B?PBFAZ\"},"
				+ "\"start_location\":{\"lat\":37.97779010000001,\"lng\":23.7704548},"
				+ "\"travel_mode\":\"DRIVING\""
				+ "},"
				+ "{\"distance\":{\"text\":\"0.2 km\",\"value\":181},"
				+ "\"duration\":{\"text\":\"1 min\",\"value\":33},"
				+ "\"end_location\":{\"lat\":37.9801795,\"lng\":23.7592967},"
				+ "\"html_instructions\":\"Continue onto <b>Papadiamantopoulou</b>\","
				+ "\"polyline\":{\"points\":\"k_yfFo{_pC?Z@T@lA?L@rA?nB@N?x@\"},"
				+ "\"start_location\":{\"lat\":37.980216,\"lng\":23.7613649},"
				+ "\"travel_mode\":\"DRIVING\""
				+ "},"
				+ "{\"distance\":{\"text\":\"0.3 km\",\"value\":343},"
				+ "\"duration\":{\"text\":\"2 mins\",\"value\":128},"
				+ "\"end_location\":{\"lat\":37.9828283,\"lng\":23.7573079},"
				+ "\"html_instructions\":\"Turn <b>right</b> onto <b>Xenias</b>\","
				+ "\"maneuver\":\"turn-right\","
				+ "\"polyline\":{\"points\":\"c_yfFsn_pCc@^YT}@j@aAp@[ROJe@XwA|@cAp@iAr@[Z\"},"
				+ "\"start_location\":{\"lat\":37.9801795,\"lng\":23.7592967},"
				+ "\"travel_mode\":\"DRIVING\""
				+ "},"
				+ "{\"distance\":{\"text\":\"86 m\",\"value\":86},"
				+ "\"duration\":{\"text\":\"1 min\",\"value\":44},"
				+ "\"end_location\":{\"lat\":37.9830785,\"lng\":23.756383},"
				+ "\"html_instructions\":\"Continue onto <b>Dorileou</b>\","
				+ "\"polyline\":{\"points\":\"uoyfFeb_pCKVIn@QhAIf@\"},"
				+ "\"start_location\":{\"lat\":37.9828283,\"lng\":23.7573079},"
				+ "\"travel_mode\":\"DRIVING\""
				+ "},"
				+ "{\"distance\":{\"text\":\"0.5 km\",\"value\":465},"
				+ "\"duration\":{\"text\":\"2 mins\",\"value\":125},"
				+ "\"end_location\":{\"lat\":37.9871219,\"lng\":23.7577137},"
				+ "\"html_instructions\":\"Turn <b>right</b> onto <b>Dim. Soutsou</b>\","
				+ "\"maneuver\":\"turn-right\","
				+ "\"polyline\":{\"points\":\"gqyfFk|~oCq@Oo@MiCs@oAWgDq@m@KkASs@OWK}Bq@\"},"
				+ "\"start_location\":{\"lat\":37.9830785,\"lng\":23.756383},"
				+ "\"travel_mode\":\"DRIVING\""
				+ "},"
				+ "{\"distance\":{\"text\":\"2.4 km\",\"value\":2357},"
				+ "\"duration\":{\"text\":\"6 mins\",\"value\":330},"
				+ "\"end_location\":{\"lat\":37.9917819,\"lng\":23.7317617},"
				+ "\"html_instructions\":\"Turn <b>left</b> onto <b>Leof. Alexandras</b>\","
				+ "\"maneuver\":\"turn-left\","
				+ "\"polyline\":{\"points\":\"ojzfFud_pCUWCP]`DQnAYnCGf@CTE\\YhC_@tDGn@YdC_@hDIj@SpBY`CMbASlBc@dESlBIz@a@bDKjAMpAMlAAJCPUzBSxBWxBUbCObBAT[hDUbCIz@MnAAFC^Cd@Ch@CVYxC]nEc@pEa@zEs@~Hc@xEU~BSrB\"},"
				+ "\"start_location\":{\"lat\":37.9871219,\"lng\":23.7577137},"
				+ "\"travel_mode\":\"DRIVING\""
				+ "},"
				+ "{\"distance\":{\"text\":\"0.3 km\",\"value\":278},"
				+ "\"duration\":{\"text\":\"1 min\",\"value\":48},"
				+ "\"end_location\":{\"lat\":37.9941503,\"lng\":23.7319537},"
				+ "\"html_instructions\":\"Turn <b>right</b> onto <b>28is Oktovriou</b><div style=\\\"font-size:0.9em\\\">Destination will be on the right</div>\",\"maneuver\":\"turn-right\","
				+ "\"polyline\":{\"points\":\"sg{fFobzoCIJOPKJIJKFUCaC]s@K_@GsAU}AW\"},"
				+ "\"start_location\":{\"lat\":37.9917819,\"lng\":23.7317617},"
				+ "\"travel_mode\":\"DRIVING\"}"
				+ "],"
				+ "\"traffic_speed_entry\":[],\"via_waypoint\":[]}"
				+ "],"
				+ "\"overview_polyline\":{\"points\":\"epxfFitapC_ClNmDnS{AvH{ApGAl@BFAZ@p@B~G@hA}@t@kD|BkG|D[ZKV[xBIf@q@OyDaAwFiAyB_@kA[}Bq@UWa@rDk@~Ek@dFaBtOeAbJuA|Mm@nF[~Co@rGm@|FQxBiAxLMvB_BrQoCtZSrBIJ[\\URwCa@eGaA\"},"
				+ "\"summary\":\"Leof. Alexandras\","
				+ "\"warnings\":[],"
				+ "\"waypoint_order\":[]}"
				+ "]\"";
		*/
		
        String liosiaPostalCode = "13341"; // zip code for Liosa
        String zografouPostalCode = "15772"; // zip code for Zografou
        String thessalonikiPostalCode = "54351"; // zip code for Thessaloniki
		String auebPostalCode = "10434"; // zip code for AUEB

        String fromLiosiaToAueb = auebPostalCode + "_" + liosiaPostalCode;
		int LiosiaToAuebId = Hash(fromLiosiaToAueb, 8);
		System.out.println("AUEB to Liosia id: " + LiosiaToAuebId);
		System.out.println();
		
        String fromZografouToAueb = auebPostalCode + "_" + zografouPostalCode;
        int ZografouToAuebId = Hash(fromZografouToAueb, 8);
		System.out.println("Zografou to AUEB id: " + ZografouToAuebId);
		System.out.println();

        String fromThessalonikiToAUEB = auebPostalCode + "_" + thessalonikiPostalCode;
        int ThessalonikiToAUEBId = Hash(fromThessalonikiToAUEB, 8);
		System.out.println("id: " + ThessalonikiToAUEBId);
		System.out.println();
		
	}

}
