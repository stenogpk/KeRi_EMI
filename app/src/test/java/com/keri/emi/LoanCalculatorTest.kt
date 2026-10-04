package com.keri.emi

import org.junit.Assert.assertEquals
import org.junit.Test

class LoanCalculatorTest {
    @Test fun standardLoanProducesExpectedEmiAndDisbursal() {
        val result = calculateLoan(500000.0, 9.5, 60, 8000.0)
        assertEquals(10501.0, result.emi, 2.0)
        assertEquals(492000.0, result.disbursal, 0.01)
        assertEquals(8000.0, result.charges, 0.01)
    }

    @Test fun zeroInterestDividesPrincipalEvenly() {
        val result = calculateLoan(120000.0, 0.0, 12, 0.0)
        assertEquals(10000.0, result.emi, 0.001)
        assertEquals(0.0, result.interest, 0.001)
    }

    @Test fun amortizationScheduleClosesAtZeroBalance() {
        val result = calculateLoan(250000.0, 11.0, 36, 1500.0)
        assertEquals(0.0, result.years.last().balance, 0.01)
        assertEquals(3, result.years.size)
    }
}
