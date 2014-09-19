tBigMLPredict
=============

In this repository you'll find a Talend Open Studio component to be used to call the predictions service at BigML.com

You can use it to easily make, in TOS, remote and local predictions using predictive models defined in BigML.

These are the configuration parameters you could use in TOS to define the behaviour of the component:

* The **schema** we are going to use to send the input fields of the prediction and save the predicted values
* The **User** and **Api KEY** used to connect to the service
* The **Model Id** or **Ensemble Id** on the server that we will use to make predictions
* If we are doing requests in **Developer Mode** or not
* If we want to do local or **Server predictions (remote)**
* If we want to **Resolve fields by name** or code (ie. "Is Potential Buyer" or "1000b2")
* If we want to **Predict with confidence** or not
* The column of the schema where we want to leave the prediction
* The column of the schema where we want to leave the confidence of the prediction

## Why to use BigML in TOS

This approach of integrating "Machine Learning" operations on your projects using ETL Tools is really important if you are an integrator or consultancy firm. You don’t have to learn new tools or complex RESTFul API’s to do such job, you only need to drop a component in the canvas and connect it with the others for the magic to happen.

Talend Open Studio provides an extensible, highly-performant, open source set of tools to access, transform and integrate data from any business system in real time or batch to meet both operational and analytical data integration needs

The broad range of use cases addressed include: massive scale integration (big data/ NoSQL), ETL for business intelligence and data warehousing, data synchronization, data migration, data sharing, data services, and now predictions!!!

## Support

Please, report problems and bugs to [BigML.io-Java issue tracker](https://github.com/javinp/bigml-java/issues)

Discussions about the different bindings take place in the general [BigML mailing list](http://groups.google.com/group/bigml). Or join us in [Campfire chatroom](https://bigmlinc.campfirenow.com/f20a0)

## Build the component

The project uses Maven as project manager.

To build the component, open a command line in the component basedir  and give:

    mvn clean install

This will package the component and copy it in your TOS custom components folder (default to $HOME$/talend_components)

If you want to specify a different custom component location, issue the command:

     mvn clean install -DcomponentsFolder=/path/to/your/tos_custom_components

If you want to package the component in the basedire but **not** deploy it in custom component folder, issue the command:

    mvn clean package


## Command-line parameters
You can set some parameters in command-line using the -D flag. Here's a list:

* **skipTests** (default: true) to skip/unskip the test phase
* **addMavenDescription** (default: true) to add/cut the META-INF/maven stuff in client JAR
* **dependencyVersion** (default: true) to add/cut the version number suffix to all external dependencies
* **useSnapshot** (default: true) to use or not snaphsot releases in all external dependencies
* **componentsFolder** (default: $HOME$/talend_components) path to your installation TOS custom component folder   


