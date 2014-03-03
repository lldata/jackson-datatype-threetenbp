package com.fasterxml.jackson.datatype.threetenbp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TestDecimalUtils
{
    @Before
    public void startUp()
    {

    }

    @After
    public void cleanUp()
    {

    }

    @Test
    public void testToDecimal01()
    {
        String decimal = DecimalUtils.toDecimal(0, 0);

        assertEquals("The returned decimal is not correct.", "0.000000000", decimal);
    }

    @Test
    public void testToDecimal02()
    {
        String decimal = DecimalUtils.toDecimal(15, 72);

        assertEquals("The returned decimal is not correct.", "15.000000072", decimal);
    }

    @Test
    public void testToDecimal03()
    {
        String decimal = DecimalUtils.toDecimal(19827342231L, 192837465);

        assertEquals("The returned decimal is not correct.", "19827342231.192837465", decimal);
    }

    @Test
    public void testToDecimal04()
    {
        String decimal = DecimalUtils.toDecimal(19827342231L, 0);

        assertEquals("The returned decimal is not correct.", "19827342231.000000000", decimal);
    }

    @Test
    public void testToDecimal05()
    {
        String decimal = DecimalUtils.toDecimal(19827342231L, 999999999);

        assertEquals("The returned decimal is not correct.", "19827342231.999999999", decimal);
    }

    @Test
    public void testExtractNanosecondDecimal01()
    {
        BigDecimal value = new BigDecimal("0");

        long seconds = value.longValue();
        assertEquals("The second part is not correct.", 0L, seconds);

        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value,  seconds);
        assertEquals("The nanosecond part is not correct.", 0, nanoseconds);
    }

    @Test
    public void testExtractNanosecondDecimal02()
    {
        BigDecimal value = new BigDecimal("15.000000072");

        long seconds = value.longValue();
        assertEquals("The second part is not correct.", 15L, seconds);

        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value,  seconds);
        assertEquals("The nanosecond part is not correct.", 72, nanoseconds);
    }

    @Test
    public void testExtractNanosecondDecimal03()
    {
        BigDecimal value = new BigDecimal("15.72");

        long seconds = value.longValue();
        assertEquals("The second part is not correct.", 15L, seconds);

        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value,  seconds);
        assertEquals("The nanosecond part is not correct.", 720000000, nanoseconds);
    }

    @Test
    public void testExtractNanosecondDecimal04()
    {
        BigDecimal value = new BigDecimal("19827342231.192837465");

        long seconds = value.longValue();
        assertEquals("The second part is not correct.", 19827342231L, seconds);

        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value,  seconds);
        assertEquals("The nanosecond part is not correct.", 192837465, nanoseconds);
    }

    @Test
    public void testExtractNanosecondDecimal05()
    {
        BigDecimal value = new BigDecimal("19827342231");

        long seconds = value.longValue();
        assertEquals("The second part is not correct.", 19827342231L, seconds);

        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value,  seconds);
        assertEquals("The nanosecond part is not correct.", 0, nanoseconds);
    }

    @Test
    public void testExtractNanosecondDecimal06()
    {
        BigDecimal value = new BigDecimal("19827342231.999999999");

        long seconds = value.longValue();
        assertEquals("The second part is not correct.", 19827342231L, seconds);

        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value,  seconds);
        assertEquals("The nanosecond part is not correct.", 999999999, nanoseconds);
    }
}
