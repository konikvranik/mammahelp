<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xhtml="http://www.w3.org/1999/xhtml">

	<!-- remove cnxml specific content from html -->

	<xsl:output method="text" encoding="UTF-8" indent="yes" />

	<xsl:strip-space elements="*" />

	<xsl:template match="br|xhtml:br">
		<xsl:text>&#10;</xsl:text>
	</xsl:template>



	<xsl:template match="p|div|h1|h2|h3|h4|h5|h6|ul|ol">
		<xsl:apply-templates />
		<xsl:text>&#10;</xsl:text>
	</xsl:template>

	<xsl:template match="ol/li">
		<xsl:value-of select="position()" />
		<xsl:text>) </xsl:text>
		<xsl:apply-templates />
		<xsl:text>&#10;</xsl:text>
	</xsl:template>

	<xsl:template match="ul/li">
		<xsl:text>* </xsl:text>
		<xsl:apply-templates />
		<xsl:text>&#10;</xsl:text>
	</xsl:template>

</xsl:stylesheet>