Requirements: Java, Apache Lucene, Porter Stemmer, Inquery English stopword list
Required libraries can be found in libraries/ folder

All codes can be found in the codes/ folder
About the upload:
->codes/project/:
	1. Proc.java: code, driver and crossfold validation method for Proximity based Rocchio models(proc1,proc2,proc3)
	2. Baselines: code, driver and crossfold validation method for RM3, RM4, SMM and DMM baseline models
	3. SignificanceTest.java: significance testing for results of proc models vs baseline models
->codes/util/ contains other utility functions and classes used by the above files

More details about the project can be found in the report: ./Report/report.pdf