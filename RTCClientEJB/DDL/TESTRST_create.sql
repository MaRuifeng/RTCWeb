--<ScriptOptions statementTerminator=";"/>
-- version 1.2
-- maintainer: ruifengm@sg.ibm.com

CREATE SCHEMA TESTRST;

CREATE TABLE TESTRST.TEST_SUITE ( 
	TEST_SUITE_ID        integer NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	SUITE_NAME           varchar(500) NOT NULL  ,
	SUITE_PACKAGE        varchar(500) NOT NULL  ,
	OWNER                varchar(100) NOT NULL  ,
    TEST_CATEGORY        varchar(50) NOT NULL  ,
	CONSTRAINT PK_TEST_SUITE_0 PRIMARY KEY ( TEST_SUITE_ID )
 );

CREATE TABLE TESTRST.TEST_SUITE_RESULT ( 
	TEST_SUITE_RESULT_ID integer NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	TEST_SUITE_ID        integer NOT NULL  ,
	BUILD_ID             integer NOT NULL  ,
	TEST_PHASE           varchar(50) NOT NULL  ,
	TEST_COUNT           integer NOT NULL  ,
	ERROR_COUNT          integer NOT NULL  ,
	FAILURE_COUNT        integer NOT NULL  ,
	EXECUTION_TIMESTAMP  timestamp NOT NULL  ,
	EXECUTION_SECONDS    integer  ,
	PASS_RATE            decimal(4,3) NOT NULL  GENERATED ALWAYS AS (1 - CAST(ERROR_COUNT+FAILURE_COUNT AS DECIMAL)/TEST_COUNT)  ,
	CONSTRAINT PK_TEST_SUITE PRIMARY KEY ( TEST_SUITE_RESULT_ID )
 );

COMMENT ON TABLE TESTRST.TEST_SUITE_RESULT IS 'A table of junit test suites. ';

CREATE TABLE TESTRST.RTC_DEFECT ( 
	DEFECT_ID            integer NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	DEFECT_NUMBER        integer NOT NULL   ,
	DEFECT_NAME          varchar(500) NOT NULL   ,
	TEST_SUITE_ID        integer NOT NULL  ,
	BUILD_ID             integer NOT NULL  ,
	FILING_TIMESTAMP     timestamp NOT NULL  ,
	MODIFICATION_TIMES   integer NOT NULL DEFAULT 0 ,
	MODIFICATION_TIMESTAMP timestamp NOT NULL GENERATED ALWAYS FOR EACH ROW ON UPDATE AS ROW CHANGE TIMESTAMP,
	DEFECT_STATUS        varchar(20) NOT NULL  ,
	DEFECT_LINK          varchar(500) NOT NULL  ,
	CONSTRAINT PK_RTC_DEFECT PRIMARY KEY ( DEFECT_ID )
 );

CREATE TABLE TESTRST.TEST_CASE ( 
	TEST_CASE_ID         integer NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	TEST_SUITE_ID        integer NOT NULL  ,
	TEST_CASE_NAME       varchar(1000) NOT NULL  ,
	TEST_CASE_PATH       varchar(500) NOT NULL ,
	RQM_TEST_CASE_ID     integer ,
	CONSTRAINT PK_TEST_CASE PRIMARY KEY ( TEST_CASE_ID )
 );
COMMENT ON TABLE TESTRST.TEST_CASE IS 'A single junit test is considered as a test case.';

CREATE TABLE TESTRST.TEST_CASE_RESULT ( 
	TEST_CASE_RESULT_ID  integer NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	TEST_CASE_ID         integer NOT NULL  ,
	TEST_SUITE_RESULT_ID integer NOT NULL  ,
	BUILD_ID             integer NOT NULL  ,
	STATUS               varchar(20) NOT NULL  ,
	ERROR_TYPE           varchar(1000)   ,
	ERROR_MESSAGE        varchar(10000)   ,
	FAILURE_TYPE         varchar(1000)   ,
	FAILURE_MESSAGE      varchar(10000)   ,
	EXECUTION_SECONDS    integer  ,
	CONSTRAINT PK_TEST_CASE_RESULT PRIMARY KEY ( TEST_CASE_RESULT_ID )
 );
 
 CREATE TABLE TESTRST.APP_BUILD ( 
	BUILD_ID             integer NOT NULL  GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	BUILD_NAME           varchar(100) NOT NULL  ,
	BUILD_VERSION        integer NOT NULL  ,
	BUILD_TIMESTAMP      timestamp NOT NULL  ,
	GIT_BRANCH           varchar(100) NOT NULL  ,
	SPRINT               varchar(100) NOT NULL  ,
	CONSTRAINT PK_APP_BUILD PRIMARY KEY ( BUILD_ID )
 );
 COMMENT ON TABLE TESTRST.APP_BUILD IS 'Application build information';
 
 CREATE TABLE TESTRST.BUILD_PASS_RATE ( 
	BUILD_PASS_RATE_ID   integer NOT NULL  GENERATED ALWAYS AS IDENTITY ( START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 NO CYCLE CACHE 20 NO ORDER ) ,
	BUILD_ID             integer NOT NULL  ,
	TEST_CATEGORY        varchar(50) NOT NULL  ,
	TEST_PHASE           varchar(50) NOT NULL  ,
	TEST_COUNT           integer NOT NULL  ,
	ERROR_COUNT          integer NOT NULL  ,
	FAILURE_COUNT        integer NOT NULL  ,
	PASS_RATE            decimal(4,3) NOT NULL GENERATED ALWAYS AS (1 - CAST(ERROR_COUNT+FAILURE_COUNT AS DECIMAL)/TEST_COUNT),
	CONSTRAINT PK_BUILD_PASS_RATE PRIMARY KEY ( BUILD_PASS_RATE_ID )
 );
