import java.net.*;
import java.io.*;
import java.util.zip.*; // Data compression/decompression streams.

public class FlagDescription {
    public String countryName = "";
    public String abbrev = "";
    public ElementDescription[] elements;
    public int numElements = 0;
    public boolean meaningsLoaded = false;

    // Static vars for managing flagdata cache
    public static FlagDescription flags[] = new FlagDescription[300];
    public static int flagCount = 0;
    public static boolean cacheLoaded = false;

    /*
      // Color table from FOTW:
      final static String fotw_colors[][] = {
      {"Very light red",     "255,102,102"},    //R--
      {"Light red",          "255, 51, 51"},    //R-
      {"Red",                "255, 0 , 0"},     //R
      {"Dark red",           "204, 0 , 0"},     //R+
      {"Very dark red",      "153, 0 , 0"},     //R++
      {"Very light blue",    "51,204,255"},     //B--
      {"Light blue",         "51,153,255"},     //B-
      {"Blue",               "0 , 0 ,255"},     //B
      {"Dark blue",          "0 , 0 ,204"},     //B+
      {"Very dark blue",     "0 , 0 ,153"},     //B++
      {"Very light green",   "102,255,102"},    //V--
      {"Light green",        "0 ,255, 51"},     //V-
      {"Green",              "0 ,204, 0"},      //V
      {"Dark green",         "0 ,153, 0"},      //V+
      {"Very dark green",    "0 ,102, 0"},      //V++
      {"Very light yellow",  "255,255,204"},    //Y--
      {"Light yellow",       "255,255,153"},    //Y-
      {"Yellow",             "255,255, 0"},     //Y
      {"Dark yellow",        "255,204, 0"},     //Y+
      {"Light orange",       "255,153, 0"},     //O-
      {"Orange",             "255,102, 0"},     //O
      {"Gold",               "255,204, 51"}     //Au
      };
     */

    // Colors
    final static String colors[] = {
        "255,255,255", //	white
        "0,0,0", //	black
        // blue
        "0,35,119", //	"usa australia, uk dark blue"
        "0,49,156", //	"(armenia, cambodia) finland, columbia, thailand, south africa, namibia, Bahamas Barbados medium blue
        "0,0,204", //	medium bright blue
        "0,107,198", //	"light blue  uzbekistan, guatemala, elsalvador, honduras, nicaragua, sweden, greece, estonia"
        "8,123,206", //	azerbaijan, israel light dull blue
        "102,204,255", //	"ukraine, luxembourg, armenia, botswana, san marino light blue"
        // green
        "0,91,24", //	dark green
        "0,102,0", //	medium green, saudi arabia
        "0,104,71", //	mexico dark dull green
        "56,126,41", //	uzbekistan
        "53,150,43", //	"togo, jordan, "     0,128,0=brazil
        "0,153,0", //	medium green
        "0,191,0", //	rwanda green
        // yellow - orange
        "255,255,0", //	"ukraine, lots of countries, brightyellow"
        //"255,214,0",   //	NOW IS 255,204,0.  "benin, south africa, barbados"
        "255,204,0", //	"germany, cyprus, vatican, sweden, lots others, yellow gold"
        "255,153,0", //	"cote divoire, armenia, india orange"
        "255,90,0", //	ireland
        // red - redorange
        "222,57,8", //	uganda rust red-orange
        "255,0,0", //	"austria, lots of countries, bright red"
        "245,30,48", //	"puerto rico, angola, canada, bright red"
        "241,0,67", //	united states red
        "222,33,16", //	"thailand, lots of countries, medium red"
        //"204,51,51",   //	NOW IS 204,0,0   azerbiajan
        "204,0,0", //	bolivia gambia cambodia iraq, UK, mexico
        //"188,2,4",   //	NOW IS 222,33,16   denmark orange red
        "153,0,0" //	latvia georgia dark dull red

    };

