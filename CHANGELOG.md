RTCWeb CHANGELOG
=====================

This file is used to list changes made in each version of the RTC web proxy. 

1.0
-----
- [ruifengm@sg.ibm.com] - RTC client that fulfills all requirements listed in Task 144144 (initial release).

1.1
-----
- [ruifengm@sg.ibm.com] - Removed test log attaching feature to save RTC resource and reduce takt time.

1.2
-----
- [ruifengm@sg.ibm.com] - Added code section in the RTCClient.getWorkItemStatus method to show all available work item status in the log.

1.3
-----
- [ruifengm@sg.ibm.com] - Modified the schema and APIs to accommodate Junit results from Cucumber and added build information. (2016-Jul-21)

1.4
-----
- [ruifengm@sg.ibm.com] - Modified API /getBuildPassRates to take buildName as an optional parameter. (2016-Aug-27)

1.5 
-----
- [ruifengm@sg.ibm.com] - Added API /getBuildLatestTestResults to retrieve latest test results of a given build. (2016-Sep-01)

1.6 
-----
- [ruifengm@sg.ibm.com] - Added functionalities (DB schema & APIs) to support processing Brakeman scan results on Jenkins. (2016-Nov-22)

1.7 
-----
- [ruifengm@sg.ibm.com] - Added API to exit established RTC instance. (2017-Mar-14)

1.8
-----
- [ruifengm@sg.ibm.com] - Added functionalities (DB & APIs) to support cookbook dependency check by Berkshelf on Jenkins. (2017-Mar-18)

1.9
-----
- [ruifengm@sg.ibm.com] - Differentiated 'filed against' value in RTC for different project components. (2017-Apr-29)

1.10
-----
- [ruifengm@sg.ibm.com] - Added test report HTML link as a comment to defect. (2017-May-17)

1.11
-----
- [ruifengm@sg.ibm.com] - Fixed work item status management issue by disconnecting workitem from working copy manager after operation. (2017-Jun-01)

1.12
-----
- [ruifengm@sg.ibm.com] - Updated syncRTCDefectStatus API to sync latest defects only, instead of all. (2017-Jun-07)

1.13
-----
- [ruifengm@sg.ibm.com] - Fixed build pass rate retrieval error in terms of defect counts. (2017-Jun-23)

- - -
Check the [Markdown Syntax Guide](http://daringfireball.net/projects/markdown/syntax) for help with Markdown.

The [Github Flavored Markdown page](http://github.github.com/github-flavored-markdown/) describes the differences between markdown on github and standard markdown.