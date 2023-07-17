package it.unibz.inf.ontop.owlapi;


import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;

/**
 *   A few basic tests to understand how to handle basic temporal queries
 */
public class MoreComplexTemporalQueries extends AbstractOWLAPITest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";
    private static final String LEFT_JOIN_NOT_OPTIMIZED_MSG = "The left join is still present in the output query";


    @BeforeClass
    public static void setUP() throws Exception {
        initOBDA("/test/temporal/complex/temporal_create.sql",
                "/test/temporal/complex/temporal_test.obda",
                "/test/temporal/complex/temporal_test.owl",
                "/test/temporal/complex/temporal_test.properties");
    }

    @AfterClass
    public static void  tearDown() throws Exception {
        release();
    }


    @Test
    public void testNonTemporalJoin() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "SELECT distinct ?x \n" +
                "WHERE {\n" +
                "  Graph ?y1 { ?x a :Employee .\n" +
                "  ?x :dept ?z . \n" +
                "  }  \n" +
                "  Graph ?y2 { \n" +
                "  ?z :location 'madrid' . \n" +
                "  }  \n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "x", ImmutableList.of(
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#employee/e1>"));

//        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
//        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }


    private String temporalSparql(String input) throws  Exception{
        String IRI = "<[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*>" ;
        String VAR = "\\?[a-zA-Z0-9]+" ;
        String CONST = "[a-zA-Z0-9]*:[a-zA-Z]+" ;
        String QuotedCONST = "(?:'|[^'])*";
        String matchedTerm = "("+VAR + "|" + IRI + "|" + CONST + "|"+ QuotedCONST + ")";
        String triple = "^(\\s?" + matchedTerm + "\\s?" + matchedTerm + "\\s?" +matchedTerm + "\\s?[.])";

        Pattern pSimple = Pattern.compile(triple + "\\s?@\\s?("+VAR+")\\s?\\Z");
        Pattern pComplex = Pattern.compile(triple + "\\s?@\\s?("+VAR+")\\s?("+VAR+")\\s?\\Z");
        Pattern overlapPoints = Pattern.compile("\\bOverlapPoints\\(\\s?("+VAR+")\\s?,\\s?("+VAR+")\\s?,\\s?("+VAR+")\\s?,\\s?("+VAR+")\\s?\\)\\s?");
        Pattern overlapInterval = Pattern.compile("\\bOverlap\\(\\s?("+VAR+")\\s?,\\s?("+VAR+")\\s?\\)\\s?");

        String[] lines = input.split(System.lineSeparator());



        ArrayList<String> newLines = new ArrayList<>();


        boolean neverMatched = true;

        for (String line : lines) {

            Matcher mSimple = pSimple.matcher(line);
            Matcher mComplex = pComplex.matcher(line);
            Matcher mOverlapPoints = overlapPoints.matcher(line);
            Matcher mOverlapInterval = overlapInterval.matcher(line);


            if (mSimple.find(0)){
                neverMatched = false;
                if (mSimple.groupCount() != 5) {
                    System.out.println("Found groups:\n");

                    for (int g = 0; g <= mSimple.groupCount(); g++){
                        System.out.println(g +" "+ mSimple.group(g));
                    }

                    throw new Exception("could not match entire temporal triple expresssion"  + mSimple.groupCount());
                }

                String tripleMatched = mSimple.group(1);
                String timeVar = mSimple.group(5);

                // for each interval, we introduce the interval graph object, and start and end dates, and
                // start and end points, needed in the W3C TIME ontology to connect the values with the interval

                newLines.add("Graph " + timeVar + "Interval {");
                newLines.add(tripleMatched);
                newLines.add("}");
                newLines.add(timeVar + "Interval time:hasInterval " +timeVar + " .");
                newLines.add(timeVar + " time:hasBeginning " + timeVar +"StartPoint ." );
                newLines.add(timeVar + "StartPoint time:inXSDDateTimeStamp " + timeVar +"Start ." );
                newLines.add(timeVar + " time:hasEnd " + timeVar +"EndPoint ." );
                newLines.add(timeVar + "EndPoint time:inXSDDateTimeStamp " + timeVar +"End ." );


            } else if (mComplex.find(0)) {
                neverMatched = false;
                if (mComplex.groupCount() != 6) {
                    System.out.println("Found groups:\n");

                    for (int g = 0; g <= mComplex.groupCount(); g++){
                        System.out.println(g +" "+ mComplex.group(g));
                    }

                    throw new Exception("could not match entire temporal triple expresssion"  + mComplex.groupCount());
                }

                String tripleMatched = mComplex.group(1);
                String StartVar = mComplex.group(5);
                String EndVar = mComplex.group(6);

                String timeVar = StartVar + EndVar.replaceAll("\\?","");

                // for each interval, we introduce the interval graph object, and start and end dates, and
                // start and end points, needed in the W3C TIME ontology to connect the values with the interval

                newLines.add("Graph " + timeVar + "Interval {");
                newLines.add(tripleMatched);
                newLines.add("}");
                newLines.add(timeVar + "Interval time:hasTime " +timeVar + " .");
                newLines.add(timeVar + " time:hasBeginning " + timeVar +"StartPoint ." );
                newLines.add(timeVar + "StartPoint time:inXSDDateTimeStamp " + StartVar +" ." );
                newLines.add(timeVar + " time:hasEnd " + timeVar +"EndPoint ." );
                newLines.add(timeVar + "EndPoint time:inXSDDateTimeStamp " + EndVar +" ." );

            } else if (mOverlapPoints.find(0)) {
                neverMatched = false;
                if (mOverlapPoints.groupCount() != 4) {
                    System.out.println("Found groups:\n");

                    for (int g = 0; g <= mOverlapPoints.groupCount(); g++){
                        System.out.println(g +" "+ mOverlapPoints.group(g));
                    }

                    throw new Exception("could not match entire temporal triple expresssion"  + mOverlapPoints.groupCount());
                }


                String firstStart = mOverlapPoints.group(1);
                String firstEnd = mOverlapPoints.group(2);
                String secStart = mOverlapPoints.group(3);
                String secEnd = mOverlapPoints.group(4);

                newLines.add(" BIND (");
                newLines.add("    IF("+secStart +" >= " +firstStart +", xsd:date(" +secStart+"), xsd:date("+firstStart+"))");
                newLines.add("  AS ?last");
                newLines.add(") ");
                newLines.add(" BIND (");
                newLines.add("    IF("+secEnd+" >= "+firstEnd+",xsd:date("+firstEnd+"),xsd:date("+secEnd+"))");
                newLines.add("  AS ?first");
                newLines.add(")");
                newLines.add("FILTER (?last < ?first) ");

            } else if (mOverlapInterval.find(0)) {
                neverMatched = false;
                if (mOverlapInterval.groupCount() != 2) {
                    System.out.println("Found groups:\n");

                    for (int g = 0; g <= mOverlapInterval.groupCount(); g++){
                        System.out.println(g +" "+ mOverlapInterval.group(g));
                    }

                    throw new Exception("could not match entire temporal triple expresssion"  + mOverlapInterval.groupCount());
                }


                String first = mOverlapInterval.group(1);
                String second = mOverlapInterval.group(2);

                String firstStart = first + "Start";
                String firstEnd = first +"End";
                String secStart = second + "Start";
                String secEnd = second + "End";

                newLines.add(" BIND (");
                newLines.add("    IF("+secStart +" >= " +firstStart +", xsd:date(" +secStart+"), xsd:date("+firstStart+"))");
                newLines.add("  AS ?last");
                newLines.add(") ");
                newLines.add(" BIND (");
                newLines.add("    IF("+secEnd+" >= "+firstEnd+",xsd:date("+firstEnd+"),xsd:date("+secEnd+"))");
                newLines.add("  AS ?first");
                newLines.add(")");
                newLines.add("FILTER (?last < ?first) ");

            }
            else {
                newLines.add(line);
            }


        }

        if (neverMatched) {
            System.out.println("Never matched anything.");
        }


        return String.join("\n ",newLines);
    }

    @Test
    public void testTemporalJoinEndPoints() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT distinct ?x \n" +
                "WHERE {\n" +
                " ?x :dept ?z . @ ?yStart ?yEnd \n" +
                " ?z :location 'barcelona' . @ ?vStart ?vEnd \n" +
                " OverlapPoints(?yStart, ?yEnd, ?vStart, ?vEnd) \n" +
                "}";

        String temporalQuery = temporalSparql(query);
        System.out.println("Parsed temporal Sparql query:\n "+ temporalQuery);


        String sql = checkReturnedValuesAndReturnSql(temporalQuery, "x", ImmutableList.of(
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#employee/e1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#employee/e2>"));
    }


    @Test
    public void testTemporalJoinInterval() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT distinct ?x \n" +
                "WHERE {\n" +
                " ?x :dept ?z . @ ?y \n" +
                " ?z :location 'barcelona' . @ ?v \n" +
                " Overlap(?y, ?v) \n" +
                "}";

        String temporalQuery = temporalSparql(query);
        System.out.println("Parsed temporal Sparql query:\n "+ temporalQuery);


        String sql = checkReturnedValuesAndReturnSql(temporalQuery, "x", ImmutableList.of(
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#employee/e1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#employee/e2>"));
    }



//    @Test
//    public void testIntervalOutput() throws Exception {
//        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
//                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
//                "SELECT distinct ?i \n" +
//                "WHERE {\n" +
//                "  ?i time:hasTime ?t . ?t time:hasBeginning ?b   \n" +
//                "  Graph ?i { ?p a :Employee .\n" +
//                "   OPTIONAL {\n" +
//                "     ?p :salary ?v\n" +
//                "  }  }  \n" +
//                "}";
//
//        String sql = checkReturnedValuesAndReturnSql(query, "i", ImmutableList.of(
//                "\"60000\"^^xsd:integer",
//                "\"70000\"^^xsd:integer"));
//
//        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
//        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
//    }


    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }
}
