package com.bankofz.inqacc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.bankofz.inqacc.Inqacc.AccountRow;
import com.bankofz.inqacc.Inqacc.Commarea;
import com.bankofz.inqacc.Inqacc.Context;
import com.bankofz.inqacc.Inqacc.Diagnostic;
import com.bankofz.inqacc.Inqacc.Fetch;

public final class InqaccAcceptanceTest {
    private static final Object POINTER = new Object();
    private static final Context CONTEXT = new Context(
            123456789012345L, "0042", "BANKZ   ", "OMEN", "INQACC  ",
            17, 29, LocalDateTime.of(2026, 10, 30, 20, 34, 1));

    public static void main(String[] args) {
        run("INQACC-T01", InqaccAcceptanceTest::ordinaryInquiry);
        run("INQACC-T03", InqaccAcceptanceTest::missingAccount);
        run("INQACC-T13", () -> ordinaryBoundary("00000000"));
        run("INQACC-T14", () -> ordinaryBoundary("99999998"));
        run("INQACC-T19", InqaccAcceptanceTest::missingAccountCloseFailure);
        System.out.println("5 INQACC acceptance tests passed");
    }

    private static void ordinaryInquiry() {
        Commarea input = caller("00000042");
        AccountRow selected = row("987654", "00000042");
        FakePorts ports = new FakePorts(List.of(
                row("111111", "00000042"), row("987654", "00000041"), selected));

        Commarea actual = new Inqacc(ports).inquire(input);

        equal(populated("00000042"), actual, "all retrieved fields and date order");
        same(POINTER, actual.pcb1Pointer(), "opaque pointer");
        equal(caller("00000042"), input, "caller snapshot");
        equal(ordinaryEvents("00000042"), ports.events, "one ordered cursor lifecycle");
        equal(List.of(), ports.diagnostics, "no diagnostic LINK");
        assertShape(actual);
    }

    private static void missingAccount() {
        Commarea input = caller("00000042");
        FakePorts ports = new FakePorts(List.of(
                row("111111", "00000042"), row("987654", "00000041")));

        Commarea actual = new Inqacc(ports).inquire(input);

        equal(unchangedSuccess(input), actual, "only success changes on FETCH +100");
        same(input.pcb1Pointer(), actual.pcb1Pointer(), "opaque pointer");
        equal(ordinaryEvents("00000042"), ports.events, "CLOSE follows FETCH +100");
        equal(List.of(), ports.diagnostics, "missing account is not diagnosed");
        assertShape(actual);
    }

    private static void ordinaryBoundary(String accno) {
        FakePorts ports = new FakePorts(List.of(row("987654", accno)));

        Commarea actual = new Inqacc(ports).inquire(caller(accno));

        equal(populated(accno), actual, "ordinary boundary account");
        equal(ordinaryEvents(accno), ports.events, "not highest-account mode");
        same(POINTER, actual.pcb1Pointer(), "opaque pointer");
        assertShape(actual);
    }

    private static void missingAccountCloseFailure() {
        Commarea input = caller("00000042");
        FakePorts ports = new FakePorts(List.of());
        ports.closeSqlcode = -904;

        try {
            new Inqacc(ports).inquire(input);
            throw new AssertionError("CLOSE -904 must not return success after FETCH +100");
        } catch (Inqacc.Abend failure) {
            equal("HRAC", failure.code(), "abend code");
            equal(true, failure.cancel(), "CANCEL");
            equal(true, failure.nodump(), "NODUMP");
        }

        equal(caller("00000042"), input, "no output copy or success assignment");
        equal(List.of(
                "OPEN:987654/00000042", "FETCH", "CLOSE", "CONTEXT",
                "LINK:ABNDPROC",
                "DISPLAY:Failure when attempting to close the DB2 CURSOR"
                        + " ACC-CURSOR. With SQL code=-00000904"),
                ports.events, "diagnostic order; no retry, rollback or further cursor call");
        equal(List.of(new Diagnostic(
                CONTEXT.utime(), CONTEXT.taskno(), CONTEXT.applid(), CONTEXT.tranid(),
                "30/10/2026", "20:34:01", "HRAC", CONTEXT.program(),
                17, 29, -904,
                pad("RAD010 -Failure when attempting to CLOSE DB2 "
                        + "CURSOR (ACC-CUSOR). Check SQLCODESQLCODE=-00000904", 600))),
                ports.diagnostics, "exact CLOSE diagnostic, including source spelling");
    }