COMMENT ON TABLE TESTRST.BUILD_PASS_RATE IS 'Pass rates of different test categories in different test phases for a build. ';

-- indices
CREATE INDEX TESTRST.IDX_RTC_DEFECT ON TESTRST.RTC_DEFECT ( TEST_SUITE_ID );

CREATE INDEX TESTRST.IDX_RTC_DEFECT_0 ON TESTRST.RTC_DEFECT ( BUILD_ID );

CREATE INDEX TESTRST.IDX_TEST_SUITE_RESULT ON TESTRST.TEST_SUITE_RESULT ( TEST_SUITE_ID );

CREATE INDEX TESTRST.IDX_TEST_SUITE_RESULT_0 ON TESTRST.TEST_SUITE_RESULT ( BUILD_ID );

CREATE INDEX TESTRST.IDX_TEST_CASE ON TESTRST.TEST_CASE ( TEST_SUITE_ID );

CREATE INDEX TESTRST.IDX_TEST_CASE_RESULT_0 ON TESTRST.TEST_CASE_RESULT ( TEST_CASE_ID );

CREATE INDEX TESTRST.IDX_TEST_CASE_RESULT_1 ON TESTRST.TEST_CASE_RESULT ( TEST_SUITE_RESULT_ID );

CREATE INDEX TESTRST.IDX_TEST_CASE_RESULT_2 ON TESTRST.TEST_CASE_RESULT ( BUILD_ID );

CREATE INDEX TESTRST.IDX_BUILD_PASS_RATE ON TESTRST.BUILD_PASS_RATE ( BUILD_ID );

-- unique constraint
ALTER TABLE TESTRST.TEST_SUITE ADD CONSTRAINT SUITE_NAME_UN UNIQUE ("SUITE_NAME");

ALTER TABLE TESTRST.TEST_CASE ADD CONSTRAINT CASE_NAME_PATH_UN UNIQUE ("TEST_CASE_NAME", "TEST_CASE_PATH");

ALTER TABLE TESTRST.RTC_DEFECT ADD CONSTRAINT DEFECT_NUM_UN UNIQUE ("DEFECT_NUMBER");

ALTER TABLE TESTRST.APP_BUILD ADD CONSTRAINT BUILD_NAME_UN UNIQUE ("BUILD_NAME");

ALTER TABLE TESTRST.APP_BUILD ADD CONSTRAINT BUILD_VERSION_UN UNIQUE ("BUILD_VERSION");

ALTER TABLE TESTRST.BUILD_PASS_RATE ADD CONSTRAINT PASS_RATE_BUILD_CAT_PHASE_UN UNIQUE ("BUILD_ID", "TEST_CATEGORY", "TEST_PHASE");

-- value check constraint
-- ALTER TABLE TESTRST.TEST_SUITE ADD CONSTRAINT TEST_CAT_CK CHECK (TEST_CATEGORY IN ('API','GUI'));

-- ALTER TABLE TESTRST.TEST_SUITE ADD CONSTRAINT TEST_PHA_CK CHECK (TEST_PHASE IN ('BVT', 'SMT', 'Regression Test'));

-- foreign keys
ALTER TABLE TESTRST.RTC_DEFECT ADD FOREIGN KEY FK_RTC_DEFECT_TEST_SUITE ( TEST_SUITE_ID ) REFERENCES TESTRST.TEST_SUITE( TEST_SUITE_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.TEST_CASE ADD FOREIGN KEY FK_TEST_CASE_TEST_SUITE ( TEST_SUITE_ID ) REFERENCES TESTRST.TEST_SUITE( TEST_SUITE_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.TEST_CASE_RESULT ADD FOREIGN KEY FK_TEST_CASE_RESULT_TEST_CASE ( TEST_CASE_ID ) REFERENCES TESTRST.TEST_CASE( TEST_CASE_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.TEST_CASE_RESULT ADD FOREIGN KEY FK_TEST_CASE_RESULT ( TEST_SUITE_RESULT_ID ) REFERENCES TESTRST.TEST_SUITE_RESULT( TEST_SUITE_RESULT_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.TEST_SUITE_RESULT ADD FOREIGN KEY FK_TEST_SUITE_RESULT ( TEST_SUITE_ID ) REFERENCES TESTRST.TEST_SUITE( TEST_SUITE_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.TEST_SUITE_RESULT ADD FOREIGN KEY FK_TEST_SUITE_RESULT_APP_BUILD ( BUILD_ID ) REFERENCES TESTRST.APP_BUILD( BUILD_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.TEST_CASE_RESULT ADD FOREIGN KEY FK_TEST_CASE_RESULT_APP_BUILD ( BUILD_ID ) REFERENCES TESTRST.APP_BUILD( BUILD_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.BUILD_PASS_RATE ADD FOREIGN KEY FK_BUILD_PASS_RATE_APP_BUILD ( BUILD_ID ) REFERENCES TESTRST.APP_BUILD( BUILD_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;

ALTER TABLE TESTRST.RTC_DEFECT ADD FOREIGN KEY FK_RTC_DEFECT_APP_BUILD ( BUILD_ID ) REFERENCES TESTRST.APP_BUILD( BUILD_ID ) ON DELETE NO ACTION ON UPDATE RESTRICT;
