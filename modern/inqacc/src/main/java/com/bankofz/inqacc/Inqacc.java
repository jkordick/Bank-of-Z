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
        if ("99999999".equals(input.accno())) {
            throw new UnsupportedOperationException("Highest-account mode is outside slice 1");
        }

        int openSqlcode = ports.open(SORTCODE, input.accno());
        if (openSqlcode != 0) {
            throw new UnsupportedOperationException("OPEN failure is outside slice 1: " + openSqlcode);
        }

        Fetch fetched = ports.fetch();
        if (fetched.sqlcode() != 0 && fetched.sqlcode() != 100) {
            throw new UnsupportedOperationException(
                    "FETCH failure is outside slice 1: " + fetched.sqlcode());
        }

        int closeSqlcode = ports.close();
        if (closeSqlcode != 0) {
            failClose(closeSqlcode);
        }

        if (fetched.sqlcode() == 100) {
            return withSuccess(input);
        }

        AccountRow row = Objects.requireNonNull(fetched.row(), "Successful FETCH must supply a row");
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

    private static Commarea withSuccess(Commarea c) {
        return new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), c.accType(), c.intRate(),
                c.opened(), c.overdraft(), c.lastStmtDt(), c.nextStmtDt(),
                c.availBal(), c.actualBal(), "Y", c.pcb1Pointer());
    }

    private static String displayDate(String date) {
        return date.substring(8, 10) + date.substring(5, 7) + date.substring(0, 4);
    }

    private void failClose(int sqlcode) {
        Context context = ports.context();
        String sqlcodeDisplay = String.format(Locale.ROOT, "%+09d", sqlcode);
        String freeform = "RAD010 -Failure when attempting to CLOSE DB2 "
                + "CURSOR (ACC-CUSOR). Check SQLCODESQLCODE=" + sqlcodeDisplay;
        ports.link("ABNDPROC", new Diagnostic(
                context.utime(), context.taskno(), context.applid(), context.tranid(),
                context.time().format(DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT)),
                context.time().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)),
                "HRAC", context.program(), context.resp(), context.resp2(), sqlcode,
                freeform + " ".repeat(600 - freeform.length())));
        ports.display("Failure when attempting to close the DB2 CURSOR"
                + " ACC-CURSOR. With SQL code=" + sqlcodeDisplay);
        if (sqlcode == 923) {
            ports.display("INQACC: Check-For-Storm-Drain-DB2: Storm "
                    + "Drain condition (DB2 Connection lost ) "
                    + "has been met (" + sqlcodeDisplay + ").");
        }
        throw new Abend("HRAC");
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
