package com.bankofz.inqacc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.bankofz.inqacc.Inqacc.AccountRow;
import com.bankofz.inqacc.Inqacc.Commarea;
import com.bankofz.inqacc.Inqacc.Context;
import com.bankofz.inqacc.Inqacc.Diagnostic;
import com.bankofz.inqacc.Inqacc.Fetch;

public final class InqaccAcceptanceTest {
    private static int failures;
    private static final Object POINTER = new Object();
    private static final Context CONTEXT = new Context(
            123456789012345L, "0042", "BANKZ   ", "OMEN", "INQACC  ",
            17, 29, LocalDateTime.of(2026, 10, 30, 20, 34, 1));

    public static void main(String[] args) {
        run("INQACC-T01", InqaccAcceptanceTest::ordinaryInquiry);
        run("INQACC-T02", InqaccAcceptanceTest::highestAccount);
        run("INQACC-T03", InqaccAcceptanceTest::missingAccount);
        run("INQACC-T04", () -> ordinaryType("        ", true));
        run("INQACC-T05", () -> ordinaryType("\0".repeat(8), true));
        run("INQACC-T06", () -> highestSuppressedType("        "));
        run("INQACC-T07", () -> highestSuppressedType("\0".repeat(8)));
        run("INQACC-T08", () -> ordinaryType("MYSTERY!", false));
        run("INQACC-T09", InqaccAcceptanceTest::ordinaryEyecatcher);
        run("INQACC-T10", InqaccAcceptanceTest::ordinaryNumericLimits);
        run("INQACC-T11", InqaccAcceptanceTest::highestBalanceLimits);
        run("INQACC-T12", InqaccAcceptanceTest::ordinaryZeroAndNegativeCent);
        run("INQACC-T13", () -> ordinaryBoundary("00000000"));
        run("INQACC-T14", () -> ordinaryBoundary("99999998"));
        run("INQACC-T15", InqaccAcceptanceTest::highestSentinelRow);
        run("INQACC-T19", InqaccAcceptanceTest::missingAccountCloseFailure);
        run("INQACC-T28", () -> highestFailure(-911));
        run("INQACC-T29", () -> highestFailure(-913));
        run("INQACC-T30", () -> highestFailure(100));
        run("INQACC-T31", () -> highestFailure(-904));
        run("INQACC-T32", () -> highestFailure(923));
        run("INQACC-T36", () -> highestFailure(1));
        run("INQACC-T38", () -> highestFailure(-305));
        run("INQACC-T47", () -> ordinaryType(" X      ", false));
        run("INQACC-T48", InqaccAcceptanceTest::ordinaryDateBoundaries);
        if (failures != 0) {
            throw new AssertionError(failures + " of 25 INQACC acceptance tests failed");
        }
        System.out.println("25 INQACC acceptance tests passed");
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

    private static void ordinaryType(String type, boolean suppressed) {
        AccountRow r = row("987654", "00000042");
        AccountRow selected = new AccountRow(
                r.eye(), r.custno(), r.scode(), r.accno(), type, r.intRate(),
                r.opened(), r.overdraft(), r.lastStmtDt(), r.nextStmtDt(),
                r.availBal(), r.actualBal());
        Commarea c = populated("00000042");
        Commarea expected = suppressed ? unchangedSuccess(caller("00000042")) : new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), type, c.intRate(),
                c.opened(), c.overdraft(), c.lastStmtDt(), c.nextStmtDt(),
                c.availBal(), c.actualBal(), "Y", POINTER);

