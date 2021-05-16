package com.company;

// PalvelinEsimerkki.java
// Kuuntelee porttia, yhteydestÃ¤ otta vastaan kokonaislukuja, vastaa neliÃ¶imÃ¤llÃ¤
// sulkee yhteyden jollei tule kokonaislukua

import java.net.*;
import java.util.Scanner;
import java.io.PrintWriter;

public class PalvelinEsimerkki {

    // kuunteleva palvelintÃ¶pseli
    ServerSocket ss = null;

    public static void main(String[] args) {

        PalvelinEsimerkki p;

        if (args.length > 0)
            p = new PalvelinEsimerkki(Integer.valueOf(args[0]));
        else
            p = new PalvelinEsimerkki();

        p.kuuntele();

    } // main()


    // konstruktorit avaavat yhteyden kuuntelulle
    public PalvelinEsimerkki(int portti) {

        try {
            ss = new ServerSocket(portti);
            System.out.println("Kuunnellaan porttia " + portti);
        } catch (Exception e) {
            System.err.println(e);
            ss = null;
        }
    }

    public PalvelinEsimerkki() {
        this(1234);
    }


    // kuuntelu "sÃ¤ie" (sÃ¤ikeistetÃ¤Ã¤n myÃ¶hemmin kurssilla)
    public void kuuntele() {

        try {
            while (true) {

                // odotetaan uutta yhteyttÃ¤
                Socket cs = ss.accept();

                // palvellaan yhteys
                palvele(cs);
            }
        } catch (Exception e) {
            System.err.println(e);
            ss = null;
        }
    }   // kuuntele()

    // palvelee yhden yhteyden
    public void palvele(Socket cs) {

        try {

            System.out.println("Uusi yhteys: " + cs.getInetAddress() +
                    ":" + cs.getPort());

            // virrat kÃ¤yttÃ¶kelpoiseen muotoon
            Scanner in = new Scanner(cs.getInputStream());
            PrintWriter out = new PrintWriter(cs.getOutputStream(), true);

            while (in.hasNextInt()) {
                // luetaan "pyyntÃ¶"
                int x = in.nextInt();

                // lÃ¤hetetÃ¤Ã¤n vastaus
                out.println("" + x + "*" + x + " = " + (x * x));
            }

            cs.close();

        } catch (Exception e) {
            System.err.println(e);
        }

    }   // palvele()

}   // class
