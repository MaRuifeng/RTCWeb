
--<ScriptOptions statementTerminator="!"/>
-- version 1.0
-- maintainer: ruifengm@sg.ibm.com

BEGIN
	DECLARE V_SCHEMA_NAME VARCHAR(50) DEFAULT 'JENKINS';
	DECLARE V_DROP_SQL VARCHAR(500);

	--drop fk
	FOR V1 AS
	  	SELECT CONSTNAME, TABNAME, REFTABNAME FROM SYSCAT.REFERENCES WHERE TABSCHEMA = V_SCHEMA_NAME
	DO
		SET V_DROP_SQL = 'ALTER TABLE '||V_SCHEMA_NAME||'.'||V1.TABNAME||' DROP CONSTRAINT '||V1.CONSTNAME;
		EXECUTE IMMEDIATE (V_DROP_SQL);
		
	END FOR;
	
	--drop pk,check and unique constraints, and excluding system generated constraints
	FOR V1 AS
	  	SELECT CONSTNAME, TABNAME FROM SYSCAT.TABCONST WHERE TABSCHEMA = V_SCHEMA_NAME AND CONSTNAME NOT LIKE 'SQL%'
	DO
		SET V_DROP_SQL = 'ALTER TABLE '||V_SCHEMA_NAME||'.'||V1.TABNAME||' DROP CONSTRAINT '||V1.CONSTNAME;
		EXECUTE IMMEDIATE (V_DROP_SQL);
		
	END FOR;
	
    --drop index
	FOR V1 AS
	  	SELECT INDNAME FROM SYSCAT.INDEXES WHERE TABSCHEMA = V_SCHEMA_NAME
	DO
		SET V_DROP_SQL = 'DROP INDEX '||V_SCHEMA_NAME||'.'||V1.INDNAME;
	    EXECUTE IMMEDIATE (V_DROP_SQL);
		
	END FOR;
	
	--drop table
	FOR V1 AS
	  	SELECT TABNAME FROM SYSCAT.TABLES WHERE TABSCHEMA = V_SCHEMA_NAME AND TYPE = 'T'
	DO
		SET V_DROP_SQL = 'DROP TABLE '||V_SCHEMA_NAME||'.'||V1.TABNAME;
		EXECUTE IMMEDIATE (V_DROP_SQL);
		
	END FOR;
	
	--drop schema
	SET V_DROP_SQL = 'DROP SCHEMA ' ||V_SCHEMA_NAME||' RESTRICT'; 
	EXECUTE IMMEDIATE (V_DROP_SQL);
END!