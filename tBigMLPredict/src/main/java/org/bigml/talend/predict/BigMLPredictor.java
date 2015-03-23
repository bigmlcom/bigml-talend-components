package org.bigml.talend.predict;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.InputDataParseException;
import org.bigml.binding.LocalEnsemble;
import org.bigml.binding.LocalPredictiveModel;
import org.bigml.binding.resources.Model;
import org.bigml.binding.resources.Prediction;
import org.bigml.binding.utils.Utils;
import org.gabrielebaldassarre.tcomponent.bridge.TalendType;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * A Talend component to allow predictions using BigML.com
 */
public class BigMLPredictor {

    // Logging
    Logger logger = LoggerFactory.getLogger(BigMLPredictor.class);

    /**
     * Name of the bigML user account
     */
    protected String user;

    /**
     * The ApiKey used to connect to BigML service
     */
    protected String apiKey;

    /**
     * If we are in development mode or not
     */
    protected boolean devMode = false;

    /**
     * The ID of the model we are going to use to make predictions
     */
    protected String modelId;

    /**
     * The ID of the ensemble we are going to use to make predictions
     */
    protected String ensembleId;

    /**
     * Decides if we are going to look the fields inside the model by its label.
     *
     * If its set to false, the internal code of the field will be used.
     */
    protected boolean resolveFieldsByName = true;

    /**
     * If we use the confidence field of the model. As it provides a measure of how certain
     * the model is of the prediction
     */
    protected boolean predictWithConfidence = true;

    /**
     * The type id of the column that will hold the prediction in Talend (id_String, etc)
     */
    protected String predictionColumnTypeId;

    /**
     * The reference to the BigMLClient instance required to make calls to the BigML service
     */
    protected BigMLClient bigMLClient;


    /**
     * The BigML model
     */
    protected JSONObject model;

    /**
     * The BigML ensemble
     */
    protected JSONObject ensemble;

    /**
     * This is a remote prediction, that is a prediction made by BigML and
     * it will consume BigML credits (~0.01)
     */
    protected boolean remotePrediction = true;

    /**
     * The BigML model to allow local predictions
     */
    LocalPredictiveModel predictiveModel;

    /**
     * The BigML ensemble to allow local predictions
     */
    LocalEnsemble predictiveEnsemble;

    /**
     * The last predicted value
     */
    protected Object prediction;

    /**
     * The confidence of the last prediction
     */
    protected Double predictionConfidence;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean getDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        if( modelId != null && modelId.trim().length() > 0 && !modelId.startsWith("model/")) {
            modelId = "model/" + modelId;
        }

