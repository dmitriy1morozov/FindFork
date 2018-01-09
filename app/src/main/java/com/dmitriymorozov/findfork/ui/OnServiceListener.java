package com.dmitriymorozov.findfork.ui;

public interface OnServiceListener {
		void onNetworkJobsFinished();
		void onNetworkError(int code, String errorType, String errorDetail);
}