    // Keywords in alpha order
    final static String keywords[] = {
        "agriculture",
        "blood",
        "catholic",
        "communism",
        "compassion",
        "courage",
        "energy",
        "equality",
        "freedom",
        "forest",
        "grain",
        "harmony",
        "heaven",
        "hope",
        "innocence",
        "islam",
        "liberty",
        "loyalty",
        "peace",
        "progress",
        "protestant",
        "purity",
        "revolution",
        "royalty",
        "sacrifice",
        "suffering",
        "sand",
        "sea",
        "sky",
        "snow",
        "sun",
        "strength",
        "unity",
        "valour",
        "virtue",
        "wealth"
    };

    // Countries in alpha order (must match with countryAbbrevs[] below)
    final static String countryNames[] = {
        "Afghanistan",
        "Albania",
        "Algeria",
        "Andorra",
        "Angola",
        "Argentina",
        "Armenia",
        "Austria",
        "Australia",
        "Azerbaijan",
        "Bahamas",
        "Bangladesh",
        "Barbados",
        "Benin",
        "Belgium",
        "Bolivia",
        "Botswana",
        "Brazil",
        "Bulgaria",
        "Cambodia",
        "Cameroon",
        "Canada",
        "Chad",
        "Chile",
        "China",
        "Colombia",
        "Costa Rica",
        "Cote d'Ivoire",
        "Croatia",
        "Cuba",
        "Cyprus",
        "Czech Republic",
        "Denmark",
        "Dominican Republic",
        "Ecuador",
        "El Salvador",
        "Egypt",
        "Estonia",
        "Ethiopia",
        "Finland",
        "France",
        "Gabon",
        "Gambia",
        "Georgia",
        "Germany",
        "Ghana",
        "Greece",
        "Greenland", //
        "Guadaloupe",
        "Guatemala",
        "Guinea",
        "Haiti",
        "Honduras",
        "Hong Kong",
        "Hungary",
        "Iceland",
        "India",
        "Indonesia",
        "Iraq",
        "Ireland",
        "Israel",
        "Italy",
        "Japan",
        "Jordan",
        "Kuwait",
        "Latvia",
        "Laos",
        "Lebanon",
        "Libya",
        "Lithuania",
        "Luxembourg",
        "Malaysia",
        "Mali",
        "Mexico",
        "Monaco",
        "Morocco",
        "Namibia",
        "Netherlands",
        "Nicaragua",
        "Nigeria",
        "North Korea", //
        "Norway", //
        "Pakistan",
        "Palestine",
        "Panama",
        "Paraguay",
        "Peru",
        "Philippines",
        "Poland",
        "Puerto Rico",
        "Romania",
        "Russian Federation",
        "Rwanda",
        "San Marino",
        "Saudi Arabia",
        "Senegal",
        "Singapore",
        "Somalia",
        "South Africa",
        "South Korea",
        "Spain",
        "Sudan",
        "Sweden", //
        "Switzerland",
        "Syria",
        "Thailand",
        "Togo",
        "Uganda",
        "Ukraine",
        "United Kingdom",
        "United States",
        "Uzbekistan",
        "Vatican",
        "Venezuela",
        "Vietnam",
        "Yemen",
        "Yugoslavia",
        "Zimbabwe" //
    };