        this.modelId = modelId;
    }

    public String getEnsembleId() {
        return ensembleId;
    }

    public void setEnsembleId(String ensembleId) {
        if( ensembleId != null && ensembleId.trim().length() > 0 && !ensembleId.startsWith("ensemble/")) {
            ensembleId = "ensemble/" + ensembleId;
        }

        this.ensembleId = ensembleId;
    }

    public boolean getResolveFieldsByName() {
        return resolveFieldsByName;
    }

    public void setResolveFieldsByName(boolean resolveFieldsByName) {
        this.resolveFieldsByName = resolveFieldsByName;
    }

    public boolean getPredictWithConfidence() {
        return predictWithConfidence;
    }

    public void setPredictWithConfidence(boolean predictWithConfidence) {
        this.predictWithConfidence = predictWithConfidence;
    }

    public String getPredictionColumnTypeId() {
        return predictionColumnTypeId;
    }

    public void setPredictionColumnTypeId(String predictionColumnTypeId) {
        this.predictionColumnTypeId = predictionColumnTypeId;
    }

    public boolean isRemotePrediction() {
        return remotePrediction;
    }

    public void setRemotePrediction(boolean remotePrediction) {
        this.remotePrediction = remotePrediction;
    }

    public String getPredictionCast() {
        if( predictionColumnTypeId == null ) {
            throw new IllegalStateException("The prediction column must be informed!");
        }

        TalendType predictionColumnType = TalendType.getInstanceFromTalendId(predictionColumnTypeId);

        switch (predictionColumnType) {
            case BIGDECIMAL:
            case BOOLEAN:
            case DOUBLE:
            case FLOAT:
            case INTEGER:
            case LONG:
            case STRING:
                return predictionColumnType.getTypeString();

            default:
                throw new IllegalArgumentException("Unsupported Taled Type: " + predictionColumnType.getTypeString());
        }
    }

    public Object getPrediction() {
        return prediction;
    }

    public Double getPredictionConfidence() {
        return predictionConfidence;
    }

    public void predict(Map<String, Object> rowInputs) throws Exception {
        if( predictionColumnTypeId == null ) {
            throw new IllegalStateException("The prediction column must be informed!");
        }

        try {
            Iterator<String> itrInputFields = new HashSet<String>(rowInputs.keySet()).iterator();
            while (itrInputFields.hasNext()) {
                String fieldName = itrInputFields.next();
                if( fieldName.contains("_") ) {
                    rowInputs.put(fieldName.replaceAll("_"," "), rowInputs.get(fieldName));
                }
            }

            if( !remotePrediction && (null != predictiveModel || null != predictiveEnsemble)  ) {
                if( predictiveModel != null ) {
                    HashMap<Object, Object> predictionResponse = predictiveModel.predictWithMap(rowInputs, resolveFieldsByName, predictWithConfidence);
                    prediction = predictionResponse.get("prediction");
                    predictionConfidence = (Double) predictionResponse.get("confidence");

                } else {
                    int plurality = 0;
                    Map<Object, Object> predictionResponse = predictiveEnsemble.predictWithMap(rowInputs, resolveFieldsByName, plurality, predictWithConfidence);
                    prediction = predictionResponse.get("prediction");
                    predictionConfidence = (Double) predictionResponse.get("confidence");
                }
            } else {
                // This is a remote prediction, that is a prediction made by BigML and
                // it will consume BigML credits (~0.01)
                JSONObject inputData = new JSONObject(rowInputs);
                JSONObject args = new JSONObject();
                // args.put("locale", "pt_BR");
                JSONObject remotePrediction = null;

                logger.debug("Input fields: " + inputData.toJSONString());

                if( null != modelId && modelId.trim().length() > 0 ) {
                    remotePrediction = bigMLClient.createPrediction(
                            (String) model.get("resource"), inputData, resolveFieldsByName, args, null, null);
                } else {
                    remotePrediction = bigMLClient.createPrediction(ensembleId, inputData, resolveFieldsByName, args,
                            null, null);
                }

                if( remotePrediction != null && remotePrediction.get("resource") != null) {
                    logger.debug("Prediction created...");
                    String predictionId = (String) remotePrediction.get("resource");
                    while (!bigMLClient.predictionIsReady(remotePrediction)) {
                        try {
                            logger.debug("Prediction not ready...waiting 1s and starting checking again.");
                            remotePrediction = bigMLClient.getPrediction(predictionId);
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.err
                                    .println("Something wen't wrong while checking prediction status");
                            e.printStackTrace();
                        }
                    }
                    predictionConfidence = (Double) Utils.getJSONObject(
                            remotePrediction, "object.confidence");
                    prediction = Utils.getJSONObject(
                            remotePrediction, "object.output");
                } else {
                    logger.error("It wasn't possible to make the prediction for the following input: " + inputData.toJSONString());
                    prediction = null;
                    predictionConfidence = null;
                }
            }

            TalendType predictionColumnType = TalendType.getInstanceFromTalendId(predictionColumnTypeId);

            if( prediction != null ) {
                switch (predictionColumnType) {
                    case STRING:
                        prediction = prediction.toString();
                        break;

                    case INTEGER: {
                        if( prediction instanceof Number ) {
                            prediction = ((Number) prediction).intValue();
                        } else {
                            prediction = Integer.parseInt(prediction.toString());
                        }
                    }
                    break;

                    case LONG: {
                        if( prediction instanceof Number ) {
                            prediction = ((Number) prediction).longValue();
                        } else {
                            prediction = Long.parseLong(prediction.toString());
                        }
                    }
                    break;

                    case FLOAT: {
                        if( prediction instanceof Number ) {
                            prediction = ((Number) prediction).floatValue();
                        } else {
                            prediction = Float.parseFloat(prediction.toString());
                        }
                    }
                    break;

                    case DOUBLE: {
                        if( prediction instanceof Number ) {
                            prediction = ((Number) prediction).doubleValue();
                        } else {
                            prediction = Double.parseDouble(prediction.toString());
                        }
                    }
                    break;

                    case BIGDECIMAL: {
                        if( prediction instanceof Number ) {
                            prediction = new BigDecimal(((Number) prediction).doubleValue());
                        } else {
                            prediction = BigDecimal.valueOf(Double.parseDouble(prediction.toString()));
                        }
                    }
                    break;

                    case BOOLEAN: {
                        if( !(prediction instanceof Boolean) ) {
                            prediction = toBooleanObject(prediction.toString().toLowerCase().trim());
                        }

                    }
                    break;

                }
            }
        } catch (InputDataParseException e) {
            StringBuilder errMsg = new StringBuilder("It was unable to predict using the informed inputs: ");
            int fieldIndex=0;
            for (String fieldName : rowInputs.keySet()) {
                if( fieldIndex > 0) {
                    errMsg.append(", ");
                }
                errMsg.append("(").append(fieldName).append(":").append(rowInputs.get(fieldName)).append(")");
                fieldIndex++;
            }
            throw new IllegalStateException(errMsg.toString(), e);
        }
    }

    /**
     * This method is responsible for the initialization of the BigMLClient instance
     *
     * @throws Exception the service will thrown this exception if we were unable to connect the BigML service
     */
    public void initialize() throws Exception {
        bigMLClient = BigMLClient.getInstance(user, apiKey, devMode);

        if( (null == modelId || modelId.trim().length() == 0) && (null == ensembleId || ensembleId.trim().length() == 0)) {
            throw new IllegalStateException("At least a model or an ensemble id must be informed.");
        }

        try {
            // We are using and ensemble instead a model
            if( null != modelId && modelId.trim().length() > 0 ) {
                model = bigMLClient.getModel(modelId, user, apiKey);
                predictiveModel = new LocalPredictiveModel(model);
            } else {
                ensemble = bigMLClient.getEnsemble(ensembleId);
                predictiveEnsemble = new LocalEnsemble(ensemble);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("It was unable to obtain the model with id [" + modelId + "]", e);
        }
    }

    public static Boolean toBooleanObject(final String str) {
        // Previously used equalsIgnoreCase, which was fast for interned 'true'.
        // Non interned 'true' matched 15 times slower.
        //
        // Optimisation provides same performance as before for interned 'true'.
        // Similar performance for null, 'false', and other strings not length 2/3/4.
        // 'true'/'TRUE' match 4 times slower, 'tRUE'/'True' 7 times slower.
        if (str == "true") {
            return Boolean.TRUE;
        }
        if (str == null) {
            return null;
        }
        switch (str.length()) {
            case 1: {
                final char ch0 = str.charAt(0);
                if (ch0 == 'y' || ch0 == 'Y' ||
                        ch0 == 't' || ch0 == 'T') {
                    return Boolean.TRUE;
                }
                if (ch0 == 'n' || ch0 == 'N' ||
                        ch0 == 'f' || ch0 == 'F') {
                    return Boolean.FALSE;
                }
                break;
            }
            case 2: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                if ((ch0 == 'o' || ch0 == 'O') &&
                        (ch1 == 'n' || ch1 == 'N') ) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'n' || ch0 == 'N') &&
                        (ch1 == 'o' || ch1 == 'O') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 3: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                if ((ch0 == 'y' || ch0 == 'Y') &&
                        (ch1 == 'e' || ch1 == 'E') &&
                        (ch2 == 's' || ch2 == 'S') ) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'o' || ch0 == 'O') &&
                        (ch1 == 'f' || ch1 == 'F') &&
                        (ch2 == 'f' || ch2 == 'F') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 4: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                final char ch3 = str.charAt(3);
                if ((ch0 == 't' || ch0 == 'T') &&
                        (ch1 == 'r' || ch1 == 'R') &&
                        (ch2 == 'u' || ch2 == 'U') &&
                        (ch3 == 'e' || ch3 == 'E') ) {
                    return Boolean.TRUE;
                }
                break;
            }
            case 5: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                final char ch3 = str.charAt(3);
                final char ch4 = str.charAt(4);
                if ((ch0 == 'f' || ch0 == 'F') &&
                        (ch1 == 'a' || ch1 == 'A') &&
                        (ch2 == 'l' || ch2 == 'L') &&
                        (ch3 == 's' || ch3 == 'S') &&
                        (ch4 == 'e' || ch4 == 'E') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            default:
                break;
        }
        return null;
    }
}
