<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="journalContainer/journals/entry">
        <xsl:for-each select="value">
            <tr>
                <td>
                    <input type="radio" class="form-control" name="usernumber">
                        <xsl:attribute name="value">
                            <xsl:value-of select="id/text()"/>
                        </xsl:attribute>
                    </input>
                </td>
                <td>
                    <xsl:value-of select="name/text()"/>
                </td>
                <td>
                    <xsl:value-of select="description/text()"/>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>