    private static Commarea caller(String accno) {
        return new Commarea(
                "OLD!", "0000000007", "111111", accno, "SAVING  ",
                decimal("1.23"), "01012020", "00000017", "01022020", "01032020",
                decimal("12.34"), decimal("-56.78"), "N", POINTER);
    }

    private static Commarea populated(String accno) {
        return new Commarea(
                "ACCT", "0000000123", "987654", accno, "CURRENT ",
                decimal("3.25"), "02012026", "00000250", "30092026", "31102026",
                decimal("-12.34"), decimal("56.78"), "Y", POINTER);
    }

    private static Commarea unchangedSuccess(Commarea c) {
        return new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), c.accType(), c.intRate(),
                c.opened(), c.overdraft(), c.lastStmtDt(), c.nextStmtDt(),
                c.availBal(), c.actualBal(), "Y", c.pcb1Pointer());
    }

    private static AccountRow row(String scode, String accno) {
        return new AccountRow(
                "ACCT", "0000000123", scode, accno, "CURRENT ", decimal("3.25"),
                "2026-01-02", 250, "2026-09-30", "2026-10-31",
                decimal("-12.34"), decimal("56.78"));
    }

    private static List<String> ordinaryEvents(String accno) {
        return List.of("OPEN:987654/" + accno, "FETCH", "CLOSE");
    }

    private static void assertShape(Commarea c) {
        equal(List.of(
                "eye", "custno", "scode", "accno", "accType", "intRate", "opened",
                "overdraft", "lastStmtDt", "nextStmtDt", "availBal", "actualBal",
                "success", "pcb1Pointer"),
                Arrays.stream(Commarea.class.getRecordComponents())
                        .map(component -> component.getName()).toList(),
                "complete logical COMMAREA; no added fail-code field");
        equal(4, c.eye().length(), "INQACC-EYE width");
        equal(10, c.custno().length(), "INQACC-CUSTNO width");
        equal(6, c.scode().length(), "INQACC-SCODE width");
        equal(8, c.accno().length(), "INQACC-ACCNO width");
        equal(8, c.accType().length(), "INQACC-ACC-TYPE width");
        equal(8, c.opened().length(), "INQACC-OPENED width");
        equal(8, c.overdraft().length(), "INQACC-OVERDRAFT width");
        equal(8, c.lastStmtDt().length(), "INQACC-LAST-STMT-DT width");
        equal(8, c.nextStmtDt().length(), "INQACC-NEXT-STMT-DT width");
        equal(1, c.success().length(), "INQACC-SUCCESS width");
        equal(2, c.intRate().scale(), "exact interest scale");
        equal(2, c.availBal().scale(), "exact available-balance scale");
        equal(2, c.actualBal().scale(), "exact actual-balance scale");
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private static String pad(String value, int width) {
        return value + " ".repeat(width - value.length());
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void same(Object expected, Object actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": identity changed");
        }
    }

    private static void run(String id, Runnable test) {
        test.run();
        System.out.println("PASS " + id);
    }

    private static final class FakePorts implements Inqacc.Ports {
        private final List<AccountRow> rows;
        private final List<String> events = new ArrayList<>();
        private final List<Diagnostic> diagnostics = new ArrayList<>();
        private String selectedSortcode;
        private String selectedAccno;
        private int closeSqlcode;

        private FakePorts(List<AccountRow> rows) {
            this.rows = rows;
        }

        @Override
        public int open(String sortcode, String accno) {
            events.add("OPEN:" + sortcode + "/" + accno);
            selectedSortcode = sortcode;
            selectedAccno = accno;
            return 0;
        }

        @Override
        public Fetch fetch() {
            events.add("FETCH");
            return rows.stream()
                    .filter(r -> r.scode().equals(selectedSortcode) && r.accno().equals(selectedAccno))
                    .findFirst()
                    .map(r -> new Fetch(0, r))
                    .orElseGet(() -> new Fetch(100, null));
        }

        @Override
        public int close() {
            events.add("CLOSE");
            return closeSqlcode;
        }

        @Override
        public Context context() {
            events.add("CONTEXT");
            return CONTEXT;
        }

        @Override
        public void link(String program, Diagnostic diagnostic) {
            events.add("LINK:" + program);
            diagnostics.add(diagnostic);
        }

        @Override
        public void display(String message) {
            events.add("DISPLAY:" + message);
        }
    }
}
