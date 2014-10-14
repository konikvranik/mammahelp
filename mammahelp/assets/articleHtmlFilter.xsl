<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />
	<xsl:strip-space elements="*" />

	<xsl:template match="/">
		<xsl:apply-templates select="//div[@id='content']/article" />
	</xsl:template>

	<xsl:template match="div[@id='content']/article">
		<xsl:copy>
			<xsl:apply-templates select="//div[@id='title']/div[@class='container']//h1" />
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- Default: remove node with children -->
	<xsl:template
		match="@style|@font|@color|@background|a[contains(@rel,'bookmark')]">
	</xsl:template>

	<!-- Default: skip node but children -->
	<xsl:template match="font|a[@href='#']">
		<xsl:apply-templates select="node()" />
	</xsl:template>

	<!-- Default: copy everything -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>