    // Abbreviations in alpha order (must EXACTLY match countryNames[])
    final static String countryAbbrevs[] = {
        "af",
        "al",
        "dz",
        "ad",
        "ao",
        "ar",
        "am",
        "at",
        "au",
        "az",
        "bs",
        "bd",
        "bb",
        "bj",
        "be",
        "bo",
        "bw",
        "br",
        "bg",
        "kh",
        "cm",
        "ca",
        "td",
        "cl",
        "cn",
        "co",
        "cr",
        "ci",
        "hr",
        "cu",
        "cy",
        "cz",
        "dk",
        "do",
        "ec",
        "sv",
        "eg",
        "ee",
        "et",
        "fi",
        "fr",
        "ga",
        "gm",
        "ge",
        "de",
        "gh",
        "gr",
        "gl", //
        "gp",
        "gt",
        "gn",
        "ht",
        "hn",
        "hk",
        "hu",
        "is",
        "in",
        "id",
        "iq",
        "ie",
        "il",
        "it",
        "jp",
        "jo",
        "kw",
        "lv",
        "la",
        "lb",
        "ly",
        "lt",
        "lu",
        "my",
        "ml",
        "mx",
        "mc",
        "ma",
        "na",
        "nl",
        "ni",
        "ng", // Nigeria
        "kp", // North Korea
        "no", // Norway
        "pk",
        "ps",
        "pa",
        "py",
        "pe",
        "ph",
        "pl",
        "pr",
        "ro",
        "ru",
        "rw",
        "sm",
        "sa",
        "sn",
        "sg",
        "so",
        "za",
        "kr", // South Korea
        "es", // Spain
        "sd",
        "se", //
        "ch",
        "sy",
        "th",
        "tg",
        "ug",
        "ua",
        "gb",
        "us",
        "uz",
        "va",
        "ve",
        "vn",
        "ye",
        "yu",
        "zw" //
    };

    /*
     final static String countries[] = {
     "Afghanistan",
     "Albania",
     "Algeria",
     "American Samoa",
     "Andorra",
     "Angola",
     "Anguilla",
     "Antarctica",
     "Antigua and Barbuda",
     "Argentina",
     "Armenia",
     "Aruba",
     "Australia",
     "Austria",
     "Azerbaijan",
     "Bahamas",
     "Bahrain",
     "Bangladesh",
     "Barbados",
     "Belarus",
     "Belgium",
     "Belize",
     "Benin",
     "Bermuda",
     "Bhutan",
     "Bolivia",
     "Bosnia and Herzegovina",
     "Botswana",
     "Bouvet Island",
     "Brazil",
     "British Indian Ocean Territory",
     "Brunei Darussalam",
     "Bulgaria",
     "Burkina Faso",
     "Burundi",
     "Cambodia",
     "Cameroon",
     "Canada",
     "Cape verde",
     "Cayman Islands",
     "Central African Republic",
     "Chad",
     "Chile",
     "China",
     "Christmas Island",
     "Cocos (Keeling) Islands",
     "Colombia",
     "Comoros",
     "Congo",
     "Congo (Democratic Republic)",
     "Cook Islands",
     "Costa Rica",
     "Cote d'Ivoire",
     "Croatia",
     "Cuba",
     "Cyprus",
     "Czech Republic",
     "Denmark",
     "Djibouti",
     "Dominica",
     "Dominican Republic",
     "East Timor",
     "Ecuador",
     "Egypt",
     "El Salvador",
     "Equatorial Guinea",
     "Eritrea",
     "Estonia",
     "Ethiopia",
     "Falkland Islands (Malvinas)",
     "Faroe Islands",
     "Fiji",
     "Finland",
     "France",
     "France Metropolitan",
     "French Guiana",
     "French Polynesia",
     "French Southern Territories",
     "Gabon",
     "Gambia",
     "Georgia",
     "Germany",
     "Ghana",
     "Gibraltar",
     "Greece",
     "Greenland",
     "Grenada",
     "Guadeloupe",
     "Guam",
     "Guatemala",
     "Guinea",
     "Guinea-Bissau",
     "Guyana",
     "Haiti",
     "Heard and McDonald Islands",
     "Honduras",
     "Hong kong",
     "Hungary",
     "Iceland",
     "India",
     "Indonesia",
     "Iran (Islamic Republic)",
     "Iraq",
     "Ireland",
     "Israel",
     "Italy",
     "Jamaica",
     "Japan",
     "Jordan",
     "Kazakhstan",
     "Kenya",
     "Kiribati",
     "Korea (Democratic People's Republic)",
     "Korea (Republic)",
     "Kuwait",
     "Kyrgyzstan",
     "Lao People's Democratic Republic",
     "Latvia",
     "Lebanon",
     "Lesotho",
     "Liberia",
     "Libyan Arab Jamahiriya",
     "Liechtenstein",
     "Lithuania",
     "Luxembourg",
     "Macau",
     "Macedonia (Former Yugoslav Republic)",
     "Madagascar",
     "Malawi",
     "Malaysia",
     "Maldives",
     "Mali",
     "Malta",
     "Marshall Islands",
     "Martinique",
     "Mauritania",
     "Mauritius",
     "Mayotte",
     "Mexico",
     "Micronesia (Federated States)",
     "Moldova (Republic)",
     "Monaco",
     "Mongolia",
     "Montserrat",
     "Morocco",
     "Mozambique",
     "Myanmar",
     "Namibia",
     "Nauru",
     "Nepal",
     "Netherlands",
     "Netherlands Antilles",
     "New Caledonia",
     "New Zealand",
     "Nicaragua",
     "Niger",
     "Nigeria",
     "Niue",
     "Norfolk Island",
     "Northern Mariana Islands",
     "Norway",
     "Oman",
     "Pakistan",
     "Palau",
     "Palestinian Authority",
     "Panama",
     "Papua new guinea",
     "Paraguay",
     "Peru",
     "Philippines",
     "Pitcairn",
     "Poland",
     "Portugal",
     "Puerto Rico",
     "Qatar",
     "Reunion",
     "Romania",
     "Russian Federation",
     "Rwanda",
     "Saint Kitts and Nevis",
     "Saint Lucia",
     "Saint Vincent and the Grenadines",
     "Samoa",
     "San Marino",
     "Sao Tome and Principe",
     "Saudi Arabia",
     "Senegal",
     "Seychelles",
     "Sierra Leone",
     "Singapore",
     "Slovakia (Slovak Republic)",
     "Slovenia",
     "Solomon Islands",
     "Somalia",
     "South Africa",
     "South Georgia and the South Sandwich Islands",
     "Spain",
     "Sri Lanka",
     "St. Helena",
     "St. Pierre and Miquelon",
     "Sudan",
     "Suriname",
     "Svalbard and Jan Mayen Islands",
     "Swaziland",
     "Sweden",
     "Switzerland",
     "Syrian Arab Republic",
     "Taiwan, Republic of China",
     "Tajikistan",
     "Tanzania (United Republic)",
     "Thailand",
     "Togo",
     "Tokelau",
     "Tonga",
     "Trinidad and Tobago",
     "Tunisia",
     "Turkey",
     "Turkmenistan",
     "Turks and Caicos Islands",
     "Tuvalu",
     "Uganda",
     "Ukraine",
     "United Arab Emirates",
     "United Kingdom",
     "United States",
     "United States Minor Outlying Islands",
     "Uruguay",
     "Uzbekistan",
     "Vanuatu",
     "Vatican City State (Holy See)",
     "Venezuela",
     "Viet Nam",
     "Virgin Islands (British)",
     "Virgin Islands (U.S.)",
     "Wallis and Futuna Islands",
     "Western Sahara",
     "Yemen",
     "Yugoslavia",
     "Zambia",
     "Zimbabwe"
     };
     */

