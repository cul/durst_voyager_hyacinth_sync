<?xml version="1.0" encoding="utf-8"?>
<!--

  JAFER Toolkit Project.
  Copyright (C) 2002, JAFER Toolkit Project, Oxford University.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.loc.gov/mods/"
                xmlns:oai_marc="http://www.openarchives.org/OAI/1.1/oai_marc" exclude-result-prefixes="oai_marc">

<!-- Adapted for MODS Schema: v3.0,  by Adrian - May 2005 for MDC II project -->

<!-- Based on MODS Schema: Draft v1.0 R1,  March 8, 2002
        using MODS to MARC mapping: Draft, April 9, 2002  -->

<!-- R indicates repeatable field, NR indicates non-repeatable field. -->
<!-- The positions given in the MARC documentation for fixed field elements are increased by 2
     for OAI formatted records, as the data in OAI fixed fields are enclosed in quotes. -->

<xsl:template match="oai_marc:oai_marc">

  <mods xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/ http://www.loc.gov/standards/mods/mods.xsd">

    <xsl:variable name="field001" select="oai_marc:fixfield[@id='001']"/>
    <xsl:variable name="field007" select="oai_marc:fixfield[@id='007']"/>
    <xsl:variable name="field008" select="oai_marc:fixfield[@id='008']"/>

    <xsl:variable name="materialType">
    <!-- "type" attribute (@type) of OAI record is equivalent to MARC LDR/6 -->
    <!-- "level" attribute (@level) of OAI record is equivalent to MARC LDR/7 -->
      <xsl:choose>
        <xsl:when test="@type = 'a' or @type = 't'">book</xsl:when>
        <xsl:when test="@type = 'm'">computer file</xsl:when>
        <xsl:when test="@type = 'e' or @type = 'f'">map</xsl:when>
        <xsl:when test="@type = 'c' or @type = 'd' or @type = 'i' or @type = 'j'">music</xsl:when>
        <xsl:when test="@type = 'g' or @type = 'k' or @type = 'r'">visual material</xsl:when>
        <xsl:when test="@type = 'g'">mixed material</xsl:when>
        <xsl:when test="@level = 'b' or @level = 's'">serial</xsl:when>
      </xsl:choose>
    </xsl:variable>



<!--  ********** Titles ********** -->
  <!-- 245, 210, 242, 246, 240, 130 -->

	<titleInfo>
  <xsl:for-each select="oai_marc:varfield[@id='245']"><!-- 245 processed separately, to ensure it is the first "title" element in output document -->
    <xsl:call-template name="title">
      <xsl:with-param name="type" select="@id"/>
    </xsl:call-template>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='210' or @id='242' or @id='246' or @id='240' or @id='130']">
    <xsl:call-template name="title">
     <xsl:with-param name="type" select="@id"/>
    </xsl:call-template>
  </xsl:for-each>
	</titleInfo>



<!--  **********  Names ********** -->
  <!-- (100 700) (110 710) (111 711) 720 -->

  <xsl:for-each select="oai_marc:varfield[@id='100' or @id='110' or @id='111' or @id='700' or @id='710' or @id='711']">
    <xsl:call-template name="name">
     <xsl:with-param name="type" select="@id"/>
    </xsl:call-template>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='720']"><!-- R  -->
    <name type="uncontrolled">
      <xsl:value-of select="."/>
    </name>
  </xsl:for-each>


<!--  ********** Resource Type ********** -->
    <!-- LDR/6 (and LDR/7)-->

  <typeOfResource>
    <xsl:if test="@level = 'c'">
      <xsl:attribute name="collection">yes</xsl:attribute>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="@type = 'a'">text</xsl:when>
      <xsl:when test="@type = 't'"><xsl:attribute name="manuscript">yes</xsl:attribute>text</xsl:when>
      <xsl:when test="@type = 'e'">cartographic</xsl:when>
      <xsl:when test="@type = 'f'"><xsl:attribute name="manuscript">yes</xsl:attribute>cartographic</xsl:when>
      <xsl:when test="@type = 'c'">notated music</xsl:when>
      <xsl:when test="@type = 'd'"><xsl:attribute name="manuscript">yes</xsl:attribute>notated music</xsl:when>
      <xsl:when test="@type = 'i' or @type = 'j'">sound recording</xsl:when>
      <xsl:when test="@type = 'k'">still image</xsl:when>
      <xsl:when test="@type = 'g'">moving image</xsl:when>
      <xsl:when test="@type = 'r'">three dimensional object</xsl:when>
      <xsl:when test="@type = 'm'">software, multimedia</xsl:when>
      <xsl:when test="@type = 'p'"><xsl:attribute name="manuscript">yes</xsl:attribute>mixed material</xsl:when>
    </xsl:choose>
  </typeOfResource>


