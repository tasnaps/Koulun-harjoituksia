package com.company;


// ChatPalvelin.java

// palvelin tehtÃ¤vien 34-35 testaamista varten

import java.io.IOException;
import java.net.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

    public class ChatPalvelin {

        // kuunteleva palvelintÃ¶pseli
        private ServerSocket ss;

        // Chattiasiakkaat (sÃ¤ikeet)
        private HashSet<Chattaaja> chattaajat;

        public static void main(String[] args) {

            ChatPalvelin p;

            if (args.length > 0)
                p = new ChatPalvelin(Integer.parseInt(args[0]));
            else
                p = new ChatPalvelin();


            p.odotaYhteyksia();


        } // main()


        // konstruktorit avaavat yhteyden kuuntelulle
        private ChatPalvelin(int portti) {

            chattaajat = new HashSet<>();

            try {
                ss = new ServerSocket(portti);
                System.out.println("Kuunnellaan porttia " + portti);
            } catch (Exception e) {
                System.err.println("" + e);
                ss = null;
            }
        }

        private ChatPalvelin() {
            this(20214);
        }


        // kuuntelu "sÃ¤ie"
        private void odotaYhteyksia() {


            if (ss == null)
                return;

            Botti b = new Botti();
            b.start();

            BufferedReader kayttaja = new BufferedReader(new InputStreamReader(System.in));

            try {
                ss.setSoTimeout(1000);

                System.out.println("Paina ENTER lopettaaksesi palvelimen");

                while (true) {

                    // odotetaan uutta yhteyttÃ¤, vÃ¤lillÃ¤ kÃ¤ydÃ¤Ã¤n katsomassa kÃ¤yttÃ¤jÃ¤Ã¤
                    Socket cs = null;
                    try {
                        cs = ss.accept();
                    } catch (SocketTimeoutException ignored) {
                    }

                    if (kayttaja.ready()) {
                        lopeta();
                        return;
                    }
                    if (cs == null) // jos tultiin accept:stÃ¤ aikakatkaisulla
                        continue;

                    // luodaan ja kÃ¤ynnistetÃ¤Ã¤n uusi palvelijasÃ¤ie

                    Chattaaja uusi = new Chattaaja(cs);
                    uusi.start();
                }
            } catch (Exception e) {
                System.err.println("" + e);
                ss = null;
            }
        }   // kuuntele()

        private void lopeta() {
            // sykronoidaan lÃ¤hetys jotta toisaalta uusia kÃ¤yttÃ¤jiÃ¤ ei lisÃ¤ttÃ¤isi
            // kesken lÃ¤pikÃ¤ynnin ja toisaalta jotta kaikki saisivat viestit
            // samassa jÃ¤rjestyksessÃ¤
            System.out.println("LÃ¤hetetÃ¤Ã¤n kaikille lopetusviesti");
            synchronized (this) {
                for (Chattaaja kohde : chattaajat) {
                    if (!kohde.nimi.equals("Botti"))
                        kohde.lahetaViesti(ChatProto.C_QUIT);
                }
            }
            // odotetaan hetki jotta asiakkaat vastaavat
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }

            for (Chattaaja kohde : chattaajat) {
                if (!kohde.nimi.equals("Botti"))
                    kohde.sulje();
            }

        }


        private void lahetaViestiKaikille(String viesti, String lahettaja) {
            // sykronoidaan lÃ¤hetys jotta toisaalta uusia kÃ¤yttÃ¤jiÃ¤ ei lisÃ¤ttÃ¤isi
            // kesken lÃ¤pikÃ¤ynnin ja toisaalta jotta kaikki saisivat viestit
            // samassa jÃ¤rjestyksessÃ¤
            System.out.println("Viesti kaikille: " + lahettaja + " > " + viesti);
            synchronized (this) {
                for (Chattaaja kohde : chattaajat) {
                    if (lahettaja == null || !lahettaja.equals(kohde.nimi))
                        kohde.lahetaViesti(ChatProto.C_MESSAGE + " " + lahettaja + ChatProto.EOL + viesti);
                }
            }
        }

        private void poistaChattaaja(Chattaaja p) {
            System.out.println("Poistetaan " + p.nimi);
            synchronized (this) {
                chattaajat.remove(p);
            }
        }

        private void lisaaChattaaja(Chattaaja p) {
            System.out.println("Lisataan " + p.nimi);
            synchronized (this) {
                chattaajat.add(p);
            }
        }


        private boolean onkoVarattu(String nimi) {
            synchronized (chattaajat) {
                for (Chattaaja kohde : chattaajat) {
                    if (nimi.equals(kohde.nimi))
                        return true;
                }
                return false;
            }
        }


        /**
         * Ohjelmassa on myÃ¶s botti joka keskustelee jos muut eivÃ¤t keskustele.
         * Ja kommentoi kaikkien muiden viestejÃ¤.
         */
        class Botti extends Chattaaja {
            long lastMessage = System.currentTimeMillis(); // viimeinen botin lÃ¤hetysaika
            Timer timer = new Timer(true);

            Botti() {
                nimi = "Botti";
                this.setDaemon(true);
            }

            public void run() {

                try {

                    System.out.println("Botti kÃ¤ynnistyy");

                    lisaaChattaaja(this);
                    viestiJotain();

                } catch (Exception e) {
                    System.err.println("Botti.run: " + e);
                } finally {
                    poistaChattaaja(this);
                }
            }   // run()

            void viestiJotain() {
                while (true) {

                    try {
                        Thread.sleep(20 * 1000);
                    } catch (Exception ignored) {
                    }
                    ;

                    synchronized (this) {
                        // ei viestitÃ¤ jos on hiljattain kommentoitu muiden viestejÃ¤
                        if (lastMessage > System.currentTimeMillis() - 19 * 1000)
                            continue;

                        String viesti = "Botti edes hereillÃ¤";
                        lahetaViestiKaikille(viesti, this.nimi);
                        lastMessage = System.currentTimeMillis();
                    }
                }

            }

            // botti kun saa viestin, niin se miettii hetken ja sitten lÃ¤hettÃ¤Ã¤
            // viestin muille
            synchronized void lahetaViesti(String viesti) {
                lastMessage = System.currentTimeMillis();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        lahetaViestiKaikille("Todellakin!", nimi);
                    }
                }, 1000 + (int) (Math.random() * 1000));
            }

        }


        /**
         * niputettuna yhden chat-asiakkaan tarvitsemat tiedot
         * ja sÃ¤ie joka hanskaa toiminnan yhden chattÃ¤Ã¤jÃ¤n suuntaan
         */
        class Chattaaja extends Thread {

            Socket asiakas = null;
            PrintWriter out = null;
            BufferedReader in = null;
            String nimi = null;
            boolean lopeta = false;

            Chattaaja() {
                super();
            }

            Chattaaja(Socket cs) {
                super();
                asiakas = cs;
            }

            void sulje() {

                try {
                    lopeta = true;
                    asiakas.close();
                } catch (IOException ignored) {
                }
            }

            @Override
            public void run() {

                if (asiakas == null || asiakas.isClosed())
                    return;

                try {

                    // uuden asiakkaan kÃ¤sittely

                    System.out.println("Uusi chattaaja: " + asiakas.getInetAddress() +
                            ":" + asiakas.getPort());

                    // virrat kÃ¤yttÃ¶kelpoiseen muotoon
                    in = new BufferedReader(new InputStreamReader(asiakas.getInputStream()));
                    out = new PrintWriter(asiakas.getOutputStream(), true);

                    // luetaan chattaajan tiedot

                    String ok = lueTiedot();
                    if (ok != null) {
                        lahetaViesti(ChatProto.C_ERROR + " " + ok);
                    } else {

                        // jos kaikki ok
                        lisaaChattaaja(this);
                        lahetaViesti(ChatProto.C_OK + " " +
                                chattaajat.size() + " asiakasta lasna" +
                                ChatProto.EOL);
                        // itse palvelu
                        palvele();
                    }

                    asiakas.close();


                } catch (Exception e) {
                    System.err.println("Chattaaja.run: " + e);
                } finally {
                    poistaChattaaja(this);
                }


            }   // run()

            /**
             * varsinainen yhden asiakkaan palvelu
             */
            void palvele() {
                try {
                    while (true) {
                        String otsikko = in.readLine();
                        otsikko = otsikko.trim();
                        if (otsikko.length() == 0)
                            continue;
                        if (otsikko.startsWith(ChatProto.C_QUIT)) {
                            lahetaViesti(ChatProto.C_OK);
                            poistaChattaaja(this);
                            break;
                        }

                        if (otsikko.startsWith(ChatProto.C_MESSAGE)) {
                            String viesti = in.readLine();
                            if (viesti != null) {
                                lahetaViestiKaikille(viesti, this.nimi);
                            } else {
                                lahetaViesti(ChatProto.C_ERROR + " viesti viallinen");
                            }
                        } else {
                            lahetaViesti(ChatProto.C_ERROR + " tuntematon komento ");
                        }

                    }
                } catch (Exception e) {

                    if (!lopeta) {
                        System.err.println("Chattaaja.palvele: " + e);
                        e.printStackTrace();
                    }
                }

            }

            String lueTiedot() {

                // TODO: tarkempi raportointi
                try {
                    String liittymisRivi = in.readLine();
                    liittymisRivi = liittymisRivi.trim();

                    if (!liittymisRivi.startsWith(ChatProto.C_JOIN))
                        return "JOIN: puuttuu";

                    // TODO: vÃ¤lilyÃ¶nnistÃ¤
                    String uusiTunnus = liittymisRivi.substring(ChatProto.C_JOIN.length()).trim();

                    if (uusiTunnus.length() < 1)
                        return "nimi on tyhja";

                    boolean varattu = onkoVarattu(uusiTunnus);

                    if (varattu)
                        return "nimi on varattu";

                    nimi = uusiTunnus;

                    return null;

                } catch (IOException e) {
                    System.err.println("lueTiedot poikkeus: " + e);
                    return "poikkeus";
                }


            }

            // lÃ¤hettÃ¤Ã¤ valmiin viestin
            // tÃ¤tÃ¤ kÃ¤yttÃ¤Ã¤ sekÃ¤ oma sÃ¤ie, ettÃ¤ muut (lahetaKaikille)
            synchronized void lahetaViesti(String viesti) {
                out.print(viesti.trim()); // TODO
                out.print(ChatProto.EOL);
                out.flush();
            }
        }
    }