    // Creates a blank description object
    //
    public FlagDescription() {
        elements = new ElementDescription[30]; //!!! for now
    }

    ///////////////////////////////////////
    // STATIC METHODS work on flag description array
    //

    // Return index of country name in countryNames[]
    // This is also the index of that country's flag in flags[]
    // Param may be country name or abbrev, so look
    // in abbrevs first, then country names (faster).
    //
    public static int getCountryId(String countryname) {
        for (int idx = 0; idx < countryAbbrevs.length; idx++) {
            if (countryAbbrevs[idx].equals(countryname)) {
                return idx;
            }
        }
        for (int idx = 0; idx < countryNames.length; idx++) {
            if (countryNames[idx].equals(countryname)) {
                return idx;
            }
        }
        return -1;
    }

    // Get country name from abbrev
    //
    public static String getCountryName(String abbrev) {
        int idx = getCountryId(abbrev);
        if (idx >= 0 && idx < countryNames.length) {
            //System.out.println("Found flag " + countryname + " idx=" + idx);
            return countryNames[idx];
        }
        return "notfound(" + abbrev + ")";
    }

    // Get abbrev  from country name
    //
    public static String getAbbrev(String countryname) {
        int idx = getCountryId(countryname);
        if (idx >= 0 && idx < countryAbbrevs.length) {
            //System.out.println("Found flag " + countryname + " idx=" + idx);
            return countryAbbrevs[idx];
        }
        return null;
    }