<!--  Genre  -->
  <!-- 008/26, 008/33, 008/29, 008/30, 008/24+, 008/21+, 008/25, 655 -->
    <!-- "authority" attribute: value from list at www.loc.gov/marc/sourcecode/genre -->
    <xsl:variable name="position24" select="substring($field008, 26 , 1)"/><!-- 008/24 -->
    <xsl:variable name="position29" select="substring($field008, 31 , 1)"/><!-- 008/29 -->

    <xsl:if test="$materialType ='book' or $materialType ='serial'"><!-- Other types not done... -->
  <genre>
      <xsl:choose>
        <xsl:when test="$position24 = 'a'">abstract or summary</xsl:when>
        <xsl:when test="$position24 = 'b'">bibliography</xsl:when>
        <xsl:when test="$position24 = 'c'">catalog</xsl:when>
        <xsl:when test="$position24 = 'd'">dictionary</xsl:when>
        <xsl:when test="$position24 = 'r'">directory</xsl:when>
        <xsl:when test="$position24 = 'k'">discography</xsl:when>
        <xsl:when test="$position24 = 'e'">encyclopedia</xsl:when>
        <xsl:when test="$position24 = 'q'">filmography</xsl:when>
        <xsl:when test="$position24 = 'f'">handbook</xsl:when>
        <xsl:when test="$position24 = 'i'">index</xsl:when>
        <xsl:when test="$position24 = 'w'">law report or digest</xsl:when>
        <xsl:when test="$position24 = 'g'">legal article</xsl:when>
        <xsl:when test="$position24 = 'v'">legal case and case notes</xsl:when>
        <xsl:when test="$position24 = 'l'">legislation</xsl:when>
        <xsl:when test="$position24 = 'p'">programmed text</xsl:when>
        <xsl:when test="$position24 = 'o'">review</xsl:when>
        <xsl:when test="$position24 = 's'">statistics</xsl:when>
        <xsl:when test="$position24 = 'n'">survey of literature</xsl:when>
        <xsl:when test="$position24 = 't'">technical report</xsl:when>
        <xsl:when test="$position24 = 'm'">theses</xsl:when>
        <xsl:when test="$position24 = 'z'">treaty</xsl:when>
        <xsl:when test="$position29 = '1'">conference publication</xsl:when>
      </xsl:choose>
		</genre>
    </xsl:if>

    <xsl:variable name="position30" select="substring($field008, 32 , 1)"/> <!-- 008/30 -->
    <xsl:variable name="position34" select="substring($field008, 36 , 1)"/> <!-- 008/34 -->

    <xsl:if test="$materialType ='book'">
  <genre>
      <xsl:choose>
        <xsl:when test="$position24 = 'j'">patent</xsl:when>
        <xsl:when test="$position30 = '1'">festschrift</xsl:when>
        <xsl:when test="$position34 = 'a'">biography</xsl:when>
        <xsl:when test="$position34 = 'b'">biography</xsl:when>
        <xsl:when test="$position34 = 'c'">biography</xsl:when>
        <xsl:when test="$position34 = 'd'">biography</xsl:when>
      </xsl:choose>
	</genre>
			</xsl:if>

    <xsl:variable name="position21" select="substring($field008, 23 , 1)"/><!-- 008/21 -->

    <xsl:if test="$materialType ='serial'">
  <genre>
      <xsl:choose>
        <xsl:when test="$position21 = 'd'">database</xsl:when>
        <xsl:when test="$position21 = 'l'">looseleaf</xsl:when>
        <xsl:when test="$position21 = 'n'">newspaper</xsl:when>
        <xsl:when test="$position21 = 'p'">periodical</xsl:when>
        <xsl:when test="$position21 = 'm'">series</xsl:when>
        <xsl:when test="$position21 = 'w'">website</xsl:when>
      </xsl:choose>
  </genre>
    </xsl:if>

  <xsl:for-each select="oai_marc:varfield[@id='655']">
    <genre authority="{oai_marc:subfield[@label = '2']}">
      <xsl:for-each select="oai_marc:subfield[@label = 'a' or @label = 'b' or @label = 'v' or @label = 'x' or @label = 'y' or @label = 'z']">
        <xsl:value-of select="."/><xsl:text>   </xsl:text>
      </xsl:for-each>
    </genre>
  </xsl:for-each>


