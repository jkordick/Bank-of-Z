package com.bankofz.inqacc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.bankofz.inqacc.Inqacc.AccountRow;
import com.bankofz.inqacc.Inqacc.CapturedAbend;
import com.bankofz.inqacc.Inqacc.Commarea;
import com.bankofz.inqacc.Inqacc.Context;
import com.bankofz.inqacc.Inqacc.Diagnostic;
import com.bankofz.inqacc.Inqacc.Fetch;
import com.bankofz.inqacc.Inqacc.Response;
import com.bankofz.inqacc.Inqacc.SqlDiagnostics;

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
        run("INQACC-T16", () -> ordinaryFailure("OPEN", -904));
        run("INQACC-T17", () -> ordinaryFailure("FETCH", -904));
        run("INQACC-T18", () -> ordinaryFailure("CLOSE", -904));
        run("INQACC-T19", InqaccAcceptanceTest::missingAccountCloseFailure);
        run("INQACC-T20", () -> ordinaryFailure("OPEN", 923));
        run("INQACC-T21", () -> ordinaryFailure("OPEN", -923));
        run("INQACC-T22", () -> ordinaryFailure("FETCH", 923));
        run("INQACC-T23", () -> ordinaryFailure("FETCH", -923));
        run("INQACC-T24", () -> ordinaryFailure("CLOSE", 923));
        run("INQACC-T25", () -> ordinaryFailure("CLOSE", -923));
        run("INQACC-T26", () -> ordinaryFailure("FETCH", -911));
        run("INQACC-T27", () -> ordinaryFailure("FETCH", -913));
        run("INQACC-T28", () -> highestFailure(-911));
        run("INQACC-T29", () -> highestFailure(-913));
        run("INQACC-T30", () -> highestFailure(100));
        run("INQACC-T31", () -> highestFailure(-904));
        run("INQACC-T32", () -> highestFailure(923));
        run("INQACC-T33", () -> ordinaryFailure("OPEN", 100));
        run("INQACC-T34", () -> ordinaryFailure("CLOSE", 100));
        run("INQACC-T35", () -> ordinaryFailure("FETCH", 1));
        run("INQACC-T36", () -> highestFailure(1));
        run("INQACC-T37", () -> ordinaryFailure("FETCH", -305));
        run("INQACC-T38", () -> highestFailure(-305));
        run("INQACC-T39", () -> stormHandler("AFCR", false));
        run("INQACC-T40", () -> stormHandler("AFCS", false));
        run("INQACC-T41", () -> stormHandler("AFCT", false));
        run("INQACC-T42", () -> stormHandler("AFCR", true));
        run("INQACC-T43", () -> stormHandler("AFCS", true));
        run("INQACC-T44", () -> stormHandler("AFCT", true));
        run("INQACC-T45", InqaccAcceptanceTest::deadlockHandler);
        run("INQACC-T46", InqaccAcceptanceTest::otherHandler);
        run("INQACC-T47", () -> ordinaryType(" X      ", false));
        run("INQACC-T48", InqaccAcceptanceTest::ordinaryDateBoundaries);
        if (failures != 0) {
            throw new AssertionError(failures + " of 48 INQACC acceptance tests failed");
        }
        System.out.println("48 INQACC acceptance tests passed");
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

    private static void ordinaryFailure(String site, int sqlcode) {
        Commarea input = caller("00000042");
        FakePorts ports = new FakePorts(List.of(row("987654", "00000042")));
        String freeform;
        String display;
        List<String> expectedEvents = new ArrayList<>();
        expectedEvents.add("OPEN:987654/00000042");
        switch (site) {
            case "OPEN" -> {
                ports.openSqlcode = sqlcode;
                freeform = "RAD010 -Failure when attempting to OPEN DB2 "
                        + "CURSOR. Check SQLCODE. SQLCODE=";
                display = "Failure when attempting to open DB2 CURSOR "
                        + "ACC-CURSOR. With SQL code=";
            }
            case "FETCH" -> {
                ports.fetchSqlcode = sqlcode;
                expectedEvents.add("FETCH");
                freeform = "FD010 -Failure when attempting to FETCH from "
                        + "DB2 CURSOR (ACC-CURSOR). Check SQLCODESQLCODE=";
                display = "Failure when attempting to FETCH from the DB2 "
                        + "CURSOR ACC-CURSOR. With SQL code=";
            }
            case "CLOSE" -> {
                ports.closeSqlcode = sqlcode;
                expectedEvents.addAll(List.of("FETCH", "CLOSE"));
                freeform = "RAD010 -Failure when attempting to CLOSE DB2 "
                        + "CURSOR (ACC-CUSOR). Check SQLCODESQLCODE=";
                display = "Failure when attempting to close the DB2 CURSOR"
                        + " ACC-CURSOR. With SQL code=";
            }
            default -> throw new AssertionError("Unknown failure site: " + site);
        }

        try {
            new Inqacc(ports).inquire(input);
            throw new AssertionError(site + " " + sqlcode + " must not return normally");
        } catch (Inqacc.Abend failure) {
            equal("HRAC", failure.code(), site + " abend code");
            equal(true, failure.cancel(), "CANCEL");
            equal(true, failure.nodump(), "NODUMP");
        }

        String code = String.format(java.util.Locale.ROOT, "%+09d", sqlcode);
        String storm = "DISPLAY:INQACC: Check-For-Storm-Drain-DB2: Storm "
                + "Drain condition (DB2 Connection lost ) has been met (" + code + ").";
        if (sqlcode == 923 && "FETCH".equals(site)) {
            expectedEvents.add(storm);
        }
        expectedEvents.addAll(List.of("CONTEXT", "LINK:ABNDPROC", "DISPLAY:" + display + code));
        if (sqlcode == 923 && !"FETCH".equals(site)) {
            expectedEvents.add(storm);
        }
        equal(expectedEvents, ports.events,
                "exact cursor and diagnostic order; no extra calls, retry or rollback");
        equal(List.of(new Diagnostic(
                CONTEXT.utime(), CONTEXT.taskno(), CONTEXT.applid(), CONTEXT.tranid(),
                "30/10/2026", "20:34:01", "HRAC", CONTEXT.program(), 17, 29, sqlcode,
                pad(freeform + code, 600))),
                ports.diagnostics, "exact " + site + " diagnostic and supplied context/clock");
        equal(caller("00000042"), input, "no output copy or success assignment");
        same(POINTER, input.pcb1Pointer(), "opaque pointer");
        assertShape(input);
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

    private static void stormHandler(String code, boolean rollbackFails) {
        Commarea input = unchangedSuccess(caller("00000042"));
        FakePorts ports = new FakePorts(List.of(row("987654", "00000042")));
        ports.capturedAbend = new CapturedAbend(code, null);
        ports.captureSite = "FETCH";
        ports.rollbackResponse = new Response(rollbackFails ? 16 : 0, 23);
        ports.rollbackContext = new Context(
                CONTEXT.utime(), CONTEXT.taskno(), CONTEXT.applid(), CONTEXT.tranid(),
                CONTEXT.program(), 51, 63, CONTEXT.time());
        List<String> expectedEvents = new ArrayList<>(List.of(
                "OPEN:987654/00000042", "FETCH",
                "DISPLAY:INQACC: Check-For-Storm-Drain-VSAM: Storm "
                        + "Drain condition (Abend " + code + ") has been met.",
                "ROLLBACK:CALLER-UNIT-OF-WORK"));

        if (rollbackFails) {
            expectAbend("HROL", () -> new Inqacc(ports).inquire(input));
            expectedEvents.addAll(List.of(
                    "CONTEXT", "LINK:ABNDPROC",
                    "DISPLAY:INQACC: Unable to perform Syncpoint "
                            + "Rollback. Possible Integrity issue "
                            + " following VSAM RLS abend. "
                            + " RESP CODE=+00000016 RESP2 CODE=+00000023"));
            equal(List.of(handlerDiagnostic("HROL", ports.rollbackContext,
                    "AH010 -Unable to perform SYNCPOINT ROLLBACK."
                            + " Possible integrity issue following VSAM RLS "
                            + " abend. EIBRESP=+00000051 RESP2=+00000063")),
                    ports.diagnostics, "HROL uses captured EIB codes, recorded SQLCODE zero");
            equal(List.of("pending caller work"), ports.callerWork,
                    "failed rollback must not report caller work as rolled back");
        } else {
            Commarea actual = new Inqacc(ports).inquire(input);
            equal(caller("00000042"), actual, "only success changes from Y to N");
            same(input.pcb1Pointer(), actual.pcb1Pointer(), "opaque pointer");
            assertShape(actual);
            equal(List.of(), ports.diagnostics, "normal rollback needs no diagnostic LINK");
            equal(List.of(), ports.callerWork, "rollback acts on participating caller work");
        }
        equal(expectedEvents, ports.events, "one rollback; no CLOSE, retry or handler reentry");
        equal(unchangedSuccess(caller("00000042")), input,
                "no output copy or premature success assignment");
        same(POINTER, input.pcb1Pointer(), "opaque pointer");
        assertShape(input);
    }

    private static void deadlockHandler() {
        SqlDiagnostics sql = new SqlDiagnostics(
                -911, "00000042", "40001", "LOCK RESOURCE   unused", 13,
                List.of("+000000011", "-000000022", "+000000033",
                        "-000000044", "+000000055", "-000000066"));
        FakePorts ports = new FakePorts(List.of(row("987654", "00000042")));
        ports.capturedAbend = new CapturedAbend("AD2Z", sql);
        ports.captureSite = "FETCH";
        Commarea input = caller("00000042");

        expectAbend("AD2Z", () -> new Inqacc(ports).inquire(input));

        equal(List.of(
                "OPEN:987654/00000042", "FETCH",
                "DISPLAY:DB2 DEADLOCK DETECTED IN INQACC, SQLCODE=-00000911",
                "DISPLAY:DB2 DEADLOCK FOR ACCOUNT 00000042",
                "DISPLAY:SQLSTATE=40001,SQLERRMC=LOCK RESOURCE"
                        + ",SQLERRD(1)=+000000011,SQLERRD(2)=-000000022"
                        + ",SQLERRD(3)=+000000033,SQLERRD(4)=-000000044"
                        + ",SQLERRD(5)=+000000055,SQLERRD(6)=-000000066",
                "CONTEXT", "LINK:ABNDPROC"), ports.events,
                "AD2Z displays full SQL diagnostics before LINK; no rollback or retry");
        equal(List.of(handlerDiagnostic("AD2Z", CONTEXT,
                "AH010 -WVS-STORM-DRAIN=N EIBRESP=+00000017 RESP2=+00000029")),
                ports.diagnostics, "displayed -911 is recorded as SQLCODE zero");
        equal(caller("00000042"), input, "no output or success assignment");
        equal(List.of("pending caller work"), ports.callerWork, "no explicit caller rollback");
        assertShape(input);
    }

    private static void otherHandler() {
        for (String site : List.of("OPEN", "FETCH", "CLOSE", "SELECT")) {
            String accno = "SELECT".equals(site) ? "99999999" : "00000042";
            Commarea input = caller(accno);
            FakePorts ports = new FakePorts(List.of(row("987654", "00000042")));
            ports.capturedAbend = new CapturedAbend("ASRA", null);
            ports.captureSite = site;

            expectAbend("ASRA", () -> new Inqacc(ports).inquire(input));

            List<String> expectedEvents = new ArrayList<>();
            if ("SELECT".equals(site)) {
                expectedEvents.add("SELECT-HIGHEST:987654");
            } else {
                expectedEvents.add("OPEN:987654/00000042");
                if (!"OPEN".equals(site)) {
                    expectedEvents.add("FETCH");
                }
                if ("CLOSE".equals(site)) {
                    expectedEvents.add("CLOSE");
                }
            }
            expectedEvents.addAll(List.of("CONTEXT", "LINK:ABNDPROC"));
            equal(expectedEvents, ports.events, "captured " + site + " abend reaches handler once");
            equal(List.of(handlerDiagnostic("ASRA", CONTEXT,
                    "AH010 -WVS-STORM-DRAIN=N EIBRESP=+00000017 RESP2=+00000029")),
                    ports.diagnostics, "exact unmatched-code diagnostic; no SQL/storm display");
            equal(caller(accno), input, "no output copy or success assignment");
            equal(List.of("pending caller work"), ports.callerWork, "no explicit caller rollback");
            same(POINTER, input.pcb1Pointer(), "opaque pointer");
            assertShape(input);
        }
    }

    private static Diagnostic handlerDiagnostic(String code, Context context, String freeform) {
        return new Diagnostic(
                context.utime(), context.taskno(), context.applid(), context.tranid(),
                "30/10/2026", "20:34:01", code, context.program(),
                context.resp(), context.resp2(), 0, pad(freeform, 600));
    }

    private static void expectAbend(String code, Runnable invocation) {
        try {
            invocation.run();
            throw new AssertionError(code + " must not return normally");
        } catch (Inqacc.Abend failure) {
            equal(code, failure.code(), "abend code");
            equal(true, failure.cancel(), "CANCEL");
            equal(true, failure.nodump(), "NODUMP");
        }
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
        private final List<String> callerWork = new ArrayList<>(List.of("pending caller work"));
        private CapturedAbend capturedAbend;
        private String captureSite;
        private Response rollbackResponse;
        private Context rollbackContext;
        private Context currentContext = CONTEXT;
        private String selectedSortcode;
        private String selectedAccno;
        private int openSqlcode;
        private int fetchSqlcode;
        private int closeSqlcode;
        private int highestSqlcode;

        private FakePorts(List<AccountRow> rows) {
            this.rows = rows;
        }

        @Override
        public Fetch selectHighest(String sortcode) {
            events.add("SELECT-HIGHEST:" + sortcode);
            capture("SELECT");
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
            capture("OPEN");
            selectedSortcode = sortcode;
            selectedAccno = accno;
            return openSqlcode;
        }

        @Override
        public Fetch fetch() {
            events.add("FETCH");
            capture("FETCH");
            if (fetchSqlcode != 0) {
                return new Fetch(fetchSqlcode, null);
            }
            return rows.stream()
                    .filter(r -> r.scode().equals(selectedSortcode) && r.accno().equals(selectedAccno))
                    .findFirst()
                    .map(r -> new Fetch(0, r))
                    .orElseGet(() -> new Fetch(100, null));
        }

        @Override
        public int close() {
            events.add("CLOSE");
            capture("CLOSE");
            return closeSqlcode;
        }

        private void capture(String site) {
            if (site.equals(captureSite)) {
                throw capturedAbend;
            }
        }

        @Override
        public Response rollbackCallerUnitOfWork() {
            events.add("ROLLBACK:CALLER-UNIT-OF-WORK");
            currentContext = Objects.requireNonNull(rollbackContext);
            if (rollbackResponse.resp() == 0) {
                callerWork.clear();
            }
            return rollbackResponse;
        }

        @Override
        public Context context() {
            events.add("CONTEXT");
            return currentContext;
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