    // Get flag description from cache
    //
    public static FlagDescription getFlagDesc(String countryname) {
        // Get flag description from flags array
        // flags[] and countryNames[] have the same index
        int idx = getCountryId(countryname);
        if (idx >= 0 && flags[idx] != null) {
            //System.out.println("Found flag " + countryname + " idx=" + idx);
            return flags[idx];
        }
        return null;
    }

    // Find flags with the given shape.
    // Return an array of flag ids.
    //
    public static int[] findShape(String shapetype) {
        int[] answerIds = new int[flags.length];
        int[] retIds = null;
        int count = 0;
        for (int idx = 0; idx < flags.length; idx++) {
            //System.out.println("flagdesc " + idx + " = " + flags[idx].countryName);
            if (flags[idx] != null) {
                if (flags[idx].getElementWithShape(shapetype) != null) {
                    //System.out.println("Found flag " +flags[idx].countryName+ " with shape " + shapetype + " idx=" + idx);
                    answerIds[count] = idx;
                    count++;
                }
            }
        }
        if (count > 0) {
            retIds = new int[count];
            for (int i = 0; i < count; i++) {
                retIds[i] = answerIds[i];
            }
        }
        return retIds;
    }

    // Find flags with the given keyword.
    // Return an array of flag ids.
    //
    public static int[] findKeyword(String keyword) {
        int[] answerIds = new int[flags.length];
        int[] retIds = null;
        int count = 0;
        for (int idx = 0; idx < flags.length; idx++) {
            //System.out.println("flagdesc " + idx + " = " + flags[idx].countryName);
            if (flags[idx] != null) {
                if (flags[idx].getElementWithKeyword(keyword) != null) {
                    //System.out.println("Found flag " +flags[idx].countryName+ " with keyword " + keyword + " idx=" + idx);
                    answerIds[count] = idx;
                    count++;
                }
            }
        }
        if (count > 0) {
            retIds = new int[count];
            for (int i = 0; i < count; i++) {
                retIds[i] = answerIds[i];
            }
        }
        return retIds;
    }

    // Find flags with the given color.
    // Return an array of flag ids.
    //
    public static int[] findColor(String RGBstring) {
        int[] answerIds = new int[flags.length];
        int[] retIds = null;
        int count = 0;
        for (int idx = 0; idx < flags.length; idx++) {
            //System.out.println("flagdesc " + idx + " = " + flags[idx].countryName);
            if (flags[idx] != null) {
                if (flags[idx].getElementWithColor(RGBstring) != null) {
                    //System.out.println("Found flag " +flags[idx].countryName+ " with color " + RGBstring + " idx=" + idx);
                    answerIds[count] = idx;
                    count++;
                }
            }
        }
        if (count > 0) {
            retIds = new int[count];
            for (int i = 0; i < count; i++) {
                retIds[i] = answerIds[i];
            }
        }
        return retIds;
    }

    // Set meaning into flag description.
    // Meanings are loaded later in app, on demand.
    //
    public static boolean setMeaning(int flagId, String elementName, String meaning) {
        ElementDescription eld = flags[flagId].getElement(elementName);
        if (eld != null) {
            //System.out.println("Found flag " +flags[idx].countryName+ " with shape " + shapetype + " idx=" + idx);
            eld.meaning = meaning;
            return true;
        }
        return false;
    }

    // END STATIC METHODS
    ///////////////////////////////////////

    public ElementDescription getElementWithColor(String RGBstring) {
        for (int eid = 0; eid < numElements; eid++) {
            if (elements[eid].RGBstring.equals(RGBstring)) {
                return elements[eid];
            }
        }
        return null;
    }

