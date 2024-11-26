package be.vdab;

import java.sql.SQLException;

public class Rekening {

    private static String rekeningNummer;
    private static double saldo = 0;

    Rekening(String rekeningNummer) {

        setRekeningNummer(rekeningNummer);
    }

    //    GETTERS
    public static String getRekeningNummer() {
        return rekeningNummer;
    }

    public static double getSaldo() {
        return saldo;
    }

    //    set rekeningnummer + voeg toe aan de database

    static void setRekeningNummer(String reknr) throws IllegalArgumentException {
        rekeningNummer = reknr;
        var repository = new RekeningRepository();
        try {
            repository.rekeningToevoegen(Rekening.rekeningNummer);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}