<!--  ********** Publication ********** -->
  <!-- (044 or 008/15-17),  260 $a, 260 $b, 250, LDR/7, 310 -->
  <originInfo>
    <xsl:choose>
      <xsl:when test="oai_marc:varfield[@id='044']/oai_marc:subfield[@label = 'c']" >
        <place>
					<placeTerm authority="iso3166">
            <xsl:value-of select="oai_marc:varfield[@id='044']"/>
					</placeTerm>
				</place>
      </xsl:when>
      <xsl:when test="oai_marc:varfield[@id='044']" ><!-- NR -->
        <place>
					<placeTerm authority="marc">
          	<xsl:value-of select="oai_marc:varfield[@id='044']"/>
					</placeTerm>
				</place>
      </xsl:when>
      <xsl:otherwise>
        <place>
					<placeTerm authority="marc">
         		<xsl:value-of select="substring($field008, 17 , 3)"/>
					</placeTerm>
				</place>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:for-each select="oai_marc:varfield[@id='260']/oai_marc:subfield[@label='a']"><!-- R -->
        <place>
					<placeTerm>
						<xsl:value-of select="."/>
					</placeTerm>
				</place>
    </xsl:for-each>

    <xsl:for-each select="oai_marc:varfield[@id='260']/oai_marc:subfield[@label='b']"><!-- R -->
      <publisher>
        <xsl:value-of select="."/>
      </publisher>
    </xsl:for-each>

    <xsl:if test="oai_marc:varfield[@id='250']"><!-- NR -->
      <edition>
        <xsl:value-of select="oai_marc:varfield[@id='250']/oai_marc:subfield[@label='a']"/>
      </edition>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="@level = 'b' or @level = 'i' or @level = 's'">
        <issuance>continuing</issuance>
      </xsl:when>
      <xsl:when test="@level = 'a' or @level = 'c' or @level = 'd' or @level = 'm'">
        <issuance>monographic</issuance>
      </xsl:when>
    </xsl:choose>

    <xsl:if test="oai_marc:varfield[@id='310']"><!-- NR -->
      <frequency>
        <xsl:value-of select="oai_marc:varfield[@id='310']/oai_marc:subfield[@label='a']"/><xsl:text>   </xsl:text>
        <xsl:value-of select="oai_marc:varfield[@id='310']/oai_marc:subfield[@label='b']"/>
      </frequency>
    </xsl:if>


<!--  ********** Dates **********  now part of originInfo -->
  <!-- 260 $c, 008/07-14, 033 -->

  <!-- possible <date> attributes: -->
    <!-- "type": created captured published -->
    <!-- "encoding": w3cdtf iso8601 marc -->
    <!-- "point": start end -->

  <xsl:choose>
    <xsl:when test="oai_marc:varfield[@id='260']"> <!-- R -->
      <xsl:for-each select="oai_marc:varfield[@id='260']/oai_marc:subfield[@label='c']"> <!-- R -->
        <dateIssued>
          <xsl:value-of select="."/>
        </dateIssued>
      </xsl:for-each>
      <xsl:if test="oai_marc:varfield[@id='260']/oai_marc:subfield[@label='g']"> <!-- NR -->
        <dateCreated>
          <xsl:value-of select="oai_marc:varfield[@id='260']/oai_marc:subfield[@label='g']"/>
        </dateCreated>
      </xsl:if>
    </xsl:when>

    <xsl:when test="oai_marc:varfield[@id='033']"> <!-- R -->
      <xsl:for-each select="oai_marc:varfield[@id='033']">
        <xsl:variable name="ind1"><xsl:value-of select="@i1"/></xsl:variable>
        <xsl:variable name="ind2"><xsl:value-of select="@i2"/></xsl:variable>

        <xsl:if test="($ind1 = ' ' or $ind1 = '0') and ($ind2 = '0' or $ind2 = '1')">
          <dateCaptured encoding="iso8601">
            <xsl:value-of select="oai_marc:subfield[@label='a']"/>
          </dateCaptured>
        </xsl:if>

        <xsl:if test="($ind1 = '2') and ($ind2 = '0' or $ind2 = '1')">
          <dateCaptured point="start" encoding="iso8601">
            <xsl:value-of select="oai_marc:subfield[@label='a' and position() = 1]"/>
          </dateCaptured>
          <dateCaptured point="end" encoding="iso8601">
            <xsl:value-of select="oai_marc:subfield[@label='a' and position() = 2]"/>
          </dateCaptured>
        </xsl:if>
      </xsl:for-each>
    </xsl:when>

    <xsl:otherwise>
      <xsl:variable name="typeOfDate">
        <xsl:value-of select="substring($field008, 8, 1)"/><!-- 008/6 -->
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="$typeOfDate = 'e' or $typeOfDate = 'p' or $typeOfDate = 'r' or $typeOfDate = 't' or $typeOfDate = 's'">
          <datePublished encoding="marc">
            <xsl:value-of select="substring($field008, 9, 4)"/>
          </datePublished>
        </xsl:when>

        <xsl:when test="$typeOfDate = 'c'">
          <datePublished  point="start" encoding="marc">
            <xsl:value-of select="substring($field008, 9, 4)"/>
          </datePublished>
        </xsl:when>

        <xsl:when test="$typeOfDate = 'd' or $typeOfDate = 'i' or $typeOfDate = 'k' or $typeOfDate = 'm'
                         or $typeOfDate = 'q' or $typeOfDate = 'u'">
          <datePublished  point="start" encoding="marc">
            <xsl:value-of select="substring($field008, 9, 4)"/>
          </datePublished>

          <datePublished  point="end" encoding="marc">
            <xsl:value-of select="substring($field008, 13, 4)"/>
          </datePublished>
        </xsl:when>
      </xsl:choose>
    </xsl:otherwise>

  </xsl:choose>

  </originInfo>

