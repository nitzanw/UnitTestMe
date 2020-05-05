package com.example.unittestme

import com.example.unittestme.service.Endpoint
import com.example.unittestme.service.Status
import com.example.unittestme.utils.SchedulerProvider
import com.example.unittestme.utils.TestSchedulerProvider
import com.example.unittestme.utils.TrampolineSchedulerProvider
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit

class CalculatorViewModelTest {

    private var endpoint: Endpoint = mock(Endpoint::class.java)
    private val testScheduler = TestScheduler()

    private lateinit var viewModel: CalculatorViewModel

    @BeforeEach
    internal fun setUp() {

    }

    @Test
    fun testAdd() {
        //test
        val result = viewModel.add(1, 7)


        //verify assumption
        assertEquals(8, result, "value was suppose to be 8, but was not")

        verify(endpoint, times(0)).sendEqualToPrevious()
    }


    @Test
    fun testSub() {
    }

    @Test
    fun testMulResultPositive() {
        val result = viewModel.mul(1, 8)

        //verify assumption
        verify(endpoint, times(1)).sendPositiveResults()

        assertEquals(8, result, "value was suppose to be 8, but was not")
    }

    @Test
    fun testMulResultZero() {
        val result = viewModel.mul(0, 8)

        //verify assumption
        verify(endpoint, times(1)).sendZeroResults()

        assertEquals(result,
                0, "value was suppose to be 0, but was not")
    }

    @Test
    fun `0) test getStatus with Success on normal scheduler`() {
        viewModel = CalculatorViewModel(endpoint, SchedulerProvider())
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.just(Endpoint.StatusResponse(Status.SUCCESS)))

        viewModel.getStatus()
        assertEquals(viewModel.currentStatus, Status.SUCCESS, "we did not get a SUCCESS status")

    }

    @Test
    fun `1) test getStatus with Success`() {
        viewModel = CalculatorViewModel(endpoint, TrampolineSchedulerProvider())
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.just(Endpoint.StatusResponse(Status.SUCCESS)))

        viewModel.getStatus()
        assertEquals(viewModel.currentStatus, Status.SUCCESS, "we did not get a SUCCESS status")

    }

    @Test
    fun `2) test getStatusAfterDelay with Success`() {
        viewModel = CalculatorViewModel(endpoint, TrampolineSchedulerProvider()) //<- synchronous Let's get back to the presentation
        //given
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.just(Endpoint.StatusResponse(Status.SUCCESS)))

        viewModel.getStatusAfterDelay()
        //?
        assertEquals(null, viewModel.currentStatus, "the status was already initialized")

        assertEquals(Status.SUCCESS, viewModel.currentStatus, "we did not get a success status")
        verify(endpoint, times(1)).getStatus(viewModel.schedulerProvider.io())
    }

    @Test
    fun `3) test getStatusAfterDelay with Exception`() {
        viewModel = CalculatorViewModel(endpoint, TestSchedulerProvider(testScheduler))
        //given
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.error(Exception()))

        viewModel.getStatusAfterDelay()
        assertEquals(null, viewModel.currentStatus, "the status was already initialized")
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        assertEquals( Status.FAIL, viewModel.currentStatus,"we did not get a FAIL status")
        verify(endpoint, times(2)).getStatus(viewModel.schedulerProvider.io())
    }

    @Test
    fun `4) test getStatusWithInterval with Processing`(){
        viewModel = CalculatorViewModel(endpoint, TestSchedulerProvider(testScheduler))
        //given
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.just(Endpoint.StatusResponse(Status.PROCESSING)))

        viewModel.getStatusWithInterval()

        assertEquals(null, viewModel.currentStatus, "the status was already initialized")
        testScheduler.advanceTimeBy(15 , TimeUnit.SECONDS)
        assertEquals(Status.PROCESSING, viewModel.currentStatus, "we did not get a processing status")

        verify(endpoint, times(1)).getStatus(viewModel.schedulerProvider.io())
    }

    @Test
    fun `5) test getStatusWithInterval 8 times`(){
        viewModel = CalculatorViewModel(endpoint, TestSchedulerProvider(testScheduler))
        //given
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.just(Endpoint.StatusResponse(Status.PROCESSING)))

       val testObserver =  TestObserver<Endpoint.StatusResponse>()
        viewModel.getStatusObservable().observeOn(testScheduler).subscribe(testObserver)

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        testObserver.assertNotTerminated()

        testScheduler.advanceTimeBy(15 * 4, TimeUnit.SECONDS)

        testObserver.assertNotTerminated()

        testScheduler.advanceTimeBy(15 * 4, TimeUnit.SECONDS)

        testObserver.assertValues(Endpoint.StatusResponse(Status.PROCESSING),Endpoint.StatusResponse(Status.PROCESSING),Endpoint.StatusResponse(Status.PROCESSING),
                Endpoint.StatusResponse(Status.PROCESSING),Endpoint.StatusResponse(Status.PROCESSING),Endpoint.StatusResponse(Status.PROCESSING),Endpoint.StatusResponse(Status.PROCESSING),
                Endpoint.StatusResponse(Status.PROCESSING))

        testObserver.assertComplete()

    }

    @Test
    fun `5) test getStatusWithInterval 1 success`(){
        viewModel = CalculatorViewModel(endpoint, TestSchedulerProvider(testScheduler))
        //given
        `when`(endpoint.getStatus(viewModel.schedulerProvider.io())).thenReturn(Observable.just(Endpoint.StatusResponse(Status.SUCCESS)))

        val testObserver =  TestObserver<Endpoint.StatusResponse>()
        viewModel.getStatusObservable().observeOn(testScheduler).subscribe(testObserver)

        testScheduler.advanceTimeBy(15, TimeUnit.SECONDS)
        assertEquals(Status.SUCCESS, viewModel.currentStatus, "we did not get a processing status")
        testObserver.assertValues(Endpoint.StatusResponse(Status.SUCCESS))
        testObserver.assertComplete()

    }
}