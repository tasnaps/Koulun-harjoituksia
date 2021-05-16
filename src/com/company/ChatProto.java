package com.company;


/**
 * Rajapinta joka esittelee muutaman merkkijonovakion jota chatissÃ¤ kÃ¤ytetÃ¤Ã¤n.
 *
 * KÃ¤ytettÃ¤vÃ¤ protokolla:
 *
 * Kukin komento pÃ¤Ã¤ttyy rivinvaihtoon (CRLF) vaikka sitÃ¤ ei alla aina mainita.
 * Asiakas ottaa TCP-yhteyden palvelimen porttiin (oletusarvoisesti 20214).
 * Asiakas lÃ¤hettÃ¤Ã¤ viestin "JOIN nimi"
 * Palvelin vastaa "200 OK" (tai "500 error" ja sulkee yhteyden)
 * TCP-yhteys jÃ¤tetÃ¤Ã¤n pÃ¤Ã¤lle jos yhteydenotto oli ok.
 *
 * Kun asiakkaalla on viesti lÃ¤hetettÃ¤vÃ¤nÃ¤, hÃ¤n lÃ¤hettÃ¤Ã¤
 * "MESSAGE nimi" yhdellÃ¤ rivillÃ¤ ja seuraavalla rivillÃ¤ varsinainen viesti
 * (varsinainen viesti on aina tasan yksirivinen)
 * Palvelin kuittaa lÃ¤hettÃ¤jÃ¤lle "200 OK" (tai "500 error").
 * Jos viesti on ok, palvelin lÃ¤hettÃ¤Ã¤ saman viestin kullekin muulle
 * asiakkaalle muodossa  "VIESTI nimi" yhdellÃ¤ rivillÃ¤ ja seuraavalla
 * riveillÃ¤ varsinainen viesti.
 * Asiakkaat eivÃ¤t kuittaa tÃ¤tÃ¤ viestiÃ¤.
 *
 * Asiakkaan tai palvelimen halutessa poistua, se lÃ¤hettÃ¤Ã¤ kumppanilleen viestin
 * "QUIT", kumppani vastaa "200 OK" ja sulkee yhteyden.
 *
 **/
public interface ChatProto {

    String C_JOIN = "JOIN";
    String C_ERROR = "500 error";
    String C_MESSAGE = "MESSAGE";
    String C_OK = "200 OK";
    String C_QUIT = "QUIT";
    String EOL = "\r\n";    // rivin loppumerkki

}