<!--  ********** Language ********** -->
  <!-- 008/35-37, 041 -->

  <xsl:choose>
    <xsl:when test="oai_marc:varfield[@id='041']/oai_marc:subfield[@label='2'] = 'rfc3066'">
      <language>
				<languageTerm authority="rfc3066" type="code">
        	<xsl:value-of select="oai_marc:varfield[@id='041']/oai_marc:subfield[@label='a']"/>
				</languageTerm>
      </language>
    </xsl:when>
    <xsl:when test="oai_marc:varfield[@id='041']">
      <language>
				<languageTerm authority="iso 639-2b" type="code">
        	<xsl:value-of select="oai_marc:varfield[@id='041']/oai_marc:subfield[@label='a']"/>
				</languageTerm>
      </language>
    </xsl:when>
    <xsl:otherwise>
      <language>
				<languageTerm authority="iso 639-2b" type="code">
        	<xsl:value-of select="substring($field008, 37, 3)"/>
				</languageTerm>
      </language>
    </xsl:otherwise>
  </xsl:choose>


<!--  ********** Form and Physical Description ********** -->
  <!-- (008/23 or 29) (856$q) (256, 300) -->

  <physicalDescription>
    <xsl:if test="$materialType = 'computer file'">
      <xsl:if test="substring($field007, 13, 1) = 'a' or substring($field007, 13, 1) = 'b'">
       <digitalOrigin>reformatted digital</digitalOrigin>
      </xsl:if>
    </xsl:if>

    <xsl:variable name="form">
      <xsl:choose>
        <xsl:when test="$materialType = 'book' or $materialType = 'music' or $materialType = 'mixed material' or $materialType = 'serial'">
          <xsl:value-of select="substring($field008, 25, 1)"/><!-- 008/23 -->
        </xsl:when>
        <xsl:when test="$materialType = 'map' or $materialType = 'visual material'">
          <xsl:value-of select="substring($field008, 31, 1)"/><!-- 008/29 -->
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="$form = 'f'"><form>braille</form></xsl:if>
    <xsl:if test="$form = 's'"><form>electronic</form></xsl:if>
    <xsl:if test="$form = 'b'"><form>microfiche</form></xsl:if>
    <xsl:if test="$form = 'a'"><form>microfilm</form></xsl:if>

    <xsl:for-each select="oai_marc:varfield[@id='856']/oai_marc:subfield[@label='q']"><!-- NR -->
      <internetMediaType>
        <xsl:value-of select="."/>
      </internetMediaType>
    </xsl:for-each>

    <xsl:if test="oai_marc:varfield[@id='256']"><!-- NR -->
      <extent>
        <xsl:value-of select="oai_marc:varfield[@id='256']/oai_marc:subfield[@label='a']"/><!-- NR -->
      </extent>
    </xsl:if>

    <xsl:for-each select="oai_marc:varfield[@id='300']"><!-- R -->
      <extent>
        <xsl:value-of select="oai_marc:subfield[@label='a']"/>
        <xsl:value-of select="oai_marc:subfield[@label='b']"/>
        <xsl:value-of select="oai_marc:subfield[@label='c']"/>
      </extent>
    </xsl:for-each>

  </physicalDescription>


