package com.bankofz.inqacc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class Inqacc {
    private static final String SORTCODE = "987654";
    private final Ports ports;

    public Inqacc(Ports ports) {
        this.ports = Objects.requireNonNull(ports);
    }

    public Commarea inquire(Commarea input) {
        Fetch fetched;
        if ("99999999".equals(input.accno())) {
            fetched = ports.selectHighest(SORTCODE);
            if (fetched.sqlcode() != 0) {
                failHighest(fetched.sqlcode());
            }
        } else {
            fetched = readOrdinary(input.accno());
        }

        if (fetched.sqlcode() == 100) {
            return withSuccess(input);
        }

        AccountRow row = Objects.requireNonNull(fetched.row(), "Successful query must supply a row");
        if ("        ".equals(row.accType()) || "\0".repeat(8).equals(row.accType())) {
            return withSuccess(input);
        }
        return new Commarea(
                row.eye(), row.custno(), row.scode(), row.accno(), row.accType(),
                row.intRate(), displayDate(row.opened()),
                String.format(Locale.ROOT, "%08d", row.overdraft()),
                displayDate(row.lastStmtDt()), displayDate(row.nextStmtDt()),
                row.availBal(), row.actualBal(), "Y", input.pcb1Pointer());
    }

    private Fetch readOrdinary(String accno) {
        int openSqlcode = ports.open(SORTCODE, accno);
        if (openSqlcode != 0) {
            failOpen(openSqlcode);
        }

        Fetch fetched = ports.fetch();
        if (fetched.sqlcode() != 0 && fetched.sqlcode() != 100) {
            failFetch(fetched.sqlcode());
        }

        int closeSqlcode = ports.close();
        if (closeSqlcode != 0) {
            failClose(closeSqlcode);
        }

        return fetched;
    }

    private static Commarea withSuccess(Commarea c) {
        return new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), c.accType(), c.intRate(),
                c.opened(), c.overdraft(), c.lastStmtDt(), c.nextStmtDt(),
                c.availBal(), c.actualBal(), "Y", c.pcb1Pointer());
    }

    private static String displayDate(String date) {
        return date.substring(8, 10) + date.substring(5, 7) + date.substring(0, 4);
    }

    private void failOpen(int sqlcode) {
        String sqlcodeDisplay = String.format(Locale.ROOT, "%+09d", sqlcode);
        linkDiagnostic("HRAC", sqlcode, "RAD010 -Failure when attempting to OPEN DB2 "
                + "CURSOR. Check SQLCODE. SQLCODE=" + sqlcodeDisplay);
        ports.display("Failure when attempting to open DB2 CURSOR "
                + "ACC-CURSOR. With SQL code=" + sqlcodeDisplay);
        checkStormDrain(sqlcode);
        throw new Abend("HRAC");
    }

    private void failFetch(int sqlcode) {
        checkStormDrain(sqlcode);
        String sqlcodeDisplay = String.format(Locale.ROOT, "%+09d", sqlcode);
        linkDiagnostic("HRAC", sqlcode, "FD010 -Failure when attempting to FETCH from "
                + "DB2 CURSOR (ACC-CURSOR). Check SQLCODESQLCODE=" + sqlcodeDisplay);
        ports.display("Failure when attempting to FETCH from the DB2 "
                + "CURSOR ACC-CURSOR. With SQL code=" + sqlcodeDisplay);
        throw new Abend("HRAC");
    }

    private void failClose(int sqlcode) {
        String sqlcodeDisplay = String.format(Locale.ROOT, "%+09d", sqlcode);
        String freeform = "RAD010 -Failure when attempting to CLOSE DB2 "
                + "CURSOR (ACC-CUSOR). Check SQLCODESQLCODE=" + sqlcodeDisplay;
        linkDiagnostic("HRAC", sqlcode, freeform);
        ports.display("Failure when attempting to close the DB2 CURSOR"
                + " ACC-CURSOR. With SQL code=" + sqlcodeDisplay);
        checkStormDrain(sqlcode);
        throw new Abend("HRAC");
    }

    private void checkStormDrain(int sqlcode) {
        if (sqlcode == 923) {
            ports.display("INQACC: Check-For-Storm-Drain-DB2: Storm "
                    + "Drain condition (DB2 Connection lost ) "
                    + "has been met (" + String.format(Locale.ROOT, "%+09d", sqlcode) + ").");
        }
    }

    private void failHighest(int sqlcode) {
        String sqlcodeDisplay = String.format(Locale.ROOT, "%+09d", sqlcode);
        String ncsName = "HBNKACCT        ";
        linkDiagnostic("HNCS", sqlcode, "GLAD010 -ACCOUNT NCS " + ncsName
                + " CANNOT be accessed and DB2  SELECT failed. SQLCODE=" + sqlcodeDisplay);
        ports.display("INQACC - ACCOUNT NCS " + ncsName
                + " CANNOT BE ACCESSED AND DB2 SELECT FAILED. SQLCODE=" + sqlcodeDisplay);
        throw new Abend("HNCS");
    }

    private void linkDiagnostic(String code, int sqlcode, String freeform) {
        Context context = ports.context();
        ports.link("ABNDPROC", new Diagnostic(
                context.utime(), context.taskno(), context.applid(), context.tranid(),
                context.time().format(DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT)),
                context.time().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)),
                code, context.program(), context.resp(), context.resp2(), sqlcode,
                freeform + " ".repeat(600 - freeform.length())));
    }

    public record Commarea(
            String eye, String custno, String scode, String accno, String accType,
            BigDecimal intRate, String opened, String overdraft, String lastStmtDt,
            String nextStmtDt, BigDecimal availBal, BigDecimal actualBal,
            String success, Object pcb1Pointer) {}

    public record AccountRow(
            String eye, String custno, String scode, String accno, String accType,
            BigDecimal intRate, String opened, int overdraft, String lastStmtDt,
            String nextStmtDt, BigDecimal availBal, BigDecimal actualBal) {}

    public record Fetch(int sqlcode, AccountRow row) {}

    public record Context(
            long utime, String taskno, String applid, String tranid, String program,
            int resp, int resp2, LocalDateTime time) {}

    public record Diagnostic(
            long utime, String taskno, String applid, String tranid, String date,
            String time, String code, String program, int resp, int resp2,
            int sqlcode, String freeform) {}

    public interface Ports {
        Fetch selectHighest(String sortcode);

        int open(String sortcode, String accno);

        Fetch fetch();

        int close();

        Context context();

        void link(String program, Diagnostic diagnostic);

        void display(String message);
    }

    public static final class Abend extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String code;

        public Abend(String code) {
            super(code + " NODUMP CANCEL");
            this.code = code;
        }

        public String code() {
            return code;
        }

        public boolean cancel() {
            return true;
        }

        public boolean nodump() {
            return true;
        }
    }
}
