/*
insert into user (user_id, password, name, organization) values ('oagis', 'oagis', 'Open Applications Group Developer', 'Open Applications Group');
*/

insert into app_user (login_id, password, name, organization, oagis_developer_indicator) values ('oagis', 'oagis','Open Applications Group Developer', 'Open Applications Group', True);
insert into namespace (uri, prefix, prescription, owner_user_id, created_by, last_updated_by, creation_timestamp, last_updated_timestamp) select 'http://www.openapplications.org/oagis/10', '', 'OAGIS release 10 namespace', app_user_id, app_user_id, app_user_id, '2014-06-27', '2014-06-27' from app_user where login_id = 'oagis';  

insert into oagsrt_revision.`release` (release_num, release_note, namespace_id) select '10.1', 'Open Applications Group
Interface Specification XMLSchemas and Sample XML Files

OAGIS Release 10_1  

27 June 2014


OAGIS Release 10_1 is a general availability release of OAGIS the release
date is 27 June 2014. 

This release is the continuation of the focus on enabling integration that 
the Open Applications Group and its members are known.

Please provide all feedback to the OAGI Architecture Team via the Feedback 
Forum at: oagis@openapplications.org

These XML reference files continue to evolve.  Please feel
free to use them, but check www.openapplications.org for the most 
recent updates.

OAGIS Release 10_1 includes:

  - Addition of more Open Parties and Quantities from implementation feedback.
  - Updates to the ConfirmBOD to make easier to use.
  - Addtion of DocumentReferences and Attachments for PartyMaster 
  - Support for UN/CEFACT Core Components 3.0.
  - Support for UN/CEFACT XML Naming and Design Rules 3.0
  - Support for UN/CEFACT Data Type Catalog 3.1
  - Support for Standalone BODs using Local elements.


NOTICE: We recommend that you install on your root directory drive as the 
paths may be too long otherwise.
	
As with all OAGIS releases OAGIS Release 10_1 contains XML Schema. To view 
XML Schema it is recommended that you use an XML IDE, as the complete structure 
of the Business Object Documents are not viewable from a single file.

Note that the sample files were used to verify the XMLSchema 
development, and do not necessarily reflect actual business 
transactions.  In many cases,the data entered in the XML files are just 
placeholder text.  Real-world examples for each transaction will be 
provided as they become available. If you are interested in providing 
real-world examples please contact oagis@openapplications.org

Please send suggestions or bug reports to oagis@openapplications.org

Thank you for your interest and support.

Best Regards,
The Open Applications Group Architecture Council
' , namespace_id from namespace where prescription = 'OAGIS release 10 namespace';