<!--  ********** Abstract ********** -->
  <!-- 520 -->
  <xsl:for-each select="oai_marc:varfield[@id='520']"><!-- R -->
    <xsl:choose>
      <xsl:when test="oai_marc:subfield[@label='u']">
        <xsl:for-each select="oai_marc:subfield[@label='u']"><!-- R -->
          <abstract uri="{.}">
            <xsl:value-of select="ancestor::oai_marc:varfield[@id='520']/oai_marc:subfield[@label='a']"/><!-- NR -->
          </abstract>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <abstract>
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </abstract>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each>



<!--  ********** Target audience ********** -->
  <!-- 008/22, 521-->
  <xsl:if test="$materialType = 'book' or $materialType = 'computer file' or $materialType = 'music' or $materialType = 'visual material'">
    <xsl:variable name="targetAudience" select="substring($field008, 24, 1)"/><!-- 008/22 -->
    <xsl:choose>
      <xsl:when test="$targetAudience = 'd'"><targetAudience>adolescent</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'e'"><targetAudience>adult</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'g'"><targetAudience>general</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'b'"><targetAudience>juvenile</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'c'"><targetAudience>juvenile</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'j'"><targetAudience>juvenile</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'a'"><targetAudience>preschool</targetAudience></xsl:when>
      <xsl:when test="$targetAudience = 'f'"><targetAudience>specialized</targetAudience></xsl:when>
    </xsl:choose>
  </xsl:if>

  <xsl:for-each select="oai_marc:varfield[@id='521']">
    <targetAudience>
      <xsl:value-of select="oai_marc:subfield[@label='a']"/>
    </targetAudience>
  </xsl:for-each>


<!-- ********** Note ********** -->
  <!-- 500????, 505, 511, 518 -->
  <xsl:for-each select="oai_marc:varfield[@id='500']"><!-- R -->
    <note>
      <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
    </note>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='511']"><!-- R -->
    <note type="performers">
      <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
    </note>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='518']"><!-- R -->
    <note type="venue">
      <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
    </note>
  </xsl:for-each>

<!-- ********** Table of contents ********** -->
  <xsl:for-each select="oai_marc:varfield[@id='505']"><!-- R -->
    <xsl:choose>
      <xsl:when test="oai_marc:subfield[@label='u']">
        <xsl:for-each select="oai_marc:subfield[@label='u']"><!-- R -->
          <tableOfContents xlink="{.}">
            <xsl:value-of select="ancestor::oai_marc:varfield[@id='505']/oai_marc:subfield[@label='a']"/><!-- NR -->
          </tableOfContents>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <tableOfContents>
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </tableOfContents>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each>


<!-- ********** Cartographics ********** - under subject in 3.0 -->
  <!-- 255 -->
  <xsl:for-each select="oai_marc:varfield[@id='255']"><!-- R -->
	<subject>
		<cartographics>
      <xsl:for-each select="oai_marc:varfield[@id='034']"><!-- R -->
        <coordinates>
          <xsl:value-of select="oai_marc:subfield[@label='d']"/><!-- NR -->
          <xsl:value-of select="oai_marc:subfield[@label='e']"/><!-- NR -->
          <xsl:value-of select="oai_marc:subfield[@label='f']"/><!-- NR -->
          <xsl:value-of select="oai_marc:subfield[@label='g']"/><!-- NR -->
        </coordinates>
      </xsl:for-each>

      <xsl:if test="oai_marc:subfield[@label='c']">
        <coordinates>
          <xsl:value-of select="oai_marc:subfield[@label='c']"/><!-- NR -->
        </coordinates>
      </xsl:if>

      <xsl:if test="oai_marc:subfield[@label='a']">
        <scale>
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </scale>
      </xsl:if>

      <xsl:if test="oai_marc:subfield[@label='b']">
        <projection>
          <xsl:value-of select="oai_marc:subfield[@label='b']"/><!-- NR -->
        </projection>
      </xsl:if>

    </cartographics>
	</subject>
  </xsl:for-each>



<!--  ********** Subject ********** -->
  <!-- (650, 6xx $x) (630) (651, 6xx $z or 752) (6xx $y) (600, 610, 611) (050, 082, 080, 060, 086, 084) -->

  <!-- incomplete... -->
  <subject>
    <xsl:for-each select="oai_marc:varfield[@id='650']"><!-- R -->
      <topic>
        <xsl:for-each select="oai_marc:subfield[@label ='a' or @label ='b' or @label ='c' or @label ='d']">
          <xsl:value-of select="."/><xsl:text>  </xsl:text>
        </xsl:for-each>
      </topic>
    </xsl:for-each>

    <xsl:for-each select="oai_marc:varfield[@id='630']"><!-- R -->
      <titleInfo><title><!-- supposed to be structured like the "title" element -->
        <xsl:value-of select="."/>
      </title></titleInfo>
    </xsl:for-each>

    <xsl:for-each select="oai_marc:varfield[@id='651']"><!-- R -->
      <geographic><!-- hierarchical not done: 752 -->
        <xsl:for-each select="oai_marc:subfield">
          <xsl:value-of select="."/><xsl:text>  </xsl:text>
        </xsl:for-each>
      </geographic>
    </xsl:for-each>


    <!-- <temporal> 6xx $y -->

    <xsl:for-each select="oai_marc:varfield[@id='600' or @id='610' or @id='611']">
      <name><namePart><!-- supposed to be structured like the "name" element -->
        <xsl:value-of select="."/>
      </namePart></name>
    </xsl:for-each>


  </subject>


