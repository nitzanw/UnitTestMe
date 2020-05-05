package com.example.unittestme

import androidx.annotation.VisibleForTesting
import com.example.unittestme.service.Endpoint
import com.example.unittestme.service.Status
import com.example.unittestme.utils.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class CalculatorViewModel internal constructor(@VisibleForTesting val endpoint: Endpoint,
                                               @VisibleForTesting val schedulerProvider: BaseSchedulerProvider) {
    var currentStatus: Status? = null

    /**
     * Addition operation
     */
    fun add(firstOperand: Int, secondOperand: Int): Int {
        val result = firstOperand + secondOperand
        if (result > 10) {
            endpoint.sendEqualToPrevious()
        }
        return result
    }

    /**
     * Substract operation
     */
    fun sub(firstOperand: Int, secondOperand: Int): Int {
        return firstOperand - secondOperand
    }

    /**
     * Multiply operation
     */
    fun mul(firstOperand: Int, secondOperand: Int): Int {
        val result = firstOperand * secondOperand
        if (result > 0) {
            endpoint.sendPositiveResults()
        } else if (result == 0) {
            endpoint.sendZeroResults()
        }
        return result
    }

    @VisibleForTesting
    fun getStatus(): Disposable? {
        return endpoint.getStatus(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ response ->
                    currentStatus = response.status
                }, { _ ->
                    currentStatus = Status.FAIL
                })
    }

    @VisibleForTesting
    fun getStatusAfterDelay(): Disposable? {
        val statusCallDelay = 5L
        return Observable.timer(statusCallDelay, TimeUnit.SECONDS, schedulerProvider.computation())
                .switchMap {
                    endpoint.getStatus(schedulerProvider.io())
                            .onErrorReturn {
                                //we need this onErrorReturn s.t we have a 2nd call
                                Endpoint.StatusResponse(Status.FAIL)
                            }
                            .flatMap {
                                if (it.status != Status.SUCCESS) {
                                    //if the api call fails the first time - make *one* more call
                                    endpoint.getStatus(schedulerProvider.io())
                                } else {
                                    Observable.just(it)
                                }
                            }
                }
                .observeOn(schedulerProvider.ui())
                .subscribe({ response ->
                    currentStatus = response.status
                    if (currentStatus == Status.PROCESSING) {
                        goToProcessingScreen()
                    }
                }, { _ ->
                    currentStatus = Status.FAIL
                })
    }

    @VisibleForTesting
    fun goToProcessingScreen() {

    }

    @VisibleForTesting
    fun getStatusObservable(): Observable<Endpoint.StatusResponse> {
        val intervalObservable = Observable.interval(15, TimeUnit.SECONDS, schedulerProvider.computation()).take(8).doOnNext {
        }
        val onBoardingStatusObservable = endpoint.getStatus(schedulerProvider.io())
                .onErrorReturn {

                    Endpoint.StatusResponse(Status.FAIL)
                }

        return intervalObservable.switchMap {
            onBoardingStatusObservable
        }.takeUntil { response ->
            currentStatus = response.status
            response.status == Status.SUCCESS
        }
    }

    @VisibleForTesting
    fun getStatusWithInterval(): Disposable? {

        return getStatusObservable().observeOn(schedulerProvider.ui())
                .subscribe({},
                        {
                            //no op
                        },
                        {//onComplete
                            if (currentStatus == Status.FAIL) {
                                // something bad
                            } else {
                                //something ok
                            }
                        })
    }

}