<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
   <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="oai_marc">
   <test>
    <xsl:copy-of select="*"/>
   </test>
  </xsl:template>

</xsl:stylesheet>