<!--  ********** Classification ********** at root level in 3.0, not under subject -->
    <xsl:for-each select="oai_marc:varfield[@id='050' or @id='082' or @id='080' or @id='060' or @id='086' or @id='084']">
      <classification>
      <!-- authority attribute: value from list at http:/www.loc.gov/marc/sourcecode/classification -->
        <xsl:value-of select="."/>
      </classification>
    </xsl:for-each>

<!--  ********** Related Items ********** -->
  <!-- 787, 700, 710, 711, 730, 780, 785, 776, 534, 786, 772, 773, 770, 774, 490, 440, 760, 800, 810, 811, 830 -->
  <!-- Incomplete, and not all are handled... -->
  <!-- "series" handled separately (not in named template). -->

  <xsl:for-each select="oai_marc:varfield[@id='780' or @id='785' or @id='776' or @id='534' or @id='786' or @id='772'
   or @id='773' or @id='770' or @id='774']">
    <xsl:call-template name="relatedItem">
      <xsl:with-param name="type" select="@id"/>
    </xsl:call-template>
  </xsl:for-each>


  <xsl:for-each select="oai_marc:varfield[@id='490' or @id='440' or @id='760' or @id='800' or @id='810' or @id='811' or @id='830']">
    <relatedItem type="series">
    <!-- incomplete, needs checking -->
      <titleInfo><title>
        <xsl:value-of select="oai_marc:subfield[@label='a']"/>
      </title></titleInfo>
      <identifier>
        <xsl:value-of select="oai_marc:subfield[@label='v']"/>
      </identifier>
    </relatedItem>
  </xsl:for-each>




<!--  ********** Identifiers ********** -->
  <!-- 010, 020, 022, 024, 028, 856 -->
  <!-- see schema for further possible values for "type"-->

  <xsl:for-each select="oai_marc:varfield[@id='020']"><!-- R -->
    <xsl:if test="oai_marc:subfield[@label='a']">
      <identifier type="isbn">
        <xsl:value-of select="oai_marc:subfield[@label='a']"/>
      </identifier>
    </xsl:if>
    <xsl:if test="oai_marc:subfield[@label='z']">
      <identifier type="isbn">
        <xsl:value-of select="oai_marc:subfield[@label='z']"/><xsl:text>(invalid)</xsl:text>
      </identifier>
    </xsl:if>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='022']"><!-- R -->
    <xsl:if test="oai_marc:subfield[@label='a']">
      <identifier type="issn">
        <xsl:value-of select="oai_marc:subfield[@label='a']"/>
      </identifier>
    </xsl:if>
    <xsl:if test="oai_marc:subfield[@label='y']">
      <identifier type="issn">
        <xsl:value-of select="oai_marc:subfield[@label='y']"/><xsl:text>(incorrect)</xsl:text>
      </identifier>
    </xsl:if>
    <xsl:if test="oai_marc:subfield[@label='z']">
      <identifier type="issn">
        <xsl:value-of select="oai_marc:subfield[@label='z']"/><xsl:text>(cancelled)</xsl:text>
      </identifier>
    </xsl:if>
  </xsl:for-each>


  <xsl:if test="oai_marc:varfield[@id='010']"><!-- NR -->
    <identifier type="lccn">
      <xsl:value-of select="oai_marc:varfield[@id='010']/oai_marc:subfield[@label='a']"/>
    </identifier>
  </xsl:if>

  <xsl:for-each select="oai_marc:varfield[@id='024']"><!-- R -->
    <xsl:choose>
      <xsl:when test="@i1='0'">
        <identifier type="isrc">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='2'">
        <identifier type="ismn">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='4'">
        <identifier type="sici">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='1'">
        <identifier type="upc">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
    </xsl:choose>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='028']"><!-- R -->
    <xsl:choose>
      <xsl:when test="@i1='0'">
        <identifier type="issue number">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='1'">
        <identifier type="matrix number">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='3'">
        <identifier type="music publisher">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='2'">
        <identifier type="music plate">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
      <xsl:when test="@i1='4'">
        <identifier type="videorecording identifier">
          <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
        </identifier>
      </xsl:when>
    </xsl:choose>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='856']/oai_marc:subfield[@label='u']"><!-- R -->
      <xsl:value-of select="."/>
        <xsl:for-each select="ancestor::oai_marc:varfield[@id='856']/oai_marc:subfield[@label='z']"><!-- R -->
					<identifier type="uri">
            <xsl:value-of select="."/>
          </identifier>
        </xsl:for-each>
  </xsl:for-each>




