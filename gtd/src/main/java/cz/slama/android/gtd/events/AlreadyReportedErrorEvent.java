package cz.slama.android.gtd.events;

import retrofit.RetrofitError;

/**
 * Error that is posted when a non-network error event occurs in the {@link retrofit.RestAdapter}
 */
public class AlreadyReportedErrorEvent {
    private RetrofitError cause;

    public AlreadyReportedErrorEvent(RetrofitError cause) {
        this.cause = cause;
    }

    public RetrofitError getCause() {
        return cause;
    }
}
