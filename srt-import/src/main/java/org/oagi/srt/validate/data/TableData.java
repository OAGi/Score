package org.oagi.srt.validate.data;

public class TableData {
	public static final String[][] XDT_BUILT_IN_TYPE = {
		{"any type", "xsd:anyType", null},
		{"any simple type", "xsd:anySimpleType", "xsd:anyType"},
		{"duration", "xsd:duration", "xsd:anySimpleType"},
		{"date time", "xsd:dateTime", "xsd:anySimpleType"},
		{"time", "xsd:time", "xsd:anySimpleType"},
		{"date", "xsd:date", "xsd:anySimpleType"},
		{"gregorian year month", "xsd:gYearMonth", "xsd:anySimpleType"},
		{"gregorian year", "xsd:gYear", "xsd:anySimpleType"},
		{"gregorian month day", "xsd:gMonthDay", "xsd:anySimpleType"},
		{"gregorian day", "xsd:gDay", "xsd:anySimpleType"},
		{"gregorian month", "xsd:gMonth", "xsd:anySimpleType"},
		{"string", "xsd:string", "xsd:anySimpleType"},
		{"normalized string", "xsd:normalizedString", "xsd:string"},
		{"token", "xsd:token", "xsd:normalizedString"},
		{"boolean", "xsd:boolean", "xsd:anySimpleType", "xsd:anySimpleType"},
		{"base64 binary", "xsd:base64Binary", "xsd:anySimpleType"},
		{"hex binary", "xsd:hexBinary", "xsd:anySimpleType"},
		{"float", "xsd:float", "xsd:anySimpleType"},
		{"decimal", "xsd:decimal", "xsd:anySimpleType"},
		{"integer", "xsd:integer", "xsd:decimal"},
		{"non negative integer", "xsd:nonNegativeInteger", "xsd:integer"},
		{"positive integer", "xsd:positiveeInteger", "xsd:nonNegativeInteger"},
		{"double", "xsd:double", "xsd:anySimpleType"},
		{"any uri", "xsd:anyURI", "xsd:anySimpleType"}
	};
									
}
