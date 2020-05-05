package com.example.unittestme.service

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import io.reactivex.Observable
import io.reactivex.Scheduler

@VisibleForTesting
public class Endpoint {
    private val service = FakeRetrofitService()
    @VisibleForTesting
    fun getStatus(scheduler: Scheduler) : Observable<StatusResponse> {
        print("getStatus")
       return service.getStatus().subscribeOn(scheduler)
//            service.getStatus().subscribeOn(Schedulers.io())
    }

    @VisibleForTesting
    public fun sendPositiveResults() {}

    @VisibleForTesting
    public fun sendZeroResults() {}

    @VisibleForTesting
    public fun sendEqualToPrevious() {}

    @Keep
    data class StatusResponse(val status: Status)
}

class FakeRetrofitService{
    fun getStatus(): Observable<Endpoint.StatusResponse> {
        return Observable.just(Endpoint.StatusResponse(Status.SUCCESS))
    }

}
enum class Status {
    SUCCESS,
    FAIL,
    PROCESSING
}