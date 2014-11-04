<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<locations>
			<xsl:apply-templates
				select="//*[@id='middle']/div[@class='middle-in']/table" />
		</locations>
	</xsl:template>

	<xsl:template match="table">
		<xsl:apply-templates select="tr" />
	</xsl:template>

	<xsl:template match="tr">
		<location>
			<type>mamo</type>
			<name>
				<xsl:apply-templates select="td[1]/strong/child::node()"
					mode="html" />
			</name>
			<description>
				<p>
					<xsl:apply-templates select="td[1]/strong/following-sibling::node()"
						mode="html" />
				</p>
				<h3>Objednací doba</h3>
				<p>
					<xsl:apply-templates select="td[2]" mode="html" />
				</p>
			</description>
			<url>
				<xsl:apply-templates select="td[4]/a[text() = 'www']/@href" />
			</url>
			<location>
				<mAddressLines>
					<entry>
						<integer>0</integer>
						<string>
							<xsl:apply-templates select="td[3]/br/preceding-sibling::node()" />
						</string>
					</entry>
				</mAddressLines>
				<mMaxAddressLineIndex>0</mMaxAddressLineIndex>
				<mExtras>
					<mMap>
						<entry>
							<string>double</string>
							<object class="java.lang.Double">545.546</object>
						</entry>
						<entry>
							<string>string</string>
							<object class="java.lang.String">value</object>
						</entry>
						<entry>
							<string>boolean</string>
							<object class="java.lang.Boolean">true</object>
						</entry>
					</mMap>
					<mHasFds>false</mHasFds>
					<mFdsKnown>true</mFdsKnown>
					<mAllowFds>true</mAllowFds>
				</mExtras>
				<mUrl>Url</mUrl>
				<mLocale>cs_CZ</mLocale>
				<mLocality>
					<xsl:apply-templates select="td[3]/strong/child::node()"
						mode="html" />
				</mLocality>
				<mPhone>phone</mPhone>
				<mPostalCode>
					<xsl:value-of
						select="normalize-space(td[3]/br/following-sibling::node()[count(preceding-sibling::strong) = 0])" />
				</mPostalCode>
			</location>
		</location>
	</xsl:template>

	<xsl:template match="img"></xsl:template>
	<xsl:template match="img" mode="html"></xsl:template>

	<xsl:template match="node()|@*" mode="html">
		<xsl:copy>
			<xsl:apply-templates select="child::node()|@*"
				mode="html" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>