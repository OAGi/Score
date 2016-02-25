package org.oagi.srt.persistence.populate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Types {
	
	public static final String defaultDataTypeList[] = {
		"AmountType_0723C8",
		"CodeType_1DEB05"
	};
	
	public static final String dataTypeList[] = {
		"AmountType",
		"BinaryObjectType",
		"GraphicType",
		"SoundType",
		"VideoType",
		"CodeType",
		"DateType",
		"DateTimeType",
		"DurationType",
		"IDType",
		"IndicatorType",
		"MeasureType",
		"NameType",
		"NumberType",
		"PercentType",
		"QuantityType",
		"TextType",	
		"TimeType",
		"ValueType"
	};
	
//	public static final String simpleTypeList[] = {
//		"NormalizedStringType",
//		"TokenType",
//		"StringType",
//		"DayDateType",
//		"MonthDateType",
//		"MonthDayDateType",
//		"YearDateType",
//		"YearMonthDateType",
//		"URIType",
//		"DurationMeasureType",
//		"IntegerNumberType",
//		"PositiveIntegerNumberType",
//		"DayOfWeekHourMinuteUTCType"
//	};
	
	public static List<String> getCorrespondingXSDBuiltType(String cdtPrimitive) {
		List<String> res = new ArrayList<String>();
		if(cdtPrimitive.equalsIgnoreCase("Binary")) {
			res.add("xsd:base64Binary");
			res.add("xsd:hexBinary");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Boolean")) {
			res.add("xsd:boolean");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Decimal")) {
			res.add("xsd:decimal");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Double")) {
			res.add("xsd:double");
			res.add("xsd:float");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Float")) {
			res.add("xsd:float");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Integer")) {
			res.add("xsd:integer");
			res.add("xsd:nonNegativeInteger");
			res.add("xsd:positiveInteger");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Double")) {
			res.add("xsd:double");
			res.add("xsd:float");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Double")) {
			res.add("xsd:double");
			res.add("xsd:float");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("NormalizedString")) {
			res.add("xsd:normalizedString");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("String")) {
			res.add("xsd:string");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("TimeDuration")) {
			res.add("xsd:token");
			res.add("xsd:duration");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("TimePoint")) {
			res.add("xsd:token");
			res.add("xsd:dateTime");
			res.add("xsd:date");
			res.add("xsd:time");
			res.add("xsd:gYearMonth");
			res.add("xsd:gYear");
			res.add("xsd:gMonthDay");
			res.add("xsd:gDay");
			res.add("xsd:gMonth");
			return res;
		}
		
		if(cdtPrimitive.equalsIgnoreCase("Token")) {
			res.add("xsd:token");
			return res;
		}
		
		return res;
	}
}
