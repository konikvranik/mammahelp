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
		<xsl:apply-templates select="tr[1]/following-sibling::tr" />
	</xsl:template>

	<xsl:template match="tr">
		<location>
			<type>screning</type>
			<name>
				<xsl:call-template name="cdata-start" />
				<xsl:apply-templates select="td[1]/strong/child::node()"
					mode="html" />
				<xsl:call-template name="cdata-end" />
			</name>
			<description>
				<xsl:call-template name="cdata-start" />
				<p>
					<xsl:apply-templates
						select="td[1]/strong/following-sibling::br[1]/following-sibling::node()"
						mode="html" />
				</p>
				<h3>Objednací doba</h3>
				<p>
					<xsl:apply-templates select="td[2]/child::node()"
						mode="html" />
				</p>
				<xsl:call-template name="cdata-end" />
			</description>
			<url>
				<xsl:apply-templates select="td[4]/a[text() = 'www']/@href" />
			</url>


			<address>
				<xsl:call-template name="cdata-start" />
				<!-- <xsl:value-of select="td[1]/strong/child::node()" />, -->
				<xsl:for-each select="td[3]//text()">
					<xsl:value-of select="." />
					<xsl:if test="not(position()=last())">
						<xsl:text>, </xsl:text>
					</xsl:if>
				</xsl:for-each>
				<xsl:call-template name="cdata-end" />
			</address>
			<location>
				<mAddressLines>
					<entry>
						<integer>0</integer>
						<string>
							<xsl:apply-templates select="td[3]/br/preceding-sibling::node()" />
						</string>
					</entry>
				</mAddressLines>
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
				<mUrl>
					<xsl:value-of select="normalize-space(td[4]/a[. = 'www']/@href)" />
				</mUrl>
				<mLocale>cs_CZ</mLocale>
				<mLocality>
					<xsl:call-template name="cdata-start" />
					<xsl:apply-templates select="td[3]/strong/child::node()"
						mode="html" />
					<xsl:call-template name="cdata-end" />
				</mLocality>
				<mPhone>
					<xsl:for-each select="td[4]/text()">
						<xsl:value-of select="." />
						<xsl:if test="not(position()=last())">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
				</mPhone>
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

	<xsl:template name="cdata-start">
		<xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA]]>[</xsl:text>
	</xsl:template>
	<xsl:template name="cdata-end">
		<xsl:text disable-output-escaping="yes">]<![CDATA[]>]]></xsl:text>
	</xsl:template>
</xsl:stylesheet>