    public ElementDescription getElementWithShape(String shapeType) {
        for (int eid = 0; eid < numElements; eid++) {
            if (elements[eid].type.equals(shapeType)) {
                return elements[eid];
            }
        }
        return null;
    }

    public ElementDescription getElementWithKeyword(String keyword) {
        for (int eid = 0; eid < numElements; eid++) {
            if (elements[eid].keywords.indexOf(keyword) >= 0) {
                return elements[eid];
            }
        }
        return null;
    }

    public ElementDescription getElement(String elementName) {
        for (int eid = 0; eid < numElements; eid++) {
            if (elements[eid].elementName.equals(elementName)) {
                return elements[eid];
            }
        }
        return null;
    }

    public ElementDescription getElement(int elementIdx) {
        if (elementIdx >= 0 && elementIdx < numElements) {
            return elements[elementIdx];
        }
        return null;
    }

    public int addElement(ElementDescription ed) {
        elements[numElements] = ed;
        numElements++;
        return numElements;
    }

    public int getNumElements() {
        return numElements;
    }

    // Load flag descriptions into cache
    //
    public static boolean preLoadFlags(URL flagdataURL) {
        System.out.println("FlagDescription.preLoadFlags(): load flag data from url=" + flagdataURL);
        try {
            byte bb;
            int counter = 0;
            String line;

            // Open the GZIP flag parts file
            URL localThisUrl = flagdataURL;
            InputStream urlIn = localThisUrl.openStream();
            GZIPInputStream in = new GZIPInputStream(urlIn); // Uncompress

            // Read the results
            InputStreamReader inreader = new InputStreamReader(in);
            BufferedReader inbuffer = new BufferedReader(inreader);
            ElementDescription ed = null;
            FlagDescription fd = null;
            String currCountry = "";

            while ( (line = inbuffer.readLine()) != null) {
                if ( (ed = breakoutline(line)) != null) {
                    if (ed.countryName.equals(currCountry)) {
// DEBUG load flag data
//              System.out.println("loadFlagFromUrl: add element " +ed.elementName+ " to flag " + ed.countryName);
                        fd.addElement(ed);
                    }
                    else {
                        // found a new country in data file
                        // start new flag
                        int idx = getCountryId(ed.countryName);
                        if (idx < 0) {
                            System.out.println("ERROR: Can't find country: " + ed.countryName);
                            break;
                        }
// DEBUG load flag data
//              System.out.println("loadFlagFromUrl: load flag " + ed.countryName + " at idx " + idx);
//              System.out.println("loadFlagFromUrl: add element " +ed.elementName+ " to flag " + ed.countryName);
                        fd = new FlagDescription();
                        fd.addElement(ed);
                        fd.countryName = ed.countryName;
                        fd.abbrev = ed.abbrev;
                        currCountry = ed.countryName;
                        flags[idx] = fd;
                    }
                }
            }
            in.close(); // Close the stream.
        }
        catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        }
        catch (EOFException eofe) {
            System.err.println("EOFException: " + eofe.getMessage());
        }
        catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
        cacheLoaded = true;
        return true;
    }

    // breakout line of flag design data from nf_shapedata.txt

    private static ElementDescription breakoutline(String line) {
        String[] tokens = new String[20];
        String[] rgbtokens = new String[4];
        int colorid = 0, x = 0, y = 0, w = 0, h = 0, r = 0, g = 0, b = 0, var1 = 0, var2 = 0, var3 = 0;
        ElementDescription ed;

        if (parseRecord(line, tokens, "\t") == false) {
            return null;
        }

        String countryname = ""; //tokens[0];
        String abbrev = tokens[0];
        String elementname = tokens[1];
        String colorname = ""; //tokens[2];
        String colorrgb = tokens[2];
        //String colorpms = tokens[5];
        String shapetype = tokens[3];
        String keywords = tokens[11];
        String meaning = ""; //tokens[12];
        String polyPoints = null;

        // chop off "" and '' around values
        if (colorrgb == null || colorrgb.equals("")) {
            //colorrgb = "\"\"";
            colorrgb = "";
        }
        colorrgb = colorrgb.replace('"', ' ');
        colorrgb = colorrgb.replace('\'', ' ');
        colorrgb = colorrgb.trim();
        //colorrgb = colorrgb.substring(1,colorrgb.length()-1);
        if (parseRecord(colorrgb, rgbtokens, ",") == false) {
            System.out.println("FlagDesc: Can't read RGB values for " + countryname + " " + elementname);
        }
        //System.out.println("rgb tokens1,2,3 = " + rgbtokens[0] + " "  + rgbtokens[1]+ " "  + rgbtokens[2]);

        // grab id, x,y and char
        try {
            x = Integer.parseInt(tokens[4]);
            y = Integer.parseInt(tokens[5]);
            w = Integer.parseInt(tokens[6]);
            h = Integer.parseInt(tokens[7]);
            r = Integer.parseInt(rgbtokens[0]);
            g = Integer.parseInt(rgbtokens[1]);
            b = Integer.parseInt(rgbtokens[2]);
            if (shapetype.equals("poly")) {
                polyPoints = tokens[8];
            }
            else {
                var1 = Integer.parseInt(tokens[8]);
            }
            var2 = Integer.parseInt(tokens[9]);
            var3 = Integer.parseInt(tokens[10]);
        }
        catch (NumberFormatException e) {
            System.out.println("breakoutline():" + e);
            System.out.println("line: 0=" + tokens[0] + " 1=" + tokens[1] + " 2=" + tokens[2]);
            return null;
        }
        catch (Exception e) {
            System.out.println("breakoutline():" + e);
            System.out.println("line: 0=" + tokens[0] + " 1=" + tokens[1] + " 2=" + tokens[2]);
            return null;
        }

        countryname = FlagDescription.getCountryName(abbrev);
        ed = new ElementDescription(
            countryname, abbrev, elementname,
            x, y, w, h, shapetype, colorname, r, g, b, var1, var2, var3, keywords);
        ed.RGBstring = colorrgb;
        // if shape is a polygon, save points list
        if (polyPoints != null) {
            String strPoints[] = new String[50];
            int intPoints[] = new int[50];
            int numPoints = 0;
            // remove '' and "" around numbers
            polyPoints = polyPoints.replace('"', ' ');
            polyPoints = polyPoints.replace('\'', ' ');
            polyPoints = polyPoints.trim();
            if (parseRecord(polyPoints, strPoints, ",")) {
                for (int i = 0; i < strPoints.length && strPoints[i] != null; i += 2) {
                    try {
                        intPoints[i] = Integer.parseInt(strPoints[i]);
                        intPoints[i + 1] = Integer.parseInt(strPoints[i + 1]);
//                System.out.println("\t x=" + intPoints[i] + " y="+intPoints[i+1]);
                    }
                    catch (Exception e) {
                        System.out.println("breakoutline readPolyPoints:" + e);
                        return null;
                    }
                    numPoints++;
                }
                ed.setPolyPoints(intPoints, numPoints);
            }
        }
        return ed;
    }

    public static boolean parseRecord(String textValues, String[] token, String delim) {
        try {
            boolean endOfStr = false;
            int tcount = 0;
            int tokenPos = 0;
            int nextComma = 0;
            if (textValues.length() < 5) {
                // blank or malformed string
                return false;
            }
            // Parse values from string
            for (tokenPos = 0;
                 endOfStr == false;
                 tokenPos = textValues.indexOf(delim, tokenPos) + 1) {
                nextComma = textValues.indexOf(delim, tokenPos);
                if (nextComma == -1) {
                    nextComma = textValues.length();
                    endOfStr = true;
                }
                token[tcount] = textValues.substring(tokenPos, nextComma);
                tcount++;
            }

            return true;
        }
        catch (Exception e) {
            System.out.println("p2p.parseRecord(): " + e);
            return false;
        }
    }

}