<!--  **********  Location  ********** -->
  <!-- 852 $a, $b, $j -->

<xsl:for-each select="oai_marc:varfield[@id='852']"><!-- R -->
  <location><physicalLocation>
    <xsl:for-each select="oai_marc:subfield[@label='b' or @label='j']"><!-- R -->
      <xsl:value-of select="."/><xsl:text>  </xsl:text>
    </xsl:for-each>
  </physicalLocation></location>
</xsl:for-each>



<!--  ********** Access Conditions ********** -->
  <!-- 506, 540 -->

  <xsl:for-each select="oai_marc:varfield[@id='506']"><!-- R -->
    <accessCondition type="restrictionOnAccess">
      <xsl:value-of select="oai_marc:subfield[@label='a']"/>
    </accessCondition>
  </xsl:for-each>

  <xsl:for-each select="oai_marc:varfield[@id='540']"><!-- R -->
    <accessCondition type="useAndReproduction">
      <xsl:value-of select="oai_marc:subfield[@label='a']"/>
    </accessCondition>
  </xsl:for-each>


<!--  ********** Optional <extension> elements go here... ********** -->

  <!-- JAFER and University of Oxford specific: -->

<xsl:for-each select="oai_marc:varfield[@id='852']"><!-- R -->
  <extension>
    <xsl:for-each select="oai_marc:subfield[@label='b']"><!-- R -->
      <shelvingLocation>
        <xsl:value-of select="."/>
      </shelvingLocation>
    </xsl:for-each>

    <xsl:for-each select="oai_marc:subfield[@label='h'] | oai_marc:subfield[@label='m']"><!-- NR?? -->
      <callNumber>
        <xsl:value-of select="."/>
      </callNumber>
    </xsl:for-each>

    <circulationStatus>
      <xsl:value-of select="oai_marc:subfield[@label='y']"/><!-- not defined by loc -->
    </circulationStatus>

  </extension>
</xsl:for-each>




<!--  ********** Information about this record ********** -->
  <!-- 040 $a $d, 008/00-05, 005 -->

  <recordInfo>
    <xsl:for-each select="oai_marc:varfield[@id='040']"><!-- NR -->
      <recordContentSource>
        <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
      </recordContentSource>
    </xsl:for-each>

    <recordCreationDate>
      <xsl:value-of select="substring($field008, 2, 6)"/><!-- 008/00-05 -->
    </recordCreationDate>

    <xsl:for-each select="oai_marc:varfield[@id='005']"><!-- NR -->
      <recordChangeDate>
        <xsl:value-of select="."/>
      </recordChangeDate>
    </xsl:for-each>

    <recordIdentifier>
       <!-- was <docId> -->
        <xsl:value-of select="substring($field001, 8, 8)"/>
    </recordIdentifier>
  </recordInfo>

  </mods>

</xsl:template>



<xsl:template name="title">

  <xsl:param name="type"/>

    <titleInfo>
		<title>
      <xsl:choose>
        <xsl:when test="$type = '245'"></xsl:when>
        <xsl:when test="$type = '210'"><xsl:attribute name="type">abbreviated</xsl:attribute></xsl:when>
        <xsl:when test="$type = '242'"><xsl:attribute name="type">translated</xsl:attribute></xsl:when>
        <xsl:when test="$type = '246'"><xsl:attribute name="type">alternative</xsl:attribute></xsl:when>
        <xsl:when test="$type = '240' or $type = '130'"><xsl:attribute name="type">uniform</xsl:attribute></xsl:when>
      </xsl:choose>

      <xsl:value-of select="oai_marc:subfield[@label='a']"/><!-- NR -->
      <xsl:value-of select="oai_marc:subfield[@label='b']"/><!-- NR -->
    </title>

      <xsl:if test="oai_marc:subfield[@label='p'] | oai_marc:subfield[@label='n']">
          <xsl:for-each select="oai_marc:subfield[@label='n']"><!-- R -->
            <partNumber>
              <xsl:value-of select="."/>
            </partNumber>
          </xsl:for-each>

          <xsl:for-each select="oai_marc:subfield[@label='p']"><!-- R -->
            <partName>
              <xsl:value-of select="."/>
            </partName>
          </xsl:for-each>
      </xsl:if>
		</titleInfo>

