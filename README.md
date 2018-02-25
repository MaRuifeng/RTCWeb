# RTC Web Proxy

Description
---------------

This RTC Web Proxy wraps up the plain Java client libraries of RTC (*https://rsjazz.wordpress.com/2013/03/20/understanding-and-using-the-rtc-java-client-api/*) in an IBM WebSphere Application and exposes APIs for RTC work item query/create/update/close. 

The benefits including 
1. Custom APIs can be easily constructed, exposed and managed to obtain RTC client service in an automated manner
2. The running WAS application serves as a running RTC client that can hold a login session for faster access to the work items

The application also hosts APIs that connect to a DB2 database storing test result related information. Upon automated test execution, the APIs can be invoked to store the results in the database and file RTC work items. 

The database schema is designed in a way most suitable for JUnit format results. 

Owners
------
Author: Ruifeng Ma

Organization: IBM

Requirements
------------
IBM WebSphere Application Server (8.5.0 and above)
IBM DB2

Usage
-----
Build up the EAR application file and deploy it to the WAS server. 
Ensure the data source connection is properly set up. 


Contributing
------------
Contact the owner before contributing.

1. Fork the repository on Github
2. Create a named feature branch (like `add_component_x`)
3. Write your change
4. Write tests for your change (if applicable)
5. Run the tests, ensuring they all pass
6. Submit a Pull Request using Github

License and Authors
-------------------
Authors: ruifengm@sg.ibm.com mrfflyer@gmail.com

