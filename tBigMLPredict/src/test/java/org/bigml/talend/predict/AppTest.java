package org.bigml.talend.predict;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.LocalPredictiveModel;
import org.gabrielebaldassarre.tcomponent.bridge.TalendType;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testBigMLClient() throws Throwable
    {
        BigMLClient bigMLClient = BigMLClient.getInstance("alperte", "d72179b82807642d131dfb39131e4ffdf4b3a9bb", true);
        // Use here the identifier of an model you own
        String mid = "model/54134f2599fca47b5100299b";
        LocalPredictiveModel m =
                new LocalPredictiveModel(BigMLClient.getInstance().getModel(mid));

        assertTrue(true);
    }

    /**
     * Rigourous Test :-)
     */
    public void testBigMLPredictor() throws Throwable
    {
        BigMLPredictor bigMLPredictor = new BigMLPredictor();
        bigMLPredictor.setUser("alperte");
        bigMLPredictor.setApiKey("d72179b82807642d131dfb39131e4ffdf4b3a9bb");
        bigMLPredictor.setDevMode(true);
        bigMLPredictor.setResolveFieldsByName(false);
        bigMLPredictor.setPredictWithConfidence(true);
        bigMLPredictor.setEnsembleId("ensemble/54134f1cc4063748fd002cb9");
        bigMLPredictor.setPredictionColumnTypeId("id_Boolean");
        bigMLPredictor.setRemotePrediction(true);
        // Use here the identifier of an model you own

        bigMLPredictor.initialize();

        Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("Age", "Between 19 and 25"); //motivation
        inputs.put("Civil State", "Divorced"); //occupation
        inputs.put("Studies", "High school completed"); //rent
        inputs.put("Income", "Between 25K and 50K"); //civilState
        inputs.put("Events Attended", 2); //gender
        inputs.put("Occupation", "Entrepreneur / Employer"); //attendedEvents
        inputs.put("Gender", "Man"); //attendedEvents

        bigMLPredictor.predict(inputs);
        Boolean prediction = (Boolean) bigMLPredictor.getPrediction();
        assertTrue(prediction.equals("2700.0 <= totalExpenditure < 3600.0"));

        inputs.clear();
        inputs.put("100005", "De 31 a 40"); //age
        inputs.put("100016", "Férias / lazer"); //motivation
        inputs.put("100008", "Conta própria"); //occupation
        inputs.put("100009", "De 4 a 10 salários mínimos"); //rent
        inputs.put("100006", "Casado"); //civilState
        inputs.put("100004", "Mulher"); //gender
        inputs.put("10000e", 1); //attendedEvents

        bigMLPredictor.predict(inputs);
        prediction = (Boolean) bigMLPredictor.getPrediction();
        assertTrue(prediction.equals("0.0 <= totalExpenditure < 900.0"));

        assertTrue(true);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static String convert(String str)
    {
        StringBuffer ostr = new StringBuffer();

        for(int i=0; i<str.length(); i++)
        {
            char ch = str.charAt(i);

            if ((ch >= 0x0020) && (ch <= 0x007e))	// Does the char need to be converted to unicode?
            {
                ostr.append(ch);					// No.
            } else 									// Yes.
            {
                ostr.append("\\u") ;				// standard unicode format.
                String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);	// Get hex value of the char.
                for(int j=0; j<4-hex.length(); j++)	// Prepend zeros because unicode requires 4 digits
                    ostr.append("0");
                ostr.append(hex.toLowerCase());		// standard unicode format.
                //ostr.append(hex.toLowerCase(Locale.ENGLISH));
            }
        }

        return (new String(ostr));		//Return the stringbuffer cast as a string.

    }

}
