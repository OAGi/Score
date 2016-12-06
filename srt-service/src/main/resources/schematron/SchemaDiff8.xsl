<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:svcdoc="http://xmlns.oracle.com/Services/Documentation/V1" xmlns:ebocontext="http://xmlns.oracle.com/EBO/BusinessContext/V1" xmlns="http://www.ascc.net/xml/schematron" exclude-result-prefixes="xsi svcdoc xsd ebocontext">
	<xsl:output method="xml"/>
	<!-- Known Limitation 
		- When the schema contains anonymous types.
		- Annotation is not checked at every possible location.
		- Since we introduce the xsd:group in Version5, the uniqueness of the xsd:choice may not be by the @name or @ref of the xsd:element but it may be by the @ref of the xsd:group. I have not modififed the code to take that into account. We are okay for now because we don't have an xsd:choice which contains only the xsd:group.
	-->
	<!-- Version notes.
	Version3: This version was enhanced to take into accoun the XSD features used by the Meta.xsd. The particular addition is the use of the 'default' attribute in the xsd:attribute element.

	Version4: 
	1) This version is enhanced to deal with the svcdoc:Deprecated which is nested under the xsd:appinfo. The EBO DES does not produce that info while the original schemas have that. So I need to ensure that assertions related to those elements are not produced. The difficulty is for the case when there is no existing xsd:annotation, i.e., the xsd:annotation is added to contain only the xsd:appinfo/svcdoc:Deprecated element. In that case, the whole xsd:annotation needs to be ignored.
    2) It is also fixed to check the xsd:annotation which is a child of the xsd:choice. Previously, I didn't know that there could be an xsd:annotation child of the xsd:choice.   

	Version5:
	1) Add checking of the xsd:group declarations and usages.
	2) Make sure that all text comparisons escapes the single quote or double quotes in the content. I added the template "getTextComparisonExpression" to handle this generically.
	Version6:
	1) Ignore verification of the value of the xsd:schema/@version attribute.
	2) Fix relating the switching from the svcdoc:Industry tag to the svcdoc:BusinessContext
	Version7:
	1) Take into account the fact that there can be an xsd:annotation appearing as a first child of the xsd:sequence. So when checking the structured content of the xsd:sequence, the code ignore the xsd:annotation. This change does not attempt to check the content of the xsd:annotation.
	2) Fix a bug in the xsd:group under the xsd:complexContent. There was a typo in the XPATH that points to the xsd:element instead of the xsd:group.

    -->
	<!-- toggle the $checkAnno variable to values other than 1 to turn-off the assertions that check about the existent of the xsd:annotation -->
	<xsl:variable name="checkAnno">2</xsl:variable>
	<xsl:template name="START" match="/">
		<xsl:element name="schema">
			<xsl:element name="ns">
				<xsl:attribute name="prefix">xsd</xsl:attribute>
				<xsl:attribute name="uri">http://www.w3.org/2001/XMLSchema</xsl:attribute>
			</xsl:element>
			<xsl:element name="ns">
				<xsl:attribute name="prefix">svcdoc</xsl:attribute>
				<xsl:attribute name="uri">http://xmlns.oracle.com/Services/Documentation/V1</xsl:attribute>
			</xsl:element>
			<xsl:element name="ns">
				<xsl:attribute name="prefix">ebocontext</xsl:attribute>
				<xsl:attribute name="uri">http://xmlns.oracle.com/EBO/BusinessContext/V1</xsl:attribute>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">root</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">/</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(child::*) = <xsl:value-of select="count(child::*)"/></xsl:attribute>#Issue: The are too many root elements.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = <xsl:value-of select="count(@*)"/></xsl:attribute>#Issue: The are too many root attributes.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:schema) = 1</xsl:attribute>#Issue: There must be one xsd:schema root element.
					</xsl:element>
				</xsl:element>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">Generic rules about xsd:import</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">xsd:import</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The 'xsd:import' element must not have any child element.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 2</xsl:attribute>#Issue: The 'xsd:import' element must have exactly 2 attributes.
					</xsl:element>
				</xsl:element>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">Generic rules about xsd:include</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">xsd:include</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The 'xsd:include' element must not have any child element.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 1</xsl:attribute>#Issue: The 'xsd:include' element must have exactly 1 attributes.
					</xsl:element>
				</xsl:element>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">Generic rules for xsd:sequence</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">xsd:sequence</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'xsd:sequence' must have no attribute.
					</xsl:element>
				</xsl:element>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">Generic rules for xsd:choice</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">xsd:choice</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'xsd:choice' element must have no attribute.
					</xsl:element>
				</xsl:element>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">Generic rules for xsd:complexContent</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">xsd:complexContent</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'xsd:complexContent' element must have no attribute.
					</xsl:element>
					<xsl:element name="assert">
					<!-- Note: This is assuming that xsd:annotation is not used under the xsd:complexContent. -->
						<xsl:attribute name="test">count(./*) = 1</xsl:attribute>#Issue: The 'xsd:complexContent' element must have only one child element.
					</xsl:element>
				</xsl:element>
			</xsl:element>
			<xsl:element name="pattern">
				<xsl:attribute name="name">Generic rules for xsd:attribute</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">xsd:attribute</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) &lt;= 5</xsl:attribute>#Issue: The 'xsd:attribute' element can have at most 4 attributes - @name, @type, @id, @default and/or @use attribute.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*[name() != 'name' and name() != 'type' and  name() != 'use' and name() !='default' and name() != 'id']) = 0</xsl:attribute>#Issue: The 'xsd:attribute' element has unusual attributes other than @name, @type, @id, @default, and @use.</xsl:element>
				<!--	<xsl:element name="assert">
						 <xsl:attribute name="test">count(./*) = 0</xsl:attribute>#Issue: The 'xsd:attribute' element must have no child element. 
								This is not applicable as it can have annotation.
						
					</xsl:element>
				-->
				</xsl:element>
			</xsl:element>
			<!-- Remove this block b/c it contains duplicated content as the block below.
			<xsl:element name="pattern">
				<xsl:attribute name="name">Svcdoc leaf nodes</xsl:attribute>
				<xsl:element name="rule">
					<xsl:attribute name="context">svcdoc:Description</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The 'svcdoc:Description' element has too many child elements.
				</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'svcdoc:Description' element has too many attributes.
				</xsl:element>
				</xsl:element>
				<xsl:element name="rule">
					<xsl:attribute name="context">svcdoc:Type</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The 'svcdoc:Type' element has too many child elements.
				</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'svcdoc:Type' element has too many attributes.
				</xsl:element>
				</xsl:element>
				 Remove this in version 6 as the svcdoc:Industry element is no longer used. It is replaced by the svcdoc:BusinessContext 
				<xsl:element name="rule">
					<xsl:attribute name="context">svcdoc:Industry</xsl:attribute>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The 'svcdoc:Industry' element has too many child elements.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'svcdoc:Industry' element has too many attributes.
					</xsl:element>
				</xsl:element>
					
			</xsl:element>
-->
			<!--Annotation related assertions-->
			<xsl:if test="$checkAnno = 1">
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:BusinessContext assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:BusinessContext</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The ebocontext:BusinessContext element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:BusinessContext element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:Sequence assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:Sequence</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The ebocontext:Sequence element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:Sequence element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:DerivationTypeCode assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:DerivationTypeCode</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The ebocontext:DerivationTypeCode element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:DerivationTypeCode element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:DerivationHierarchy assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:DerivationHierarchy</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:DerivationHierarchy element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:DerivationBase assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:DerivationBase</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:DerivationBase element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:DerivedBusinessContext assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:DerivedBusinessContext </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:DerivedBusinessContext element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global ebocontext:EBOContext assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">ebocontext:EBOContext </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The ebocontext:EBOContext element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The ebocontext:DerivedBusinessContext element must have no attribute.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(preceding-sibling::*) = 0 and count(following-sibling::*) = 0</xsl:attribute>#Issue: The ebocontext:EBOContext element must have no brother and sister. It exists in its own 'xsd:appinfo'.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global svcdoc:Description assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">svcdoc:Description</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The svcdoc:Description element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The svcdoc:Description element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global svcdoc:Type assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">svcdoc:Type </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The svcdoc:Type element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The svcdoc:Type element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<!-- Remove this in version 6 as the svcdoc:Industry element is no longer used. It is replaced by the svcdoc:BusinessContext 
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global svcdoc:Industry assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">svcdoc:Industry </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The svcdoc:Industry element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The svcdoc:Industry element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				-->
				<!-- Add this in version 6 -->
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global svcdoc:BusinessContext assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">svcdoc:BusinessContext</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The 'svcdoc:BusinessContext' element has too many child elements.
							</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(@*) = 0</xsl:attribute>#Issue: The 'svcdoc:BusinessContext' element has too many attributes.
						</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global svcdoc:EBOName assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">svcdoc:EBOName </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(child::*) = 0</xsl:attribute>#Issue: The svcdoc:EBOName element must have no child element.</xsl:element>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The svcdoc:EBOName element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global svcdoc:EBO assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">svcdoc:EBO </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The svcdoc:EBO element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global xsd:annotation assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:annotation</xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The xsd:annotation element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global xsd:documentation assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:documentation </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The xsd:documentation element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
				<xsl:element name="pattern">
					<xsl:attribute name="name">Global xsd:appinfo assertions</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:appinfo </xsl:attribute>
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./@*) = 0</xsl:attribute>#Issue: The xsd:appinfo element must have no attribute.</xsl:element>
					</xsl:element>
				</xsl:element>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template name="schema" match="xsd:schema">
		<xsl:element name="pattern">
			<xsl:attribute name="name">xsd:schema</xsl:attribute>
			<xsl:element name="rule">
				<xsl:attribute name="context">xsd:schema</xsl:attribute>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(child::*) = <xsl:value-of select="count(child::*)"/></xsl:attribute>#Issue: The '<xsl:value-of select="name(.)"/>' element has unmatched number of child elements.
				</xsl:element>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(@*) = <xsl:value-of select="count(@*)"/></xsl:attribute>#Issue: The '<xsl:value-of select="name(.)"/>' has unmatched number of attributes.
				</xsl:element>
				<xsl:for-each select="@*">
					<!-- Ignoring verification of the 'version' attribute value in version 6 -->
					<xsl:if test="name(.) != 'version'">
						<xsl:element name="assert">
							<xsl:attribute name="test">@<xsl:value-of select="name(current())"/>='<xsl:value-of select="current()"/>'</xsl:attribute>#Issue: Issue with the 'xsd:schema/@<xsl:value-of select="name(current())"/>'.
					</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(namespace::*) = <xsl:value-of select="count(namespace::*)"/></xsl:attribute>#Issue: The number of namespace declarations do not match.
				</xsl:element>
				<xsl:for-each select="namespace::*">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(namespace::*[. = '<xsl:value-of select="current()"/>' and name()='<xsl:value-of select="name(current())"/>']) = 1</xsl:attribute>#Issue: Unmatched namespace declaration for <xsl:value-of select="current()"/>.
					</xsl:element>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:import) = <xsl:value-of select="count(xsd:import)"/></xsl:attribute>#Issue: The number of 'xsd:import' elements do not match.
				</xsl:element>
				<xsl:for-each select="xsd:import">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:import[@namespace='<xsl:value-of select="current()/@namespace"/>' and @schemaLocation='<xsl:value-of select="current()/@schemaLocation"/>']) = 1</xsl:attribute>#Issue: Issue with an 'xsd:import'. The required 'xsd:import' does not exist or has duplicates.
					</xsl:element>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:include) = <xsl:value-of select="count(xsd:include)"/></xsl:attribute>#Issue: The number of 'xsd:include' elements do not match.
				</xsl:element>
				<xsl:for-each select="xsd:include">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:include[@schemaLocation='<xsl:value-of select="current()/@schemaLocation"/>']) = 1</xsl:attribute>#Issue: Issue with an 'xsd:include'. The required 'xsd:include' does not exist or has duplicates.
					</xsl:element>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:element) = <xsl:value-of select="count(xsd:element)"/></xsl:attribute>#Issue: The number of global xsd:element declarations do not match.
				</xsl:element>
				<xsl:for-each select="xsd:element">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:element[@name='<xsl:value-of select="current()/@name"/>']) = 1</xsl:attribute>#Issue: Issue with a global xsd:element declaration. The required global xsd:element whose name is '<xsl:value-of select="current()/@name"/>' does not exist or has duplicates.
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="xsd:element">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:element[@name='<xsl:value-of select="current()/@name"/>']) = 1</xsl:attribute>#Issue: Issue with a global xsd:element declaration. The required global xsd:element whose name is '<xsl:value-of select="current()/@name"/>' does not exist or has duplicates.
				</xsl:element>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:group) = <xsl:value-of select="count(xsd:group)"/></xsl:attribute>#Issue: The number of global xsd:group declarations do not match.
				</xsl:element>
				<xsl:for-each select="xsd:group">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:group[@name='<xsl:value-of select="current()/@name"/>']) = 1</xsl:attribute>#Issue: Issue with a global xsd:group declaration. The required global xsd:group whose name is '<xsl:value-of select="current()/@name"/>' does not exist or has duplicates.
					</xsl:element>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:complexType) = <xsl:value-of select="count(xsd:complexType)"/></xsl:attribute>#Issue: The number of global complexType declarations do not match.
				</xsl:element>
				<xsl:for-each select="xsd:complexType">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:complexType[@name='<xsl:value-of select="current()/@name"/>']) = 1</xsl:attribute>#Issue: Issue with a global complexType declaration. The required global complexType whose name is '<xsl:value-of select="current()/@name"/>' does not exist or has duplicates.
					</xsl:element>
				</xsl:for-each>
				<xsl:if test="count(xsd:annotation) &gt; 0 and $checkAnno = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:annotation) = <xsl:value-of select="count(xsd:annotation)"/></xsl:attribute>#Issue: Unmatched number of child 'xsd:annotation'. Expect <xsl:value-of select="count(xsd:annotation)"/>.
					</xsl:element>
				</xsl:if>
			</xsl:element>
		</xsl:element>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template name="globalElement" match="xsd:schema/xsd:element">
		<xsl:element name="pattern">
			<xsl:attribute name="name">Investigating content of the global xsd:element '<xsl:value-of select="@name"/>'</xsl:attribute>
			<xsl:element name="rule">
				<xsl:attribute name="context">xsd:schema/xsd:element[@name = '<xsl:value-of select="@name"/>']</xsl:attribute>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(child::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(child::*[name() != 'xsd:annotation'])"/></xsl:attribute>#Issue: The number of child elements (excepts xsd:annotation) do not match.
				</xsl:element>
				<!-- This can give a wrong warning particularly when the attribute is optional and has default value.
				<xsl:element name="assert">
					<xsl:attribute name="test">count(@*) = <xsl:value-of select="count(@*)"/></xsl:attribute>#Issue: The number of child attributes do not match.
				</xsl:element>
				-->
				<xsl:for-each select="@*">
					<xsl:choose>
						<xsl:when test="name(current()) != 'abstract' and name(current()) != 'nillable'">
							<xsl:element name="assert">
								<xsl:attribute name="test">@<xsl:value-of select="name(current())"/> = '<xsl:value-of select="current()"/>'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' does not match.
							</xsl:element>						
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="name(current()) = 'nillable' and @nillable = 'false'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(@nillable) = 0 or @nillable = 'false'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' does not match.
									</xsl:element>		
							</xsl:if>
							<xsl:if test="name(current()) = 'nillable' and @nillable = 'true'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(@nillable) = 1 and @nillable = 'true'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' does not match.
									</xsl:element>		
							</xsl:if>
							<xsl:if test="name(current()) = 'abstract' and @abstract = 'false'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(@abstract) = 0 or @abstract = 'false'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' does not match.
									</xsl:element>		
							</xsl:if>
							<xsl:if test="name(current()) = 'abstract' and @abstract = 'true'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(@abstract) = 1 and @abstract = 'true'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' does not match.
									</xsl:element>		
							</xsl:if>							
						</xsl:otherwise>
					</xsl:choose>				
				</xsl:for-each>
				<xsl:if test="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0]) &gt; 0 and $checkAnno = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:annotation) = <xsl:value-of select="count(xsd:annotation)"/></xsl:attribute>#Issue: Unmatched number of child 'xsd:annotation'. Expect <xsl:value-of select="count(xsd:annotation)"/>.
					</xsl:element>
				</xsl:if>
			</xsl:element>
		</xsl:element>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template name="globalComplexTypeAndGroup" match="xsd:schema/xsd:complexType | xsd:schema/xsd:group">
		<xsl:element name="pattern">
			<xsl:choose>
				<xsl:when test="local-name(.)='complexType'">
					<xsl:attribute name="name">Investigating content of the global xsd:complexType '<xsl:value-of select="@name"/>'</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="name">Investigating content of the global xsd:group '<xsl:value-of select="@name"/>'</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:element name="rule">
				<xsl:choose>
					<xsl:when test="local-name(.)='complexType'">
						<xsl:attribute name="context">xsd:schema/xsd:complexType[@name='<xsl:value-of select="@name"/>']</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="context">xsd:schema/xsd:group[@name='<xsl:value-of select="@name"/>']</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:for-each select=".//xsd:element">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(.//xsd:element[@id = '<xsl:value-of select="current()/@id"/>']) = 1</xsl:attribute>Missing an xsd:element with @id = '<xsl:value-of select="current()/@id"/>'.
					</xsl:element>
				</xsl:for-each>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(child::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(child::*[name() != 'xsd:annotation'])"/></xsl:attribute>#Issue: The number of child elements (except xsd:annotation) do not match.
				</xsl:element>
				<xsl:if test="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0]) &gt; 0 and $checkAnno = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:annotation) = <xsl:value-of select="count(xsd:annotation) "/></xsl:attribute>#Issue: Unmatched number of child 'xsd:annotation'. Expect <xsl:value-of select="count(xsd:annotation)"/>.
					</xsl:element>
				</xsl:if>
				<!-- This can give a false warning if a particular attribute is optional with a default value
				<xsl:element name="assert">
					<xsl:attribute name="test">count(@*) = <xsl:value-of select="count(@*)"/></xsl:attribute>#Issue: The number of child attributes do not match.
				</xsl:element>
				
				<xsl:for-each select="@*">
					<xsl:element name="assert">
						<xsl:attribute name="test">@<xsl:value-of select="name(current())"/> = '<xsl:value-of select="current()"/>'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' does not match.
					</xsl:element>
				</xsl:for-each>
				-->
				<xsl:if test="count(@id) = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">@id = '<xsl:value-of select="@id"/>'</xsl:attribute>#Issue: The value of the attribute @id does not match.
					</xsl:element>												
				</xsl:if>
				<xsl:if test="count(@final) = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">@final = '<xsl:value-of select="@final"/>'</xsl:attribute>#Issue: The value of the attribute @final does not match.
					</xsl:element>												
				</xsl:if>				
				<xsl:if test="local-name(.)='complexType'">
					<!-- The content in this IF block is only applicable to the case of the xsd:complexType -->
					<xsl:if test="count(@abstract) = 1">
						<xsl:if test="@abstract = 'true'">
							<xsl:element name="assert">
								<xsl:attribute name="test">@abstract = 'true'</xsl:attribute>#Issue: The value of the attribute @abstract does not match.
							</xsl:element>	
						</xsl:if>
						<xsl:if test="@abstract = 'false'">
							<xsl:element name="assert">
								<xsl:attribute name="test">count(@abstract) = 0 or @abstract = 'false'</xsl:attribute>#Issue: The value of the attribute @abstract does not match.
							</xsl:element>	
						</xsl:if>
					</xsl:if>		
					<xsl:if test="count(@abstract) = 0">
						<xsl:element name="assert">
							<xsl:attribute name="test">count(@abstract) = 0 or @abstract = 'false'</xsl:attribute>#Issue: The value of the attribute @abstract does not match or shouldn't be present.
						</xsl:element>
					</xsl:if>
					<xsl:if test="count(@mixed) = 1">
						<xsl:if test="@mixed = 'true'">
							<xsl:element name="assert">
								<xsl:attribute name="test">@mixed = 'true'</xsl:attribute>#Issue: The value of the attribute @mixed does not match.
							</xsl:element>												
						</xsl:if>
						<xsl:if test="@mixed = 'false'">
							<xsl:element name="assert">
								<xsl:attribute name="test">count(@mix) = 0 or @mixed = 'false'</xsl:attribute>#Issue: The value of the attribute @mixed does not match.
							</xsl:element>												
						</xsl:if>
					</xsl:if>		
					<xsl:if test="count(@mixed) = 0">
						<xsl:element name="assert">
							<xsl:attribute name="test">count(@mixed) = 0 or @mixed = 'false'</xsl:attribute>#Issue: The value of the attribute @mixed does not match or shouldn't be present.
						</xsl:element>
					</xsl:if>																
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:attribute) = <xsl:value-of select="count(xsd:attribute)"/></xsl:attribute>#Issue: The number of child 'xsd:attribute' nodes does not match.
					</xsl:element>
					<!-- taking care of the descendant xsd:attribute nodes -->
					<xsl:for-each select=".//xsd:attribute">
						<xsl:element name="assert">
							<xsl:attribute name="test">count(.//xsd:attribute[@id = '<xsl:value-of select="@id"/>' and @name='<xsl:value-of select="@name"/>' and @type='<xsl:value-of select="@type"/>' and <xsl:if test="count(@default) = 1">@default='<xsl:value-of select="@default"/>' and </xsl:if><xsl:choose><xsl:when test="@use = 'optional' or count(@use) = 0">(@use='optional' or count(@use) = 0)]) = 1</xsl:when><xsl:otherwise>@use='<xsl:value-of select="@use"/>']) = 1</xsl:otherwise></xsl:choose></xsl:attribute>#Issue: Checking for the attribute '<xsl:value-of select="@name"/>' declaration. The required attribute declaration does not exist or has duplicate. Maybe the value of the 'type', 'use', or 'default' attributes does not match.
					</xsl:element>
					</xsl:for-each>
				</xsl:if>
				<!-- Checking the existent of the xsd:annotation in xsd:element -->
				<xsl:if test="$checkAnno = 1">
					<xsl:for-each select=".//xsd:element">
						<xsl:choose>
							<xsl:when test="count(@ref) = 1">
								<xsl:element name="assert">
									<xsl:attribute name="test">count(.//xsd:element[@ref = '<xsl:value-of select="@ref"/>']/xsd:annotation) = <xsl:value-of select="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0])"/></xsl:attribute>#Issue: Unmatched number of child 'xsd:annotation' in element reference '<xsl:value-of select="@ref"/>'. Expect <xsl:value-of select="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0])"/>.
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:element name="assert">
									<xsl:attribute name="test">count(.//xsd:element[@name = '<xsl:value-of select="@name"/>']/xsd:annotation) = <xsl:value-of select="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0]) "/></xsl:attribute>#Issue: Unmatched number of child 'xsd:annotation' in element declaration named '<xsl:value-of select="@name"/>'. Expect <xsl:value-of select="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0])"/>.
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:if>
				<!-- The case when it is not an xsd:extension. This is also applicable to the xsd:group -->
				<xsl:if test="count(xsd:complexContent)=0">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:sequence) = <xsl:value-of select="count(xsd:sequence)"/></xsl:attribute>#Issue: Missing or too many of the 'xsd:sequence' element.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:sequence/*[name() != 'xsd:annotation']) = <xsl:value-of select="count(xsd:sequence/*[name() != 'xsd:annotation'])"/></xsl:attribute>#Issue: The number of children except xsd:annotation in the 'xsd:sequence' do not match.
					</xsl:element>
					<!-- Checking for the xsd:element in the xsd:sequence -->
					<xsl:for-each select="xsd:sequence/xsd:element">
						<xsl:choose>
							<xsl:when test="count(@ref) = 1">
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@ref='<xsl:value-of select="@ref"/>']) = 1</xsl:attribute>#Issue: The xsd:element reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Need element '<xsl:value-of select="@ref"/>'.							
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<!-- assuming it is the local element with the name and type attribute. In general this could be element with localized/anonymous complexType as well -->
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@name='<xsl:value-of select="@name"/>' and @type='<xsl:value-of select="@type"/>']) = 1</xsl:attribute>#Issue: The local xsd:element declaration at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Need element name '<xsl:value-of select="@name"/>' and type '<xsl:value-of select="@type"/>'
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="count(@minOccurs) = 0 or @minOccurs = '1'">
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present.
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@minOccurs='<xsl:value-of select="@minOccurs"/>']) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@minOccurs"/>.
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="count(@maxOccurs) = 0 or @maxOccurs = '1'">
								<xsl:element name="assert">
								<!--	<xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match. Needed value is either one or the attribute does not present.
									 07/07/2016: Here I fixed position() in 'The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match.' to use the 'count(preceding-sibling::*[name() != 'xsd:annotation']) + 1', same as other places. -->
									 <xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present.
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='<xsl:value-of select="@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@maxOccurs"/>.
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<!-- Checking for the xsd:group reference in the xsd:sequence -->
					<xsl:for-each select="xsd:sequence/xsd:group">
						<xsl:element name="assert">
							<xsl:attribute name="test">count(xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@ref='<xsl:value-of select="@ref"/>']) = 1</xsl:attribute>#Issue: The xsd:group reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Need an xsd:group reference '<xsl:value-of select="@ref"/>'.
						</xsl:element>
						<xsl:choose>
							<xsl:when test="count(@minOccurs) = 0 or @minOccurs = '1'">
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:group at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present. The source xsd:group name is '<xsl:value-of select="@ref"/>'. 
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@minOccurs='<xsl:value-of select="@minOccurs"/>']) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:group at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@minOccurs"/>. The source xsd:group name is '<xsl:value-of select="@ref"/>'. 
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="count(@maxOccurs) = 0 or @maxOccurs = '1'">
								<xsl:element name="assert">
								<!--	<xsl:attribute name="test">count(xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group at the position <xsl:value-of select="position()"/> does not match. Needed value is either one or the attribute does not present. The source xsd:group name is '<xsl:value-of select="@ref"/>'.
								07/07/2016: Here I fixed position() in 'The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match.' to use the 'count(preceding-sibling::*[name() != 'xsd:annotation']) + 1', same as other places.-->
								<xsl:attribute name="test">count(xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present. The source xsd:group name is '<xsl:value-of select="@ref"/>'.
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:element name="assert">
									<xsl:attribute name="test">count(xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='<xsl:value-of select="@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@maxOccurs"/>. The source xsd:group name is '<xsl:value-of select="@ref"/>'.
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<!-- This loop check the existent of an xsd:choice structure using an existent of a child element either by @name or @ref attribute. This one I am taking the advantage that an xsd:choice within a complexType can be uniquely identified by one of its child element. In other words, choices within a single xsd:complexType cannot contain a child element with the same name (either by @name or @ref). Here I only check the existent of the xsd:choice with that uniqueness. I have a seperate section which checks the detail content of the choice. -->
					<xsl:for-each select=".//xsd:choice">
						<xsl:element name="assert">
							<xsl:choose>
								<xsl:when test="count(current()//xsd:element[count(@name)=1]) &gt;= 1">
									<xsl:attribute name="test">count(.//xsd:choice[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]//xsd:element[@name='<xsl:value-of select="current()//xsd:element/@name"/>']) = 1</xsl:attribute>
									#Issue: Issue with an xsd:choice at position <xsl:value-of select="count(preceding-sibling::*) + 1"/> does not have the required or has too many element name '<xsl:value-of select="current()//xsd:element/@name"/>'.
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="test">count(.//xsd:choice[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]//xsd:element[@ref='<xsl:value-of select="current()//xsd:element/@ref"/>']) = 1</xsl:attribute>
									#Issue: Issue with an xsd:choice at position <xsl:value-of select="count(preceding-sibling::*) + 1"/> does not have the required or has too many element ref  '<xsl:value-of select="current()//xsd:element/@ref"/>'.
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="count(xsd:complexContent)=1">
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:complexContent) = 1</xsl:attribute>#Issue: Missing or too many 'xsd:complexContent'.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:complexContent/xsd:extension) = 1</xsl:attribute>#Issue: Missing or too many child 'xsd:extension' of the 'xsd:complexContent'.
					</xsl:element>
					<xsl:for-each select="xsd:complexContent/xsd:extension/@*">
						<xsl:element name="assert">
							<xsl:attribute name="test">xsd:complexContent/xsd:extension/@<xsl:value-of select="name(current())"/> = '<xsl:value-of select="current()"/>'</xsl:attribute>#Issue: The value of the attribute '<xsl:value-of select="name(current())"/>' in the xsd:complexContent/xsd:extension does not match.
						</xsl:element>
					</xsl:for-each>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/*[name() != 'xsd:annotation']) = <xsl:value-of select="count(xsd:complexContent/xsd:extension/*)"/></xsl:attribute>
						#Issue: The number of children (except xsd:annotation) of the xsd:complexContent/xsd:extension does not match.
					</xsl:element>
					<xsl:element name="assert">
						<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence) = <xsl:value-of select="count(xsd:complexContent/xsd:extension/xsd:sequence)"/></xsl:attribute>
						#Issue: The number of child xsd:sequence element of the xsd:complexContent/xsd:extension does not match.
					</xsl:element>
					<xsl:if test="count(xsd:complexContent/xsd:extension/xsd:sequence)">
						<xsl:for-each select="xsd:complexContent/xsd:extension/xsd:sequence/xsd:element">
							<xsl:choose>
								<xsl:when test="count(@ref) = 1">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@ref='<xsl:value-of select="@ref"/>']) = 1</xsl:attribute>
										#Issue: The xsd:element reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Need element '<xsl:value-of select="@ref"/>'.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<!-- assuming it is the local element with the name and type attribute. In general this could be element with localized/anonymous complexType as well -->
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@name='<xsl:value-of select="@name"/>' and @type='<xsl:value-of select="@type"/>']) = 1</xsl:attribute>
										#Issue: The local xsd:element declaration at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Need element name '<xsl:value-of select="@name"/>' and type '<xsl:value-of select="@type"/>'
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="count(@minOccurs) = 0 or @minOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>
									#Issue: The minOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present.
								</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@minOccurs='<xsl:value-of select="@minOccurs"/>']) = 1</xsl:attribute>
									#Issue: The minOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@minOccurs"/>.
								</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="count(@maxOccurs) = 0 or @maxOccurs = '1'">
									<xsl:element name="assert">
									<!--	<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match. Needed value is either one or the attribute does not present. 
									07/07/2016: Here I fixed position() in 'The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match.' to use the 'count(preceding-sibling::*[name() != 'xsd:annotation']) + 1', same as other places.-->
									<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present.
								</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
									<!--	<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>][@maxOccurs='<xsl:value-of select="@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match. Needed value is <xsl:value-of select="@maxOccurs"/>.
										07/07/2016: Here I fixed position() in 'The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match.' to use the 'count(preceding-sibling::*[name() != 'xsd:annotation']) + 1', same as other places.-->
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:element[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='<xsl:value-of select="@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:element at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@maxOccurs"/>.
								</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
						<xsl:for-each select="xsd:complexContent/xsd:extension/xsd:sequence/xsd:group">
							<xsl:element name="assert">
								<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@ref='<xsl:value-of select="@ref"/>']) = 1</xsl:attribute>
								#Issue: The xsd:group reference at the position <xsl:value-of select="count(preceding-sibling::*) + 1"/> does not match. Need xsd:group reference '<xsl:value-of select="@ref"/>'.
							</xsl:element>
							<xsl:choose>
								<xsl:when test="count(@minOccurs) = 0 or @minOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>
									#Issue: The minOccurs value of the xsd:group reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present. The source xsd:group reference is '<xsl:value-of select="@ref"/>'.
								</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@minOccurs='<xsl:value-of select="@minOccurs"/>']) = 1</xsl:attribute>
									#Issue: The minOccurs value of the xsd:group reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@minOccurs"/>. The source xsd:group reference is '<xsl:value-of select="@ref"/>'.
								</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="count(@maxOccurs) = 0 or @maxOccurs = '1'">
									<xsl:element name="assert">
									<!--	<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group reference at the position <xsl:value-of select="position()"/> does not match. Needed value is either one or the attribute does not present. The source xsd:group reference is '<xsl:value-of select="@ref"/>'.
										07/07/2016: Here I fixed position() in 'The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match.' to use the 'count(preceding-sibling::*[name() != 'xsd:annotation']) + 1', same as other places.-->
									<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is either one or the attribute does not present. The source xsd:group reference is '<xsl:value-of select="@ref"/>'.
								</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
									<!--	<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='<xsl:value-of select="@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group reference at the position <xsl:value-of select="position()"/> does not match. Needed value is <xsl:value-of select="@maxOccurs"/>. The source xsd:group reference is '<xsl:value-of select="@ref"/>'.
										07/07/2016: Here I fixed position() in 'The maxOccurs value of the xsd:element at the position <xsl:value-of select="position()"/> does not match.' to use the 'count(preceding-sibling::*[name() != 'xsd:annotation']) + 1', same as other places.-->
										<xsl:attribute name="test">count(xsd:complexContent/xsd:extension/xsd:sequence/xsd:group[count(preceding-sibling::*[name() != 'xsd:annotation']) = <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation'])"/>][@maxOccurs='<xsl:value-of select="@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:group reference at the position <xsl:value-of select="count(preceding-sibling::*[name() != 'xsd:annotation']) + 1"/> does not match. Needed value is <xsl:value-of select="@maxOccurs"/>. The source xsd:group reference is '<xsl:value-of select="@ref"/>'.										
								</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
						<xsl:for-each select=".//xsd:choice">
							<xsl:element name="assert">
								<xsl:choose>
									<xsl:when test="count(current()//xsd:element[count(@name)=1]) &gt;= 1">
										<xsl:attribute name="test">count(.//xsd:choice[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]//xsd:element[@name='<xsl:value-of select="current()//xsd:element/@name"/>']) = 1</xsl:attribute>
										#Issue: Issue with an xsd:choice at position <xsl:value-of select="count(preceding-sibling::*) + 1"/> does not have the required or has too many element name '<xsl:value-of select="current()//xsd:element/@name"/>'.
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="test">count(.//xsd:choice[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]//xsd:element[@ref='<xsl:value-of select="current()//xsd:element/@ref"/>']) = 1</xsl:attribute>
										#Issue: Issue with an xsd:choice at position <xsl:value-of select="count(preceding-sibling::*) + 1"/> does not have the required or has too many element ref  '<xsl:value-of select="current()//xsd:element/@ref"/>'.
									</xsl:otherwise>
								</xsl:choose>
							</xsl:element>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
			</xsl:element>
			<!-- Here is where I check the detail content of the xsd:choice. -->
			<xsl:if test="count(.//xsd:choice) &gt; 0">
				<xsl:for-each select=".//xsd:choice">
					<xsl:element name="rule">
						<!-- Setting the context -->
						<xsl:choose>
							<xsl:when test="count(.//xsd:element[count(@name) = 1]) &gt;= 1">
								<xsl:choose>
									<xsl:when test="count(ancestor::xsd:complexType) = 1">
										<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="current()/ancestor::xsd:complexType/@name"/>']//xsd:choice[.//xsd:element/@name='<xsl:value-of select="current()//xsd:element/@name"/>']</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="context">xsd:schema/xsd:group[@name = '<xsl:value-of select="current()/ancestor::xsd:group/@name"/>']//xsd:choice[.//xsd:element/@name='<xsl:value-of select="current()//xsd:element/@name"/>']</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:choose>
									<xsl:when test="count(ancestor::xsd:complexType) = 1">
										<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="current()/ancestor::xsd:complexType/@name"/>']//xsd:choice[.//xsd:element/@ref='<xsl:value-of select="current()//xsd:element/@ref"/>']</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="context">xsd:schema/xsd:group[@name = '<xsl:value-of select="current()/ancestor::xsd:group/@name"/>']//xsd:choice[.//xsd:element/@ref='<xsl:value-of select="current()//xsd:element/@ref"/>']</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
						<!-- Checking the xsd:annotation -->
						<xsl:call-template name="PresentOfXsdAnnotationAssertion"/>
						<!-- check the number of child elements matches -->
						<xsl:element name="assert">
							<xsl:attribute name="test">count(./*[name() != 'xsd:annotation']) = <xsl:value-of select="count(current()/*[name() != 'xsd:annotation'])"/></xsl:attribute>#Issue: The number of child elements of the 'xsd:choice' (except xsd:annotation) does not match.
						</xsl:element>
						<!-- for each child sequence -->
						<!-- check the sequence with the particular content exists. -->
						<!-- check the number of children in each sequence  -->
						<xsl:for-each select="current()/xsd:sequence">
							<xsl:element name="assert">
								<xsl:attribute name="test">count(./xsd:sequence[count(./*) = <xsl:value-of select="count(./*)"/><xsl:for-each select="current()/xsd:element[count(@name)=1]"> and count(xsd:element[@name='<xsl:value-of select="current()/@name"/>' and count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/> and @type = '<xsl:value-of select="current()/@type"/>']) = 1
</xsl:for-each><xsl:for-each select="current()/xsd:element[count(@ref)=1]"> and count(xsd:element[@ref='<xsl:value-of select="current()/@ref"/>' and count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]) = 1
</xsl:for-each><xsl:for-each select="current()/xsd:group"> and count(xsd:group[@ref='<xsl:value-of select="current()/@ref"/>' and count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]) = 1
</xsl:for-each><xsl:for-each select="current()/xsd:choice"><xsl:choose><xsl:when test="count(current()//xsd:element[count(@name)=1]) &gt;= 1">
and count(xsd:choice[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]//xsd:element[@name='<xsl:value-of select="current()//xsd:element/@name"/>']) = 1
</xsl:when><xsl:otherwise>
and count(xsd:choice[count(preceding-sibling::*) = <xsl:value-of select="count(preceding-sibling::*)"/>]//xsd:element[@ref='<xsl:value-of select="current()//xsd:element/@ref"/>']) = 1</xsl:otherwise></xsl:choose></xsl:for-each>]) = 1</xsl:attribute>
#Issue: There is an issue with content of the xsd:Choice.
</xsl:element>
						</xsl:for-each>
						<!-- Check the minOccurs and maxOccurs of each descendant xsd:element or xsd:group of the choice. There will be multiple assertions for 
						the xsd:element or xsd:group within the nested choices. Names of xsd:elements or xsd:groups within nested choice are unique so we can just cycle thru them.
						-->
						<xsl:for-each select="current()//xsd:element[count(@name) = 1]">
							<xsl:choose>
								<xsl:when test="count(current()/@minOccurs) = 0 or current()/@minOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:element[@name='<xsl:value-of select="current()/@name"/>'][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:choice's xsd:element name '<xsl:value-of select="current()/@name"/>' may not match. Needed value is either one or the attribute does not present.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:element[@name='<xsl:value-of select="current()/@name"/>'][@minOccurs='<xsl:value-of select="current()/@minOccurs"/>']) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:choice's xsd:element name '<xsl:value-of select="current()/@name"/> may not match. Needed value is <xsl:value-of select="current()/@minOccurs"/>.
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="count(current()/@maxOccurs) = 0 or current()/@maxOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:element[@name='<xsl:value-of select="current()/@name"/>'][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:choice's xsd:element name '<xsl:value-of select="current()/@name"/>' may not match. Needed value is either one or the attribute does not present.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:sequence/xsd:element[@name='<xsl:value-of select="current()/@name"/>'][@maxOccurs='<xsl:value-of select="current()/@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:choice's xsd:element name <xsl:value-of select="current()/@name"/> does not match. Needed value is <xsl:value-of select="current()/@maxOccurs"/>.
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
						<xsl:for-each select="current()//xsd:element[count(@ref) = 1]">
							<xsl:choose>
								<xsl:when test="count(current()/@minOccurs) = 0 or current()/@minOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:element[@ref='<xsl:value-of select="current()/@ref"/>'][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:choice's xsd:element ref '<xsl:value-of select="current()/@ref"/>' may not match. Needed value is either one or the attribute does not present.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:element[@ref='<xsl:value-of select="current()/@ref"/>'][@minOccurs='<xsl:value-of select="current()/@minOccurs"/>']) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:choice's xsd:element ref '<xsl:value-of select="current()/@ref"/> may not match. Needed value is <xsl:value-of select="current()/@minOccurs"/>.
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="count(current()/@maxOccurs) = 0 or current()/@maxOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:element[@ref='<xsl:value-of select="current()/@ref"/>'][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:choice's xsd:element ref '<xsl:value-of select="current()/@ref"/>' may not match. Needed value is either one or the attribute does not present.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:sequence/xsd:element[@ref='<xsl:value-of select="current()/@ref"/>'][@maxOccurs='<xsl:value-of select="current()/@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:choice's xsd:element ref <xsl:value-of select="current()/@ref"/> may not match. Needed value is <xsl:value-of select="current()/@maxOccurs"/>.
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
						<xsl:for-each select="current()//xsd:group">
							<xsl:choose>
								<xsl:when test="count(current()/@minOccurs) = 0 or current()/@minOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:group[@ref='<xsl:value-of select="current()/@ref"/>'][(@minOccurs='1' or count(@minOccurs)=0)]) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:choice's xsd:group ref '<xsl:value-of select="current()/@ref"/>' may not match. Needed value is either one or the attribute does not present.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:group[@ref='<xsl:value-of select="current()/@ref"/>'][@minOccurs='<xsl:value-of select="current()/@minOccurs"/>']) = 1</xsl:attribute>#Issue: The minOccurs value of the xsd:choice's xsd:group ref '<xsl:value-of select="current()/@ref"/> may not match. Needed value is <xsl:value-of select="current()/@minOccurs"/>.
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="count(current()/@maxOccurs) = 0 or current()/@maxOccurs = '1'">
									<xsl:element name="assert">
										<xsl:attribute name="test">count(.//xsd:group[@ref='<xsl:value-of select="current()/@ref"/>'][@maxOccurs='1' or count(@maxOccurs)=0]) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:choice's xsd:group ref '<xsl:value-of select="current()/@ref"/>' may not match. Needed value is either one or the attribute does not present.
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="assert">
										<xsl:attribute name="test">count(xsd:sequence/xsd:group[@ref='<xsl:value-of select="current()/@ref"/>'][@maxOccurs='<xsl:value-of select="current()/@maxOccurs"/>']) = 1</xsl:attribute>#Issue: The maxOccurs value of the xsd:choice's xsd:group ref <xsl:value-of select="current()/@ref"/> may not match. Needed value is <xsl:value-of select="current()/@maxOccurs"/>.
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
						<!-- I haven't check for the choice cardinality ... oh i kinda assume that nothing should be specified. -->
					</xsl:element>
				</xsl:for-each>
			</xsl:if>
		</xsl:element>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template name="text" match="text()"/>
	<xsl:template name="CheckLocalElementAndElementRefByID" match="xsd:element[local-name(..) != 'schema' and namespace-uri(..) = 'http://www.w3.org/2001/XMLSchema']">
		<xsl:element name="pattern">
			<!-- This needs a separate rule which checks that all xsd:element has an @id attribute. This is done in the globalComplexTypeAndGroup template. -->
			<xsl:attribute name="name">Investigating the content of the local xsd:element and xsd:element reference hinging on the @id attribute</xsl:attribute>
			<xsl:element name="rule">
				<xsl:attribute name="context"><xsl:value-of select="replace(replace(path(.), 'Q\{http://www.w3.org/2001/XMLSchema\}', 'xsd:'), '\[\d+\]', '')"/>[@id = '<xsl:value-of select="@id"/>']</xsl:attribute>
				<xsl:if test="count(@ref) = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">@ref = '<xsl:value-of select="@ref"/>'</xsl:attribute>#Issue: The @ref of the xsd:element with @id = '<xsl:value-of select="@id"/>' does not match.
					</xsl:element>				
				</xsl:if>
				<xsl:if test="count(@name) = 1">
					<xsl:element name="assert">
						<xsl:attribute name="test">@name = '<xsl:value-of select="@name"/>' and @type = '<xsl:value-of select="@type"/>'</xsl:attribute>#Issue: The @name and @type of the xsd:element with @id = '<xsl:value-of select="@id"/>' do not match.
					</xsl:element>				
				</xsl:if>
				<xsl:if test="count(@nillable) = 1">
					<xsl:element name="assert">
						<xsd:attribute name="test">
							<xsl:choose>
								<xsl:when test="@nillable = 'false'">count(@nillable) = 0 or @nillable = 'false'</xsl:when>
								<xsl:otherwise>@nillable = 'true'</xsl:otherwise>
							</xsl:choose>
						</xsd:attribute>	
					</xsl:element>				
				</xsl:if>
				<xsl:if test="count(@default) = 1">
					<xsl:element name="assert">
						<xsd:attribute name="test">@default = '<xsl:value-of select="@default"/>'</xsd:attribute>	
					</xsl:element>				
				</xsl:if>	
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<!--Annotation related assertions-->
	<xsl:template name="annotation" match="xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0]">
		<xsl:if test="$checkAnno = 1">
			<xsl:element name="pattern">
				<xsl:if test="count(parent::xsd:schema) = 1">
					<xsl:attribute name="name">Root annotation</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:schema/xsd:annotation</xsl:attribute>
						<xsl:call-template name="AnnotationChildrenAssertions"/>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:if>
				<xsl:if test="count(parent::xsd:complexType) = 1">
					<xsl:attribute name="name"><xsl:value-of select="../@name"/> annotation</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:schema/xsd:complexType[@name='<xsl:value-of select="../@name"/>']/xsd:annotation</xsl:attribute>
						<xsl:call-template name="AnnotationChildrenAssertions"/>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:if>
				<xsl:if test="count(parent::xsd:group/@name) = 1">
					<xsl:attribute name="name">Annotation of a global xsd:group '<xsl:value-of select="../@name"/>'</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:schema/xsd:group[@name='<xsl:value-of select="../@name"/>']/xsd:annotation</xsl:attribute>
						<xsl:call-template name="AnnotationChildrenAssertions"/>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:if>
				<xsl:if test="count(parent::xsd:group/@ref) = 1">
					<xsl:attribute name="name">Annotation of the xsd:group reference '<xsl:value-of select="../@ref"/>' of the ComplexType '<xsl:value-of select="ancestor::xsd:complexType/@name"/>'</xsl:attribute>
					<xsl:element name="rule">
						<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="ancestor::xsd:complexType/@name"/>']//xsd:group[@ref='<xsl:value-of select="../@ref"/>']/xsd:annotation</xsl:attribute>
						<xsl:call-template name="AnnotationChildrenAssertions"/>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:if>
				<xsl:if test="count(parent::xsd:choice) = 1">
					<xsl:attribute name="name">Annotation of an xsd:choice in the complexType '<xsl:value-of select="ancestor::xsd:complexType/@name"/>'</xsl:attribute>
					<xsl:element name="rule">
						<xsl:choose>
							<xsl:when test="count(..//xsd:element[count(@name)=1]) &gt;= 1">
								<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="ancestor::xsd:complexType/@name"/>']//xsd:choice[//xsd:element[@name='<xsl:value-of select="..//xsd:element/@name"/>']]/xsd:annotation</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="ancestor::xsd:complexType/@name"/>']//xsd:choice[//xsd:element[@ref='<xsl:value-of select="..//xsd:element/@ref"/>']]/xsd:annotation</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:call-template name="AnnotationChildrenAssertions"/>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:if>
				<xsl:if test="count(parent::xsd:element) = 1">
					<xsl:choose>
						<xsl:when test="count(../../../xsd:schema) = 1">
							<xsl:attribute name="name">Annotation of the global element '<xsl:value-of select="../@name"/>' </xsl:attribute>
							<xsl:element name="rule">
								<xsl:attribute name="context">xsd:schema/xsd:element[@name='<xsl:value-of select="../@name"/>']/xsd:annotation</xsl:attribute>
								<xsl:call-template name="AnnotationChildrenAssertions"/>
								<xsl:apply-templates/>
							</xsl:element>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="count(../@name) = 1">
									<xsl:attribute name="name">Annotation of the local element declaration '<xsl:value-of select="../@name"/>' of the ComplexType '<xsl:value-of select="ancestor::xsd:complexType/@name"/>'</xsl:attribute>
									<xsl:element name="rule">
										<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="ancestor::xsd:complexType/@name"/>']//xsd:element[@name='<xsl:value-of select="../@name"/>']/xsd:annotation</xsl:attribute>
										<xsl:call-template name="AnnotationChildrenAssertions"/>
										<xsl:apply-templates/>
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<!-- The case of element reference -->
									<xsl:attribute name="name">Annotation of the local element reference '<xsl:value-of select="../@ref"/>' of the ComplexType '<xsl:value-of select="ancestor::xsd:complexType/@name"/>'</xsl:attribute>
									<xsl:element name="rule">
										<xsl:attribute name="context">xsd:schema/xsd:complexType[@name = '<xsl:value-of select="ancestor::xsd:complexType/@name"/>']//xsd:element[@ref='<xsl:value-of select="../@ref"/>']/xsd:annotation</xsl:attribute>
										<xsl:call-template name="AnnotationChildrenAssertions"/>
										<xsl:apply-templates/>
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	<xsl:template name="AnnotationChildrenAssertions">
		<xsl:param name="node"/>
		<!-- Taking this out because of the introduction of the xsd:appinfo/svcdoc:Depreated element renders this not applicable
		<xsl:element name="assert">
			<xsl:attribute name="test">count(child::*) = <xsl:value-of select="count(child::*)"/></xsl:attribute>#Issue: The number of child elements do not match.
		</xsl:element>
		-->
		<xsl:element name="assert">
			<xsl:attribute name="test">count(child::xsd:documentation) =<xsl:value-of select="count(child::xsd:documentation)"/></xsl:attribute>#Issue: The number of child 'xsd:documentation' elements do not match.
		</xsl:element>
		<xsl:element name="assert">
			<xsl:attribute name="test">count(child::xsd:appinfo) = <xsl:value-of select="count(child::xsd:appinfo[count(svcdoc:Deprecated) = 0])"/></xsl:attribute>#Issue: The number of child 'xsd:appinfo' elements do not match.
		</xsl:element>
	</xsl:template>
	<xsl:template name="XsdDocumentation" match="xsd:documentation">
		<xsl:choose>
			<xsl:when test="count(svcdoc:EBO) = 1">
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO) = 1</xsl:attribute>#Issue: Missing the 'xsd:documentation/svcdoc:EBO' element.
				</xsl:element>
				<xsl:element name="assert">
					<xsl:attribute name="test">string-length(normalize-space(xsd:documentation/child::text())) = 0</xsl:attribute>#Issue: Found unexpected text content in the 'xsd:documenation', only expect a child 'svcdoc:EBO' element.
				</xsl:element>
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="count(xsd:documentation/*) > 0">
					<xsl:element name="assert">
						<xsl:attribute name="test">false</xsl:attribute>#Something wrong: I do not expect a child element in this 'xsd:documentation' in my program.
					</xsl:element>
				</xsl:if>
				<xsl:element name="assert">
					<xsl:attribute name="test">count(xsd:documentation/*) = 0</xsl:attribute>#Issue: Do not expect any child element here.
				</xsl:element>
				<xsl:element name="assert">
					<xsl:attribute name="test">normalize-space(xsd:documentation/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(./text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the xsd:documentation does not match.
				</xsl:element>
				<xsl:apply-templates/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="svcdocEBO" match="svcdoc:EBO">
		<xsl:element name="assert">
			<xsl:attribute name="test">count(child::*) = <xsl:value-of select="count(../*)"/></xsl:attribute>#Issue: The number of child elements of the svcdoc:EBO does not match.
		</xsl:element>
		<xsl:if test="count(svcdoc:Description) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO/svcdoc:Description) = 1</xsl:attribute>#Issue: Missing or too many 'svcdoc:Description' elements in the 'xsd:documentation/svcdoc:EBO'.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:documentation/svcdoc:EBO/svcdoc:Description/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(svcdoc:Description/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:documentation/svcdoc:EBO/svcdoc:Description' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(svcdoc:Type) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO/svcdoc:Type) = 1</xsl:attribute>#Issue: Missing or too many 'svcdoc:Type' elements in the 'xsd:documentation/svcdoc:EBO'.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:documentation/svcdoc:EBO/svcdoc:Type/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(svcdoc:Type/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:documentation/svcdoc:EBO/svcdoc:Type' does not match.
			</xsl:element>
		</xsl:if>
		<!-- Replace with the svcdoc:Industry and the svcdoc:BusinessContext blocks below, because the svcdoc:Industry is replaced by the svcdoc:BusinessContext. The svcdoc:Industry block just below is supporting the transition from the svcdoc:Industry to the svcdoc:BusinessContext. That is when the script sees the svcdoc:Industry it checks the content of the svcdocBusinessContext instead.
		<xsl:if test="count(svcdoc:Industry) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO/svcdoc:Industry) = 1</xsl:attribute>#Issue: Missing or too many 'svcdoc:Industry' elements in the 'xsd:documentation/svcdoc:EBO'.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:documentation/svcdoc:EBO/svcdoc:Industry/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(svcdoc:Industry/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:documentation/svcdoc:EBO/svcdoc:Industry' does not match.
			</xsl:element>
		</xsl:if>
-->
		<xsl:if test="count(svcdoc:Industry) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO/svcdoc:BusinessContext) = 1</xsl:attribute>#Issue: Missing or too many 'svcdoc:BusinessContext' elements in the 'xsd:documentation/svcdoc:EBO'.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:documentation/svcdoc:EBO/svcdoc:BusinessContext/text()) = <xsl:choose><xsl:when test="string-length(normalize-space(svcdoc:Industry/text())) = 0">'Core'</xsl:when><xsl:otherwise><xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(svcdoc:Industry/text())"/></xsl:with-param></xsl:call-template></xsl:otherwise></xsl:choose></xsl:attribute>#Issue: Text content of the 'xsd:documentation/svcdoc:EBO/svcdoc:BusinessContext' does not match with the source value '<xsl:choose><xsl:when test="string-length(normalize-space(svcdoc:Industry/text())) = 0">'Core</xsl:when><xsl:otherwise><xsl:value-of select="normalize-space(svcdoc:Industry/text())"/></xsl:otherwise></xsl:choose>' in the 'xsd:documentation/svcdoc:EBO/svcdoc:IndustryContext'.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(svcdoc:BusinessContext) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO/svcdoc:BusinessContext) = 1</xsl:attribute>#Issue: Missing or too many 'svcdoc:BusinessContext' elements in the 'xsd:documentation/svcdoc:EBO'.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:documentation/svcdoc:EBO/svcdoc:BusinessContext/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(svcdoc:BusinessContext/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:documentation/svcdoc:EBO/svcdoc:BusinessContext' does not match with the source value '<xsl:value-of select="normalize-space(svcdoc:BusinessContext/text())"/>'.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(svcdoc:EBOName) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:documentation/svcdoc:EBO/svcdoc:EBOName) = 1</xsl:attribute>#Issue: Missing or too many 'svcdoc:EBO' elements in the 'xsd:documentation/svcdoc:EBO'.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:documentation/svcdoc:EBO/svcdoc:EBOName/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(svcdoc:EBOName/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:documentation/svcdoc:EBO/svcdoc:EBOName' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template name="xsdAppInfo" match="xsd:appinfo[count(svcdoc:Deprecated) = 0]">
		<xsl:element name="assert">
			<xsl:attribute name="test">count(xsd:appinfo/child::*) = <xsl:value-of select="count(../xsd:appinfo[count(svcdoc:Deprecated) = 0]/child::*)"/></xsl:attribute>#Issue: The number of child elements of the xsd:appinfo does not match. Expect <xsl:value-of select="count(../xsd:appinfo[count(svcdoc:Deprecated) = 0]/child::*)"/>.
		</xsl:element>
		<xsl:if test="count(ebocontext:BusinessContext) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:BusinessContext) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:Business' elements in the 'xsd:appinfo'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:BusinessContext/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:BusinessContext/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:BusinessContext' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(ebocontext:DerivationHierarchy) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivationHierarchy) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:DerivationHierarchy' elements in the 'xsd:appinfo'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivationHierarchy/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:DerivationHierarchy/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivationHierarchy' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(ebocontext:DerivedBusinessContext) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivedBusinessContext) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:DerivedBusinessContext' elements in the 'xsd:appinfo'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivedBusinessContext/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:DerivedBusinessContext/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivedBusinessContext' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(ebocontext:EBOContext) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:EBOContext) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:EBOContext' elements in the 'xsd:appinfo'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:EBOContext/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:EBOContext/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:EBOContext' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="ebocontext:DerivationHierarchy">
		<xsl:element name="assert">
			<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivationHierarchy/*) = <xsl:value-of select="count(child::*)"/></xsl:attribute>#Issue: The xsd:appinfo/ebocontext:DerivationHierarchy has unmatched number of child elements. Expect <xsl:value-of select="count(child::*)"/>.
		</xsl:element>
		<xsl:if test="count(ebocontext:DerivationBase) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:DerivationBase' elements in the 'xsd:appinfo/ebocontext:DerivationHierarchy'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:DerivationBase/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="ebocontext:DerivationBase">
		<xsl:element name="assert">
			<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/*) = <xsl:value-of select="count(child::*)"/></xsl:attribute>#Issue: The xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase has unmatched number of child elements. Expect <xsl:value-of select="count(child::*)"/>.
		</xsl:element>
		<xsl:if test="count(ebocontext:BusinessContext) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/ebocontext:BusinessContext) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:BusinessContext' elements in the 'xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/ebocontext:BusinessContext/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:BusinessContext/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/ebocontext:BusinessContext' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(ebocontext:Sequence) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/ebocontext:Sequence) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:Sequence' elements in the 'xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/ebocontext:Sequence/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:Sequence/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivationHierarchy/ebocontext:DerivationBase/ebocontext:Sequence' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="ebocontext:DerivedBusinessContext">
		<xsl:element name="assert">
			<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivedBusinessContext/*) = <xsl:value-of select="count(child::*)"/></xsl:attribute>#Issue: The xsd:appinfo/ebocontext:DerivedBusinessContext has unmatched number of child elements. Expect <xsl:value-of select="count(child::*)"/>.
		</xsl:element>
		<xsl:if test="count(ebocontext:BusinessContext) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivedBusinessContext/ebocontext:BusinessContext) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:BusinessContext' elements in the 'xsd:appinfo/ebocontext:DerivedBusinessContext'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivedBusinessContext/ebocontext:BusinessContext/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:BusinessContext/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivedBusinessContext/ebocontext:BusinessContext' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:if test="count(ebocontext:DerivationTypeCode) = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:appinfo/ebocontext:DerivedBusinessContext/ebocontext:DerivationTypeCode) = 1</xsl:attribute>#Issue: Missing or too many 'ebocontext:DerivationTypeCode' elements in the 'xsd:appinfo/ebocontext:DerivedBusinessContext'. Expect only one.
			</xsl:element>
			<xsl:element name="assert">
				<xsl:attribute name="test">normalize-space(xsd:appinfo/ebocontext:DerivedBusinessContext/ebocontext:DerivationTypeCode/text()) = <xsl:call-template name="getTextComparisonExpression"><xsl:with-param name="txt"><xsl:value-of select="normalize-space(ebocontext:DerivationTypeCode/text())"/></xsl:with-param></xsl:call-template></xsl:attribute>#Issue: Text content of the 'xsd:appinfo/ebocontext:DerivedBusinessContext/ebocontext:DerivationTypeCode' does not match.
			</xsl:element>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template name="PresentOfXsdAnnotationAssertion">
		<xsl:if test="count(xsd:annotation[count(xsd:documentation) &gt; 0 or count(xsd:appinfo[count(svcdoc:Deprecated) = 0]) &gt; 0]) &gt; 0 and $checkAnno = 1">
			<xsl:element name="assert">
				<xsl:attribute name="test">count(xsd:annotation) = <xsl:value-of select="count(xsd:annotation)"/></xsl:attribute>#Issue: Unmatched number of child 'xsd:annotation'. Expect <xsl:value-of select="count(xsd:annotation)"/>.
				</xsl:element>
		</xsl:if>
	</xsl:template>
	<xsl:template name="getTextComparisonExpression">
		<xsl:param name="txt"/>
		<xsl:choose>
			<!--When the text contains the double quote, use apos to delimit-->
			<xsl:when test="contains(normalize-space($txt), '&quot;' )">'<xsl:value-of select="normalize-space($txt)"/>'</xsl:when>
			<xsl:otherwise>"<xsl:value-of select="normalize-space($txt)"/>"</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