</xsl:template>


<xsl:template name="name">

  <xsl:param name="type"/>
  <!-- JAFER specific: names in 1xx fields use "creator" for role attribute, to differentiate from 7xx fields. -->
  <!-- ... "role" attribute content should actually be /oai_marc:subfield[@label='e']-->
    <name>
      <xsl:choose>
        <xsl:when test="$type = '100'">
          <xsl:attribute name="type">personal</xsl:attribute>
        </xsl:when>
        <xsl:when test="$type = '110'">
          <xsl:attribute name="type">corporate</xsl:attribute>
        </xsl:when>
        <xsl:when test="$type = '111'">
          <xsl:attribute name="type">conference</xsl:attribute>
        </xsl:when>
        <xsl:when test="$type = '700'">
          <xsl:attribute name="type">personal</xsl:attribute>
        </xsl:when>
        <xsl:when test="$type = '710'">
          <xsl:attribute name="type">corporate</xsl:attribute>
        </xsl:when>
        <xsl:when test="$type = '711'">
          <xsl:attribute name="type">conference</xsl:attribute>
        </xsl:when>
      </xsl:choose>

			<!-- namePart -->
      <!-- Structured corporate names should be expressed in multiple components, each in <component> element -->
			<namePart>
      <xsl:value-of select="oai_marc:subfield[@label='a']"/>
      <xsl:value-of select="oai_marc:subfield[@label='d']"/>
			</namePart>
			<!-- role -->
      <xsl:choose>
        <xsl:when test="$type = '100'">
          <role><roleTerm type="text">creator</roleTerm></role>
				</xsl:when>
        <xsl:when test="$type = '110'">
          <role><roleTerm type="text">creator</roleTerm></role>
        </xsl:when>
        <xsl:when test="$type = '111'">
          <role><roleTerm type="text">creator</roleTerm></role>
        </xsl:when>
      </xsl:choose>

      <xsl:if test="following::oai_marc:varfield[@id='245']/oai_marc:subfield[@label='c']"><!-- NR -->
        <displayForm>
          <xsl:value-of select="following::oai_marc:varfield[@id='245']/oai_marc:subfield[@label='c']"/>
        </displayForm>
      </xsl:if>

      <xsl:if test="($type = '100' or $type = '700') and (oai_marc:subfield[@label='u'])">
        <affiliation>
          <xsl:value-of select="oai_marc:subfield[@label='u']"/>
        </affiliation>
      </xsl:if>

    </name>
</xsl:template>


<xsl:template name="relatedItem">

  <xsl:param name="type"/>

    <relatedItem>
      <xsl:attribute name="type">
        <xsl:choose>
          <xsl:when test="$type = '780'">preceding</xsl:when>
          <xsl:when test="$type = '785'">succeeding</xsl:when>
          <xsl:when test="$type = '776'">reproduction</xsl:when>
          <xsl:when test="$type = '534' or $type = '786'">original</xsl:when>
          <xsl:when test="$type = '772' or $type = '773'">host</xsl:when>
          <xsl:when test="$type = '770' or $type = '774'">constituent</xsl:when>
        </xsl:choose>
      </xsl:attribute>

      <xsl:if test="oai_marc:subfield[@label='t']"><!-- NR -->
        <titleInfo><title>
          <xsl:value-of select="oai_marc:subfield[@label='t']"/>
        </title></titleInfo>
      </xsl:if>

      <xsl:if test="oai_marc:subfield[@label='x']"><!-- NR -->
        <identifier type="issn">
          <xsl:value-of select="oai_marc:subfield[@label='x']"/>
        </identifier>
      </xsl:if>

      <xsl:for-each select="oai_marc:subfield[@label='z']"><!-- R -->
        <identifier type="isbn">
          <xsl:value-of select="."/>
        </identifier>
      </xsl:for-each>

      <xsl:for-each select="oai_marc:subfield[@label='w']"><!-- R -->
        <identifier type="local">
         <xsl:value-of select="."/>
        </identifier>
      </xsl:for-each>

      <xsl:if test="oai_marc:subfield[@label='a']"><!-- NR -->
        <name><namePart>
          <xsl:value-of select="oai_marc:subfield[@label='a']"/>
        </namePart></name>
      </xsl:if>

    </relatedItem>
</xsl:template>

</xsl:stylesheet>