bigml-talend-components
=======================

BigML Components for [Talend Open Studio](https://www.talend.com/products/talend-open-studio) (TOS).

The aim of this project is to give to integrators and consulting companies a bunch of BigML components ready to be used inside TOS. 

These components are licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## TOS-Components

BigML.io is a REST-style API for creating and managing BigML resources programmatically. Using BigML.io you can create, retrieve, update and delete Sources, Datasets, Models, Ensembles, Clusters, Predictions, Centroids, Batch Predictions, Batch Centroids, and Evaluations using standard HTTP methods. 

In this repository you'll find all the TOS components that you can use in your job designs to interact with all the BigML.io services - The BigML API.

Each component has its own directory and its own maven project. 

For now, the available componentes are:

* [**tBigMLPredict**](tBigMLPredict/README.md): allows real-time predictions. You can send your input fields and get in return the prediction and its confidence degree.

## Why Talend Open Studio?

Talend Open Studio provides an extensible, highly-performant, open source set of tools to access, transform and integrate data from any business system in real time or batch to meet both operational and analytical data integration needs. With 800+ connectors, it integrates almost any data source. The broad range of use cases addressed include: massive scale integration (big data/ NoSQL), ETL for business intelligence and data warehousing, data synchronization, data migration, data sharing, data services, and now predictions.

Talend has been positioned by Gartner, Inc. in the “Visionaries” quadrant of the [Magic Quadrant for Data Integration Tools](http://www.gartner.com/technology/reprints.do?id=1-1Y3RAD7&ct=140724&st=sb).

Talend's customer base for this product portfolio is estimated at more than 3,300 organizations.

## Support

Please, report problems and bugs to 
[BigML.io-TOS issue tracker](https://github.com/bigmlcom/bigml-talend-components/issues)

Discussions about the different bindings take place in the general
[BigML mailing list](http://groups.google.com/group/bigml). Or join us
in [Campfire chatroom](https://bigmlinc.campfirenow.com/f20a0)
