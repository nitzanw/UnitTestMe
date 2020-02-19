package com.example.unittestme

import android.content.SharedPreferences
import com.example.unittestme.Constants.ADDED
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runners.Parameterized

class CalculatorViewModelTest {

    private var mockSp: SharedPreferences = mock()
    private var mockMathServer: MathServer = mock()

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        viewModel = CalculatorViewModel(mockSp, mockMathServer)
    }



    @Test
    fun testAdd() {
        //given
        whenever(mockSp.getInt(ADDED,0)).thenReturn(8)

        //test
        val result = viewModel.add(1, 8)


        assertEquals("value was suppose to be 9, but was not", result,
                9)

        verify(mockMathServer, times(1)).sendEqualToPrevious()
    }


    @Test
    fun testSub() {
    }

    @Test
    fun testMulResultPositive() {
        val result = viewModel.mul(1, 8)

        verify(mockMathServer, times(1)).sendPositiveResults()
        assertEquals("value was suppose to be 8, but was not", result,
                8)
    }

    @Test
    fun testMulResultZero() {
    }
}