package it.unibz.inf.ontop.owlapi;


import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 *   A few basic tests to understand how to handle basic temporal queries
 */
public class TemporalQueryClass extends AbstractOWLAPITest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";
    private static final String LEFT_JOIN_NOT_OPTIMIZED_MSG = "The left join is still present in the output query";


    @BeforeClass
    public static void setUP() throws Exception {
        initOBDA("/test/temporal/temporal_create.sql",
                "/test/temporal/temporal_test.obda",
                "/test/temporal/temporal_test.owl",
                "/test/temporal/temporal_test.properties");
    }

    @AfterClass
    public static void  tearDown() throws Exception {
        release();
    }

    @Test
    public void testBasic() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "SELECT distinct ?v\n" +
                "WHERE {\n" +
                "  Graph ?i { ?p a :Employee .\n" +
                "   OPTIONAL {\n" +
                "     ?p :salary ?v\n" +
                "  }  }  \n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"60000\"^^xsd:integer",
                "\"70000\"^^xsd:integer"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testIntervalOutput() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "SELECT distinct ?i \n" +
                "WHERE {\n" +
                "  ?i time:hasTime ?t . ?t time:hasBeginning ?b   \n" +
                "  Graph ?i { ?p a :Employee .\n" +
                "   OPTIONAL {\n" +
                "     ?p :salary ?v\n" +
                "  }  }  \n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "i", ImmutableList.of(
                "\"60000\"^^xsd:integer",
                "\"70000\"^^xsd:integer"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }


    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }
}