        assertOrdinary(selected, expected);
    }

    private static void ordinaryEyecatcher() {
        AccountRow r = row("987654", "00000042");
        Commarea c = populated("00000042");

        assertOrdinary(new AccountRow(
                "TEST", r.custno(), r.scode(), r.accno(), r.accType(), r.intRate(),
                r.opened(), r.overdraft(), r.lastStmtDt(), r.nextStmtDt(),
                r.availBal(), r.actualBal()), new Commarea(
                "TEST", c.custno(), c.scode(), c.accno(), c.accType(), c.intRate(),
                c.opened(), c.overdraft(), c.lastStmtDt(), c.nextStmtDt(),
                c.availBal(), c.actualBal(), "Y", POINTER));
    }

    private static void ordinaryNumericLimits() {
        ordinaryAmounts("9999999999.99", "-9999999999.99", "9999.99",
                99999999, "99999999");
    }

    private static void ordinaryZeroAndNegativeCent() {
        ordinaryAmounts("0.00", "-0.01", "0.00", 0, "00000000");
    }

    private static void ordinaryAmounts(
            String available, String actual, String interest, int overdraft,
            String expectedOverdraft) {
        AccountRow r = row("987654", "00000042");
        Commarea c = populated("00000042");

        assertOrdinary(new AccountRow(
                r.eye(), r.custno(), r.scode(), r.accno(), r.accType(), decimal(interest),
                r.opened(), overdraft, r.lastStmtDt(), r.nextStmtDt(),
                decimal(available), decimal(actual)), new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), c.accType(), decimal(interest),
                c.opened(), expectedOverdraft, c.lastStmtDt(), c.nextStmtDt(),
                decimal(available), decimal(actual), "Y", POINTER));
    }

    private static void ordinaryDateBoundaries() {
        AccountRow r = row("987654", "00000042");
        Commarea c = populated("00000042");

        assertOrdinary(new AccountRow(
                r.eye(), r.custno(), r.scode(), r.accno(), r.accType(), r.intRate(),
                "2024-02-29", r.overdraft(), "2026-12-31", "2027-01-01",
                r.availBal(), r.actualBal()), new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), c.accType(), c.intRate(),
                "29022024", c.overdraft(), "31122026", "01012027",
                c.availBal(), c.actualBal(), "Y", POINTER));
    }

    private static void assertOrdinary(AccountRow selected, Commarea expected) {
        Commarea input = caller("00000042");
        FakePorts ports = new FakePorts(List.of(selected));

        Commarea actual = new Inqacc(ports).inquire(input);

        equal(expected, actual, "all ordinary-account fields");
        equal(caller("00000042"), input, "caller snapshot");
        same(input.pcb1Pointer(), actual.pcb1Pointer(), "opaque pointer");
        equal(ordinaryEvents("00000042"), ports.events, "one ordered cursor lifecycle");
        equal(List.of(), ports.diagnostics, "no diagnostic LINK");
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

    private static void highestAccount() {
        AccountRow lower = new AccountRow(
                "ACCT", "0000000123", "987654", "00000010", "CURRENT ",
                decimal("3.25"), "2026-10-30", 250, "2026-09-30", "2026-10-31",
                decimal("-12.34"), decimal("56.78"));
        FakePorts ports = new FakePorts(List.of(
                lower, row("111111", "99999998"), row("987654", "00000042")));

        assertHighest(ports, populated("00000042"));
    }

    private static void highestSuppressedType(String type) {
        AccountRow r = row("987654", "00000042");
        AccountRow suppressed = new AccountRow(
                r.eye(), r.custno(), r.scode(), r.accno(), type, r.intRate(),
                r.opened(), r.overdraft(), r.lastStmtDt(), r.nextStmtDt(),
                r.availBal(), r.actualBal());
        FakePorts ports = new FakePorts(List.of(
                row("987654", "00000010"), suppressed));

        assertHighest(ports, unchangedSuccess(caller("99999999")));
    }

    private static void highestBalanceLimits() {
        AccountRow r = row("987654", "00000042");
        FakePorts ports = new FakePorts(List.of(new AccountRow(
                r.eye(), r.custno(), r.scode(), r.accno(), r.accType(), r.intRate(),
                r.opened(), r.overdraft(), r.lastStmtDt(), r.nextStmtDt(),
                decimal("-9999999999.99"), decimal("9999999999.99"))));
        Commarea c = populated("00000042");

        assertHighest(ports, new Commarea(
                c.eye(), c.custno(), c.scode(), c.accno(), c.accType(), c.intRate(),
                c.opened(), c.overdraft(), c.lastStmtDt(), c.nextStmtDt(),
                decimal("-9999999999.99"), decimal("9999999999.99"), "Y", POINTER));
    }

    private static void highestSentinelRow() {
        FakePorts ports = new FakePorts(List.of(
                row("987654", "00000042"), row("987654", "99999999")));

        assertHighest(ports, populated("99999999"));
    }

    private static void assertHighest(FakePorts ports, Commarea expected) {
        Commarea input = caller("99999999");

        Commarea actual = new Inqacc(ports).inquire(input);

        equal(expected, actual, "all highest-account fields");
        equal(caller("99999999"), input, "caller snapshot");
        same(input.pcb1Pointer(), actual.pcb1Pointer(), "opaque pointer");
        equal(List.of("SELECT-HIGHEST:987654"), ports.events,
                "one configured-sort SELECT; no cursor, retry or fallback");
        equal(List.of(), ports.diagnostics, "no diagnostic LINK");
        assertShape(actual);
    }

    private static void highestFailure(int sqlcode) {
        Commarea input = caller("99999999");
        FakePorts ports = new FakePorts(List.of(row("111111", "99999998")));
        if (sqlcode != 100) {
            ports.highestSqlcode = sqlcode;
        }

        try {
            new Inqacc(ports).inquire(input);
            throw new AssertionError("SELECT " + sqlcode + " must not return normally");
        } catch (Inqacc.Abend failure) {
            equal("HNCS", failure.code(), "SELECT abend code");
            equal(true, failure.cancel(), "CANCEL");
            equal(true, failure.nodump(), "NODUMP");
        }

        String code = String.format(java.util.Locale.ROOT, "%+09d", sqlcode);
        equal(caller("99999999"), input, "no output copy or success assignment");
        same(POINTER, input.pcb1Pointer(), "opaque pointer");
        equal(List.of(
                "SELECT-HIGHEST:987654", "CONTEXT", "LINK:ABNDPROC",
                "DISPLAY:INQACC - ACCOUNT NCS HBNKACCT        "
                        + " CANNOT BE ACCESSED AND DB2 SELECT FAILED. SQLCODE=" + code),
                ports.events, "one SELECT, LINK before DISPLAY; no cursor, storm check or retry");
        equal(List.of(new Diagnostic(
                CONTEXT.utime(), CONTEXT.taskno(), CONTEXT.applid(), CONTEXT.tranid(),
                "30/10/2026", "20:34:01", "HNCS", CONTEXT.program(), 17, 29, sqlcode,
                pad("GLAD010 -ACCOUNT NCS HBNKACCT        "
                        + " CANNOT be accessed and DB2  SELECT failed. SQLCODE=" + code, 600))),
                ports.diagnostics, "exact SELECT diagnostic and supplied context/clock");
        assertShape(input);
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
        try {
            test.run();
            System.out.println("PASS " + id);
        } catch (AssertionError | RuntimeException failure) {
            failures++;
            System.err.println("FAIL " + id + ": " + failure);
        }
    }

    private static final class FakePorts implements Inqacc.Ports {
        private final List<AccountRow> rows;
        private final List<String> events = new ArrayList<>();
        private final List<Diagnostic> diagnostics = new ArrayList<>();
        private String selectedSortcode;
        private String selectedAccno;
        private int closeSqlcode;
        private int highestSqlcode;

        private FakePorts(List<AccountRow> rows) {
            this.rows = rows;
        }

        @Override
        public Fetch selectHighest(String sortcode) {
            events.add("SELECT-HIGHEST:" + sortcode);
            if (highestSqlcode != 0) {
                return new Fetch(highestSqlcode, null);
            }
            return rows.stream()
                    .filter(r -> r.scode().equals(sortcode))
                    .max(Comparator.comparing(AccountRow::accno))
                    .map(r -> new Fetch(0, r))
                    .orElseGet(() -> new Fetch(100, null));
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
