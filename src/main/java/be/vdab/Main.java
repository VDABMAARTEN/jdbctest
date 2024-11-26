package be.vdab;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        System.out.println("----------" + "Maak een keuze" + "----------"
                + "\n typ 1 om een nieuwe rekening aan te maken"
                + "\n typ 2 om je saldo te consulteren"
                + "\n typ 3 om een bedrag over te schrijven"
                + "\n ----------"
        );

        switch (scanner.nextInt()) {
            case (1) -> {
                System.out.println("Typ het nummer van de nieuwe rekening, zonder streepjes, zonder spaties: BEXXXXXXXXXXXXXX");
                var rekeningnummer = scanner.next();
                if (controleRekeningNummer(rekeningnummer)) {
                    var rekening = new Rekening(rekeningnummer);
                    System.out.println("Rekeningnummer is toegevoegd");
                } else {
                    throw new IllegalArgumentException("Rekeningnummer is niet correct");
                }
            }

            case (2) -> {
                var repository = new RekeningRepository();
                System.out.println("Typ het rekeningnummer waarvan je het saldo wil consulteren.");
                var rekeningnummer = scanner.next();
                System.out.println("Saldo rekening " + rekeningnummer + " : ");
                try {
                    repository.saldoConsulteren(rekeningnummer)
                            .ifPresentOrElse(System.out::println, () -> System.out.println("Rekeningnummer niet gevonden"));
                } catch (
                        SQLException e) {
                    e.printStackTrace(System.err);
                }
            }
            case (3) -> {
                System.out.println("Typ het rekeningnummer vanwaar je wil overschrijven");
                var vanRekeningNummer = scanner.next();
                if (controleRekeningNummer(vanRekeningNummer) == false){
                    throw new IllegalArgumentException("Rekeningnummer is niet correct");
                }
                zitHetRekeningNummerAlInDeDatabase(vanRekeningNummer);


                System.out.println("Typ het rekeningnummer waarnaar je wil overschrijven");
                var naarRekeningNummer = scanner.next();
                if (controleRekeningNummer(naarRekeningNummer) == false){
                    throw new IllegalArgumentException("Rekeningnummer is niet correct");
                }
                zitHetRekeningNummerAlInDeDatabase(naarRekeningNummer);

                System.out.println("Typ het bedrag dat je wil overschrijven");
                var bedrag = scanner.nextDouble();
                while (bedrag <= 0) {
                    System.out.println("FOUT: bedrag moet groter zijn dan 0. Geef een nieuw bedrag in.");
                    bedrag = scanner.nextDouble();
                }

                var repository = new RekeningRepository();
                try {
                    repository.bedragOverschrijven(vanRekeningNummer, naarRekeningNummer, bedrag);
                    System.out.println("De overschrijving van " + bedrag + " euro van " + vanRekeningNummer + " naar " + naarRekeningNummer + "is geslaagd");
                } catch (SQLException e) {
                    e.printStackTrace(System.err);
                }

            }
            default -> System.out.println("Verkeerde input. Start het programma opnieuw en kies 1,2 of 3");

        }
    }




    static boolean controleRekeningNummer(String rek) {

//          Begin met controle dmv regex
        final Pattern PATTERN = Pattern.compile("^BE\\d{14}");
        var matcher = PATTERN.matcher(rek);

        if (matcher.matches()) {
//            eerste controle op cijfer 3 en 4 samen kleiner zijn dan 2 of groter dan 98
            var controle1Int = Long.parseLong(rek.substring(2, 4));
            if (controle1Int >= 2 && controle1Int <= 98) {
//                tweede controle op de string vanaf cijfer 5 tot einde, + 1147 + controlegetal1
                var controle2 = new StringBuilder(rek.substring(4));
                controle2.append("1114");
                controle2.append(rek.substring(2, 4));
                var controle2Long = Long.parseLong(controle2.toString());
                if (controle2Long % 97 == 1) {
                    return true;

                } else {
                    return false;
                }

            } else {
                return false;
            }

        } else {
            throw new IllegalArgumentException("\"Het rekeningnummer voldoet niet aan de voorwaarden:" +
                    "                     \n1.Het moet 16 tekens bevatten" +
                    "                     \n2.De eerste twee tekens zijn BE" +
                    "                     \n3.De rest van de tekens zijn cijfers" +
                    "                     \n4.Geen spaties, geen streepjes: BEXXXXXXXXXXXXXX");
        }
    }



    static void zitHetRekeningNummerAlInDeDatabase(String rek){
        var repository = new RekeningRepository();
        try {
            if (!repository.rekeningBestaat(rek)) {
                throw new IllegalArgumentException("Rekeningnummer bestaat niet");
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}