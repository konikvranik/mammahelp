wget -O - "http://www.mamo.cz/index.php?pg=mamograficky-screening--centra--seznam" | tidy -q -asxml -raw --new-blocklevel-tags nav,section,article,header,aside | sed "s/[\x1e\x1c\x13]/ /g" | sed 's#xmlns="http://www.w3.org/1999/xhtml"##g' | sed '/<!DOCTYPE html\|"http:\/\/www.w3.org\/TR\/xhtml1\/DTD\/xhtml1-strict.dtd">/ d' | xsltproc --encoding iso-8859-2 mamo_centra.xsl mamocentra.xhtml
