<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes"
		omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:template match="/">
		<xsl:apply-templates
			select="//div[@id='content']/div[@class='post clearfix']/div[@class='post-content']" />
	</xsl:template>

	<!-- Default: remove node with children -->
	<xsl:template
		match="@style|@font|@color|@background|a[contains(@rel,'bookmark')]">
	</xsl:template>

	<!-- Default: skip node but children -->
	<xsl:template match="font|a[@href='#']">
		<xsl:apply-templates select="node()" />
	</xsl:template>

	<xsl:template
		match="div[contains(@class,'post-meta')]//a|div[contains(@class,'post-tags')]//a">
		<sapn class="a-href" data-href="{@href}">
			<xsl:apply-templates select="@*|node()" />
		</sapn>
	</xsl:template>

	<!-- Default: copy everything -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>